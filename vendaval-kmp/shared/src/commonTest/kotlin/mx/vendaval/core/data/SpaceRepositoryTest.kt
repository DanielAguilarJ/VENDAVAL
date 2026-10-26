package mx.vendaval.core.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mx.vendaval.core.data.local.CatalogueCache
import mx.vendaval.core.data.local.StoragePaths
import mx.vendaval.core.data.remote.SupabaseConfig
import mx.vendaval.core.data.remote.SupabaseRealtimeSource
import mx.vendaval.core.data.remote.SupabaseSpaceApi
import mx.vendaval.core.data.remote.installSupabaseDefaults
import mx.vendaval.core.data.repository.SpaceRepositoryImpl
import mx.vendaval.core.data.seed.SeedCatalogue
import mx.vendaval.core.domain.model.Municipality
import mx.vendaval.core.domain.model.SpaceDraft
import mx.vendaval.core.domain.model.SpaceOrigin
import mx.vendaval.core.domain.model.SyncFailure
import mx.vendaval.core.domain.model.SyncState
import mx.vendaval.core.domain.model.VenueType
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Storage backed by an in-memory filesystem, so tests touch no real disk. */
private class InMemoryPaths : StoragePaths {
    override val fileSystem: FileSystem = FakeFileSystem()
    override val root: Path = "/vendaval".toPath()
}

private val testConfig = SupabaseConfig(
    projectUrl = "https://example.supabase.co",
    anonKey = "test-anon-key",
)

private fun jsonClient(handler: MockEngine.Companion.() -> MockEngine): HttpClient =
    HttpClient(MockEngine.handler()) { installSupabaseDefaults(testConfig) }

