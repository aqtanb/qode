"""Publish deals that Claude extracted from the fetched messages.

Input is a JSON file:

    {
      "newest_id": 137350,
      "items": [
        {"kind": "promocode", "source_id": 137248, "code": "ERTE30",
         "service": {"name": "FlyArystan", "site": "flyarystan.com"},
         "discount": {"type": "Percentage", "value": 10},
         "minimum_order_amount": 0, "start_date": "2026-11-08", "end_date": "2026-12-31",
         "description": "10% off flights booked 30+ days ahead", "photos": ["/abs/path.jpg"]},
        {"kind": "post", "source_id": 137297, "title": "2 coffees for 1 at Starbucks Astana",
         "content": "...", "tags": ["кофе", "астана"], "photos": ["/abs/path.jpg"]}
      ]
    }

discount.type is Percentage, FixedAmount (value in tenge) or FreeItem (with "description").
Items are checked with the app's rules; a bad item is reported and skipped, never half-saved.
The saved state moves to newest_id only after every item was handled.

Reads SUPABASE_URL, SUPABASE_SECRET_KEY and DEALS_AUTHOR_ID (the Qode Deals user id).

    python publish.py work/deals.json [--dry-run]
"""

import argparse
import json
import os
import re
import sys
import uuid
from datetime import datetime, timedelta, timezone
from pathlib import Path

import supa

CHAT_LINK = "https://t.me/superpodeshevle/"

POST_TITLE_MAX = 50
POST_CONTENT_MAX = 1000
POST_CONTENT_MAX_LINES = 10
POST_MAX_TAGS = 5
POST_TAG_MAX = 15
POST_MAX_IMAGES = 5
CODE_MAX = 50
DESCRIPTION_MAX = 1000
FREE_ITEM_MAX = 50
PROMOCODE_MAX_IMAGES = 3
SERVICE_NAME_MAX = 50
SERVICE_NAME_RE = re.compile(r"^[\w &'\-.,()]+$")
FREE_ITEM_RE = re.compile(r"^[\w \-]+$")


class Invalid(Exception):
    pass


def filter_content(text: str) -> str:
    text = re.sub(r"\n{2,}", "\n", text.strip())
    lines = text.split("\n")
    if len(lines) > POST_CONTENT_MAX_LINES:
        text = "\n".join(lines[:POST_CONTENT_MAX_LINES])
    return text[:POST_CONTENT_MAX]


def filter_tags(tags: list) -> list:
    clean = []
    for tag in tags:
        tag = "".join(ch for ch in str(tag).lower() if ch.isalnum() or ch == "_")[:POST_TAG_MAX]
        if tag and tag not in clean:
            clean.append(tag)
    return clean[:POST_MAX_TAGS]


def root_domain(site: str) -> str:
    host = site.strip().lower().removeprefix("https://").removeprefix("http://").removeprefix("www.")
    host = re.split(r"[/?#]", host)[0]
    host = re.sub(r"[^a-z0-9.-]", "", host)
    parts = [part for part in host.split(".") if part]
    return ".".join(parts[-2:]) if len(parts) >= 2 else host


ALMATY = timezone(timedelta(hours=5))


def parse_date(value: str, default: datetime, end_of_day: bool = False) -> datetime:
    """Dates are Almaty time; a bare end date means the end of that day."""
    if not value:
        return default
    parsed = datetime.fromisoformat(value)
    if len(value) == 10 and end_of_day:
        parsed = parsed.replace(hour=23, minute=59, second=59)
    return parsed if parsed.tzinfo else parsed.replace(tzinfo=ALMATY)


def promocode_id(service_name: str, code: str) -> str:
    def norm(value: str) -> str:
        return re.sub(r"\s+", "_", value.lower().strip())

    return f"{norm(service_name)}_{norm(code)}"


