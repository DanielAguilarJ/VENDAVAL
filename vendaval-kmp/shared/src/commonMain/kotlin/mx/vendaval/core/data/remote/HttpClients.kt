package mx.vendaval.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import mx.vendaval.core.data.local.VendavalJson

/**
 * Identifies the app to third-party servers. The OpenStreetMap tile usage policy
 * requires a descriptive, contactable user agent and rejects generic library
 * defaults, so this is not cosmetic.
 */
const val VENDAVAL_USER_AGENT: String =
    "VendavalKMP/1.0 (Colectivo Vendaval; cartografia cultural Aguascalientes)"

/**
 * Shared configuration for any client talking to Supabase.
 *
 * Extracted from [createSupabaseHttpClient] so tests can apply the identical setup on
 * top of Ktor's `MockEngine`. Without this split a test would either exercise a
 * different configuration than production or need a live network.
 */
fun HttpClientConfig<*>.installSupabaseDefaults(config: SupabaseConfig) {
    expectSuccess = false

    install(ContentNegotiation) {
        json(VendavalJson)
    }

    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 20_000
        socketTimeoutMillis = 20_000
    }

    defaultRequest {
        header("apikey", config.anonKey)
        header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
        header(HttpHeaders.UserAgent, VENDAVAL_USER_AGENT)
    }
}

/**
 * Client for the Supabase REST and realtime endpoints.
 *
 * `expectSuccess` stays false so non-2xx answers are handled as data by
 * [SupabaseSpaceApi] and classified into a [mx.vendaval.core.domain.model.SyncFailure],
 * instead of arriving as exceptions that would have to be unwrapped again.
 */
fun createSupabaseHttpClient(config: SupabaseConfig): HttpClient = HttpClient {
    installSupabaseDefaults(config)

    // Only the realtime source needs this, and MockEngine cannot support it, which is
    // why it lives here rather than in the shared defaults.
    install(WebSockets)
}

/**
 * Separate client for map tiles.
 *
 * Kept apart from the Supabase client on purpose: tiles must not carry the
 * `apikey`/`Authorization` headers to a third-party CDN, and they want a much
 * shorter timeout because a stalled tile should be dropped and retried on the
 * next pan rather than holding a connection open.
 */
fun createTileHttpClient(): HttpClient = HttpClient {
    expectSuccess = false

    install(HttpTimeout) {
        connectTimeoutMillis = 6_000
        requestTimeoutMillis = 8_000
        socketTimeoutMillis = 8_000
    }

    defaultRequest {
        header(HttpHeaders.UserAgent, VENDAVAL_USER_AGENT)
    }
}
