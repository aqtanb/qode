"use client";

import { usePathname, useRouter } from "next/navigation";
import { localeNames, locales, type Locale } from "@/i18n/config";
import { useI18n } from "./i18n-context";

export function LocaleSwitcher() {
  const { locale, dict } = useI18n();
  const pathname = usePathname();
  const router = useRouter();

  const change = (next: Locale) => {
    document.cookie = `locale=${next}; path=/; max-age=31536000; samesite=lax`;
    const rest = pathname.split("/").slice(2).join("/");
    router.push(`/${next}${rest ? `/${rest}` : ""}${window.location.search}`);
  };

  return (
    <label className="relative">
      <span className="sr-only">{dict.nav.language}</span>
      <select
        value={locale}
        onChange={(e) => change(e.target.value as Locale)}
        className="h-9 cursor-pointer appearance-none rounded-full border border-outline-variant bg-transparent px-3 text-sm font-medium"
      >
        {locales.map((l) => (
          <option key={l} value={l} className="bg-surface text-on-surface normal-case">
            {localeNames[l]}
          </option>
        ))}
      </select>
    </label>
  );
}
