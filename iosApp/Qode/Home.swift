import Shared
import SwiftUI

@MainActor
@Observable
final class HomeModel {
    private let api = QodeApi()

    var banners: [BannerItem] = []
    var popularServices: [ServiceItem] = []
    var promocodes: LoadState<[PromocodeItem]> = .loading
    var selectedServices: [ServiceItem] = []
    var newest = false
    var isLoadingMore = false
    var hasMore: Bool { api.hasMorePromocodes }

    func load() async {
        async let banners = try? api.banners(languageCode: Locale.current.language.languageCode?.identifier ?? "default")
        async let services = try? api.popularServices()
        await reloadPromocodes()
        self.banners = await banners ?? []
        popularServices = await services ?? []
    }

    func reloadPromocodes() async {
        promocodes = .loading
        do {
            promocodes = .loaded(try await api.promocodes(newest: newest, serviceIds: selectedServices.map(\.id), nextPage: false))
        } catch {
            promocodes = .failed(errorMessage(error))
        }
    }

    func loadMore() async {
        guard case .loaded(let current) = promocodes, hasMore, !isLoadingMore else { return }
        isLoadingMore = true
        defer { isLoadingMore = false }
        if let next = try? await api.promocodes(newest: newest, serviceIds: selectedServices.map(\.id), nextPage: true) {
            promocodes = .loaded(current + next)
        }
    }

    func toggle(_ service: ServiceItem) async {
        if let index = selectedServices.firstIndex(where: { $0.id == service.id }) {
            selectedServices.remove(at: index)
        } else {
            selectedServices.append(service)
        }
        await reloadPromocodes()
    }

    func resetServices() async {
        selectedServices = []
        await reloadPromocodes()
    }
}

struct HomeView: View {
    @Environment(AccountStore.self) private var store
    @State private var model = HomeModel()
    @State private var showServices = false

    var body: some View {
        List {
            if !model.banners.isEmpty {
                Section { BannerCarousel(banners: model.banners) }
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.clear)
            }
            Section { serviceChips }
                .listRowInsets(EdgeInsets(top: 8, leading: 0, bottom: 8, trailing: 0))
                .listRowBackground(Color.clear)
            Section {
                promocodeRows
            } header: {
                HStack {
                    Text(L(model.newest ? "sort_section_title_newest" : "sort_section_title_popularity"))
                    Spacer()
                    sortMenu
                }
            }
        }
        .navigationTitle("Qode")
        .navigationDestination(for: String.self) { PromocodeDetailView(id: $0) }
        .sheet(isPresented: $showServices) {
            ServiceSheet(selected: model.selectedServices) { service in
                Task { await model.toggle(service) }
            }
        }
        .task { await model.load() }
        .refreshable { await model.load() }
        .onChange(of: store.contentVersion) { Task { await model.reloadPromocodes() } }
        .onChange(of: store.account?.id) { Task { await model.reloadPromocodes() } }
    }

    private var serviceChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                Button {
                    showServices = true
                } label: {
                    Label(L("popular_services_title"), systemImage: "magnifyingglass")
                }
                .buttonStyle(.bordered)
                if !model.selectedServices.isEmpty {
                    Button(L("filter_chip_reset")) { Task { await model.resetServices() } }
                        .buttonStyle(.bordered)
                }
                ForEach(chipServices, id: \.id) { service in
                    let selected = model.selectedServices.contains { $0.id == service.id }
                    Button(service.name) { Task { await model.toggle(service) } }
                        .buttonStyle(.bordered)
                        .tint(selected ? .accentColor : .secondary)
                }
            }
            .padding(.horizontal)
        }
    }

    private var chipServices: [ServiceItem] {
        let selectedIds = Set(model.selectedServices.map(\.id))
        return model.selectedServices + model.popularServices.filter { !selectedIds.contains($0.id) }
    }

    private var sortMenu: some View {
        Menu {
            Picker(L("filter_chip_sort"), selection: Binding(
                get: { model.newest },
                set: { value in
                    model.newest = value
                    Task { await model.reloadPromocodes() }
                }
            )) {
                Text(L("sort_popularity")).tag(false)
                Text(L("sort_newest")).tag(true)
            }
        } label: {
            Image(systemName: "arrow.up.arrow.down")
        }
    }

    @ViewBuilder
    private var promocodeRows: some View {
        switch model.promocodes {
        case .loading:
            ProgressView().frame(maxWidth: .infinity)
        case .failed(let message):
            ErrorView(message: message) { Task { await model.reloadPromocodes() } }
        case .loaded(let items) where items.isEmpty:
            ContentUnavailableView(
                model.selectedServices.isEmpty ? L("empty_no_promo_codes") : L("filter_no_results_title"),
                systemImage: "tag",
                description: Text(model.selectedServices.isEmpty ? L("empty_check_back_later") : L("filter_no_results_body"))
            )
        case .loaded(let items):
            ForEach(items, id: \.id) { item in
                NavigationLink(value: item.id) { PromocodeRow(item: item) }
            }
            if model.hasMore {
                ProgressView()
                    .frame(maxWidth: .infinity)
                    .task { await model.loadMore() }
            }
        }
    }
}

struct PromocodeRow: View {
    let item: PromocodeItem

    var body: some View {
        HStack(spacing: 12) {
            LogoView(url: item.serviceLogoUrl)
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 4) {
                    Text(item.serviceName).font(.headline)
                    if item.isVerified {
                        Image(systemName: "checkmark.seal.fill").foregroundStyle(.blue).accessibilityLabel(L("cd_verified"))
                    }
                }
                Text(Format.discount(item)).font(.subheadline).foregroundStyle(.secondary).lineLimit(2)
                Text(item.code).font(.system(.callout, design: .monospaced)).bold()
            }
            Spacer()
            Label("\(item.voteScore)", systemImage: "arrow.up").font(.caption).foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
}

struct BannerCarousel: View {
    let banners: [BannerItem]
    @Environment(\.openURL) private var openURL

    var body: some View {
        TabView {
            ForEach(banners, id: \.id) { banner in
                Button {
                    if let url = banner.ctaUrl.flatMap(URL.init(string:)) { openURL(url) }
                } label: {
                    AsyncImage(url: URL(string: banner.imageUrl)) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Color.secondary.opacity(0.1)
                    }
                    .overlay(alignment: .bottomLeading) {
                        if let title = banner.title, !title.isEmpty {
                            VStack(alignment: .leading) {
                                Text(title).font(.headline)
                                if let description = banner.description_ { Text(description).font(.caption) }
                            }
                            .foregroundStyle(.white)
                            .padding()
                            .shadow(radius: 4)
                        }
                    }
                    .clipped()
                }
                .buttonStyle(.plain)
            }
        }
        .tabViewStyle(.page)
        .frame(height: 180)
    }
}
