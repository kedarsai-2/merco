#!/usr/bin/env node
/**
 * Backend HTML Test Report
 * Same UI as Frontend Report
 * Reads Maven Surefire XML
 */

import { readFileSync, readdirSync, mkdirSync, writeFileSync } from "fs";
import { dirname, join } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));

const reportsDir = join(__dirname, "..", "..", "server", "target", "surefire-reports");
const outPath = join(__dirname, "..", "..", "server", "target", "surefire-report.html");

/* -------------------- Utilities -------------------- */

function escapeHtml(s) {
  if (!s) return "";
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

/* -------------------- Parse Surefire -------------------- */

function parseSuite(xml) {

  const getAttr = (str, key) => {
    const m = str.match(new RegExp(`${key}="([^"]*)"'));
    return m ? m[1] : "";
  };

  const suiteTag = xml.match(/<testsuite[^>]+>/);

  if (!suiteTag) return null;

  const tag = suiteTag[0];

  const suite = {
    name: getAttr(tag, "name"),
    tests: +getAttr(tag, "tests") || 0,
    failures: +getAttr(tag, "failures") || 0,
    errors: +getAttr(tag, "errors") || 0,
    skipped: +getAttr(tag, "skipped") || 0,
    time: +getAttr(tag, "time") || 0,
    cases: []
  };

  const caseRegex =
    /<testcase[^>]*classname="([^"]*)"[^>]*name="([^"]*)"[^>]*time="([^"]*)"[^>]*>([\s\S]*?)<\/testcase>/g;

  let m;

  while ((m = caseRegex.exec(xml)) !== null) {

    const failure = /<failure[^>]*>([\s\S]*?)<\/failure>/.exec(m[4]);
    const error = /<error[^>]*>([\s\S]*?)<\/error>/.exec(m[4]);

    suite.cases.push({
      name: m[2],
      time: m[3],
      status: failure || error ? "fail" : "pass"
    });
  }

  return suite;
}

/* -------------------- Build HTML -------------------- */

function buildHtml(suites) {

  const total = suites.reduce(
    (a, s) => ({
      tests: a.tests + s.tests,
      failures: a.failures + s.failures,
      errors: a.errors + s.errors,
      skipped: a.skipped + s.skipped,
      time: a.time + s.time
    }),
    { tests: 0, failures: 0, errors: 0, skipped: 0, time: 0 }
  );

  const passed = total.tests - total.failures - total.errors;
  const failed = total.failures + total.errors;
  const passRate = total.tests ? ((passed / total.tests) * 100).toFixed(1) : "0";

  const date = new Date().toLocaleString();

  let rows = "";

  for (const suite of suites) {

    const status = suite.failures + suite.errors > 0 ? "FAIL" : "PASS";
    const suiteColor = status === "PASS" ? "#b7d7c8" : "#f5b7b1";

    rows += `
<tr bgcolor="${suiteColor}">
<td colspan="5" style="padding:10px">
${escapeHtml(suite.name)} [${status}]
</td>
</tr>`;

    for (const c of suite.cases) {

      rows += `
<tr>
<td style="padding:8px 20px">${escapeHtml(c.name)}</td>
<td align="center">1</td>
<td align="center"><font color="${c.status === "fail" ? "red" : "green"}">
${c.status === "fail" ? "1" : "0"}
</font></td>
<td align="center">0</td>
<td align="right">${c.time}s</td>
</tr>`;
    }
  }

  return `
<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<title>Backend Test Report</title>
</head>

<body style="font-family:Times;background:#d9d9d9;padding:20px">

<h1>Backend Test Report</h1>

<p>
Project: MercoTrace | Framework: Spring Boot | Generated: ${date}
</p>

<table border="1" cellpadding="10" cellspacing="0" style="background:white">

<tr bgcolor="#6366f1">
<td><font color="white"><b>Total Tests</b></font></td>
<td><font color="white"><b>Passed</b></font></td>
<td><font color="white"><b>Failed</b></font></td>
<td><font color="white"><b>Pass Rate</b></font></td>
<td><font color="white"><b>Execution Time</b></font></td>
</tr>

<tr>
<td align="center">${total.tests}</td>
<td align="center"><font color="green">${passed}</font></td>
<td align="center"><font color="red">${failed}</font></td>
<td align="center">${passRate}%</td>
<td align="center">${total.time.toFixed(3)}s</td>
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
</html>
`;
}

/* -------------------- Run -------------------- */

try {

  const files = readdirSync(reportsDir)
    .filter(f => f.startsWith("TEST-") && f.endsWith(".xml"));

  const suites = [];

  for (const f of files) {

    const xml = readFileSync(join(reportsDir, f), "utf8");

    const suite = parseSuite(xml);

    if (suite) suites.push(suite);
  }

  const html = buildHtml(suites);

  mkdirSync(dirname(outPath), { recursive: true });

  writeFileSync(outPath, html);

  console.log("Backend report generated:", outPath);

} catch (err) {

  console.error("Failed to generate report:", err.message);
  process.exit(1);

}