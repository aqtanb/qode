-- Counts how often people pick a service as a filter, so the popular list reflects demand
-- instead of how many codes a service has. Only a total per service is kept, never who picked it.
-- Clients can't write the column (the insert grant names its columns), only bump it by one here.

alter table public.services add column pick_count integer not null default 0;

create function public.record_service_pick(service_id text)
returns void
language sql
security definer
set search_path = ''
as $$
  update public.services set pick_count = pick_count + 1 where id = service_id;
$$;

revoke execute on function public.record_service_pick(text) from public;
grant execute on function public.record_service_pick(text) to anon, authenticated, service_role;
