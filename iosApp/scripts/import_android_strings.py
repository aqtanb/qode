"""Builds Qode/Localizable.xcstrings from the Android string resources for the keys the iOS app uses."""

import glob
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).resolve().parents[1] / "Qode" / "Localizable.xcstrings"
LOCALES = {"en": "values", "ru": "values-ru", "kk": "values-kk"}
KEYS = [
    "ui_home", "ui_feed", "ui_close", "copy_code", "cd_verified", "action_retry",
    "error_default_title", "error_network_no_connection",
    "empty_no_promo_codes", "empty_check_back_later",
    "filter_chip_reset", "filter_chip_sort", "filter_no_results_title", "filter_no_results_body",
    "sort_newest", "sort_popularity", "sort_section_title_newest", "sort_section_title_popularity",
    "popular_services_title", "search_services_placeholder", "no_services_found_message",
    "promocode_details_title", "promocode_details_service", "promocode_details_discount",
    "promocode_details_minimum_order", "promocode_details_start_date", "promocode_details_end_date",
    "promocode_fixed_amount_discount", "promocode_percentage_discount", "promocode_free_item_discount",
    "promocode_fixed_amount_discount_no_minimum", "promocode_percentage_discount_no_minimum",
    "promocode_free_item_discount_no_minimum",
    "ui_profile", "profile_action_signout", "continue_with_google", "auth_continue_with_email", "auth_email_placeholder", "auth_send_code",
    "auth_code_sent", "auth_code_placeholder", "auth_sign_in", "auth_change_email", "auth_resend_code",
    "error_auth_invalid_email", "error_auth_invalid_code", "error_auth_too_many_attempts",
    "error_auth_service_unavailable", "error_auth_invalid_credentials",
    "legal_consent_title", "legal_consent_subtitle", "legal_consent_accept", "legal_consent_decline",
    "terms_of_service", "privacy_policy", "error_legal_document_unavailable",
    "cd_upvote", "cd_downvote", "action_report", "action_block", "ui_cancel",
    "report_title", "submit_report", "select_reason_label", "reason_spam", "reason_spam_desc", "reason_scam",
    "reason_scam_desc", "reason_inappropriate", "reason_inappropriate_desc", "reason_malicious",
    "reason_malicious_desc", "reason_other", "reason_other_desc", "additional_details_label",
    "additional_details_placeholder_required", "additional_details_placeholder_optional", "character_count",
    "report_submitted_success", "error_reason_required", "error_details_required",
    "block_user_title", "block_user_message", "block_user_message_no_username", "block_user_info", "block_confirm",
    "profile_promocodes", "profile_posts", "profile_action_blocked", "profile_empty_promocodes_title",
    "profile_empty_promocodes_description", "profile_empty_posts_title", "profile_empty_posts_description",
    "settings_title", "settings_theme_title", "theme_system", "theme_light", "theme_dark", "settings_language_title",
    "settings_about_title", "settings_feedback_title", "about_version", "settings_delete_account_title",
    "delete_account_dialog_title", "delete_account_dialog_message", "delete_account_checkbox", "delete_account_confirm",
    "delete_account_error_title", "blocked_users_title", "blocked_users_empty", "blocked_users_empty_description",
    "unblock_button", "unblock_user_info",
]


def load(folder: str) -> dict:
    strings = {}
    for path in glob.glob(str(ROOT / "**" / "src" / "main" / "res" / folder / "strings.xml"), recursive=True):
        for node in ET.parse(path).getroot().iter("string"):
            strings.setdefault(node.get("name"), "".join(node.itertext()))
    return strings


def to_swift(value: str) -> str:
    value = value.replace("\\'", "'").replace('\\"', '"').replace("\\n", "\n")
    value = re.sub(r"%(\d+)\$s", r"%\1$@", value)
    value = value.replace("%s", "%@")
    return value


def main() -> None:
    tables = {locale: load(folder) for locale, folder in LOCALES.items()}
    missing = [k for k in KEYS if k not in tables["en"]]
    if missing:
        sys.exit(f"missing English strings: {missing}")
    catalog = {"sourceLanguage": "en", "version": "1.0", "strings": {}}
    for key in KEYS:
        localizations = {}
        for locale, table in tables.items():
            if key in table:
                localizations[locale] = {"stringUnit": {"state": "translated", "value": to_swift(table[key])}}
        catalog["strings"][key] = {"extractionState": "manual", "localizations": localizations}
    OUT.write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + "\n")
    print(f"wrote {len(KEYS)} keys to {OUT}")


if __name__ == "__main__":
    main()
