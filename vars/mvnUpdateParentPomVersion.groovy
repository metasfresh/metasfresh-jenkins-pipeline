#!/usr/bin/groovy

import de.metas.jenkins.MvnConf;

// many thanks to
// * https://github.com/fabric8io/jenkins-pipeline-library#git-tag
// * https://github.com/fabric8io/fabric8-pipeline-library/blob/master/vars/gitTag.groovy
// * https://jenkins.io/doc/book/pipeline/shared-libraries/

/**
  * Makes sure that the parent pom declared within the given {@code mvnConf}'s {@code pomFile} is resolved to a particular version.
  * Note that we can't reliably resolve the range to *a particular* version.
  * 'resolve-ranges' has no parameter to specify a version and
  * 'update-parent' happens to only to its job if the 'parentVersion' values is *not the latest one*.
  * Therefore and becauwse metasfresh-parent is a slow-moving target, we don't insist on controlling it to particular version.
  */
def call(final MvnConf mvnConf)
{
    echo "mvnUpdateParentPomVersion is called with mvnConf=${mvnConf}"

    // make sure we know which plugin version we run
    final String versionsPlugin='org.codehaus.mojo:versions-maven-plugin:2.4'

    // this method is *just* about the parent pom. don't do unexpected additional stuff
    final String processOnlyParentParams="-DprocessParent=true -DprocessDependencies=false -DprocessDependencyManagement=false -DprocessProperties=false"

    echo "mvnUpdateParentPomVersion: Resolve the parent version range"
    sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --non-recursive --batch-mode -DallowSnapshots=false -DgenerateBackupPoms=true ${processOnlyParentParams} ${mvnConf.resolveParams} ${versionsPlugin}:resolve-ranges"
}
