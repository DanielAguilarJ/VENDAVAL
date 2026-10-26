package mx.vendaval.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import mx.vendaval.core.data.dto.SpaceDto
import mx.vendaval.core.data.local.VendavalJson

/**
 * Live `INSERT` feed for the catalogue, mirroring the web portal's realtime
 * subscription so a venue added on one client appears on the others without a
 * manual refresh.
 *
 * Speaks the Phoenix channel protocol that Supabase Realtime is built on
 * directly over a Ktor WebSocket. Doing it by hand rather than pulling in the
 * full `supabase-kt` stack keeps the dependency graph small and, more usefully,
 * keeps the failure behaviour explicit: this feed is strictly an enhancement, so
 * every error path degrades to "no live updates" and never to a broken app.
 */
class SupabaseRealtimeSource(
    private val client: HttpClient,
    private val config: SupabaseConfig,
) {

    /**
     * Emits every venue inserted by any client while collected.
     *
     * `channelFlow` is used rather than `flow` because the heartbeat runs as a
     * sibling coroutine inside the socket session, and only a channel-backed flow
     * allows emission from a coroutine other than the collector's.
     *
     * Reconnection uses capped exponential backoff. Without the cap a long
     * outage would retry every few hours; without the backoff a server that
     * closes immediately would produce a reconnect storm.
     */
    fun inserts(): Flow<SpaceDto> = channelFlow {
        var attempt = 0

        while (isActive) {
            try {
                client.webSocket(urlString = config.realtimeUrl) {
                    attempt = 0
                    send(Frame.Text(VendavalJson.encodeToString(JsonObject.serializer(), joinMessage())))

                    // Phoenix drops a channel that stops sending heartbeats.
                    val heartbeat = launch {
                        var ref = HEARTBEAT_FIRST_REF
                        while (isActive) {
                            delay(HEARTBEAT_INTERVAL_MS)
                            runCatching {
                                send(
                                    Frame.Text(
                                        VendavalJson.encodeToString(
                                            JsonObject.serializer(),
                                            heartbeatMessage(ref++),
                                        ),
                                    ),
                                )
                            }.onFailure { return@launch }
                        }
                    }

                    try {
                        for (frame in incoming) {
                            val text = (frame as? Frame.Text)?.readText() ?: continue
                            parseInsertedRow(text)?.let { trySend(it) }
                        }
                    } finally {
                        heartbeat.cancel()
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                // Swallowed by design: a realtime outage must not surface as an
                // app error. The catalogue is already on screen from cache.
            }

            if (!isActive) break
            attempt = (attempt + 1).coerceAtMost(MAX_BACKOFF_EXPONENT)
            delay(backoffFor(attempt))
        }

        awaitClose { }
    }

    /** Subscribes to inserts only; updates and deletes are not part of the contract. */
    private fun joinMessage(): JsonObject = buildJsonObject {
        put("topic", config.realtimeTopic)
        put("event", "phx_join")
        put("ref", "1")
        putJsonObject("payload") {
            putJsonObject("config") {
                putJsonArray("postgres_changes") {
                    add(
                        buildJsonObject {
                            put("event", "INSERT")
                            put("schema", "public")
                            put("table", config.table)
                        },
                    )
                }
            }
        }
    }

    private fun heartbeatMessage(ref: Int): JsonObject = buildJsonObject {
        put("topic", "phoenix")
        put("event", "heartbeat")
        put("ref", ref.toString())
        putJsonObject("payload") { }
    }

    /**
     * Pulls the new row out of a Phoenix frame.
     *
     * Navigated as a [JsonObject] rather than decoded into a typed envelope
     * because the socket also carries replies, presence diffs and system
     * messages whose shapes are irrelevant here; anything that is not an insert
     * payload yields `null` and is ignored.
     */
    private fun parseInsertedRow(text: String): SpaceDto? = runCatching {
        val root = VendavalJson.parseToJsonElement(text).jsonObject
        if (root["event"]?.jsonPrimitive?.content != "postgres_changes") return null

        val data = root["payload"]?.jsonObject?.get("data")?.jsonObject ?: return null
        if (data["type"]?.jsonPrimitive?.content != "INSERT") return null

        val record = data["record"]?.jsonObject ?: return null
        VendavalJson.decodeFromJsonElement(SpaceDto.serializer(), record)
    }.getOrNull()

    private fun backoffFor(attempt: Int): Long {
        var millis = BASE_BACKOFF_MS
        repeat(attempt - 1) { millis *= 2 }
        return millis.coerceAtMost(MAX_BACKOFF_MS)
    }

    private companion object {
        const val HEARTBEAT_INTERVAL_MS = 25_000L
        const val HEARTBEAT_FIRST_REF = 100
        const val BASE_BACKOFF_MS = 2_000L
        const val MAX_BACKOFF_MS = 60_000L
        const val MAX_BACKOFF_EXPONENT = 6
    }
}
