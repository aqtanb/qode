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
