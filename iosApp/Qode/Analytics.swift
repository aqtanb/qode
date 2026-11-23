import FirebaseAnalytics
import Shared

/// Firebase Analytics events with the same names and parameters as Android's core/analytics.
enum Track {
    static func login(method: String) {
        log("login", ["method": method, "success": "true"])
    }

    static func logout() {
        log("logout")
    }

    static func viewPromocode(_ item: PromocodeItem) {
        log("view_promocode", ["promocode_id": item.id, "promocode_type": item.discountKind.name])
    }

    static func copyPromocode(id: String) {
        log("copy_promocode", ["promocode_id": id])
    }

    static func vote(itemId: String, vote: VoteState) {
        log("vote", ["promocode_id": itemId, "vote_type": vote.name])
    }

    static func submitPromocode(id: String, type: DiscountKind) {
        log("submit_promocode", ["promocode_id": id, "promocode_type": type.name, "success": "true"])
    }

    static func submitPost(id: String) {
        log("submit_post", ["post_id": id, "success": "true"])
    }

    static func report(isPost: Bool, reason: ReportReason) {
        log("report_submitted", ["item_type": isPost ? "POST" : "PROMOCODE", "reason": reason.name])
    }

    static func block(userId: String) {
        log("block_user", ["blocked_user_id": userId])
    }

    static func accountDeleted() {
        log("account_deleted")
    }

    static func search(term: String) {
        log("search", ["search_term": term])
    }

    private static func log(_ name: String, _ parameters: [String: Any]? = nil) {
        Analytics.logEvent(name, parameters: parameters)
    }
}
