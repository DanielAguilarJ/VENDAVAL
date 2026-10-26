package mx.vendaval.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import mx.vendaval.core.data.dto.SpaceDto
import mx.vendaval.core.data.local.VendavalJson
import mx.vendaval.core.domain.model.SyncFailure

/**
 * Outcome of a remote call.
 *
 * A sealed result rather than exceptions: transport problems here are an expected,
 * routine state for an offline-first app, not an exceptional one, and the caller
 * has to branch on the cause anyway to decide what badge to show.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val cause: SyncFailure, val detail: String? = null) : ApiResult<Nothing>
}

/**
 * Read/write access to the `espacios_culturales` table over PostgREST.
 *
 * Only the two operations the app actually needs are exposed. There is no update
 * or delete: the catalogue is append-only from a client's point of view, which
 * keeps the required RLS surface as small as possible.
 */
class SupabaseSpaceApi(
    private val client: HttpClient,
    private val config: SupabaseConfig,
) {

    /**
     * Fetches the full catalogue ordered by id.
     *
     * [limit] is a hard ceiling rather than pagination: the dataset is small by
     * design, and an unbounded select against a publicly writable table is how a
     * client gets taken down by someone else's spam.
     */
    suspend fun fetchAll(limit: Int = MAX_ROWS): ApiResult<List<SpaceDto>> = call {
        val response = client.get(config.restUrl) {
            parameter("select", "*")
            parameter("order", "id.asc")
            parameter("limit", limit.toString())
        }
        when {
            response.status.value in 200..299 -> ApiResult.Success(decodeList(response))
            else -> ApiResult.Failure(response.status.value.toSyncFailure(), response.bodyAsText())
        }
    }

    /**
     * Publishes one venue and returns the row as stored, so the caller learns the
     * server-assigned id. `Prefer: return=representation` is what makes PostgREST
     * echo the inserted row back.
     */
    suspend fun insert(dto: SpaceDto): ApiResult<SpaceDto> = call {
        val response = client.post(config.restUrl) {
            contentType(ContentType.Application.Json)
            header("Prefer", "return=representation")
            setBody(listOf(dto))
        }
        when {
            response.status.value in 200..299 -> {
                val inserted = decodeList(response).firstOrNull()
                if (inserted == null) {
                    ApiResult.Failure(SyncFailure.MALFORMED, "Empty representation")
                } else {
                    ApiResult.Success(inserted)
                }
            }
            else -> ApiResult.Failure(response.status.value.toSyncFailure(), response.bodyAsText())
        }
    }

    private suspend fun decodeList(response: HttpResponse): List<SpaceDto> {
        // Decoded from text rather than via `body()` so a malformed payload is
        // classified as MALFORMED instead of surfacing as a generic transport error.
        val text = response.bodyAsText()
        if (text.isBlank()) return emptyList()
        return VendavalJson.decodeFromString(text)
    }

    /**
     * Shared error funnel. Cancellation is rethrown untouched — swallowing it
     * would detach the call from its parent scope and leak the coroutine.
     */
    private inline fun <T> call(block: () -> ApiResult<T>): ApiResult<T> =
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (serialization: SerializationException) {
            ApiResult.Failure(SyncFailure.MALFORMED, serialization.message)
        } catch (throwable: Throwable) {
            ApiResult.Failure(throwable.toSyncFailure(), throwable.message)
        }

    private companion object {
        const val MAX_ROWS = 2_000
    }
}

private fun Int.toSyncFailure(): SyncFailure = when (this) {
    401, 403 -> SyncFailure.REJECTED
    in 400..499 -> SyncFailure.MALFORMED
    in 500..599 -> SyncFailure.SERVER
    else -> SyncFailure.UNKNOWN
}

/**
 * Classifies transport exceptions without referring to platform-specific types.
 *
 * Ktor surfaces DNS, socket and TLS problems through different classes on JVM,
 * Android and Darwin, and none of them are shared in common code, so the class
 * name is the only portable signal available.
 */
private fun Throwable.toSyncFailure(): SyncFailure {
    val name = this::class.simpleName.orEmpty()
    val offlineMarkers = listOf(
        "UnresolvedAddress", "ConnectTimeout", "SocketTimeout", "HttpRequestTimeout",
        "IOException", "Socket", "Network", "Connect",
    )
    return if (offlineMarkers.any { name.contains(it, ignoreCase = true) }) {
        SyncFailure.OFFLINE
    } else {
        SyncFailure.UNKNOWN
    }
}
