#!/usr/bin/env node

import { readFileSync, mkdirSync, writeFileSync, existsSync } from "fs";
import { join } from "path";

const root = process.cwd();

const frontendJUnit = join(root, "client/test-results/junit.xml");
const backendReports = join(root, "server/target/surefire-reports");

const outDir = join(root, "reports");
const outFile = join(outDir, "executive-summary.html");

function parseFrontend() {
if (!existsSync(frontendJUnit)) return { tests: 0, failures: 0, time: 0 };

const xml = readFileSync(frontendJUnit, "utf8");

const m = xml.match(/tests="(\d+)"[^>]*failures="(\d+)"/);

return {
tests: m ? +m[1] : 0,
failures: m ? +m[2] : 0
};
}

function parseBackend() {
if (!existsSync(backendReports)) return { tests: 0, failures: 0 };

const files = require("fs").readdirSync(backendReports)
.filter(f => f.endsWith(".xml"));

let tests = 0;
let failures = 0;

for (const f of files) {
const xml = readFileSync(join(backendReports, f), "utf8");

```
const m = xml.match(/tests="(\d+)"[^>]*failures="(\d+)"/);

if (m) {
  tests += +m[1];
  failures += +m[2];
}
```

}

return { tests, failures };
}

const fe = parseFrontend();
const be = parseBackend();

const totalTests = fe.tests + be.tests;
const totalFailures = fe.failures + be.failures;
const passed = totalTests - totalFailures;
const passRate = totalTests ? ((passed / totalTests) * 100).toFixed(1) : 0;

const date = new Date().toLocaleString();

mkdirSync(outDir, { recursive: true });

const html = `

<html>

<head>
<title>MercoTrace Executive Summary</title>

<style>

body{
font-family:Arial;
background:#f1f5f9;
padding:30px;
}

h1{
margin-bottom:5px;
}

.subtitle{
color:#475569;
margin-bottom:25px;
}

table{
border-collapse:collapse;
width:700px;
background:white;
}

td,th{
border:1px solid #cbd5e1;
padding:12px;
text-align:center;
}

th{
background:#2563eb;
color:white;
}

.pass{
color:#16a34a;
font-weight:bold;
}

.fail{
color:#dc2626;
font-weight:bold;
}

.links a{
display:block;
margin:8px 0;
color:#2563eb;
text-decoration:none;
}

</style>

</head>

<body>

<h1>MercoTrace CI Executive Summary</h1>

<div class="subtitle">
Generated: ${date}
</div>

<table>

<tr>
<th>Metric</th>
<th>Value</th>
</tr>

<tr>
<td>Backend Tests</td>
<td>${be.tests}</td>
</tr>

<tr>
<td>Frontend Tests</td>
<td>${fe.tests}</td>
</tr>

<tr>
<td>Total Tests</td>
<td>${totalTests}</td>
</tr>

<tr>
<td>Passed</td>
<td class="pass">${passed}</td>
</tr>

<tr>
<td>Failed</td>
<td class="fail">${totalFailures}</td>
</tr>

<tr>
<td>Pass Rate</td>
<td>${passRate}%</td>
</tr>

</table>

<br>

<h2>Reports</h2>

<div class="links">

<a href="../server/target/surefire-reports-html/surefire.html">
Backend Test Report
</a>

<a href="../client/test-results/html/index.html">
Frontend Test Report
</a>

<a href="../server/target/site/jacoco/index.html">
Backend Coverage
</a>

<a href="../client/coverage/index.html">
Frontend Coverage
</a>

</div>

</body>
</html>
`;

writeFileSync(outFile, html);

console.log("Executive report generated:", outFile);
