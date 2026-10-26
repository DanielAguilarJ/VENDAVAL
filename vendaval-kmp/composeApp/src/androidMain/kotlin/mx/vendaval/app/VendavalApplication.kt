package mx.vendaval.app

import android.app.Application
import mx.vendaval.core.di.loadVendavalModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class VendavalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // ERROR level in release-style startup: Koin's INFO logging walks every
            // definition, which is noise once the graph is known to resolve.
            androidLogger(Level.ERROR)
            androidContext(this@VendavalApplication)
            loadVendavalModules()
        }
    }
}
