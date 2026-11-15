import { fileURLToPath } from "node:url";
import { defineConfig, searchForWorkspaceRoot } from "vite";
import vinext from "vinext";
import { cloudflare } from "@cloudflare/vite-plugin";

export default defineConfig({
  // The privacy and terms pages render the policy files at the repo root, which the app also shows.
  server: {
    fs: {
      // A file entry never matches its "?raw" import, so allow the repo folder (dev server only).
      allow: [searchForWorkspaceRoot(process.cwd()), fileURLToPath(new URL("..", import.meta.url))],
    },
  },
  plugins: [
    vinext(),
    cloudflare({
      viteEnvironment: {
        name: "rsc",
        childEnvironments: ["ssr"],
      },
    }),
  ],
});
