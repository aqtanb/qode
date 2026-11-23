import Shared
import SwiftUI

struct VoteControl: View {
    let itemId: String
    let isPost: Bool
    let baseScore: Int32

    @Environment(AccountStore.self) private var store
    @State private var vote = VoteState.none
    @State private var delta: Int32 = 0
    @State private var isSaving = false
    @State private var showSignIn = false
    @State private var error: String?

    var body: some View {
        HStack(spacing: 16) {
            button(.upvote, systemImage: "arrow.up", key: "cd_upvote")
            Text("\(baseScore + delta)").font(.headline).monospacedDigit()
            button(.downvote, systemImage: "arrow.down", key: "cd_downvote")
        }
        .task(id: store.account?.id) {
            delta = 0
            vote = (try? await InteractionApi().voteState(itemId: itemId, isPost: isPost)) ?? .none
        }
        .sheet(isPresented: $showSignIn) { SignInSheet { Task { await store.refresh() } } }
        .alert(error ?? "", isPresented: Binding(get: { error != nil }, set: { if !$0 { error = nil } })) {}
    }

    private func button(_ target: VoteState, systemImage: String, key: String) -> some View {
        Button {
            toggle(target)
        } label: {
            Image(systemName: vote == target ? systemImage + ".circle.fill" : systemImage + ".circle")
                .font(.title2)
        }
        .buttonStyle(.plain)
        .foregroundStyle(vote == target ? Color.accentColor : .secondary)
        .accessibilityLabel(String(format: L(key), Int(baseScore + delta)))
    }

    private func toggle(_ target: VoteState) {
        guard store.account != nil else {
            showSignIn = true
            return
        }
        guard !isSaving else { return }
        let previous = vote
        let previousDelta = delta
        let optimistic = previous.toggleTo(target: target)
        vote = optimistic
        delta += VoteState.Companion.shared.computeScoreDelta(from: previous, to: optimistic)
        isSaving = true
        Task {
            do {
                vote = try await InteractionApi().vote(itemId: itemId, isPost: isPost, current: previous, target: target)
                Track.vote(itemId: itemId, vote: vote)
            } catch {
                vote = previous
                delta = previousDelta
                self.error = errorMessage(error)
            }
            isSaving = false
        }
    }
}

/// The "…" menu on detail screens. [onRemoved] runs after a report or block, when the item should leave the screen.
struct ContentMenu: View {
    let itemId: String
    let isPost: Bool
    let authorId: String
    let authorName: String?
    let onRemoved: () -> Void

    @Environment(AccountStore.self) private var store
    @State private var showSignIn = false
    @State private var showReport = false
    @State private var confirmBlock = false
    @State private var error: String?

    var body: some View {
        Menu {
            Button(L("action_report"), systemImage: "flag") { requireAccount { showReport = true } }
            if store.account?.id != authorId {
                Button(L("action_block"), systemImage: "hand.raised", role: .destructive) { requireAccount { confirmBlock = true } }
            }
        } label: {
            Image(systemName: "ellipsis.circle")
        }
        .sheet(isPresented: $showSignIn) { SignInSheet { Task { await store.refresh() } } }
        .sheet(isPresented: $showReport) {
            ReportSheet(itemId: itemId, isPost: isPost) {
                store.contentChanged()
                onRemoved()
            }
        }
        .confirmationDialog(L("block_user_title"), isPresented: $confirmBlock, titleVisibility: .visible) {
            Button(L("block_confirm"), role: .destructive, action: block)
            Button(L("ui_cancel"), role: .cancel) {}
        } message: {
            let question = authorName.map { String(format: L("block_user_message"), $0) } ?? L("block_user_message_no_username")
            Text("\(question)\n\(L("block_user_info"))")
        }
        .alert(error ?? "", isPresented: Binding(get: { error != nil }, set: { if !$0 { error = nil } })) {}
    }

    private func requireAccount(_ action: () -> Void) {
        if store.account == nil { showSignIn = true } else { action() }
    }

    private func block() {
        Task {
            do {
                try await InteractionApi().block(authorId: authorId)
                Track.block(userId: authorId)
                store.contentChanged()
                onRemoved()
            } catch {
                self.error = errorMessage(error)
            }
        }
    }
}

struct ReportSheet: View {
    let itemId: String
    let isPost: Bool
    let onSubmitted: () -> Void

    private static let maxDetails = 500
    private static let reasons: [(ReportReason, String)] = [
        (.spam, "reason_spam"),
        (.scamOrMisleading, "reason_scam"),
        (.inappropriateContent, "reason_inappropriate"),
        (.maliciousLink, "reason_malicious"),
        (.other, "reason_other"),
    ]

    @Environment(\.dismiss) private var dismiss
    @State private var reason: ReportReason?
    @State private var details = ""
    @State private var isSubmitting = false
    @State private var error: String?
    @State private var submitted = false

    var body: some View {
        NavigationStack {
            Form {
                Section(L("select_reason_label")) {
                    ForEach(Self.reasons, id: \.1) { item, key in
                        Button {
                            reason = item
                        } label: {
                            HStack {
                                VStack(alignment: .leading) {
                                    Text(L(key)).foregroundStyle(.primary)
                                    Text(L(key + "_desc")).font(.caption).foregroundStyle(.secondary)
                                }
                                Spacer()
                                if reason == item { Image(systemName: "checkmark").foregroundStyle(.tint) }
                            }
                        }
                    }
                }
                Section {
                    TextField(
                        L(reason == .other ? "additional_details_placeholder_required" : "additional_details_placeholder_optional"),
                        text: $details,
                        axis: .vertical
                    )
                    .lineLimit(3...6)
                    .onChange(of: details) { _, value in
                        if value.count > Self.maxDetails { details = String(value.prefix(Self.maxDetails)) }
                    }
                } header: {
                    Text(L("additional_details_label"))
                } footer: {
                    Text(String(format: L("character_count"), details.count, Self.maxDetails))
                }
                if let error {
                    Section { Text(error).foregroundStyle(.red) }
                }
                Section {
                    Button(L("submit_report"), action: submit).bold().disabled(isSubmitting)
                }
            }
            .navigationTitle(L("report_title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("ui_close")) { dismiss() } }
            }
            .alert(L("report_submitted_success"), isPresented: $submitted) {
                Button("OK") {
                    dismiss()
                    onSubmitted()
                }
            }
        }
    }

    private func submit() {
        guard let reason else {
            error = L("error_reason_required")
            return
        }
        if reason == .other && details.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            error = L("error_details_required")
            return
        }
        error = nil
        isSubmitting = true
        Task {
            do {
                try await InteractionApi().report(itemId: itemId, isPost: isPost, reason: reason, details: details)
                Track.report(isPost: isPost, reason: reason)
                submitted = true
            } catch {
                self.error = errorMessage(error)
            }
            isSubmitting = false
        }
    }
}
