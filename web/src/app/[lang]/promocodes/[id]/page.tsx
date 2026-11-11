import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { cache } from "react";
import { CopyButton } from "@/components/copy-button";
import { ServiceLogo } from "@/components/service-logo";
import { ShareButton } from "@/components/share-button";
import { VoteButtons } from "@/components/vote-buttons";
import { hasLocale } from "@/i18n/config";
import { getDictionary } from "@/i18n/get-dictionary";
import { fill } from "@/lib/fill";
import { discountLabel, formatDate, formatTenge } from "@/lib/format";
import { getPromocode } from "@/lib/queries";

const load = cache((id: string) => getPromocode(decodeURIComponent(id)));

export async function generateMetadata({ params }: PageProps<"/[lang]/promocodes/[id]">): Promise<Metadata> {
  const { lang, id } = await params;
  if (!hasLocale(lang)) return {};
  const [dict, promo] = await Promise.all([getDictionary(lang), load(id)]);
  if (!promo) return { title: dict.promo.notFound };
  const discount = discountLabel(promo, lang, dict.promo.freeItem);
  const title = `${promo.service_name}: ${discount}`;
  const description = `${dict.promo.promocode} ${promo.code}. ${fill(dict.promo.validUntil, { date: formatDate(promo.end_date, lang) })}`;
  return {
    title,
    description,
    alternates: { canonical: `/${lang}/promocodes/${id}` },
    openGraph: { title, description, images: promo.service_logo_url ? [promo.service_logo_url] : undefined },
    twitter: { card: "summary", title, description },
  };
}

export default async function PromocodePage({ params }: PageProps<"/[lang]/promocodes/[id]">) {
  const { lang, id } = await params;
  if (!hasLocale(lang)) notFound();
  const [dict, promo] = await Promise.all([getDictionary(lang), load(id)]);
  if (!promo) notFound();
  const t = dict.promo;
  const expired = new Date(promo.end_date) < new Date();
  const siteUrl = promo.service_site_url ? `https://${promo.service_site_url}` : null;

  return (
    <article className="mx-auto flex max-w-2xl flex-col gap-6">
      <div className="flex items-center gap-4">
        <ServiceLogo name={promo.service_name} url={promo.service_logo_url} size={56} />
        <div className="min-w-0">
          <h1 className="truncate font-display text-3xl font-bold">{promo.service_name}</h1>
          <p className="text-sm text-on-surface-variant">{fill(t.by, { name: promo.author_name ?? t.anonymous })}</p>
        </div>
      </div>

      <div className="overflow-hidden rounded-3xl bg-surface-container">
        <div className="bg-tertiary-container p-6 text-center text-on-tertiary-container">
          <p className="font-display text-4xl font-extrabold">{discountLabel(promo, lang, t.freeItem)}</p>
          <p className="mt-1 text-sm">
            {promo.minimum_order_amount > 0
              ? fill(t.minOrder, { amount: formatTenge(promo.minimum_order_amount, lang) })
              : t.noMinOrder}
          </p>
        </div>
        <div className="flex flex-col items-center gap-4 p-6">
          <p className="text-xs uppercase tracking-wider text-on-surface-variant">{t.promocode}</p>
          <p className="break-all text-center font-mono text-4xl font-bold tracking-widest">{promo.code}</p>
          <CopyButton text={promo.code} large />
          <p className={`text-sm ${expired ? "text-error" : "text-on-surface-variant"}`}>
            {expired ? t.expired : fill(t.validUntil, { date: formatDate(promo.end_date, lang) })}
          </p>
        </div>
      </div>

      {promo.description && <p className="whitespace-pre-line text-on-surface-variant">{promo.description}</p>}

      <div className="flex flex-wrap items-center gap-3">
        <VoteButtons type="PROMOCODE" id={promo.id} score={promo.vote_score} />
        <ShareButton title={`${promo.service_name}: ${promo.code}`} />
        {siteUrl && (
          <a
            href={siteUrl}
            target="_blank"
            rel="noreferrer"
            className="flex h-10 items-center rounded-full border border-outline-variant px-4 text-sm font-medium hover:bg-surface-container-high"
          >
            {fill(t.openService, { service: promo.service_name })} ↗
          </a>
        )}
      </div>
    </article>
  );
}
