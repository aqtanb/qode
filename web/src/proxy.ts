import { NextResponse, type NextRequest } from "next/server";
import { defaultLocale, hasLocale } from "@/i18n/config";

export function proxy(request: NextRequest) {
  if (request.nextUrl.hostname.startsWith("www.")) {
    const url = request.nextUrl.clone();
    url.hostname = url.hostname.slice(4);
    return NextResponse.redirect(url, 308);
  }

  const { pathname } = request.nextUrl;
  const first = pathname.split("/")[1] ?? "";
  if (hasLocale(first)) {
    const headers = new Headers(request.headers);
    headers.set("x-locale", first);
    return NextResponse.next({ request: { headers } });
  }

  const preferred = request.cookies.get("locale")?.value;
  const locale = preferred && hasLocale(preferred) ? preferred : defaultLocale;
  request.nextUrl.pathname = `/${locale}${pathname === "/" ? "" : pathname}`;
  return NextResponse.redirect(request.nextUrl);
}

export const config = {
  matcher: [
    "/((?!_next|favicon.ico|icon.png|apple-icon.png|robots.txt|sitemap.xml|.*\\.(?:png|jpg|jpeg|svg|webp|ico|txt|xml)$).*)",
  ],
};
