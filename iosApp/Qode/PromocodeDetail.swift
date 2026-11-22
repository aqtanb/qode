import Shared
import SwiftUI

struct PromocodeDetailView: View {
    let id: String
    @State private var state: LoadState<PromocodeItem> = .loading
    @State private var copied = false

    var body: some View {
        Group {
            switch state {
            case .loading:
                ProgressView()
            case .failed(let message):
                ErrorView(message: message) { Task { await load() } }
            case .loaded(let item):
                content(item)
            }
        }
        .navigationTitle(L("promocode_details_title"))
        .navigationBarTitleDisplayMode(.inline)
        .task { await load() }
    }

    private func load() async {
        state = .loading
        do {
            state = .loaded(try await QodeApi().promocode(id: id))
        } catch {
            state = .failed(errorMessage(error))
        }
    }

    private func content(_ item: PromocodeItem) -> some View {
        List {
            Section {
                HStack(spacing: 12) {
                    LogoView(url: item.serviceLogoUrl, size: 56)
                    VStack(alignment: .leading) {
                        Text(item.serviceName).font(.title3).bold()
                        if let site = item.serviceSiteUrl { Text(site).font(.caption).foregroundStyle(.secondary) }
                    }
                }
                Button {
                    UIPasteboard.general.string = item.code
                    copied = true
                } label: {
                    HStack {
                        Text(item.code).font(.system(.title2, design: .monospaced)).bold()
                        Spacer()
                        Image(systemName: copied ? "checkmark" : "doc.on.doc")
                    }
                }
                .accessibilityLabel(L("copy_code"))
                .sensoryFeedback(.success, trigger: copied)
            }
            Section {
                LabeledContent(L("promocode_details_discount"), value: Format.discount(item))
                if item.minimumOrderAmount > 0 {
                    LabeledContent(L("promocode_details_minimum_order"), value: "\(Format.amount(item.minimumOrderAmount))₸")
                }
                LabeledContent(L("promocode_details_start_date"), value: Format.date(item.startEpochMillis))
                LabeledContent(L("promocode_details_end_date"), value: Format.date(item.endEpochMillis))
            }
            if let description = item.description_, !description.isEmpty {
                Section { Text(description) }
            }
            if !item.imageUrls.isEmpty {
                Section { RemoteImages(urls: item.imageUrls) }
            }
        }
    }
}
