import GoogleSignIn
import Shared
import SwiftUI

@main
struct QodeApp: App {
    @State private var account = AccountStore()
    @AppStorage("theme") private var theme = AppTheme.system

    init() {
        let info = Bundle.main.infoDictionary ?? [:]
        KoinIosKt.doInitKoin(
            supabaseUrl: info["SupabaseURL"] as? String ?? "",
            supabaseKey: info["SupabaseKey"] as? String ?? ""
        )
    }

    var body: some Scene {
        WindowGroup {
            TabView {
                NavigationStack { HomeView() }
                    .tabItem { Label(L("ui_home"), systemImage: "house") }
                NavigationStack { FeedView() }
                    .tabItem { Label(L("ui_feed"), systemImage: "text.bubble") }
            }
            .environment(account)
            .preferredColorScheme(theme.colorScheme)
            .onOpenURL { GIDSignIn.sharedInstance.handle($0) }
            .task { await account.refresh() }
        }
    }
}
