-- Private hand-off between the GitHub Action that reads Telegram and the Claude routine
-- that publishes deals. No policies: only the service role can read or write it.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('deals-inbox', 'deals-inbox', false, 10485760, array['image/jpeg', 'application/json']);
