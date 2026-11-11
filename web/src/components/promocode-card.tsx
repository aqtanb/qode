import Link from "next/link";
import type { Locale } from "@/i18n/config";
import type { Dictionary } from "@/i18n/get-dictionary";
import { fill } from "@/lib/fill";
import { discountLabel, formatDate, isExpiringSoon } from "@/lib/format";
import type { Promocode } from "@/lib/types";
import { CopyButton } from "./copy-button";
import { ServiceLogo } from "./service-logo";
import { VoteButtons } from "./vote-buttons";

export function PromocodeCard({ promo, locale, dict }: { promo: Promocode; locale: Locale; dict: Dictionary }) {
  const expiring = isExpiringSoon(promo.end_date);
  return (
    <article className="relative flex overflow-hidden rounded-3xl bg-surface-container transition hover:bg-surface-container-high">
      <div className="flex min-w-0 flex-1 flex-col gap-4 p-5">
        <div className="flex items-center gap-3">
          <ServiceLogo name={promo.service_name} url={promo.service_logo_url} />
          <Link
            href={`/${locale}/promocodes/${encodeURIComponent(promo.id)}`}
            className="truncate font-display text-lg font-semibold after:absolute after:inset-0"
          >
            {promo.service_name}
          </Link>
          {promo.verified && (
            <span className="rounded-full bg-success/15 px-2 py-0.5 text-xs font-medium text-success">
              {dict.promo.verified}
            </span>
          )}
        </div>
        <div className="flex items-end justify-between gap-3">
          <div className="min-w-0">
            <p className="text-xs uppercase tracking-wider text-on-surface-variant">{dict.promo.promocode}</p>
            <p className="truncate font-mono text-2xl font-bold tracking-wider">{promo.code}</p>
          </div>
          <div className="relative z-10">
            <CopyButton text={promo.code} />
          </div>
        </div>
        <div className="relative z-10 self-start">
          <VoteButtons type="PROMOCODE" id={promo.id} score={promo.vote_score} />
        </div>
      </div>
      <div className="flex w-28 shrink-0 flex-col items-center justify-center gap-2 border-l-2 border-dashed border-background bg-tertiary-container p-3 text-center text-on-tertiary-container sm:w-32">
        <span className="line-clamp-3 font-display text-xl font-bold leading-tight">
          {discountLabel(promo, locale, dict.promo.freeItem)}
        </span>
        <span className="text-xs">
          {expiring
            ? dict.promo.expiringSoon
            : fill(dict.promo.validUntil, { date: formatDate(promo.end_date, locale) })}
        </span>
      </div>
    </article>
  );
}
