package de.metas.jenkins

class UpstreamBuildInfo implements Serializable
{
    final String upstreamBuildURL;
		final String upstreamBranch;

    DockerConf(
			String upstreamBuildURL,
			String upstreamBranch)
		{
			this.upstreamBuildURL = upstreamBuildURL
			this.upstreamBranch = upstreamBranch
		}
}