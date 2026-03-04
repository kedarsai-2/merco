#!/usr/bin/env node
/**
 * Generates a detailed, manager-ready static HTML report from SpotBugs XML.
 * Safe to open via file:// and to share (no server, no CORS).
 * Usage: node scripts/generate-spotbugs-report.js <spotbugsXml.xml> <output.html> [title]
 */

const fs = require('fs');
const path = require('path');
const { XMLParser } = require('fast-xml-parser');

const inputPath = process.argv[2];
const outputPath = process.argv[3];
const title = process.argv[4] || 'SpotBugs Security Report';

if (!inputPath || !outputPath) {
  console.error('Usage: node generate-spotbugs-report.js <spotbugsXml.xml> <output.html> [title]');
  process.exit(1);
}

if (!fs.existsSync(inputPath)) {
  console.error('Input file not found:', inputPath);
  process.exit(1);
}

const parser = new XMLParser({ ignoreAttributes: false, attributeNamePrefix: '@_' });
const xml = fs.readFileSync(inputPath, 'utf8');

let bugs = [];
let projectName = 'Project';
let analysisTimestamp = '';

try {
  const doc = parser.parse(xml);
  const collection = doc.BugCollection;
  if (!collection) {
    console.error('Invalid SpotBugs XML: no BugCollection');
    process.exit(1);
  }
  projectName = collection.Project?.['@_projectName'] || projectName;
  analysisTimestamp = collection['@_analysisTimestamp'] || '';
  const raw = collection.BugInstance;
  bugs = raw ? (Array.isArray(raw) ? raw : [raw]) : [];
} catch (e) {
  console.error('Parse error:', e.message);
  process.exit(1);
}

function getSourceLine(bug) {
  const sl = bug.SourceLine;
  if (Array.isArray(sl)) {
    const primary = sl.find((s) => s['@_primary'] === 'true') || sl[0];
    return primary;
  }
  return sl;
}

function getMessage(bug, key) {
  const m = bug[key];
  if (!m) return '';
  if (typeof m === 'string') return m;
  return m['#text'] || m['@_message'] || '';
}

const items = bugs.map((bug) => {
  const type = bug['@_type'] || 'UNKNOWN';
  const priority = parseInt(bug['@_priority'] || '2', 10);
  const category = bug['@_category'] || 'STYLE';
  const shortMsg = getMessage(bug, 'ShortMessage');
  const longMsg = getMessage(bug, 'LongMessage');
  const sl = getSourceLine(bug);
  const classname = sl?.['@_classname'] || bug.Class?.['@_classname'] || '';
  const sourcefile = sl?.['@_sourcefile'] || '';
  const start = sl?.['@_start'] || '';
  const end = sl?.['@_end'] || '';
  const loc = sourcefile ? (start && end ? `${sourcefile}:${start}-${end}` : `${sourcefile}:${start || '?'}`) : classname;

  let severity = 'medium';
  if (priority === 1) severity = 'high';
  else if (priority >= 3) severity = 'low';

  return {
    type,
    category,
    severity,
    shortMessage: shortMsg,
    longMessage: longMsg,
    classname,
    location: loc,
  };
});

const bySeverity = { high: 0, medium: 0, low: 0 };
const byType = {};
items.forEach((i) => {
  bySeverity[i.severity]++;
  byType[i.type] = (byType[i.type] || 0) + 1;
});

const typeBreakdown = Object.entries(byType).sort((a, b) => b[1] - a[1]);

