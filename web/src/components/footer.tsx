import type { Dictionary } from "@/i18n/get-dictionary";

export const PLAY_URL = "https://play.google.com/store/apps/details?id=com.qodein.qode";

export function Footer({ dict }: { dict: Dictionary }) {
  return (
    <footer className="border-t border-outline-variant/60 py-6 text-sm text-on-surface-variant">
      <div className="mx-auto flex max-w-5xl flex-wrap items-center gap-x-6 gap-y-2 px-4">
        <span>© Qode</span>
        <a
          href="https://github.com/aqtanb/qode/blob/main/TERMS_OF_SERVICE.md"
          target="_blank"
          rel="noreferrer"
          className="hover:text-on-surface"
        >
          {dict.footer.terms}
        </a>
        <a
          href="https://github.com/aqtanb/qode/blob/main/PRIVACY_POLICY.md"
          target="_blank"
          rel="noreferrer"
          className="hover:text-on-surface"
        >
          {dict.footer.privacy}
        </a>
        <a href={PLAY_URL} target="_blank" rel="noreferrer" className="hover:text-on-surface">
          {dict.footer.android}
        </a>
      </div>
    </footer>
  );
}
