import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { SubmitForm } from "@/components/submit-form";
import { hasLocale } from "@/i18n/config";
import { getDictionary } from "@/i18n/get-dictionary";

export async function generateMetadata({ params }: PageProps<"/[lang]/submit">): Promise<Metadata> {
  const { lang } = await params;
  if (!hasLocale(lang)) return {};
  return { title: (await getDictionary(lang)).submit.title, robots: { index: false } };
}

export default async function SubmitPage({ params }: PageProps<"/[lang]/submit">) {
  const { lang } = await params;
  if (!hasLocale(lang)) notFound();
  const dict = await getDictionary(lang);
  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6">
      <h1 className="font-display text-3xl font-bold">{dict.submit.title}</h1>
      <SubmitForm />
    </div>
  );
}
