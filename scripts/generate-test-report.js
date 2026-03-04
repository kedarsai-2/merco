#!/usr/bin/env node
/**
 * Generates a single, self-contained static HTML test report from JUnit XML.
 * Safe to open via file:// and to share (no server, no CORS).
 * Usage: node scripts/generate-test-report.js <input-path> <output.html> [title]
 *   input-path: path to a single .xml file or a directory containing *.xml
 */

const fs = require('fs');
const path = require('path');
const { XMLParser } = require('fast-xml-parser');

const inputPath = process.argv[2];
const outputPath = process.argv[3];
const title = process.argv[4] || 'Test Report';

if (!inputPath || !outputPath) {
  console.error('Usage: node generate-test-report.js <input-path> <output.html> [title]');
  process.exit(1);
}

function readXmlFiles(inputPath) {
  const stat = fs.statSync(inputPath);
  if (stat.isFile()) {
    return [fs.readFileSync(inputPath, 'utf8')];
  }
  if (stat.isDirectory()) {
    const files = fs.readdirSync(inputPath)
      .filter((f) => f.endsWith('.xml'))
      .map((f) => fs.readFileSync(path.join(inputPath, f), 'utf8'));
    return files;
  }
  return [];
}

const parser = new XMLParser({ ignoreAttributes: false, attributeNamePrefix: '@_' });
const xmls = readXmlFiles(inputPath);

const cases = [];
let total = 0;
let failures = 0;
let errors = 0;
let skipped = 0;
let totalTime = 0;

for (const xml of xmls) {
  try {
    const doc = parser.parse(xml);
    const suites = doc.testsuites
      ? (Array.isArray(doc.testsuites.testsuite) ? doc.testsuites.testsuite : [doc.testsuites.testsuite])
      : doc.testsuite
        ? (Array.isArray(doc.testsuite) ? doc.testsuite : [doc.testsuite])
        : [];
    for (const suite of suites) {
      if (!suite) continue;
      const suiteName = suite['@_name'] || suite['@_name'] || 'Suite';
      const suiteTime = parseFloat(suite['@_time'] || 0);
      totalTime += suiteTime;
      const rawCases = suite.testcase;
      const list = rawCases ? (Array.isArray(rawCases) ? rawCases : [rawCases]) : [];
      for (const tc of list) {
        if (!tc) continue;
        total++;
        const name = tc['@_name'] || tc['@_name'] || 'Unknown';
        const classname = tc['@_classname'] || tc['@_classname'] || suiteName;
        const time = parseFloat(tc['@_time'] || 0);
        let status = 'passed';
        let message = '';
        if (tc.failure) {
          failures++;
          status = 'failed';
          const f = tc.failure;
          message = (f['@_message'] || f['#text'] || '').toString().trim().split('\n')[0];
        } else if (tc.error) {
          errors++;
          status = 'error';
          const e = tc.error;
          message = (e['@_message'] || e['#text'] || '').toString().trim().split('\n')[0];
        } else if (tc.skipped) {
          skipped++;
          status = 'skipped';
          const s = tc.skipped;
          message = (s['@_message'] || s['#text'] || '').toString().trim();
        }
        cases.push({ name, classname, time, status, message });
      }
    }
  } catch (e) {
    console.warn('Parse warning:', e.message);
  }
}

const passed = total - failures - errors - skipped;

