#!/usr/bin/env node
/**
 * Generates Allure report from backend and/or frontend test results.
 * Run backend tests first: cd server && ./mvnw test -DskipITs
 * Run frontend tests: cd client && npm run test:allure
 */
const { spawn } = require("child_process");
const { existsSync } = require("fs");
const { join } = require("path");

const backendResults = join(__dirname, "..", "server", "target", "allure-results");
const frontendResults = join(__dirname, "..", "client", "allure-results");
const outputDir = join(__dirname, "..", "allure-report");

const dirs = [backendResults, frontendResults].filter((d) => existsSync(d));
if (dirs.length === 0) {
  console.error("No Allure results found. Run tests first:");
  console.error("  Backend:  cd server && ./mvnw test -DskipITs");
  console.error("  Frontend: cd client && npm run test:allure");
  process.exit(1);
}

console.log("Generating Allure report from:", dirs.join(", "));
const allure = require("allure-commandline");
const generation = allure([
  "generate",
  ...dirs,
  "-o",
  outputDir,
  "--clean",
]);

generation.on("exit", (code) => {
  if (code !== 0) process.exit(code);
  console.log("Report generated at", outputDir);
  console.log("Opening in browser...");
  const open = allure(["open", outputDir]);
  open.on("exit", (c) => process.exit(c));
});
