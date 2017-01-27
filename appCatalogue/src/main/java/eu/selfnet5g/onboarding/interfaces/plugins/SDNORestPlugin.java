package eu.selfnet5g.onboarding.interfaces.plugins;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import eu.selfnet5g.onboarding.interfaces.AppRegistration;
import eu.selfnet5g.onboarding.model.AppMetadata;

public class SDNORestPlugin implements AppRegistration {

	private Logger log = LoggerFactory.getLogger(SDNORestPlugin.class);
	
	@Value("${sdno.rest.skip:false}")
	private boolean skipRest;
	
	private String sdnoUrl;
	
	private RestTemplate restTemplate;
	
	public SDNORestPlugin(String sdnoIp, String sdnoPort) {
		this.restTemplate = new RestTemplate();
		this.sdnoUrl  = "http://" + sdnoIp + ":" + sdnoPort;
	}
	
	public String registerNewApp(AppMetadata metadata) throws Exception {
		
		String url = "NULL";
		
		try {
		
			log.info("Going to register new AppType " + metadata.getAppType() + " implementation to SDNO");
				
			url = this.sdnoUrl + "/cxf/manager/" + metadata.getAppType();
			
			if (!skipRest) {
			
				ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, metadata.getAppArchive().getLink(), String.class);
				HttpStatus code = httpResponse.getStatusCode();
				if (code.equals(HttpStatus.OK)) {
					//return the orderId for this appType as generated by the SDNO
					String orderId = httpResponse.getBody().toString();	
					
					log.info("Registration done (orderId=" + orderId +")");
					
					return orderId;
					
				} else {
					log.error("SDNO REST server returned " + code.toString() + " code.");
					throw new Exception("Impossible to register the App into SDNO");
				}
			} else {
				log.info("Registration skipped by configuration (url:"+ url + ")");
				return UUID.randomUUID().toString();
			}
		} catch (Exception e) {
			log.error("Error while sending POST to " + url + " with body: " + metadata.getAppArchive().getLink());
			throw new Exception(e.getMessage());
		}
	}
	
	public void unregisterApp(AppMetadata metadata) throws Exception {
		String url = "NULL";
		
		try {
		
			log.info("Going to unregister AppType (" + metadata.getAppType() + "/" + metadata.getAppTypeOrderId() + ") from SDNO");
				
			url = this.sdnoUrl + "/cxf/manager/" + metadata.getAppType() + "/" + metadata.getAppTypeOrderId();
			
			if (!skipRest) {
				restTemplate.delete(url);
			
				log.info("Unregistration done.");
			} else {
				log.info("Unregistration skipped by configuration (url:"+ url + ")");
			}
			
		} catch (Exception e) {
			log.error("Error while sending DELETE to " + url);
			throw new Exception(e.getMessage());
		}
	}
	
}