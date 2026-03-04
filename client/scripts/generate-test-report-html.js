#!/usr/bin/env node
/**

* Generates a professional static HTML test report from JUnit XML
* Jenkins Safe (No JS, No external resources)
  */

import { readFileSync, mkdirSync, writeFileSync } from "fs";
import { dirname, join } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));

const junitPath = join(__dirname, "..", "test-results", "junit.xml");
const outPath = join(__dirname, "..", "test-results", "html", "index.html");

/* -------------------- Utilities -------------------- */

function escapeHtml(s) {
  if (!s) return "";
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

/* -------------------- Parse JUnit -------------------- */

function parseJunit(xml) {
const suites = [];

const tsMatch = xml.match(
/<testsuites[^>]*\s+tests="(\d+)"[^>]*\s+failures="(\d+)"[^>]*\s+errors="(\d+)"[^>]*\s+time="([^"]*)"/
);

const total = tsMatch
? {
tests: +tsMatch[1],
failures: +tsMatch[2],
errors: +tsMatch[3],
time: tsMatch[4],
}
: { tests: 0, failures: 0, errors: 0, time: "0" };

const suiteRegex =
/<testsuite\s+name="([^"]*)"[^>]*\s+tests="(\d+)"[^>]*\s+failures="(\d+)"[^>]*\s+errors="(\d+)"[^>]*\s+skipped="(\d+)"[^>]*\s+time="([^"]*)"/g;

let m;

while ((m = suiteRegex.exec(xml)) !== null) {
const suiteStart = m.index;
const suiteEnd = xml.indexOf("</testsuite>", suiteStart) + 12;
const suiteXml = xml.slice(suiteStart, suiteEnd);

    const cases = [];

const caseRegex =
  /<testcase[^>]*\s+classname="([^"]*)"[^>]*\s+name="([^"]*)"[^>]*\s+time="([^"]*)"[^>]*>([\s\S]*?)<\/testcase>/g;

let cm;

while ((cm = caseRegex.exec(suiteXml)) !== null) {
  const hasFailure = /<failure[^>]*>([\s\S]*?)<\/failure>/.exec(cm[4]);
  const hasError = /<error[^>]*>([\s\S]*?)<\/error>/.exec(cm[4]);

  cases.push({
    classname: cm[1],
    name: cm[2],
    time: cm[3],
    status: hasFailure || hasError ? "fail" : "pass",
    message:
      (hasFailure && hasFailure[1]) ||
      (hasError && hasError[1]) ||
      "",
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

/* -------------------- Build HTML -------------------- */

function buildHtml(data) {
  const { total, suites } = data;

  const passed = total.tests - total.failures - total.errors;
  const failed = total.failures + total.errors;
  const passRate = total.tests ? ((passed / total.tests) * 100).toFixed(1) : "0";

  const date = new Date().toLocaleString();

  let rows = "";

  for (const suite of suites) {
    const status = suite.failures + suite.errors > 0 ? "FAIL" : "PASS";
    const suiteColor = status === "PASS" ? "#d1fae5" : "#fecaca";

    rows += `
<tr bgcolor="${suiteColor}">
<td colspan="5" style="padding:10px;font-weight:bold">
${escapeHtml(suite.name)} [${status}]
</td>
</tr>`;

    for (const c of suite.cases) {
      const failColor = c.status === "fail" ? "#b91c1c" : "#047857";

      rows += `
<tr>
<td style="padding:8px 20px">${escapeHtml(c.name)}</td>
<td align="center">1</td>
<td align="center"><font color="${failColor}">
${c.status === "fail" ? "1" : "0"}
</font></td>
<td align="center">0</td>
<td align="right">${c.time}s</td>
</tr>`;
    }
  }

  return `<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Frontend Test Report</title>
</head>

<body style="font-family:Arial;background:#e2e8f0;padding:25px">

<h1 style="margin-bottom:5px">Frontend Test Report</h1>

<p style="color:#475569">
Project: MercoTrace | Framework: Vitest | Generated: ${date}
</p>

<table border="1" cellpadding="10" cellspacing="0" style="margin-top:20px;background:white">

<tr bgcolor="#6366f1">
<td><font color="white"><b>Total Tests</b></font></td>
<td><font color="white"><b>Passed</b></font></td>
<td><font color="white"><b>Failed</b></font></td>
<td><font color="white"><b>Pass Rate</b></font></td>
<td><font color="white"><b>Execution Time</b></font></td>
</tr>

<tr>
<td align="center">${total.tests}</td>
<td align="center"><font color="#16a34a">${passed}</font></td>
<td align="center"><font color="#dc2626">${failed}</font></td>
<td align="center">${passRate}%</td>
<td align="center">${total.time}s</td>
</tr>

</table>

<br>

<table border="1" cellpadding="10" cellspacing="0" width="100%" style="background:white">

<tr bgcolor="#4f46e5">
<th><font color="white">Test Name</font></th>
<th><font color="white">Tests</font></th>
<th><font color="white">Failures</font></th>
<th><font color="white">Skipped</font></th>
<th><font color="white">Time</font></th>
</tr>

${rows}

</table>

</body>
</html>`;
}
/* -------------------- Run -------------------- */

try {
let xml;

try {
xml = readFileSync(junitPath, "utf8");
} catch {
if (process.env.CI) {
console.error(
"junit.xml not found. Run tests with CI=true first."
);
process.exit(1);
}
process.exit(0);
}

const data = parseJunit(xml);
const html = buildHtml(data);

mkdirSync(dirname(outPath), { recursive: true });

writeFileSync(outPath, html, "utf8");

console.log("Generated HTML report at", outPath);
} catch (err) {
console.error("Failed to generate report:", err.message);
process.exit(1);
}
