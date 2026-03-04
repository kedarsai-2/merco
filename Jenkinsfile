// MercoTrace — Jenkins Pipeline
// Builds backend (Spring Boot) and frontend (Vite/React), runs tests, archives artifacts.
// Fetches from https://github.com/kedarsai-2/merco.git by default. Override REPO_URL/BRANCH if needed.
// SonarQube: Configure server in Manage Jenkins → SonarQube servers. Add token. Set SKIP_SONAR=true to skip.

pipeline {
    agent any

    parameters {
        string(name: 'REPO_URL', defaultValue: 'https://github.com/kedarsai-2/merco.git', description: 'Git repo URL to fetch code and tests from')
        string(name: 'BRANCH', defaultValue: 'main', description: 'Branch to build (e.g. main, master)')
        string(name: 'JAVA_21_HOME', defaultValue: '/opt/homebrew/opt/openjdk@21', description: 'Path to JDK 21. Homebrew: /opt/homebrew/opt/openjdk@21')
        string(name: 'NODE_HOME', defaultValue: '/opt/homebrew', description: 'Path to Node.js (e.g. /opt/homebrew, /usr/local). Required for frontend.')
        string(name: 'SONAR_SERVER', defaultValue: 'SonarQube', description: 'Jenkins SonarQube server name. Configure in Manage Jenkins → SonarQube servers.')
        booleanParam(name: 'SKIP_SONAR', defaultValue: false, description: 'Skip SonarQube analysis (e.g. when SonarQube is not configured)')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        // Override in Jenkins if needed (e.g. JAVA_HOME, MAVEN_OPTS)
        MAVEN_OPTS = '-Djava.security.egd=file:/dev/./urandom -Xmx1G'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    def repoUrl = params.REPO_URL?.trim()
                    if (!repoUrl) {
                        error 'REPO_URL is required. Run "Build with Parameters" and set REPO_URL (default: https://github.com/kedarsai-2/merco.git).'
                    }
                    checkout([$class: 'GitSCM', branches: [[name: "*/${params.BRANCH}"]], userRemoteConfigs: [[url: repoUrl]]])
                }
            }
        }

        stage('Backend: Build & Test') {
            steps {
                dir('server') {
                    script {
                        def jhome = params.JAVA_21_HOME?.trim()
                        if (jhome) {
                            withEnv(["JAVA_HOME=${jhome}"]) {
                                sh 'echo "Using JAVA_HOME=$JAVA_HOME" && java -version && ./mvnw -ntp -B clean verify -DskipTests=false -DskipITs'
                            }
                        } else {
                            sh '''#!/bin/bash
                            if [ ! -d "${JAVA_HOME}" ] && [ -x "/usr/libexec/java_home" ]; then
                              export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null) || export JAVA_HOME=$(/usr/libexec/java_home -v 23 2>/dev/null) || true
                            fi
                            if [ ! -d "${JAVA_HOME}" ]; then
                              echo "ERROR: JAVA_HOME not set or invalid. Set JAVA_21_HOME in Build with Parameters to your JDK 21/23 path (e.g. /usr/libexec/java_home -v 23)."
                              exit 1
                            fi
                            echo "Using JAVA_HOME=$JAVA_HOME"
                            java -version
                            ./mvnw -ntp -B clean verify -DskipTests=false -DskipITs
                            '''
                        }
                    }
                }
            }
            post {
                success {
                    echo 'Backend build and tests passed.'
                }
                failure {
                    echo 'Backend build or tests failed.'
                }
            }
        }

        stage('Backend: Package') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                dir('server') {
                    script {
                        def jhome = params.JAVA_21_HOME?.trim()
                        if (jhome) {
                            withEnv(["JAVA_HOME=${jhome}"]) {
                                sh './mvnw -ntp -B package -DskipTests -Pprod'
                            }
                        } else {
                            sh '''#!/bin/bash
                            if [ ! -d "${JAVA_HOME}" ] && [ -x "/usr/libexec/java_home" ]; then
                              export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null) || true
                            fi
                            [ -d "${JAVA_HOME}" ] || { echo "ERROR: JAVA_HOME not set or invalid."; exit 1; }
                            ./mvnw -ntp -B package -DskipTests -Pprod
                            '''
                        }
                    }
                }
            }
        }

        stage('Frontend: Install & Lint') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                dir('client') {
                    script {
                        def nodeHome = params.NODE_HOME?.trim()
                        def pathAdd = nodeHome ? "export PATH=\"${nodeHome}/bin:\$PATH\" && " : ''
                        sh "${pathAdd}npm ci --no-audit --prefer-offline"
                        sh "${pathAdd}npm run lint"
                    }
                }
            }
        }

        stage('Frontend: Test') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    def pathAdd = params.NODE_HOME?.trim() ? "export PATH=\"${params.NODE_HOME.trim()}/bin:\$PATH\" && " : ''
                    sh "${pathAdd}node client/scripts/generate-backend-test-report-html.js"
                }
                dir('client') {
                    script {
                        def pathAdd = params.NODE_HOME?.trim() ? "export PATH=\"${params.NODE_HOME.trim()}/bin:\$PATH\" && " : ''
                        sh 'mkdir -p test-results/html'
                        sh "${pathAdd}CI=true npm run test:coverage"
                        sh 'test -f test-results/html/index.html && wc -c test-results/html/index.html || (echo "ERROR: Frontend test report not generated"; exit 1)'
                    }
                }
            }
        }

        stage('Frontend: Build') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                dir('client') {
                    script {
                        def pathAdd = params.NODE_HOME?.trim() ? "export PATH=\"${params.NODE_HOME.trim()}/bin:\$PATH\" && " : ''
                        sh "${pathAdd}npm run build"
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            when {
                expression { (currentBuild.result == null || currentBuild.result == 'SUCCESS') && !params.SKIP_SONAR }
            }
            steps {
                dir('server') {
                    script {
                        def jhome = params.JAVA_21_HOME?.trim()
                        def sonarServer = params.SONAR_SERVER?.trim() ?: 'SonarQube'
                        def runSonar = {
                            withSonarQubeEnv(sonarServer) {
                                if (jhome) {
                                    withEnv(["JAVA_HOME=${jhome}"]) {
                                        sh './mvnw -ntp -B sonar:sonar'
                                    }
                                } else {
                                    sh './mvnw -ntp -B sonar:sonar'
                                }
                            }
                        }
                        runSonar()
                    }
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'server/target/*.jar', fingerprint: true, allowEmptyArchive: false
            archiveArtifacts artifacts: 'client/dist/**/*', fingerprint: true, allowEmptyArchive: true
            archiveArtifacts artifacts: 'server/target/surefire-reports-html/**/*', fingerprint: true, allowEmptyArchive: true
            archiveArtifacts artifacts: 'server/target/site/jacoco/**/*', fingerprint: true, allowEmptyArchive: true
            archiveArtifacts artifacts: 'client/coverage/**/*', fingerprint: true, allowEmptyArchive: true
            archiveArtifacts artifacts: 'client/test-results/**/*', fingerprint: true, allowEmptyArchive: true
            echo 'MercoTrace pipeline completed successfully.'
        }
        failure {
            echo 'MercoTrace pipeline failed.'
        }
        unstable {
            echo 'MercoTrace pipeline has unstable stages.'
        }
        always {
            echo 'Pipeline finished. Consider using Workspace Cleanup Plugin for node_modules/target cleanup.'
            // Publish JUnit test results (Backend + Frontend) — visible in Jenkins "Test Result" and trends
            junit allowEmptyResults: true, testResults: 'server/target/surefire-reports/*.xml'
            junit allowEmptyResults: true, testResults: 'client/test-results/junit.xml'
            // Publish HTML reports — visible as links in the job sidebar (requires "HTML Publisher" plugin)
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'server/target/surefire-reports-html', reportFiles: 'surefire.html', reportName: 'Backend Test Report (HTML)'])
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'server/target/site/jacoco', reportFiles: 'index.html', reportName: 'Backend Coverage (JaCoCo)'])
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'client/test-results/html', reportFiles: 'index.html', reportName: 'Frontend Test Report (HTML)'])
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'client/coverage', reportFiles: 'index.html', reportName: 'Frontend Coverage'])
            sh 'node scripts/generate-executive-report.js 2>/dev/null || true'
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'reports', reportFiles: 'executive-summary.html', reportName: 'Executive Summary'])
        }
    }
}
