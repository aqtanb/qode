import Shared
import SwiftUI

struct ServiceSheet: View {
    let selected: [ServiceItem]
    let onToggle: (ServiceItem) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var query = ""
    @State private var popular: [ServiceItem] = []
    @State private var results: LoadState<[ServiceItem]> = .loaded([])
    @State private var selectedIds: Set<String> = []

    var body: some View {
        NavigationStack {
            List {
                if query.trimmingCharacters(in: .whitespaces).isEmpty {
                    Section(L("popular_services_title")) { rows(popular) }
                } else {
                    switch results {
                    case .loading:
                        ProgressView().frame(maxWidth: .infinity)
                    case .failed(let message):
                        Text(message).foregroundStyle(.secondary)
                    case .loaded(let items) where items.isEmpty:
                        Text(L("no_services_found_message")).foregroundStyle(.secondary)
                    case .loaded(let items):
                        rows(items)
                    }
                }
            }
            .searchable(text: $query, placement: .navigationBarDrawer(displayMode: .always), prompt: L("search_services_placeholder"))
            .toolbar {
                ToolbarItem(placement: .confirmationAction) { Button(L("ui_close")) { dismiss() } }
            }
            .task {
                selectedIds = Set(selected.map(\.id))
                popular = (try? await QodeApi().popularServices()) ?? []
            }
            .task(id: query) { await search() }
        }
    }

    private func rows(_ services: [ServiceItem]) -> some View {
        ForEach(services, id: \.id) { service in
            Button {
                if selectedIds.contains(service.id) { selectedIds.remove(service.id) } else { selectedIds.insert(service.id) }
                onToggle(service)
            } label: {
                HStack {
                    LogoView(url: service.logoUrl, size: 32)
                    Text(service.name).foregroundStyle(.primary)
                    Spacer()
                    if selectedIds.contains(service.id) { Image(systemName: "checkmark").foregroundStyle(.tint) }
                }
            }
        }
    }

    private func search() async {
        let text = query.trimmingCharacters(in: .whitespaces)
        guard !text.isEmpty else { return }
        try? await Task.sleep(for: .milliseconds(400))
        guard !Task.isCancelled else { return }
        results = .loading
        do {
            results = .loaded(try await QodeApi().searchServices(query: text))
            Track.search(term: text)
        } catch {
            if !Task.isCancelled { results = .failed(errorMessage(error)) }
        }
    }
}
