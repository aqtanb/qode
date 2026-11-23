import PhotosUI
import Shared
import SwiftUI

/// A picked photo, scaled down and saved as JPEG so it fits the buckets' 5 MB / JPEG-PNG-WebP rules.
struct ImageAttachment: Identifiable {
    let id = UUID()
    let path: String
    let thumbnail: UIImage

    private static let maxDimension: CGFloat = 2048

    static func load(_ item: PhotosPickerItem) async -> ImageAttachment? {
        guard let data = try? await item.loadTransferable(type: Data.self), let image = UIImage(data: data) else { return nil }
        let scale = min(1, maxDimension / max(image.size.width, image.size.height))
        let size = CGSize(width: image.size.width * scale, height: image.size.height * scale)
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1
        let resized = UIGraphicsImageRenderer(size: size, format: format).image { _ in image.draw(in: CGRect(origin: .zero, size: size)) }
        guard let jpeg = resized.jpegData(compressionQuality: 0.8) else { return nil }
        let url = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString + ".jpg")
        guard (try? jpeg.write(to: url)) != nil else { return nil }
        return ImageAttachment(path: url.path, thumbnail: resized)
    }
}

struct PhotosSection: View {
    @Binding var attachments: [ImageAttachment]
    let limit: Int
    let limitKey: String

    @State private var picked: [PhotosPickerItem] = []

    var body: some View {
        Section {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(attachments) { attachment in
                        Image(uiImage: attachment.thumbnail)
                            .resizable()
                            .scaledToFill()
                            .frame(width: 88, height: 88)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                            .overlay(alignment: .topTrailing) {
                                Button {
                                    attachments.removeAll { $0.id == attachment.id }
                                } label: {
                                    Image(systemName: "xmark.circle.fill").symbolRenderingMode(.palette)
                                        .foregroundStyle(.white, .black.opacity(0.6))
                                }
                                .accessibilityLabel(L("ui_image_remove"))
                                .padding(4)
                            }
                    }
                    if attachments.count < limit {
                        PhotosPicker(selection: $picked, maxSelectionCount: limit - attachments.count, matching: .images) {
                            Image(systemName: "photo.badge.plus")
                                .font(.title2)
                                .frame(width: 88, height: 88)
                                .background(Color.secondary.opacity(0.12), in: RoundedRectangle(cornerRadius: 10))
                        }
                        .accessibilityLabel(L("cd_add_image"))
                    }
                }
            }
        } footer: {
            Text(L(limitKey))
        }
        .onChange(of: picked) { _, items in
            guard !items.isEmpty else { return }
            picked = []
            Task {
                for item in items where attachments.count < limit {
                    if let attachment = await ImageAttachment.load(item) { attachments.append(attachment) }
                }
            }
        }
    }
}

/// The "+" toolbar menu on Home and Feed.
struct SubmitMenu: View {
    private enum Form: Identifiable {
        case promocode, post
        var id: Self { self }
    }

    @Environment(AccountStore.self) private var store
    @State private var form: Form?
    @State private var showSignIn = false

    var body: some View {
        Menu {
            Button(L("app_submit_promocode"), systemImage: "tag") { open(.promocode) }
            Button(L("app_create_post"), systemImage: "square.and.pencil") { open(.post) }
        } label: {
            Image(systemName: "plus.circle.fill")
        }
        .accessibilityLabel(L("app_fab_tooltip"))
        .sheet(item: $form) { form in
            switch form {
            case .promocode: PromocodeForm { store.contentChanged() }
            case .post: PostForm { store.contentChanged() }
            }
        }
        .sheet(isPresented: $showSignIn) { SignInSheet { Task { await store.refresh() } } }
    }

    private func open(_ target: Form) {
        if store.account == nil { showSignIn = true } else { form = target }
    }
}

/// Runs [work] as an iOS background task so a locked phone doesn't cut an upload short.
@MainActor
func submitInBackground(_ work: @escaping () async throws -> Void) async throws {
    let task = UIApplication.shared.beginBackgroundTask()
    defer { UIApplication.shared.endBackgroundTask(task) }
    try await work()
}

struct SubmitProgress: View {
    let current: Int
    let total: Int

    var body: some View {
        VStack(spacing: 8) {
            ProgressView()
            if total > 0 {
                Text(String(format: L("core_notifications_upload_progress"), current, total)).font(.footnote)
            }
        }
        .padding(24)
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 16))
    }
}

