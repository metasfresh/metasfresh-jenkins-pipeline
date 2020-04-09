package de.metas.jenkins;

/**
 * According to the documentation at https://docs.docker.com/engine/reference/commandline/tag/ :
 * A tag name must be valid ASCII and may contain lowercase and uppercase letters, digits, underscores, periods and dashes. A tag name may not start with a period or a dash and may contain a maximum of 128 characters.
 */
String mkDockerTag(final String input)
{
 	final String onlyLegalChars = input
 		.replaceFirst('^[#\\.]', '') // delete the first letter if it is a period or dash
 		.replaceAll('[^a-zA-Z0-9_#\\.]', '_'); // replace everything that's not allowed with an underscore

	// Thx https://stackoverflow.com/a/15713996/1012103
	return onlyLegalChars.drop(onlyLegalChars.length() - 128); // if the onlyLegalchars is longer than 128 chars, then only get the last 128 of that string
}

/**
  * Just returns the current date, formatted as yyyy-dd-MM (e.g. "2017-08-22").
  */
String mkReleaseDate()
{
  return new Date().format( 'yyyy-MM-dd' )
}

/**
  * For a given version string such as "5.23.1-23+master" this method returns "5.23" (i.e. the major and minor).
  */
String extractReleaseVersion(final String version)
{
  final String releaseVersion = (version =~ '^([^\\.]+\\.[^\\.]+)(\\..*)*')[0][1]
  echo "extractReleaseVersion: Extracted releaseVersion=${releaseVersion} from the given version=${version}"
  return releaseVersion
}

/**
  * Invokes {@code java.net.URLEncoder.encode(urlPart, "UTF-8")} on the given {@code urlPart} string.
  */
String urlEncode(final String urlPart)
{
   final String encodedURLpart = java.net.URLEncoder.encode(urlPart, "UTF-8")
   echo "urlEncode: Encoded given urlPart=${urlPart} into ${encodedURLpart}"
   return encodedURLpart;
}

/**
  * Iterates the given map and creates a new map with the given map's keys and URL-encoded values.
  *
  * Thanks to https://stackoverflow.com/questions/40159258/impossibility-to-iterate-over-a-map-using-groovy-within-jenkins-pipeline
  */
Map urlEncodeMapValues(final Map mapToEncode)
{
  echo "urlEncodeMapValues - mapToEncode=${mapToEncode}"
  final def mapEntriesToEncode = mapToEncode.entrySet().toArray();
  final def result = [:];
  for ( int i = 0; i < mapEntriesToEncode.length; i++ )
  {
    echo "urlEncodeMapValues - i=${i}, mapEntriesToEncode[i]=${mapEntriesToEncode[i]}"
    result.put(mapEntriesToEncode[i].key, urlEncode(mapEntriesToEncode[i].value));
  }
  return result;
}

/**
 * This method calls additional downstream jobs such as metasfresh-procurement and metasfresh-webui from metasfresh.
 * This method needs to be invoked within a `node` block (label=`linux`), because it uses the `sh` step`!
 *
 * @param buildId is used as value for the {@code MF_UPSTREAM_BUILDNO} job parameter
 * @param upstreamBranch the preferred branch. If there is a jenkins job for that branch, it will be invoked. Otherwise the {@code master} branch's job will be invoked.
 * @param parentPomVersion is used as value for the {@code MF_PARENT_VERSION} job parameter
 * @param skipToDist is used as value for the {@code MF_SKIP_TO_DIST} job parameter (only evalated by the metasfresh jobs!)
 * @param triggerDownStreamBuilds is used as value for the {@code MF_TRIGGER_DOWNSTREAM_BUILDS} job parameter
 * @param wait if {@code true}, then this invokcation will wait for the invkoked job to finish and will fail if the invoked job fails.
 * @param jobFolderName the jenkins job folder name which also happens to be the github repository name of the project that we want to invoke here. Is used as prefix for the name of the job to be called.
 */
Map invokeDownStreamJobs(
		final String buildId,
		final String upstreamBranch,
		final String parentPomVersion,
		final boolean skipToDist,
		final boolean triggerDownStreamBuilds,
		final boolean wait,
		final String jobFolderName)
{
	echo "Invoking downstream job from folder=${jobFolderName} with preferred branch=${upstreamBranch}"

	final String jobName = getEffectiveDownStreamJobName(jobFolderName, upstreamBranch);

	final buildResult = build job: jobName,
		parameters: [
			string(name: 'MF_PARENT_VERSION', value: parentPomVersion),
			string(name: 'MF_UPSTREAM_BRANCH', value: upstreamBranch),
			string(name: 'MF_UPSTREAM_BUILDNO', value: buildId),
			booleanParam(name: 'MF_TRIGGER_DOWNSTREAM_BUILDS', value: triggerDownStreamBuilds), // the job shall just run but not trigger further builds because we are doing all the orchestration
			booleanParam(name: 'MF_SKIP_TO_DIST', value: skipToDist) // this param is only recognised by metasfresh
		], wait: wait

	echo "Job invokation done; buildResult.getBuildVariables()=${buildResult.getBuildVariables()}"
	return buildResult.getBuildVariables();
}

String getEffectiveDownStreamJobNameInNodeBlock(final String jobFolderName, final String upstreamBranch)
{
  node('linux')
  {
    return getEffectiveDownStreamJobName(jobFolderName, upstreamBranch)
  }
}

