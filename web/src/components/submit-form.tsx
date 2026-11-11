"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { fill } from "@/lib/fill";
import {
  isValidServiceName,
  isValidSiteUrl,
  logoUrlFor,
  newServiceId,
  promocodeId,
  sanitizeUrl,
  validatePromocode,
  type PromocodeForm,
} from "@/lib/promocode";
import { getBrowserClient } from "@/lib/supabase/browser";
import type { DiscountType, Service } from "@/lib/types";
import { useAuth } from "./auth-context";
import { useI18n } from "./i18n-context";
import { ServiceLogo } from "./service-logo";

type Picked = { kind: "existing"; service: Service } | { kind: "new"; name: string };
type ErrorKey = keyof ReturnType<typeof useI18n>["dict"]["submit"]["errors"];

const today = () => new Date().toISOString().slice(0, 10);
const inMonth = () => new Date(Date.now() + 30 * 86_400_000).toISOString().slice(0, 10);
const TYPES: DiscountType[] = ["Percentage", "FixedAmount", "FreeItem"];

export function SubmitForm() {
  const { dict, locale } = useI18n();
  const t = dict.submit;
  const { session, ready, openSignIn } = useAuth();
  const supabase = getBrowserClient();
  const router = useRouter();

  const [query, setQuery] = useState("");
  const [results, setResults] = useState<Service[]>([]);
  const [picked, setPicked] = useState<Picked | null>(null);
  const [siteUrl, setSiteUrl] = useState("");
  const [form, setForm] = useState<PromocodeForm>({
    code: "",
    discountType: "Percentage",
    discountValue: "",
    freeItem: "",
    minOrder: "",
    startDate: today(),
    endDate: inMonth(),
    description: "",
  });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<ErrorKey | null>(null);

  useEffect(() => {
    const q = query.trim();
    if (picked || q.length < 1) return;
    const timer = setTimeout(async () => {
      const { data } = await supabase.rpc("search_services", { search_query: q, result_limit: 8 });
      setResults((data as Service[] | null) ?? []);
    }, 250);
    return () => clearTimeout(timer);
  }, [query, picked, supabase]);

  const visibleResults = picked || !query.trim() ? [] : results;
  const exactMatch = visibleResults.some((s) => s.name.toLowerCase() === query.trim().toLowerCase());

  const set = <K extends keyof PromocodeForm>(key: K, value: PromocodeForm[K]) =>
    setForm((f) => ({ ...f, [key]: value }));

  if (!ready) return null;
  if (!session) {
    return (
      <div className="flex flex-col items-start gap-4 rounded-3xl bg-surface-container p-6">
        <p>{t.signInNeeded}</p>
        <button
          type="button"
          onClick={openSignIn}
          className="rounded-full bg-primary px-5 py-2.5 text-sm font-medium text-on-primary"
        >
          {dict.nav.signIn}
        </button>
      </div>
    );
  }

  const resolveService = async (): Promise<Service | ErrorKey> => {
    if (!picked) return "service";
    if (picked.kind === "existing") return picked.service;
    const name = picked.name.trim();
    if (!isValidServiceName(name)) return "serviceName";
    const { data: found } = await supabase
      .from("services")
      .select("id, name, site_url, logo_url, promocode_count")
      .eq("name", name)
      .limit(1);
    if (found && found.length > 0) return found[0] as Service;
    if (!isValidSiteUrl(siteUrl)) return "siteUrl";
    const site = sanitizeUrl(siteUrl);
    const service: Service = {
      id: newServiceId(),
      name,
      site_url: site,
      logo_url: logoUrlFor(site),
      promocode_count: 0,
    };
    const { error: insertError } = await supabase.from("services").insert({
      id: service.id,
      name: service.name,
      site_url: service.site_url,
      logo_url: service.logo_url,
    });
    return insertError ? "generic" : service;
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!picked) return setError("service");
    const serviceName = picked.kind === "existing" ? picked.service.name : picked.name.trim();
    const invalid = validatePromocode(form, serviceName);
    if (invalid) return setError(invalid);

    setBusy(true);
    const service = await resolveService();
    if (typeof service === "string") {
      setBusy(false);
      return setError(service);
    }
    const code = form.code.trim();
    const id = promocodeId(service.name, code);
    const { error: insertError } = await supabase.from("promocodes").insert({
      id,
      code,
      service_id: service.id,
      author_id: session.user.id,
      start_date: new Date(form.startDate).toISOString(),
      end_date: new Date(`${form.endDate}T23:59:59`).toISOString(),
      discount_type: form.discountType,
      discount_value: form.discountType === "FreeItem" ? null : Number(form.discountValue),
      free_item_description: form.discountType === "FreeItem" ? form.freeItem.trim() : null,
      minimum_order_amount: form.minOrder.trim() === "" ? 0 : Number(form.minOrder),
      description: form.description.trim() || null,
      image_urls: [],
    });
    setBusy(false);
    if (insertError) return setError(insertError.code === "23505" ? "duplicate" : "generic");
    router.push(`/${locale}/promocodes/${encodeURIComponent(id)}`);
  };

  const input =
    "h-12 w-full rounded-xl border border-outline bg-transparent px-4 outline-none focus:border-primary focus:ring-1 focus:ring-primary";
  const label = "flex flex-col gap-1.5 text-sm font-medium";

  return (
    <form onSubmit={submit} className="flex flex-col gap-5">
      <div className={label}>
        <span>{t.service}</span>
        {picked ? (
          <div className="flex h-12 items-center gap-3 rounded-xl bg-secondary-container px-4 text-on-secondary-container">
            {picked.kind === "existing" && (
              <ServiceLogo name={picked.service.name} url={picked.service.logo_url} size={24} />
            )}
            <span className="flex-1 truncate">{picked.kind === "existing" ? picked.service.name : picked.name}</span>
            <button type="button" onClick={() => setPicked(null)} className="text-lg" aria-label="clear">
              ×
            </button>
          </div>
        ) : (
          <div className="relative">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder={t.servicePlaceholder}
              className={input}
            />
            {query.trim() && (
              <ul className="absolute z-20 mt-1 w-full overflow-hidden rounded-2xl bg-surface-container-high shadow-lg">
                {visibleResults.map((s) => (
                  <li key={s.id}>
                    <button
                      type="button"
                      onClick={() => setPicked({ kind: "existing", service: s })}
                      className="flex w-full items-center gap-3 px-4 py-3 text-left hover:bg-surface-container"
                    >
                      <ServiceLogo name={s.name} url={s.logo_url} size={24} />
                      {s.name}
                    </button>
                  </li>
                ))}
                {!exactMatch && (
                  <li>
                    <button
                      type="button"
                      onClick={() => setPicked({ kind: "new", name: query.trim() })}
                      className="w-full px-4 py-3 text-left text-primary hover:bg-surface-container"
                    >
                      {fill(t.newService, { name: query.trim() })}
                    </button>
                  </li>
                )}
              </ul>
            )}
          </div>
        )}
      </div>

      {picked?.kind === "new" && (
        <label className={label}>
          <span>{t.siteUrl}</span>
          <input
            value={siteUrl}
            onChange={(e) => setSiteUrl(e.target.value)}
            placeholder={t.siteUrlPlaceholder}
            inputMode="url"
            className={input}
          />
        </label>
      )}

      <label className={label}>
        <span>{t.code}</span>
        <input
          value={form.code}
          onChange={(e) => set("code", e.target.value)}
          maxLength={50}
          autoCapitalize="characters"
          className={`${input} font-mono uppercase tracking-wider`}
        />
      </label>

      <div className={label}>
        <span>{t.type}</span>
        <div className="flex gap-1 rounded-full bg-surface-container p-1">
          {TYPES.map((type) => (
            <button
              key={type}
              type="button"
              onClick={() => set("discountType", type)}
              className={`flex-1 rounded-full px-3 py-2 text-sm ${form.discountType === type ? "bg-primary text-on-primary" : "text-on-surface-variant"}`}
            >
              {t.types[type]}
            </button>
          ))}
        </div>
      </div>

      <div className="grid gap-5 sm:grid-cols-2">
        {form.discountType === "FreeItem" ? (
          <label className={label}>
            <span>{t.freeItem}</span>
            <input
              value={form.freeItem}
              onChange={(e) => set("freeItem", e.target.value)}
              maxLength={50}
              className={input}
            />
          </label>
        ) : (
          <label className={label}>
            <span>{form.discountType === "Percentage" ? t.percentage : t.amount}</span>
            <input
              type="number"
              inputMode="decimal"
              min={0}
              step="any"
              value={form.discountValue}
              onChange={(e) => set("discountValue", e.target.value)}
              className={input}
            />
          </label>
        )}
        <label className={label}>
          <span>{t.minOrder}</span>
          <input
            type="number"
            inputMode="decimal"
            min={0}
            step="any"
            value={form.minOrder}
            onChange={(e) => set("minOrder", e.target.value)}
            className={input}
          />
        </label>
        <label className={label}>
          <span>{t.startDate}</span>
          <input
            type="date"
            value={form.startDate}
            onChange={(e) => set("startDate", e.target.value)}
            className={input}
          />
        </label>
        <label className={label}>
          <span>{t.endDate}</span>
          <input type="date" value={form.endDate} onChange={(e) => set("endDate", e.target.value)} className={input} />
        </label>
      </div>

      <label className={label}>
        <span>{t.description}</span>
        <textarea
          value={form.description}
          onChange={(e) => set("description", e.target.value)}
          rows={3}
          maxLength={1000}
          className="w-full rounded-xl border border-outline bg-transparent p-4 outline-none focus:border-primary focus:ring-1 focus:ring-primary"
        />
      </label>

      {error && (
        <p role="alert" className="text-sm text-error">
          {t.errors[error]}
        </p>
      )}

      <button
        type="submit"
        disabled={busy}
        className="h-14 rounded-xl bg-primary text-base font-medium text-on-primary disabled:opacity-40"
      >
        {t.submit}
      </button>
    </form>
  );
}
