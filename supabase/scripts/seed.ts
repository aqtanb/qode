// Seeds qode-dev with fake data. Usage: npm run seed, or npm run seed -- --reset to remove it.
// Needs `supabase login`. See docs/ai/backend.md.
import { fakerRU as faker } from '@faker-js/faker';
import { adminClientConfig, clientFor } from './project';

const ID_PREFIX = 'seed-';
const EMAIL_DOMAIN = 'seed.qode.invalid';
const DAY_MS = 24 * 60 * 60 * 1000;

const USER_COUNT = 50;
const PROMOCODE_COUNT = 300;
const POST_COUNT = 100;
const BANNER_COUNT = 5;

const SERVICE_NAMES = [
  'Bazarly', 'Dastarkhan Food', 'Yurta Market', 'Tulpar Taxi', 'Altyn Beauty', 'Sputnik Books',
  'Kokshe Sport', 'Nomad Travel', 'Samgau Kino', 'Aqsu Pharmacy', 'Zhetysu Coffee', 'Steppe Fashion',
  'Orda Electronics', 'Medeu Fitness', 'Shanyrak Home', 'Baursak Bakery', 'Almaty Eats', 'Tengri Mobile',
  'Qazaq Kids', 'Saryarka Auto', 'Astana Flowers', 'Kulan Delivery', 'Burabay Tours', 'Ulytau Games',
  'Kaspiy Fish', 'Otau Furniture', 'Dala Pets', 'Arman Learning', 'Sholpan Jewelry', 'Tau Outdoor',
];

const POST_TITLES = [
  'Лучшие скидки недели', 'Как экономить на доставке', 'Промокод реально работает', 'Подборка для студентов',
  'Что купить на распродаже', 'Кэшбэк или промокод?', 'Мой лайфхак с такси', 'Скидки на продукты',
  'Осторожно, истёкший код', 'Топ сервисов этого месяца',
];

const POST_TAGS = ['скидки', 'еда', 'такси', 'шопинг', 'красота', 'спорт', 'книги', 'кино', 'доставка', 'лайфхак'];

const FREE_ITEMS = ['Бесплатная доставка', 'Десерт в подарок', 'Кофе в подарок', 'Месяц подписки'];

const db = clientFor(adminClientConfig().secretKey);

const id = (kind: string, index: number) => `${ID_PREFIX}${kind}-${String(index).padStart(3, '0')}`;
const image = (key: string, width: number, height: number) => `https://picsum.photos/seed/${key}/${width}/${height}`;
const daysFromNow = (days: number) => new Date(Date.now() + days * DAY_MS).toISOString();

type SeedUser = { id: string; display_name: string; photo_url: string };

async function listSeedAuthUsers() {
  const found: { id: string; email?: string }[] = [];
  for (let page = 1; ; page++) {
    const { data, error } = await db.auth.admin.listUsers({ page, perPage: 200 });
    if (error) throw error;
    found.push(...data.users.filter((user) => user.email?.endsWith(`@${EMAIL_DOMAIN}`)));
    if (data.users.length < 200) return found;
  }
}

async function createUsers(): Promise<SeedUser[]> {
  const existing = new Map((await listSeedAuthUsers()).map((user) => [user.email, user.id]));
  const users: SeedUser[] = [];

  for (let index = 0; index < USER_COUNT; index++) {
    const email = `${id('user', index)}@${EMAIL_DOMAIN}`;
    let userId = existing.get(email);
    if (!userId) {
      const { data, error } = await db.auth.admin.createUser({ email, email_confirm: true });
      if (error) throw error;
      userId = data.user.id;
    }
    users.push({ id: userId, display_name: faker.person.fullName(), photo_url: image(`avatar${index}`, 128, 128) });
  }
  return users;
}

function buildServices() {
  return SERVICE_NAMES.map((name, index) => ({
    id: id('service', index),
    name,
    site_url: `${name.toLowerCase().replace(/\s+/g, '')}.example.com`,
    logo_url: image(`logo${index}`, 128, 128),
  }));
}

function buildDiscount() {
  const roll = faker.number.float();
  if (roll < 0.6) return { discount_type: 'Percentage', discount_value: faker.number.int({ min: 5, max: 70 }) };
  if (roll < 0.85) return { discount_type: 'FixedAmount', discount_value: faker.number.int({ min: 1, max: 20 }) * 500 };
  return { discount_type: 'FreeItem', free_item_description: faker.helpers.arrayElement(FREE_ITEMS) };
}

