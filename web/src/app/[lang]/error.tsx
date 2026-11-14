"use client";

import Link from "next/link";
import { useEffect } from "react";
import { useI18n } from "@/components/i18n-context";

export default function Error({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  const { dict, locale } = useI18n();

  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="flex flex-col items-center gap-4 py-20 text-center">
      <h1 className="font-display text-2xl font-bold">{dict.error.title}</h1>
      <div className="flex gap-3">
        <button
          type="button"
          onClick={reset}
          className="rounded-full bg-primary px-5 py-2.5 text-sm font-medium text-on-primary"
        >
          {dict.error.retry}
        </button>
        <Link href={`/${locale}`} className="rounded-full border border-outline px-5 py-2.5 text-sm font-medium">
          {dict.notFound.back}
        </Link>
      </div>
    </div>
  );
}
