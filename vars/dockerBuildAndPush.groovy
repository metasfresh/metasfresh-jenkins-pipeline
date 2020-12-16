#!/usr/bin/groovy

import de.metas.jenkins.DockerConf

String call(final DockerConf dockerConf)
{
  return buildAndPush(dockerConf)
}

private String buildAndPush(final DockerConf dockerConf)
{
  final String dockerConfStr = dockerConf.toString();
  echo "buildAndPush is called with dockerConf=${dockerConfStr}"

	final def misc = new de.metas.jenkins.Misc()


  final String imageName = "metasfresh/${dockerConf.artifactName}"
	final String buildSpecificTag = misc.mkDockerTag("${dockerConf.branchName}-${dockerConf.versionSuffix}")
  final String latestTag = misc.mkDockerTag("${dockerConf.branchName}-LATEST")

  // if our Dockerfile supports it, we can make sure that we don't use any cached base image for more than one day.
  // thx to https://github.com/moby/moby/issues/1996#issuecomment-185872769
  final String additionalCacheBustArg;
  if(dockerConf.additionalBuildArgs.contains("CACHEBUST"))
  {
    additionalCacheBustArg = '' // assume that our called already specified this arg
  }
  else
  {
    final def dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    final def date = new Date()
    final String currentDate = dateFormat.format(date)

    additionalCacheBustArg = "--build-arg CACHEBUST=${currentDate}"
  }

  def image
  if(dockerConf.pullOnBuild)
  {
    echo 'dockerConf.pullOnBuild=true, so we log in and build using "--pull"'
    docker.withRegistry("https://${dockerConf.pullRegistry}/v2/", dockerConf.pullRegistryCredentialsId)
    {
      // despite being within "withRegistry", it's still required to include the pullRegistry in the Dockerfile's FROM (unless the default is fine for you)
      image = docker.build("${imageName}:${buildSpecificTag}", "--pull ${dockerConf.additionalBuildArgs} ${additionalCacheBustArg} ${dockerConf.workDir}")
    }
  }
  else
  {
    echo 'dockerConf.pullOnBuild=false, so we build without "--pull"'
    image = docker.build("${imageName}:${buildSpecificTag}", "${dockerConf.additionalBuildArgs} ${additionalCacheBustArg} ${dockerConf.workDir}")
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
