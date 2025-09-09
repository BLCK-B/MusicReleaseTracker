import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vitejs.dev/config/
// https://vitest.dev/config/
export default defineConfig({
  test: {
    globals: true,
    environment: "jsdom",
    include: ["**/*.{test,spec}.js"],
    coverage: {
      provider: "c8",
      reporter: ["text", "json", "html"],
      exclude: ["node_modules", "dist"],
    },
  },
  plugins: [vue()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  build: {
    outDir: "../src/main/resources/static",
  },
  base: "./",
});
