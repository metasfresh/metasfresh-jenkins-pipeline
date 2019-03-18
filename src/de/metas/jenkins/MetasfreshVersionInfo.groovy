package de.metas.jenkins

class MetasfreshVersionInfo implements Serializable
{
    final String metasfreshVersion;
    final String procurementWebuiVersion;
    final String webuiApiVersion;
    final String webuiFrontendVersion;
	final String reportDockerBaseImage;
	final String adminDockerImage;
    final String ediDockerImage;
    final String e2eDockerImage;

    DockerConf(
		String metasfreshVersion,
		String procurementWebuiVersion,
		String webuiApiVersion,
		String webuiFrontendVersion,
		String reportDockerBaseImage,
		String adminDockerImage,
        String ediDockerImage,
		String e2eDockerImage)
	{
		this.metasfreshVersion = metasfreshVersion
		this.procurementWebuiVersion = procurementWebuiVersion
		this.webuiApiVersion = webuiApiVersion
		this.webuiFrontendVersion = webuiFrontendVersion
		this.reportDockerBaseImage = reportDockerBaseImage
		this.adminDockerImage = adminDockerImage
        this.ediDockerImage = ediDockerImage
		this.e2eDockerImage = e2eDockerImage
	}
}