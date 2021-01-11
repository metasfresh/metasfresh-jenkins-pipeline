package de.metas.jenkins

/**
 * If the docker-image's URL ends with LATEST, then invoke the nexus REST API to find the docker image's sha256.
 * Without this, e.g. in the case of a "master_LATEST" image tag, we won't get the lastest image version
 */
String retrieveDockerUrlToUse(final String dockerRegImageAndTag)
{
	echo 'BEGIN retrieveDockerUrlToUse'
	//echo "retrieveDockerUrlToUse - dockerRegImageAndTag=${dockerRegImageAndTag}"

	final String dockerUrlToUse
	if(dockerRegImageAndTag.endsWith('LATEST'))
	{
		echo "retrieveDockerUrlToUse - docker image ends with '_LATEST'; -> will invoke nexus-search-API"

		final String[] dockerUrlParts = splitDockerUrl(dockerRegImageAndTag)

		final String dockerRegistry =dockerUrlParts[0]
		echo "dockerRegistry=${dockerRegistry}"
		final String dockerImage = dockerUrlParts[1]
		echo "dockerImage=${dockerImage}"
		final String dockerTag = dockerUrlParts[2]
		echo "dockerTag=${dockerTag}"

		final def misc = new de.metas.jenkins.Misc()
		def normalizedDockerTag = misc.mkDockerTag(dockerTag)
		echo "normalizedDockerTag=${normalizedDockerTag}"

		final String dockerSha256 = invokeSearchAPI(dockerImage, normalizedDockerTag)
		if(dockerSha256)
		{
			dockerUrlToUse = "${dockerRegistry}/${dockerImage}:${normalizedDockerTag}@sha256:${dockerSha256}"
			echo "retrieveDockerUrlToUse - Found sha256 for tag=${normalizedDockerTag}"
		}
		else
		{
			final String fallbackDockerTag = 'master_LATEST'
			final String fallbackDockerSha256 = invokeSearchAPI(dockerImage, fallbackDockerTag)
			if(fallbackDockerSha256)
			{
				dockerUrlToUse = "${dockerRegistry}/${dockerImage}:${fallbackDockerTag}@sha256:${fallbackDockerSha256}"
				echo "retrieveDockerUrlToUse - !!! Found sha256 for fallback-tag=${fallbackDockerTag} !!!"
			}
			else
			{
				error "retrieveDockerUrlToUse - !!! Found no sha256 to normalized tag=${normalizedDockerTag} or fallback-tag=${fallbackDockerTag} !!!"
			}
		}
	}
	else
	{
		dockerUrlToUse = dockerRegImageAndTag
	}
	echo "retrieveDockerUrlToUse - dockerUrlToUse=${dockerUrlToUse}"
	echo 'END retrieveDockerUrlToUse'
	return dockerUrlToUse
}

@NonCPS
String[] splitDockerUrl(final String dockerRegImageAndTag)
{
	echo 'BEGIN splitDockerUrl'
	final def dockerUrlRegExp = /^(?<registryWithPort>[^\:]*\:?[0-9]*)\/(?<image>[^\:]*):(?<tag>.*)$/
	echo "dockerRegImageAndTag=${dockerRegImageAndTag}; dockerUrlRegExp=${dockerUrlRegExp}"
	final def urlComponents = (dockerRegImageAndTag =~ /$dockerUrlRegExp/)[0]
	echo "urlComponents.size()=${urlComponents.size()}; urlComponents=${urlComponents}"

	final def dockerRegistry = urlComponents[1]
	echo "splitDockerUrl - dockerRegistry=${dockerRegistry}"
	final def dockerImage = urlComponents[2]
	echo "splitDockerUrl - dockerImage=${dockerImage}"
	final def dockerTag = urlComponents[3]
	echo "splitDockerUrl - dockerTag=${dockerTag}"

	def result = [dockerRegistry, dockerImage, dockerTag]
	echo "END splitDockerUrl; result=${result}"
	return result
}

String invokeSearchAPI(final String dockerImage, final String normalizedDockerTag)
{
	echo 'BEGIN invokeSearchAPI'
	final def misc = new de.metas.jenkins.Misc();

	// thx to https://chadmayfield.com/2018/09/01/pulling-artifacts-from-nexus-in-less-than-25-lines-of-bash/
	final String nexusQueryUrl = "https://nexus.metasfresh.com/service/rest/v1/search/assets?docker.imageName=${misc.urlEncode(dockerImage)}&docker.imageTag=${misc.urlEncode(normalizedDockerTag)}"
	final String curlCommand = "curl -s -X GET \"${nexusQueryUrl}\" -H \"accept: application/json\" | grep -Po '\"sha256\" *: .*' | awk -F '\"' '{print \$4}'"

	final String dockerSha256 = sh label: 'Retrieve docker-image sha256', returnStdout: true, script: curlCommand
	echo "Retrieved docker-dockerSha256=${dockerSha256}"

	echo 'END invokeSearchAPI'
	return dockerSha256 ? dockerSha256.trim() : ''
}

