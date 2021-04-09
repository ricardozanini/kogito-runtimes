kogitoRuntimesRepo = 'kogito-runtimes'
optaplannerRepo = 'optaplanner'
kogitoAppsRepo = 'kogito-apps'
kogitoExamplesRepo = 'kogito-examples'

void saveReports(boolean allowEmptyResults = false) {
    junit testResults: '**/target/surefire-reports/**/*.xml, **/target/failsafe-reports/**/*.xml', allowEmptyResults: allowEmptyResults
}

void checkoutRepo(String repo, String author, String branch, String targetBranch = '') {
    dir(repo) {
        if (targetBranch) {
            githubscm.checkoutIfExists(repo, author, branch, 'kiegroup', targetBranch, true)
        } else {
            checkout(githubscm.resolveRepository(repo, author, branch, false))
        }
    }
}

String getOptaplannerReleaseBranch(String branch) {
    String checkedBranch = branch
    String [] versionSplit = checkedBranch.split("\\.")
    if (versionSplit.length == 3
        && versionSplit[0].isNumber()
        && versionSplit[1].isNumber()
       && versionSplit[2] == 'x') {
        checkedBranch = "${Integer.parseInt(versionSplit[0]) + 7}.${versionSplit[1]}.x"
    } else {
      echo "Cannot parse branch as release branch so going further with current value: ${checkedBranch}"
    }
    return checkedBranch
}

void checkoutQuarkusRepo(boolean allowDefault = false) {
    checkoutRepo('quarkus', 'quarkusio', getQuarkusBranch(allowDefault))
}

void checkoutDroolsRepo() {
    checkoutRepo('drools', 'kiegroup', 'master')
}

void runQuickBuild(MavenCommand mvnCmd) {
    mvnCmd.withProperty('quickly')
            .run('clean install')
}

void runUnitTests(MavenCommand mvnCmd) {
    if (project == 'optaplanner') {
        mvnCmd.withProperty('enforcer.skip')
            .withProperty('formatter.skip')
            .withProperty('impsort.skip')
            .withProperty('revapi.skip')
    } else {
        mvnCmd.withProperty('quickTests')
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

void runSonarcloudAnalysis(MavenCommand mvnCmd) {
    mvnCmd.withOptions(['-e', '-nsu'])
            .withProfiles(['sonarcloud-analysis'])
            .run('validate')
}

void runIntegrationTests(MavenCommand mvnCmd, String profiles=[]) {
    String itFolder = "${project}-it${profiles ? '-' + profiles.join('-') : ''}"
    sh "cp -r ${project} ${itFolder}"

    try {
        mvnCmd.inDirectory(itFolder)
                .withProfiles(profiles)
                .run('verify')
    } catch (err) {
        currentBuild.currentResult = 'FAILURE'
    } finally {
        saveReports()
        cleanContainers()
    }
}

MavenCommand getMavenCommand(String project, boolean quarkusEnabled=false, boolean nativeEnabled = false) {
    mvnCmd = new MavenCommand(this, ['-fae'])
                .withSettingsXmlId('kogito_release_settings')
                .inDirectory(project)
    if (quarkusEnabled) {
        mvnCmd.withProperty('version.io.quarkus', '999-SNAPSHOT')
    }
    if (nativeEnabled) {
        mvnCmd.withProfiles(['native'])
            .withProperty('quarkus.native.container-build', true)
            .withProperty('quarkus.native.container-runtime', 'docker')
            .withProperty('quarkus.profile', 'native') // Added due to https://github.com/quarkusio/quarkus/issues/13341
    }
    return mvnCmd
}

String getQuarkusBranch(boolean allowDefault = false) {
    if (!env['QUARKUS_BRANCH'] && allowDefault) {
        return 'main'
    }
    return env['QUARKUS_BRANCH']
}

boolean isNative() {
    return env['NATIVE'] && env['NATIVE'].toBoolean()
}

void cleanContainers() {
    cloud.cleanContainersAndImages('docker')
}

void sendNotification(String jobName) {
    emailext body: "Kogito daily ${jobName} #${BUILD_NUMBER} was: ${currentBuild.currentResult}\nPlease look here: ${BUILD_URL}",
             subject: "[${params.BUILD_BRANCH_NAME}][d] ${jobName}",
             to: env.KOGITO_CI_EMAIL_TO
}
