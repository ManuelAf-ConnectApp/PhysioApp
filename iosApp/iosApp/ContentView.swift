import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        IosMainKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}

class IosCryptoManager: CryptoManager {
    func encrypt(value: String) -> String {
        // Implementación con Keychain o similar
        // Por ahora devolvemos un Base64 simple para pruebas
        return Data(value.utf8).base64EncodedString()
    }
    
    func decrypt(value: String) -> String {
        // Implementación con Keychain o similar
        guard let data = Data(base64Encoded: value) else { return "" }
        return String(data: data, encoding: .utf8) ?? ""
    }
}


