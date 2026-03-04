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
  for (const suite of suites) {
    const status = suite.failures + suite.errors > 0 ? "fail" : "pass";
    rows += `
    <tr class="suite ${status}">
      <td>${escapeHtml(suite.name)}</td>
      <td>${suite.tests}</td>
      <td>${suite.failures + suite.errors}</td>
      <td>${suite.skipped}</td>
      <td>${suite.time}s</td>
    </tr>`;
    for (const c of suite.cases) {
      rows += `
    <tr class="case ${c.status}">
      <td class="indent">${escapeHtml(c.name)}</td>
      <td>1</td>
      <td>${c.status === "fail" ? "1" : "0"}</td>
      <td>0</td>
      <td>${c.time}s</td>
    </tr>`;
      if (c.status === "fail" && c.message) {
        rows += `
    <tr class="failure-detail">
      <td colspan="5"><pre>${escapeHtml(c.message)}</pre></td>
    </tr>`;
      }
    }
  }

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Frontend Test Report</title>
</head>
<body>
  <!-- MercoTrace static test report - no JavaScript required -->
  <h1>Frontend Test Report</h1>
  <p><b>Total:</b> ${total.tests} | <b>Passed:</b> ${passed} | <b>Failed:</b> ${failed} | <b>Time:</b> ${total.time}s</p>
  <table border="1" cellpadding="8" cellspacing="0" width="100%">
    <thead>
      <tr>
        <th>Test</th>
        <th>Tests</th>
        <th>Failures</th>
        <th>Skipped</th>
        <th>Time</th>
      </tr>
    </thead>
    <tbody>${rows}
    </tbody>
  </table>
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
