"""Minimal Supabase REST client using the secret key, standard library only.

Reads SUPABASE_URL from the environment. In the routine, the cloud proxy adds the
secret key's headers to requests for the Supabase host, so the key never enters
the session. Locally, set SUPABASE_SECRET_KEY instead. The key bypasses RLS.
"""

import json
import os
from datetime import datetime, timezone
import urllib.error
import urllib.parse
import urllib.request

STATE_SOURCE = "telegram:superpodeshevle/24"


class SupabaseError(Exception):
    def __init__(self, status: int, body: str):
        super().__init__(f"HTTP {status}: {body[:300]}")
        self.status = status


def _request(method: str, path: str, body=None, headers=None, raw=None):
    url = os.environ["SUPABASE_URL"].rstrip("/") + path
    key = os.environ.get("SUPABASE_SECRET_KEY")
    request_headers = {"apikey": key, "Authorization": f"Bearer {key}"} if key else {}
    request_headers.update(headers or {})
    data = raw
    if body is not None:
        data = json.dumps(body).encode()
        request_headers.setdefault("Content-Type", "application/json")
    request = urllib.request.Request(url, data=data, method=method, headers=request_headers)
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            text = response.read().decode()
            return json.loads(text) if text else None
    except urllib.error.HTTPError as error:
        raise SupabaseError(error.code, error.read().decode()) from None


def select(table: str, query: dict) -> list:
    return _request("GET", f"/rest/v1/{table}?{urllib.parse.urlencode(query)}")


def insert(table: str, row: dict) -> None:
    _request("POST", f"/rest/v1/{table}", row, {"Prefer": "return=minimal"})


def update(table: str, match: dict, values: dict) -> None:
    _request("PATCH", f"/rest/v1/{table}?{urllib.parse.urlencode(match)}", values, {"Prefer": "return=minimal"})


def upload(bucket: str, path: str, content: bytes, content_type: str, upsert: bool = False) -> str:
    headers = {"Content-Type": content_type, "x-upsert": "true" if upsert else "false"}
    _request("POST", f"/storage/v1/object/{bucket}/{path}", raw=content, headers=headers)
    return os.environ["SUPABASE_URL"].rstrip("/") + f"/storage/v1/object/public/{bucket}/{path}"


def download(bucket: str, path: str) -> bytes:
    url = os.environ["SUPABASE_URL"].rstrip("/") + f"/storage/v1/object/authenticated/{bucket}/{path}"
    key = os.environ.get("SUPABASE_SECRET_KEY")
    headers = {"apikey": key, "Authorization": f"Bearer {key}"} if key else {}
    request = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            return response.read()
    except urllib.error.HTTPError as error:
        raise SupabaseError(error.code, error.read().decode()) from None


def last_message_id() -> int:
    rows = select("ingest_state", {"source": f"eq.{STATE_SOURCE}", "select": "last_message_id"})
    return rows[0]["last_message_id"] if rows else 0


def save_last_message_id(message_id: int) -> None:
    update("ingest_state", {"source": f"eq.{STATE_SOURCE}"}, {"last_message_id": message_id, "updated_at": datetime.now(timezone.utc).isoformat()})
