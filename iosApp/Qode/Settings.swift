import Shared
import SwiftUI

enum AppTheme: String, CaseIterable {
    case system, light, dark

    var colorScheme: ColorScheme? {
        switch self {
        case .system: nil
        case .light: .light
        case .dark: .dark
        }
    }

    var titleKey: String { "theme_\(rawValue)" }
}

struct SettingsView: View {
    private static let feedbackEmail = "support@qodein.com"
    private static let githubURL = URL(string: "https://github.com/aqtanb/qode")!
    private static let telegramURL = URL(string: "https://t.me/qodeinhq")!

    @Environment(AccountStore.self) private var store
    @Environment(\.openURL) private var openURL
    @AppStorage("theme") private var theme = AppTheme.system
    @State private var showDelete = false
    @State private var document: LegalDocument?
    @State private var error: String?

    var body: some View {
        List {
            Section {
                Picker(L("settings_theme_title"), selection: $theme) {
                    ForEach(AppTheme.allCases, id: \.self) { Text(L($0.titleKey)).tag($0) }
                }
                Button {
                    if let url = URL(string: UIApplication.openSettingsURLString) { openURL(url) }
                } label: {
                    LabeledContent(L("settings_language_title"), value: Locale.current.localizedString(forLanguageCode: Locale.current.language.languageCode?.identifier ?? "en") ?? "")
                }
                .foregroundStyle(.primary)
            }
            Section {
                Button(L("settings_feedback_title")) {
                    if let url = URL(string: "mailto:\(Self.feedbackEmail)?subject=Qode%20iOS%20feedback") { openURL(url) }
                }
                Link("GitHub", destination: Self.githubURL)
                Link("Telegram", destination: Self.telegramURL)
                Button(L("terms_of_service")) { openDocument(privacy: false) }
                Button(L("privacy_policy")) { openDocument(privacy: true) }
            } header: {
                Text(L("settings_about_title"))
            } footer: {
                Text(String(format: L("about_version"), Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? ""))
            }
            if store.account != nil {
                Section {
                    Button(L("settings_delete_account_title"), role: .destructive) { showDelete = true }
                }
            }
        }
        .navigationTitle(L("settings_title"))
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showDelete) { DeleteAccountSheet() }
        .sheet(item: $document) { LegalDocumentView(document: $0) }
        .alert(error ?? "", isPresented: Binding(get: { error != nil }, set: { if !$0 { error = nil } })) {}
    }

    private func openDocument(privacy: Bool) {
        Task {
            do {
                let content = try await AuthApi().legalDocument(privacyPolicy: privacy)
                document = LegalDocument(title: L(privacy ? "privacy_policy" : "terms_of_service"), content: content)
            } catch {
                self.error = errorMessage(error)
            }
        }
    }
}

struct DeleteAccountSheet: View {
    @Environment(AccountStore.self) private var store
    @Environment(\.dismiss) private var dismiss
    @State private var understood = false
    @State private var isDeleting = false
    @State private var error: String?

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Text(L("delete_account_dialog_message"))
                    Toggle(L("delete_account_checkbox"), isOn: $understood)
                }
                if let error {
                    Section(L("delete_account_error_title")) { Text(error).foregroundStyle(.red) }
                }
                Section {
                    Button(L("delete_account_confirm"), role: .destructive, action: delete)
                        .disabled(!understood || isDeleting)
                }
            }
            .overlay { if isDeleting { ProgressView() } }
            .navigationTitle(L("delete_account_dialog_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("ui_cancel")) { dismiss() } }
            }
        }
        .interactiveDismissDisabled(isDeleting)
    }

    private func delete() {
        isDeleting = true
        error = nil
        Task {
            do {
                try await ProfileApi().deleteAccount()
                await store.signOut()
                store.contentChanged()
                dismiss()
            } catch {
                self.error = errorMessage(error)
            }
            isDeleting = false
        }
    }
}
