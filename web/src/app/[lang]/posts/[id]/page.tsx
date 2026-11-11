import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { cache } from "react";
import { Avatar } from "@/components/avatar";
import { ShareButton } from "@/components/share-button";
import { VoteButtons } from "@/components/vote-buttons";
import { hasLocale } from "@/i18n/config";
import { getDictionary } from "@/i18n/get-dictionary";
import { formatDate } from "@/lib/format";
import { getPost } from "@/lib/queries";

const load = cache((id: string) => getPost(decodeURIComponent(id)));

export async function generateMetadata({ params }: PageProps<"/[lang]/posts/[id]">): Promise<Metadata> {
  const { lang, id } = await params;
  if (!hasLocale(lang)) return {};
  const [dict, post] = await Promise.all([getDictionary(lang), load(id)]);
  if (!post) return { title: dict.feed.notFound };
  const description = post.content.slice(0, 160);
  return {
    title: post.title,
    description,
    alternates: { canonical: `/${lang}/posts/${id}` },
    openGraph: { title: post.title, description, type: "article", images: post.image_urls.slice(0, 1) },
    twitter: { card: post.image_urls[0] ? "summary_large_image" : "summary", title: post.title, description },
  };
}

export default async function PostPage({ params }: PageProps<"/[lang]/posts/[id]">) {
  const { lang, id } = await params;
  if (!hasLocale(lang)) notFound();
  const [dict, post] = await Promise.all([getDictionary(lang), load(id)]);
  if (!post) notFound();
  const author = post.author_name ?? dict.promo.anonymous;

  return (
    <article className="mx-auto flex max-w-2xl flex-col gap-5">
      <div className="flex items-center gap-3 text-sm text-on-surface-variant">
        <Avatar name={author} url={post.author_avatar_url} size={36} />
        <span className="font-medium text-on-surface">{author}</span>
        <span>·</span>
        <time dateTime={post.created_at}>{formatDate(post.created_at, lang)}</time>
      </div>
      <h1 className="font-display text-3xl font-bold">{post.title}</h1>
      {post.content && <p className="whitespace-pre-line text-lg leading-relaxed">{post.content}</p>}
      {post.image_urls.map((url) => (
        <img key={url} src={url} alt="" className="w-full rounded-3xl object-cover" />
      ))}
      {post.tags.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {post.tags.map((tag) => (
            <span
              key={tag}
              className="rounded-full bg-secondary-container px-3 py-1 text-sm text-on-secondary-container"
            >
              #{tag}
            </span>
          ))}
        </div>
      )}
      <div className="flex items-center gap-3">
        <VoteButtons type="POST" id={post.id} score={post.vote_score} />
        <ShareButton title={post.title} />
      </div>
    </article>
  );
}
