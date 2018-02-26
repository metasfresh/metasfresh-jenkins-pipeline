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

	final buildSpecificTag = misc.mkDockerTag("${branchName}-${versionSuffix}")

  final imageName = "metasfresh/${publishRepositoryName}"
  final imageNameWithTag = "${imageName}:${buildSpecificTag}"
  echo "The docker image name we will push is ${imageName}"

  final String latestTag = misc.mkDockerTag("${branchName}-LATEST")

  def image
  docker.withRegistry('https://nexus.metasfresh.com:6000/v2/', 'nexus.metasfresh.com_jenkins')
  {
    image = docker.build(imageNameWithTag, "--pull ${additionalBuildArgs} ${dockerWorkDir}")
  }

  docker.withRegistry('https://nexus.metasfresh.com:6001/v2/', 'nexus.metasfresh.com_jenkins')
  {
    image.push(buildSpecificTag)

    // Also publish a branch specific "LATEST".
    // Use uppercase because this way it's the same keyword that we use in maven.
    // Downstream jobs might look for "LATEST" in their base image tag
    image.push(latestTag)

    if(branchName=='release')
    {
      echo 'branchName=release, so we also push this with the "latest" tag'
      image.push('latest');
    }
  }

  // cleanup to avoid disk space issues
  //  sh "docker rmi ${imageName}:${latestTag}"
  //  sh "docker rmi ${imageNameWithTag}"

  return imageNameWithTag
}
