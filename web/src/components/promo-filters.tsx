"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect, useMemo, useRef, useState, useTransition } from "react";
import { fill } from "@/lib/fill";
import {
  discountKinds,
  filtersQuery,
  hasActiveFilters,
  PAGE_SIZE,
  sorts,
  type DiscountKind,
  type PromoFilters,
} from "@/lib/filters";
import type { Service } from "@/lib/types";
import { Dialog } from "./dialog";
import { useI18n } from "./i18n-context";
import { ServiceLogo } from "./service-logo";

const chipClass = (active: boolean) =>
  `flex h-9 shrink-0 items-center gap-1.5 rounded-full border px-3.5 text-sm font-medium whitespace-nowrap ${
    active
      ? "border-transparent bg-secondary-container text-on-secondary-container"
      : "border-outline-variant text-on-surface hover:bg-surface-container-high"
  }`;

function Chevron() {
  return (
    <svg viewBox="0 0 24 24" className="size-4" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M6 9l6 6 6-6" />
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

export function PromoFilterBar({ filters, services }: { filters: PromoFilters; services: Service[] }) {
  const { dict } = useI18n();
  const t = dict.filters;
  const router = useRouter();
  const pathname = usePathname();
  const [pending, startTransition] = useTransition();
  const [q, setQ] = useState(filters.q);
  const [dialog, setDialog] = useState<"sort" | "service" | null>(null);

  const latest = useRef(filters);
  const searchTimer = useRef<ReturnType<typeof setTimeout>>(undefined);

  useEffect(() => {
    latest.current = filters;
  }, [filters]);

  useEffect(() => () => clearTimeout(searchTimer.current), []);

  const apply = (next: Partial<PromoFilters>) => {
    clearTimeout(searchTimer.current);
    const merged = { ...latest.current, limit: PAGE_SIZE, ...next };
    latest.current = merged;
    startTransition(() => router.replace(`${pathname}${filtersQuery(merged)}`, { scroll: false }));
  };

  const onSearch = (value: string) => {
    setQ(value);
    clearTimeout(searchTimer.current);
    searchTimer.current = setTimeout(() => {
      if (value.trim() !== latest.current.q.trim()) apply({ q: value });
    }, 350);
  };

  const toggleKind = (kind: DiscountKind) =>
    apply({ kinds: filters.kinds.includes(kind) ? filters.kinds.filter((k) => k !== kind) : [...filters.kinds, kind] });

  type ToggleKey = DiscountKind | "nomin" | "verified";
  const toggles: { key: ToggleKey; label: string; on: boolean }[] = [
    ...discountKinds.map((kind) => ({ key: kind, label: t[kind], on: filters.kinds.includes(kind) })),
    { key: "nomin", label: t.noMin, on: filters.noMin },
    { key: "verified", label: t.verified, on: filters.verified },
  ];

  const flip = (key: ToggleKey) => {
    if (key === "nomin") apply({ noMin: !filters.noMin });
    else if (key === "verified") apply({ verified: !filters.verified });
    else toggleKind(key);
  };

  const serviceLabel =
    filters.services.length === 1
      ? filters.services[0]
      : filters.services.length > 1
        ? fill(t.servicesSelected, { count: String(filters.services.length) })
        : t.service;

  return (
    <div className={`flex flex-col gap-3 transition-opacity ${pending ? "opacity-70" : ""}`}>
      <div className="relative">
        <svg
          viewBox="0 0 24 24"
          className="pointer-events-none absolute left-4 top-1/2 size-5 -translate-y-1/2 text-on-surface-variant"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
        >
          <circle cx="11" cy="11" r="7" />
          <path d="M20 20l-3.5-3.5" />
        </svg>
        <input
          type="search"
          value={q}
          onChange={(e) => onSearch(e.target.value)}
          placeholder={t.search}
          aria-label={t.search}
          maxLength={50}
          className="h-12 w-full rounded-full bg-surface-container pl-12 pr-12 text-base outline-none placeholder:text-on-surface-variant focus:ring-2 focus:ring-primary [&::-webkit-search-cancel-button]:hidden"
        />
        {q && (
          <button
            type="button"
            onClick={() => {
              setQ("");
              apply({ q: "" });
            }}
            aria-label={t.clearSearch}
            className="absolute right-2 top-1/2 flex size-9 -translate-y-1/2 items-center justify-center rounded-full text-on-surface-variant hover:bg-surface-container-high"
          >
            <svg viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M6 6l12 12M18 6L6 18" />
            </svg>
          </button>
        )}
      </div>

      <div className="no-scrollbar -mx-4 flex gap-2 overflow-x-auto px-4">
        <button type="button" onClick={() => setDialog("sort")} className={chipClass(filters.sort !== "popular")}>
          {dict.home.sort[filters.sort]}
          <Chevron />
        </button>
        <button type="button" onClick={() => setDialog("service")} className={chipClass(filters.services.length > 0)}>
          {serviceLabel}
          <Chevron />
        </button>
        {toggles
          .filter((toggle) => toggle.on)
          .map((toggle) => (
            <ToggleChip key={toggle.key} label={toggle.label} on={toggle.on} onToggle={() => flip(toggle.key)} />
          ))}
        {hasActiveFilters(filters) && (
          <button
            type="button"
            onClick={() => {
              setQ("");
              apply({ q: "", services: [], kinds: [], noMin: false, verified: false });
            }}
            className="flex h-9 shrink-0 items-center px-2 text-sm font-medium text-primary hover:underline"
          >
            {t.clear}
          </button>
        )}
        {toggles
          .filter((toggle) => !toggle.on)
          .map((toggle) => (
            <ToggleChip key={toggle.key} label={toggle.label} on={toggle.on} onToggle={() => flip(toggle.key)} />
          ))}
      </div>

      <Dialog open={dialog === "sort"} onClose={() => setDialog(null)} label={t.sort}>
        <h2 className="mb-3 font-display text-xl font-bold">{t.sort}</h2>
        <div className="flex flex-col">
          {sorts.map((sort) => (
            <button
              key={sort}
              type="button"
              onClick={() => {
                setDialog(null);
                apply({ sort });
              }}
              className="flex items-center justify-between rounded-xl px-3 py-3 text-left hover:bg-surface-container"
            >
              {dict.home.sort[sort]}
              {filters.sort === sort && <Check className="size-5 text-primary" />}
            </button>
          ))}
        </div>
      </Dialog>

      {dialog === "service" && (
        <ServicePicker
          services={services}
          selected={filters.services}
          onClose={() => setDialog(null)}
          onApply={(next) => {
            setDialog(null);
            apply({ services: next });
          }}
        />
      )}
    </div>
  );
}

function ToggleChip({ label, on, onToggle }: { label: string; on: boolean; onToggle: () => void }) {
  return (
    <button type="button" aria-pressed={on} onClick={onToggle} className={chipClass(on)}>
      {on && <Check />}
      {label}
    </button>
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
