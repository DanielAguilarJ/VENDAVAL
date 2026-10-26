package mx.vendaval.core.di

import mx.vendaval.core.data.local.StoragePaths
import mx.vendaval.core.domain.service.ExternalLauncher
import mx.vendaval.core.domain.service.LocationProvider
import mx.vendaval.core.platform.IosExternalLauncher
import mx.vendaval.core.platform.IosLocationProvider
import mx.vendaval.core.platform.IosStoragePaths
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<StoragePaths> { IosStoragePaths() }
    single<LocationProvider> { IosLocationProvider() }
    single<ExternalLauncher> { IosExternalLauncher() }
}
