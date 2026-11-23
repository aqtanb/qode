"""Builds Qode/Localizable.xcstrings from the Android string resources for the keys the iOS app uses."""

import glob
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).resolve().parents[1] / "Qode" / "Localizable.xcstrings"
ERROR_KEYS_OUT = Path(__file__).resolve().parents[1] / "Qode" / "ErrorKeys.swift"
ERROR_MAPPER = ROOT / "core" / "ui" / "src" / "main" / "java" / "com" / "qodein" / "core" / "ui" / "error" / "ErrorStringMapper.kt"
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
    "app_submit_promocode", "app_create_post", "app_fab_tooltip", "action_submit",
    "promocode_service_choose_selector_label", "service_step_cant_find_service", "service_step_type_manually",
    "service_step_browse_services_instead", "promocode_service_choose_name", "promocode_service_choose_url",
    "service_step_helper_service_url", "promocode_code_choose_label", "promo_code_step_helper_text",
    "promocode_type_choose_label", "promocode_type_percentage", "promocode_type_amount", "promocode_type_gift",
    "promocode_discount_value_choose_label", "promocode_discount_percentage_helper", "promocode_discount_amount_helper",
    "promocode_free_item_placeholder", "promocode_free_item_helper", "promocode_discount_min_order_choose_label",
    "promocode_minimum_order_helper", "promocode_description_placeholder", "promocode_image_limit_reached",
    "placeholder_title", "placeholder_description", "select_tags", "post_tag_placeholder", "image_limit_reached",
    "cd_add_image", "ui_image_remove", "core_notifications_upload_progress", "ui_description",
    "display_name_label", "display_name_helper", "display_name_edit", "ui_save", "user_anonymous",
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


def error_keys() -> dict:
    """Error path (e.g. PromocodeError.CreationFailure.CodeTooShort) to string key, from Android's mapper."""
    text = ERROR_MAPPER.read_text()
    return dict(re.findall(r"([A-Z]\w*(?:\.\w+)+)\s*->\s*StringResource\(R\.string\.(\w+)", text))


def write_error_keys(errors: dict) -> None:
    lines = ["// Generated by scripts/import_android_strings.py from Android's ErrorStringMapper.kt. Don't edit.", ""]
    lines.append("let errorStringKeys: [String: String] = [")
    lines += [f'    "{path}": "{key}",' for path, key in sorted(errors.items())]
    lines.append("]")
    ERROR_KEYS_OUT.write_text("\n".join(lines) + "\n")


def main() -> None:
    tables = {locale: load(folder) for locale, folder in LOCALES.items()}
    errors = error_keys()
    write_error_keys(errors)
    keys = KEYS + sorted(set(errors.values()) - set(KEYS))
    missing = [k for k in keys if k not in tables["en"]]
    if missing:
        sys.exit(f"missing English strings: {missing}")
    catalog = {"sourceLanguage": "en", "version": "1.0", "strings": {}}
    for key in keys:
        localizations = {}
        for locale, table in tables.items():
            if key in table:
                localizations[locale] = {"stringUnit": {"state": "translated", "value": to_swift(table[key])}}
        catalog["strings"][key] = {"extractionState": "manual", "localizations": localizations}
    OUT.write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + "\n")
    print(f"wrote {len(keys)} keys to {OUT} and {len(errors)} error mappings to {ERROR_KEYS_OUT}")


if __name__ == "__main__":
    main()
