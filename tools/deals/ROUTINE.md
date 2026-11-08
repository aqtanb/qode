# Daily deals import

You import deals from topic 24 of the Telegram chat t.me/superpodeshevle ("Суперчат подешевле", Kazakhstan) into Qode, a promocode and deals app. The chat admin allowed this. Everything you publish goes live immediately under the "Qode Deals" account, so only publish real, current deals.

## Steps

1. Set up and fetch:
   ```
   cd tools/deals
   python3 -m venv .venv && .venv/bin/pip install -q -r requirements.txt
   .venv/bin/python fetch.py --out work/messages.json
   ```
2. Read `work/messages.json`. For every message, look at its photos too (use the Read tool on each path): many deals are only an image.
3. Write `work/deals.json` with one item per real deal, following the rules below. Set `newest_id` to the `newest_id` from `messages.json`, even when there are no items.
4. Run `.venv/bin/python publish.py work/deals.json --dry-run`, fix anything it skips for a fixable reason, then run it without `--dry-run`.
5. Finish with a short summary: how many messages were read, what was published, and what was skipped and why.

If fetch fails because the Telegram session is logged out, stop and say so; don't retry in a loop.

## What to publish

Read the whole message and its photos, then decide:

- **Promocode**: the deal needs a code the user types or shows (for example `ERTE30`, `NAVAT`). Discount types:
  - `Percentage` with `value` 1–100.
  - `FixedAmount` with `value` in tenge; it can't be more than `minimum_order_amount`, so set the minimum when the message gives one.
  - `FreeItem` with a short `description` of the gift (up to 50 characters: letters, digits, spaces, hyphens), e.g. "Бальзам для губ Yves Rocher".
  - `service.name` is the brand as people know it (e.g. "FlyArystan", "Starbucks"), and `service.site` its website domain (e.g. "flyarystan.com"). Existing services are matched by name; a new one needs the real domain.
  - If one message lists several codes (ERTE30, ERTE60, ERTE90), publish each as its own promocode.
- **Post**: a real offer without a code (a sale price, "second coffee free today", "−3000 ₸ with a card", a cheap set menu). Write:
  - `title`: up to 50 characters, specific, in the message's language (usually Russian), e.g. "Starbucks Астана: второй кофе в подарок 8 ноября".
  - `content`: the useful details in a few lines: what, where (city and place), price, conditions, dates, and the shop link if there is one. Don't copy emoji walls or "Автор" signatures.
  - `tags`: 1–5 lowercase words, letters/digits/underscore only, e.g. `кофе`, `астана`, `скидка`.
  - `photos`: the message's photos that show the deal.
- **Skip**: chatter, questions, requests ("у кого есть промокод…"), people selling their own subscription slots, ads for channels, anything without a concrete offer, and anything already expired. A message that only links to an offer with no details is a skip unless the photo shows the offer.

Dates: `start_date` and `end_date` as `YYYY-MM-DD`. Use the dates the message or photo states; "только 8 ноября" means start and end are that day. If the deal has no end date, leave `end_date` out (it defaults to 30 days). Don't guess codes, prices or dates that aren't in the message.

`source_id` is the message `id`. Every item keeps the `photos` paths from `messages.json` that belong to it.

## Example `work/deals.json`

```json
{
  "newest_id": 137350,
  "items": [
    {"kind": "promocode", "source_id": 137248, "code": "ERTE30",
     "service": {"name": "FlyArystan", "site": "flyarystan.com"},
     "discount": {"type": "Percentage", "value": 10},
     "description": "Скидка 10% на билеты, купленные в приложении более чем за 30 дней до вылета",
     "photos": []},
    {"kind": "post", "source_id": 137297,
     "title": "Starbucks Астана: второй кофе в подарок 8 ноября",
     "content": "В новом филиале Starbucks в Астане: заказываете любой кофе, второй бесплатно.\nТолько 8 ноября.",
     "tags": ["кофе", "астана", "акция"],
     "photos": ["/abs/path/work/photos/137297.jpg"]}
  ]
}
```

Don't commit `work/`, and never print the environment's secrets.
