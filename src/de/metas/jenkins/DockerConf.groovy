package de.metas.jenkins

class DockerConf implements Serializable
{
	/**
	  * Mandatory. Examples: metasfresh-app, metasfresh-material-dispo.
		* This string will be made docker-compliant if necessary. Also, <code>metasfresh/</code> will be prepended
		*/ 
	final String artifactName

	/**
	  * Mandatory: the branch we are building a docker image for. Will go into the tag the images is pushed with.
		* This string will be made docker-compliant if necessary.
		*/ 
	final String branchName

	/**
	  * Mandatory: the build version we are building a docker image for. Will go into the tag the images is pushed with.
		* This string will be made docker-compliant if necessary.
		*/
	final String versionSuffix

	/**
	  * Optional: name of path of the docker file, to be concatenated to a "--file .." parameter. 
		* If a relative path is given, then it's relative to dockerWorkDir.
	  */
	final String dockerFile

	/**
	  * Optional: directory in which the build will be done.
		*/
	final String workDir

	/**
	  * Optional: the docker registry from which we pull.
		* Example: <code>nexus.metasfresh.com:6000</code>
	  */
	final String pullRegistry

	final String pullRegistryCredentialsId

	/**
	  * Optional: the docker registry to which we push.
		* Example: <code>nexus.metasfresh.com:6001</code>
	  */
	final String pushRegistry

	final String pushRegistryCredentialsId

	/**
	  * Optional
	  */
	final String additionalBuildArgs

	DockerConf(
			String artifactName,
			String branchName,
			String versionSuffix,
			String workDir = '.',
			String additionalBuildArgs = '',
			String dockerFile = 'Dockerfile',
			String pullRegistry = 'docker.metasfresh.com',
			String pullRegistryCredentialsId = 'nexus.metasfresh.com_jenkins',
			String pushRegistry = 'nexus.metasfresh.com:6001',
			String pushRegistryCredentialsId = 'nexus.metasfresh.com_jenkins')
	{
		this.artifactName = artifactName
		this.branchName = branchName
		this.versionSuffix = versionSuffix
		this.workDir = workDir
		this.additionalBuildArgs = additionalBuildArgs
		this.dockerFile = dockerFile
		this.pullRegistry = pullRegistry
		this.pullRegistryCredentialsId = pullRegistryCredentialsId
		this.pushRegistry = pushRegistry
		this.pushRegistryCredentialsId = pushRegistryCredentialsId
	}

	DockerConf withArtifactName(String altArtifactName)
	{
		return new DockerConf(
			altArtifactName,
			this.branchName,
			this.versionSuffix,
			this.workDir,
			this.additionalBuildArgs, 
			this.dockerFile,
			this.pullRegistry,
			this.pullRegistryCredentialsId,
			this.pushRegistry,
			this.pushRegistryCredentialsId)
	}

	DockerConf withWorkDir(String altWorkDir)
	{
		return new DockerConf(
			this.artifactName,
			this.branchName,
			this.versionSuffix,
			altWorkDir,
			this.additionalBuildArgs, 
			this.dockerFile,
			this.pullRegistry,
			this.pullRegistryCredentialsId,
			this.pushRegistry,
			this.pushRegistryCredentialsId)
	}
	
	DockerConf withAdditionalBuildArgs(String additionalBuildArgs)
	{
		return new DockerConf(
			this.artifactName,
			this.branchName,
			this.versionSuffix,
			this.workDir,
			additionalBuildArgs, 
			this.dockerFile,
			this.pullRegistry,
			this.pullRegistryCredentialsId,
			this.pushRegistry,
			this.pushRegistryCredentialsId)
	}

	String toString()
	{
		return """DockerConf[
  dockerFile=${dockerFile},
  workDir=${workDir},
  pullRegistry=${pullRegistry},
  pushRegistry=${pushRegistry},
  additionalBuildArgs=${additionalBuildArgs},
	branchName=${branchName},
	versionSuffix=${versionSuffix}
]""";
	}
}
