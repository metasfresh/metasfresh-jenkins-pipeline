#!/usr/bin/groovy

String call(
  final String dockerRepositoryName,
  final String dockerModuleDir,
  final String dockerBranchName,
  final String dockerVersionSuffix)
{
  return createAndPublishDockerImage(
    dockerRepositoryName,
    dockerModuleDir,
    dockerBranchName,
    dockerVersionSuffix
  )
}

private String createAndPublishDockerImage(
				final String dockerRepositoryName,
				final String dockerModuleDir,
				final String dockerBranchName,
				final String dockerVersionSuffix)
{
	final dockerWorkDir="${dockerModuleDir}/target/docker"

	final def misc = new de.metas.jenkins.Misc();

	final dockerName = "metasfresh/${dockerRepositoryName}-dev"
	final buildSpecificDockerTag = misc.mkDockerTag("${dockerBranchName}-${dockerVersionSuffix}")

	docker.withRegistry('https://index.docker.io/v1/', 'dockerhub_metasfresh')
	{
		// note: we omit the "-service" in the docker image name, because we also don't have "-service" in the webui-api and backend and it's pretty clear that it is a service
		final def app = docker.build dockerName, dockerWorkDir

		app.push misc.mkDockerTag("${dockerBranchName}-latest");
		app.push buildSpecificDockerTag
		if(dockerBranchName == 'release')
		{
			echo 'dockerBranchName == release, so we also push this with the "latest" tag'
			app.push misc.mkDockerTag('latest');
		}
	}

	return "${dockerName}:${buildSpecificDockerTag}"
}
