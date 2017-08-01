#!/usr/bin/groovy

import de.metas.jenkins.MvnConf;

// many thanks to
// * https://github.com/fabric8io/jenkins-pipeline-library#git-tag
// * https://github.com/fabric8io/fabric8-pipeline-library/blob/master/vars/gitTag.groovy
// * https://jenkins.io/doc/book/pipeline/shared-libraries/

/**
  * Makes sure that the parent pom declared within the given {@code mvnConf}'s {@code pomFile} points to the given {@code newParentVersion}
  */
def call(final MvnConf mvnConf, final String newParentVersion='LATEST')
{
    //echo "mvnUpdateParentPomVersion is called with newParentVersion=${newParentVersion} and mvnConf=${mvnConf}"

    // make sure we know which plugin version we run
    final String pluginGAV='org.codehaus.mojo:versions-maven-plugin:2.4'

    if(newParentVersion && newParentVersion!='LATEST')
    {
       // update the parent pom version. Since https://github.com/metasfresh/metasfresh/issues/2102 we have a parent version *range*, therefore, we don't use "update-parent" anymore, but "resolve-ranges"
       echo "mvnUpdateParentPomVersion: Update the parent pom version to the explicitly given value ${newParentVersion}"
       sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --non-recursive --batch-mode -DallowSnapshots=false -DgenerateBackupPoms=true ${mvnConf.resolveParams} -DparentVersion=[${newParentVersion}] ${pluginGAV}:update-parent"
    }
    else
    {
       // this method is *just* about the parent pom. don't do unexpected additional stuff
       final String processOnlyParentParams="-DprocessParent=true -DprocessDependencies=false -DprocessDependencyManagement=false -DprocessProperties=false"

       echo "mvnUpdateParentPomVersion: Resolve the parent version range"
       sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --non-recursive --batch-mode -DallowSnapshots=false -DgenerateBackupPoms=true ${processOnlyParentParams} ${mvnConf.resolveParams} ${pluginGAV}:resolve-ranges"
     }
}
