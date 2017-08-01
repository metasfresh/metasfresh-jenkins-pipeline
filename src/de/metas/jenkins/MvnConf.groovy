package de.metas.jenkins;

class MvnConf
{
	String pomfile;

	/**
	  * String containing the settings file, to be contatenated to a "--settings .." parameter
		*/
	String settingsFile;

	/**
	  * String containing a number of "-Dtask-repo-.." maven parameters, used to resolve dependencies from the repos that we want to resolve from
	  */
	String resolveParams;

	/**
	  * String containing a "-DaltDeploymentRepository=.." maven parameter, used to deploy dependencies to the repo which we want to deploy to
		*/
	String deployParam;

	String getPomFileParam()
	{
		return "--file ${pomFile}"
	}

	/**
	  * @return string containing a "--settings .." parameter, used to tell maven which settings to use
		*/
	String getSettingsFileParam()
	{
		return "--file ${settingsFile}"
	}

	String getDeployParam()
	{
		return deployParam;
	}

	String getResolveParams()
	{
		return resolveParams;
	}
}
