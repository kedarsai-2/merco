#!/usr/bin/env node
/**
 * Generates a styled HTML test report from Maven Surefire XML reports.
 * Reads server/target/surefire-reports/*.xml and outputs server/target/surefire-reports-html/surefire.html
 */
import { readFileSync, readdirSync, mkdirSync, writeFileSync } from "fs";
import { dirname, join } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = join(__dirname, "..", "..");
const reportsDir = join(projectRoot, "server", "target", "surefire-reports");
const outPath = join(projectRoot, "server", "target", "surefire-reports-html", "surefire.html");

function escapeHtml(s) {
  if (!s) return "";
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

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
  const caseRegex = /<testcase[^>]*\s+classname="([^"]*)"[^>]*\s+name="([^"]*)"[^>]*\s+time="([^"]*)"[^>]*>([\s\S]*?)<\/testcase>/g;
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
    tests: +tests,
    failures: +failures,
    errors: +errors,
    skipped: +skipped,
    time,
    cases,
  };
}

function buildHtml(suites) {
  const total = suites.reduce(
    (acc, s) => ({
      tests: acc.tests + s.tests,
      failures: acc.failures + s.failures,
      errors: acc.errors + s.errors,
      skipped: acc.skipped + s.skipped,
      time: acc.time + parseFloat(s.time) || 0,
    }),
    { tests: 0, failures: 0, errors: 0, skipped: 0, time: 0 }
  );
  const passed = total.tests - total.failures - total.errors;
  const failed = total.failures + total.errors;

  let rows = "";
  let rowIdx = 0;
  for (const suite of suites) {
    const status = suite.failures + suite.errors > 0 ? "fail" : "pass";
    const suiteBg = status === "pass" ? "#ecfdf5" : "#fef2f2";
    rows += `
    <tr style="background:${suiteBg};font-weight:600">
      <td style="padding:10px 14px;border:1px solid #e5e7eb">${escapeHtml(suite.name)}</td>
      <td style="padding:10px 14px;border:1px solid #e5e7eb;text-align:center">${suite.tests}</td>
      <td style="padding:10px 14px;border:1px solid #e5e7eb;text-align:center;color:${status === "fail" ? "#dc2626" : "#059669"}">${suite.failures + suite.errors}</td>
      <td style="padding:10px 14px;border:1px solid #e5e7eb;text-align:center">${suite.skipped}</td>
      <td style="padding:10px 14px;border:1px solid #e5e7eb;text-align:right">${suite.time}s</td>
    </tr>`;
    for (const c of suite.cases) {
      const caseBg = rowIdx % 2 === 0 ? "#ffffff" : "#f9fafb";
      const caseColor = c.status === "fail" ? "#dc2626" : "#059669";
      rows += `
    <tr style="background:${caseBg}">
      <td style="padding:8px 14px 8px 28px;border:1px solid #e5e7eb;color:#374151">${escapeHtml(c.name)}</td>
      <td style="padding:8px 14px;border:1px solid #e5e7eb;text-align:center">1</td>
      <td style="padding:8px 14px;border:1px solid #e5e7eb;text-align:center;color:${caseColor};font-weight:500">${c.status === "fail" ? "1" : "0"}</td>
      <td style="padding:8px 14px;border:1px solid #e5e7eb;text-align:center">0</td>
      <td style="padding:8px 14px;border:1px solid #e5e7eb;text-align:right;color:#6b7280;font-size:13px">${c.time}s</td>
    </tr>`;
      rowIdx++;
      if (c.status === "fail" && c.message) {
        rows += `
    <tr style="background:#fef2f2">
      <td colspan="5" style="padding:12px 28px;border:1px solid #fecaca;font-size:12px;font-family:ui-monospace,monospace;white-space:pre-wrap;word-break:break-word;color:#991b1b">${escapeHtml(c.message)}</td>
    </tr>`;
      }
    }
  }

  const timeStr = total.time.toFixed(3);
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Backend Test Report - MercoTrace</title>
</head>
<body style="margin:0;padding:24px;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#f3f4f6;color:#111827">
  <div style="max-width:1200px;margin:0 auto">
    <div style="display:flex;align-items:center;gap:16px;margin-bottom:24px;padding-bottom:20px;border-bottom:2px solid #e5e7eb">
      <h1 style="margin:0;font-size:24px;font-weight:700;color:#111827">Backend Test Report</h1>
      <span style="font-size:13px;color:#6b7280">MercoTrace · Spring Boot</span>
    </div>
    <div style="display:flex;flex-wrap:wrap;gap:12px;margin-bottom:24px">
      <span style="display:inline-block;padding:8px 16px;background:#3b82f6;color:#fff;border-radius:8px;font-weight:600;font-size:14px">Total: ${total.tests}</span>
      <span style="display:inline-block;padding:8px 16px;background:#059669;color:#fff;border-radius:8px;font-weight:600;font-size:14px">Passed: ${passed}</span>
      <span style="display:inline-block;padding:8px 16px;background:${failed > 0 ? "#dc2626" : "#9ca3af"};color:#fff;border-radius:8px;font-weight:600;font-size:14px">Failed: ${failed}</span>
      <span style="display:inline-block;padding:8px 16px;background:#6b7280;color:#fff;border-radius:8px;font-weight:600;font-size:14px">Time: ${timeStr}s</span>
    </div>
    <div style="background:#fff;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.08);overflow:hidden">
      <table style="width:100%;border-collapse:collapse">
        <thead>
          <tr style="background:#f9fafb">
            <th style="padding:12px 14px;text-align:left;font-weight:600;font-size:13px;color:#374151;border-bottom:2px solid #e5e7eb">Test</th>
            <th style="padding:12px 14px;text-align:center;font-weight:600;font-size:13px;color:#374151;border-bottom:2px solid #e5e7eb">Tests</th>
            <th style="padding:12px 14px;text-align:center;font-weight:600;font-size:13px;color:#374151;border-bottom:2px solid #e5e7eb">Failures</th>
            <th style="padding:12px 14px;text-align:center;font-weight:600;font-size:13px;color:#374151;border-bottom:2px solid #e5e7eb">Skipped</th>
            <th style="padding:12px 14px;text-align:right;font-weight:600;font-size:13px;color:#374151;border-bottom:2px solid #e5e7eb">Time</th>
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
