"""Hand fetched messages from the GitHub Action to the routine through the private
deals-inbox bucket. Each upload replaces the previous batch under latest/.

    python inbox.py upload work/messages.json      # GitHub Action, after fetch.py
    python inbox.py download work/messages.json    # routine, before extracting deals
"""

import json
import sys
from pathlib import Path

import supa

BUCKET = "deals-inbox"
PREFIX = "latest"


def upload(messages_file: Path) -> None:
    data = json.loads(messages_file.read_text())
    old = supa._request("POST", f"/storage/v1/object/list/{BUCKET}", {"prefix": f"{PREFIX}/photos", "limit": 1000}) or []
    if old:
        supa._request("DELETE", f"/storage/v1/object/{BUCKET}", {"prefixes": [f"{PREFIX}/photos/{o['name']}" for o in old]})
    for message in data["messages"]:
        names = []
        for photo in message["photos"]:
            name = Path(photo).name
            supa.upload(BUCKET, f"{PREFIX}/photos/{name}", Path(photo).read_bytes(), "image/jpeg", upsert=True)
            names.append(f"photos/{name}")
        message["photos"] = names
    supa.upload(BUCKET, f"{PREFIX}/messages.json", json.dumps(data, ensure_ascii=False).encode(), "application/json", upsert=True)
    print(f"Uploaded {len(data['messages'])} messages (after {data['after_id']}, newest {data['newest_id']}).")


def download(messages_file: Path) -> None:
    photo_dir = messages_file.parent / "photos"
    photo_dir.mkdir(parents=True, exist_ok=True)
    data = json.loads(supa.download(BUCKET, f"{PREFIX}/messages.json"))
    for message in data["messages"]:
        paths = []
        for photo in message["photos"]:
            target = (messages_file.parent / photo).resolve()
            target.write_bytes(supa.download(BUCKET, f"{PREFIX}/{photo}"))
            paths.append(str(target))
        message["photos"] = paths
    messages_file.write_text(json.dumps(data, ensure_ascii=False, indent=2))
    print(f"Downloaded {len(data['messages'])} messages (after {data['after_id']}, newest {data['newest_id']}).")
    if data["after_id"] < supa.last_message_id():
        print("STALE: this batch was already published. The Deals Fetch GitHub Action didn't upload a new one today.")


if __name__ == "__main__":
    if len(sys.argv) != 3 or sys.argv[1] not in ("upload", "download"):
        raise SystemExit(__doc__)
    (upload if sys.argv[1] == "upload" else download)(Path(sys.argv[2]))
