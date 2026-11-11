import "server-only";
import { createServerClient } from "@/lib/supabase/server";
import type { Banner, Post, Promocode, Service } from "@/lib/types";

export type PromocodeSort = "popular" | "newest" | "expiring";

const SORTS: Record<PromocodeSort, { column: string; ascending: boolean }> = {
  popular: { column: "vote_score", ascending: false },
  newest: { column: "created_at", ascending: false },
  expiring: { column: "end_date", ascending: true },
};

export async function getPromocodes(sort: PromocodeSort, serviceName?: string, limit = 30): Promise<Promocode[]> {
  const { column, ascending } = SORTS[sort];
  let query = createServerClient()
    .from("promocodes_view")
    .select("*")
    .gte("end_date", new Date().toISOString())
    .order(column, { ascending })
    .order("id", { ascending: true })
    .limit(limit);
  if (serviceName) query = query.eq("service_name", serviceName);
  const { data, error } = await query;
  if (error) throw error;
  return data as Promocode[];
}

export async function getPromocode(id: string): Promise<Promocode | null> {
  const { data, error } = await createServerClient().from("promocodes_view").select("*").eq("id", id).maybeSingle();
  if (error) throw error;
  return data as Promocode | null;
}

export async function getPosts(limit = 30): Promise<Post[]> {
  const { data, error } = await createServerClient()
    .from("posts_view")
    .select("*")
    .order("created_at", { ascending: false })
    .order("id", { ascending: true })
    .limit(limit);
  if (error) throw error;
  return data as Post[];
}

export async function getPost(id: string): Promise<Post | null> {
  const { data, error } = await createServerClient().from("posts_view").select("*").eq("id", id).maybeSingle();
  if (error) throw error;
  return data as Post | null;
}

export async function getPopularServices(limit = 12): Promise<Service[]> {
  const { data, error } = await createServerClient()
    .from("services")
    .select("id, name, site_url, logo_url, promocode_count")
    .gt("promocode_count", 0)
    .order("promocode_count", { ascending: false })
    .order("name", { ascending: true })
    .limit(limit);
  if (error) throw error;
  return data as Service[];
}

export async function getBanners(limit = 5): Promise<Banner[]> {
  const { data, error } = await createServerClient()
    .from("banners")
    .select("id, image_url, cta_title, cta_description, cta_url")
    .eq("is_active", true)
    .gt("expires_at", new Date().toISOString())
    .order("priority", { ascending: false })
    .limit(limit);
  if (error) throw error;
  return data as Banner[];
}
