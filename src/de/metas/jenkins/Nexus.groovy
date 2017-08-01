package.de.metas.jenkins


def boolean isRepoExists(String repoBaseURL, String repoId)
{
	withCredentials([usernameColonPassword(credentialsId: 'nexus_jenkins', variable: 'NEXUS_LOGIN')])
	{
		echo "Check if the nexus repository ${repoId} exists";

		// check if there is a repository for ur branch
		final String checkForRepoCommand = "curl --silent -X GET -u ${NEXUS_LOGIN} ${repoBaseURL}/service/local/repositories | grep '<id>${repoId}-releases</id>'";
		final grepExitCode = sh returnStatus: true, script: checkForRepoCommand;
		final repoExists = grepExitCode == 0;

		echo "The nexus repository ${repoId} exists: ${repoExists}";
		return repoExists;
	}
}

def createRepo(String repoBaseURL, String repoId)
{
	withCredentials([usernameColonPassword(credentialsId: 'nexus_jenkins', variable: 'NEXUS_LOGIN')])
	{
		echo "Create the repository ${repoId}-releases";
		final String createHostedRepoPayload = """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<repository>
  <data>
	<id>${repoId}-releases</id>
	<name>${repoId}-releases</name>
	<exposed>true</exposed>
	<repoType>hosted</repoType>
	<writePolicy>ALLOW_WRITE_ONCE</writePolicy>
    <browseable>true</browseable>
    <indexable>true</indexable>
	<repoPolicy>RELEASE</repoPolicy>
	<providerRole>org.sonatype.nexus.proxy.repository.Repository</providerRole>
	<provider>maven2</provider>
	<format>maven2</format>
  </data>
</repository>
""";

		// # nexus ignored application/json
		final String createHostedRepoCommand =  "curl --silent -H \"Content-Type: application/xml\" -X POST -u ${NEXUS_LOGIN} -d \'${createHostedRepoPayload}\' ${repoBaseURL}/service/local/repositories"
		sh "${createHostedRepoCommand}"

// TODO: creating this proxy repo currently doesn't work
// we need it to have all the task/branch specific artifacts that were build on the public jenkins
		echo "Create the repository ${repoId}-proxy";
		final String createProxyRepoPayload = """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<repository>
  <data>
	<id>${repoId}-proxy</id>
	<name>${repoId}-proxy</name>
	<exposed>true</exposed>
	<repoType>proxy</repoType>
	<writePolicy>READ_ONLY</writePolicy>
    <browseable>true</browseable>
    <indexable>true</indexable>
	<repoPolicy>RELEASE</repoPolicy>
	<checksumPolicy>WARN</checksumPolicy>
	<downloadRemoteIndexes>true</downloadRemoteIndexes>
	<providerRole>org.sonatype.nexus.proxy.repository.Repository</providerRole>
	<provider>maven2</provider>
	<format>maven2</format>
	<remoteStorage>
      <remoteStorageUrl>https://repo.metasfresh.com/content/repositories/${repoId}/</remoteStorageUrl>
    </remoteStorage>
  </data>
</repository>
""";

		// # nexus ignored application/json
		final String createProxyRepoCommand =  "curl --silent -H \"Content-Type: application/xml\" -X POST -u ${NEXUS_LOGIN} -d \'${createProxyRepoPayload}\' ${repoBaseURL}/service/local/repositories"
		sh "${createProxyRepoCommand}"

// create a repo group that contains both the local/hosted repo and the remote/proxy repo
// this reposity will be used by the build
		echo "Create the repository-group ${repoId}";
		final String createGroupPayload = """<?xml version="1.0" encoding="UTF-8"?>
<repo-group>
  <data>
    <repositories>
      <repo-group-member>
        <name>${repoId}-releases</name>
        <id>${repoId}-releases</id>
        <resourceURI>${repoBaseURL}/content/repositories/${repoId}-releases/</resourceURI>
      </repo-group-member>
      <repo-group-member>
        <name>${repoId}-proxy</name>
        <id>${repoId}-proxy</id>
        <resourceURI>https://repo.metasfresh.com/content/repositories/${repoId}/</resourceURI>
      </repo-group-member>
	  <repo-group-member>
        <name>mvn-3rdparty-private</name>
        <id>mvn-3rdparty-private</id>
        <resourceURI>${repoBaseURL}/content/repositories/mvn-3rdparty-private/</resourceURI>
      </repo-group-member>
      <!--
          We need the following repo in the group to cover the case of a task branch that exists only in this repo and not in any upstream repo.
          In that scenario, the "task"-proxy-repo will not work and be "blocked" by nexus, but everything required by the build will be available here.
      -->
	    <repo-group-member>
        <name>mvn-master-proxy</name>
        <id>mvn-master-proxy</id>
        <resourceURI>https://repo.metasfresh.com/content/repositories/mvn-master/</resourceURI>
      </repo-group-member>
    </repositories>
    <name>${repoId}</name>
    <repoType>group</repoType>
    <providerRole>org.sonatype.nexus.proxy.repository.Repository</providerRole>
    <exposed>true</exposed>
    <id>${repoId}</id>
	<provider>maven2</provider>
	<format>maven2</format>
  </data>
</repo-group>
"""

		// # nexus ignored application/json
		final String createGroupCommand =  "curl --silent -H \"Content-Type: application/xml\" -X POST -u ${NEXUS_LOGIN} -d \'${createGroupPayload}\' ${repoBaseURL}/service/local/repo_groups"
		sh "${createGroupCommand}"

		echo "Create the scheduled task to keep ${repoId}-releases from growing too big";

final String createSchedulePayload = """<?xml version="1.0" encoding="UTF-8"?>
<scheduled-task>
  <data>
	<id>cleanup-repo-${repoId}-releases</id>
	<enabled>true</enabled>
	<name>Remove Releases from ${repoId}-releases</name>
	<typeId>ReleaseRemoverTask</typeId>
	<schedule>daily</schedule>
	<startDate>${currentBuild.startTimeInMillis}</startDate>
	<recurringTime>03:00</recurringTime>
	<properties>
      <scheduled-task-property>
        <key>numberOfVersionsToKeep</key>
        <value>3</value>
      </scheduled-task-property>
      <scheduled-task-property>
        <key>indexBackend</key>
        <value>false</value>
      </scheduled-task-property>
      <scheduled-task-property>
        <key>repositoryId</key>
        <value>${repoId}-releases</value>
      </scheduled-task-property>
	</properties>
  </data>
</scheduled-task>"""

		// # nexus ignored application/json
		final String createScheduleCommand =  "curl --silent -H \"Content-Type: application/xml\" -X POST -u ${NEXUS_LOGIN} -d \'${createSchedulePayload}\' ${repoBaseURL}/service/local/schedules"
		sh "${createScheduleCommand}"
	} // withCredentials
}

def deleteRepo(String repoBaseURL, String repoId)
{
	withCredentials([usernameColonPassword(credentialsId: 'nexus_jenkins', variable: 'NEXUS_LOGIN')])
	{
		echo "Delete the repository ${repoId}";

		final String deleteGroupCommand = "curl --silent -X DELETE -u ${NEXUS_LOGIN} ${repoBaseURL}/service/local/repo_groups/${repoId}"
		sh "${deleteGroupCommand}"

		final String deleteRepoCommand = "curl --silent -X DELETE -u ${NEXUS_LOGIN} ${repoBaseURL}/service/local/repositories/${repoId}-releases"
		sh "${deleteRepoCommand}"

		final String deleteScheduleCommand = "curl --silent -X DELETE -u ${NEXUS_LOGIN} ${repoBaseURL}/service/local/schedules/cleanup-repo-${repoId}-releases"
		sh "${deleteScheduleCommand}"
	}
}
