package mx.vendaval.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import mx.vendaval.app.feature.add.DesktopPhotoPicker
import mx.vendaval.app.feature.add.LocalPhotoPicker
import mx.vendaval.core.di.loadVendavalModules
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        loadVendavalModules()
    }

    application {
        val windowState = rememberWindowState(
            // Wide enough to land in the EXPANDED regime, so the desktop build opens
            // with the two-pane layout rather than the phone one.
            size = DpSize(1280.dp, 860.dp),
        )

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Vendaval · Cartografía Cultural de Aguascalientes",
        ) {
            CompositionLocalProvider(LocalPhotoPicker provides DesktopPhotoPicker()) {
                App()
            }
        }
    }
}
