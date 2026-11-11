"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { Locale } from "@/i18n/config";

export function NavLinks({ locale, labels }: { locale: Locale; labels: { home: string; feed: string } }) {
  const pathname = usePathname();
  const items = [
    {
      href: `/${locale}`,
      label: labels.home,
      active: pathname === `/${locale}` || pathname.startsWith(`/${locale}/promocodes`),
    },
    {
      href: `/${locale}/feed`,
      label: labels.feed,
      active: pathname.startsWith(`/${locale}/feed`) || pathname.startsWith(`/${locale}/posts`),
    },
  ];
  return (
    <nav className="flex gap-1">
      {items.map((item) => (
        <Link
          key={item.href}
          href={item.href}
          className={`rounded-full px-2.5 py-1.5 text-sm font-medium sm:px-3 ${
            item.active
              ? "bg-secondary-container text-on-secondary-container"
              : "text-on-surface-variant hover:bg-surface-container-high"
          }`}
        >
          {item.label}
        </Link>
      ))}
    </nav>
  );
}
