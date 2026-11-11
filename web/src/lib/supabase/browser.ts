"use client";

import { createClient, type SupabaseClient } from "@supabase/supabase-js";
import { env } from "@/lib/env";

let client: SupabaseClient | undefined;

export function getBrowserClient(): SupabaseClient {
  client ??= createClient(env.supabaseUrl, env.supabaseKey);
  return client;
}
