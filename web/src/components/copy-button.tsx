"use client";

import { useState } from "react";
import { useI18n } from "./i18n-context";

export function CopyButton({ text, large = false }: { text: string; large?: boolean }) {
  const { dict } = useI18n();
  const [copied, setCopied] = useState(false);

  const copy = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    await navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  return (
    <button
      type="button"
      onClick={copy}
      aria-label={dict.promo.copy}
      className={
        large
          ? "h-12 rounded-full bg-primary px-6 text-base font-medium text-on-primary"
          : "flex size-11 shrink-0 items-center justify-center rounded-full border border-outline-variant text-on-surface-variant hover:bg-surface-container-high"
      }
    >
      {large ? (
        copied ? (
          dict.promo.copied
        ) : (
          dict.promo.copy
        )
      ) : copied ? (
        <svg viewBox="0 0 24 24" className="size-5 text-success" fill="none" stroke="currentColor" strokeWidth="2.5">
          <path d="m5 12 5 5L20 7" />
        </svg>
      ) : (
        <svg viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="1.8">
          <rect x="9" y="9" width="11" height="11" rx="2" />
          <path d="M5 15V6a2 2 0 0 1 2-2h9" />
        </svg>
      )}
    </button>
  );
}
