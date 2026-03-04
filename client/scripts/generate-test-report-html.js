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

  const date = new Date().toLocaleString();

  let rows = "";

  for (const suite of suites) {
    const status = suite.failures + suite.errors > 0 ? "FAIL" : "PASS";
    const suiteColor = status === "PASS" ? "#16a34a" : "#dc2626";

    rows += `
    <tr style="background:#f1f5f9;font-weight:bold">
      <td colspan="5" style="padding:10px">
        ${escapeHtml(suite.name)} 
        <span style="color:${suiteColor};margin-left:10px">[${status}]</span>
      </td>
    </tr>`;

    for (const c of suite.cases) {
      const statusColor = c.status === "fail" ? "#dc2626" : "#16a34a";

      rows += `
      <tr>
        <td style="padding-left:30px">${escapeHtml(c.name)}</td>
        <td align="center">1</td>
        <td align="center" style="color:${statusColor}">
          ${c.status === "fail" ? "1" : "0"}
        </td>
        <td align="center">0</td>
        <td align="right">${c.time}s</td>
      </tr>`;
    }
  }

  return `
<!DOCTYPE html>
<html>
<head>
<title>Frontend Test Report</title>

<style>
body{
  font-family: Arial, sans-serif;
  background:#f8fafc;
  margin:0;
  padding:30px;
}

.container{
  max-width:1200px;
  margin:auto;
}

.header{
  margin-bottom:30px;
}

h1{
  margin:0;
  color:#1e293b;
}

.subtitle{
  color:#64748b;
  margin-top:5px;
}

.summary{
  display:flex;
  gap:20px;
  margin:30px 0;
}

.card{
  flex:1;
  padding:20px;
  border-radius:8px;
  color:white;
  font-weight:bold;
  text-align:center;
}

.total{background:#6366f1;}
.pass{background:#16a34a;}
.fail{background:#dc2626;}
.time{background:#475569;}

.card span{
  display:block;
  font-size:28px;
  margin-top:10px;
}

table{
  width:100%;
  border-collapse:collapse;
  background:white;
}

th{
  background:#6366f1;
  color:white;
  padding:12px;
}

td{
  padding:10px;
  border-bottom:1px solid #e2e8f0;
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
</html>
`;
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