// Mixes active, expired and upcoming codes so every filter and sort has something to show.
function buildPromocodeDates() {
  const roll = faker.number.float();
  if (roll < 0.15) {
    return { start_date: daysFromNow(-60), end_date: daysFromNow(-faker.number.int({ min: 1, max: 30 })) };
  }
  if (roll < 0.25) {
    return { start_date: daysFromNow(faker.number.int({ min: 1, max: 14 })), end_date: daysFromNow(45) };
  }
  return {
    start_date: daysFromNow(-faker.number.int({ min: 1, max: 30 })),
    end_date: daysFromNow(faker.number.int({ min: 1, max: 90 })),
  };
}

function buildPromocodes(users: SeedUser[], services: ReturnType<typeof buildServices>) {
  return Array.from({ length: PROMOCODE_COUNT }, (_, index) => {
    const createdAt = daysFromNow(-faker.number.int({ min: 0, max: 60 }));
    const hasImage = faker.datatype.boolean({ probability: 0.3 });

    return {
      discount_value: null,
      free_item_description: null,
      id: id('promocode', index),
      code: `${faker.string.alpha({ length: 5, casing: 'upper' })}${faker.number.int({ min: 10, max: 99 })}`,
      ...buildPromocodeDates(),
      service_id: faker.helpers.arrayElement(services).id,
      author_id: faker.helpers.arrayElement(users).id,
      ...buildDiscount(),
      minimum_order_amount: faker.helpers.arrayElement([0, 2000, 5000, 10000]),
      description: faker.datatype.boolean({ probability: 0.7 }) ? faker.lorem.sentence() : null,
      image_urls: hasImage ? [image(`promo${index}`, 800, 600)] : [],
      upvotes: faker.number.int({ min: 0, max: 200 }),
      downvotes: faker.number.int({ min: 0, max: 40 }),
      verified: faker.datatype.boolean({ probability: 0.2 }),
      created_at: createdAt,
      updated_at: createdAt,
    };
  });
}

function buildPosts(users: SeedUser[]) {
  return Array.from({ length: POST_COUNT }, (_, index) => {
    const createdAt = daysFromNow(-faker.number.int({ min: 0, max: 60 }));
    const imageCount = faker.helpers.arrayElement([0, 0, 1, 3]);

    return {
      id: id('post', index),
      author_id: faker.helpers.arrayElement(users).id,
      title: faker.helpers.arrayElement(POST_TITLES),
      content: faker.lorem.paragraphs({ min: 1, max: 3 }, '\n').slice(0, 1000),
      image_urls: Array.from({ length: imageCount }, (_, i) => image(`post${index}x${i}`, 800, 600)),
      tags: faker.helpers.arrayElements(POST_TAGS, { min: 0, max: 5 }),
      upvotes: faker.number.int({ min: 0, max: 150 }),
      downvotes: faker.number.int({ min: 0, max: 30 }),
      created_at: createdAt,
      updated_at: createdAt,
    };
  });
}

function buildBanners() {
  return Array.from({ length: BANNER_COUNT }, (_, index) => ({
    id: id('banner', index),
    image_url: image(`banner${index}`, 1200, 600),
    cta_title: { default: `Deal ${index + 1}`, en: `Deal ${index + 1}`, ru: `Акция ${index + 1}`, kk: `Науқан ${index + 1}` },
    cta_description: {
      default: 'Limited offer',
      en: 'Limited offer',
      ru: 'Ограниченное предложение',
      kk: 'Шектеулі ұсыныс',
    },
    cta_url: 'https://example.com',
    priority: index,
    expires_at: daysFromNow(365),
  }));
}

// ignoreDuplicates leaves existing rows alone, so a re-run can't reset counts the triggers maintain.
async function insertAll(table: string, rows: object[]) {
  for (let start = 0; start < rows.length; start += 100) {
    const { error } = await db.from(table).upsert(rows.slice(start, start + 100), { ignoreDuplicates: true });
    if (error) throw new Error(`${table}: ${error.message}`);
  }
  console.log(`${table}: ${rows.length} checked`);
}

async function seed() {
  faker.seed(20261101);
  const users = await createUsers();
  const services = buildServices();

  await insertAll('users', users);
  await insertAll('services', services);
  await insertAll('banners', buildBanners());
  await insertAll('promocodes', buildPromocodes(users, services));
  await insertAll('posts', buildPosts(users));
}

// Deleting an auth user cascades to their users row, promocodes, posts and interactions.
async function reset() {
  for (const user of await listSeedAuthUsers()) {
    const { error } = await db.auth.admin.deleteUser(user.id);
    if (error) throw error;
  }
  for (const table of ['banners', 'services']) {
    const { error } = await db.from(table).delete().like('id', `${ID_PREFIX}%`);
    if (error) throw new Error(`${table}: ${error.message}`);
  }
  console.log('Seed data removed');
}

(process.argv.includes('--reset') ? reset() : seed()).catch((error) => {
  console.error(error);
  process.exit(1);
});
