#!/usr/bin/groovy

import de.metas.jenkins.Misc

String call(final String branchName)
{
  echo "retrieveReleaseInfo is called with branchName=${branchName}"
  final Misc misc = new de.metas.jenkins.Misc()
  final String effectiveBranchName = misc.retrieveEffectiveBranchName('metasfresh-release-info', branchName)

  echo "Attempting to retrive the latest release-info.properties for effectiveBranchName=${effectiveBranchName}"
  nodeIfNeeded('linux', {
  
    sh "git clone --depth 1 https://github.com/metasfresh/metasfresh-release-info.git -b ${effectiveBranchName}"
    dir('metasfresh-release-info') 
    {
        final Properties props = readProperties file: 'release-info.properties'
        final String releaseVersion = props."release.version"
        echo "Succeeded to load the following props: ${props}; return only release.version=${releaseVersion} for now as noone uses the other(s)."
        return releaseVersion
    }
  })
}
