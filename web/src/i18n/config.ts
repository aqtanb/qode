export const locales = ["kk", "ru", "en"] as const;
export type Locale = (typeof locales)[number];
export const defaultLocale: Locale = "kk";

export const localeNames: Record<Locale, string> = {
  kk: "Қазақша",
  ru: "Русский",
  en: "English",
};

export const themes = ["light", "dark"] as const;
export type Theme = (typeof themes)[number];
export const isTheme = (value: string | undefined): value is Theme =>
  (themes as readonly (string | undefined)[]).includes(value);

export const hasLocale = (value: string): value is Locale => (locales as readonly string[]).includes(value);