const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>${escapeHtml(title)}</title>
  <style>
    * { box-sizing: border-box; }
    body {
      font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
      margin: 0;
      padding: 24px;
      background: linear-gradient(160deg, #0f0f14 0%, #1a1a24 100%);
      min-height: 100vh;
      color: #e4e4e7;
    }
    .container { max-width: 960px; margin: 0 auto; }
    h1 {
      font-size: 1.75rem;
      font-weight: 700;
      margin: 0 0 24px 0;
      color: #fafafa;
    }
    .summary {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 16px;
      margin-bottom: 28px;
    }
    .card {
      background: rgba(255,255,255,0.06);
      border: 1px solid rgba(255,255,255,0.08);
      border-radius: 12px;
      padding: 18px 20px;
      text-align: center;
    }
    .card .value { font-size: 1.75rem; font-weight: 700; }
    .card .label { font-size: 0.8rem; color: #a1a1aa; margin-top: 4px; }
    .card.passed .value { color: #22c55e; }
    .card.failed .value { color: #ef4444; }
    .card.skipped .value { color: #eab308; }
    .card.total .value { color: #3b82f6; }
    .card.time .value { color: #a78bfa; font-size: 1.35rem; }
    .bar-wrap {
      height: 8px;
      background: rgba(255,255,255,0.08);
      border-radius: 4px;
      overflow: hidden;
      margin-bottom: 28px;
    }
    .bar {
      height: 100%;
      border-radius: 4px;
      transition: width 0.4s ease;
    }
    .bar.passed { background: #22c55e; }
    .bar.failed { background: #ef4444; }
    .bar.skipped { background: #eab308; }
    .tests h2 {
      font-size: 1rem;
      font-weight: 600;
      margin: 0 0 12px 0;
      color: #d4d4d8;
    }
    .test {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      background: rgba(255,255,255,0.04);
      border: 1px solid rgba(255,255,255,0.06);
      border-radius: 8px;
      margin-bottom: 8px;
      font-size: 0.9rem;
    }
    .test .status {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      flex-shrink: 0;
    }
    .test .status.passed { background: #22c55e; }
    .test .status.failed { background: #ef4444; }
    .test .status.error { background: #dc2626; }
    .test .status.skipped { background: #eab308; }
    .test .name { flex: 1; color: #fafafa; }
    .test .classname { color: #71717a; font-size: 0.8rem; }
    .test .time { color: #a78bfa; font-variant-numeric: tabular-nums; }
    .test .message { font-size: 0.8rem; color: #f87171; margin-top: 4px; }
    .meta { font-size: 0.8rem; color: #71717a; margin-top: 24px; }
  </style>
</head>
<body>
  <div class="container">
    <h1>${escapeHtml(title)}</h1>
    <div class="summary">
      <div class="card passed"><div class="value" id="v-passed">${passed}</div><div class="label">Passed</div></div>
      <div class="card failed"><div class="value" id="v-failed">${failures + errors}</div><div class="label">Failed</div></div>
      <div class="card skipped"><div class="value" id="v-skipped">${skipped}</div><div class="label">Skipped</div></div>
      <div class="card total"><div class="value" id="v-total">${total}</div><div class="label">Total</div></div>
      <div class="card time"><div class="value" id="v-time">${totalTime.toFixed(2)}s</div><div class="label">Duration</div></div>
    </div>
    <div class="bar-wrap">
      <div class="bar passed" style="width: ${total ? (passed / total) * 100 : 0}%"></div>
      <div class="bar failed" style="width: ${total ? ((failures + errors) / total) * 100 : 0}%"></div>
      <div class="bar skipped" style="width: ${total ? (skipped / total) * 100 : 0}%"></div>
    </div>
    <div class="tests">
      <h2>Test cases</h2>
      <div id="list"></div>
    </div>
    <p class="meta">Generated at ${new Date().toISOString()} · Open this file in any browser to view (no server needed).</p>
  </div>
  <script>
    function escapeHtml(s) {
      if (!s) return '';
      var d = document.createElement('div');
      d.textContent = s;
      return d.innerHTML;
    }
    var cases = ${JSON.stringify(cases)};
    var list = document.getElementById('list');
    cases.forEach(function(c) {
      var el = document.createElement('div');
      el.className = 'test';
      el.innerHTML =
        '<span class="status ' + c.status + '"></span>' +
        '<span class="name">' + escapeHtml(c.name) + '</span>' +
        '<span class="classname">' + escapeHtml(c.classname) + '</span>' +
        '<span class="time">' + c.time.toFixed(2) + 's</span>' +
        (c.message ? '<span class="message" style="display:block;width:100%;">' + escapeHtml(c.message) + '</span>' : '');
      list.appendChild(el);
    });
  </script>
</body>
</html>
`;

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

const outDir = path.dirname(outputPath);
if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}
fs.writeFileSync(outputPath, html, 'utf8');
console.log('Wrote', outputPath, '(', total, 'tests )');
