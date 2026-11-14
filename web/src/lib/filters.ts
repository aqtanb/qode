export type PromocodeSort = "popular" | "newest" | "expiring";
export const sorts: PromocodeSort[] = ["popular", "newest", "expiring"];

export const PAGE_SIZE = 30;
const MAX_LIMIT = 300;

export type DiscountKind = "percent" | "fixed" | "free";
export const discountKinds: DiscountKind[] = ["percent", "fixed", "free"];
export const discountTypeFor: Record<DiscountKind, string> = {
  percent: "Percentage",
  fixed: "FixedAmount",
  free: "FreeItem",
};

export interface PromoFilters {
  q: string;
  sort: PromocodeSort;
  services: string[];
  kinds: DiscountKind[];
  noMin: boolean;
  verified: boolean;
  limit: number;
}

type Params = Record<string, string | string[] | undefined>;

const all = (value: string | string[] | undefined) =>
  value === undefined ? [] : Array.isArray(value) ? value : [value];

export function parseFilters(params: Params): PromoFilters {
  const sort = all(params.sort)[0] as PromocodeSort;
  return {
    q: (all(params.q)[0] ?? "").slice(0, 50),
    sort: sorts.includes(sort) ? sort : "popular",
    services: [...new Set(all(params.service))].slice(0, 20),
    kinds: discountKinds.filter((kind) => all(params.type).includes(kind)),
    noMin: all(params.nomin)[0] === "1",
    verified: all(params.verified)[0] === "1",
    limit: Math.min(Math.max(Math.floor(Number(all(params.limit)[0])) || PAGE_SIZE, PAGE_SIZE), MAX_LIMIT),
  };
}

export function filtersQuery(filters: PromoFilters): string {
  const p = new URLSearchParams();
  if (filters.q.trim()) p.set("q", filters.q.trim());
  if (filters.sort !== "popular") p.set("sort", filters.sort);
  filters.services.forEach((service) => p.append("service", service));
  filters.kinds.forEach((kind) => p.append("type", kind));
  if (filters.noMin) p.set("nomin", "1");
  if (filters.verified) p.set("verified", "1");
  if (filters.limit > PAGE_SIZE) p.set("limit", String(filters.limit));
  const qs = p.toString();
  return qs ? `?${qs}` : "";
}

export const hasActiveFilters = (f: PromoFilters) =>
  Boolean(f.q.trim()) || f.services.length > 0 || f.kinds.length > 0 || f.noMin || f.verified;

// Keeps letters, digits, spaces and hyphens so the term is safe inside a PostgREST or() filter.
export const searchTerm = (q: string) =>
  q
    .replace(/[^\p{L}\p{N}\s-]/gu, " ")
    .replace(/\s+/g, " ")
    .trim()
    .slice(0, 50);
