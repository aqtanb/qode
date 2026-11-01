import { createClient } from 'npm:@supabase/supabase-js@2';

const BUCKETS = ['post-images', 'promocode-images'];

Deno.serve(async (request) => {
  const token = request.headers.get('Authorization')?.replace('Bearer ', '');
  if (!token) return new Response('Unauthorized', { status: 401 });

  const admin = createClient(
    Deno.env.get('SUPABASE_URL')!,
    Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!,
    { auth: { persistSession: false } },
  );

  const { data: { user }, error: userError } = await admin.auth.getUser(token);
  if (userError || !user) return new Response('Unauthorized', { status: 401 });

  for (const bucket of BUCKETS) {
    const { data: files, error } = await admin.storage.from(bucket).list(user.id, { limit: 1000 });
    if (error) return Response.json({ error: 'Storage listing failed' }, { status: 500 });

    if (files.length > 0) {
      const removal = await admin.storage.from(bucket).remove(files.map((file) => `${user.id}/${file.name}`));
      if (removal.error) return Response.json({ error: 'Storage removal failed' }, { status: 500 });
    }
  }

  // Foreign keys cascade from auth.users, so this removes the profile and all of the user's content.
  const { error } = await admin.auth.admin.deleteUser(user.id);
  if (error) return Response.json({ error: 'Account deletion failed' }, { status: 500 });

  return Response.json({ success: true });
});
