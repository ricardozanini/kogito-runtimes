@Library('jenkins-pipeline-shared-libraries')_

import org.kie.jenkins.MavenCommand

changeAuthor = env.ghprbPullAuthorLogin ?: CHANGE_AUTHOR
changeBranch = env.ghprbSourceBranch ?: CHANGE_BRANCH
changeTarget = env.ghprbTargetBranch ?: CHANGE_TARGET

pipeline {
    agent {
        label 'kie-rhel7 && kie-mem16g'
    }
    tools {
        maven 'kie-maven-3.6.2'
        jdk 'kie-jdk11'
    }
    options {
        timeout(time: 600, unit: 'MINUTES')
    }
    environment {
        SONARCLOUD_TOKEN = credentials('SONARCLOUD_TOKEN')
        MAVEN_OPTS = '-Xms1024m -Xmx4g'
    }
    stages {
        stage('Initialize') {
            steps {
                script {
                    mailer.buildLogScriptPR()

                    checkoutRepo('kogito-runtimes')
                    checkoutOptaplannerRepo()
                    checkoutRepo('kogito-apps')
                    checkoutRepo('kogito-examples')
                }
            }
        }
        stage('Build quarkus') {
            when {
                expression { return getQuarkusBranch() }
            }
            steps {
                script {
                    checkoutQuarkusRepo()
                    getMavenCommand('quarkus', false)
                        .withProperty('quickly')
                        .run('clean install')
                }
            }
        }
        stage('Build&Test Runtimes') {
            steps {
                script {
                    runUnitTests('kogito-runtimes', { mvnCmd -> return isNormalPRCheck() ? mvnCmd.withProfiles(['run-code-coverage']) : mvnCmd })
                }
            }
        }
        stage('Analyze Runtimes by SonarCloud') {
            when {
                expression { isNormalPRCheck() }
            }
            steps {
                script {
                    getMavenCommand('kogito-runtimes')
                            .withOptions(['-e', '-nsu'])
                            .withProfiles(['sonarcloud-analysis'])
                            .run('validate')
                }
            }
            post {
                cleanup {
                    script {
                        cleanContainers()
                    }
                }
            }
        }
        stage('Build&Test OptaPlanner') {
            steps {
                script {
                    runUnitTests('optaplanner')
                }
            }
        }
        stage('Build&Test Apps') {
            steps {
                script {
                    runUnitTests('kogito-apps')
                }
            }
        }
        stage('Build&Test Examples') {
            steps {
                script {
                    runUnitTests('kogito-examples')
                }
            }
        }

        stage('Run Runtimes integration-tests') {
            steps {
                script {
                    runIntegrationTests('kogito-runtimes')
                }
            }
        }

        stage('Run Runtimes integration-tests with persistence') {
            steps {
                script {
                    runIntegrationTests('kogito-runtimes', ['persistence'])
                }
            }
        }

        stage('Run Apps integration-tests') {
            steps {
                script {
                    runIntegrationTests('kogito-apps')
                }
            }
        }

        stage('Run Apps integration-tests with persistence') {
            steps {
                script {
                    runIntegrationTests('kogito-apps', ['persistence'])
                }
            }
        }

        stage('Run Apps integration-tests with events') {
            steps {
                script {
                    runIntegrationTests('kogito-apps', ['events'])
                }
            }
        }

        stage('Run Examples integration-tests') {
            steps {
                script {
                    runIntegrationTests('kogito-examples')
                }
            }
        }

        stage('Run Examples integration-tests with persistence') {
            steps {
                script {
                    runIntegrationTests('kogito-examples', ['persistence'])
                }
            }
        }

        stage('Run Examples integration-tests with events') {
            steps {
                script {
                    runIntegrationTests('kogito-examples', ['events'])
                }
            }
        }
    }
    post {
        always {
            script {
                sh '$WORKSPACE/trace.sh'
            }
        }
        failure {
            script {
                mailer.sendEmail_failedPR()
            }
        }
        unstable {
            script {
                mailer.sendEmail_unstablePR()
            }
        }
        fixed {
            script {
                mailer.sendEmail_fixedPR()
            }
        }
        cleanup {
            script {
                util.cleanNode('docker')
            }
        }
    }
}