function escapeHtml(s) {
  return String(s ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

const genDate = new Date().toLocaleString('en-IN', { dateStyle: 'full', timeStyle: 'short' });
const execSummary = bugs.length === 0
  ? 'No security or code quality findings were reported. The backend codebase passes SpotBugs analysis.'
  : `This report summarizes ${bugs.length} static analysis finding(s) from SpotBugs. ${bySeverity.high > 0 ? `${bySeverity.high} high-priority issue(s) require immediate attention. ` : ''}${bySeverity.medium > 0 ? `${bySeverity.medium} medium-priority finding(s) should be addressed in the next sprint. ` : ''}${bySeverity.low > 0 ? `${bySeverity.low} low-priority finding(s) can be scheduled for future iterations.` : ''}`;

const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>${escapeHtml(title)} – ${escapeHtml(projectName)}</title>
  <style>
    * { box-sizing: border-box; }
    body { font-family: 'Segoe UI', system-ui, -apple-system, sans-serif; margin: 0; padding: 24px; background: #f8fafc; color: #1e293b; line-height: 1.6; }
    .container { max-width: 1000px; margin: 0 auto; }
    .header { background: linear-gradient(135deg, #1e3a5f 0%, #0f172a 100%); color: #fff; padding: 28px 32px; border-radius: 12px; margin-bottom: 24px; }
    .header h1 { margin: 0 0 8px 0; font-size: 1.5rem; font-weight: 700; }
    .header .subtitle { opacity: 0.9; font-size: 0.9rem; }
    .meta-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; margin-top: 16px; font-size: 0.85rem; }
    .meta-item { background: rgba(255,255,255,0.1); padding: 10px 14px; border-radius: 8px; }
    .meta-item strong { display: block; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; opacity: 0.8; margin-bottom: 4px; }
    .packages-section { margin-top: 16px; padding: 14px 18px; background: rgba(255,255,255,0.1); border-radius: 8px; font-size: 0.85rem; }
    .packages-section strong { display: block; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; opacity: 0.9; margin-bottom: 8px; }
    .packages-section ul { margin: 0; padding-left: 20px; opacity: 0.95; }
    .packages-section li { margin: 4px 0; }
    .packages-section code { font-size: 0.8em; background: rgba(0,0,0,0.2); padding: 2px 6px; border-radius: 4px; }
    .exec-summary { background: #fff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 20px 24px; margin-bottom: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
    .exec-summary h2 { margin: 0 0 12px 0; font-size: 1rem; font-weight: 600; color: #334155; }
    .exec-summary p { margin: 0; font-size: 0.95rem; color: #475569; }
    .summary { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .card { background: #fff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 20px; text-align: center; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
    .card .value { font-size: 2rem; font-weight: 700; }
    .card .label { font-size: 0.8rem; color: #64748b; margin-top: 4px; }
    .card.high .value { color: #dc2626; }
    .card.medium .value { color: #d97706; }
    .card.low .value { color: #ca8a04; }
    .card.total .value { color: #2563eb; }
    .bar-wrap { height: 10px; background: #e2e8f0; border-radius: 5px; overflow: hidden; margin-bottom: 24px; }
    .bar { height: 100%; border-radius: 5px; }
    .bar.high { background: #dc2626; }
    .bar.medium { background: #d97706; }
    .bar.low { background: #ca8a04; }
    .section { background: #fff; border: 1px solid #e2e8f0; border-radius: 10px; margin-bottom: 24px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
    .section h2 { margin: 0; padding: 16px 20px; font-size: 1rem; font-weight: 600; background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
    .section-content { padding: 20px; }
    table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
    th, td { padding: 10px 14px; text-align: left; border-bottom: 1px solid #e2e8f0; }
    th { background: #f8fafc; font-weight: 600; color: #334155; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.03em; }
    tr:hover { background: #f8fafc; }
    .bug { padding: 16px 20px; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 12px; background: #fff; }
    .bug.high { border-left: 4px solid #dc2626; }
    .bug.medium { border-left: 4px solid #d97706; }
    .bug.low { border-left: 4px solid #ca8a04; }
    .bug .type { font-weight: 600; font-family: monospace; font-size: 0.85rem; color: #1e293b; }
    .bug .message { color: #475569; margin: 6px 0 4px 0; font-size: 0.9rem; }
    .bug .location { font-size: 0.8rem; color: #64748b; font-family: monospace; }
    .bug .classname { font-size: 0.8rem; color: #6366f1; margin-top: 4px; }
    .filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
    .filter-btn { padding: 8px 16px; border: 1px solid #e2e8f0; background: #fff; border-radius: 6px; cursor: pointer; font-size: 0.85rem; }
    .filter-btn.active { background: #2563eb; color: #fff; border-color: #2563eb; }
    .filter-btn:hover { background: #f1f5f9; }
    .filter-btn.active:hover { background: #1d4ed8; }
    .footer { font-size: 0.8rem; color: #94a3b8; margin-top: 24px; padding-top: 16px; border-top: 1px solid #e2e8f0; }
    @media print { body { background: #fff; } .header { -webkit-print-color-adjust: exact; print-color-adjust: exact; } .filter-bar { display: none; } }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>${escapeHtml(title)}</h1>
      <p class="subtitle">${escapeHtml(projectName)} · Backend (Java/Spring Boot)</p>
      <div class="meta-grid">
        <div class="meta-item"><strong>Project</strong>${escapeHtml(projectName)}</div>
        <div class="meta-item"><strong>Tool</strong>SpotBugs 4.8</div>
        <div class="meta-item"><strong>Scope</strong>Production code</div>
        <div class="meta-item"><strong>Generated</strong>${escapeHtml(genDate)}</div>
      </div>
      <div class="packages-section">
        <strong>Packages / plugins explored</strong>
        <ul>
          <li><code>com.github.spotbugs:spotbugs-maven-plugin</code> (4.8.3)</li>
          <li>SpotBugs core detectors (Malicious code, Correctness, Performance, Security, etc.)</li>
          <li>Scope: <code>src/main/java</code> (compiled bytecode)</li>
          <li>Exclude filter: <code>spotbugs-exclude.xml</code> (known false positives)</li>
        </ul>
      </div>
    </div>

    <div class="exec-summary">
      <h2>Executive Summary</h2>
      <p>${escapeHtml(execSummary)}</p>
    </div>

    <div class="summary">
      <div class="card high"><div class="value">${bySeverity.high}</div><div class="label">High</div></div>
      <div class="card medium"><div class="value">${bySeverity.medium}</div><div class="label">Medium</div></div>
      <div class="card low"><div class="value">${bySeverity.low}</div><div class="label">Low</div></div>
      <div class="card total"><div class="value">${bugs.length}</div><div class="label">Total</div></div>
    </div>
    <div class="bar-wrap">
      <div class="bar high" style="width: ${bugs.length ? (bySeverity.high / bugs.length) * 100 : 0}%"></div>
      <div class="bar medium" style="width: ${bugs.length ? (bySeverity.medium / bugs.length) * 100 : 0}%"></div>
      <div class="bar low" style="width: ${bugs.length ? (bySeverity.low / bugs.length) * 100 : 0}%"></div>
    </div>

    ${typeBreakdown.length > 0 ? `
    <div class="section">
      <h2>Findings by Type</h2>
      <div class="section-content">
        <table>
          <thead><tr><th>Bug Type</th><th>Count</th><th>%</th></tr></thead>
          <tbody>
            ${typeBreakdown.map(([t, c]) => `<tr><td><code>${escapeHtml(t)}</code></td><td>${c}</td><td>${((c / bugs.length) * 100).toFixed(1)}%</td></tr>`).join('')}
          </tbody>
        </table>
      </div>
    </div>
    ` : ''}

    <div class="section">
      <h2>Detailed Findings</h2>
      <div class="section-content">
        <div class="filter-bar">
          <button class="filter-btn active" data-sev="all">All (${bugs.length})</button>
          <button class="filter-btn" data-sev="high">High (${bySeverity.high})</button>
          <button class="filter-btn" data-sev="medium">Medium (${bySeverity.medium})</button>
          <button class="filter-btn" data-sev="low">Low (${bySeverity.low})</button>
        </div>
        <div id="list"></div>
      </div>
    </div>

    <p class="footer">Generated ${genDate} · SpotBugs static analysis · Single-file report – open in any browser or share via email. No server required.</p>
  </div>
  <script>
    function esc(s) {
      if (!s) return '';
      var d = document.createElement('div');
      d.textContent = s;
      return d.innerHTML;
    }
    var items = ${JSON.stringify(items)};
    var list = document.getElementById('list');
    function render(filter) {
      list.innerHTML = '';
      var filtered = filter === 'all' ? items : items.filter(function(b) { return b.severity === filter; });
      if (filtered.length === 0) {
        list.innerHTML = '<p style="color:#64748b;padding:20px;">No findings in this category.</p>';
        return;
      }
      filtered.forEach(function(b) {
        var el = document.createElement('div');
        el.className = 'bug ' + b.severity;
        el.innerHTML =
          '<span class="type">' + esc(b.type) + '</span>' +
          '<div class="message">' + esc(b.shortMessage) + '</div>' +
          (b.longMessage ? '<div class="message" style="font-size:0.85rem;color:#64748b;">' + esc(b.longMessage) + '</div>' : '') +
          '<div class="location">' + esc(b.location) + '</div>' +
          (b.classname ? '<div class="classname">' + esc(b.classname) + '</div>' : '');
        list.appendChild(el);
      });
    }
    render('all');
    document.querySelectorAll('.filter-btn').forEach(function(btn) {
      btn.onclick = function() {
        document.querySelectorAll('.filter-btn').forEach(function(b) { b.classList.remove('active'); });
        this.classList.add('active');
        render(this.getAttribute('data-sev'));
      };
    });
  </script>
</body>
</html>
`;

const outDir = path.dirname(outputPath);
if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}
fs.writeFileSync(outputPath, html, 'utf8');
console.log('Wrote', outputPath, '(', bugs.length, 'findings )');
