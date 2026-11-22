"""Print what the Qode Deals account published in the last 7 days, for the weekly report."""

import os
from datetime import datetime, timedelta, timezone

import supa


def main() -> None:
    since = (datetime.now(timezone.utc) - timedelta(days=7)).isoformat()
    query = {"author_id": f"eq.{os.environ['DEALS_AUTHOR_ID']}", "created_at": f"gte.{since}", "order": "created_at"}
    promocodes = supa.select("promocodes", {**query, "select": "code,discount_type,discount_value,free_item_description,services(name)"})
    posts = supa.select("posts", {**query, "select": "title"})

    print(f"Last 7 days: {len(promocodes)} promocodes, {len(posts)} posts")
    print("\nPromocodes:")
    for row in promocodes:
        discount = row["free_item_description"] or f"{row['discount_type']} {row['discount_value']:g}"
        print(f"- {row['code']} ({(row.get('services') or {}).get('name', '?')}): {discount}")
    print("\nPosts:")
    for row in posts:
        print(f"- {row['title']}")


if __name__ == "__main__":
    main()
