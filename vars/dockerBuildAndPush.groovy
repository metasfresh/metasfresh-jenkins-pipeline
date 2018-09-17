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
  final String latestTag = misc.mkDockerTag("${dockerConf.branchName}-LATEST")

  def image
  docker.withRegistry("https://${dockerConf.pullRegistry}/v2/", dockerConf.pullRegistryCredentialsId)
  {
    // despite being within "withRegistry", it's still required to include the pullRegistry in the Dockerfile's FROM (unless the default is fine for you)
    image = docker.build("${imageName}:${buildSpecificTag}", "--pull ${dockerConf.additionalBuildArgs} ${dockerConf.workDir}")
  }

  docker.withRegistry("https://${dockerConf.pushRegistry}/v2/", dockerConf.pushRegistryCredentialsId)
  {
    image.push(buildSpecificTag)

    // Also publish a branch specific "LATEST".
    // Use uppercase because this way it's the same keyword that we use in maven.
    // Downstream jobs might look for "LATEST" in their base image tag
    image.push(latestTag)

    if(dockerConf.branchName=='release')
    {
      echo 'branchName is "release", therefore we also push this image with the "latest" tag'
      image.push('latest');
    }
  }

  // cleanup to avoid disk space issues
  //  sh "docker rmi ${imageName}:${latestTag}"
  //  sh "docker rmi ${imageNameWithTag}"

  return "${dockerConf.pushRegistry}/${imageName}:${buildSpecificTag}"
}
