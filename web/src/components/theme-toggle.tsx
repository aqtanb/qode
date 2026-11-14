"use client";

import type { Theme } from "@/i18n/config";
import { setPreferenceCookie } from "@/lib/cookies";
import { useI18n } from "./i18n-context";

export function ThemeToggle() {
  const { dict } = useI18n();

  const toggle = () => {
    const root = document.documentElement;
    const current =
      (root.dataset.theme as Theme | undefined) ??
      (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
    const next: Theme = current === "dark" ? "light" : "dark";
    root.dataset.theme = next;
    setPreferenceCookie("theme", next);
  };

  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={dict.nav.theme}
      className="flex size-9 items-center justify-center rounded-full text-on-surface-variant hover:bg-surface-container-high"
    >
      <svg viewBox="0 0 24 24" className="theme-icon-moon size-5" fill="none" stroke="currentColor" strokeWidth="1.8">
        <path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z" />
      </svg>
      <svg viewBox="0 0 24 24" className="theme-icon-sun size-5" fill="none" stroke="currentColor" strokeWidth="1.8">
        <circle cx="12" cy="12" r="4" />
        <path d="M12 2.5v2M12 19.5v2M2.5 12h2M19.5 12h2M5.3 5.3l1.4 1.4M17.3 17.3l1.4 1.4M5.3 18.7l1.4-1.4M17.3 6.7l1.4-1.4" />
      </svg>
    </button>
  );
}
