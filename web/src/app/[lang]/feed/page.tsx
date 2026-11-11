import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { PostCard } from "@/components/post-card";
import { hasLocale } from "@/i18n/config";
import { getDictionary } from "@/i18n/get-dictionary";
import { getPosts } from "@/lib/queries";

export async function generateMetadata({ params }: PageProps<"/[lang]/feed">): Promise<Metadata> {
  const { lang } = await params;
  if (!hasLocale(lang)) return {};
  return { title: (await getDictionary(lang)).feed.title };
}

export default async function FeedPage({ params }: PageProps<"/[lang]/feed">) {
  const { lang } = await params;
  if (!hasLocale(lang)) notFound();
  const [dict, posts] = await Promise.all([getDictionary(lang), getPosts()]);

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-4">
      <h1 className="font-display text-3xl font-bold">{dict.feed.title}</h1>
      {posts.length === 0 ? (
        <p className="py-12 text-center text-on-surface-variant">{dict.feed.empty}</p>
      ) : (
        posts.map((post) => <PostCard key={post.id} post={post} locale={lang} dict={dict} />)
      )}
    </div>
  );
}
