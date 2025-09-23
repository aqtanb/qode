# Single Service Populator

Quick tool to add individual services to Firestore one by one.

## Usage

Navigate to the `functions/` directory and use:

```bash
npm run add-service "<Service Name>" "<Category>" [domain]
```

## Examples

```bash
# Add Netflix with domain
npm run add-service "Netflix" "Entertainment" "netflix.com"

# Add local business without domain
npm run add-service "Local Coffee Shop" "Food"

# Add Cyrillic service (use quotes!)
npm run add-service "Яндекс Go Еда" "Food" "eda.yandex.kz"

# Add international service
npm run add-service "Spotify" "Entertainment" "spotify.com"

# Add Teez
npm run add-service "Teez" "Shopping" "teez.kz"
```

## Available Categories

- Food
- Entertainment
- Shopping
- Transport
- Education
- Fitness
- Beauty
- Clothing
- Electronics
- Travel
- Jewelry
- Other

## What it does

1. **Transliterates Cyrillic to Latin** for document IDs
2. **Sanitizes names** for consistent IDs (removes special chars, etc.)
3. **Creates document ID** as `{service_slug}_{category_slug}`
4. **Fetches logo** from Clearbit if domain provided
5. **Adds to Firestore** at `services/{document_id}`

## Document Structure

```json
{
  "name": "Netflix",
  "category": "Entertainment",
  "logoUrl": "https://logo.clearbit.com/netflix.com",
  "domain": "netflix.com",
  "promoCodeCount": 0,
  "createdAt": "2025-01-22T...",
  "updatedAt": "2025-01-22T..."
}
```

## Document ID Examples

- `"Netflix"` + `"Entertainment"` → `netflix_entertainment`
- `"Магазин одежды"` + `"Shopping"` → `magazin_odezhdy_shopping`
- `"Papa John's"` + `"Food"` → `papa_john_s_food`

## Notes

- Uses `merge: true` so you can safely re-run without overwriting
- Logo URLs use Clearbit's free service
- Empty domain = empty logoUrl
- Always sets `promoCodeCount: 0` initially