struct PromocodeForm: View {
    let onSubmitted: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var service: ServiceItem?
    @State private var typeServiceManually = false
    @State private var serviceName = ""
    @State private var serviceSite = ""
    @State private var showServicePicker = false
    @State private var code = ""
    @State private var discountKind = DiscountKind.percentage
    @State private var discountValue = ""
    @State private var freeItem = ""
    @State private var minimumOrder = ""
    @State private var startDate = Date()
    @State private var endDate = Calendar.current.date(byAdding: .day, value: 30, to: Date()) ?? Date()
    @State private var description = ""
    @State private var attachments: [ImageAttachment] = []
    @State private var progress: (Int, Int)?
    @State private var error: String?

    var body: some View {
        NavigationStack {
            Form {
                serviceSection
                Section {
                    TextField(L("promocode_code_choose_label"), text: $code)
                        .textInputAutocapitalization(.characters)
                        .autocorrectionDisabled()
                        .font(.system(.body, design: .monospaced))
                } footer: {
                    Text(L("promo_code_step_helper_text"))
                }
                discountSection
                Section {
                    DatePicker(L("promocode_details_start_date"), selection: $startDate, displayedComponents: .date)
                    DatePicker(L("promocode_details_end_date"), selection: $endDate, in: startDate..., displayedComponents: .date)
                }
                Section(L("ui_description")) {
                    TextField(L("promocode_description_placeholder"), text: $description, axis: .vertical).lineLimit(2...6)
                }
                PhotosSection(attachments: $attachments, limit: 3, limitKey: "promocode_image_limit_reached")
                if let error {
                    Section { Text(error).foregroundStyle(.red) }
                }
            }
            .disabled(progress != nil)
            .overlay { if let progress { SubmitProgress(current: progress.0, total: progress.1) } }
            .navigationTitle(L("app_submit_promocode"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("ui_cancel")) { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button(L("action_submit"), action: submit).disabled(!canSubmit || progress != nil)
                }
            }
            .sheet(isPresented: $showServicePicker) { ServicePicker { service = $0 } }
        }
        .interactiveDismissDisabled(progress != nil)
    }

    private var serviceSection: some View {
        Section(L("promocode_details_service")) {
            if typeServiceManually {
                TextField(L("promocode_service_choose_name"), text: $serviceName)
                TextField(L("promocode_service_choose_url"), text: $serviceSite)
                    .keyboardType(.URL)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                Button(L("service_step_browse_services_instead")) { typeServiceManually = false }
            } else {
                Button {
                    showServicePicker = true
                } label: {
                    HStack {
                        if let service {
                            LogoView(url: service.logoUrl, size: 28)
                            Text(service.name).foregroundStyle(.primary)
                        } else {
                            Text(L("promocode_service_choose_selector_label"))
                        }
                        Spacer()
                        Image(systemName: "chevron.right").foregroundStyle(.tertiary)
                    }
                }
                Button(L("service_step_type_manually")) {
                    service = nil
                    typeServiceManually = true
                }
            }
        }
    }

    private var discountSection: some View {
        Section {
            Picker(L("promocode_type_choose_label"), selection: $discountKind) {
                Text(L("promocode_type_percentage")).tag(DiscountKind.percentage)
                Text(L("promocode_type_amount")).tag(DiscountKind.fixedAmount)
                Text(L("promocode_type_gift")).tag(DiscountKind.freeItem)
            }
            .pickerStyle(.segmented)
            if discountKind == .freeItem {
                TextField(L("promocode_free_item_placeholder"), text: $freeItem)
            } else {
                TextField(L("promocode_discount_value_choose_label"), text: $discountValue).keyboardType(.decimalPad)
            }
            TextField(L("promocode_discount_min_order_choose_label"), text: $minimumOrder).keyboardType(.decimalPad)
        } header: {
            Text(L("promocode_details_discount"))
        } footer: {
            Text(L(discountKind == .percentage ? "promocode_discount_percentage_helper"
                : discountKind == .fixedAmount ? "promocode_discount_amount_helper" : "promocode_free_item_helper"))
        }
    }

    private var canSubmit: Bool {
        let hasService = typeServiceManually ? !serviceName.isBlank && !serviceSite.isBlank : service != nil
        let hasDiscount = discountKind == .freeItem ? !freeItem.isBlank : number(discountValue) != nil
        return hasService && !code.isBlank && hasDiscount
    }

    private func number(_ text: String) -> Double? {
        Double(text.replacingOccurrences(of: ",", with: ".").replacingOccurrences(of: " ", with: ""))
    }

