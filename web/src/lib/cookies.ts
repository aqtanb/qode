export function setPreferenceCookie(name: "locale" | "theme", value: string) {
  document.cookie = `${name}=${value}; path=/; max-age=31536000; samesite=lax`;
}
