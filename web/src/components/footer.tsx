import Link from "next/link";
import type { Locale } from "@/i18n/config";
import type { Dictionary } from "@/i18n/get-dictionary";

export const PLAY_URL = "https://play.google.com/store/apps/details?id=com.qodein.qode";

export function Footer({ dict, locale }: { dict: Dictionary; locale: Locale }) {
  return (
    <footer className="border-t border-outline-variant/60 py-6 text-sm text-on-surface-variant">
      <div className="mx-auto flex max-w-5xl flex-wrap items-center gap-x-6 gap-y-2 px-4">
        <span>© Qode</span>
        <Link href={`/${locale}/terms`} className="hover:text-on-surface">
          {dict.footer.terms}
        </Link>
        <Link href={`/${locale}/privacy`} className="hover:text-on-surface">
          {dict.footer.privacy}
        </Link>
        <a href={PLAY_URL} target="_blank" rel="noreferrer" className="hover:text-on-surface">
          {dict.footer.android}
        </a>
      </div>
    </footer>
  );
}
