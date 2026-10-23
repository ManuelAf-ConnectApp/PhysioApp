import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @Environment(\.scenePhase) private var scenePhase
    @State private var isScreenCaptured = UIScreen.main.isCaptured

    var body: some View {
        ZStack {
            ComposeView()
                .ignoresSafeArea()
            // Health data (plan §21.8, D21): iOS takes the app switcher snapshot once the scene
            // stops being active, so the cover is shown before that; it also hides the content
            // while the screen is recorded, mirrored or shared. iOS offers no way to block
            // screenshots.
            if scenePhase != .active || isScreenCaptured {
                PrivacyCover()
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: UIScreen.capturedDidChangeNotification)) { _ in
            isScreenCaptured = UIScreen.main.isCaptured
        }
    }
}

/** Neutral screen with no data, shown instead of the app. */
private struct PrivacyCover: View {
    var body: some View {
        ZStack {
            Color(uiColor: .systemBackground)
                .ignoresSafeArea()
            VStack(spacing: 16) {
                Image(systemName: "lock.shield")
                    .font(.system(size: 64))
                    .foregroundStyle(.secondary)
                Text("PhysioApp")
                    .font(.title)
                    .foregroundStyle(.secondary)
            }
        }
        .accessibilityElement(children: .combine)
    }
}
