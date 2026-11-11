"use client";

import { useState } from "react";
import { useI18n } from "./i18n-context";

export function ShareButton({ title }: { title: string }) {
  const { dict } = useI18n();
  const [copied, setCopied] = useState(false);

  const share = async () => {
    const url = window.location.href;
    if (navigator.share) {
      try {
        await navigator.share({ title, url });
      } catch {
        // dismissed
      }
      return;
    }
    await navigator.clipboard.writeText(url);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  return (
    <button
      type="button"
      onClick={share}
      className="flex h-10 items-center gap-2 rounded-full border border-outline-variant px-4 text-sm font-medium hover:bg-surface-container-high"
    >
      <svg viewBox="0 0 24 24" className="size-4" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M4 12v7a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-7M16 6l-4-4-4 4M12 2v14" />
      </svg>
      {copied ? dict.share.copied : dict.share.share}
    </button>
  );
}
