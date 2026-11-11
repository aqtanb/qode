import Link from "next/link";
import type { Locale } from "@/i18n/config";
import type { Dictionary } from "@/i18n/get-dictionary";
import { formatDate } from "@/lib/format";
import type { Post } from "@/lib/types";
import { Avatar } from "./avatar";
import { VoteButtons } from "./vote-buttons";

export function PostCard({ post, locale, dict }: { post: Post; locale: Locale; dict: Dictionary }) {
  return (
    <article className="relative flex flex-col gap-3 rounded-3xl bg-surface-container p-5 transition hover:bg-surface-container-high">
      <div className="flex items-center gap-3 text-sm text-on-surface-variant">
        <Avatar name={post.author_name ?? dict.promo.anonymous} url={post.author_avatar_url} />
        <span className="font-medium text-on-surface">{post.author_name ?? dict.promo.anonymous}</span>
        <span>·</span>
        <span>{formatDate(post.created_at, locale)}</span>
      </div>
      <h3 className="font-display text-xl font-semibold">
        <Link href={`/${locale}/posts/${encodeURIComponent(post.id)}`} className="after:absolute after:inset-0">
          {post.title}
        </Link>
      </h3>
      {post.content && <p className="line-clamp-3 whitespace-pre-line text-on-surface-variant">{post.content}</p>}
      {post.image_urls[0] && (
        <img src={post.image_urls[0]} alt="" className="max-h-80 w-full rounded-2xl object-cover" loading="lazy" />
      )}
      {post.tags.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {post.tags.map((tag) => (
            <span
              key={tag}
              className="rounded-full bg-secondary-container px-3 py-1 text-xs text-on-secondary-container"
            >
              #{tag}
            </span>
          ))}
        </div>
      )}
      <div className="relative z-10 self-start">
        <VoteButtons type="POST" id={post.id} score={post.vote_score} />
      </div>
    </article>
  );
}
