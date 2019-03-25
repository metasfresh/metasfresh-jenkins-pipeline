package de.metas.jenkins

class UpstreamBuildInfo implements Serializable
{
    final String upstreamBuildURL;
	final String upstreamBranch;

	UpstreamBuildInfo(
			String upstreamBuildURL,
			String upstreamBranch)
		{
			this.upstreamBuildURL = upstreamBuildURL
			this.upstreamBranch = upstreamBranch
		}
}