#!/usr/bin/groovy

import de.metas.jenkins.Misc;

Properties call(final String branchName)
{
    final Misc misc = de.metas.jenkins.Misc();
    final String effectiveBranchName = misc.retrieveEffectiveBranchName('metasfresh-release-info', branchName);

    echo "Attempting to retrive the latest release-info.properties for effectiveBranchName=${effectiveBranchName}"
    sh "wget https://raw.githubusercontent.com/metasfresh/metasfresh-release-info/${effectiveBranchName}/release-info.properties"

    Properties props = readProperties  file: 'release-info.properties'
    echo "Succeeded to load the following props: ${props}"

    return props;
}
