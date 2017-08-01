package de.metas.jenkins;

/**
 * This method calls additional downstream jobs such as metasfresh-procurement and metasfresh-webui from metasfresh.
 * Please don't invoke it from within a node block, because it also contains a node block which might need to the pipeline beeing blocked unneccesarily.
 *
 * @param jobFolderName the jenkins job folder name which also happens to be the github repository name of the project that we want to invoke here. Is used as prefix for the name of the job to be called.
 * @param buildId is used as value for the {@code MF_UPSTREAM_BUILDNO} job parameter
 * @param upstreamBranch the preferred branch. If there is a jenkins job for that branch, it will be invoked. Otherwise the {@code master} branch's job will be invoked.
 * @param parentPomVersion is used as value for the {@code MF_PARENT_VERSION} job parameter
 * @param wait if {@code true}, then this invokcation will wait for the invkoked job to finish and will fail if the invoked job fails-
 */
def invokeDownStreamJobs(
		final String jobFolderName,
		final String buildId,
		final String upstreamBranch,
		final String parentPomVersion,
		final boolean wait)
{
	echo "Invoking downstream job from folder=${jobFolderName} with preferred branch=${upstreamBranch}"

	// if this is not the master branch but a feature branch, we need to find out if the "BRANCH_NAME" job exists or not
	//
	// Here i'm not checking if the build job exists but if the respective branch on github exists. If the branch is there, then I assume that the multibranch plugin also created the job
	def exitCode;
	node('linux')
	{
		// We run this within a node to avoid the error saying:
		// Required context class hudson.FilePath is missing
		// Perhaps you forgot to surround the code with a step that provides this, such as: node
		// ...
		// org.jenkinsci.plugins.workflow.steps.MissingContextVariableException: Required context class hudson.FilePath is missing

		exitCode = sh returnStatus: true, script: "git ls-remote --exit-code https://github.com/metasfresh/${jobFolderName} ${upstreamBranch}"
	}

	if(exitCode == 0)
	{
		echo "Branch ${upstreamBranch} also exists in ${jobFolderName}"
		jobName = jobFolderName + "/" + upstreamBranch
	}
	else
	{
		echo "Branch ${upstreamBranch} does not exist in ${jobFolderName}; falling back to master"
		jobName = jobFolderName + "/master"
	}

	// I also tried
	// https://jenkins.metasfresh.com/job/metasfresh-multibranch/api/xml?tree=jobs[name]
	// which worked from chrome, also for metas-dev.
	// It worked from the shell using curl (with [ and ] escaped) for user metas-ts and an access token,
	// but did not work from the shell with curl and user metas-dev with "metas-dev is missing the Overall/Read permission"
	// the curl string was sh "curl -XGET 'https://jenkins.metasfresh.com/job/metasfresh-multibranch/api/xml?tree=jobs%5Bname%5D' --user metas-dev:access-token

	// and I also tried inspecting the list returned by
	// Jenkins.instance.getAllItems()
	// but there I got a scurity exception and am not sure if an how I can have a SCM maintained script that is approved by an admin

	build job: jobName,
		parameters: [
			string(name: 'MF_PARENT_VERSION', value: parentPomVersion),
			string(name: 'MF_UPSTREAM_BRANCH', value: upstreamBranch),
			string(name: 'MF_UPSTREAM_BUILDNO', value: buildId),
			booleanParam(name: 'MF_TRIGGER_DOWNSTREAM_BUILDS', value: false), // the job shall just run but not trigger further builds because we are doing all the orchestration
			booleanParam(name: 'MF_SKIP_TO_DIST', value: true) // this param is only recognised by metasfresh
		], wait: wait
}
