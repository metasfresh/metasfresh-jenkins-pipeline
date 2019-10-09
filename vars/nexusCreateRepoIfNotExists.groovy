package.de.metas.jenkins

def call(final String mvnRepoBaseURL, String mvnRepoName)
{
	withCredentials([usernameColonPassword(credentialsId: 'nexus_jenkins', variable: 'NEXUS_LOGIN')])
	{
		if(!isRepoExists(mvnRepoBaseURL, mvnRepoName))
		{
			createRepo(mvnRepoBaseURL, mvnRepoName);
		}
	}
}

boolean isRepoExists(final String mvnRepoBaseURL, String mvnRepoName)
{
	echo "Check if the nexus repository ${mvnRepoName} exists";

	// check if there is a repository for ur branch
	final String checkForRepoCommand = "curl --silent -X GET -u ${NEXUS_LOGIN} ${mvnRepoBaseURL}/service/local/repositories | grep '<id>${mvnRepoName}-releases</id>'";
	final grepExitCode = sh returnStatus: true, script: checkForRepoCommand;
	final repoExists = grepExitCode == 0;

	echo "The nexus repository ${mvnRepoName} exists: ${repoExists}";
	return repoExists;
}

void createRepo(final String mvnRepoBaseURL, final String mvnRepoName)
{
		final String PUBLIC_REPO_BASE_URL="https://nexus.metasfresh.com";

		// create a repo group that contains both the 3rd-party stuff and the locally build artifacts
		//  this reposity will be used by the build
		
		/*  
        //  2019-10-09 - @metas-jb:
        //  using nexus3 script API to create the maven-repo with config body formatted as json.
        //  note: 
        //  the script ("create_maven_repo") shall be uploaded beforehand to the nexus3 script API
        */
		
		
		echo "Create the repository ${mvnRepoName}-releases and group ${mvnRepoName}";
		final String createGroupPayload = """{
  "mvn_reponame": "${mvnRepoName}-releases",
  "write_policy": "allow_once",
  "version_policy": "release",
  "layout_policy": "strict",
  "cleanup_policy": "cleanup_maven_30d",
  "blobname": "default",
  "strict_content_validation": "true",
  "mvn_groupname": "${mvnRepoName}",
  "group_members": ["maven-public", "${mvnRepoName}-releases"]
}
"""

		// # nexus ignored application/json
		final String createGroupCommand =  "curl --silent -H \"Content-Type: text/plain\" -X POST -u ${NEXUS_LOGIN} -d \'${createGroupPayload}\' ${mvnRepoBaseURL}rest/v1/script/create_maven_repo/run"
		sh "${createGroupCommand}"

}

def deleteRepo(final String mvnRepoBaseURL, String mvnRepoName)
{
		echo "Delete the repository ${mvnRepoName}";

		final String deleteGroupCommand = "curl --silent -X DELETE -u ${NEXUS_LOGIN} ${mvnRepoBaseURL}/service/local/repo_groups/${mvnRepoName}"
		sh "${deleteGroupCommand}"

		final String deleteRepoCommand = "curl --silent -X DELETE -u ${NEXUS_LOGIN} ${mvnRepoBaseURL}/service/local/repositories/${mvnRepoName}-releases"
		sh "${deleteRepoCommand}"

		final String deleteScheduleCommand = "curl --silent -X DELETE -u ${NEXUS_LOGIN} ${mvnRepoBaseURL}/service/local/schedules/cleanup-repo-${mvnRepoName}-releases"
		sh "${deleteScheduleCommand}"
}