    private func submit() {
        error = nil
        progress = (0, 0)
        let start = Calendar.current.startOfDay(for: startDate)
        let end = Calendar.current.date(bySettingHour: 23, minute: 59, second: 59, of: endDate) ?? endDate
        Task {
            do {
                try await submitInBackground {
                    let id = try await SubmitApi().submitPromocode(
                        code: code.trimmingCharacters(in: .whitespaces),
                        serviceId: typeServiceManually ? nil : service?.id,
                        serviceName: serviceName,
                        serviceSite: serviceSite,
                        discountKind: discountKind,
                        discountValue: number(discountValue) ?? 0,
                        freeItem: freeItem,
                        minimumOrderAmount: number(minimumOrder) ?? 0,
                        startEpochMillis: Int64(start.timeIntervalSince1970 * 1000),
                        endEpochMillis: Int64(end.timeIntervalSince1970 * 1000),
                        description: description,
                        imagePaths: attachments.map(\.path)
                    ) { current, total in
                        Task { @MainActor in progress = (Int(truncating: current), Int(truncating: total)) }
                    }
                    Track.submitPromocode(id: id, type: discountKind)
                }
                onSubmitted()
                dismiss()
            } catch {
                self.error = errorMessage(error)
            }
            progress = nil
        }
    }
}

struct ServicePicker: View {
    let onPick: (ServiceItem) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var query = ""
    @State private var services: [ServiceItem] = []

    var body: some View {
        NavigationStack {
            List(services, id: \.id) { service in
                Button {
                    onPick(service)
                    dismiss()
                } label: {
                    HStack {
                        LogoView(url: service.logoUrl, size: 32)
                        Text(service.name).foregroundStyle(.primary)
                    }
                }
            }
            .searchable(text: $query, placement: .navigationBarDrawer(displayMode: .always), prompt: L("search_services_placeholder"))
            .navigationTitle(L("promocode_service_choose_selector_label"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button(L("ui_close")) { dismiss() } } }
            .task(id: query) {
                let text = query.trimmingCharacters(in: .whitespaces)
                if !text.isEmpty {
                    try? await Task.sleep(for: .milliseconds(400))
                    guard !Task.isCancelled else { return }
                }
                let api = QodeApi()
                let result = text.isEmpty ? try? await api.popularServices() : try? await api.searchServices(query: text)
                if !Task.isCancelled { services = result ?? [] }
            }
        }
    }
}

struct PostForm: View {
    let onSubmitted: () -> Void

    private static let maxTags = 5

    @Environment(\.dismiss) private var dismiss
    @State private var title = ""
    @State private var content = ""
    @State private var tagInput = ""
    @State private var tags: [String] = []
    @State private var attachments: [ImageAttachment] = []
    @State private var progress: (Int, Int)?
    @State private var error: String?

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField(L("placeholder_title"), text: $title)
                        .onChange(of: title) { _, value in if value.count > 50 { title = String(value.prefix(50)) } }
                    TextField(L("placeholder_description"), text: $content, axis: .vertical)
                        .lineLimit(4...10)
                        .onChange(of: content) { _, value in if value.count > 1000 { content = String(value.prefix(1000)) } }
                }
                Section(L("select_tags")) {
                    if !tags.isEmpty {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack {
                                ForEach(tags, id: \.self) { tag in
                                    Button("#\(tag) ✕") { tags.removeAll { $0 == tag } }.buttonStyle(.bordered)
                                }
                            }
                        }
                    }
                    if tags.count < Self.maxTags {
                        TextField(L("post_tag_placeholder"), text: $tagInput)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                            .onSubmit(addTag)
                    }
                }
                PhotosSection(attachments: $attachments, limit: 5, limitKey: "image_limit_reached")
                if let error {
                    Section { Text(error).foregroundStyle(.red) }
                }
            }
            .disabled(progress != nil)
            .overlay { if let progress { SubmitProgress(current: progress.0, total: progress.1) } }
            .navigationTitle(L("app_create_post"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(L("ui_cancel")) { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button(L("action_submit"), action: submit).disabled(title.isBlank || progress != nil)
                }
            }
        }
        .interactiveDismissDisabled(progress != nil)
    }

    private func addTag() {
        let tag = tagInput.lowercased().filter { $0.isLetter || $0.isNumber || $0 == "_" }
        tagInput = ""
        guard !tag.isEmpty, !tags.contains(tag), tags.count < Self.maxTags else { return }
        tags.append(tag)
    }

    private func submit() {
        addTag()
        error = nil
        progress = (0, 0)
        Task {
            do {
                try await submitInBackground {
                    let id = try await SubmitApi().submitPost(
                        title: title.trimmingCharacters(in: .whitespacesAndNewlines),
                        content: content.trimmingCharacters(in: .whitespacesAndNewlines),
                        tags: tags,
                        imagePaths: attachments.map(\.path)
                    ) { current, total in
                        Task { @MainActor in progress = (Int(truncating: current), Int(truncating: total)) }
                    }
                    Track.submitPost(id: id)
                }
                onSubmitted()
                dismiss()
            } catch {
                self.error = errorMessage(error)
            }
            progress = nil
        }
    }
}

extension String {
    var isBlank: Bool { trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
}
