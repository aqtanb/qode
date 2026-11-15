"use client";

import { usePathname, useRouter } from "next/navigation";
import { useMemo, useState, useTransition } from "react";
import { fill } from "@/lib/fill";
import { filtersQuery, hasActiveFilters, PAGE_SIZE, sorts, type PromocodeSort, type PromoFilters } from "@/lib/filters";
import { getBrowserClient } from "@/lib/supabase/browser";
import type { Service } from "@/lib/types";
import { Dialog } from "./dialog";
import { useI18n } from "./i18n-context";
import { ServiceLogo } from "./service-logo";

const POPULAR_CHIPS = 10;

const chipClass = (active: boolean) =>
  `flex h-9 shrink-0 items-center gap-1.5 rounded-full border px-3.5 text-sm font-medium whitespace-nowrap ${
    active
      ? "border-transparent bg-secondary-container text-on-secondary-container"
      : "border-outline-variant text-on-surface hover:bg-surface-container-high"
  }`;

function useApplyFilters(filters: PromoFilters) {
  const router = useRouter();
  const pathname = usePathname();
  const [pending, startTransition] = useTransition();
  const apply = (next: Partial<PromoFilters>) =>
    startTransition(() =>
      router.replace(`${pathname}${filtersQuery({ ...filters, limit: PAGE_SIZE, ...next })}`, { scroll: false }),
    );
  return { apply, pending };
}

// Counts services the user just added, so the popular chips follow demand. Failures don't matter here.
// Supabase builders only send the request once awaited.
function recordPicks(ids: string[]) {
  const client = getBrowserClient();
  ids.forEach((id) => void client.rpc("record_service_pick", { service_id: id }).then(() => undefined));
}

function SortIcon({ sort }: { sort: PromocodeSort }) {
  const paths: Record<PromocodeSort, React.ReactNode> = {
    popular: <path d="M6 3h12l4 6-10 12L2 9l4-6zM2 9h20M12 21L8 9l4-6 4 6-4 12" />,
    newest: (
      <>
        <circle cx="12" cy="12" r="9" />
        <path d="M12 7v5l3 2" />
      </>
    ),
    expiring: <path d="M6 2h12M6 22h12M7 2c0 5 10 5 10 10S7 17 7 22M17 2c0 5-10 5-10 10s10 5 10 10" />,
  };
  return (
    <svg
      viewBox="0 0 24 24"
      className="size-5"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinejoin="round"
    >
      {paths[sort]}
    </svg>
  );
}

function Check({ className = "size-4" }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" className={className} fill="none" stroke="currentColor" strokeWidth="2.4">
      <path d="M5 12.5l4.5 4.5L19 7.5" />
    </svg>
  );
}

export function SortButton({ filters }: { filters: PromoFilters }) {
  const { dict } = useI18n();
  const { apply } = useApplyFilters(filters);
  const [open, setOpen] = useState(false);

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        aria-label={dict.filters.sort}
        className={`flex size-10 items-center justify-center rounded-full hover:bg-surface-container-high ${
          filters.sort === "popular" ? "text-on-surface-variant" : "text-primary"
        }`}
      >
        <SortIcon sort={filters.sort} />
      </button>
      <Dialog open={open} onClose={() => setOpen(false)} label={dict.filters.sort}>
        <h2 className="mb-3 font-display text-xl font-bold">{dict.filters.sort}</h2>
        <div className="flex flex-col">
          {sorts.map((sort) => (
            <button
              key={sort}
              type="button"
              onClick={() => {
                setOpen(false);
                apply({ sort });
              }}
              className="flex items-center gap-3 rounded-xl px-3 py-3 text-left hover:bg-surface-container"
            >
              <SortIcon sort={sort} />
              <span className="flex-1">{dict.home.sort[sort]}</span>
              {filters.sort === sort && <Check className="size-5 text-primary" />}
            </button>
          ))}
        </div>
      </Dialog>
    </>
  );
}

