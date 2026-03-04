#!/usr/bin/env node
/**
 * Generates an executive summary HTML report for MercoTrace.
 * Combines backend + frontend test results into one attractive dashboard for sharing.
 * Run after backend and frontend tests. Output: reports/executive-summary.html
 */
import { readFileSync, readdirSync, mkdirSync, writeFileSync, existsSync } from "fs";
import { dirname, join } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = join(__dirname, "..");
const outDir = join(projectRoot, "reports");
const outPath = join(outDir, "executive-summary.html");

function parseSurefire(xml) {
  const suites = [];
  const suiteRegex = /<testsuite[^>]*\s+name="([^"]*)"[^>]*\s+tests="(\d+)"[^>]*\s+failures="(\d+)"[^>]*\s+errors="(\d+)"[^>]*\s+skipped="(\d+)"[^>]*\s+time="([^"]*)"/g;
  let m;
  while ((m = suiteRegex.exec(xml)) !== null) {
    suites.push({
      name: m[1].replace(/^com\.mercotrace\./, ""),
      tests: +m[2],
      failures: +m[3],
      errors: +m[4],
      skipped: +m[5],
      time: parseFloat(m[6]) || 0,
    });
  }
  return suites;
}

function parseJunit(xml) {
  const tsMatch = xml.match(/<testsuites[^>]*\s+tests="(\d+)"[^>]*\s+failures="(\d+)"[^>]*\s+errors="(\d+)"[^>]*\s+time="([^"]*)"/);
  const total = tsMatch
    ? { tests: +tsMatch[1], failures: +tsMatch[2], errors: +tsMatch[3], time: parseFloat(tsMatch[4]) || 0 }
    : { tests: 0, failures: 0, errors: 0, time: 0 };
  const suites = [];
  const suiteRegex = /<testsuite\s+name="([^"]*)"[^>]*\s+tests="(\d+)"[^>]*\s+failures="(\d+)"[^>]*\s+errors="(\d+)"[^>]*\s+skipped="(\d+)"[^>]*\s+time="([^"]*)"/g;
  let m;
  while ((m = suiteRegex.exec(xml)) !== null) {
    suites.push({
      name: m[1],
      tests: +m[2],
      failures: +m[3],
      errors: +m[4],
      skipped: +m[5],
      time: parseFloat(m[6]) || 0,
    });
  }
  return { total, suites };
}

function getBackendStats() {
  const reportsDir = join(projectRoot, "server", "target", "surefire-reports");
  if (!existsSync(reportsDir)) return null;
  const files = readdirSync(reportsDir).filter((f) => f.startsWith("TEST-") && f.endsWith(".xml"));
  if (files.length === 0) return null;
  let total = { tests: 0, failures: 0, errors: 0, skipped: 0, time: 0 };
  const suites = [];
  for (const f of files.sort()) {
    const xml = readFileSync(join(reportsDir, f), "utf8");
    const parsed = parseSurefire(xml);
    for (const s of parsed) {
      suites.push(s);
      total.tests += s.tests;
      total.failures += s.failures;
      total.errors += s.errors;
      total.skipped += s.skipped;
      total.time += s.time;
    }
  }
  return { total, suites };
}

function getFrontendStats() {
  const junitPath = join(projectRoot, "client", "test-results", "junit.xml");
  if (!existsSync(junitPath)) return null;
  const xml = readFileSync(junitPath, "utf8");
  return parseJunit(xml);
}

function getCoverage() {
  const jacocoPath = join(projectRoot, "server", "target", "site", "jacoco", "index.html");
  const frontCovPath = join(projectRoot, "client", "coverage", "index.html");
  const cov = { backend: null, frontend: null };
  if (existsSync(jacocoPath)) cov.backend = "../server/target/site/jacoco/index.html";
  if (existsSync(frontCovPath)) cov.frontend = "../client/coverage/index.html";
  return cov;
}

function buildHtml(backend, frontend, coverage) {
  const now = new Date().toLocaleString();
  const backendPassed = backend ? backend.total.tests - backend.total.failures - backend.total.errors : 0;
  const backendFailed = backend ? backend.total.failures + backend.total.errors : 0;
  const frontendPassed = frontend ? frontend.total.tests - frontend.total.failures - frontend.total.errors : 0;
  const frontendFailed = frontend ? frontend.total.failures + frontend.total.errors : 0;
  const totalTests = (backend?.total.tests || 0) + (frontend?.total.tests || 0);
  const totalPassed = backendPassed + frontendPassed;
  const totalFailed = backendFailed + frontendFailed;
  const overallOk = totalFailed === 0;

  const statusBg = overallOk ? "#059669" : "#dc2626";
  const statusText = overallOk ? "All Tests Passed" : "Some Tests Failed";

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>MercoTrace - Executive Summary</title>
  <style type="text/css">
    body{margin:0;padding:24px;font-family:system-ui,sans-serif;background:linear-gradient(135deg,#0f172a 0%,#1e293b 50%,#334155 100%);color:#f8fafc;min-height:100vh}
    .wrap{max-width:1000px;margin:0 auto}
    .header{text-align:center;margin-bottom:32px;padding:24px;border-bottom:2px solid #475569}
    h1{margin:0 0 8px 0;font-size:32px;font-weight:700;color:#fff}
    .subtitle{font-size:14px;color:#94a3b8}
    .status{display:inline-block;padding:12px 24px;border-radius:12px;font-weight:700;font-size:18px;margin:16px 0;color:#fff}
    .cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:20px;margin-bottom:32px}
    .card{background:rgba(255,255,255,0.08);border:1px solid #475569;border-radius:12px;padding:24px;backdrop-filter:blur(8px)}
    .card h2{margin:0 0 16px 0;font-size:18px;color:#94a3b8;font-weight:600}
    .card .stat{font-size:36px;font-weight:700;color:#fff}
    .card .stat.green{color:#22c55e}
    .card .stat.red{color:#ef4444}
    .card .meta{font-size:13px;color:#64748b;margin-top:8px}
    .links{margin-top:24px;padding-top:24px;border-top:1px solid #475569}
    .links a{display:inline-block;margin:4px 8px 4px 0;padding:8px 16px;background:#3b82f6;color:#fff;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px}
    .links a:hover{background:#2563eb}
    .links a.secondary{background:#475569}
    .footer{text-align:center;margin-top:32px;font-size:13px;color:#64748b}
  </style>
</head>
<body bgcolor="#0f172a">
  <div class="wrap">
    <div class="header">
      <h1>MercoTrace</h1>
      <p class="subtitle">Executive Summary · ${now}</p>
      <div class="status" style="background:${statusBg}"><font color="#fff">${statusText}</font></div>
    </div>
    <div class="cards">
      <div class="card" style="background:rgba(59,130,246,0.15);border-color:#3b82f6">
        <h2>Total Tests</h2>
        <div class="stat">${totalTests}</div>
        <div class="meta">${totalPassed} passed · ${totalFailed} failed</div>
      </div>
      <div class="card">
        <h2>Backend (Spring Boot)</h2>
        <div class="stat ${backendFailed > 0 ? "red" : "green"}">${backend ? backend.total.tests : "—"}</div>
        <div class="meta">${backend ? backendPassed + " passed · " + backendFailed + " failed" : "No data"} · ${backend ? backend.total.time.toFixed(2) + "s" : ""}</div>
      </div>
      <div class="card">
        <h2>Frontend (Vitest)</h2>
        <div class="stat ${frontendFailed > 0 ? "red" : "green"}">${frontend ? frontend.total.tests : "—"}</div>
        <div class="meta">${frontend ? frontendPassed + " passed · " + frontendFailed + " failed" : "No data"} · ${frontend ? frontend.total.time.toFixed(2) + "s" : ""}</div>
      </div>
    </div>
    <div class="card" style="padding:20px">
      <h2 style="margin-bottom:12px">Report Links</h2>
      <div class="links">
        <a href="../client/test-results/html/index.html">Frontend Test Report</a>
        ${coverage.backend ? '<a href="' + coverage.backend + '">Backend Coverage (JaCoCo)</a>' : ""}
        ${coverage.frontend ? '<a href="' + coverage.frontend + '">Frontend Coverage</a>' : ""}
      </div>
    </div>
    <p class="footer">Generated by MercoTrace CI · Share this report with your team</p>
  </div>
</body>
</html>`;
}

try {
  const backend = getBackendStats();
  const frontend = getFrontendStats();
  const coverage = getCoverage();

  mkdirSync(outDir, { recursive: true });
  const html = buildHtml(backend, frontend, coverage);
  writeFileSync(outPath, html, "utf8");
  console.log("Generated executive summary at", outPath);
} catch (err) {
  console.error("Failed to generate executive report:", err.message);
  process.exit(1);
}
