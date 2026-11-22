import CryptoKit
import GoogleSignIn
import Shared
import SwiftUI

@MainActor
@Observable
final class AccountStore {
    var account: AccountItem?
    /// Bumped after a report or block so lists reload without the hidden content.
    var contentVersion = 0

    func contentChanged() {
        contentVersion += 1
    }

    func refresh() async {
        account = try? await AuthApi().currentAccount()
    }

    func signOut() async {
        try? await AuthApi().signOut()
        account = nil
    }
}

func authErrorMessage(_ error: Swift.Error) -> String {
    let code = ((error as NSError).userInfo["KotlinException"] as? QodeException)?.code
    switch code {
    case "InvalidEmail": return L("error_auth_invalid_email")
    case "InvalidCode": return L("error_auth_invalid_code")
    case "TooManyAttempts": return L("error_auth_too_many_attempts")
    case "ServiceUnavailable": return L("error_auth_service_unavailable")
    case "InvalidCredentials": return L("error_auth_invalid_credentials")
    default: return errorMessage(error)
    }
}

struct AccountButton: View {
    @Environment(AccountStore.self) private var store
    @State private var showSignIn = false

    var body: some View {
        Group {
            if let account = store.account {
                Menu {
                    Text(account.displayName ?? L("ui_profile"))
                    Button(L("profile_action_signout"), role: .destructive) { Task { await store.signOut() } }
                } label: {
                    Image(systemName: "person.crop.circle.fill")
                }
            } else {
                Button { showSignIn = true } label: { Image(systemName: "person.crop.circle") }
                    .accessibilityLabel(L("auth_sign_in"))
            }
        }
        .sheet(isPresented: $showSignIn) {
            SignInSheet { Task { await store.refresh() } }
        }
    }
}

struct SignInSheet: View {
    let onSignedIn: () -> Void

    private enum Step { case email, code, consent }

    @Environment(\.dismiss) private var dismiss
    @State private var api = AuthApi()
    @State private var step = Step.email
    @State private var email = ""
    @State private var code = ""
    @State private var isLoading = false
    @State private var error: String?
    @State private var document: LegalDocument?

    var body: some View {
        NavigationStack {
            Form {
                switch step {
                case .email: emailStep
                case .code: codeStep
                case .consent: consentStep
                }
                if let error {
                    Section { Text(error).foregroundStyle(.red) }
                }
            }
            .disabled(isLoading)
            .overlay { if isLoading { ProgressView() } }
            .navigationTitle(step == .consent ? L("legal_consent_title") : L("auth_continue_with_email"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                if step != .consent {
                    ToolbarItem(placement: .cancellationAction) { Button(L("ui_close")) { dismiss() } }
                }
            }
            .sheet(item: $document) { LegalDocumentView(document: $0) }
        }
        .interactiveDismissDisabled(step == .consent)
    }

    @ViewBuilder
    private var emailStep: some View {
        Section {
            Button(action: signInWithGoogle) {
                Label(L("continue_with_google"), systemImage: "g.circle.fill")
            }
        }
        Section {
            TextField(L("auth_email_placeholder"), text: $email)
                .textContentType(.emailAddress)
                .keyboardType(.emailAddress)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .onSubmit(sendCode)
            Button(L("auth_send_code"), action: sendCode)
                .disabled(email.trimmingCharacters(in: .whitespaces).isEmpty)
        }
    }

    private var codeStep: some View {
        Section {
            Text(String(format: L("auth_code_sent"), email)).font(.subheadline)
            TextField(L("auth_code_placeholder"), text: $code)
                .textContentType(.oneTimeCode)
                .keyboardType(.numberPad)
                .font(.system(.title2, design: .monospaced))
                .onChange(of: code) { _, value in
                    let digits = String(value.filter(\.isNumber).prefix(6))
                    if digits != value { code = digits }
                    if digits.count == 6 { signIn() }
                }
            Button(L("auth_sign_in"), action: signIn).disabled(code.count != 6)
            Button(L("auth_resend_code"), action: sendCode)
            Button(L("auth_change_email")) {
                code = ""
                error = nil
                step = .email
            }
        }
    }

    private var consentStep: some View {
        Group {
            Section {
                Text(L("legal_consent_subtitle"))
                Button(L("terms_of_service")) { openDocument(privacy: false) }
                Button(L("privacy_policy")) { openDocument(privacy: true) }
            }
            Section {
                Button(L("legal_consent_accept"), action: acceptConsent).bold()
                Button(L("legal_consent_decline"), role: .destructive, action: declineConsent)
            }
        }
    }

    private func run(_ work: @escaping () async throws -> Void) {
        guard !isLoading else { return }
        isLoading = true
        error = nil
        Task {
            do {
                try await work()
            } catch {
                self.error = authErrorMessage(error)
            }
            isLoading = false
        }
    }

    private func sendCode() {
        run {
            try await api.sendEmailCode(email: email)
            code = ""
            step = .code
        }
    }

    private func signIn() {
        run {
            let outcome = try await api.signInWithEmailCode(email: email, code: code)
            if outcome == .signedIn {
                finish()
            } else {
                step = .consent
            }
        }
    }

    private func signInWithGoogle() {
        guard let presenter = UIApplication.shared.connectedScenes
            .compactMap({ ($0 as? UIWindowScene)?.keyWindow?.rootViewController })
            .first
            .map(topmost)
        else { return }
        let nonce = UUID().uuidString
        run {
            let result: GIDSignInResult
            do {
                result = try await GIDSignIn.sharedInstance.signIn(
                    withPresenting: presenter,
                    hint: nil,
                    additionalScopes: nil,
                    nonce: sha256Hex(nonce)
                )
            } catch let error as NSError where error.domain == kGIDSignInErrorDomain && error.code == GIDSignInError.canceled.rawValue {
                return
            }
            guard let idToken = result.user.idToken?.tokenString else { throw GoogleSignInFailed() }
            let outcome = try await api.signInWithGoogle(idToken: idToken, nonce: nonce)
            if outcome == .signedIn {
                finish()
            } else {
                step = .consent
            }
        }
    }

    private func acceptConsent() {
        run {
            try await api.acceptConsent()
            finish()
        }
    }

    private func declineConsent() {
        run {
            try await api.signOut()
            dismiss()
        }
    }

    private func openDocument(privacy: Bool) {
        run {
            let content = try await api.legalDocument(privacyPolicy: privacy)
            document = LegalDocument(title: L(privacy ? "privacy_policy" : "terms_of_service"), content: content)
        }
    }

    private func finish() {
        onSignedIn()
        dismiss()
    }
}

private struct GoogleSignInFailed: Swift.Error {}

private func sha256Hex(_ value: String) -> String {
    SHA256.hash(data: Data(value.utf8)).map { String(format: "%02x", $0) }.joined()
}

private func topmost(_ controller: UIViewController) -> UIViewController {
    controller.presentedViewController.map(topmost) ?? controller
}

struct LegalDocument: Identifiable {
    let title: String
    let content: String
    var id: String { title }
}

struct LegalDocumentView: View {
    let document: LegalDocument
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                MarkdownText(text: document.content)
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .navigationTitle(document.title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) { Button(L("ui_close")) { dismiss() } }
            }
        }
    }
}
