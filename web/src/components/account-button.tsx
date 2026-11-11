"use client";

import { useState } from "react";
import { useAuth } from "./auth-context";
import { useI18n } from "./i18n-context";

export function AccountButton() {
  const { session, ready, openSignIn, signOut } = useAuth();
  const { dict } = useI18n();
  const [menu, setMenu] = useState(false);

  if (!ready) return <div className="size-9" />;
  if (!session) {
    return (
      <button
        type="button"
        onClick={openSignIn}
        className="h-9 whitespace-nowrap rounded-full border border-outline px-3 text-sm font-medium hover:bg-surface-container-high sm:px-4"
      >
        {dict.nav.signIn}
      </button>
    );
  }

  const meta = session.user.user_metadata ?? {};
  const avatar: string | undefined = meta.avatar_url ?? meta.picture;
  const initial = (meta.full_name ?? meta.name ?? session.user.email ?? "?").charAt(0).toUpperCase();

  return (
    <div className="relative">
      <button
        type="button"
        onClick={() => setMenu((m) => !m)}
        aria-label={session.user.email ?? "account"}
        className="flex size-9 items-center justify-center overflow-hidden rounded-full bg-primary-container font-medium text-on-primary-container"
      >
        {avatar ? <img src={avatar} alt="" className="size-full object-cover" referrerPolicy="no-referrer" /> : initial}
      </button>
      {menu && (
        <div className="absolute right-0 mt-2 w-56 rounded-2xl bg-surface-container-high p-2 shadow-lg">
          <p className="truncate px-3 py-2 text-sm text-on-surface-variant">{session.user.email}</p>
          <button
            type="button"
            onClick={() => {
              setMenu(false);
              void signOut();
            }}
            className="w-full rounded-xl px-3 py-2 text-left text-sm hover:bg-surface-container"
          >
            {dict.nav.signOut}
          </button>
        </div>
      )}
    </div>
  );
}
