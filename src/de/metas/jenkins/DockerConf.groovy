package de.metas.jenkins

class DockerConf implements Serializable {
    public static final String PUSH_REGISTRY = 'dockertemp.metasfresh.com:6001'
    public static final String PUSH_REGISTRY_CREDENTIALS_ID = 'nexus.metasfresh.com_jenkins'

    public static final String PULL_REGISTRY = 'dockertemp.metasfresh.com'
    public static final String PULL_REGISTRY_CREDENTIALS_ID = 'nexus.metasfresh.com_jenkins'

    /**
     * Mandatory. Examples: metasfresh-app, metasfresh-material-dispo.
     * This string will be made docker-compliant if necessary. Also, <code>metasfresh/</code> will be prepended
     */
    final String artifactName

    /**
     * Mandatory: the branch we are building a docker image for. Will go into the tag the images is pushed with.
     * This string will be made docker-compliant if necessary.
     */
    final String branchName

    /**
     * Mandatory: the build version we are building a docker image for. Will go into the tag the images is pushed with.
     * This string will be made docker-compliant if necessary.
     */
    final String versionSuffix

    /**
     * Optional: name of path of the docker file, to be concatenated to a "--file .." parameter. 
     * If a relative path is given, then it's relative to dockerWorkDir.
     */
    final String dockerFile

    /**
     * Optional: directory in which the build will be done.
     */
    final String workDir

    /**
     * Optional: the docker registry from which we pull.
     * Example: <code>nexus.metasfresh.com:6000</code>
     */
    final String pullRegistry

    final String pullRegistryCredentialsId

    /**
     * Optional: the docker registry to which we push.
     * Example: <code>nexus.metasfresh.com:6001</code>
     */
    final String pushRegistry

    final String pushRegistryCredentialsId

    final boolean pullOnBuild

    /**
     * Optional
     */
    final String additionalBuildArgs

    /**
     * Optional; the dockerBuildAndPush step will push the respective image also with the tags contained in this list
     */
    final ArrayList additionalDockerTags

    DockerConf(
            String artifactName,
            String branchName,
            String versionSuffix,
            String workDir = '.',
            String additionalBuildArgs = '',
            String dockerFile = 'Dockerfile',
            String pullRegistry = PULL_REGISTRY,
            String pullRegistryCredentialsId = PULL_REGISTRY_CREDENTIALS_ID,
            String pushRegistry = PUSH_REGISTRY,
            String pushRegistryCredentialsId = PUSH_REGISTRY_CREDENTIALS_ID,
            boolean pullOnBuild = true,
            ArrayList additionalDockerTags = new ArrayList()) {
        this.artifactName = artifactName
        this.branchName = branchName
        this.versionSuffix = versionSuffix
        this.workDir = workDir
        this.additionalBuildArgs = additionalBuildArgs
        this.dockerFile = dockerFile
        this.pullRegistry = pullRegistry
        this.pullRegistryCredentialsId = pullRegistryCredentialsId
        this.pushRegistry = pushRegistry
        this.pushRegistryCredentialsId = pushRegistryCredentialsId
        this.pullOnBuild = pullOnBuild
        this.additionalDockerTags = additionalDockerTags
    }

    DockerConf withArtifactName(String altArtifactName) {
        return new DockerConf(
                altArtifactName,
                this.branchName,
                this.versionSuffix,
                this.workDir,
                this.additionalBuildArgs,
                this.dockerFile,
                this.pullRegistry,
                this.pullRegistryCredentialsId,
                this.pushRegistry,
                this.pushRegistryCredentialsId,
                this.pullOnBuild,
                this.additionalDockerTags)
    }

    DockerConf withWorkDir(String altWorkDir) {
        return new DockerConf(
                this.artifactName,
                this.branchName,
                this.versionSuffix,
                altWorkDir,
                this.additionalBuildArgs,
                this.dockerFile,
                this.pullRegistry,
                this.pullRegistryCredentialsId,
                this.pushRegistry,
                this.pushRegistryCredentialsId,
                this.pullOnBuild,
                this.additionalDockerTags)
    }

    DockerConf withAdditionalBuildArgs(String additionalBuildArgs) {
        return new DockerConf(
                this.artifactName,
                this.branchName,
                this.versionSuffix,
                this.workDir,
                additionalBuildArgs,
                this.dockerFile,
                this.pullRegistry,
                this.pullRegistryCredentialsId,
                this.pushRegistry,
                this.pushRegistryCredentialsId,
                this.pullOnBuild,
                this.additionalDockerTags)
    }

    DockerConf withPullOnBuild(boolean pullOnBuild) {
        return new DockerConf(
                this.artifactName,
                this.branchName,
                this.versionSuffix,
                this.workDir,
                this.additionalBuildArgs,
                this.dockerFile,
                this.pullRegistry,
                this.pullRegistryCredentialsId,
                this.pushRegistry,
                this.pushRegistryCredentialsId,
                pullOnBuild,
                this.additionalDockerTags)
    }

    DockerConf addAdditionalDockerTag(String additionalDockerTag) {
        this.additionalDockerTags.add(additionalDockerTag)
        return this
    }

    String toString() {
        return """DockerConf[
  dockerFile=${dockerFile},
  workDir=${workDir},
  pullRegistry=${pullRegistry},
  pushRegistry=${pushRegistry},
  additionalBuildArgs=${additionalBuildArgs},
	branchName=${branchName},
	versionSuffix=${versionSuffix}
]""";
    }
}
