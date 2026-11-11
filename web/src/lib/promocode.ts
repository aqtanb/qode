import type { DiscountType } from "@/lib/types";

// Mirrors shared/src/commonMain/kotlin/com/qodein/shared/model/Promocode.kt and Service.kt.
export const CODE_MAX_LENGTH = 50;
export const DESCRIPTION_MAX_LENGTH = 1000;
export const FREE_ITEM_MAX_LENGTH = 50;
export const SERVICE_NAME_MAX_LENGTH = 50;
const MAX_ID_LENGTH = 200;
const SERVICE_NAME_REGEX = /^[\p{L}\p{N} &'\-.,()]+$/u;
const ROOT_DOMAIN_REGEX = /^[a-z0-9-]+\.[a-z]{2,}$/;
const LOGO_DEV_KEY = "pk_FgWiGDdIQ1KJ7DIWJIHqeA";

export const promocodeId = (serviceName: string, code: string) =>
  `${serviceName.toLowerCase().trim().replace(/\s+/g, "_")}_${code.toLowerCase().trim().replace(/\s+/g, "_")}`;

export function sanitizeUrl(raw: string): string {
  const trimmed = raw.trim().toLowerCase();
  if (!trimmed) return "";
  const host = trimmed
    .replace(/^https?:\/\//, "")
    .replace(/^www\./, "")
    .split(/[/?#]/)[0]
    .replace(/[^a-z0-9.-]/g, "");
  const segments = host.split(".").filter(Boolean);
  return segments.length >= 2 ? `${segments[segments.length - 2]}.${segments[segments.length - 1]}` : host;
}

export const isValidSiteUrl = (raw: string) => ROOT_DOMAIN_REGEX.test(sanitizeUrl(raw));

export const isValidServiceName = (name: string) => {
  const clean = name.trim();
  return clean.length >= 1 && clean.length <= SERVICE_NAME_MAX_LENGTH && SERVICE_NAME_REGEX.test(clean);
};

export const logoUrlFor = (siteUrl: string) =>
  `https://img.logo.dev/${siteUrl}?token=${LOGO_DEV_KEY}&size=128&format=webp&fallback=monogram`;

export const newServiceId = () => crypto.randomUUID().replaceAll("-", "");

export interface PromocodeForm {
  code: string;
  discountType: DiscountType;
  discountValue: string;
  freeItem: string;
  minOrder: string;
  startDate: string;
  endDate: string;
  description: string;
}

export type PromocodeFormError = "code" | "percentage" | "amount" | "freeItem" | "minOrder" | "dates" | "description";

export function validatePromocode(form: PromocodeForm, serviceName: string): PromocodeFormError | null {
  const code = form.code.trim();
  if (!code || code.length > CODE_MAX_LENGTH) return "code";
  const minOrder = form.minOrder.trim() === "" ? 0 : Number(form.minOrder);
  if (!Number.isFinite(minOrder) || minOrder < 0) return "minOrder";
  const value = Number(form.discountValue);
  if (form.discountType === "Percentage" && !(value >= 1 && value <= 100)) return "percentage";
  if (form.discountType === "FixedAmount" && !(value > 0 && value <= minOrder)) return "amount";
  if (form.discountType === "FreeItem") {
    const item = form.freeItem.trim();
    if (!item || item.length > FREE_ITEM_MAX_LENGTH) return "freeItem";
  }
  if (!form.startDate || !form.endDate || new Date(form.endDate) <= new Date(form.startDate)) return "dates";
  if (form.description.trim().length > DESCRIPTION_MAX_LENGTH) return "description";
  if (promocodeId(serviceName, code).length > MAX_ID_LENGTH) return "code";
  return null;
}
