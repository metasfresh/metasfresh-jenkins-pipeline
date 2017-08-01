package de.metas.jenkins;

class MvnConf implements Serializable
{
	final MF_MAVEN_REPO_ID = "metasfresh-task-repo";


	/**
	  * String containing the pom file, to be concatenated to a "--file .." parameter
	  */
	final String pomFile;

	/**
	  * String containing the settings file, to be contatenated to a "--settings .." parameter
		*/
	final String settingsFile;

	final String mvnRepoName;

	final String mvnRepoBaseURL;

	MvnConf(
			String pomFile,
			String settingsFile,
			String mvnRepoBaseURL,
			String mvnRepoName
			)
	{
		this.pomFile = pomFile
		this.settingsFile = settingsFile
		this.mvnRepoBaseURL = mvnRepoBaseURL
		this.mvnRepoName = mvnRepoName

	}

	String toString()
	{
		return """MvnConf[
\tpomFile=${pomFile},
\tsettingsFile=${settingsFile},
\tresolveParams=${resolveParams},
\tdeployParam=${deployParam}
]""";
	}

	/**
	  * @return a string containing a number of "-Dtask-repo-.." maven parameters, used to resolve dependencies from the repos that we want to resolve from
		*/
	String getResolveParams()
	{
		String mvnResolveRepoURL = "${this.mvnRepoBaseURL}/${this.mvnRepoName}"
		return "-Dtask-repo-id=${MF_MAVEN_REPO_ID} -Dtask-repo-name=\"${this.mvnRepoName}\" -Dtask-repo-url=\"${mvnResolveRepoURL}\"";
	}

	/**
	  * String containing a "-DaltDeploymentRepository=.." maven parameter, used to deploy dependencies to the repo which we want to deploy to
		*/
	String getDeployParam()
	{
		String mvnDeployRepoURL = "${this.mvnRepoBaseURL}/${this.mvnRepoName}-releases"
		return "-DaltDeploymentRepository=\"${MF_MAVEN_REPO_ID}::default::${mvnDeployRepoURL}\"";
	}
}
