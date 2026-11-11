"use client";

import { useState } from "react";
import { Dialog } from "./dialog";
import { useI18n } from "./i18n-context";

const TERMS_URL = "https://github.com/aqtanb/qode/blob/main/TERMS_OF_SERVICE.md";
const PRIVACY_URL = "https://github.com/aqtanb/qode/blob/main/PRIVACY_POLICY.md";

export function ConsentDialog({
  open,
  onAccept,
  onDecline,
}: {
  open: boolean;
  onAccept: () => Promise<boolean>;
  onDecline: () => Promise<void>;
}) {
  const { dict } = useI18n();
  const t = dict.auth;
  const [terms, setTerms] = useState(false);
  const [privacy, setPrivacy] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(false);

  const accept = async () => {
    setBusy(true);
    setError(false);
    const ok = await onAccept();
    setBusy(false);
    setError(!ok);
  };

  return (
    <Dialog open={open} label={t.consentTitle}>
      <h2 className="font-display text-2xl font-bold">{t.consentTitle}</h2>
      <p className="mt-2 text-sm text-on-surface-variant">{t.consentSubtitle}</p>
      <div className="mt-5 space-y-3 text-sm">
        {[
          { checked: terms, set: setTerms, label: t.terms, href: TERMS_URL },
          { checked: privacy, set: setPrivacy, label: t.privacy, href: PRIVACY_URL },
        ].map((item) => (
          <label key={item.href} className="flex items-center gap-3">
            <input
              type="checkbox"
              className="size-5 accent-primary"
              checked={item.checked}
              onChange={(e) => item.set(e.target.checked)}
            />
            <span>
              {t.acceptPrefix}{" "}
              <a href={item.href} target="_blank" rel="noreferrer" className="text-primary underline">
                {item.label}
              </a>
            </span>
          </label>
        ))}
      </div>
      {error && <p className="mt-3 text-sm text-error">{t.errors.generic}</p>}
      <div className="mt-6 flex justify-end gap-2">
        <button
          type="button"
          onClick={() => void onDecline()}
          disabled={busy}
          className="rounded-full px-4 py-2.5 text-sm font-medium"
        >
          {t.decline}
        </button>
        <button
          type="button"
          onClick={accept}
          disabled={!terms || !privacy || busy}
          className="rounded-full bg-primary px-5 py-2.5 text-sm font-medium text-on-primary disabled:opacity-40"
        >
          {t.accept}
        </button>
      </div>
    </Dialog>
  );
}
