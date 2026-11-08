// Checks RLS, column grants, triggers and storage rules against qode-dev.
// Creates two throwaway users and removes them afterwards. Usage: npm run test:security
import { randomBytes, randomUUID } from 'node:crypto';
import { adminClientConfig, clientFor } from './project';

const { secretKey, publishableKey } = adminClientConfig();
const admin = clientFor(secretKey);
const anon = clientFor(publishableKey);

let failures = 0;
function check(name: string, passed: boolean, detail = '') {
  console.log(`${passed ? 'PASS' : 'FAIL'}  ${name}${passed ? '' : `  ${detail}`}`);
  if (!passed) failures++;
}
const allowed = (name: string, result: { error: unknown }) =>
  check(name, !result.error, JSON.stringify(result.error));
const denied = (name: string, result: { error: unknown; data?: unknown }) => {
  const emptyResult = Array.isArray(result.data) && result.data.length === 0;
  check(name, !!result.error || emptyResult, 'expected denial');
};

async function createUser() {
  const email = `test-${randomUUID()}@seed.qode.invalid`;
  const password = randomBytes(24).toString('hex');
  const { data, error } = await admin.auth.admin.createUser({ email, password, email_confirm: true });
  if (error) throw error;
  const client = clientFor(publishableKey);
  const signIn = await client.auth.signInWithPassword({ email, password });
  if (signIn.error) throw signIn.error;
  return { id: data.user.id, client };
}

async function count(table: string, id: string, column: string) {
  const { data } = await admin.from(table).select(column).eq('id', id).single();
  return (data as unknown as Record<string, number>)[column];
}

