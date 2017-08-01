package.de.metas.jenkins

class NexusClient implements Serializable
{
	final String PUBLIC_REPO_BASE_URL="https://repo.metasfresh.com/content/repositories/";

	final String mvnRepoBaseURL;
	final String mvnRepoName;

	NexusClient(
			String mvnRepoBaseURL,
			String mvnRepoName
			)
	{
		this.mvnRepoBaseURL = mvnRepoBaseURL
		this.mvnRepoName = mvnRepoName
	}

	void createRepoIfNotExists()
	{
		if(isRepoExists())
		{
			return;
		}
		createRepo();
	}

	boolean isRepoExists()
	{
		withCredentials([usernameColonPassword(credentialsId: 'nexus_jenkins', variable: 'NEXUS_LOGIN')])
		{
			echo "Check if the nexus repository ${this.mvnRepoName} exists";

			// check if there is a repository for ur branch
			final String checkForRepoCommand = "curl --silent -X GET -u ${NEXUS_LOGIN} ${this.mvnRepoBaseURL}/service/local/repositories | grep '<id>${this.mvnRepoName}-releases</id>'";
			final grepExitCode = sh returnStatus: true, script: checkForRepoCommand;
			final repoExists = grepExitCode == 0;

			echo "The nexus repository ${this.mvnRepoName} exists: ${repoExists}";
			return repoExists;
		}
	}

	void createRepo()
	{
		withCredentials([usernameColonPassword(credentialsId: 'nexus_jenkins', variable: 'NEXUS_LOGIN')])
		{
			echo "Create the repository ${this.mvnRepoName}-releases";
			final String createHostedRepoPayload = """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<repository>
  <data>
	<id>${this.mvnRepoName}-releases</id>
	<name>${this.mvnRepoName}-releases</name>
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
			final String createHostedRepoCommand =  "curl --silent -H \"Content-Type: application/xml\" -X POST -u ${NEXUS_LOGIN} -d \'${createHostedRepoPayload}\' ${this.mvnRepoBaseURL}/service/local/repositories"
			sh "${createHostedRepoCommand}"

			if(this.mvnRepoBaseURL != PUBLIC_REPO_BASE_URL)
			{
				// we need it to have all the task/branch specific artifacts that were build on the public jenkins
				echo "Create the repository ${this.mvnRepoName}-proxy";
				final String createProxyRepoPayload = """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<repository>
  <data>
	<id>${this.mvnRepoName}-proxy</id>
	<name>${this.mvnRepoName}-proxy</name>
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
      <remoteStorageUrl>https://repo.metasfresh.com/content/repositories/${this.mvnRepoName}/</remoteStorageUrl>
    </remoteStorage>
  </data>
</repository>
""";

				// # nexus ignored application/json
				final String createProxyRepoCommand =  "curl --silent -H \"Content-Type: application/xml\" -X POST -u ${NEXUS_LOGIN} -d \'${createProxyRepoPayload}\' ${this.mvnRepoBaseURL}/service/local/repositories"
				sh "${createProxyRepoCommand}"
			}
			else
			{
				echo "SKIP creating a ${this.mvnRepoName}-proxy, because mvnRepoBaseURL=${PUBLIC_REPO_BASE_URL}"
			}

			// create a repo group that contains both the local/hosted repo and the remote/proxy repo
			// this reposity will be used by the build
			echo "Create the repository-group ${this.mvnRepoName}";
			final String createGroupPayload = """<?xml version="1.0" encoding="UTF-8"?>
<repo-group>
  <data>
    <repositories>
      <repo-group-member>
        <name>${this.mvnRepoName}-releases</name>
        <id>${this.mvnRepoName}-releases</id>
        <resourceURI>${this.mvnRepoBaseURL}/content/repositories/${this.mvnRepoName}-releases/</resourceURI>
      </repo-group-member>
      <repo-group-member>
        <name>${this.mvnRepoName}-proxy</name>
        <id>${this.mvnRepoName}-proxy</id>
        <resourceURI>https://repo.metasfresh.com/content/repositories/${this.mvnRepoName}/</resourceURI>
      </repo-group-member>
	  <repo-group-member>
        <name>mvn-3rdparty-private</name>
        <id>mvn-3rdparty-private</id>
        <resourceURI>${this.mvnRepoBaseURL}/content/repositories/mvn-3rdparty-private/</resourceURI>
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
    <name>${this.mvnRepoName}</name>
    <repoType>group</repoType>
    <providerRole>org.sonatype.nexus.proxy.repository.Repository</providerRole>
    <exposed>true</exposed>
    <id>${this.mvnRepoName}</id>
	<provider>maven2</provider>
	<format>maven2</format>
  </data>
</repo-group>
"""

			// # nexus ignored application/json
			final String createGroupCommand =  "curl --silent -H \"Content-Type: application/xml\" -X POST -u ${NEXUS_LOGIN} -d \'${createGroupPayload}\' ${this.mvnRepoBaseURL}/service/local/repo_groups"
			sh "${createGroupCommand}"

			echo "Create the scheduled task to keep ${this.mvnRepoName}-releases from growing too big";

	final String createSchedulePayload = """<?xml version="1.0" encoding="UTF-8"?>
<scheduled-task>
  <data>
	<id>cleanup-repo-${this.mvnRepoName}-releases</id>
	<enabled>true</enabled>
	<name>Remove Releases from ${this.mvnRepoName}-releases</name>
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
        <value>${this.mvnRepoName}-releases</value>
      </scheduled-task-property>
	</properties>
  </data>
</scheduled-task>"""

			// # nexus ignored application/json
			final String createScheduleCommand =  "curl --silent -H \"Content-Type: application/xml\" -X POST -u ${NEXUS_LOGIN} -d \'${createSchedulePayload}\' ${this.mvnRepoBaseURL}/service/local/schedules"
			sh "${createScheduleCommand}"
		} // withCredentials
	}

	def deleteRepo()
	{
		withCredentials([usernameColonPassword(credentialsId: 'nexus_jenkins', variable: 'NEXUS_LOGIN')])
		{
			echo "Delete the repository ${this.mvnRepoName}";

			final String deleteGroupCommand = "curl --silent -X DELETE -u ${NEXUS_LOGIN} ${this.mvnRepoBaseURL}/service/local/repo_groups/${this.mvnRepoName}"
			sh "${deleteGroupCommand}"

			final String deleteRepoCommand = "curl --silent -X DELETE -u ${NEXUS_LOGIN} ${this.mvnRepoBaseURL}/service/local/repositories/${this.mvnRepoName}-releases"
			sh "${deleteRepoCommand}"

			final String deleteScheduleCommand = "curl --silent -X DELETE -u ${NEXUS_LOGIN} ${this.mvnRepoBaseURL}/service/local/schedules/cleanup-repo-${this.mvnRepoName}-releases"
			sh "${deleteScheduleCommand}"
		}
	}
}
