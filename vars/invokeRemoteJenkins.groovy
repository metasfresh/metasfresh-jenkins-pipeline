#!/usr/bin/groovy

import de.metas.jenkins.MetasfreshVersionInfo
import de.metas.jenkins.UpstreamBuildInfo

String call(
		final UpstreamBuildInfo upstreamBuildInfo,
		final String metasfreshStackUrl)
{
  return invokeZapier(upstreamBuildInfo, metasfreshStackUrl)
}


String createZapierUrl()
{
	  withCredentials([string(credentialsId: 'zapier-metasfresh-build-notification-webhook', variable: 'zapier_WEBHOOK_SECRET')])
	  {
	    // the zapier secret contains a trailing slash and another slash that is somewhere in the middle.
	  	return "https://hooks.zapier.com/hooks/catch/${zapier_WEBHOOK_SECRET}"
		}
}

void invokeZapier(
		final UpstreamBuildInfo upstreamBuildInfo,
		final String metasfreshStackUrl)
{
	// now that the "basic" build is done, notify zapier so we can do further things external to this jenkins instance
	// note: even with "skiptodist=true we do this, because we still want to make the notifcations

				 echo "Going to notify external systems via zapier webhook"

	final def hook = registerWebhook()
	echo "Waiting for POST to ${hook.getURL()}"

	final jsonPayload = """{
    \"METASFRESH_UPSTREAM_STACK_URL\":\"${metasfreshStackUrl}\",
   	\"MF_WEBHOOK_CALLBACK_URL\":\"${hook.getURL()}\"
}"""

	// invoke zapier to trigger external jobs
	nodeIfNeeded('linux')
			{
				sh "curl -X POST -d \'${jsonPayload}\' ${createZapierUrl()}";
			}
	waitForWebhookCall(hook);
}

void waitForWebhookCall(final def hook)
{
	    echo "Wait 30 minutes for the zapier-triggered downstream jobs to succeed or fail"
	    timeout(time: 30, unit: 'MINUTES')
	    {
				// stop and wait, for someone to do e.g. curl -X POST -d 'OK' <hook-URL>
	      final def message = waitForWebhook hook ?: '<webhook returned NULL>'
	      if(message.trim() == 'OK')
	      {
					echo "The external jobs that were invoked by zapier succeeeded; message='${message}'; hook-URL=${hook.getURL()}"
	      }
				else
				{
					error "An external job that was invoked by zapier failed; message='${message}'; hook-URL=${hook.getURL()}"
				}
	    }
}