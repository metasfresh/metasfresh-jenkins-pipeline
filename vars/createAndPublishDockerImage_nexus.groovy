#!/usr/bin/groovy

String call(
  final String publishRepositoryName,
  final String moduleDir,
  final String branchName,
  final String versionSuffix,
  final String additionalBuildArgs = '')
{
  return createAndPublishDockerImage(
    publishRepositoryName,
    moduleDir,
    branchName,
    versionSuffix,
    additionalBuildArgs
  )
}

private String createAndPublishDockerImage(
				final String publishRepositoryName,
				final String moduleDir,
				final String branchName,
				final String versionSuffix,
				final String additionalBuildArgs)
{
  echo """createAndPublishDockerImage is called with'
      publishRepositoryName=${publishRepositoryName}"
      moduleDir=${moduleDir}"
      branchName=${branchName}"
      versionSuffix=${versionSuffix}"
      additionalBuildArgs=${additionalBuildArgs}"""

	final dockerWorkDir="${moduleDir}/target/docker"

	final def misc = new de.metas.jenkins.Misc()

	final buildSpecificDockerTag = misc.mkDockerTag("${branchName}-${versionSuffix}")

  final imageName = "metasfresh/${publishRepositoryName}"
  final imageNameWithTag = "${imageName}:${buildSpecificDockerTag}"
  echo "The docker image name we will push is ${imageName}"

  final String latestTag = misc.mkDockerTag("${branchName}-LATEST")

  def app;
  docker.withRegistry('https://nexus.metasfresh.com:6000/v2/', 'nexus.metasfresh.com_jenkins')
  {
    app = docker.build(imageNameWithTag, "--pull ${additionalBuildArgs} ${dockerWorkDir}")
  }

  docker.withRegistry('https://nexus.metasfresh.com:6001/v2/', 'nexus.metasfresh.com_jenkins')
  {
    app.push

    // Also publish a branch specific "LATEST".
    // Use uppercase because this way it's the same keyword that we use in maven.
    // Downstream jobs might look for "LATEST" in their base image tag
    app.push(latestTag)
  }

  // cleanup to avoid disk space issues
  sh "docker rmi ${imageName}:${latestTag}"
  sh "docker rmi ${imageNameWithTag}"

  return imageNameWithTag
}
