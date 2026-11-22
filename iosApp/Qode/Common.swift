import Shared
import SwiftUI

func L(_ key: String) -> String {
    NSLocalizedString(key, comment: "")
}

enum LoadState<Value> {
    case loading
    case loaded(Value)
    case failed(String)
}

func errorMessage(_ error: Swift.Error) -> String {
    if let qode = (error as NSError).userInfo["KotlinException"] as? QodeException, qode.isOffline {
        return L("error_network_no_connection")
    }
    return L("error_default_title")
}

enum Format {
    private static let number: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.maximumFractionDigits = 2
        return formatter
    }()

    static func amount(_ value: Double) -> String {
        number.string(from: NSNumber(value: value)) ?? String(value)
    }

    static func date(_ epochMillis: Int64) -> String {
        Date(timeIntervalSince1970: TimeInterval(epochMillis) / 1000).formatted(date: .abbreviated, time: .omitted)
    }

    static func discount(_ promocode: PromocodeItem) -> String {
        let minimum = promocode.minimumOrderAmount > 0 ? amount(promocode.minimumOrderAmount) : nil
        let (key, value): (String, String) =
            if promocode.discountKind == .percentage {
                ("promocode_percentage_discount", amount(promocode.discountValue))
            } else if promocode.discountKind == .fixedAmount {
                ("promocode_fixed_amount_discount", amount(promocode.discountValue))
            } else {
                ("promocode_free_item_discount", promocode.freeItem ?? "")
            }
        if let minimum {
            return String(format: L(key), value, minimum)
        }
        return String(format: L(key + "_no_minimum"), value)
    }
}

struct ErrorView: View {
    let message: String
    let retry: () -> Void

    var body: some View {
        ContentUnavailableView {
            Label(message, systemImage: "exclamationmark.triangle")
        } actions: {
            Button(L("action_retry"), action: retry).buttonStyle(.borderedProminent)
        }
    }
}

struct LogoView: View {
    let url: String?
    var size: CGFloat = 40

    var body: some View {
        AsyncImage(url: url.flatMap(URL.init(string:))) { image in
            image.resizable().scaledToFit()
        } placeholder: {
            Image(systemName: "tag").foregroundStyle(.secondary)
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: size / 4))
    }
}

struct RemoteImages: View {
    let urls: [String]

    var body: some View {
        if !urls.isEmpty {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(urls, id: \.self) { url in
                        AsyncImage(url: URL(string: url)) { image in
                            image.resizable().scaledToFill()
                        } placeholder: {
                            Color.secondary.opacity(0.1)
                        }
                        .frame(width: 240, height: 180)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
            }
        }
    }
}
