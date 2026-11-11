import Link from "next/link";
import { headers } from "next/headers";
import { defaultLocale, hasLocale } from "@/i18n/config";
import { getDictionary } from "@/i18n/get-dictionary";

export default async function NotFound() {
  const segment = (await headers()).get("x-locale") ?? "";
  const locale = hasLocale(segment) ? segment : defaultLocale;
  const dict = await getDictionary(locale);
  return (
    <div className="flex flex-col items-center gap-4 py-20 text-center">
      <p className="font-display text-6xl font-extrabold text-primary">404</p>
      <h1 className="font-display text-2xl font-bold">{dict.notFound.title}</h1>
      <Link href={`/${locale}`} className="rounded-full bg-primary px-5 py-2.5 text-sm font-medium text-on-primary">
        {dict.notFound.back}
      </Link>
    </div>
  );
}
