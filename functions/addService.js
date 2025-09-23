const { addSingleService } = require('./lib/populators/addSingleService');

// Simple command line interface
const args = process.argv.slice(2);

if (args.length < 2) {
  console.error('❌ Usage: node addService.js <name> <category> [domain]');
  console.error('📋 Examples:');
  console.error('   node addService.js "Netflix" "Entertainment" "netflix.com"');
  console.error('   node addService.js "Local Coffee Shop" "Food"');
  console.error('   node addService.js "Магазин одежды" "Shopping" "shop.kz"');
  console.error('');
  console.error('📝 Available categories: Food, Entertainment, Shopping, Transport, Education, Fitness, Beauty, Clothing, Electronics, Travel, Jewelry, Other');
  process.exit(1);
}

const [name, category, domain] = args;

console.log('🔧 Adding service with:');
console.log(`   Name: ${name}`);
console.log(`   Category: ${category}`);
console.log(`   Domain: ${domain || 'none'}`);
console.log('');

addSingleService({ name, category, domain })
  .then((id) => {
    console.log(`🎉 Service successfully added!`);
    console.log(`   Document ID: ${id}`);
    console.log(`   Firestore path: services/${id}`);
  })
  .catch((error) => {
    console.error('❌ Error adding service:', error.message);
    process.exit(1);
  });