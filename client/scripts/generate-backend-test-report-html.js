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
<tr bgcolor="${suiteBg}">
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
<title>Backend Test Report</title>
</head>

<body style="font-family:Arial;background:#e2e8f0;padding:25px">

<h1 style="margin-bottom:5px">Backend Test Report</h1>

<p style="color:#475569">
Project: MercoTrace | Framework: Spring Boot
</p>

<table border="1" cellpadding="10" cellspacing="0" style="background:white">

<tr bgcolor="#2563eb">
<td><font color="white"><b>Total Tests</b></font></td>
<td><font color="white"><b>Passed</b></font></td>
<td><font color="white"><b>Failed</b></font></td>
<td><font color="white"><b>Skipped</b></font></td>
<td><font color="white"><b>Execution Time</b></font></td>
</tr>

<tr>
<td align="center">${total.tests}</td>
<td align="center"><font color="#16a34a">${passed}</font></td>
<td align="center"><font color="#dc2626">${failed}</font></td>
<td align="center">${total.skipped}</td>
<td align="center">${timeStr}s</td>
</tr>

</table>

<br>

<table border="1" cellpadding="10" cellspacing="0" width="100%" style="background:white">

<tr bgcolor="#3b82f6">
<th><font color="white">Test</font></th>
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
