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
const passRate =
total.tests > 0 ? ((passed / total.tests) * 100).toFixed(1) : "0";

const date = new Date().toLocaleString();

let rows = "";

for (const suite of suites) {
const status = suite.failures + suite.errors > 0 ? "FAIL" : "PASS";
const suiteColor = status === "PASS" ? "#16a34a" : "#dc2626";

    rows += `

<tr class="suite">
<td colspan="5">
${escapeHtml(suite.name)}
<span class="suite-status" style="color:${suiteColor}">
[${status}]
</span>
</td>
</tr>`;

    for (const c of suite.cases) {
  const statusColor = c.status === "fail" ? "#dc2626" : "#16a34a";

  rows += `
<tr>
<td class="test">${escapeHtml(c.name)}</td>
<td align="center">1</td>
<td align="center" style="color:${statusColor}">
${c.status === "fail" ? "1" : "0"}
</td>
<td align="center">0</td>
<td align="right">${c.time}s</td>
</tr>`;
    }
  }

return `<!DOCTYPE html>

<html lang="en">

<head>
<meta charset="UTF-8">
<title>Frontend Test Report</title>

<style>

body{
font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Arial;
background:#f3f4f6;
margin:0;
padding:40px;
color:#111827;
}

.container{
max-width:1100px;
margin:auto;
}

.header{
margin-bottom:30px;
}

h1{
margin:0;
font-size:34px;
}

.subtitle{
margin-top:6px;
color:#6b7280;
font-size:14px;
}

.summary{
display:grid;
grid-template-columns:repeat(5,1fr);
gap:18px;
margin:30px 0;
}

.card{
padding:20px;
border-radius:10px;
color:white;
font-weight:600;
text-align:center;
box-shadow:0 6px 15px rgba(0,0,0,0.1);
}

.total{background:#6366f1;}
.pass{background:#16a34a;}
.fail{background:#dc2626;}
.rate{background:#0ea5e9;}
.time{background:#334155;}

.card span{
display:block;
font-size:30px;
margin-top:8px;
}

table{
width:100%;
border-collapse:collapse;
background:white;
border-radius:10px;
overflow:hidden;
box-shadow:0 4px 10px rgba(0,0,0,0.08);
}

th{
background:#4f46e5;
color:white;
padding:14px;
text-align:left;
}

td{
padding:12px;
border-bottom:1px solid #e5e7eb;
}

tr:hover{
background:#f9fafb;
}

.suite{
background:#eef2ff;
font-weight:600;
}

.suite-status{
margin-left:10px;
font-weight:bold;
}

.test{
padding-left:28px;
color:#374151;
}

</style>

</head>

<body>

<div class="container">

<div class="header">
<h1>Frontend Test Report</h1>
<div class="subtitle">
Project: MercoTrace | Framework: Vitest | Generated: ${date}
</div>
</div>

<div class="summary">

<div class="card total">
Total Tests
<span>${total.tests}</span>
</div>

<div class="card pass">
Passed
<span>${passed}</span>
</div>

<div class="card fail">
Failed
<span>${failed}</span>
</div>

<div class="card rate">
Pass Rate
<span>${passRate}%</span>
</div>

<div class="card time">
Execution Time
<span>${total.time}s</span>
</div>

</div>

<table>

<thead>
<tr>
<th>Test Name</th>
<th>Tests</th>
<th>Failures</th>
<th>Skipped</th>
<th>Time</th>
</tr>
</thead>

<tbody>
${rows}
</tbody>

</table>

</div>

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
