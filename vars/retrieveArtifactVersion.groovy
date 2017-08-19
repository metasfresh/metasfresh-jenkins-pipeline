#!/usr/bin/groovy


String call(final String branchName, final String buildNo)
{
	final String majorAndMinorVersion = retrieveReleaseInfo(branchName);
	echo "Retrieved MajorVersion.MinorVersion=${majorAndMinorVersion}"

	// set the version prefix, 1 for "master", 2 for "not-master" a.k.a. feature
	final patchVersion = branchName.equals('master') ? "1" : "2"
	echo "Set PatchVersion=${patchVersion}"

 	final String branchNameToUse = branchName
		.replaceAll('[^a-zA-Z0-9]', ''); // remove everything that's not allowed

	final String artifactVersion="${majorAndMinorVersion}.${patchVersion}-${buildNo}+${branchNameToUse}"
	echo "Set artifactVersion={artifactVersion}"

	return artifactVersion
}
