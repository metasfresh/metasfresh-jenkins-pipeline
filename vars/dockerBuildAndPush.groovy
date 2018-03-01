#!/usr/bin/groovy

import de.metas.jenkins.DockerConf

String call(final DockerConf dockerConf)
{
  return buildAndPush(dockerConf)
}

private String buildAndPush(final DockerConf dockerConf)
{
  echo "buildAndPush is called with dockerConf=${dockerConf}"

	final def misc = new de.metas.jenkins.Misc()


  final String imageName = "metasfresh/${dockerConf.artifactName}"
	final String buildSpecificTag = misc.mkDockerTag("${dockerConf.branchName}-${dockerConf.versionSuffix}")
  final String latestTag = misc.mkDockerTag("${branchName}-LATEST")

  echo "The docker image name we will build and push is ${imageName}"

  def image
  docker.withRegistry("https://${dockerConf.pullRegistry}/v2/", dockerConf.pullRegistryCredentialsId)
  {
    image = docker.build("${imageName}:${buildSpecificTag}", "--pull ${additionalBuildArgs} ${dockerWorkDir}")
  }

  docker.withRegistry("https://${dockerConf.pushRegistry}/v2/", dockerConf.pushRegistryCredentialsId)
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

  return "${dockerConf.pullRegistry}/${imageName}:${buildSpecificTag}"
}
