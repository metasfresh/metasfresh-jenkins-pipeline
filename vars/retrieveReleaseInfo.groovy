#!/usr/bin/groovy

import de.metas.jenkins.Misc

String retrieveReleaseInfo(final String branchName)
{
  echo "retrieveReleaseInfo is called with branchName=${branchName}"
  final Misc misc = new de.metas.jenkins.Misc()
  final String effectiveBranchName = misc.retrieveEffectiveBranchName('metasfresh-release-info', branchName)

  echo "Attempting to retrive the latest release-info.properties for effectiveBranchName=${effectiveBranchName}"
  // use -O so a possible stale file is overwritten
  sh "wget https://raw.githubusercontent.com/metasfresh/metasfresh-release-info/${effectiveBranchName}/release-info.properties -O release-info.properties"

  Properties props = readProperties  file: 'release-info.properties'
  final String releaseVersion = props."release.version"
  echo "Succeeded to load the following props: ${props}; return only release.version=${releaseVersion} for now as noone uses the other(s)."

  return releaseVersion
}

String call(final String branchName)
{
  if(env.NODE_LABELS)
  {
    final def labels = env.NODE_LABELS.tokenize(' ');
    if(!labels.contains('linux'))
    {
      error "Our current node ${env.NODE_NAME} lacks the \'linux\' label. Please make sure to explicitly run on a 'linux' node, no node at all, to leave it to this step"
    }
    // we are on a 'linux' node
    echo "retrieveReleaseInfo is called on node ${env.NODE_NAME}"
    return retrieveReleaseInfo(branchName);
  }
  // we are not winthin a node, so start one now
  echo 'retrieveReleaseInfo is not called on a node, so we now open our own node block'
  node('linux')
  {
    return retrieveReleaseInfo(branchName);
  }
}
