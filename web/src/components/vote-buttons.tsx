"use client";

import type { ItemType, VoteState } from "@/lib/types";
import { useAuth, voteStateFor } from "./auth-context";
import { useI18n } from "./i18n-context";

const delta = (state: VoteState) => (state === "UPVOTE" ? 1 : state === "DOWNVOTE" ? -1 : 0);

export function VoteButtons({
  type,
  id,
  score,
  vertical = false,
}: {
  type: ItemType;
  id: string;
  score: number;
  vertical?: boolean;
}) {
  const { votes, savedVotes, vote, session } = useAuth();
  const { dict } = useI18n();
  const current = session ? voteStateFor(votes, type, id) : "NONE";
  // The server score already includes the saved vote, so adjust from that baseline.
  const shown = score - delta(voteStateFor(savedVotes, type, id)) + delta(current);

  const press = (target: VoteState) => (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    void vote(type, id, current === target ? "NONE" : target);
  };

  const arrow = (up: boolean, active: boolean) => (
    <svg
      viewBox="0 0 24 24"
      className="size-5"
      fill={active ? "currentColor" : "none"}
      stroke="currentColor"
      strokeWidth="2"
      strokeLinejoin="round"
    >
      <path d={up ? "M12 4 4 14h5v6h6v-6h5z" : "M12 20 4 10h5V4h6v6h5z"} />
    </svg>
  );

  return (
    <div
      className={`flex items-center gap-1 rounded-full bg-surface-container-high px-1 ${vertical ? "flex-col py-1" : ""}`}
    >
      <button
        type="button"
        onClick={press("UPVOTE")}
        aria-label={session ? dict.vote.up : dict.vote.signInToVote}
        aria-pressed={current === "UPVOTE"}
        className={`rounded-full p-1.5 ${current === "UPVOTE" ? "text-primary" : "text-on-surface-variant"} hover:bg-surface-container`}
      >
        {arrow(true, current === "UPVOTE")}
      </button>
      <span className="min-w-6 text-center text-sm font-semibold tabular-nums">{shown}</span>
      <button
        type="button"
        onClick={press("DOWNVOTE")}
        aria-label={session ? dict.vote.down : dict.vote.signInToVote}
        aria-pressed={current === "DOWNVOTE"}
        className={`rounded-full p-1.5 ${current === "DOWNVOTE" ? "text-error" : "text-on-surface-variant"} hover:bg-surface-container`}
      >
        {arrow(false, current === "DOWNVOTE")}
      </button>
    </div>
  );
}
