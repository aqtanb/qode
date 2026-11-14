import { NextResponse, type NextRequest } from "next/server";
import { defaultLocale, hasLocale } from "@/i18n/config";
import { env } from "@/lib/env";

const isDev = process.env.NODE_ENV === "development";

function contentSecurityPolicy(nonce: string) {
  const supabase = new URL(env.supabaseUrl);
  return [
    "default-src 'self'",
    `script-src 'self' 'nonce-${nonce}' 'strict-dynamic'${isDev ? " 'unsafe-eval'" : ""}`,
    "style-src 'self' 'unsafe-inline' https://accounts.google.com/gsi/style",
    "img-src 'self' data: blob: https:",
    "font-src 'self'",
    `connect-src 'self' ${supabase.origin} wss://${supabase.host} https://accounts.google.com/gsi/`,
    "frame-src https://accounts.google.com/gsi/",
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
    "frame-ancestors 'none'",
    ...(isDev ? [] : ["upgrade-insecure-requests"]),
  ].join("; ");
}

function withSecurityHeaders(response: NextResponse, csp: string) {
  response.headers.set("Content-Security-Policy", csp);
  response.headers.set("Strict-Transport-Security", "max-age=63072000; includeSubDomains; preload");
  response.headers.set("X-Content-Type-Options", "nosniff");
  response.headers.set("X-Frame-Options", "DENY");
  response.headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
  response.headers.set("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()");
  response.headers.set("Cross-Origin-Opener-Policy", "same-origin-allow-popups");
  return response;
}

export function proxy(request: NextRequest) {
  const nonce = btoa(crypto.randomUUID());
  const csp = contentSecurityPolicy(nonce);

  if (request.nextUrl.hostname.startsWith("www.")) {
    const url = request.nextUrl.clone();
    url.hostname = url.hostname.slice(4);
    return withSecurityHeaders(NextResponse.redirect(url, 308), csp);
  }

  const { pathname } = request.nextUrl;
  const first = pathname.split("/")[1] ?? "";
  if (hasLocale(first)) {
    const headers = new Headers(request.headers);
    headers.set("x-locale", first);
    headers.set("x-nonce", nonce);
    headers.set("Content-Security-Policy", csp);
    return withSecurityHeaders(NextResponse.next({ request: { headers } }), csp);
  }

  const preferred = request.cookies.get("locale")?.value;
  const locale = preferred && hasLocale(preferred) ? preferred : defaultLocale;
  request.nextUrl.pathname = `/${locale}${pathname === "/" ? "" : pathname}`;
  return withSecurityHeaders(NextResponse.redirect(request.nextUrl), csp);
}

export const config = {
  matcher: [
    "/((?!_next|favicon.ico|icon.png|apple-icon.png|robots.txt|sitemap.xml|.*\\.(?:png|jpg|jpeg|svg|webp|ico|txt|xml)$).*)",
  ],
};
