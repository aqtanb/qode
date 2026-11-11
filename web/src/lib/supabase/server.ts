import "server-only";
import { createClient } from "@supabase/supabase-js";
import { env } from "@/lib/env";

// Server reads are public (anon), so no session or cookies are involved.
export const createServerClient = () =>
  createClient(env.supabaseUrl, env.supabaseKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
