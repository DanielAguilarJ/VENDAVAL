package mx.vendaval.core.di

import mx.vendaval.core.data.local.StoragePaths
import mx.vendaval.core.domain.service.ExternalLauncher
import mx.vendaval.core.domain.service.LocationProvider
import mx.vendaval.core.platform.DesktopExternalLauncher
import mx.vendaval.core.platform.DesktopLocationProvider
import mx.vendaval.core.platform.DesktopStoragePaths
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<StoragePaths> { DesktopStoragePaths() }
    single<LocationProvider> { DesktopLocationProvider() }
    single<ExternalLauncher> { DesktopExternalLauncher() }
}
