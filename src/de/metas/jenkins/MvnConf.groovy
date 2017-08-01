package de.metas.jenkins

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

	/**
	  * Creates a new instance.
		*
		* @param pomFile example 1: 'pom.xml'; example 2: 'de.metas.reactor/pom.xml'
		* @param settingsFile the settings.xml file
		* @param mvnRepoBaseURL exmaple 'https://repo.metasfresh.com'
		* @param mvnRepoName example 'gh2102-mf'
		*/
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
  pomFile=${pomFile},
  settingsFile=${settingsFile},
  resolveParams=${resolveParams},
  deployParam=${deployParam}
]""";
	}

	String getRepoURL()
	{
		return "${this.mvnRepoBaseURL}/content/repositories/${this.mvnRepoName}"
	}

	/**
	  * @return a string containing a number of "-Dtask-repo-.." parameters, used to resolve dependencies from the repos that we want to resolve from.<br>
		*		IMPORTANT: please note that these are not "real" maven paramters, but properties which we use in the settings.xml file that is provided by jenkins.
		*/
	String getResolveParams()
	{
		return "-Dtask-repo-id=${MF_MAVEN_REPO_ID} -Dtask-repo-name=\"${this.mvnRepoName}\" -Dtask-repo-url=\"${getRepoURL()}\"";
	}

	/**
	  * String containing a "-DaltDeploymentRepository=.." maven parameter, used to deploy dependencies to the repo which we want to deploy to
		*/
	String getDeployParam()
	{
		String mvnDeployRepoURL = "${this.mvnRepoBaseURL}/content/repositories/${this.mvnRepoName}-releases"
		return "-DaltDeploymentRepository=\"${MF_MAVEN_REPO_ID}::default::${mvnDeployRepoURL}\"";
	}

	MvnConf withPomFile(String pomFile)
	{
		return new MvnConf(pomFile,	this.settingsFile, this.mvnRepoBaseURL,	this.mvnRepoName)
	}
}
