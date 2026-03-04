#!/usr/bin/env node
/**

* Generates a styled HTML test report from Maven Surefire XML reports.
* Reads server/target/surefire-reports/*.xml
* Outputs server/target/surefire-reports-html/surefire.html
  */

import { readFileSync, readdirSync, mkdirSync, writeFileSync } from "fs";
import { dirname, join } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = join(__dirname, "..", "..");

const reportsDir = join(projectRoot, "server", "target", "surefire-reports");
const outPath = join(projectRoot, "server", "target", "surefire-reports-html", "surefire.html");

/* -------------------- Utilities -------------------- */

function escapeHtml(s) {
  if (!s) return "";
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

/* -------------------- Parse XML -------------------- */

function parseSurefireFile(xml) {
  const getAttr = (str, key) => {
    const m = str.match(new RegExp(`${key}="([^"]*)"`));
    return m ? m[1] : "";
  };

  const suiteTag = xml.match(/<testsuite[^>]+>/);
  if (!suiteTag) return null;

  const tag = suiteTag[0];
  const name = getAttr(tag, "name");
  const tests = parseInt(getAttr(tag, "tests"), 10) || 0;
  const failures = parseInt(getAttr(tag, "failures"), 10) || 0;
  const errors = parseInt(getAttr(tag, "errors"), 10) || 0;
  const skipped = parseInt(getAttr(tag, "skipped"), 10) || 0;
  const time = getAttr(tag, "time") || "0";

  const cases = [];
  const caseRegex =
    /<testcase[^>]*\s+classname="([^"]*)"[^>]*\s+name="([^"]*)"[^>]*\s+time="([^"]*)"[^>]*>([\s\S]*?)<\/testcase>/g;
  let m;

  while ((m = caseRegex.exec(xml)) !== null) {
    const hasFailure = /<failure[^>]*>([\s\S]*?)<\/failure>/.exec(m[4]);
    const hasError = /<error[^>]*>([\s\S]*?)<\/error>/.exec(m[4]);
    cases.push({
      classname: m[1],
      name: m[2],
      time: m[3],
      status: hasFailure || hasError ? "fail" : "pass",
      message: (hasFailure && hasFailure[1]) || (hasError && hasError[1]) || "",
    });
  }

  return {
    name: name.replace(/^com\.mercotrace\./, ""),
    tests,
    failures,
    errors,
    skipped,
    time,
    cases,
  };
}

/* -------------------- Build HTML -------------------- */

