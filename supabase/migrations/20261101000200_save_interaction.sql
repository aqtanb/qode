-- Upserts through the Data API rewrite key columns, which clients can't update.
-- This runs as the caller, so RLS and grants still apply.
create function public.save_interaction(
  target_type text,
  target_id text,
  new_vote_state text,
  is_bookmarked boolean
)
returns void
language sql security invoker set search_path = ''
as $$
  insert into public.interactions (user_id, item_type, item_id, vote_state, bookmarked)
  values ((select auth.uid()), target_type, target_id, new_vote_state, is_bookmarked)
  on conflict (user_id, item_type, item_id)
  do update set vote_state = excluded.vote_state, bookmarked = excluded.bookmarked;
$$;

revoke execute on function public.save_interaction(text, text, text, boolean) from public, anon;
grant execute on function public.save_interaction(text, text, text, boolean) to authenticated, service_role;
