#!/usr/bin/groovy

//import de.metas.jenkins.MvnConf;

// many thanks to
// * https://github.com/fabric8io/jenkins-pipeline-library#git-tag
// * https://github.com/fabric8io/fabric8-pipeline-library/blob/master/vars/gitTag.groovy
// * https://jenkins.io/doc/book/pipeline/shared-libraries/


def call(final Map mvnConf, final String newParentVersion='LATEST')
{
    echo """mvnUpdateParentPomVersion is called with
\tmvnConf=${mvnConf}
\tnewParentVersion=${newParentVersion}
""";

    if(newParentVersion && newParentVersion!='LATEST')
    {
       echo "Update the parent pom version to the expolicitly given value ${newParentVersion}"
       sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --batch-mode -DallowSnapshots=false -DgenerateBackupPoms=true ${mvnConf.resolveParams} -DparentVersion=[${newParentVersion}] versions:update-parent";
    }
    else
    {
       echo "Resolve the parent version range"
       sh "mvn --settings ${mvnConf.settingsFile} --file ${mvnConf.pomFile} --batch-mode -DallowSnapshots=false -DgenerateBackupPoms=true ${mvnConf.resolveParams} -DprocessParent=true versions:resolve-ranges";
    }
}