function buildHtml(suites) {
  const total = suites.reduce(
    (acc, s) => ({
      tests: acc.tests + s.tests,
      failures: acc.failures + s.failures,
      errors: acc.errors + s.errors,
      skipped: acc.skipped + s.skipped,
      time: acc.time + (parseFloat(s.time) || 0),
    }),
    { tests: 0, failures: 0, errors: 0, skipped: 0, time: 0 }
  );

  const passed = total.tests - total.failures - total.errors;
  const failed = total.failures + total.errors;
  const timeStr = total.time.toFixed(3);

  let rows = "";

  for (const suite of suites) {
    const status = suite.failures + suite.errors > 0 ? "FAIL" : "PASS";
    const suiteBg = status === "PASS" ? "#d1fae5" : "#fecaca";

    rows += `
<tr class="suite" bgcolor="${suiteBg}" style="background:${suiteBg};font-weight:600">
<td colspan="5" style="padding:10px 14px;border:1px solid #9ca3af" bgcolor="${suiteBg}">${escapeHtml(suite.name)} <span style="color:${status === "PASS" ? "#047857" : "#b91c1c"}">[${status}]</span></td>
</tr>`;

    for (const c of suite.cases) {
      const failColor = c.status === "fail" ? "#b91c1c" : "#047857";
      rows += `
<tr bgcolor="#ffffff">
<td class="test" style="padding:8px 14px 8px 28px;border:1px solid #e5e7eb;color:#334155">${escapeHtml(c.name)}</td>
<td align="center" style="border:1px solid #e5e7eb">1</td>
<td align="center" style="border:1px solid #e5e7eb"><font color="${failColor}">${c.status === "fail" ? "1" : "0"}</font></td>
<td align="center" style="border:1px solid #e5e7eb">0</td>
<td align="right" style="border:1px solid #e5e7eb;color:#6b7280">${c.time}s</td>
</tr>`;
      if (c.status === "fail" && c.message) {
        rows += `
<tr bgcolor="#fecaca">
<td colspan="5" style="padding:12px 28px;border:1px solid #f87171;font-size:12px;white-space:pre-wrap;word-break:break-word" bgcolor="#fecaca"><font color="#991b1b">${escapeHtml(c.message)}</font></td>
</tr>`;
      }
    }
  }

  const passRate = total.tests > 0 ? ((passed / total.tests) * 100).toFixed(1) : "0";
  const date = new Date().toLocaleString();

  return `<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Backend Test Report - MercoTrace</title>
<style type="text/css">
body{margin:0;padding:24px;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;background:#f3f4f6;color:#111827}
.container{max-width:1200px;margin:0 auto}
.header{margin-bottom:24px;padding-bottom:20px;border-bottom:2px solid #e5e7eb}
h1{margin:0;font-size:24px;font-weight:700;color:#111827}
.subtitle{font-size:13px;color:#6b7280;margin-top:6px}
.summary{display:flex;flex-wrap:wrap;gap:12px;margin-bottom:24px}
.badge{display:inline-block;padding:8px 16px;border-radius:8px;font-weight:600;font-size:14px;color:#fff}
.badge-total{background:#6366f1}
.badge-pass{background:#059669}
.badge-fail{background:${failed > 0 ? "#dc2626" : "#9ca3af"}}
.badge-skip{background:#6b7280}
.badge-time{background:#475569}
.card{background:#fff;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.08);overflow:hidden}
table{width:100%;border-collapse:collapse}
th{padding:12px 14px;text-align:left;font-weight:600;font-size:13px;color:#374151;border-bottom:2px solid #e5e7eb;background:#f9fafb}
th:nth-child(2),th:nth-child(3),th:nth-child(4){text-align:center}
th:last-child{text-align:right}
td{padding:10px 14px;border:1px solid #e5e7eb}
.suite{font-weight:600;background:#eef2ff}
.test{padding-left:28px;color:#374151}
</style>
</head>
<body bgcolor="#f3f4f6">
<div class="container">
<div class="header">
<h1>Backend Test Report</h1>
<div class="subtitle">MercoTrace · Spring Boot | Generated: ${date}</div>
</div>
<div class="summary">
<span class="badge badge-total" bgcolor="#6366f1"><font color="#fff">Total: ${total.tests}</font></span>
<span class="badge badge-pass" bgcolor="#059669"><font color="#fff">Passed: ${passed}</font></span>
<span class="badge badge-fail" bgcolor="${failed > 0 ? "#dc2626" : "#9ca3af"}"><font color="#fff">Failed: ${failed}</font></span>
<span class="badge badge-skip" bgcolor="#6b7280"><font color="#fff">Skipped: ${total.skipped}</font></span>
<span class="badge badge-time" bgcolor="#475569"><font color="#fff">Time: ${timeStr}s</font></span>
<span class="badge" style="background:#0ea5e9" bgcolor="#0ea5e9"><font color="#fff">Pass Rate: ${passRate}%</font></span>
</div>
<div class="card">
<table border="1" cellpadding="0" cellspacing="0" bordercolor="#e5e7eb">
<thead>
<tr bgcolor="#4f46e5">
<th style="background:#4f46e5;color:#fff" bgcolor="#4f46e5"><font color="#fff">Test</font></th>
<th style="background:#4f46e5;color:#fff;text-align:center" bgcolor="#4f46e5" align="center"><font color="#fff">Tests</font></th>
<th style="background:#4f46e5;color:#fff;text-align:center" bgcolor="#4f46e5" align="center"><font color="#fff">Failures</font></th>
<th style="background:#4f46e5;color:#fff;text-align:center" bgcolor="#4f46e5" align="center"><font color="#fff">Skipped</font></th>
<th style="background:#4f46e5;color:#fff;text-align:right" bgcolor="#4f46e5" align="right"><font color="#fff">Time</font></th>
</tr>
</thead>
<tbody>
${rows}
</tbody>
</table>
</div>
</div>
</body>
</html>`;
}

/* -------------------- Run -------------------- */

try {
  let files;
  try {
    files = readdirSync(reportsDir).filter((f) => f.startsWith("TEST-") && f.endsWith(".xml"));
  } catch {
    if (process.env.CI) {
      console.error("Backend surefire reports not found. Run backend tests first.");
      process.exit(1);
    }
    process.exit(0);
  }

  if (files.length === 0) {
    if (process.env.CI) {
      console.error("No TEST-*.xml files in", reportsDir);
      process.exit(1);
    }
    process.exit(0);
  }

  const suites = [];
  for (const f of files.sort()) {
    const xml = readFileSync(join(reportsDir, f), "utf8");
    const suite = parseSurefireFile(xml);
    if (suite) suites.push(suite);
  }

  const html = buildHtml(suites);
  mkdirSync(dirname(outPath), { recursive: true });
  writeFileSync(outPath, html, "utf8");

  console.log("Generated backend test report at", outPath);
} catch (err) {
  console.error("Failed to generate backend report:", err.message);
  process.exit(1);
}
