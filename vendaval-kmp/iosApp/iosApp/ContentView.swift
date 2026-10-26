import SwiftUI
import UIKit
import VendavalApp

/// Bridges the Kotlin Compose hierarchy into SwiftUI.
///
/// `MainViewController()` is the single entry point exported by the `VendavalApp`
/// framework; it also starts Koin, so Swift never needs to know about the
/// dependency graph.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Compose owns all state; nothing to push down from SwiftUI.
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}
