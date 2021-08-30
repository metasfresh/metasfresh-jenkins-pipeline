package de.metas.jenkins

class MvnConf implements Serializable {
    final String MF_MAVEN_REPO_ID = "metasfresh-task-repo"


    /**
     * String containing the pom file, to be concatenated to a "--file .." parameter
     */
    final String pomFile

    /**
     * String containing the settings file, to be contatenated to a "--settings .." parameter
     */
    final String settingsFile

    final String mvnRepoName

    final String mvnResolveRepoBaseURL

    final String mvnDeployRepoBaseURL

    /**
     * Creates a new instance.
     *
     * @param pomFile example 1: 'pom.xml'; example 2: 'de.metas.reactor/pom.xml'
     * @param settingsFile the settings.xml file
     * @param mvnRepoName example 'gh2102-mf'
     * @param mvnResolveRepoBaseURL ; used for both the resolve and deployment related URLs exmaple 'https://repo.metasfresh.com'
     */
    MvnConf(
            String pomFile,
            String settingsFile,
            String mvnRepoName,
            String mvnRepoBaseURL
    ) {
        this.pomFile = pomFile
        this.settingsFile = settingsFile
        this.mvnRepoName = mvnRepoName
        this.mvnResolveRepoBaseURL = mvnRepoBaseURL
        this.mvnDeployRepoBaseURL = mvnRepoBaseURL
    }

    /**
     * Similar to the other constructor, but allows deployment and resolve repos to differ
     */
    MvnConf(
            String pomFile,
            String settingsFile,
            String mvnRepoName,
            String mvnResolveRepoBaseURL,
            String mvnDeployRepoBaseURL
    ) {
        this.pomFile = pomFile
        this.settingsFile = settingsFile
        this.mvnRepoName = mvnRepoName
        this.mvnResolveRepoBaseURL = mvnResolveRepoBaseURL
        this.mvnDeployRepoBaseURL = mvnDeployRepoBaseURL
    }

// This toString() method causes problems in the area of "CPS".
// Namely, our cucumber-tests are not executed and the message is
// "expected to call Script5.build but wound up catching de.metas.jenkins.MvnConf.toString; see: https://jenkins.io/redirect/pipeline-cps-method-mismatches/"
//    
//    String toString() {
//        return """MvnConf[
//  pomFile=${pomFile},
//  settingsFile=${settingsFile},
//  mvnRepoName=${mvnRepoName},
//  mvnResolveRepoBaseURL=${mvnResolveRepoBaseURL},
//  mvnDeployRepoBaseURL=${mvnDeployRepoBaseURL}
//]""";
//    }

    String getDeployRepoURL() {
        return "${this.mvnDeployRepoBaseURL}/repository/${this.mvnRepoName}-releases"
    }

    String getResolveRepoURL() {
        return "${this.mvnResolveRepoBaseURL}/repository/${this.mvnRepoName}"
    }

    /**
     * @return a string containing a number of "-Dtask-repo-.." parameters, used to resolve dependencies from the repos that we want to resolve from.<br>
     * 		IMPORTANT: please note that these are not "real" maven paramters, but properties which we use in the settings.xml file that is provided by jenkins.
     */
    String getResolveParams() {
        return "-Dtask-repo-id=${MF_MAVEN_REPO_ID} -Dtask-repo-name=\"${this.mvnRepoName}\" -Dtask-repo-url=\"${this.resolveRepoURL}\"";
    }

    /**
     * String containing a "-DaltDeploymentRepository=.." maven parameter, used to deploy dependencies to the repo which we want to deploy to.
     * Note that credentials are provided by jenkins "Config File Management" plugin
     */
    String getDeployParam() {
        return "-DretryFailedDeploymentCount=5 -DaltDeploymentRepository=\"${MF_MAVEN_REPO_ID}::default::${getDeployRepoURL()}\"";
    }

    MvnConf withPomFile(String pomFile) {
        return new MvnConf(pomFile, this.settingsFile, this.mvnRepoName, this.mvnResolveRepoBaseURL, this.mvnDeployRepoBaseURL)
    }
}
