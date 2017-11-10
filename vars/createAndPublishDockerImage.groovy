#!/usr/bin/groovy

void call(
  final String dockerRepositoryName,
  final String dockerModuleDir,
  final String dockerBranchName,
  final String dockerVersionSuffix)
{
  createAndPublishDockerImage(
    dockerRepositoryName,
    dockerModuleDir,
    dockerBranchName,
    dockerVersionSuffix
  )
}

private void createAndPublishDockerImage(
				final String dockerRepositoryName,
				final String dockerModuleDir,
				final String dockerBranchName,
				final String dockerVersionSuffix)
{
	final dockerWorkDir="${dockerModuleDir}/target/docker"
	final dockerSourceArtifactName="${dockerRepositoryName}-service"

	docker.withRegistry('https://index.docker.io/v1/', 'dockerhub_metasfresh')
	{
		// note: we ommit the "-service" in the docker image name, because we also don't have "-service" in the webui-api and backend and it's pretty clear that it is a service
		final def app = docker.build "metasfresh/${dockerRepositoryName}-dev", "${dockerWorkDir}";

		final def misc = new de.metas.jenkins.Misc();
		app.push misc.mkDockerTag("${dockerBranchName}-latest");
		app.push misc.mkDockerTag("${dockerBranchName}-${dockerVersionSuffix}");
		if(dockerBranchName == 'release')
		{
			echo 'dockerBranchName == release, so we also push this with the "latest" tag'
			app.push misc.mkDockerTag('latest');
		}
	} // docker.withRegistry
}
