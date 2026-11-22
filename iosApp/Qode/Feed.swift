import Shared
import SwiftUI

@MainActor
@Observable
final class FeedModel {
    private let api = QodeApi()

    var posts: LoadState<[PostItem]> = .loading
    var isLoadingMore = false
    var hasMore: Bool { api.hasMorePosts }

    func load() async {
        do {
            posts = .loaded(try await api.posts(nextPage: false))
        } catch {
            posts = .failed(errorMessage(error))
        }
    }

    func loadMore() async {
        guard case .loaded(let current) = posts, hasMore, !isLoadingMore else { return }
        isLoadingMore = true
        defer { isLoadingMore = false }
        if let next = try? await api.posts(nextPage: true) {
            posts = .loaded(current + next)
        }
    }
}

struct FeedView: View {
    @Environment(AccountStore.self) private var store
    @State private var model = FeedModel()

    var body: some View {
        Group {
            switch model.posts {
            case .loading:
                ProgressView()
            case .failed(let message):
                ErrorView(message: message) {
                    model.posts = .loading
                    Task { await model.load() }
                }
            case .loaded(let posts):
                List {
                    ForEach(posts, id: \.id) { post in
                        NavigationLink(value: post.id) { PostRow(post: post) }
                    }
                    if model.hasMore {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                            .task { await model.loadMore() }
                    }
                }
                .refreshable { await model.load() }
            }
        }
        .navigationTitle(L("ui_feed"))
        .toolbar { AccountButton() }
        .navigationDestination(for: String.self) { PostDetailView(id: $0) }
        .task { await model.load() }
        .onChange(of: store.contentVersion) { Task { await model.load() } }
        .onChange(of: store.account?.id) { Task { await model.load() } }
    }
}

struct PostRow: View {
    let post: PostItem

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(post.authorName).font(.caption).foregroundStyle(.secondary)
                Spacer()
                Text(Format.date(post.createdEpochMillis)).font(.caption).foregroundStyle(.secondary)
            }
            Text(post.title).font(.headline)
            Text(post.content).font(.subheadline).foregroundStyle(.secondary).lineLimit(3)
            if let first = post.imageUrls.first {
                AsyncImage(url: URL(string: first)) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Color.secondary.opacity(0.1)
                }
                .frame(height: 160)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            Label("\(post.voteScore)", systemImage: "arrow.up").font(.caption).foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
}

struct PostDetailView: View {
    let id: String
    @Environment(\.dismiss) private var dismiss
    @State private var state: LoadState<PostItem> = .loading

    var body: some View {
        Group {
            switch state {
            case .loading:
                ProgressView()
            case .failed(let message):
                ErrorView(message: message) { Task { await load() } }
            case .loaded(let post):
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        Text(post.title).font(.title2).bold()
                        Text("\(post.authorName) · \(Format.date(post.createdEpochMillis))")
                            .font(.caption).foregroundStyle(.secondary)
                        MarkdownText(text: post.content)
                        RemoteImages(urls: post.imageUrls)
                        if !post.tags.isEmpty {
                            Text(post.tags.map { "#\($0)" }.joined(separator: " ")).font(.footnote).foregroundStyle(.tint)
                        }
                        VoteControl(itemId: post.id, isPost: true, baseScore: post.voteScore)
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
                .toolbar {
                    ContentMenu(itemId: post.id, isPost: true, authorId: post.authorId, authorName: post.authorName) { dismiss() }
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .task { await load() }
    }

    private func load() async {
        state = .loading
        do {
            state = .loaded(try await QodeApi().post(id: id))
        } catch {
            state = .failed(errorMessage(error))
        }
    }
}
