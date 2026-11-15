export type PromocodeSort = "popular" | "newest" | "expiring";
export const sorts: PromocodeSort[] = ["popular", "newest", "expiring"];

export const PAGE_SIZE = 30;
const MAX_LIMIT = 300;

export interface PromoFilters {
  sort: PromocodeSort;
  services: string[];
  limit: number;
}

type Params = Record<string, string | string[] | undefined>;

const all = (value: string | string[] | undefined) =>
  value === undefined ? [] : Array.isArray(value) ? value : [value];

export function parseFilters(params: Params): PromoFilters {
  const sort = all(params.sort)[0] as PromocodeSort;
  return {
    sort: sorts.includes(sort) ? sort : "popular",
    services: [...new Set(all(params.service))].slice(0, 20),
    limit: Math.min(Math.max(Math.floor(Number(all(params.limit)[0])) || PAGE_SIZE, PAGE_SIZE), MAX_LIMIT),
  };
}

export function filtersQuery(filters: PromoFilters): string {
  const p = new URLSearchParams();
  if (filters.sort !== "popular") p.set("sort", filters.sort);
  filters.services.forEach((service) => p.append("service", service));
  if (filters.limit > PAGE_SIZE) p.set("limit", String(filters.limit));
  const qs = p.toString();
  return qs ? `?${qs}` : "";
}

export const hasActiveFilters = (f: PromoFilters) => f.services.length > 0;
