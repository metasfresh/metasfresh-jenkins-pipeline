package de.metas.jenkins;

class MvnConf implements Serializable
{
	/**
	  * String containing the pom file, to be concatenated to a "--file .." parameter
	  */
	String pomFile;

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
}
