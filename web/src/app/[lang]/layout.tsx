import type { Metadata, Viewport } from "next";
import { Inter, Overpass } from "next/font/google";
import { notFound } from "next/navigation";
import { AuthProvider } from "@/components/auth-context";
import { Footer } from "@/components/footer";
import { Header, SubmitFab } from "@/components/header";
import { I18nProvider } from "@/components/i18n-context";
import { hasLocale, locales } from "@/i18n/config";
import { getDictionary } from "@/i18n/get-dictionary";
import { env } from "@/lib/env";
import "../globals.css";

const inter = Inter({ variable: "--font-inter", subsets: ["latin", "cyrillic", "cyrillic-ext"] });
const overpass = Overpass({ variable: "--font-overpass", subsets: ["latin", "cyrillic", "cyrillic-ext"] });

export function generateStaticParams() {
  return locales.map((lang) => ({ lang }));
}

export const viewport: Viewport = {
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#fff8f6" },
    { media: "(prefers-color-scheme: dark)", color: "#1a110f" },
  ],
};

export async function generateMetadata({ params }: LayoutProps<"/[lang]">): Promise<Metadata> {
  const { lang } = await params;
  if (!hasLocale(lang)) return {};
  const dict = await getDictionary(lang);
  return {
    metadataBase: new URL(env.siteUrl),
    title: { default: dict.meta.title, template: "%s · Qode" },
    description: dict.meta.description,
    alternates: { languages: Object.fromEntries(locales.map((l) => [l, `/${l}`])) },
    openGraph: { siteName: "Qode", type: "website", locale: lang },
  };
}

export default async function RootLayout({ children, params }: LayoutProps<"/[lang]">) {
  const { lang } = await params;
  if (!hasLocale(lang)) notFound();
  const dict = await getDictionary(lang);

  return (
    <html lang={lang} className={`${inter.variable} ${overpass.variable} antialiased`}>
      <body className="flex min-h-dvh flex-col font-sans">
        <I18nProvider locale={lang} dict={dict}>
          <AuthProvider>
            <Header locale={lang} dict={dict} />
            <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-6">{children}</main>
            <Footer dict={dict} />
            <SubmitFab locale={lang} label={dict.nav.submit} />
          </AuthProvider>
        </I18nProvider>
      </body>
    </html>
  );
}
