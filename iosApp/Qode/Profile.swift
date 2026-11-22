import Shared
import SwiftUI

enum AppRoute: Hashable {
    case profile
    case settings
    case blockedUsers
    case promocode(String)
    case post(String)

    @ViewBuilder
    var destination: some View {
        switch self {
        case .profile: ProfileView()
        case .settings: SettingsView()
        case .blockedUsers: BlockedUsersView()
        case .promocode(let id): PromocodeDetailView(id: id)
        case .post(let id): PostDetailView(id: id)
        }
    }
}

@MainActor
@Observable
final class ProfileModel {
    enum Tab { case promocodes, posts }

    private let api = ProfileApi()

    var profile: LoadState<ProfileItem?> = .loading
    var tab = Tab.promocodes
    var promocodes: LoadState<[PromocodeItem]> = .loading
    var posts: LoadState<[PostItem]> = .loading

    var hasMore: Bool { tab == .promocodes ? api.hasMorePromocodes : api.hasMorePosts }

    func load() async {
        do {
            profile = .loaded(try await api.profile())
        } catch {
            profile = .failed(errorMessage(error))
        }
        await loadTab()
    }

    func loadTab() async {
        switch tab {
        case .promocodes:
            do { promocodes = .loaded(try await api.myPromocodes(nextPage: false)) } catch { promocodes = .failed(errorMessage(error)) }
        case .posts:
            do { posts = .loaded(try await api.myPosts(nextPage: false)) } catch { posts = .failed(errorMessage(error)) }
        }
    }

    func loadMore() async {
        switch tab {
        case .promocodes:
            guard case .loaded(let current) = promocodes, let next = try? await api.myPromocodes(nextPage: true) else { return }
            promocodes = .loaded(current + next)
        case .posts:
            guard case .loaded(let current) = posts, let next = try? await api.myPosts(nextPage: true) else { return }
            posts = .loaded(current + next)
        }
    }
}

struct ProfileView: View {
    @Environment(AccountStore.self) private var store
    @Environment(\.dismiss) private var dismiss
    @State private var model = ProfileModel()

    var body: some View {
        List {
            Section { header }
            Section {
                Picker("", selection: $model.tab) {
                    Text(L("profile_promocodes")).tag(ProfileModel.Tab.promocodes)
                    Text(L("profile_posts")).tag(ProfileModel.Tab.posts)
                }
                .pickerStyle(.segmented)
                .listRowBackground(Color.clear)
                .listRowInsets(EdgeInsets())
                items
            }
            Section {
                NavigationLink(value: AppRoute.blockedUsers) {
                    Label(L("profile_action_blocked"), systemImage: "hand.raised")
                }
                Button(L("profile_action_signout"), role: .destructive) {
                    Task {
                        await store.signOut()
                        dismiss()
                    }
                }
            }
        }
        .navigationTitle(L("ui_profile"))
        .navigationBarTitleDisplayMode(.inline)
        .task { await model.load() }
        .refreshable { await model.load() }
        .onChange(of: model.tab) { Task { await model.loadTab() } }
    }

    @ViewBuilder
    private var header: some View {
        switch model.profile {
        case .loading:
            ProgressView().frame(maxWidth: .infinity)
        case .failed(let message):
            ErrorView(message: message) { Task { await model.load() } }
        case .loaded(let profile):
            HStack(spacing: 16) {
                AsyncImage(url: profile?.photoUrl.flatMap(URL.init(string:))) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Image(systemName: "person.crop.circle.fill").resizable().foregroundStyle(.secondary)
                }
                .frame(width: 64, height: 64)
                .clipShape(Circle())
                VStack(alignment: .leading, spacing: 4) {
                    Text(profile?.displayName ?? profile?.email ?? L("ui_profile")).font(.title3).bold()
                    if let profile {
                        Text("\(L("profile_promocodes")): \(profile.promocodeCount) · \(L("profile_posts")): \(profile.postCount)")
                            .font(.caption).foregroundStyle(.secondary)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var items: some View {
        switch model.tab {
        case .promocodes:
            rows(model.promocodes, emptyTitle: "profile_empty_promocodes_title", emptyBody: "profile_empty_promocodes_description") { item in
                NavigationLink(value: AppRoute.promocode(item.id)) { PromocodeRow(item: item) }
            }
        case .posts:
            rows(model.posts, emptyTitle: "profile_empty_posts_title", emptyBody: "profile_empty_posts_description") { post in
                NavigationLink(value: AppRoute.post(post.id)) { PostRow(post: post) }
            }
        }
    }

    @ViewBuilder
    private func rows<Item: AnyObject, Row: View>(
        _ state: LoadState<[Item]>,
        emptyTitle: String,
        emptyBody: String,
        @ViewBuilder row: @escaping (Item) -> Row
    ) -> some View {
        switch state {
        case .loading:
            ProgressView().frame(maxWidth: .infinity)
        case .failed(let message):
            ErrorView(message: message) { Task { await model.loadTab() } }
        case .loaded(let items) where items.isEmpty:
            ContentUnavailableView(L(emptyTitle), systemImage: "tray", description: Text(L(emptyBody)))
        case .loaded(let items):
            ForEach(items.indices, id: \.self) { row(items[$0]) }
            if model.hasMore {
                ProgressView().frame(maxWidth: .infinity).task { await model.loadMore() }
            }
        }
    }
}

struct BlockedUsersView: View {
    @Environment(AccountStore.self) private var store
    @State private var api = ProfileApi()
    @State private var users: LoadState<[BlockedUserItem]> = .loading
    @State private var error: String?

    var body: some View {
        Group {
            switch users {
            case .loading:
                ProgressView()
            case .failed(let message):
                ErrorView(message: message) { Task { await load() } }
            case .loaded(let items) where items.isEmpty:
                ContentUnavailableView(
                    L("blocked_users_empty"),
                    systemImage: "hand.raised",
                    description: Text(L("blocked_users_empty_description"))
                )
            case .loaded(let items):
                List {
                    ForEach(items, id: \.id) { user in
                        HStack {
                            AsyncImage(url: user.photoUrl.flatMap(URL.init(string:))) { image in
                                image.resizable().scaledToFill()
                            } placeholder: {
                                Image(systemName: "person.crop.circle.fill").resizable().foregroundStyle(.secondary)
                            }
                            .frame(width: 36, height: 36)
                            .clipShape(Circle())
                            Text(user.displayName ?? "—")
                            Spacer()
                            Button(L("unblock_button")) { unblock(user) }.buttonStyle(.bordered)
                        }
                    }
                    if api.hasMoreBlocked {
                        ProgressView().frame(maxWidth: .infinity).task { await loadMore() }
                    }
                }
            }
        }
        .navigationTitle(L("blocked_users_title"))
        .navigationBarTitleDisplayMode(.inline)
        .task { await load() }
        .alert(error ?? "", isPresented: Binding(get: { error != nil }, set: { if !$0 { error = nil } })) {}
    }

    private func load() async {
        do {
            users = .loaded(try await api.blockedUsers(nextPage: false))
        } catch {
            users = .failed(errorMessage(error))
        }
    }

    private func loadMore() async {
        guard case .loaded(let current) = users, let next = try? await api.blockedUsers(nextPage: true) else { return }
        users = .loaded(current + next)
    }

    private func unblock(_ user: BlockedUserItem) {
        Task {
            do {
                try await api.unblock(userId: user.id)
                if case .loaded(let current) = users {
                    users = .loaded(current.filter { $0.id != user.id })
                }
                store.contentChanged()
            } catch {
                self.error = errorMessage(error)
            }
        }
    }
}
