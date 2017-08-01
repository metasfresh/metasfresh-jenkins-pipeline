#!/usr/bin/groovy

// many thanks to
// * https://github.com/fabric8io/jenkins-pipeline-library#git-tag
// * https://github.com/fabric8io/fabric8-pipeline-library/blob/master/vars/gitTag.groovy
// * https://jenkins.io/doc/book/pipeline/shared-libraries/


def call(final script, final String pomFile='pom.xml', final String newParentVersion='LATEST')
{
    echo """mvnUpdateParentPomVersion is called with
\tscript=${script}
\tpomFile=${pomFile}
\tnewParentVersion=${newParentVersion}
""";

    if(config.newParentVersion && .config.newParentVersion!='LATEST')
    {
       echo "Update the parent pom version to the expolicitly given value ${newParentVersion}"
       sh "mvn --settings ${script.MAVEN_SETTINGS} --file ${pomFile} --batch-mode -DallowSnapshots=false -DgenerateBackupPoms=true ${script.MF_MAVEN_TASK_RESOLVE_PARAMS} -DparentVersion=[${newParentVersion}] versions:update-parent";
    }
    else
    {
       echo "Resolve the parent version range"
       sh "mvn --settings ${script.MAVEN_SETTINGS} --file ${pomFile} --batch-mode -DallowSnapshots=false -DgenerateBackupPoms=true ${script.MF_MAVEN_TASK_RESOLVE_PARAMS} -DprocessParent=true versions:resolve-ranges";
    }
}