void saveReports() {
    junit testResults: '**/target/surefire-reports/**/*.xml, **/target/failsafe-reports/**/*.xml', allowEmptyResults: false
}

void checkoutRepo(String repo, String dirName=repo) {
    dir(dirName) {
        githubscm.checkoutIfExists(repo, changeAuthor, changeBranch, 'kiegroup', changeTarget, true)
    }
}

void checkoutQuarkusRepo() {
    dir('quarkus') {
        checkout(githubscm.resolveRepository('quarkus', 'quarkusio', getQuarkusBranch(), false))
    }
}

void checkoutOptaplannerRepo() {
    String targetBranch = changeTarget
    String [] versionSplit = targetBranch.split("\\.")
    if (versionSplit.length == 3
        && versionSplit[0].isNumber()
        && versionSplit[1].isNumber()
       && versionSplit[2] == 'x') {
        targetBranch = "${Integer.parseInt(versionSplit[0]) + 7}.${versionSplit[1]}.x"
    } else {
        echo "Cannot parse changeTarget as release branch so going further with current value: ${changeTarget}"
       }
    dir('optaplanner') {
        githubscm.checkoutIfExists('optaplanner', changeAuthor, changeBranch, 'kiegroup', targetBranch, true)
    }
}

MavenCommand getMavenCommand(String directory, boolean addQuarkusVersion=true, boolean canNative = false) {
    mvnCmd = new MavenCommand(this, ['-fae'])
                .withSettingsXmlId('kogito_release_settings')
                // add timestamp to Maven logs
                .withOptions(['-Dorg.slf4j.simpleLogger.showDateTime=true', '-Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss,SSS'])
                .inDirectory(directory)
    if (addQuarkusVersion && getQuarkusBranch()) {
        mvnCmd.withProperty('version.io.quarkus', '999-SNAPSHOT')
    }
    if (canNative && isNative()) {
        mvnCmd.withProfiles(['native'])
            .withProperty('quarkus.native.container-build', true)
            .withProperty('quarkus.native.container-runtime', 'docker')
            .withProperty('quarkus.profile', 'native') // Added due to https://github.com/quarkusio/quarkus/issues/13341
    }
    return mvnCmd
}

void runUnitTests(String project, Closure mvnCmdModifier = null) {
    mvnCmd = getMavenCommand(project, true, true)

    if (project == 'optaplanner') {
        mvnCmd.withProperty('enforcer.skip')
            .withProperty('formatter.skip')
            .withProperty('impsort.skip')
            .withProperty('revapi.skip')
    } else {
        mvnCmd.withProperty('quickTests')
    }
    if (mvnCmdModifier) {
        mvnCmd = mvnCmdModifier(mvnCmd)
    }

    try {
        mvnCmd.run('clean install')
    } catch (err) {
        currentBuild.currentResult = 'FAILURE'
    } finally {
        saveReports()
        cleanContainers()
    }
}

void runIntegrationTests(String project, String profiles=[], Closure mvnCmdModifier = null) {
    String itFolder = "${project}-it${profiles ? '-' + profiles.join('-') : ''}"
    sh "cp -r ${project} ${itFolder}"

    mvnCmd = getMavenCommand(itFolder, true, true)
        .withProfiles(profiles)

    if (mvnCmdModifier) {
        mvnCmd = mvnCmdModifier(mvnCmd)
    }

    try {
        mvnCmd.run('verify')
    } catch (err) {
        currentBuild.currentResult = 'FAILURE'
    } finally {
        saveReports()
        cleanContainers()
    }
}

void cleanContainers() {
    cloud.cleanContainersAndImages('docker')
}

String getQuarkusBranch() {
    return env['QUARKUS_BRANCH']
}

boolean isNative() {
    return env['NATIVE'] && env['NATIVE'].toBoolean()
}

boolean isNormalPRCheck() {
    return !(getQuarkusBranch() || isNative())
}
