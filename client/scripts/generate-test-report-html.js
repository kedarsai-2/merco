#!/usr/bin/env node
/**
 * Generates a simple static HTML test report from JUnit XML.
 * Designed for Jenkins - no JavaScript, no external resources, works with strict CSP.
 */
import { readFileSync, mkdirSync, writeFileSync } from "fs";
import { dirname, join } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const junitPath = join(__dirname, "..", "test-results", "junit.xml");
const outPath = join(__dirname, "..", "test-results", "html", "index.html");

function escapeHtml(s) {
  if (!s) return "";
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function parseJunit(xml) {
  const suites = [];
  const tsMatch = xml.match(/<testsuites[^>]*\s+tests="(\d+)"[^>]*\s+failures="(\d+)"[^>]*\s+errors="(\d+)"[^>]*\s+time="([^"]*)"/);
  const total = tsMatch ? { tests: +tsMatch[1], failures: +tsMatch[2], errors: +tsMatch[3], time: tsMatch[4] } : { tests: 0, failures: 0, errors: 0, time: "0" };

  const suiteRegex = /<testsuite\s+name="([^"]*)"[^>]*\s+tests="(\d+)"[^>]*\s+failures="(\d+)"[^>]*\s+errors="(\d+)"[^>]*\s+skipped="(\d+)"[^>]*\s+time="([^"]*)"/g;
  let m;
  while ((m = suiteRegex.exec(xml)) !== null) {
    const suiteStart = m.index;
    const suiteEnd = xml.indexOf("</testsuite>", suiteStart) + 12;
    const suiteXml = xml.slice(suiteStart, suiteEnd);
    const cases = [];
    const caseRegex = /<testcase[^>]*\s+classname="([^"]*)"[^>]*\s+name="([^"]*)"[^>]*\s+time="([^"]*)"[^>]*>([\s\S]*?)<\/testcase>/g;
    let cm;
    while ((cm = caseRegex.exec(suiteXml)) !== null) {
      const hasFailure = /<failure[^>]*>([\s\S]*?)<\/failure>/.exec(cm[4]);
      const hasError = /<error[^>]*>([\s\S]*?)<\/error>/.exec(cm[4]);
      cases.push({
        classname: cm[1],
        name: cm[2],
        time: cm[3],
        status: hasFailure || hasError ? "fail" : "pass",
        message: (hasFailure && hasFailure[1]) || (hasError && hasError[1]) || "",
      });
    }
    suites.push({
      name: m[1],
      tests: +m[2],
      failures: +m[3],
      errors: +m[4],
      skipped: +m[5],
      time: m[6],
      cases,
    });
  }

  return { total, suites };
}