class Publisher:
    def __init__(self, author_id: str, dry_run: bool):
        self.author_id = author_id
        self.dry_run = dry_run
        self.logo_token = None
        self.services = {}

    def upload_photos(self, bucket: str, photos: list, limit: int) -> list:
        urls = []
        for photo in photos[:limit]:
            path = Path(photo)
            if not path.is_file():
                continue
            if self.dry_run:
                urls.append(f"(dry run) {path.name}")
                continue
            object_path = f"{self.author_id}/{uuid.uuid4().hex}.jpg"
            urls.append(supa.upload(bucket, object_path, path.read_bytes(), "image/jpeg"))
        return urls

    def find_or_create_service(self, service: dict) -> dict:
        name = str(service.get("name", "")).strip()
        if not name or len(name) > SERVICE_NAME_MAX or not SERVICE_NAME_RE.match(name):
            raise Invalid(f"bad service name {name!r}")
        if name.lower() in self.services:
            return self.services[name.lower()]
        found = supa.select("services", {"name": f"ilike.{name}", "select": "id,name", "limit": "1"})
        if found:
            self.services[name.lower()] = found[0]
            return found[0]
        site = root_domain(str(service.get("site", "")))
        if not re.fullmatch(r"[a-z0-9-]+\.[a-z]{2,}", site):
            raise Invalid(f"new service {name!r} needs a real site, got {service.get('site')!r}")
        row = {"id": uuid.uuid4().hex, "name": name, "site_url": site, "logo_url": self.logo_url(site)}
        if not self.dry_run:
            supa.insert("services", row)
        print(f"  created service {name} ({site})")
        self.services[name.lower()] = row
        return row

    def logo_url(self, site: str):
        if self.logo_token is None:
            rows = supa.select("services", {"logo_url": "like.*token=*", "select": "logo_url", "limit": "1"})
            match = re.search(r"token=([^&]+)", rows[0]["logo_url"]) if rows else None
            self.logo_token = match.group(1) if match else ""
        if not self.logo_token:
            return None
        return f"https://img.logo.dev/{site}?token={self.logo_token}&size=128&format=webp&fallback=monogram"

    def publish_post(self, item: dict) -> str:
        title = str(item.get("title", "")).strip()[:POST_TITLE_MAX]
        if not title:
            raise Invalid("empty title")
        row = {
            "id": uuid.uuid4().hex,
            "author_id": self.author_id,
            "title": title,
            "content": filter_content(str(item.get("content", ""))),
            "tags": filter_tags(item.get("tags", [])),
            "source_url": f"{CHAT_LINK}{item['source_id']}",
        }
        if supa.select("posts", {"source_url": f"eq.{row['source_url']}", "select": "id"}):
            raise Invalid("already imported")
        row["image_urls"] = self.upload_photos("post-images", item.get("photos", []), POST_MAX_IMAGES)
        if not self.dry_run:
            supa.insert("posts", row)
        return f"post {title!r}"

    def publish_promocode(self, item: dict) -> str:
        code = str(item.get("code", "")).strip()
        if not code or len(code) > CODE_MAX:
            raise Invalid(f"bad code {code!r}")
        minimum = float(item.get("minimum_order_amount") or 0)
        if minimum < 0:
            raise Invalid("negative minimum order")

        discount = item.get("discount") or {}
        kind = discount.get("type")
        row = {"discount_type": kind, "discount_value": None, "free_item_description": None}
        if kind == "Percentage":
            value = float(discount.get("value", -1))
            if not 0 <= value <= 100:
                raise Invalid(f"bad percentage {value}")
            row["discount_value"] = value
        elif kind == "FixedAmount":
            value = float(discount.get("value", -1))
            if value < 0 or value > minimum:
                raise Invalid(f"fixed amount {value} must be between 0 and the minimum order {minimum}")
            row["discount_value"] = value
        elif kind == "FreeItem":
            description = str(discount.get("description", "")).strip()
            if not description or len(description) > FREE_ITEM_MAX or not FREE_ITEM_RE.match(description):
                raise Invalid(f"bad free item {description!r}")
            row["free_item_description"] = description
        else:
            raise Invalid(f"unknown discount type {kind!r}")

        now = datetime.now(timezone.utc)
        start = parse_date(item.get("start_date"), now)
        end = parse_date(item.get("end_date"), start + timedelta(days=30), end_of_day=True)
        if end <= start:
            raise Invalid("end date is not after start date")
        if end < now:
            raise Invalid("already expired")

        description = (str(item.get("description") or "").strip() or None)
        if description and len(description) > DESCRIPTION_MAX:
            raise Invalid("description too long")

        service = self.find_or_create_service(item.get("service") or {})
        row.update({
            "id": promocode_id(service["name"], code),
            "code": code,
            "service_id": service["id"],
            "author_id": self.author_id,
            "start_date": start.isoformat(),
            "end_date": end.isoformat(),
            "minimum_order_amount": minimum,
            "description": description,
            "source_url": f"{CHAT_LINK}{item['source_id']}",
        })
        if supa.select("promocodes", {"id": f"eq.{row['id']}", "select": "id"}):
            raise Invalid(f"promocode {row['id']} already exists")
        row["image_urls"] = self.upload_photos("promocode-images", item.get("photos", []), PROMOCODE_MAX_IMAGES)
        if not self.dry_run:
            supa.insert("promocodes", row)
        return f"promocode {code} for {service['name']}"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("deals")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    data = json.loads(Path(args.deals).read_text())
    publisher = Publisher(os.environ["DEALS_AUTHOR_ID"], args.dry_run)

    published, skipped, failed = 0, 0, 0
    for item in data.get("items", []):
        try:
            if item.get("kind") == "promocode":
                result = publisher.publish_promocode(item)
            elif item.get("kind") == "post":
                result = publisher.publish_post(item)
            else:
                raise Invalid(f"unknown kind {item.get('kind')!r}")
            published += 1
            print(f"published {result} (message {item['source_id']})")
        except Invalid as error:
            skipped += 1
            print(f"skipped message {item.get('source_id')}: {error}")
        except supa.SupabaseError as error:
            if error.status == 409:
                skipped += 1
                print(f"skipped message {item.get('source_id')}: already imported")
            else:
                failed += 1
                print(f"FAILED message {item.get('source_id')}: {error}")

    if failed:
        print(f"\n{failed} items failed; state not moved so the next run retries them.")
        sys.exit(1)
    if not args.dry_run and data.get("newest_id"):
        supa.save_last_message_id(int(data["newest_id"]))
    print(f"\nDone: {published} published, {skipped} skipped. State at {data.get('newest_id')}.")


if __name__ == "__main__":
    main()
