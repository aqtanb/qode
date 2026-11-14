const sizedLogoUrl = (url: string, size: number) =>
  url.startsWith("https://img.logo.dev/") ? url.replace(/([?&]size=)\d+/, `$1${Math.min(size * 2, 128)}`) : url;

export function ServiceLogo({ name, url, size = 32 }: { name: string; url: string | null; size?: number }) {
  return url ? (
    <img
      src={sizedLogoUrl(url, size)}
      alt=""
      width={size}
      height={size}
      loading="lazy"
      decoding="async"
      className="shrink-0 rounded-full bg-surface-container-high object-cover"
      style={{ width: size, height: size }}
    />
  ) : (
    <span
      className="flex shrink-0 items-center justify-center rounded-full bg-primary-container text-sm font-bold text-on-primary-container"
      style={{ width: size, height: size }}
    >
      {name.charAt(0).toUpperCase()}
    </span>
  );
}
