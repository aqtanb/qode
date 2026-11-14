import "server-only";
import { createServerClient } from "@/lib/supabase/server";
import { discountTypeFor, searchTerm, type PromocodeSort, type PromoFilters } from "@/lib/filters";
import type { Banner, Post, Promocode, Service } from "@/lib/types";

const SORTS: Record<PromocodeSort, { column: string; ascending: boolean }> = {
  popular: { column: "vote_score", ascending: false },
  newest: { column: "created_at", ascending: false },
  expiring: { column: "end_date", ascending: true },
};

export async function getPromocodes(filters: PromoFilters, limit = 30): Promise<{ items: Promocode[]; total: number }> {
  const { column, ascending } = SORTS[filters.sort];
  let query = createServerClient()
    .from("promocodes_view")
    .select("*", { count: "exact" })
    .gte("end_date", new Date().toISOString())
    .order(column, { ascending })
    .order("id", { ascending: true })
    .limit(limit);
  if (filters.services.length > 0) query = query.in("service_name", filters.services);
  if (filters.kinds.length > 0)
    query = query.in(
      "discount_type",
      filters.kinds.map((kind) => discountTypeFor[kind]),
    );
  if (filters.noMin) query = query.eq("minimum_order_amount", 0);
  if (filters.verified) query = query.eq("verified", true);
  const term = searchTerm(filters.q);
  if (term) query = query.or(`code.ilike.*${term}*,service_name.ilike.*${term}*,description.ilike.*${term}*`);
  const { data, error, count } = await query;
  if (error) throw error;
  return { items: data as Promocode[], total: count ?? data.length };
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
