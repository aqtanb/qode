-- Deals imported from the Telegram chat t.me/superpodeshevle (topic 24) by the daily routine.
-- source_url holds the original message link. It's unique for posts, so a message is never
-- imported twice; one message can list several codes, and promocode ids already prevent duplicates.
alter table public.promocodes add column source_url text;
alter table public.posts add column source_url text unique;

-- Remembers the last Telegram message the routine has read. Only the service role touches it.
create table public.ingest_state (
  source text primary key,
  last_message_id bigint not null default 0,
  updated_at timestamptz not null default now()
);

alter table public.ingest_state enable row level security;

grant select, insert, update on public.ingest_state to service_role;

insert into public.ingest_state (source) values ('telegram:superpodeshevle/24');