export function PromoFilterBar({ filters, services }: { filters: PromoFilters; services: Service[] }) {
  const { dict } = useI18n();
  const t = dict.filters;
  const { apply, pending } = useApplyFilters(filters);
  const [picking, setPicking] = useState(false);

  const idOf = (name: string) => services.find((s) => s.name === name)?.id;
  const setServices = (next: string[]) => {
    recordPicks(next.filter((name) => !filters.services.includes(name)).flatMap((name) => idOf(name) ?? []));
    apply({ services: next });
  };
  const toggle = (name: string) =>
    setServices(
      filters.services.includes(name) ? filters.services.filter((n) => n !== name) : [...filters.services, name],
    );

  const count = filters.services.length;

  return (
    <div
      className={`no-scrollbar -mx-4 flex items-center gap-2 overflow-x-auto px-4 transition-opacity ${pending ? "opacity-70" : ""}`}
    >
      <button type="button" onClick={() => setPicking(true)} className={chipClass(count > 0)}>
        <svg viewBox="0 0 24 24" className="size-4" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M3 9l1.5-5h15L21 9M3 9h18v2a3 3 0 01-6 0 3 3 0 01-6 0 3 3 0 01-6 0V9zM5 13v8h14v-8" />
        </svg>
        {count > 0 ? fill(t.servicesSelected, { count: String(count) }) : t.service}
        <svg viewBox="0 0 24 24" className="size-4" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M6 9l6 6 6-6" />
        </svg>
      </button>

      {services.slice(0, POPULAR_CHIPS).map((service) => {
        const on = filters.services.includes(service.name);
        return (
          <button
            key={service.id}
            type="button"
            aria-pressed={on}
            onClick={() => toggle(service.name)}
            className={chipClass(on)}
          >
            <ServiceLogo name={service.name} url={service.logo_url} size={20} />
            {service.name}
          </button>
        );
      })}

      {hasActiveFilters(filters) && (
        <button
          type="button"
          onClick={() => apply({ services: [] })}
          aria-label={t.clear}
          className="flex size-9 shrink-0 items-center justify-center rounded-full text-on-surface-variant hover:bg-surface-container-high"
        >
          <svg viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M6 6l12 12M18 6L6 18" />
          </svg>
        </button>
      )}

      {picking && (
        <ServicePicker
          services={services}
          selected={filters.services}
          onClose={() => setPicking(false)}
          onApply={(next) => {
            setPicking(false);
            setServices(next);
          }}
        />
      )}
    </div>
  );
}

function ServicePicker({
  services,
  selected,
  onClose,
  onApply,
}: {
  services: Service[];
  selected: string[];
  onClose: () => void;
  onApply: (services: string[]) => void;
}) {
  const { dict } = useI18n();
  const t = dict.filters;
  const [picked, setPicked] = useState<string[]>(selected);
  const [query, setQuery] = useState("");

  const visible = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return needle ? services.filter((s) => s.name.toLowerCase().includes(needle)) : services;
  }, [services, query]);

  const toggle = (name: string) =>
    setPicked((current) => (current.includes(name) ? current.filter((n) => n !== name) : [...current, name]));

  return (
    <Dialog open onClose={onClose} label={t.service}>
      <h2 className="mb-3 font-display text-xl font-bold">{t.service}</h2>
      <input
        type="search"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder={t.searchServices}
        aria-label={t.searchServices}
        className="mb-2 h-11 w-full rounded-full bg-surface-container px-4 text-base outline-none placeholder:text-on-surface-variant focus:ring-2 focus:ring-primary"
      />
      <div className="-mx-2 max-h-[50vh] overflow-y-auto">
        {visible.length === 0 ? (
          <p className="px-3 py-6 text-center text-sm text-on-surface-variant">{t.noResults}</p>
        ) : (
          visible.map((service) => {
            const on = picked.includes(service.name);
            return (
              <button
                key={service.id}
                type="button"
                role="checkbox"
                aria-checked={on}
                onClick={() => toggle(service.name)}
                className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left hover:bg-surface-container"
              >
                <ServiceLogo name={service.name} url={service.logo_url} size={28} />
                <span className="flex-1 truncate">{service.name}</span>
                <span className="text-sm text-on-surface-variant">{service.promocode_count}</span>
                <span
                  className={`flex size-5 items-center justify-center rounded-md border-2 ${
                    on ? "border-primary bg-primary text-on-primary" : "border-outline"
                  }`}
                >
                  {on && <Check className="size-3.5" />}
                </span>
              </button>
            );
          })
        )}
      </div>
      <div className="mt-4 flex items-center justify-between gap-3">
        <button
          type="button"
          onClick={() => setPicked([])}
          disabled={picked.length === 0}
          className="rounded-full px-4 py-2.5 text-sm font-medium text-primary disabled:opacity-40"
        >
          {t.reset}
        </button>
        <button
          type="button"
          onClick={() => onApply(picked)}
          className="rounded-full bg-primary px-5 py-2.5 text-sm font-medium text-on-primary"
        >
          {t.apply}
        </button>
      </div>
    </Dialog>
  );
}
