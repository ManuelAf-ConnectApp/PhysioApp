import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    
    init() {
        let cryptoManager = IosCryptoManager()
        
        IosMainKt.startKoinIos(iosCryptoManager: cryptoManager)
    }
    
    var body: some Scene {

        WindowGroup {
            
            ContentView()
        }
    }
}
