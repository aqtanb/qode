"""One-time Telegram login for the deals ingest.

Run it yourself in a real terminal: it asks for the api_id and api_hash from
my.telegram.org, shows a QR code to scan from Telegram on your phone, then
prints a session string. Store that string in Bitwarden and the routine's environment, never in
this repo. Anyone holding it can use your Telegram account.
"""

import asyncio
import getpass
from datetime import datetime, timedelta, timezone

import qrcode
from telethon import TelegramClient
from telethon.errors import SessionPasswordNeededError
from telethon.sessions import StringSession

CHAT = "superpodeshevle"


async def main() -> None:
    api_id = int(input("api_id: ").strip())
    api_hash = getpass.getpass("api_hash (hidden): ").strip()

    client = TelegramClient(StringSession(), api_id, api_hash)
    await client.connect()
    try:
        # QR login avoids SMS and app codes, which Telegram often withholds for new API apps.
        login = await client.qr_login()
        while True:
            qr = qrcode.QRCode(border=1)
            qr.add_data(login.url)
            qr.print_ascii(invert=True)
            print("Telegram on your phone: Settings > Devices > Link Desktop Device, then scan this.")
            try:
                await login.wait(timeout=60)
                break
            except asyncio.TimeoutError:
                print("QR expired, showing a new one.\n")
                await login.recreate()
            except SessionPasswordNeededError:
                await client.sign_in(password=getpass.getpass("two-step password (hidden): "))
                break

        me = await client.get_me()
        chat = await client.get_entity(CHAT)

        since = datetime.now(timezone.utc) - timedelta(days=1)
        recent = 0
        async for message in client.iter_messages(chat, limit=500):
            if message.date < since:
                break
            recent += 1

        print(f"\nLogged in as {me.first_name} (@{me.username}).")
        print(f"Can read '{getattr(chat, 'title', CHAT)}': {recent} messages in the last 24 hours.")
        print("\nSession string (save it in Bitwarden, then clear this terminal):\n")
        print(client.session.save())
    finally:
        await client.disconnect()


if __name__ == "__main__":
    asyncio.run(main())
