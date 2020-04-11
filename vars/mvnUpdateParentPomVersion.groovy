#!/usr/bin/groovy

import de.metas.jenkins.MvnConf;

// many thanks to
// * https://github.com/fabric8io/jenkins-pipeline-library#git-tag
// * https://github.com/fabric8io/fabric8-pipeline-library/blob/master/vars/gitTag.groovy
// * https://jenkins.io/doc/book/pipeline/shared-libraries/

def call(final MvnConf mvnConf)
{
    echo "mvnUpdateParentPomVersion is called with mvnConf=${mvnConf}"

    // make sure we know which plugin version we run
    final String VERSIONS_PLUGIN='org.codehaus.mojo:versions-maven-plugin:2.5'

    // this method is *just* about the parent pom. don't do unexpected additional stuff
    // don't process the dpendencies and dependencyManagement *section* of the pom
    final String processOnlyParentParams="-DprocessParent=true -DprocessDependencies=false -DprocessDependencyManagement=false -DprocessProperties=false"

    echo "mvnUpdateParentPomVersion: Resolve the parent version range"
    sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --non-recursive --batch-mode -DallowSnapshots=false -DgenerateBackupPoms=true ${processOnlyParentParams} ${mvnConf.resolveParams} ${VERSIONS_PLUGIN}:resolve-ranges"
}
