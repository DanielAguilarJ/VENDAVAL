package mx.vendaval.core.di

import kotlinx.datetime.Clock
import mx.vendaval.core.data.local.CatalogueCache
import mx.vendaval.core.data.local.FavouritesCache
import mx.vendaval.core.data.local.PreferencesCache
import mx.vendaval.core.data.remote.SupabaseConfig
import mx.vendaval.core.data.remote.SupabaseRealtimeSource
import mx.vendaval.core.data.remote.SupabaseSpaceApi
import mx.vendaval.core.data.remote.createSupabaseHttpClient
import mx.vendaval.core.data.remote.createTileHttpClient
import mx.vendaval.core.data.repository.FavouritesRepositoryImpl
import mx.vendaval.core.data.repository.PreferencesRepository
import mx.vendaval.core.data.repository.SpaceRepositoryImpl
import mx.vendaval.core.domain.repository.FavouritesRepository
import mx.vendaval.core.domain.repository.SpaceRepository
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Distinguishes the two HTTP clients, which must not share headers. */
val SupabaseClientQualifier = named("supabase-http")
val TileClientQualifier = named("tile-http")

/**
 * Wiring for everything above the platform line.
 *
 * Repositories are singletons because they hold the in-memory catalogue that the
 * whole app observes; a second instance would mean a second source of truth.
 * Everything is constructor-injected and every binding is to an interface, so a
 * test can replace any layer without touching the ones above it.
 */
val sharedModule: Module = module {

    single { SupabaseConfig.PRODUCTION }

    // Bound explicitly to `Clock`, not to the inferred type of `Clock.System`.
    // Without the explicit type parameter Koin registers the singleton under
    // `Clock.System` and every `get<Clock>()` fails to resolve at startup.
    single<Clock> { Clock.System }

    single(SupabaseClientQualifier) { createSupabaseHttpClient(get()) }
    single(TileClientQualifier) { createTileHttpClient() }

    single { SupabaseSpaceApi(client = get(SupabaseClientQualifier), config = get()) }
    single { SupabaseRealtimeSource(client = get(SupabaseClientQualifier), config = get()) }

    // One typed cache per concern rather than a single blob, so an unreadable
    // preferences file can never take the catalogue cache down with it. The distinct
    // types also keep the container from confusing them (see LocalStores.kt).
    single { CatalogueCache(paths = get()) }
    single { FavouritesCache(paths = get()) }
    single { PreferencesCache(paths = get()) }

    single<SpaceRepository> {
        SpaceRepositoryImpl(
            api = get(),
            realtimeSource = get(),
            cache = get(),
            clock = get(),
        )
    }

    single { FavouritesRepositoryImpl(cache = get()) }
    single<FavouritesRepository> { get<FavouritesRepositoryImpl>() }

    single { PreferencesRepository(cache = get()) }
}

/**
 * Every module the app needs. Platform entry points call this so the module list
 * lives in one place instead of being duplicated per target.
 */
fun KoinApplication.loadVendavalModules(extraModules: List<Module> = emptyList()) {
    modules(listOf(sharedModule, platformModule()) + extraModules)
}
