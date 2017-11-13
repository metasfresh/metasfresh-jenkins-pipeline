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

  final imageName = "metasfresh/${publishRepositoryName}:${buildSpecificDockerTag}"
  echo "The docker image name we will push is ${imageName}"

	docker.withRegistry('https://index.docker.io/v1/', 'dockerhub_metasfresh')
	{
    final String baseImageRepo='metasfresh-report-dev';
    final String baseImageVersion=''
		// note: we omit the "-service" in the docker image name, because we also don't have "-service" in the webui-api and backend and it's pretty clear that it is a service
    // note 2: we need the --pull to avoid building with a stale "latest" base image, see https://docs.docker.com/engine/reference/commandline/build/
    final def app = docker.build imageName, "--pull ${additionalBuildArgs} ${dockerWorkDir}"
    app.push

    final String additionalLatestTag = misc.mkDockerTag("${branchName}-latest")
    echo "Also pushing this image with tag ${additionalLatestTag}"
		app.push additionalLatestTag
	}

	return imageName
}
