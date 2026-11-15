import Markdown from "react-markdown";

// Renders the repo's policy markdown, the same files the Android app shows.
export function LegalDocument({ markdown }: { markdown: string }) {
  return (
    <article className="mx-auto flex max-w-3xl flex-col gap-4 leading-relaxed">
      <Markdown
        components={{
          h1: ({ children }) => <h1 className="font-display text-3xl font-bold">{children}</h1>,
          h2: ({ children }) => <h2 className="mt-4 font-display text-xl font-bold">{children}</h2>,
          h3: ({ children }) => <h3 className="mt-2 font-semibold">{children}</h3>,
          p: ({ children }) => <p className="text-on-surface">{children}</p>,
          ul: ({ children }) => <ul className="ml-5 flex list-disc flex-col gap-1">{children}</ul>,
          ol: ({ children }) => <ol className="ml-5 flex list-decimal flex-col gap-1">{children}</ol>,
          a: ({ href, children }) => (
            <a href={href} className="text-primary underline" target="_blank" rel="noreferrer">
              {children}
            </a>
          ),
          hr: () => <hr className="border-outline-variant" />,
        }}
      >
        {markdown}
      </Markdown>
    </article>
  );
}
