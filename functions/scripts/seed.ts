// Seeds qode-dev with fake data. Usage: npm run seed, or npm run seed -- --reset to remove it.
// Needs `gcloud auth application-default login` and access to qode-dev. See docs/ai/backend.md.
import { fakerRU as faker } from '@faker-js/faker';
import * as admin from 'firebase-admin';

const PROJECT_ID = 'qode-dev';
const ID_PREFIX = 'seed-';
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

admin.initializeApp({ projectId: PROJECT_ID });
const db = admin.firestore();
const { Timestamp, FieldPath } = admin.firestore;

const id = (kind: string, index: number) => `${ID_PREFIX}${kind}-${String(index).padStart(3, '0')}`;
const image = (key: string, width: number, height: number) => `https://picsum.photos/seed/${key}/${width}/${height}`;
const daysFromNow = (days: number) => Timestamp.fromMillis(Date.now() + days * DAY_MS);

function buildUsers() {
  return Array.from({ length: USER_COUNT }, (_, index) => ({
    id: id('user', index),
    data: {
      profile: { displayName: faker.person.fullName(), photoUrl: image(`avatar${index}`, 128, 128) },
      stats: { submittedPromocodesCount: 0, submittedPostsCount: 0 },
      consent: { legalPoliciesAcceptedAt: daysFromNow(-90) },
    },
  }));
}

function buildServices() {
  return SERVICE_NAMES.map((name, index) => ({
    id: id('service', index),
    data: {
      name,
      siteUrl: `${name.toLowerCase().replace(/\s+/g, '')}.example.com`,
      logoUrl: image(`logo${index}`, 128, 128),
      promoCodeCount: 0,
    },
  }));
}

function buildDiscount() {
  const roll = faker.number.float();
  if (roll < 0.6) return { type: 'Percentage', value: faker.number.int({ min: 5, max: 70 }) };
  if (roll < 0.85) return { type: 'FixedAmount', value: faker.number.int({ min: 1, max: 20 }) * 500 };
  return { type: 'FreeItem', freeItemDescription: faker.helpers.arrayElement(FREE_ITEMS) };
}

// Mixes active, expired and upcoming codes so every filter and sort has something to show.
function buildPromocodeDates() {
  const roll = faker.number.float();
  if (roll < 0.15) {
    return { startDate: daysFromNow(-60), endDate: daysFromNow(-faker.number.int({ min: 1, max: 30 })) };
  }
  if (roll < 0.25) {
    return { startDate: daysFromNow(faker.number.int({ min: 1, max: 14 })), endDate: daysFromNow(45) };
  }
  return {
    startDate: daysFromNow(-faker.number.int({ min: 1, max: 30 })),
    endDate: daysFromNow(faker.number.int({ min: 1, max: 90 })),
  };
}

function buildPromocodes(users: ReturnType<typeof buildUsers>, services: ReturnType<typeof buildServices>) {
  return Array.from({ length: PROMOCODE_COUNT }, (_, index) => {
    const author = faker.helpers.arrayElement(users);
    const service = faker.helpers.arrayElement(services);
    const upvotes = faker.number.int({ min: 0, max: 200 });
    const downvotes = faker.number.int({ min: 0, max: 40 });
    const createdAt = daysFromNow(-faker.number.int({ min: 0, max: 60 }));
    const hasImage = faker.datatype.boolean({ probability: 0.3 });

    return {
      id: id('promocode', index),
      data: {
        code: `${faker.string.alpha({ length: 5, casing: 'upper' })}${faker.number.int({ min: 10, max: 99 })}`,
        ...buildPromocodeDates(),
        service: {
          id: service.id,
          name: service.data.name,
          logoUrl: service.data.logoUrl,
          siteUrl: service.data.siteUrl,
        },
        discount: buildDiscount(),
        minimumOrderAmount: faker.helpers.arrayElement([0, 2000, 5000, 10000]),
        description: faker.datatype.boolean({ probability: 0.7 }) ? faker.lorem.sentence() : null,
        imageUrls: hasImage ? [image(`promo${index}`, 800, 600)] : [],
        engagement: { upvotes, downvotes, voteScore: upvotes - downvotes },
        author: { id: author.id, username: author.data.profile.displayName, avatarUrl: author.data.profile.photoUrl },
        verified: faker.datatype.boolean({ probability: 0.2 }),
        createdAt,
        updatedAt: createdAt,
      },
    };
  });
}

