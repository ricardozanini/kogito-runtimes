@Library('jenkins-pipeline-shared-libraries')_

import org.kie.jenkins.MavenCommand

changeAuthor = env.ghprbPullAuthorLogin ?: CHANGE_AUTHOR
changeBranch = env.ghprbSourceBranch ?: CHANGE_BRANCH
changeTarget = env.ghprbTargetBranch ?: CHANGE_TARGET

helper = null

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
        timestamps()
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

                    helper = load '.jenkins/scripts/helper.groovy'

                    checkoutRepo(kogitoRuntimesRepo)
                    checkoutRepo(optaplannerRepo)
                    checkoutRepo(kogitoAppsRepo)
                    checkoutRepo(kogitoExamplesRepo)
                }
            }
        }
        stage('Build quarkus') {
            when {
                expression { return helper.getQuarkusBranch() }
            }
            steps {
                script {
                    helper.checkoutQuarkusRepo()
                    helper.runQuickBuild(getMavenCommand('quarkus', false))
                }
            }
        }
        stage('Build&Test Runtimes') {
            steps {
                script {
                    helper.runUnitTests(mvnCmd)

                    if (isNormalPRCheck()) {
                        helper.runUnitTests(getMavenCommand(kogitoRuntimesRepo).withProfiles(['run-code-coverage']))
                        helper.runSonarcloudAnalysis(getMavenCommand(kogitoRuntimesRepo))
                    } else {
                        helper.runUnitTests(getMavenCommand(kogitoRuntimesRepo))
                    }
                }
            }
        }
        stage('Build&Test OptaPlanner') {
            steps {
                script {
                    helper.runUnitTests(getMavenCommand(optaplannerRepo))
                }
            }
        }
        stage('Build&Test Apps') {
            steps {
                script {
                    helper.runUnitTests(getMavenCommand(kogitoAppsRepo))
                }
            }
        }
        stage('Build&Test Examples') {
            steps {
                script {
                    helper.runUnitTests(getMavenCommand(kogitoExamplesRepo))
                }
            }
        }

        stage('Run Runtimes integration-tests') {
            steps {
                script {
                    helper.runIntegrationTests(getMavenCommand(kogitoRuntimesRepo))
                }
            }
        }

        stage('Run Runtimes integration-tests with persistence') {
            steps {
                script {
                    helper.runIntegrationTests(getMavenCommand(kogitoRuntimesRepo), ['persistence'])
                }
            }
        }

        stage('Run Apps integration-tests') {
            steps {
                script {
                    helper.runIntegrationTests(getMavenCommand(kogitoAppsRepo))
                }
            }
        }

        stage('Run Apps integration-tests with persistence') {
            steps {
                script {
                    helper.runIntegrationTests(getMavenCommand(kogitoAppsRepo), ['persistence'])
                }
            }
        }

        stage('Run Apps integration-tests with events') {
            steps {
                script {
                    helper.runIntegrationTests(getMavenCommand(kogitoAppsRepo), ['events'])
                }
            }
        }

        stage('Run Examples integration-tests') {
            steps {
                script {
                    helper.runIntegrationTests(getMavenCommand(kogitoExamplesRepo))
                }
            }
        }

        stage('Run Examples integration-tests with persistence') {
            steps {
                script {
                    helper.runIntegrationTests(getMavenCommand(kogitoExamplesRepo), ['persistence'])
                }
            }
        }

        stage('Run Examples integration-tests with events') {
            steps {
                script {
                    helper.runIntegrationTests(getMavenCommand(kogitoExamplesRepo), ['events'])
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

void checkoutRepo(String repo) {
    if (repo == optaplannerRepo) {
        helper.checkoutRepo(repo, changeAuthor, changeBranch, helper.getOptaplannerReleaseBranch(changeTarget))
    } else {
        helper.checkoutRepo(repo, changeAuthor, changeBranch, changeTarget)
    }
}

void checkoutOptaplannerRepo() {
    helper.checkoutOptaplannerRepo(changeAuthor, changeBranch, changeTarget)
}

MavenCommand getMavenCommand(String directory, boolean addQuarkusVersion=true, boolean canNative = false) {
    return helper.getMavenCommand(directory, addQuarkusVersion && helper.getQuarkusBranch(), canNative && helper.isNative())
}

boolean isNormalPRCheck() {
    return !(helper.getQuarkusBranch() || helper.isNative())
}
