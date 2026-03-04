#!/usr/bin/env node
/**
 * Serves Allure report from backend and/or frontend test results.
 * Run backend tests first: cd server && ./mvnw test -DskipITs
 * Run frontend tests: cd client && npm run test:allure
 */
const { existsSync } = require("fs");
const { join } = require("path");

const backendResults = join(__dirname, "..", "server", "target", "allure-results");
const frontendResults = join(__dirname, "..", "client", "allure-results");

const dirs = [backendResults, frontendResults].filter((d) => existsSync(d));
if (dirs.length === 0) {
  console.error("No Allure results found. Run tests first:");
  console.error("  Backend:  cd server && ./mvnw test -DskipITs");
  console.error("  Frontend: cd client && npm run test:allure");
  process.exit(1);
}

console.log("Serving Allure report from:", dirs.join(", "));
const allure = require("allure-commandline");
const serve = allure(["serve", ...dirs]);
serve.on("exit", (code) => process.exit(code || 0));
