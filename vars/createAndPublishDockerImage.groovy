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
	final dockerWorkDir="${moduleDir}/target/docker"

	final def misc = new de.metas.jenkins.Misc();

	final dockerName = "metasfresh/${publishRepositoryName}"
	final buildSpecificDockerTag = misc.mkDockerTag("${branchName}-${versionSuffix}")

	docker.withRegistry('https://index.docker.io/v1/', 'dockerhub_metasfresh')
	{
    final String baseImageRepo='metasfresh-report-dev';
    final String baseImageVersion=''
		// note: we omit the "-service" in the docker image name, because we also don't have "-service" in the webui-api and backend and it's pretty clear that it is a service
    // note 2: we need the --pull to avoid building with a stale "latest" base image, see https://docs.docker.com/engine/reference/commandline/build/
    final def app = docker.build dockerName, "--pull ${additionalBuildArgs} ${dockerWorkDir}"

		app.push misc.mkDockerTag("${branchName}-latest");
		app.push buildSpecificDockerTag
	}

	return "${dockerName}:${buildSpecificDockerTag}"
}
