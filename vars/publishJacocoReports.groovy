#!/usr/bin/groovy

import de.metas.jenkins.Misc

/**
  *	Aggregate the test coverage results collected so far and upload them to codacy
  */
void call(
  final String gitCommitHash,
  final String codacyProjectTokenCredentialsId)
{
	// get the report for jenkins
  jacoco exclusionPattern: '**/src/main/java-gen' // collect coverage results for jenkins

  echo "!!!!!!!!!!!!!!!!!"
  echo "Invocation to uploadCoverageResultsForCodacy is commented out in metasfresh-jenkins-pipeline/vars/publishJacocoReports.groovy because it threw an NPE"
  echo "We tried with both versions 4.0.2 and 6.0.0"
  //uploadCoverageResultsForCodacy(gitCommitHash, codacyProjectTokenCredentialsId)
  echo "!!!!!!!!!!!!!!!!!"
}

void uploadCoverageResultsForCodacy(
  final String gitCommitHash,
  final String codacyProjectTokenCredentialsId)
{
  final String version='6.0.0'

  // get it from our own 3rd party repo; the github download stalls now and then
  sh "wget --quiet https://repo.metasfresh.com/service/local/repositories/mvn-3rdparty/content/com/codacy/codacy-coverage-reporter/${version}/codacy-coverage-reporter-${version}-assembly.jar"
	//sh "wget --quiet https://github.com/codacy/codacy-coverage-reporter/releases/download/${version}/codacy-coverage-reporter-${version}-assembly.jar"

	final String jacocoReportGlob='**/jacoco.xml' // by default, the files would be in **/target/site/jacoco/jacoco.xml, but why make assumptions here..
  final String classpathParam = "-cp codacy-coverage-reporter-${version}-assembly.jar"

  withCredentials([string(credentialsId: codacyProjectTokenCredentialsId, variable: 'CODACY_PROJECT_TOKEN')])
  {
    def jacocoReportFiles = findFiles(glob: jacocoReportGlob)
		for (int i = 0; i < jacocoReportFiles.size(); i++) 
		{
     	sh "java ${classpathParam} com.codacy.CodacyCoverageReporter report -l Java --project-token ${CODACY_PROJECT_TOKEN} --commit-uuid ${gitCommitHash} -r ${jacocoReportFiles[i]} --partial"
    }
    sh "java ${classpathParam} com.codacy.CodacyCoverageReporter final --project-token ${CODACY_PROJECT_TOKEN} --commit-uuid ${gitCommitHash}"
  }
}