/**
  * This method needs to be invoked within a `node` block (label=`linux`), because it uses the `sh` step`!
  */
String getEffectiveDownStreamJobName(final String jobFolderName, final String upstreamBranch)
{
  	// if this is not the master branch but a feature branch, we need to find out if the "BRANCH_NAME" job exists or not
  	//
    final String effectiveBranchName = retrieveEffectiveBranchName(jobFolderName, upstreamBranch);
    jobName = "${jobFolderName}/${effectiveBranchName}"

  	// I also tried
  	// https://jenkins.metasfresh.com/job/metasfresh-multibranch/api/xml?tree=jobs[name]
  	// which worked from chrome, also for metas-dev.
  	// It worked from the shell using curl (with [ and ] escaped) for user metas-ts and an access token,
  	// but did not work from the shell with curl and user metas-dev with "metas-dev is missing the Overall/Read permission"
  	// the curl string was sh "curl -XGET 'https://jenkins.metasfresh.com/job/metasfresh-multibranch/api/xml?tree=jobs%5Bname%5D' --user metas-dev:access-token

  	// and I also tried inspecting the list returned by
  	// Jenkins.instance.getAllItems()
  	// but there I got a scurity exception and am not sure if an how I can have a SCM maintained script that is approved by an admin

  	return jobName;
}

/**
  * This method needs to be invoked within a `node` block (label=`linux`), because it uses the `sh` step`!
  *
  * @param metasFreshRepoName the repository name below https://github.com/metasfresh/
  * @param branchName the branch we are looking for. If the given repos does not have such a branch, {@code master} is returned instead
  *
  * @return the given {@code banchName} or {@code master}.
  */
String retrieveEffectiveBranchName(final String metasFreshRepoName, final String branchName)
{
  	// if this is not the master branch but a feature branch, we need to find out if the "BRANCH_NAME" job exists or not
  	//
  	// Here i'm not checking if the build job exists but if the respective branch on github exists. If the branch is there, then I assume that the multibranch plugin also created the job
  	def exitCode;

	// We run this within a node to avoid the error saying:
	// Required context class hudson.FilePath is missing
	// Perhaps you forgot to surround the code with a step that provides this, such as: node
	// ...
	// org.jenkinsci.plugins.workflow.steps.MissingContextVariableException: Required context class hudson.FilePath is missing
    nodeIfNeeded('linux', {
        exitCode = sh returnStatus: true, script: "git ls-remote --exit-code https://github.com/metasfresh/${metasFreshRepoName} ${branchName}"
    })

    final String effectiveBranchName;
  	if(exitCode == 0)
  	{
  		echo "Branch ${branchName} also exists in ${metasFreshRepoName}"
  		effectiveBranchName = branchName
  	}
  	else
  	{
  		echo "Branch ${branchName} does not exist in ${metasFreshRepoName}; falling back to master"
  		effectiveBranchName = 'master'
  	}

    return effectiveBranchName;
}

/**
  * Also see https://github.com/metasfresh/mf15-platform/issues/40
	*
	* @param releaseVersion e.g. "5.93"
  */
String createReleaseLinkWithText(
	final String releaseVersion,
	final String fullVersion,
	final Map artifactUrls /* MF_ARTIFACT_URLS */,
	final Map dockerImages=[:] /* MF_DOCKER_IMAGES */ )
{
	final String description = 'lets you jump to a jenkins-job that will create and publish <b>deployable docker images</b> from this build'

	return createReleaseLinkWithText0(
		'release_docker_images'/*jobName*/, 
		fullVersion, 
		description, 
		artifactUrls, 
		dockerImages)
}

String createWeeklyReleaseLinkWithText(
	final String releaseVersion,
	final String fullVersion,
	final Map artifactUrls /* MF_ARTIFACT_URLS */,
	final Map dockerImages=[:] /* MF_DOCKER_IMAGES */ )
{
	final String description = 'lets you jump to a jenkins-job that will create the <b>weekly release package</b> from this build'

	return createReleaseLinkWithText0(
		'release_weekly_release_package'/*jobName*/, 
		releaseVersion, 
		description, 
		artifactUrls, 
		dockerImages)
}

private String createReleaseLinkWithText0(
	final String jobName, 
	final String version, 
	final String description,
	final Map artifactUrls /* MF_ARTIFACT_URLS */,
	final Map dockerImages=[:] /* MF_DOCKER_IMAGES */)
{
	final String versionUrlParam = "VERSION_RELEASE=${urlEncode(mkDockerTag(version))}"
	final String distUrlParam = "URL_APP_DIST=${artifactUrls['metasfresh-dist']}"
	final String apiUrlParam = "URL_WEBAPI_JAR=${artifactUrls['metasfresh-webui']}"
	final String frontendUrlParam = "URL_WEBUI_FRONTEND=${artifactUrls['metasfresh-webui-frontend']}"
	final String e2eUrlParam = dockerImages['metasfresh-e2e'] ? "DOCKER_IMAGE_E2E=${urlEncode(dockerImages['metasfresh-e2e'])}" : ''

	final String jobUrl="https://jenkins.metasfresh.com/job/ops/job/${jobName}/parambuild/?${versionUrlParam}&${distUrlParam}&${apiUrlParam}&${frontendUrlParam}&${e2eUrlParam}"


	final String releaseLinkWithText = "<a href=\"${jobUrl}\"><b>this link</b></a> ${description}."
	return releaseLinkWithText;
}

