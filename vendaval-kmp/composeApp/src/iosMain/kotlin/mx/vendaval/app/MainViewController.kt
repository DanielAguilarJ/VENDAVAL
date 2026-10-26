package mx.vendaval.app

import androidx.compose.ui.window.ComposeUIViewController
import mx.vendaval.core.di.loadVendavalModules
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

/**
 * Bridge consumed from Swift (`ContentView.swift`).
 *
 * Koin is started here rather than in Swift so the dependency graph is owned entirely
 * by Kotlin and the Xcode project never has to know about it. Guarded because
 * `startKoin` throws if called twice and SwiftUI may recreate the representable.
 */
fun MainViewController(): UIViewController {
    ensureKoinStarted()
    return ComposeUIViewController {
        App()
    }
}

private var koinStarted = false

private fun ensureKoinStarted() {
    if (koinStarted) return
    koinStarted = true
    startKoin {
        loadVendavalModules()
    }
}
