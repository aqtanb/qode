"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { compressImage } from "@/lib/image";
import {
  CONTENT_MAX_LENGTH,
  filterContent,
  filterTagInput,
  filterTitle,
  MAX_IMAGES,
  MAX_TAGS,
  newPostId,
  TITLE_MAX_LENGTH,
} from "@/lib/post";
import { getBrowserClient } from "@/lib/supabase/browser";
import { useAuth } from "./auth-context";
import { useI18n } from "./i18n-context";

type ErrorKey = keyof ReturnType<typeof useI18n>["dict"]["post"]["errors"];
type Photo = { file: File; preview: string };

const BUCKET = "post-images";

export function PostForm() {
  const { dict, locale } = useI18n();
  const t = dict.post;
  const { session, ready, openSignIn } = useAuth();
  const supabase = getBrowserClient();
  const router = useRouter();
  const fileInput = useRef<HTMLInputElement>(null);

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [tags, setTags] = useState<string[]>([]);
  const [tagInput, setTagInput] = useState("");
  const [photos, setPhotos] = useState<Photo[]>([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<ErrorKey | null>(null);

  const previews = useRef<string[]>([]);
  useEffect(() => {
    previews.current = photos.map((p) => p.preview);
  }, [photos]);
  useEffect(() => () => previews.current.forEach((url) => URL.revokeObjectURL(url)), []);

  if (!ready) return null;
  if (!session) {
    return (
      <div className="flex flex-col items-start gap-4 rounded-3xl bg-surface-container p-6">
        <p>{t.signInNeeded}</p>
        <button
          type="button"
          onClick={openSignIn}
          className="rounded-full bg-primary px-5 py-2.5 text-sm font-medium text-on-primary"
        >
          {dict.nav.signIn}
        </button>
      </div>
    );
  }

  const addTag = () => {
    const tag = filterTagInput(tagInput);
    if (tag && !tags.includes(tag) && tags.length < MAX_TAGS) setTags([...tags, tag]);
    setTagInput("");
  };

  const addPhotos = (files: FileList | null) => {
    if (!files) return;
    const images = [...files].filter((f) => f.type.startsWith("image/"));
    if (images.length < files.length) setError("photo");
    const room = MAX_IMAGES - photos.length;
    setPhotos([...photos, ...images.slice(0, room).map((file) => ({ file, preview: URL.createObjectURL(file) }))]);
  };

  const removePhoto = (index: number) => {
    URL.revokeObjectURL(photos[index].preview);
    setPhotos(photos.filter((_, i) => i !== index));
  };

  const uploadPhoto = async (photo: Photo): Promise<string> => {
    const blob = await compressImage(photo.file);
    const path = `${session.user.id}/${crypto.randomUUID()}`;
    const { error: uploadError } = await supabase.storage
      .from(BUCKET)
      .upload(path, blob, { contentType: "image/webp" });
    if (uploadError) throw uploadError;
    return supabase.storage.from(BUCKET).getPublicUrl(path).data.publicUrl;
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    const cleanTitle = filterTitle(title.trim());
    if (!cleanTitle) return setError("title");

    setBusy(true);
    let imageUrls: string[];
    try {
      imageUrls = await Promise.all(photos.map(uploadPhoto));
    } catch {
      setBusy(false);
      return setError("upload");
    }

    const id = newPostId();
    const { error: insertError } = await supabase.from("posts").insert({
      id,
      author_id: session.user.id,
      title: cleanTitle,
      content: filterContent(content.trim()),
      image_urls: imageUrls,
      tags: tagInput.trim()
        ? [...new Set([...tags, filterTagInput(tagInput)])].filter(Boolean).slice(0, MAX_TAGS)
        : tags,
    });
    setBusy(false);
    if (insertError) return setError("generic");
    router.push(`/${locale}/posts/${id}`);
  };

  const input =
    "w-full rounded-xl border border-outline bg-transparent px-4 outline-none focus:border-primary focus:ring-1 focus:ring-primary";
  const label = "flex flex-col gap-1.5 text-sm font-medium";

  return (
    <form onSubmit={submit} className="flex flex-col gap-5">
      <label className={label}>
        <span>{t.titleLabel}</span>
        <input
          value={title}
          onChange={(e) => setTitle(filterTitle(e.target.value))}
          placeholder={t.titlePlaceholder}
          maxLength={TITLE_MAX_LENGTH}
          className={`${input} h-12`}
        />
        <span className="self-end text-xs font-normal text-on-surface-variant">
          {title.length}/{TITLE_MAX_LENGTH}
        </span>
      </label>

      <label className={label}>
        <span>{t.content}</span>
        <textarea
          value={content}
          onChange={(e) => setContent(filterContent(e.target.value))}
          placeholder={t.contentPlaceholder}
          rows={5}
          className={`${input} resize-none py-3`}
        />
        <span className="self-end text-xs font-normal text-on-surface-variant">
          {content.length}/{CONTENT_MAX_LENGTH}
        </span>
      </label>

      <div className={label}>
        <span>{t.tags}</span>
        <div className="flex flex-wrap gap-2">
          {tags.map((tag) => (
            <button
              key={tag}
              type="button"
              onClick={() => setTags(tags.filter((x) => x !== tag))}
              className="flex h-8 items-center gap-1 rounded-full bg-secondary-container px-3 text-sm text-on-secondary-container"
            >
              #{tag} <span aria-hidden>×</span>
            </button>
          ))}
        </div>
        {tags.length < MAX_TAGS && (
          <input
            value={tagInput}
            onChange={(e) => setTagInput(filterTagInput(e.target.value))}
            onKeyDown={(e) => {
              if (e.key === "Enter" || e.key === "," || e.key === " ") {
                e.preventDefault();
                addTag();
              }
            }}
            onBlur={addTag}
            placeholder={t.tagsPlaceholder}
            className={`${input} h-12`}
          />
        )}
      </div>

      <div className={label}>
        <span>{t.photos}</span>
        <div className="grid grid-cols-3 gap-2 sm:grid-cols-5">
          {photos.map((photo, i) => (
            <div key={photo.preview} className="relative aspect-square overflow-hidden rounded-xl bg-surface-container">
              <img src={photo.preview} alt="" className="size-full object-cover" />
              <button
                type="button"
                onClick={() => removePhoto(i)}
                aria-label={t.removePhoto}
                className="absolute right-1 top-1 flex size-7 items-center justify-center rounded-full bg-black/60 text-white"
              >
                ×
              </button>
            </div>
          ))}
          {photos.length < MAX_IMAGES && (
            <button
              type="button"
              onClick={() => fileInput.current?.click()}
              aria-label={t.addPhoto}
              className="flex aspect-square items-center justify-center rounded-xl border-2 border-dashed border-outline-variant text-on-surface-variant hover:bg-surface-container"
            >
              <svg viewBox="0 0 24 24" className="size-7" fill="none" stroke="currentColor" strokeWidth="1.8">
                <path d="M12 5v14M5 12h14" />
              </svg>
            </button>
          )}
        </div>
        <input
          ref={fileInput}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          multiple
          hidden
          onChange={(e) => {
            addPhotos(e.target.files);
            e.target.value = "";
          }}
        />
      </div>

      {error && <p className="text-sm text-error">{t.errors[error]}</p>}

      <button
        type="submit"
        disabled={busy}
        className="h-12 rounded-full bg-primary px-6 font-medium text-on-primary disabled:opacity-60"
      >
        {busy ? t.publishing : t.publish}
      </button>
    </form>
  );
}