function buildPosts(users: ReturnType<typeof buildUsers>) {
  return Array.from({ length: POST_COUNT }, (_, index) => {
    const author = faker.helpers.arrayElement(users);
    const upvotes = faker.number.int({ min: 0, max: 150 });
    const downvotes = faker.number.int({ min: 0, max: 30 });
    const createdAt = daysFromNow(-faker.number.int({ min: 0, max: 60 }));
    const imageCount = faker.helpers.arrayElement([0, 0, 1, 3]);

    return {
      id: id('post', index),
      data: {
        authorId: author.id,
        authorName: author.data.profile.displayName,
        authorAvatarUrl: author.data.profile.photoUrl,
        title: faker.helpers.arrayElement(POST_TITLES),
        content: faker.lorem.paragraphs({ min: 1, max: 3 }, '\n').slice(0, 1000),
        imageUrls: Array.from({ length: imageCount }, (_, i) => image(`post${index}x${i}`, 800, 600)),
        tags: faker.helpers.arrayElements(POST_TAGS, { min: 0, max: 5 }),
        upvotes,
        downvotes,
        voteScore: upvotes - downvotes,
        createdAt,
        updatedAt: createdAt,
      },
    };
  });
}

function buildBanners() {
  return Array.from({ length: BANNER_COUNT }, (_, index) => ({
    id: id('banner', index),
    data: {
      imageUrl: image(`banner${index}`, 1200, 600),
      ctaTitle: { default: `Deal ${index + 1}`, en: `Deal ${index + 1}`, ru: `Акция ${index + 1}`, kk: `Науқан ${index + 1}` },
      ctaDescription: {
        default: 'Limited offer',
        en: 'Limited offer',
        ru: 'Ограниченное предложение',
        kk: 'Шектеулі ұсыныс',
      },
      ctaUrl: 'https://example.com',
      isActive: true,
      priority: index,
      expiresAt: daysFromNow(365),
    },
  }));
}

// create() leaves existing docs alone, so a re-run can't reset counts the triggers maintain.
async function createAll(collection: string, docs: { id: string; data: object }[]) {
  const writer = db.bulkWriter();
  let skipped = 0;
  writer.onWriteError((error) => {
    if (error.code === 6) {
      skipped++;
      return false;
    }
    return error.failedAttempts < 3;
  });

  docs.forEach((doc) => writer.create(db.collection(collection).doc(doc.id), doc.data).catch(() => undefined));
  await writer.close();
  console.log(`${collection}: ${docs.length - skipped} created, ${skipped} already there`);
}

async function deleteSeeded(collection: string) {
  const snapshot = await db
    .collection(collection)
    .where(FieldPath.documentId(), '>=', ID_PREFIX)
    .where(FieldPath.documentId(), '<', `${ID_PREFIX}`)
    .get();

  const writer = db.bulkWriter();
  snapshot.docs.forEach((doc) => writer.delete(doc.ref));
  await writer.close();
  console.log(`${collection}: ${snapshot.size} deleted`);
}

async function seed() {
  faker.seed(20261101);
  const users = buildUsers();
  const services = buildServices();

  // Triggers update users and services when promocodes arrive, so those have to exist first.
  await createAll('users', users);
  await createAll('services', services);
  await createAll('banners', buildBanners());
  await createAll('promocodes', buildPromocodes(users, services));
  await createAll('posts', buildPosts(users));
}

async function reset() {
  for (const collection of ['promocodes', 'posts', 'banners', 'services', 'users']) {
    await deleteSeeded(collection);
  }
}

(process.argv.includes('--reset') ? reset() : seed()).catch((error) => {
  console.error(error);
  process.exit(1);
});
