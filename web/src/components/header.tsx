import Link from "next/link";
import type { Locale } from "@/i18n/config";
import type { Dictionary } from "@/i18n/get-dictionary";
import { AccountButton } from "./account-button";
import { LocaleSwitcher } from "./locale-switcher";
import { NavLinks } from "./nav-links";
import { QodeLogo } from "./qode-logo";

export function Header({ locale, dict }: { locale: Locale; dict: Dictionary }) {
  return (
    <header className="sticky top-0 z-30 border-b border-outline-variant/60 bg-background/85 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-5xl items-center gap-2 px-4 sm:gap-4">
        <Link
          href={`/${locale}`}
          className="flex items-center gap-2 font-display text-2xl font-extrabold tracking-tight text-on-surface"
        >
          <span className="flex size-7 items-center justify-center rounded-lg bg-brand-deep">
            <QodeLogo className="size-[18px] text-brand" />
          </span>
          qode
        </Link>
        <NavLinks locale={locale} labels={{ home: dict.nav.home, feed: dict.nav.feed }} />
        <div className="ml-auto flex items-center gap-2">
          <Link
            href={`/${locale}/submit`}
            className="hidden rounded-full bg-primary px-4 py-2 text-sm font-medium text-on-primary sm:inline-flex"
          >
            {dict.nav.submit}
          </Link>
          <div className="hidden sm:block">
            <LocaleSwitcher />
          </div>
          <AccountButton />
        </div>
      </div>
    </header>
  );
}

export function SubmitFab({ locale, label }: { locale: Locale; label: string }) {
  return (
    <Link
      href={`/${locale}/submit`}
      aria-label={label}
      className="fixed bottom-5 right-5 z-30 flex size-14 items-center justify-center rounded-2xl bg-primary text-on-primary shadow-lg sm:hidden"
    >
      <svg viewBox="0 0 24 24" className="size-6" fill="none" stroke="currentColor" strokeWidth="2.2">
        <path d="M12 5v14M5 12h14" />
      </svg>
    </Link>
  );
}
