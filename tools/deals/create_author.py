"""Create the "Qode Deals" account that imported deals are posted under. Run once per project.

Reads SUPABASE_URL and SUPABASE_SECRET_KEY. Prints the new user id, which the
routine needs as DEALS_AUTHOR_ID.
"""

import secrets
from pathlib import Path

import supa

LOGO = Path(__file__).resolve().parents[2] / "androidApp/src/main/res/play_store_512.png"


def main() -> None:
    user = supa._request("POST", "/auth/v1/admin/users", {
        "email": "deals@qode.invalid",
        "password": secrets.token_hex(32),
        "email_confirm": True,
    })
    user_id = user["id"]
    avatar = supa.upload("post-images", f"{user_id}/avatar.png", LOGO.read_bytes(), "image/png")
    supa.insert("users", {"id": user_id, "display_name": "Qode Deals", "photo_url": avatar})
    print(f"DEALS_AUTHOR_ID={user_id}")


if __name__ == "__main__":
    main()
