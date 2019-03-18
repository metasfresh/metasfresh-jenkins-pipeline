package de.metas.jenkins

class MetasfreshVersionInfo implements Serializable
{
    final String metasfreshVersion;
    final String metasfreshProcurementWebuiVersion;
    final String metasfreshWebuiApiVersion;
    final String metasfreshWebuiFrontendVersion;
    final String metasfreshE2eDockerImage;
    final String metasfreshEdiDockerImage;

    DockerConf(
		String metasfreshVersion,
		String metasfreshProcurementWebuiVersion,
		String metasfreshWebuiApiVersion,
		String metasfreshWebuiFrontendVersion,
		String metasfreshE2eDockerImage,
        String metasfreshEdiDockerImage)
	{
		this.metasfreshVersion = metasfreshVersion
		this.metasfreshProcurementWebuiVersion = metasfreshProcurementWebuiVersion
		this.metasfreshWebuiApiVersion = metasfreshWebuiApiVersion
		this.metasfreshWebuiFrontendVersion = metasfreshWebuiFrontendVersion
		this.metasfreshE2eDockerImage = metasfreshE2eDockerImage
        this.metasfreshEdiDockerImage = metasfreshEdiDockerImage
	}
}