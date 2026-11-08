"""Write new deal messages from topic 24 of t.me/superpodeshevle to a JSON file.

Reads TG_API_ID, TG_API_HASH and TG_SESSION, and SUPABASE_URL and
SUPABASE_SECRET_KEY for the last message already read. Albums are merged into
one item, and photos are downloaded next to the output. Sender names are left
out on purpose.

    python fetch.py --out work/messages.json           # since the saved state
    python fetch.py --out work/messages.json --after-id 137000 --limit 50
"""

import argparse
import asyncio
import json
import os
from pathlib import Path

from telethon import TelegramClient
from telethon.sessions import StringSession

import supa

CHAT = "superpodeshevle"
TOPIC_ID = 24


async def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", required=True)
    parser.add_argument("--after-id", type=int)
    parser.add_argument("--limit", type=int, default=200)
    args = parser.parse_args()

    out = Path(args.out)
    photo_dir = out.parent / "photos"
    photo_dir.mkdir(parents=True, exist_ok=True)
    after_id = args.after_id if args.after_id is not None else supa.last_message_id()

    client = TelegramClient(
        StringSession(os.environ["TG_SESSION"]),
        int(os.environ["TG_API_ID"]),
        os.environ["TG_API_HASH"],
    )
    await client.connect()
    try:
        if not await client.is_user_authorized():
            raise SystemExit("TG_SESSION is not logged in; run login.py again.")
        chat = await client.get_entity(CHAT)

        items = {}
        newest = after_id
        async for message in client.iter_messages(chat, reply_to=TOPIC_ID, min_id=after_id, limit=args.limit, reverse=True):
            newest = max(newest, message.id)
            key = f"album-{message.grouped_id}" if message.grouped_id else f"msg-{message.id}"
            item = items.setdefault(key, {
                "id": message.id,
                "date": message.date.isoformat(),
                "text": "",
                "photos": [],
                "link": f"https://t.me/{CHAT}/{message.id}",
            })
            if message.message and not item["text"]:
                item["text"] = message.message
            if message.photo:
                path = await message.download_media(file=str(photo_dir / f"{message.id}.jpg"))
                if path:
                    item["photos"].append(str(Path(path).resolve()))

        result = {
            "after_id": after_id,
            "newest_id": newest,
            "messages": [item for item in items.values() if item["text"].strip() or item["photos"]],
        }
        out.write_text(json.dumps(result, ensure_ascii=False, indent=2))
        print(f"{len(result['messages'])} messages after {after_id}, newest {newest}, saved to {out}")
    finally:
        await client.disconnect()


if __name__ == "__main__":
    asyncio.run(main())
