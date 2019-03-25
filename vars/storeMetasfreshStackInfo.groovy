import de.metas.jenkins.MvnConf
import de.metas.jenkins.component.MetasfreshStack

String call(final MetasfreshStack metasfreshStack,
            final MvnConf mvnConf) {

    final String yamlFileName = 'metasfreshStack.yaml'
    writeYaml file: yamlFileName, data: metasfreshStack, charset: 'UTF-8'

    configFileProvider([configFile(fileId: 'metasfresh-global-maven-settings', replaceTokens: true, variable: 'MAVEN_SETTINGS')]) {
        withMaven(jdk: 'java-8', maven: 'maven-3.5.0', mavenLocalRepo: '.repository') {


            sh "mvn --settings ${mvnConf.settingsFile} -Dfile=${yamlFileName} -Durl=https://repo.metasfresh.com -DrepositoryId=mvn${mvnConf.MF_MAVEN_REPO_ID} -DgroupId=de.metas.stack -DartifactId=metasfresh-stackinfo -Dversion=${metasfreshStack.version} -Dpackaging=yaml -DgeneratePom=true org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file"

            final misc = new de.metas.jenkins.Misc()
            final String metasfreshStackUrl = "${mvnConf.deployRepoURL}/de/metas/ui/web/metasfresh-webui-frontend/${misc.urlEncode(metasfreshStack.version)}/metasfresh-webui-frontend-${misc.urlEncode(metasfreshStack.version)}.tar.gz"
            return metasfreshStackUrl;
        }
    }
}