private fun clientReturning(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
    HttpClient(
        MockEngine { _ ->
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        },
    ) { installSupabaseDefaults(testConfig) }

private fun failingClient(status: HttpStatusCode) =
    HttpClient(MockEngine { respondError(status) }) { installSupabaseDefaults(testConfig) }

private fun repository(client: HttpClient, paths: StoragePaths = InMemoryPaths()) =
    SpaceRepositoryImpl(
        api = SupabaseSpaceApi(client, testConfig),
        realtimeSource = SupabaseRealtimeSource(client, testConfig),
        cache = CatalogueCache(paths),
    )

class SpaceRepositoryTest {

    @Test
    fun the_seeded_catalogue_is_available_before_any_network_call() = runTest {
        val repository = repository(clientReturning("[]"))

        val spaces = repository.spaces.first()

        assertEquals(SeedCatalogue.spaces.size, spaces.size)
        assertTrue(spaces.all { it.origin == SpaceOrigin.SEED })
    }

    @Test
    fun a_successful_refresh_merges_remote_rows_on_top_of_the_seed() = runTest {
        val remote = """
            [
              {"id": 501, "name": "Foro Nuevo", "address": "Calle 1",
               "phone": "449 111 2222", "type": "Teatro", "municipio": "Aguascalientes",
               "lat": 21.88, "lng": -102.29, "mapQuery": "Foro+Nuevo"}
            ]
        """.trimIndent()

        val repository = repository(clientReturning(remote))
        repository.refresh()

        val spaces = repository.spaces.first()
        assertEquals(SeedCatalogue.spaces.size + 1, spaces.size)

        val inserted = spaces.first { it.id.value == 501L }
        assertEquals("Foro Nuevo", inserted.name)
        assertEquals(VenueType.TEATRO, inserted.type)
        assertEquals(SpaceOrigin.REMOTE, inserted.origin)

        val state = repository.syncState.value
        assertTrue(state is SyncState.Synced)
        assertEquals(1, (state as SyncState.Synced).remoteCount)
    }

    @Test
    fun a_remote_row_with_a_seeded_id_replaces_the_compiled_in_version() = runTest {
        val remote = """
            [
              {"id": 1, "name": "Instituto Cultural (actualizado)", "address": "Nueva sede",
               "phone": "449 000 0000", "type": "Casa de Cultura", "municipio": "Aguascalientes",
               "lat": 21.8804, "lng": -102.2965, "mapQuery": "ICA"}
            ]
        """.trimIndent()

        val repository = repository(clientReturning(remote))
        repository.refresh()

        val spaces = repository.spaces.first()
        assertEquals(SeedCatalogue.spaces.size, spaces.size, "the row should replace, not add")
        assertEquals("Instituto Cultural (actualizado)", spaces.first { it.id.value == 1L }.name)
    }

    @Test
    fun malformed_rows_are_dropped_without_failing_the_whole_batch() = runTest {
        val remote = """
            [
              {"id": 601, "name": "Bueno", "type": "Teatro", "municipio": "Aguascalientes",
               "lat": 21.88, "lng": -102.29},
              {"id": 602, "name": "Sin coordenadas", "type": "Teatro", "municipio": "Aguascalientes"},
              {"id": 603, "lat": 21.88, "lng": -102.29}
            ]
        """.trimIndent()

        val repository = repository(clientReturning(remote))
        repository.refresh()

        val spaces = repository.spaces.first()
        assertNotNull(spaces.firstOrNull { it.id.value == 601L })
        assertNull(spaces.firstOrNull { it.id.value == 602L }, "no coordinates means unmappable")
        assertNull(spaces.firstOrNull { it.id.value == 603L }, "no name means unusable")
    }

    @Test
    fun an_unrecognised_municipality_is_inferred_from_the_coordinates() = runTest {
        val remote = """
            [
              {"id": 701, "name": "Espacio sin municipio", "type": "Centro Cultural",
               "municipio": "Desconocido", "lat": 21.8465, "lng": -102.7188}
            ]
        """.trimIndent()

        val repository = repository(clientReturning(remote))
        repository.refresh()

        val inserted = repository.spaces.first().first { it.id.value == 701L }
        assertEquals(Municipality.CALVILLO, inserted.municipality)
    }

    @Test
    fun a_server_error_degrades_the_sync_state_but_keeps_the_catalogue() = runTest {
        val repository = repository(failingClient(HttpStatusCode.InternalServerError))
        repository.refresh()

        assertEquals(SeedCatalogue.spaces.size, repository.spaces.first().size)

        val state = repository.syncState.value
        assertTrue(state is SyncState.Failed)
        assertEquals(SyncFailure.SERVER, (state as SyncState.Failed).cause)
    }

    @Test
    fun an_unauthorised_response_is_classified_as_rejected() = runTest {
        val repository = repository(failingClient(HttpStatusCode.Unauthorized))
        repository.refresh()

        val state = repository.syncState.value
        assertTrue(state is SyncState.Failed)
        assertEquals(SyncFailure.REJECTED, (state as SyncState.Failed).cause)
    }

    @Test
    fun a_submission_is_stored_locally_and_published_when_the_server_accepts() = runTest {
        val published = """
            [
              {"id": 900, "name": "Casa Nueva", "address": "Calle 5", "phone": "449 555 6666",
               "type": "Casa de Cultura", "municipio": "Calvillo",
               "lat": 21.8465, "lng": -102.7188, "mapQuery": "Casa+Nueva"}
            ]
        """.trimIndent()

        val repository = repository(clientReturning(published, HttpStatusCode.Created))

        val result = repository.submit(
            SpaceDraft(
                name = "Casa Nueva",
                address = "Calle 5",
                phone = "449 555 6666",
                type = VenueType.CASA_DE_CULTURA,
                municipality = Municipality.CALVILLO,
                latitude = "21.8465",
                longitude = "-102.7188",
            ),
        )

        assertTrue(result.isSuccess)

        val spaces = repository.spaces.first()
        val stored = spaces.firstOrNull { it.name == "Casa Nueva" }
        assertNotNull(stored)
        // The server assigned 900, so the record left the local negative id space.
        assertEquals(900L, stored.id.value)
        assertEquals(SpaceOrigin.REMOTE, stored.origin)
    }

    @Test
    fun a_submission_survives_a_failed_upload_and_stays_pending() = runTest {
        val repository = repository(failingClient(HttpStatusCode.ServiceUnavailable))

        val result = repository.submit(
            SpaceDraft(
                name = "Casa Offline",
                address = "Calle 9",
                type = VenueType.CENTRO_CULTURAL,
                municipality = Municipality.AGUASCALIENTES,
                latitude = "21.88",
                longitude = "-102.29",
            ),
        )

        assertTrue(result.isSuccess, "the local write must succeed even when the network fails")

        val stored = repository.spaces.first().first { it.name == "Casa Offline" }
        assertEquals(SpaceOrigin.LOCAL, stored.origin)
        assertTrue(stored.id.isLocal, "an unpublished record keeps a negative id")
    }

    @Test
    fun an_invalid_draft_is_refused_before_any_network_call() = runTest {
        val repository = repository(clientReturning("[]"))

        val result = repository.submit(SpaceDraft(name = "x"))

        assertTrue(result.isFailure)
        assertEquals(SeedCatalogue.spaces.size, repository.spaces.first().size)
    }

    @Test
    fun a_pending_local_record_can_be_deleted_but_a_seeded_one_cannot() = runTest {
        val repository = repository(failingClient(HttpStatusCode.ServiceUnavailable))
        repository.submit(
            SpaceDraft(
                name = "Casa Temporal",
                address = "Calle 3",
                type = VenueType.TEATRO,
                municipality = Municipality.AGUASCALIENTES,
                latitude = "21.88",
                longitude = "-102.29",
            ),
        )

        val local = repository.spaces.first().first { it.name == "Casa Temporal" }
        assertTrue(repository.deleteLocal(local.id))
        assertNull(repository.spaces.first().firstOrNull { it.name == "Casa Temporal" })

        val seeded = SeedCatalogue.spaces.first().id
        assertTrue(!repository.deleteLocal(seeded), "shared records are not this client's to delete")
    }

    @Test
    fun the_cache_is_reloaded_from_disk_on_a_later_launch() = runTest {
        val paths = InMemoryPaths()
        val remote = """
            [
              {"id": 800, "name": "Persistido", "address": "Calle 8", "type": "Teatro",
               "municipio": "Aguascalientes", "lat": 21.88, "lng": -102.29}
            ]
        """.trimIndent()

        repository(clientReturning(remote), paths).refresh()

        // A second instance over the same storage stands in for a cold start with no
        // network available.
        val restarted = repository(failingClient(HttpStatusCode.ServiceUnavailable), paths)
        restarted.warmUp()

        val recovered = restarted.spaces.first().firstOrNull { it.id.value == 800L }
        assertNotNull(recovered, "the cached row should be available without the network")
        assertEquals("Persistido", recovered.name)
    }
}
