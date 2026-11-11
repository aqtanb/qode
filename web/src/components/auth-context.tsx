"use client";

import type { Session } from "@supabase/supabase-js";
import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { getBrowserClient } from "@/lib/supabase/browser";
import type { ItemType, VoteState } from "@/lib/types";
import { ConsentDialog } from "./consent-dialog";
import { SignInDialog } from "./sign-in-dialog";

interface AuthValue {
  session: Session | null;
  ready: boolean;
  openSignIn: () => void;
  signOut: () => Promise<void>;
  votes: Record<string, VoteState>;
  savedVotes: Record<string, VoteState>;
  vote: (type: ItemType, id: string, state: VoteState) => Promise<boolean>;
}

const AuthContext = createContext<AuthValue | null>(null);
const voteKey = (type: ItemType, id: string) => `${type}:${id}`;

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const supabase = getBrowserClient();
  const [session, setSession] = useState<Session | null>(null);
  const [ready, setReady] = useState(false);
  const [signInOpen, setSignInOpen] = useState(false);
  const [needsConsent, setNeedsConsent] = useState(false);
  const [votes, setVotes] = useState<Record<string, VoteState>>({});
  const [savedVotes, setSavedVotes] = useState<Record<string, VoteState>>({});

  const loadAccount = useCallback(
    async (current: Session | null) => {
      if (!current) {
        setNeedsConsent(false);
        setVotes({});
        setSavedVotes({});
        return;
      }
      const userId = current.user.id;
      const { data: profile } = await supabase.from("users").select("id").eq("id", userId).maybeSingle();
      if (!profile) {
        setNeedsConsent(true);
        return;
      }
      setNeedsConsent(false);
      const { data } = await supabase
        .from("interactions")
        .select("item_type, item_id, vote_state")
        .eq("user_id", userId);
      const loaded = Object.fromEntries(
        (data ?? []).map((row: { item_type: ItemType; item_id: string; vote_state: VoteState }) => [
          voteKey(row.item_type, row.item_id),
          row.vote_state,
        ]),
      );
      setVotes(loaded);
      setSavedVotes(loaded);
    },
    [supabase],
  );

  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => {
      setSession(data.session);
      setReady(true);
      void loadAccount(data.session);
    });
    const { data } = supabase.auth.onAuthStateChange((event, next) => {
      setSession(next);
      if (event === "SIGNED_IN" || event === "SIGNED_OUT") void loadAccount(next);
    });
    return () => data.subscription.unsubscribe();
  }, [supabase, loadAccount]);

  const signOut = useCallback(async () => {
    await supabase.auth.signOut();
  }, [supabase]);

  const acceptConsent = useCallback(async () => {
    if (!session) return false;
    const meta = session.user.user_metadata ?? {};
    const { error } = await supabase.from("users").insert({
      id: session.user.id,
      display_name: meta.full_name ?? meta.name ?? null,
      photo_url: meta.avatar_url ?? meta.picture ?? null,
      legal_policies_accepted_at: new Date().toISOString(),
    });
    if (error) return false;
    await loadAccount(session);
    return true;
  }, [session, supabase, loadAccount]);

  const vote = useCallback(
    async (type: ItemType, id: string, state: VoteState) => {
      if (!session || needsConsent) {
        if (!session) setSignInOpen(true);
        return false;
      }
      const key = voteKey(type, id);
      const previous = votes[key] ?? "NONE";
      setVotes((v) => ({ ...v, [key]: state }));
      const { error } = await supabase.rpc("save_interaction", {
        target_type: type,
        target_id: id,
        new_vote_state: state,
        is_bookmarked: false,
      });
      if (error) {
        setVotes((v) => ({ ...v, [key]: previous }));
        return false;
      }
      return true;
    },
    [session, needsConsent, votes, supabase],
  );

  return (
    <AuthContext.Provider
      value={{ session, ready, openSignIn: () => setSignInOpen(true), signOut, votes, savedVotes, vote }}
    >
      {children}
      {signInOpen && !session && <SignInDialog open onClose={() => setSignInOpen(false)} />}
      <ConsentDialog open={!!session && needsConsent} onAccept={acceptConsent} onDecline={signOut} />
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth outside AuthProvider");
  return value;
}

export const voteStateFor = (votes: Record<string, VoteState>, type: ItemType, id: string): VoteState =>
  votes[voteKey(type, id)] ?? "NONE";
