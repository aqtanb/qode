"use client";

import { AuthApiError } from "@supabase/supabase-js";
import { useEffect, useRef, useState } from "react";
import { env } from "@/lib/env";
import { fill } from "@/lib/fill";
import { getBrowserClient } from "@/lib/supabase/browser";
import { Dialog } from "./dialog";
import { useI18n } from "./i18n-context";

const EMAIL_REGEX = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
const CODE_LENGTH = 6;

type Step = "options" | "email" | "code";
type ErrorKey = "invalidEmail" | "invalidCode" | "tooMany" | "google" | "generic";

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (options: Record<string, unknown>) => void;
          renderButton: (element: HTMLElement, options: Record<string, unknown>) => void;
        };
      };
    };
  }
}

const GIS_SRC = "https://accounts.google.com/gsi/client";

function loadGoogleScript(): Promise<void> {
  if (window.google?.accounts) return Promise.resolve();
  return new Promise((resolve, reject) => {
    const existing = document.querySelector<HTMLScriptElement>(`script[src="${GIS_SRC}"]`);
    const script = existing ?? Object.assign(document.createElement("script"), { src: GIS_SRC, async: true });
    script.addEventListener("load", () => resolve());
    script.addEventListener("error", () => reject(new Error("gis")));
    if (!existing) document.head.appendChild(script);
  });
}

async function sha256Hex(value: string): Promise<string> {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(value));
  return Array.from(new Uint8Array(digest), (b) => b.toString(16).padStart(2, "0")).join("");
}

const errorFor = (error: unknown, invalid: ErrorKey): ErrorKey => {
  if (error instanceof AuthApiError) {
    if (error.status === 429) return "tooMany";
    if ([400, 401, 403, 422].includes(error.status ?? 0)) return invalid;
  }
  return "generic";
};

export function SignInDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { dict, locale } = useI18n();
  const t = dict.auth;
  const supabase = getBrowserClient();
  const [step, setStep] = useState<Step>("options");
  const [email, setEmail] = useState("");
  const [code, setCode] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<ErrorKey | null>(null);
  const googleRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open || step !== "options" || !googleRef.current) return;
    const container = googleRef.current;
    let cancelled = false;
    const rawNonce = crypto.randomUUID();

    (async () => {
      try {
        await loadGoogleScript();
        if (cancelled || !window.google) return;
        window.google.accounts.id.initialize({
          client_id: env.googleClientId,
          nonce: await sha256Hex(rawNonce),
          use_fedcm_for_button: true,
          callback: async ({ credential }: { credential: string }) => {
            setBusy(true);
            setError(null);
            const { error: signInError } = await supabase.auth.signInWithIdToken({
              provider: "google",
              token: credential,
              nonce: rawNonce,
            });
            setBusy(false);
            if (signInError) setError("google");
          },
        });
        container.innerHTML = "";
        window.google.accounts.id.renderButton(container, {
          type: "standard",
          theme: window.matchMedia("(prefers-color-scheme: dark)").matches ? "filled_black" : "outline",
          size: "large",
          shape: "pill",
          text: "continue_with",
          width: Math.min(container.clientWidth || 320, 400),
          locale,
        });
      } catch {
        if (!cancelled) setError("google");
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [open, step, supabase, locale]);

  const sendCode = async () => {
    const clean = email.trim().toLowerCase();
    if (!EMAIL_REGEX.test(clean)) return setError("invalidEmail");
    setBusy(true);
    setError(null);
    const { error: sendError } = await supabase.auth.signInWithOtp({ email: clean });
    setBusy(false);
    if (sendError) return setError(errorFor(sendError, "invalidEmail"));
    setEmail(clean);
    setCode("");
    setStep("code");
  };

  const verify = async (value = code) => {
    if (value.length !== CODE_LENGTH) return setError("invalidCode");
    setBusy(true);
    setError(null);
    const { error: verifyError } = await supabase.auth.verifyOtp({ email, token: value, type: "email" });
    setBusy(false);
    if (verifyError) setError(errorFor(verifyError, "invalidCode"));
  };

  const input =
    "h-14 w-full rounded-xl border border-outline bg-transparent px-4 text-base outline-none focus:border-primary focus:ring-1 focus:ring-primary";
  const primary =
    "h-14 w-full rounded-xl bg-primary text-base font-medium text-on-primary transition disabled:opacity-40";
  const link = "text-sm font-medium text-primary disabled:opacity-40";

  return (
    <Dialog open={open} onClose={onClose} label={t.title}>
      <div className="flex items-start justify-between gap-4">
        <div>
          <h2 className="font-display text-2xl font-bold">{t.title}</h2>
          <p className="mt-1 text-sm text-on-surface-variant">{t.subtitle}</p>
        </div>
        <button
          type="button"
          onClick={onClose}
          aria-label={t.close}
          className="-mr-2 -mt-1 rounded-full p-2 text-on-surface-variant hover:bg-surface-container-high"
        >
          <svg viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M6 6l12 12M18 6L6 18" />
          </svg>
        </button>
      </div>

      <div className="mt-6 flex flex-col items-stretch gap-3">
        {step === "options" && (
          <>
            <div ref={googleRef} className="flex min-h-11 w-full justify-center" style={{ colorScheme: "light" }} />
            <button
              type="button"
              onClick={() => setStep("email")}
              disabled={busy}
              className="flex h-11 w-full items-center justify-center gap-2 rounded-full border border-outline text-sm font-medium hover:bg-surface-container-high"
            >
              <svg viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="1.8">
                <rect x="3" y="5" width="18" height="14" rx="2" />
                <path d="m3 7 9 6 9-6" />
              </svg>
              {t.email}
            </button>
          </>
        )}

        {step === "email" && (
          <form
            className="flex flex-col gap-3"
            onSubmit={(e) => {
              e.preventDefault();
              void sendCode();
            }}
          >
            <input
              type="email"
              autoComplete="email"
              inputMode="email"
              autoFocus
              placeholder={t.emailPlaceholder}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={busy}
              className={input}
            />
            <button type="submit" disabled={busy || !email.trim()} className={primary}>
              {t.sendCode}
            </button>
            <button type="button" onClick={() => setStep("options")} disabled={busy} className={link}>
              {t.otherOptions}
            </button>
          </form>
        )}

        {step === "code" && (
          <form
            className="flex flex-col gap-3"
            onSubmit={(e) => {
              e.preventDefault();
              void verify();
            }}
          >
            <p className="text-center text-sm text-on-surface-variant">{fill(t.codeSent, { email })}</p>
            <input
              autoComplete="one-time-code"
              inputMode="numeric"
              autoFocus
              placeholder={t.codePlaceholder}
              value={code}
              onChange={(e) => {
                const digits = e.target.value.replace(/\D/g, "").slice(0, CODE_LENGTH);
                setCode(digits);
                if (digits.length === CODE_LENGTH && !busy) void verify(digits);
              }}
              disabled={busy}
              className={`${input} text-center text-2xl tracking-[0.5em] placeholder:text-base placeholder:tracking-normal`}
            />
            <button type="submit" disabled={busy || code.length !== CODE_LENGTH} className={primary}>
              {t.signIn}
            </button>
            <div className="flex justify-between">
              <button type="button" onClick={() => setStep("email")} disabled={busy} className={link}>
                {t.changeEmail}
              </button>
              <button type="button" onClick={() => void sendCode()} disabled={busy} className={link}>
                {t.resend}
              </button>
            </div>
          </form>
        )}

        {error && (
          <p role="alert" className="text-center text-sm text-error">
            {t.errors[error]}
          </p>
        )}
      </div>
    </Dialog>
  );
}
