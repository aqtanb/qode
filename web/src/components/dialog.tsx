"use client";

import { useEffect, useRef } from "react";

export function Dialog({
  open,
  onClose,
  label,
  children,
}: {
  open: boolean;
  onClose?: () => void;
  label: string;
  children: React.ReactNode;
}) {
  const ref = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const dialog = ref.current;
    if (!dialog) return;
    if (open && !dialog.open) dialog.showModal();
    if (!open && dialog.open) dialog.close();
  }, [open]);

  return (
    <dialog
      ref={ref}
      aria-label={label}
      onCancel={(e) => {
        e.preventDefault();
        onClose?.();
      }}
      onClick={(e) => {
        if (e.target === ref.current) onClose?.();
      }}
      className="m-auto w-[min(26rem,calc(100vw-2rem))] rounded-3xl bg-surface-container-low p-0 text-on-surface shadow-2xl backdrop:bg-black/50"
    >
      <div className="p-6">{children}</div>
    </dialog>
  );
}
