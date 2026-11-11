import type { Locale } from "@/i18n/config";
import { localized } from "@/lib/format";
import type { Banner } from "@/lib/types";

export function Banners({ banners, locale }: { banners: Banner[]; locale: Locale }) {
  if (banners.length === 0) return null;
  return (
    <div className="no-scrollbar -mx-4 flex snap-x snap-mandatory gap-3 overflow-x-auto px-4">
      {banners.map((banner) => {
        const title = localized(banner.cta_title, locale);
        const description = localized(banner.cta_description, locale);
        const body = (
          <div className="relative aspect-[16/7] w-[85vw] max-w-2xl shrink-0 snap-center overflow-hidden rounded-3xl bg-surface-container sm:w-[36rem]">
            <img src={banner.image_url} alt="" className="absolute inset-0 size-full object-cover" />
            {(title || description) && (
              <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/70 to-transparent p-5 text-white">
                {title && <p className="font-display text-xl font-bold">{title}</p>}
                {description && <p className="text-sm opacity-90">{description}</p>}
              </div>
            )}
          </div>
        );
        return banner.cta_url ? (
          <a key={banner.id} href={banner.cta_url} target="_blank" rel="noreferrer">
            {body}
          </a>
        ) : (
          <div key={banner.id}>{body}</div>
        );
      })}
    </div>
  );
}
