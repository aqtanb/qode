import type { Locale } from "@/i18n/config";
import type { Promocode } from "@/lib/types";

const intlLocale: Record<Locale, string> = { kk: "kk-KZ", ru: "ru-RU", en: "en-GB" };

export const formatTenge = (amount: number, locale: Locale) =>
  `${new Intl.NumberFormat(intlLocale[locale], { maximumFractionDigits: 2 }).format(amount).replace(/[\u00a0\u202f,]/g, " ")} ₸`;

// Workers ship limited ICU data, so month names are spelled out here.
const MONTHS: Record<Locale, string[]> = {
  kk: [
    "қаңтар",
    "ақпан",
    "наурыз",
    "сәуір",
    "мамыр",
    "маусым",
    "шілде",
    "тамыз",
    "қыркүйек",
    "қазан",
    "қараша",
    "желтоқсан",
  ],
  ru: [
    "января",
    "февраля",
    "марта",
    "апреля",
    "мая",
    "июня",
    "июля",
    "августа",
    "сентября",
    "октября",
    "ноября",
    "декабря",
  ],
  en: [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ],
};

export function formatDate(iso: string, locale: Locale): string {
  const date = new Date(iso);
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Almaty",
    year: "numeric",
    month: "numeric",
    day: "numeric",
  })
    .formatToParts(date)
    .reduce<Record<string, string>>((acc, p) => ({ ...acc, [p.type]: p.value }), {});
  const month = MONTHS[locale][Number(parts.month) - 1];
  return `${Number(parts.day)} ${month} ${parts.year}`;
}

export function discountLabel(promo: Promocode, locale: Locale, freeLabel: string): string {
  switch (promo.discount_type) {
    case "Percentage":
      return `${promo.discount_value ?? 0}%`;
    case "FixedAmount":
      return formatTenge(promo.discount_value ?? 0, locale);
    case "FreeItem":
      return promo.free_item_description || freeLabel;
  }
}

const DAY = 86_400_000;
export const isExpiringSoon = (iso: string) => new Date(iso).getTime() - Date.now() < 3 * DAY;

export const localized = (map: Record<string, string>, locale: Locale) => map[locale] ?? map.en ?? map.default ?? "";
