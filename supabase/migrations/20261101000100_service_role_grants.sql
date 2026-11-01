-- New tables aren't exposed by default on this project, so the service role needs explicit access
-- for seeding and Edge Functions. It bypasses RLS and is never shipped in the app.
grant all on all tables in schema public to service_role;
grant execute on all functions in schema public to service_role;