async function run() {
  const alice = await createUser();
  const bob = await createUser();
  const suffix = randomUUID();
  const serviceId = `test-service-${suffix}`;
  const promoId = `test-promo-${suffix}`;
  const postId = `test-post-${suffix}`;

  const promo = {
    id: promoId, code: 'TEST10', service_id: serviceId, author_id: alice.id,
    start_date: new Date().toISOString(), end_date: new Date(Date.now() + 86400000).toISOString(),
    discount_type: 'Percentage', discount_value: 10,
  };

  try {
    // Users
    allowed('create own profile', await alice.client.from('users').insert({ id: alice.id, display_name: 'Alice' }));
    allowed('create second profile', await bob.client.from('users').insert({ id: bob.id, display_name: 'Bob' }));
    denied('create profile for someone else', await alice.client.from('users').insert({ id: randomUUID() }));
    denied('anon lists users', await anon.from('users').select('id'));
    const list = await alice.client.from('users').select('id');
    check('signed-in user sees only own row', list.data?.length === 1 && list.data[0].id === alice.id);
    denied('edit own stats', await alice.client.from('users').update({ submitted_posts_count: 99 }).eq('id', alice.id));
    denied('edit another profile', await alice.client.from('users').update({ display_name: 'x' }).eq('id', bob.id).select());
    const profile = await alice.client.rpc('get_user_profile', { profile_id: bob.id });
    check('read another name by id', profile.data?.[0]?.display_name === 'Bob');
    denied('anon reads profile by id', await anon.rpc('get_user_profile', { profile_id: bob.id }));

    // Services
    denied('anon creates service', await anon.from('services').insert({ id: serviceId, name: 'Nope' }));
    allowed('signed-in creates service', await alice.client.from('services').insert({ id: serviceId, name: `Qodetest ${suffix}` }));
    denied('create service with a count', await alice.client.from('services').insert({ id: `${serviceId}x`, name: 'x', promocode_count: 5 }));
    denied('edit service', await alice.client.from('services').update({ name: 'x' }).eq('id', serviceId));
    denied('delete service', await alice.client.from('services').delete().eq('id', serviceId).select());
    allowed('anon reads services', await anon.from('services').select('id').limit(1));

    // Promocodes and posts
    denied('create promocode as someone else', await bob.client.from('promocodes').insert({ ...promo, id: `${promoId}x` }));
    denied('create promocode with votes', await alice.client.from('promocodes').insert({ ...promo, id: `${promoId}y`, upvotes: 50 }));
    denied('create verified promocode', await alice.client.from('promocodes').insert({ ...promo, id: `${promoId}z`, verified: true }));
    denied('create promocode with a source link', await alice.client.from('promocodes').insert({ ...promo, id: `${promoId}s`, source_url: 'https://t.me/x/1' }));
    allowed('create own promocode', await alice.client.from('promocodes').insert(promo));
    check('service count goes up', (await count('services', serviceId, 'promocode_count')) === 1);
    check('user stat goes up', (await count('users', alice.id, 'submitted_promocodes_count')) === 1);
    denied('edit promocode votes', await alice.client.from('promocodes').update({ upvotes: 50 }).eq('id', promoId));
    denied('delete someone else\'s promocode', await bob.client.from('promocodes').delete().eq('id', promoId).select());
    allowed('create own post', await alice.client.from('posts').insert({ id: postId, author_id: alice.id, title: 'Hi' }));
    denied('create post with a source link', await alice.client.from('posts').insert({ id: `${postId}s`, author_id: alice.id, title: 'Hi', source_url: 'https://t.me/x/1' }));
    check('post stat goes up', (await count('users', alice.id, 'submitted_posts_count')) === 1);
    const view = await anon.from('promocodes_view').select('service_name, author_name').eq('id', promoId).single();
    check('anon feed shows author and service', view.data?.author_name === 'Alice' && !!view.data?.service_name);
    const search = await anon.rpc('search_services', { search_query: 'Qodetset', result_limit: 5 });
    check('search tolerates a typo', !search.error && (search.data ?? []).some((s: { id: string }) => s.id === serviceId), JSON.stringify(search.error));

    // Votes
    const vote = { user_id: bob.id, item_type: 'PROMOCODE', item_id: promoId };
    denied('vote as someone else', await alice.client.from('interactions').insert({ ...vote, vote_state: 'UPVOTE' }));
    const saveVote = (state: string) =>
      bob.client.rpc('save_interaction', { target_type: 'PROMOCODE', target_id: promoId, new_vote_state: state, is_bookmarked: false });
    allowed('upvote', await saveVote('UPVOTE'));
    check('upvote counted', (await count('promocodes', promoId, 'vote_score')) === 1);
    allowed('change to downvote', await saveVote('DOWNVOTE'));
    check('vote moved', (await count('promocodes', promoId, 'vote_score')) === -1);
    denied('anon votes', await anon.rpc('save_interaction', { target_type: 'PROMOCODE', target_id: promoId, new_vote_state: 'UPVOTE', is_bookmarked: false }));
    allowed('remove vote', await bob.client.from('interactions').delete().match(vote));
    check('vote removed', (await count('promocodes', promoId, 'vote_score')) === 0);
    const others = await alice.client.from('interactions').select('user_id').eq('user_id', bob.id);
    check('can\'t read others\' interactions', others.data?.length === 0);

    // Blocks and reports
    allowed('block a user', await alice.client.from('blocks').insert({ user_id: alice.id, blocked_user_id: bob.id }));
    denied('block on someone\'s behalf', await bob.client.from('blocks').insert({ user_id: alice.id, blocked_user_id: bob.id }));
    const report = { reported_item_id: promoId, reported_item_type: 'PROMOCODE', reason: 'SPAM' };
    allowed('report content', await bob.client.from('reports').insert({ ...report, reporter_id: bob.id }));
    denied('report as someone else', await bob.client.from('reports').insert({ ...report, reporter_id: alice.id }));
    denied('set report status', await bob.client.from('reports').insert({ ...report, reporter_id: bob.id, status: 'RESOLVED' }));
    const reports = await alice.client.from('reports').select('id').eq('reporter_id', bob.id);
    check('can\'t read others\' reports', reports.data?.length === 0);

    // Read-only tables
    denied('edit app config', await alice.client.from('app_config').update({ minimum_version_code: 0 }).eq('id', 'update_policy'));
    denied('create banner', await alice.client.from('banners').insert({ id: 'x', image_url: 'x' }));
    denied('anon reads ingest state', await anon.from('ingest_state').select('source'));
    denied('signed-in reads ingest state', await alice.client.from('ingest_state').select('source'));
    denied('move ingest state', await alice.client.from('ingest_state').update({ last_message_id: 0 }).eq('source', 'telegram:superpodeshevle/24').select());

    // Storage
    const jpeg = new Blob([randomBytes(64)], { type: 'image/jpeg' });
    const storage = alice.client.storage.from('post-images');
    allowed('upload to own folder', await storage.upload(`${alice.id}/${suffix}.jpg`, jpeg));
    denied('upload to another folder', await storage.upload(`${bob.id}/${suffix}.jpg`, jpeg));
    denied('overwrite own file', await storage.upload(`${alice.id}/${suffix}.jpg`, jpeg, { upsert: true }));
    denied('upload a script', await storage.upload(`${alice.id}/${suffix}.html`, new Blob(['x'], { type: 'text/html' })));
    denied('upload over 5 MB', await storage.upload(`${alice.id}/big.jpg`, new Blob([new Uint8Array(5242881)], { type: 'image/jpeg' })));
    denied('anon uploads', await anon.storage.from('post-images').upload(`${alice.id}/anon.jpg`, jpeg));

    // Cleanup order
    allowed('delete own promocode', await alice.client.from('promocodes').delete().eq('id', promoId));
    check('service count goes down', (await count('services', serviceId, 'promocode_count')) === 0);
  } finally {
    await admin.storage.from('post-images').remove([`${alice.id}/${suffix}.jpg`]);
    await admin.auth.admin.deleteUser(alice.id);
    await admin.auth.admin.deleteUser(bob.id);
    await admin.from('services').delete().eq('id', serviceId);
  }

  console.log(failures === 0 ? '\nAll checks passed' : `\n${failures} checks failed`);
  process.exit(failures === 0 ? 0 : 1);
}

run().catch((error) => {
  console.error(error);
  process.exit(1);
});
