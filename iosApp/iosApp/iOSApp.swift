import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Arranca Koin en iOS (crea el CineDb con el driver nativo). Definido en KoinIos.kt.
        KoinIosKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
