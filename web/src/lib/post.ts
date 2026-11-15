// Mirrors Post.kt; change both when a rule changes.
export const TITLE_MAX_LENGTH = 50;
export const CONTENT_MAX_LENGTH = 1000;
export const CONTENT_MAX_LINES = 10;
export const MAX_TAGS = 5;
export const TAG_MAX_LENGTH = 15;
export const MAX_IMAGES = 5;

export const filterTitle = (input: string) => input.slice(0, TITLE_MAX_LENGTH);

export function filterContent(input: string): string {
  const collapsed = input.replace(/\n{2,}/g, "\n");
  const lines = collapsed.split("\n");
  const limited = lines.length > CONTENT_MAX_LINES ? lines.slice(0, CONTENT_MAX_LINES).join("\n") : collapsed;
  return limited.slice(0, CONTENT_MAX_LENGTH);
}

export const filterTagInput = (input: string) =>
  input
    .toLowerCase()
    .replace(/[^\p{L}\p{N}_]/gu, "")
    .slice(0, TAG_MAX_LENGTH);

export const newPostId = () => crypto.randomUUID().replaceAll("-", "");
