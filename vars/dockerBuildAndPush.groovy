#!/usr/bin/groovy

import de.metas.jenkins.DockerConf
import de.metas.jenkins.Misc

String call(final DockerConf dockerConf) {
    return buildAndPush(dockerConf)
}

private String buildAndPush(final DockerConf dockerConf) {
    final String dockerConfStr = dockerConf.toString();
    echo "buildAndPush is called with dockerConf=${dockerConfStr}"

    final Misc misc = new Misc()

    final String imageName = "metasfresh/${dockerConf.artifactName}"
    final String buildSpecificTag = misc.mkDockerTag("${dockerConf.branchName}-${dockerConf.versionSuffix}")
    final String latestTag = misc.mkDockerTag("${dockerConf.branchName}-LATEST")

    // if our Dockerfile supports it, we can make sure that we don't use any cached base image for more than one day.
    // thx to https://github.com/moby/moby/issues/1996#issuecomment-185872769
    final String additionalCacheBustArg;
    if (dockerConf.additionalBuildArgs.contains("CACHEBUST")) {
        additionalCacheBustArg = '' // assume that our caller already specified this arg
    } else {
        final def dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
        final def date = new Date()
        final String currentDate = dateFormat.format(date)

        additionalCacheBustArg = "--build-arg CACHEBUST=${currentDate}"
    }

    final String pullRegistry
    if (''.equals(dockerConf.pullRegistry)) {
        pullRegistry = ''
    } else {
        pullRegistry = "https://${dockerConf.pullRegistry}/v2/"
    }

    def image
    docker.withRegistry(pullRegistry, dockerConf.pullRegistryCredentialsId) {
        // we log in, with and without pullOnBuild, because even if we don't force a --pull, there might be one. and we need to avoid the error
        // "toomanyrequests: You have reached your pull rate limit. You may increase the limit by authenticating and upgrading: https://www.docker.com/increase-rate-limit"
        if (dockerConf.pullOnBuild) {

            echo 'dockerConf.pullOnBuild=true, so we log in and build using "--pull"'

            // despite being within "withRegistry", it's still required to include the pullRegistry in the Dockerfile's FROM (unless the default is fine for you)
            echo "going to invoke docker.build(\"${imageName}:${buildSpecificTag}\", \"--pull ${dockerConf.additionalBuildArgs} ${additionalCacheBustArg} ${dockerConf.workDir}\")"
            image = docker.build("${imageName}:${buildSpecificTag}", "--pull ${dockerConf.additionalBuildArgs} ${additionalCacheBustArg} ${dockerConf.workDir}")

        } else {
            echo 'dockerConf.pullOnBuild=false, so we build without "--pull"'
            echo "going to invoke docker.build(\"${imageName}:${buildSpecificTag}\", \"${dockerConf.additionalBuildArgs} ${additionalCacheBustArg} ${dockerConf.workDir}\")"
            image = docker.build("${imageName}:${buildSpecificTag}", "${dockerConf.additionalBuildArgs} ${additionalCacheBustArg} ${dockerConf.workDir}")
        }
    }

    final String pushRegistry
    if (''.equals(dockerConf.pushRegistry)) {
        pushRegistry = ''
    } else {
        pushRegistry = "https://${dockerConf.pushRegistry}/v2/"
    }
    docker.withRegistry(pushRegistry, dockerConf.pushRegistryCredentialsId) {
        
        image.push(buildSpecificTag)

        // Also publish a branch specific "LATEST".
        // Use uppercase because this way it's the same keyword that we use in maven.
        // Downstream jobs might look for "LATEST" in their base image tag
        image.push(latestTag)

        if (dockerConf.branchName == 'release') {
            echo 'branchName is "release", therefore we also push this image with the "latest" tag'
            image.push('latest');
        }

        // if additional tags were given, then also push with those
        for (String additionalDockerTag : dockerConf.additionalDockerTags) {
            if ('latest'.equals(additionalDockerTag) || buildSpecificTag.equals(additionalDockerTag)) {
                echo "We skip pushing this image with the additionalDockerTag=${additionalDockerTag} because that was already done."
                continue;
            }
            echo "We also push this image with the additionalDockerTag=${additionalDockerTag}"
            image.push(additionalDockerTag)
        }
    }

    // cleanup to avoid disk space issues
    //  sh "docker rmi ${imageName}:${latestTag}"
    //  sh "docker rmi ${imageNameWithTag}"

    return "${dockerConf.pushRegistry}/${imageName}:${buildSpecificTag}"
}
