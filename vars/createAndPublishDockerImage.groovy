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
  echo 'createAndPublishDockerImage is called with'
  echo "      publishRepositoryName=${publishRepositoryName}"
  echo "      moduleDir=${moduleDir}"
  echo "      branchName=${branchName}"
  echo "      versionSuffix=${versionSuffix}"
  echo "      additionalBuildArgs=${additionalBuildArgs}"

	final dockerWorkDir="${moduleDir}/target/docker"

	final def misc = new de.metas.jenkins.Misc()

	final buildSpecificDockerTag = misc.mkDockerTag("${branchName}-${versionSuffix}")

  final imageName = "metasfresh/${publishRepositoryName}"
  final imageNameWithTag = "${imageName}:${buildSpecificDockerTag}"
  echo "The docker image name we will push is ${imageName}"

  // this stuff doesn't work when we configure the base image version from outside
  // see https://issues.jenkins-ci.org/browse/JENKINS-46105
  //performWithJenkinsDockerSupport(imageNameWithTag, branchName, dockerWorkDir, additionalBuildArgs)
  withCredentials([usernamePassword(credentialsId: 'dockerhub_metasfresh', passwordVariable: 'dockerRegistryPassword', usernameVariable: 'dockerRegistryUserName')])
  {
    sh "docker login --username ${dockerRegistryUserName} --password ${dockerRegistryPassword}"
  }

  sh "docker build --pull --tag ${imageNameWithTag} ${additionalBuildArgs} ${dockerWorkDir}"
  sh "docker push ${imageNameWithTag}"

  // Also publish a branch specific "LATEST".
  // Uuse uppercase because this way it's the same keyword that we use in maven.
  // Downstream jobs might look for "LATEST" in their base image tag
  final String latestTag = misc.mkDockerTag("${branchName}-LATEST")
  sh "docker tag ${imageName} ${imageName}:${latestTag}"
  sh "docker push ${imageName}:${latestTag}"

	return imageName
}

performWithJenkinsDockerSupport(
  final String imageNameWithTag,
  final String branchName,
  final String dockerWorkDir,
  final String additionalBuildArgs)
{
  	docker.withRegistry('https://index.docker.io/v1/', 'dockerhub_metasfresh')
  	{
  		// note: we omit the "-service" in the docker image name, because we also don't have "-service" in the webui-api and backend and it's pretty clear that it is a service
      // note 2: we need the --pull to avoid building with a stale "latest" base image, see https://docs.docker.com/engine/reference/commandline/build/
      final def app = docker.build imageNameWithTag, "--pull ${additionalBuildArgs} ${dockerWorkDir}"
      app.push

      final String additionalLatestTag = misc.mkDockerTag("${branchName}-latest")
  		app.push additionalLatestTag
  	}
}
