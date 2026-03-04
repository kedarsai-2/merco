import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react-swc";
import path from "path";

export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: ["./src/test/setup.ts"],
    include: ["src/**/*.{test,spec}.{ts,tsx}"],
    reporters: process.env.CI
      ? ["default", "html", "junit"]
      : ["default"],
    outputFile: process.env.CI
      ? {
          html: "./test-results/html/index.html",
          junit: "./test-results/junit.xml",
        }
      : undefined,
  },
  resolve: {
    alias: { "@": path.resolve(__dirname, "./src") },
  },
});
