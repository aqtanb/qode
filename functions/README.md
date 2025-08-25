# Qode Cloud Functions

Enterprise-grade Cloud Functions for the Qode promo code application.

## 🚀 Features

- **Vote Score Management**: Automatic voteScore calculation and maintenance
- **Data Population**: Enhanced populators for services and promo codes  
- **Service Counters**: Maintain denormalized promo code counts
- **Kazakhstan Market Focus**: Localized sample data and targeting

## 📋 Prerequisites

1. **Node.js 18+** installed
2. **Firebase CLI** installed globally: `npm install -g firebase-tools`
3. **Firebase Project** set up with Blaze plan
4. **Service Account Key** (for populators)

## 🛠 Setup

### 1. Install Dependencies
```bash
cd functions
npm install
```

### 2. Add Service Account Key
For populators and admin functions, add your Firebase service account key:
```bash
# Download from Firebase Console > Project Settings > Service Accounts
# Save as: functions/src/serviceAccountKey.json
```

### 3. Build Functions
```bash
npm run build
```

## 🚀 Deployment

### Deploy All Functions
```bash
npm run deploy
```

### Deploy Specific Function
```bash
firebase deploy --only functions:updateVoteScore
firebase deploy --only functions:initializeVoteScores
```

## 🧪 Local Development

### Start Emulators
```bash
npm run serve
```

### Test Functions Locally
```bash
npm run shell
```

## 📊 Data Population

### Populate Services
```bash
npm run populate
```

### Populate Promo Codes
```bash
# Default: 100 promo codes
npm run populate:codes

# Custom amount
node lib/populators/populatePromoCodes.js 500
```

### Initialize Vote Scores
```bash
# Call the Cloud Function to calculate voteScore for existing data
npm run init:votescores
```

## 🔧 Available Scripts

- `npm run build` - Build TypeScript to JavaScript
- `npm run build:watch` - Build with file watching
- `npm run serve` - Start Firebase emulators
- `npm run deploy` - Deploy to Firebase
- `npm run logs` - View function logs
- `npm run populate` - Populate services
- `npm run populate:codes` - Populate promo codes

## 🏗 Architecture

### Cloud Functions

#### `updateVoteScore`
- **Trigger**: Firestore document update on `promocodes/{promoId}`
- **Purpose**: Maintains computed `voteScore` field (upvotes - downvotes)
- **Performance**: Only triggers when vote counts change

#### `initializeVoteScores` 
- **Type**: Callable HTTPS function
- **Purpose**: One-time migration to add voteScore to existing data
- **Security**: Requires authentication

#### `updateServicePromoCounts`
- **Type**: Callable HTTPS function  
- **Purpose**: Updates denormalized promo code counts on services
- **Use Case**: Run periodically or after bulk operations

### Data Structure

#### Promo Code Document
```typescript
{
  code: string;
  serviceName: string;
  category: string;
  upvotes: number;
  downvotes: number;
  voteScore: number;        // 🆕 Computed field
  // ... other fields
}
```

#### Service Document
```typescript
{
  name: string;
  category: string;
  promoCodeCount: number;   // Denormalized counter
  // ... other fields
}
```

## 🌍 Kazakhstan Market Focus

The populators include:
- **Local Services**: Яндекс Лавка, Sulpak, Technodom, etc.
- **Local Languages**: Kazakh and Russian titles/descriptions
- **Local Currency**: Amounts in KZT (Kazakhstani Tenge)
- **Geographic Targeting**: `targetCountries: ["KZ"]`

## 📈 Cost Optimization

- **Efficient Triggers**: Functions only run when necessary
- **Batch Operations**: Populators use Firestore batches
- **Computed Fields**: Pre-calculate expensive operations
- **Free Tier Usage**: 2M function invocations/month free

## 🔐 Security

- **Authentication Required**: Admin functions require auth
- **Input Validation**: All inputs validated
- **Error Handling**: Comprehensive error logging
- **Service Account**: Secure admin access

## 🐛 Troubleshooting

### Function Deploy Issues
```bash
# Check Node.js version
node --version  # Should be 18+

# Check Firebase CLI
firebase --version  # Should be latest

# View detailed logs
firebase functions:log --only updateVoteScore
```

### Local Emulator Issues
```bash
# Clear cache and restart
firebase emulators:start --only functions,firestore
```

### Service Account Issues
- Ensure `serviceAccountKey.json` is in `functions/src/`
- Verify the service account has Firestore Admin permissions
- Check file is not committed to git (should be in .gitignore)

## 📚 Related Documentation

- [Firebase Functions Documentation](https://firebase.google.com/docs/functions)
- [Firestore Best Practices](https://firebase.google.com/docs/firestore/best-practices)  
- [Project Architecture](../docs/FIREBASE.md)