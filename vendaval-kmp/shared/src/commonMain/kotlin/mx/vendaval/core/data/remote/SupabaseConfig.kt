package mx.vendaval.core.data.remote

/**
 * Coordinates of the backend.
 *
 * ## About [anonKey]
 *
 * This is Supabase's *publishable* anon key. It is designed to ship inside
 * clients — the web portal already serves it in plain JavaScript — and it is not
 * a credential in the usual sense: it only asserts "an anonymous visitor", and
 * every actual permission is decided by row-level security on the table.
 *
 * The consequence, which matters more than the key itself: **the security of
 * this catalogue rests entirely on RLS policies**, not on hiding this string.
 * The table must expose `SELECT` to `anon` and must constrain `INSERT` (rate
 * limits, column checks, or a moderation queue). Without a restrictive insert
 * policy, anybody holding this key — that is, anybody — can write arbitrary rows.
 *
 * It is therefore kept in a single reviewable place, injected rather than
 * referenced globally, and never mixed with a service-role key, which must never
 * reach a client binary.
 */
data class SupabaseConfig(
    val projectUrl: String,
    val anonKey: String,
    val table: String = DEFAULT_TABLE,
) {
    /** PostgREST collection endpoint. */
    val restUrl: String get() = "$projectUrl/rest/v1/$table"

    /**
     * Realtime socket endpoint. The key travels as a query parameter because the
     * WebSocket handshake cannot carry the custom `apikey` header.
     */
    val realtimeUrl: String
        get() = projectUrl.replaceFirst("https://", "wss://") +
            "/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"

    val realtimeTopic: String get() = "realtime:public:$table"

    companion object {
        const val DEFAULT_TABLE = "espacios_culturales"

        /**
         * The deployment the existing web portal talks to, so both clients share
         * one catalogue. Override in DI to point at a staging project.
         */
        val PRODUCTION = SupabaseConfig(
            projectUrl = "https://ebjejctuuhvmjmevabjh.supabase.co",
            anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
                ".eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImViamVqY3R1dWh2bWptZXZhYmpoIiwicm9sZSI6ImFub24i" +
                "LCJpYXQiOjE3ODMzMzg4NDksImV4cCI6MjA5ODkxNDg0OX0" +
                ".MCVJau8KDz_xV1iwakVW4wpKD_MeMGan3aWY3cGE6g0",
        )
    }
}
