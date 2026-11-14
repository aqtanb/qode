import Link from "next/link";
import { notFound } from "next/navigation";
import { Banners } from "@/components/banners";
import { PromocodeCard } from "@/components/promocode-card";
import { PromoFilterBar } from "@/components/promo-filters";
import { hasLocale } from "@/i18n/config";
import { getDictionary } from "@/i18n/get-dictionary";
import { fill } from "@/lib/fill";
import { filtersQuery, hasActiveFilters, PAGE_SIZE, parseFilters } from "@/lib/filters";
import { getBanners, getPopularServices, getPromocodes } from "@/lib/queries";

export default async function HomePage({ params, searchParams }: PageProps<"/[lang]">) {
  const { lang } = await params;
  if (!hasLocale(lang)) notFound();
  const filters = parseFilters(await searchParams);
  const filtered = hasActiveFilters(filters);

  const [dict, banners, services, { items: promocodes, total }] = await Promise.all([
    getDictionary(lang),
    filtered ? Promise.resolve([]) : getBanners(),
    getPopularServices(100),
    getPromocodes(filters, filters.limit),
  ]);

  return (
    <div className="flex flex-col gap-6">
      <Banners banners={banners} locale={lang} />

      <section className="flex flex-col gap-4">
        <h1 className="font-display text-3xl font-bold">
          {filters.sort === "popular" ? dict.home.popular : dict.nav.home}
        </h1>

        <PromoFilterBar filters={filters} services={services} />

        {filtered && promocodes.length > 0 && (
          <p className="text-sm text-on-surface-variant">{fill(dict.filters.results, { count: String(total) })}</p>
        )}

        {promocodes.length === 0 ? (
          <p className="py-12 text-center text-on-surface-variant">
            {filtered ? dict.filters.noResults : dict.home.empty}
          </p>
        ) : (
          <div className="grid gap-4 md:grid-cols-2">
            {promocodes.map((promo) => (
              <PromocodeCard key={promo.id} promo={promo} locale={lang} dict={dict} />
            ))}
          </div>
        )}

        {total > promocodes.length && promocodes.length >= filters.limit && (
          <Link
            href={`/${lang}${filtersQuery({ ...filters, limit: filters.limit + PAGE_SIZE })}`}
            scroll={false}
            className="self-center rounded-full border border-outline px-6 py-2.5 text-sm font-medium hover:bg-surface-container-high"
          >
            {dict.home.showMore}
          </Link>
        )}
      </section>
    </div>
  );
}
