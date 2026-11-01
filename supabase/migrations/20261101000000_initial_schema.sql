create extension if not exists pg_trgm with schema extensions;

-- Tables

create table public.users (
  id uuid primary key references auth.users (id) on delete cascade,
  display_name text,
  photo_url text,
  submitted_promocodes_count integer not null default 0,
  submitted_posts_count integer not null default 0,
  legal_policies_accepted_at timestamptz,
  created_at timestamptz not null default now()
);

create table public.services (
  id text primary key,
  name text not null check (char_length(name) between 1 and 100),
  site_url text not null default '',
  logo_url text,
  promocode_count integer not null default 0,
  created_at timestamptz not null default now()
);

create table public.promocodes (
  id text primary key,
  code text not null,
  service_id text not null references public.services (id),
  author_id uuid not null references public.users (id) on delete cascade,
  start_date timestamptz not null,
  end_date timestamptz not null,
  discount_type text not null check (discount_type in ('Percentage', 'FixedAmount', 'FreeItem')),
  discount_value double precision,
  free_item_description text,
  minimum_order_amount double precision not null default 0,
  description text,
  image_urls text[] not null default '{}',
  upvotes integer not null default 0,
  downvotes integer not null default 0,
  vote_score integer generated always as (upvotes - downvotes) stored,
  verified boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.posts (
  id text primary key,
  author_id uuid not null references public.users (id) on delete cascade,
  title text not null,
  content text not null default '',
  image_urls text[] not null default '{}',
  tags text[] not null default '{}',
  upvotes integer not null default 0,
  downvotes integer not null default 0,
  vote_score integer generated always as (upvotes - downvotes) stored,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.banners (
  id text primary key,
  image_url text not null,
  cta_title jsonb not null default '{}',
  cta_description jsonb not null default '{}',
  cta_url text,
  is_active boolean not null default true,
  priority integer not null default 0,
  expires_at timestamptz not null default '9999-12-31 23:59:59+00'
);

create table public.reports (
  id text primary key default gen_random_uuid()::text,
  reported_item_id text not null,
  reported_item_type text not null,
  reporter_id uuid not null references public.users (id) on delete cascade,
  reason text not null,
  additional_details text,
  status text not null default 'PENDING',
  created_at timestamptz not null default now()
);

create table public.app_config (
  id text primary key,
  minimum_version_code integer not null,
  flexible_update_stale_days integer not null
);

insert into public.app_config (id, minimum_version_code, flexible_update_stale_days)
values ('update_policy', 14, 3);

create table public.blocks (
  user_id uuid not null references public.users (id) on delete cascade,
  blocked_user_id uuid not null references public.users (id) on delete cascade,
  blocked_at timestamptz not null default now(),
  primary key (user_id, blocked_user_id)
);

-- item_id can point at a promocode or a post, so it has no foreign key.
-- delete_content_interactions removes rows when the content goes away.
create table public.interactions (
  user_id uuid not null references public.users (id) on delete cascade,
  item_type text not null check (item_type in ('PROMOCODE', 'POST')),
  item_id text not null,
  vote_state text not null default 'NONE' check (vote_state in ('NONE', 'UPVOTE', 'DOWNVOTE')),
  bookmarked boolean not null default false,
  primary key (user_id, item_type, item_id)
);

-- Indexes

create index promocodes_vote_score_idx on public.promocodes (vote_score desc, id);
create index promocodes_created_at_idx on public.promocodes (created_at desc, id);
create index promocodes_end_date_idx on public.promocodes (end_date, id);
create index promocodes_service_id_idx on public.promocodes (service_id);
create index promocodes_author_id_idx on public.promocodes (author_id, created_at desc);
create index posts_created_at_idx on public.posts (created_at desc, id);
create index posts_author_id_idx on public.posts (author_id, created_at desc);
create index services_popular_idx on public.services (promocode_count desc, name);
create index services_name_idx on public.services (name);
create index services_name_trgm_idx on public.services using gin (name extensions.gin_trgm_ops);
create index banners_active_idx on public.banners (is_active, expires_at, priority desc);
create index reports_reporter_id_idx on public.reports (reporter_id);
create index blocks_blocked_user_id_idx on public.blocks (blocked_user_id);
create index blocks_user_id_idx on public.blocks (user_id, blocked_at desc);
create index interactions_item_idx on public.interactions (item_type, item_id);

-- Read views. These run as their owner on purpose: users is readable only by its owner,
-- and feeds still need author names. They expose public columns only, never consent data.

create view public.promocodes_view as
select
  p.id, p.code, p.start_date, p.end_date, p.discount_type, p.discount_value, p.free_item_description,
  p.minimum_order_amount, p.description, p.image_urls, p.upvotes, p.downvotes, p.vote_score, p.verified,
  p.created_at, p.updated_at,
  p.service_id, s.name as service_name, s.logo_url as service_logo_url, s.site_url as service_site_url,
  p.author_id, u.display_name as author_name, u.photo_url as author_avatar_url
from public.promocodes p
join public.services s on s.id = p.service_id
join public.users u on u.id = p.author_id;

create view public.posts_view as
select
  p.id, p.title, p.content, p.image_urls, p.tags, p.upvotes, p.downvotes, p.vote_score,
  p.created_at, p.updated_at,
  p.author_id, u.display_name as author_name, u.photo_url as author_avatar_url
from public.posts p
join public.users u on u.id = p.author_id;

-- Functions

create function public.get_user_profile(profile_id uuid)
returns table (id uuid, display_name text, photo_url text)
language sql stable security definer set search_path = ''
as $$
  select u.id, u.display_name, u.photo_url from public.users u where u.id = profile_id;
$$;

create function public.search_services(search_query text, result_limit integer default 20)
returns setof public.services
language sql stable
set search_path = public, extensions
set pg_trgm.word_similarity_threshold = 0.3
as $$
  select s.*
  from public.services s
  where s.name ilike '%' || search_query || '%' or search_query <% s.name
  order by word_similarity(search_query, s.name) desc, s.promocode_count desc
  limit least(result_limit, 50);
$$;

create function public.update_service_promocode_count()
returns trigger
language plpgsql security definer set search_path = ''
as $$
begin
  if tg_op = 'INSERT' then
    update public.services set promocode_count = promocode_count + 1 where id = new.service_id;
  else
    update public.services set promocode_count = promocode_count - 1 where id = old.service_id;
  end if;
  return null;
end;
$$;

create function public.update_user_promocode_stats()
returns trigger
language plpgsql security definer set search_path = ''
as $$
begin
  if tg_op = 'INSERT' then
    update public.users set submitted_promocodes_count = submitted_promocodes_count + 1 where id = new.author_id;
  else
    update public.users set submitted_promocodes_count = submitted_promocodes_count - 1 where id = old.author_id;
  end if;
  return null;
end;
$$;

create function public.update_user_post_stats()
returns trigger
language plpgsql security definer set search_path = ''
as $$
begin
  if tg_op = 'INSERT' then
    update public.users set submitted_posts_count = submitted_posts_count + 1 where id = new.author_id;
  else
    update public.users set submitted_posts_count = submitted_posts_count - 1 where id = old.author_id;
  end if;
  return null;
end;
$$;

create function public.apply_interaction_votes()
returns trigger
language plpgsql security definer set search_path = ''
as $$
declare
  old_state text := case when tg_op = 'INSERT' then 'NONE' else old.vote_state end;
  new_state text := case when tg_op = 'DELETE' then 'NONE' else new.vote_state end;
  target_type text := case when tg_op = 'DELETE' then old.item_type else new.item_type end;
  target_id text := case when tg_op = 'DELETE' then old.item_id else new.item_id end;
  upvote_delta integer := (new_state = 'UPVOTE')::integer - (old_state = 'UPVOTE')::integer;
  downvote_delta integer := (new_state = 'DOWNVOTE')::integer - (old_state = 'DOWNVOTE')::integer;
begin
  if upvote_delta = 0 and downvote_delta = 0 then
    return null;
  end if;

  if target_type = 'PROMOCODE' then
    update public.promocodes
    set upvotes = upvotes + upvote_delta, downvotes = downvotes + downvote_delta
    where id = target_id;
  else
    update public.posts
    set upvotes = upvotes + upvote_delta, downvotes = downvotes + downvote_delta
    where id = target_id;
  end if;
  return null;
end;
$$;

create function public.delete_content_interactions()
returns trigger
language plpgsql security definer set search_path = ''
as $$
begin
  delete from public.interactions where item_type = tg_argv[0] and item_id = old.id;
  return null;
end;
$$;

revoke execute on all functions in schema public from public, anon, authenticated;
grant execute on function public.get_user_profile(uuid) to authenticated;
grant execute on function public.search_services(text, integer) to anon, authenticated;

-- Triggers

create trigger promocodes_service_count after insert or delete on public.promocodes
for each row execute function public.update_service_promocode_count();

create trigger promocodes_user_stats after insert or delete on public.promocodes
for each row execute function public.update_user_promocode_stats();

create trigger posts_user_stats after insert or delete on public.posts
for each row execute function public.update_user_post_stats();

create trigger interactions_votes after insert or update of vote_state or delete on public.interactions
for each row execute function public.apply_interaction_votes();

create trigger promocodes_delete_interactions after delete on public.promocodes
for each row execute function public.delete_content_interactions('PROMOCODE');

create trigger posts_delete_interactions after delete on public.posts
for each row execute function public.delete_content_interactions('POST');

-- Privileges. Clients get named columns only, so counts, votes and verified stay server-owned.

revoke all on all tables in schema public from anon, authenticated;

grant select on public.services, public.promocodes, public.posts, public.banners, public.app_config to anon, authenticated;
grant select on public.promocodes_view, public.posts_view to anon, authenticated;

grant select on public.users, public.reports, public.blocks, public.interactions to authenticated;
grant insert (id, display_name, photo_url, legal_policies_accepted_at) on public.users to authenticated;
grant update (display_name, photo_url, legal_policies_accepted_at) on public.users to authenticated;
grant insert (id, name, site_url, logo_url) on public.services to authenticated;
grant insert (
  id, code, service_id, author_id, start_date, end_date, discount_type, discount_value,
  free_item_description, minimum_order_amount, description, image_urls
) on public.promocodes to authenticated;
grant delete on public.promocodes to authenticated;
grant insert (id, author_id, title, content, image_urls, tags) on public.posts to authenticated;
grant delete on public.posts to authenticated;
grant insert (reported_item_id, reported_item_type, reporter_id, reason, additional_details) on public.reports to authenticated;
grant insert (user_id, blocked_user_id), delete on public.blocks to authenticated;
grant insert (user_id, item_type, item_id, vote_state, bookmarked), delete on public.interactions to authenticated;
grant update (vote_state, bookmarked) on public.interactions to authenticated;

-- Row level security

alter table public.users enable row level security;
alter table public.services enable row level security;
alter table public.promocodes enable row level security;
alter table public.posts enable row level security;
alter table public.banners enable row level security;
alter table public.reports enable row level security;
alter table public.app_config enable row level security;
alter table public.blocks enable row level security;
alter table public.interactions enable row level security;

create policy "Users read their own row" on public.users
for select to authenticated using (id = (select auth.uid()));
create policy "Users create their own row" on public.users
for insert to authenticated with check (id = (select auth.uid()));
create policy "Users update their own row" on public.users
for update to authenticated using (id = (select auth.uid())) with check (id = (select auth.uid()));

create policy "Services are public" on public.services for select to anon, authenticated using (true);
create policy "Signed-in users add services" on public.services for insert to authenticated with check (true);

create policy "Promocodes are public" on public.promocodes for select to anon, authenticated using (true);
create policy "Authors create promocodes" on public.promocodes
for insert to authenticated with check (author_id = (select auth.uid()));
create policy "Authors delete promocodes" on public.promocodes
for delete to authenticated using (author_id = (select auth.uid()));

create policy "Posts are public" on public.posts for select to anon, authenticated using (true);
create policy "Authors create posts" on public.posts
for insert to authenticated with check (author_id = (select auth.uid()));
create policy "Authors delete posts" on public.posts
for delete to authenticated using (author_id = (select auth.uid()));

create policy "Banners are public" on public.banners for select to anon, authenticated using (true);
create policy "App config is public" on public.app_config for select to anon, authenticated using (true);

create policy "Reporters read their reports" on public.reports
for select to authenticated using (reporter_id = (select auth.uid()));
create policy "Reporters create reports" on public.reports
for insert to authenticated with check (reporter_id = (select auth.uid()));

create policy "Users manage their blocks" on public.blocks
for all to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));

create policy "Users manage their interactions" on public.interactions
for all to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));

-- Storage. Files live under <user id>/ so a user can't touch anyone else's.

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values
  ('post-images', 'post-images', true, 5242880, array['image/jpeg', 'image/png', 'image/webp']),
  ('promocode-images', 'promocode-images', true, 5242880, array['image/jpeg', 'image/png', 'image/webp']);

create policy "Users upload to their own folder" on storage.objects
for insert to authenticated
with check (
  bucket_id in ('post-images', 'promocode-images')
  and (storage.foldername(name))[1] = (select auth.uid())::text
);
