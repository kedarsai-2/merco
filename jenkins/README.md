# Jenkins setup for MercoTrace

This folder and the root `Jenkinsfile` define the CI pipeline for MercoTrace.

## Pipeline overview

| Stage | What it does |
|-------|----------------|
| Checkout | Clones the repository |
| Backend: Build & Test | `server`: Maven clean verify (compile + unit/integration tests) |
| Backend: Package | `server`: Maven package with `-Pprod` (skips tests), produces JAR |
| Frontend: Install & Lint | `client`: npm ci, npm run lint |
| Frontend: Test | `client`: CI=true npm run test:coverage — Vitest tests + coverage + JUnit XML |
| Frontend: Build | `client`: npm run build (Vite production build) |

On **success**, artifacts are archived (download from the build’s **Build Artifacts**).

**Test results and coverage in the Jenkins UI**

The pipeline publishes test results and HTML reports so you can open them from the job page:

| In Jenkins you get | What it is |
|--------------------|------------|
| **Test Result** (sidebar + trend) | Backend (Surefire) + frontend (JUnit) test counts and history |
| **Backend Test Report (HTML)** | Link to Surefire HTML report |
| **Backend Coverage (JaCoCo)** | Link to JaCoCo coverage report |
| **Frontend Test Report (HTML)** | Link to Vitest HTML test report |
| **Frontend Coverage** | Link to Vitest coverage report |

**Required plugins:** **JUnit** (usually built-in) and **HTML Publisher** (`published-html-reports`). Install from **Manage Jenkins → Plugins** if missing.

See root **TESTING.md** for local report paths and how to generate coverage/reports.

## Agent requirements

The Jenkins agent (controller or node) must have:

- **Java 21** (OpenJDK or Temurin). The pipeline uses `tools { jdk 'Java 21' }` and/or the **JAVA_21_HOME** parameter (see below).
- **Node.js 18+** (LTS recommended). `npm` must be on `PATH`.
- **Maven 3.6+** (optional if using the wrapper; the repo provides `server/mvnw`).

### Installing Java 21 on macOS (Homebrew)

```bash
brew install openjdk@21
```

Then get the path for the **JAVA_21_HOME** parameter (or for Jenkins Global Tool Configuration):

```bash
/usr/libexec/java_home -v 21
```

Example output: `/opt/homebrew/opt/openjdk@21` or `/opt/homebrew/Cellar/openjdk@21/21.x.x/libexec/openjdk.jdk/Contents/Home`. Use that path as **JAVA_21_HOME** in Build with Parameters if the Jenkins “Java 21” tool is not configured.

### Configuring Java 21 in Jenkins (optional)

The Jenkinsfile uses `tools { jdk 'Java 21' }`. To use it:

1. **Manage Jenkins** → **Global Tool Configuration** (or **Tools**).
2. **JDK** → **Add JDK**.
3. Name: **`Java 21`** (must match exactly).
4. Either **Install automatically** (e.g. Eclipse Temurin 21) or set **JAVA_HOME** to your JDK 21 path (e.g. from `brew install openjdk@21` and `/usr/libexec/java_home -v 21`).

If the tool is missing or invalid, set **JAVA_21_HOME** in Build with Parameters to your JDK 21 path.

### Using Jenkins “tools” (optional extra)

In **Manage Jenkins → Global Tool Configuration** (or in the pipeline):

1. **JDK**: Add a JDK 17 installation (e.g. “Java 17”).
2. **Node.js**: Add a Node.js 18+ installation (e.g. “Node 20”).
3. **Maven** (optional): Add Maven 3.9+ if you prefer not to use `./mvnw`.

Then in the `Jenkinsfile` you can add a `tools` block, for example:

```groovy
pipeline {
    agent any
    tools {
        jdk 'Java 17'
        nodejs 'Node 20'
    }
    // ... rest of pipeline
}
```

Adjust the tool names to match your Jenkins configuration.

### Docker agent (alternative)

To run the whole pipeline in a container with Java + Node:

```groovy
pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-17'
            args '-u root'
        }
    }
    // ...
}
```

Then install Node in that image (e.g. via a custom Dockerfile) or use a multi-stage/sidecar approach. The current `Jenkinsfile` uses `agent any` so it works with a preconfigured host or Docker agent that already has Java and Node.

## Creating the pipeline job (local script — no Git)

Use this when you want to **upload or paste the Jenkinsfile in Jenkins** instead of loading it from Git.

1. **New Item** → name (e.g. `mercotrace`) → **Pipeline** → OK.
2. **Pipeline** section:
   - **Definition**: **“Pipeline script”** (not “from SCM”).
   - In the script text area, paste the contents of the root **Jenkinsfile** (or use **“Load from file”** / upload if your Jenkins supports it).
3. Save.
4. **Important:** Use **“Build with Parameters”** (not “Build Now”). Set:
   - **REPO_URL**: your MercoTrace Git repo URL (e.g. `https://github.com/your-org/MercoTrace.git`, or `file:///path/to/MercoTrace` if Jenkins can read the path).
   - **BRANCH**: branch to build (e.g. `main` or `master`).
   - **JAVA_21_HOME**: leave empty to use the Jenkins “Java 21” tool (or auto-detect on macOS). If you see *“JAVA_HOME is not defined correctly”* or *“release version 21 not supported”*, install JDK 21 (`brew install openjdk@21`), then set this to the path from `/usr/libexec/java_home -v 21`.
5. Click **Build**.

If you run “Build Now” without setting **REPO_URL**, the pipeline will fail with a message that REPO_URL is required.

---

## Alternative: Pipeline script from SCM

If you prefer to store the Jenkinsfile in Git and have Jenkins load it from the repo:

1. **New Item** → name (e.g. `mercotrace`) → **Pipeline**.
2. **Pipeline** section:
   - **Definition**: “Pipeline script from SCM”.
   - **SCM**: Git.
   - **Repository URL**: your MercoTrace repo URL.
   - **Branch**: e.g. `*/main` or `*/master`.
   - **Script Path**: `Jenkinsfile` (root of repo).
3. Save and run **Build Now**. Leave **REPO_URL** empty in the script so the built-in **checkout scm** is used.

## Optional: Maven from tools

If you configure Maven in Jenkins and want to use it instead of the wrapper, in the “Backend” stages replace:

```groovy
sh './mvnw -ntp -B clean verify -DskipTests=false'
```

with:

```groovy
sh 'mvn -ntp -B clean verify -DskipTests=false'
```

and similarly for the package step. The repo’s `./mvnw` works without installing Maven globally.

## Troubleshooting

- **Backend tests fail**: Ensure Java 17 and enough heap (e.g. `MAVEN_OPTS=-Xmx1G`). For integration tests, PostgreSQL/Redis may be required via Docker or services.
- **Frontend lint/test fail**: Ensure Node 18+ and a recent npm; run `npm ci` in `client` to match lockfile.
- **Out of memory**: Increase `MAVEN_OPTS` or Jenkins JVM heap for the agent.
