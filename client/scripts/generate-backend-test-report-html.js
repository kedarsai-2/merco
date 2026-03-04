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

  const timeStr = total.time.toFixed(3);
  const failBadgeBg = failed > 0 ? "#dc2626" : "#64748b";
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Backend Test Report - MercoTrace</title>
  <style type="text/css">
    body{margin:0;padding:24px;font-family:system-ui,sans-serif;background:#e2e8f0}
    .wrap{max-width:1200px;margin:0 auto}
    .header{display:flex;align-items:center;gap:16px;margin-bottom:24px;padding-bottom:20px;border-bottom:3px solid #3b82f6}
    h1{margin:0;font-size:26px;font-weight:700;color:#1e293b}
    .subtitle{font-size:14px;color:#64748b}
    .badges{display:flex;flex-wrap:wrap;gap:12px;margin-bottom:24px}
    .badge{display:inline-block;padding:10px 18px;border-radius:8px;font-weight:700;font-size:15px;color:#fff}
    .badge-total{background:#2563eb}
    .badge-pass{background:#059669}
    .badge-fail{background:${failBadgeBg}}
    .badge-time{background:#475569}
    .card{background:#fff;border-radius:12px;box-shadow:0 4px 6px rgba(0,0,0,0.1);overflow:hidden;border:1px solid #cbd5e1}
    table{width:100%;border-collapse:collapse}
    th{padding:14px 16px;text-align:left;font-weight:700;font-size:14px;background:#3b82f6;color:#fff;border:1px solid #2563eb}
    th:nth-child(2),th:nth-child(3),th:nth-child(4){text-align:center}
    th:last-child{text-align:right}
  </style>
</head>
<body bgcolor="#e2e8f0" style="margin:0;padding:24px;background:#e2e8f0;font-family:system-ui,sans-serif;color:#1e293b">
  <div class="wrap">
    <div class="header" style="margin-bottom:24px;padding-bottom:20px;border-bottom:3px solid #3b82f6">
      <h1 style="margin:0;font-size:26px;font-weight:700;color:#1e293b">Backend Test Report</h1>
      <span class="subtitle" style="font-size:14px;color:#64748b">MercoTrace · Spring Boot</span>
    </div>
    <div class="badges">
      <span class="badge badge-total" bgcolor="#2563eb" style="background:#2563eb;color:#fff;padding:10px 18px;border-radius:8px;font-weight:700;font-size:15px"><font color="#fff">Total: ${total.tests}</font></span>
      <span class="badge badge-pass" bgcolor="#059669" style="background:#059669;color:#fff;padding:10px 18px;border-radius:8px;font-weight:700;font-size:15px"><font color="#fff">Passed: ${passed}</font></span>
      <span class="badge badge-fail" bgcolor="${failBadgeBg}" style="background:${failBadgeBg};color:#fff;padding:10px 18px;border-radius:8px;font-weight:700;font-size:15px"><font color="#fff">Failed: ${failed}</font></span>
      <span class="badge badge-time" bgcolor="#475569" style="background:#475569;color:#fff;padding:10px 18px;border-radius:8px;font-weight:700;font-size:15px"><font color="#fff">Time: ${timeStr}s</font></span>
    </div>
    <div class="card" style="background:#fff;border-radius:12px;box-shadow:0 4px 6px rgba(0,0,0,0.1);overflow:hidden;border:1px solid #cbd5e1">
      <table border="1" cellpadding="0" cellspacing="0" bordercolor="#cbd5e1">
        <thead>
          <tr bgcolor="#3b82f6">
            <th style="padding:14px 16px;background:#3b82f6;color:#fff" bgcolor="#3b82f6"><font color="#fff">Test</font></th>
            <th style="padding:14px 16px;background:#3b82f6;color:#fff;text-align:center" bgcolor="#3b82f6" align="center"><font color="#fff">Tests</font></th>
            <th style="padding:14px 16px;background:#3b82f6;color:#fff;text-align:center" bgcolor="#3b82f6" align="center"><font color="#fff">Failures</font></th>
            <th style="padding:14px 16px;background:#3b82f6;color:#fff;text-align:center" bgcolor="#3b82f6" align="center"><font color="#fff">Skipped</font></th>
            <th style="padding:14px 16px;background:#3b82f6;color:#fff;text-align:right" bgcolor="#3b82f6" align="right"><font color="#fff">Time</font></th>
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