function buildHtml(data) {
  const { total, suites } = data;
  const passed = total.tests - total.failures - total.errors;
  const failed = total.failures + total.errors;

  let rows = "";
  let rowIdx = 0;
  for (const suite of suites) {
    const status = suite.failures + suite.errors > 0 ? "fail" : "pass";
    const suiteBg = status === "pass" ? "#d1fae5" : "#fecaca";
    const failColor = status === "fail" ? "#b91c1c" : "#047857";
    rows += `
    <tr bgcolor="${suiteBg}" style="background:${suiteBg};font-weight:600">
      <td style="padding:10px 14px;border:1px solid #9ca3af" bgcolor="${suiteBg}">${escapeHtml(suite.name)}</td>
      <td style="padding:10px 14px;border:1px solid #9ca3af;text-align:center" bgcolor="${suiteBg}" align="center">${suite.tests}</td>
      <td style="padding:10px 14px;border:1px solid #9ca3af;text-align:center;color:${failColor}" bgcolor="${suiteBg}" align="center"><font color="${failColor}">${suite.failures + suite.errors}</font></td>
      <td style="padding:10px 14px;border:1px solid #9ca3af;text-align:center" bgcolor="${suiteBg}" align="center">${suite.skipped}</td>
      <td style="padding:10px 14px;border:1px solid #9ca3af;text-align:right" bgcolor="${suiteBg}" align="right">${suite.time}s</td>
    </tr>`;
    for (const c of suite.cases) {
      const caseBg = rowIdx % 2 === 0 ? "#ffffff" : "#f1f5f9";
      const caseColor = c.status === "fail" ? "#b91c1c" : "#047857";
      rows += `
    <tr bgcolor="${caseBg}" style="background:${caseBg}">
      <td style="padding:8px 14px 8px 28px;border:1px solid #cbd5e1;color:#334155" bgcolor="${caseBg}">${escapeHtml(c.name)}</td>
      <td style="padding:8px 14px;border:1px solid #cbd5e1;text-align:center" bgcolor="${caseBg}" align="center">1</td>
      <td style="padding:8px 14px;border:1px solid #cbd5e1;text-align:center;color:${caseColor}" bgcolor="${caseBg}" align="center"><font color="${caseColor}">${c.status === "fail" ? "1" : "0"}</font></td>
      <td style="padding:8px 14px;border:1px solid #cbd5e1;text-align:center" bgcolor="${caseBg}" align="center">0</td>
      <td style="padding:8px 14px;border:1px solid #cbd5e1;text-align:right;color:#64748b" bgcolor="${caseBg}" align="right"><font color="#64748b">${c.time}s</font></td>
    </tr>`;
      rowIdx++;
      if (c.status === "fail" && c.message) {
        rows += `
    <tr bgcolor="#fecaca" style="background:#fecaca">
      <td colspan="5" style="padding:12px 28px;border:1px solid #f87171;font-size:12px;white-space:pre-wrap;word-break:break-word" bgcolor="#fecaca"><font color="#991b1b">${escapeHtml(c.message)}</font></td>
    </tr>`;
      }
    }
  }

  const failBadgeBg = failed > 0 ? "#dc2626" : "#64748b";
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Frontend Test Report - MercoTrace</title>
  <style type="text/css">
    body{margin:0;padding:24px;font-family:system-ui,sans-serif;background:#e2e8f0}
    .wrap{max-width:1200px;margin:0 auto}
    .header{display:flex;align-items:center;gap:16px;margin-bottom:24px;padding-bottom:20px;border-bottom:3px solid #7c3aed}
    h1{margin:0;font-size:26px;font-weight:700;color:#1e293b}
    .subtitle{font-size:14px;color:#64748b}
    .badges{display:flex;flex-wrap:wrap;gap:12px;margin-bottom:24px}
    .badge{display:inline-block;padding:10px 18px;border-radius:8px;font-weight:700;font-size:15px;color:#fff}
    .badge-total{background:#7c3aed}
    .badge-pass{background:#059669}
    .badge-fail{background:${failBadgeBg}}
    .badge-time{background:#475569}
    .card{background:#fff;border-radius:12px;box-shadow:0 4px 6px rgba(0,0,0,0.1);overflow:hidden;border:1px solid #cbd5e1}
    table{width:100%;border-collapse:collapse}
    th{padding:14px 16px;text-align:left;font-weight:700;font-size:14px;background:#7c3aed;color:#fff;border:1px solid #6d28d9}
  </style>
</head>
<body bgcolor="#e2e8f0" style="margin:0;padding:24px;background:#e2e8f0;font-family:system-ui,sans-serif;color:#1e293b">
  <div class="wrap">
    <div class="header" style="margin-bottom:24px;padding-bottom:20px;border-bottom:3px solid #7c3aed">
      <h1 style="margin:0;font-size:26px;font-weight:700;color:#1e293b">Frontend Test Report</h1>
      <span class="subtitle" style="font-size:14px;color:#64748b">MercoTrace · Vitest</span>
    </div>
    <div class="badges">
      <span class="badge badge-total" bgcolor="#7c3aed" style="background:#7c3aed;color:#fff;padding:10px 18px;border-radius:8px;font-weight:700;font-size:15px"><font color="#fff">Total: ${total.tests}</font></span>
      <span class="badge badge-pass" bgcolor="#059669" style="background:#059669;color:#fff;padding:10px 18px;border-radius:8px;font-weight:700;font-size:15px"><font color="#fff">Passed: ${passed}</font></span>
      <span class="badge badge-fail" bgcolor="${failBadgeBg}" style="background:${failBadgeBg};color:#fff;padding:10px 18px;border-radius:8px;font-weight:700;font-size:15px"><font color="#fff">Failed: ${failed}</font></span>
      <span class="badge badge-time" bgcolor="#475569" style="background:#475569;color:#fff;padding:10px 18px;border-radius:8px;font-weight:700;font-size:15px"><font color="#fff">Time: ${total.time}s</font></span>
    </div>
    <div class="card" style="background:#fff;border-radius:12px;box-shadow:0 4px 6px rgba(0,0,0,0.1);overflow:hidden;border:1px solid #cbd5e1">
      <table border="1" cellpadding="0" cellspacing="0" bordercolor="#cbd5e1">
        <thead>
          <tr bgcolor="#7c3aed">
            <th style="padding:14px 16px;background:#7c3aed;color:#fff" bgcolor="#7c3aed"><font color="#fff">Test</font></th>
            <th style="padding:14px 16px;background:#7c3aed;color:#fff;text-align:center" bgcolor="#7c3aed" align="center"><font color="#fff">Tests</font></th>
            <th style="padding:14px 16px;background:#7c3aed;color:#fff;text-align:center" bgcolor="#7c3aed" align="center"><font color="#fff">Failures</font></th>
            <th style="padding:14px 16px;background:#7c3aed;color:#fff;text-align:center" bgcolor="#7c3aed" align="center"><font color="#fff">Skipped</font></th>
            <th style="padding:14px 16px;background:#7c3aed;color:#fff;text-align:right" bgcolor="#7c3aed" align="right"><font color="#fff">Time</font></th>
          </tr>
        </thead>
        <tbody>${rows}
        </tbody>
      </table>
    </div>
  </div>
</body>
</html>`;
}

try {
  let xml;
  try {
    xml = readFileSync(junitPath, "utf8");
  } catch {
    if (process.env.CI) {
      console.error("junit.xml not found. Run tests with CI=true first.");
      process.exit(1);
    }
    process.exit(0);
  }
  const data = parseJunit(xml);
  const html = buildHtml(data);
  mkdirSync(dirname(outPath), { recursive: true });
  writeFileSync(outPath, html, "utf8");
  console.log("Generated static HTML report at", outPath);
} catch (err) {
  console.error("Failed to generate report:", err.message);
  process.exit(1);
}
