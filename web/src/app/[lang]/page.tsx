import Link from "next/link";
import { notFound } from "next/navigation";
import { Banners } from "@/components/banners";
import { PromocodeCard } from "@/components/promocode-card";
import { ServiceLogo } from "@/components/service-logo";
import { hasLocale } from "@/i18n/config";
import { getDictionary } from "@/i18n/get-dictionary";
import { getBanners, getPopularServices, getPromocodes, type PromocodeSort } from "@/lib/queries";

const SORTS: PromocodeSort[] = ["popular", "newest", "expiring"];

export default async function HomePage({ params, searchParams }: PageProps<"/[lang]">) {
  const { lang } = await params;
  if (!hasLocale(lang)) notFound();
  const query = await searchParams;
  const sort = SORTS.includes(query.sort as PromocodeSort) ? (query.sort as PromocodeSort) : "popular";
  const service = typeof query.service === "string" ? query.service : undefined;

  const [dict, banners, services, promocodes] = await Promise.all([
    getDictionary(lang),
    getBanners(),
    getPopularServices(),
    getPromocodes(sort, service),
  ]);

  const href = (next: { sort?: PromocodeSort; service?: string | null }) => {
    const p = new URLSearchParams();
    const s = next.sort ?? sort;
    const sv = next.service === undefined ? service : next.service;
    if (s !== "popular") p.set("sort", s);
    if (sv) p.set("service", sv);
    const qs = p.toString();
    return `/${lang}${qs ? `?${qs}` : ""}`;
  };

  const chip = (active: boolean) =>
    `flex shrink-0 items-center gap-2 rounded-full border px-3 py-1.5 text-sm font-medium ${
      active
        ? "border-transparent bg-secondary-container text-on-secondary-container"
        : "border-outline-variant hover:bg-surface-container-high"
    }`;

  return (
    <div className="flex flex-col gap-6">
      <Banners banners={banners} locale={lang} />

      <section className="flex flex-col gap-4">
        <h1 className="font-display text-3xl font-bold">{dict.home.popular}</h1>

        <div className="no-scrollbar -mx-4 flex gap-2 overflow-x-auto px-4">
          <Link href={href({ service: null })} className={chip(!service)}>
            {dict.home.allServices}
          </Link>
          {services.map((s) => (
            <Link key={s.id} href={href({ service: s.name })} className={chip(service === s.name)}>
              <ServiceLogo name={s.name} url={s.logo_url} size={20} />
              {s.name}
            </Link>
          ))}
        </div>

        <div className="flex gap-1 rounded-full bg-surface-container p-1 self-start">
          {SORTS.map((s) => (
            <Link
              key={s}
              href={href({ sort: s })}
              className={`rounded-full px-4 py-1.5 text-sm font-medium ${s === sort ? "bg-primary text-on-primary" : "text-on-surface-variant"}`}
            >
              {dict.home.sort[s]}
            </Link>
          ))}
        </div>

        {promocodes.length === 0 ? (
          <p className="py-12 text-center text-on-surface-variant">{dict.home.empty}</p>
        ) : (
          <div className="grid gap-4 md:grid-cols-2">
            {promocodes.map((promo) => (
              <PromocodeCard key={promo.id} promo={promo} locale={lang} dict={dict} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
