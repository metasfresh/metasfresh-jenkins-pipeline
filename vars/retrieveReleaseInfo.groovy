#!/usr/bin/groovy

import de.metas.jenkins.Misc

String call(final String branchName)
{
  echo "retrieveReleaseInfo is called with branchName=${branchName}"
  final Misc misc = new de.metas.jenkins.Misc()
  final String effectiveBranchName = misc.retrieveEffectiveBranchName('metasfresh-release-info', branchName)

  echo "Attempting to retrive the latest release-info.properties for effectiveBranchName=${effectiveBranchName}"
  nodeIfNeeded('linux', {
    // use -O so a possible stale file is overwritten
    sh "wget https://raw.githubusercontent.com/metasfresh/metasfresh-release-info/${effectiveBranchName}/release-info.properties --no-check-certificate -O release-info.properties"
    // note that readProperties also needs to be within the same node block t make sure we actually have access to the downloaded file
    final Properties props = readProperties file: 'release-info.properties'

    final String releaseVersion = props."release.version"
    echo "Succeeded to load the following props: ${props}; return only release.version=${releaseVersion} for now as noone uses the other(s)."

    return releaseVersion
  })
}
