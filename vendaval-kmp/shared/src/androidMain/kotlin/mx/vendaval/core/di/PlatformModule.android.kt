package mx.vendaval.core.di

import mx.vendaval.core.data.local.StoragePaths
import mx.vendaval.core.domain.service.ExternalLauncher
import mx.vendaval.core.domain.service.LocationProvider
import mx.vendaval.core.platform.AndroidExternalLauncher
import mx.vendaval.core.platform.AndroidLocationProvider
import mx.vendaval.core.platform.AndroidStoragePaths
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // The application context, supplied by `androidContext()` at startup. Using
    // it rather than an Activity keeps these singletons safe from leaking a
    // destroyed window across configuration changes.
    single<StoragePaths> { AndroidStoragePaths(androidContext()) }
    single<LocationProvider> { AndroidLocationProvider(androidContext()) }
    single<ExternalLauncher> { AndroidExternalLauncher(androidContext()) }
}
