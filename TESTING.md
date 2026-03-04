# MercoTrace — Testing, coverage and reports

## Backend (Java / Spring Boot)

### Run tests
```bash
cd server && ./mvnw -ntp test
```
- Unit tests: **Maven Surefire** (e.g. `*Test.java`, `*Tests.java`).
- For integration tests too: `./mvnw -ntp verify`.

### Coverage (JaCoCo)
- Generated when you run `./mvnw -ntp test` (unit tests) and `./mvnw -ntp verify` (with integration tests).
- **Report (HTML):** `server/target/site/jacoco/index.html`
- Open in a browser to see line/branch coverage.

### Test report (HTML)
- **Surefire report:** `server/target/surefire-reports-html/surefire.html`
- Generated in the `test` phase. Open in a browser for test results and failures.

### Attractive static HTML report (shareable)
- Run tests first, then from **repo root**:
  ```bash
  npm install
  npm run report:html:backend
  ```
- Opens as a **single file** — **`server/target/test-report.html`**. Double-click or share the file; no server needed. Summary cards (Passed/Failed/Skipped/Total/Duration), progress bar, and test list with status and duration.

### Allure report (local, dashboard-style)
- Run tests so Allure can collect results:
  ```bash
  cd server && ./mvnw -ntp test
  ```
- Results are written to `server/target/allure-results`.

**View in browser (no static file):**  
  ```bash
  # From repo root; requires Allure CLI (e.g. brew install allure on macOS)
  allure serve server/target/allure-results
  ```
  This starts a local server and opens the report (summary, charts, timeline, per-test details).

**Generate a static HTML folder to share:**  
  ```bash
  allure generate server/target/allure-results -o server/target/allure-report --clean
  ```
  The report is in `server/target/allure-report/`. **Do not open `index.html` directly** (file://) — the report loads JSON and browsers block that. To view or share it, serve the folder over HTTP:
  ```bash
  # From repo root (requires Node)
  npx serve server/target/allure-report
  ```
  Then open **http://localhost:3000** (or the URL shown). To share: zip `server/target/allure-report` and tell the recipient to run `npx serve` inside the unzipped folder and open the URL in a browser.

---

## Frontend (React / Vitest)

### Run tests
```bash
cd client && npm run test
```

### Coverage
```bash
cd client && npm run test:coverage
```
- **Report (HTML):** `client/coverage/index.html`
- Uses **@vitest/coverage-v8**. Also prints a text summary in the terminal and writes `lcov` for tools that support it.

### Test report (HTML + JUnit XML, for CI)
- When `CI=true`, Vitest writes:
  - **HTML report:** `client/test-results/html/index.html` — open in a browser for test results.
  - **JUnit XML:** `client/test-results/junit.xml` — for Jenkins test trends.
- In Jenkins, run: `CI=true npm run test:coverage` to produce coverage, HTML report, and JUnit XML.

### Attractive static HTML report (shareable)
- Run frontend tests with JUnit output (e.g. `CI=true npm run test:coverage` in `client`), then from **repo root**:
  ```bash
  npm run report:html:frontend
  ```
- Single file: **`client/test-results/test-report.html`**. Open or share it; no server needed. Same layout as backend (summary cards, progress bar, test list).

### Allure report (local, dashboard-style)
- Run tests so Allure can collect results:
  ```bash
  cd client && npm run test:allure
  ```
- Results are written to `client/allure-results`.
- **View in browser:** `cd client && npm run report:allure` — starts a server and opens the report (no global Allure install needed).
- **Generate a static HTML folder to share:**
  ```bash
  cd client && npm run report:allure:generate
  ```
  Report is in `client/allure-report/`. **Do not open `index.html` via file://** — serve it over HTTP:
  ```bash
  cd client && npm run report:allure:open
  ```
  Then open **http://localhost:3000**. To share: zip `client/allure-report` and tell the recipient to run `npx serve` inside the unzipped folder and open the URL.

---

## SAST reports (shareable with managers)

Static analysis security reports — single HTML files you can open in any browser or share via email. No server needed.

### Generate both reports
```bash
npm run report:sast:all
```

### Backend (SpotBugs)
```bash
npm run report:spotbugs:server
```
- **Output:** `server/target/spotbugs-report.html`
- **Tool:** SpotBugs 4.8 (Java bytecode analysis)
- **Contents:** Executive summary, severity breakdown, findings by type, detailed list with filter by severity

### Frontend (ESLint)
```bash
npm run report:sast:client
```
- **Output:** `client/test-results/sast-report.html`
- **Tool:** ESLint + eslint-plugin-security
- **Contents:** Executive summary, findings by rule, detailed list with filter by severity

Both reports include: project metadata, executive summary, High/Medium/Low/Total cards, severity bar, breakdown table, and filterable detailed findings. Suitable for sharing with managers or stakeholders.

---

## Summary

| Layer   | Coverage report (HTML)              | Test report (HTML / XML)                    | Attractive static report (shareable) | SAST report (shareable) | Allure report (local) |
|--------|-------------------------------------|---------------------------------------------|----------------------------------------|--------------------------|------------------------|
| Backend | `server/target/site/jacoco/index.html` | `server/target/surefire-reports-html/surefire.html` | `server/target/test-report.html` (from root: `npm run report:html:backend`) | `server/target/spotbugs-report.html` (`npm run report:spotbugs:server`) | `allure serve server/target/allure-results` |
| Frontend | `client/coverage/index.html`       | `client/test-results/html/index.html` (when `CI=true`) | `client/test-results/test-report.html` (from root: `npm run report:html:frontend`) | `client/test-results/sast-report.html` (`npm run report:sast:client`) | `cd client && npm run report:allure` |
