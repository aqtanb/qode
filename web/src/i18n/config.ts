export const locales = ["kk", "ru", "en"] as const;
export type Locale = (typeof locales)[number];
export const defaultLocale: Locale = "kk";

export const localeNames: Record<Locale, string> = {
  kk: "Қаз",
  ru: "Рус",
  en: "Eng",
};

export const hasLocale = (value: string): value is Locale => (locales as readonly string[]).includes(value);
