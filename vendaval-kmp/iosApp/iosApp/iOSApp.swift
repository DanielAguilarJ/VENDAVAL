import SwiftUI

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                // Compose draws its own status-bar treatment through
                // `safeDrawingPadding()`, so the SwiftUI host must not add its own.
                .ignoresSafeArea(.all)
        }
    }
}
