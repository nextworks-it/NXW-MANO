package eu.selfnet5g.onboarding.interfaces.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.selfnet5g.onboarding.interfaces.AppNotification;
import eu.selfnet5g.onboarding.messages.AppDisableNotificationMessage;
import eu.selfnet5g.onboarding.messages.AppEnableNotificationMessage;
import eu.selfnet5g.onboarding.messages.AppOffboardNotificationMessage;
import eu.selfnet5g.onboarding.messages.AppOnboardNotificationMessage;
import eu.selfnet5g.onboarding.messages.AppUpdateNotificationMessage;
import eu.selfnet5g.onboarding.model.AppClass;
import eu.selfnet5g.onboarding.model.AppFamily;
import eu.selfnet5g.onboarding.model.AppPackage;

public class RabbitNotificationPlugin implements AppNotification {
	
	private Logger log = LoggerFactory.getLogger(RabbitNotificationPlugin.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private TopicExchange topicExchange;
	
	public void appOnboardNotification(AppPackage appPackage) throws Exception {
		log.info("Going to send App Onboard Notification for app-id " + appPackage.getId());
		
		//create message to be sent over the message bus
		if (appPackage.getMetadata() == null) {
			log.error("Metadata info not found for App " + appPackage.getId());
			throw new Exception("App Package corrupted");
		}
		
		AppOnboardNotificationMessage msg = new AppOnboardNotificationMessage(appPackage.getId(),
																			  appPackage.getMetadata().getAppFamily(),
																			  appPackage.getMetadata().getAppClass(),
																			  appPackage.getMetadata().getAppType(),
																			  appPackage.getMetadata().getAppName(),
																			  appPackage.getMetadata().getAppVersion());
		
		//set specific app data
		if (msg.getAppFamily() == AppFamily.SENSOR) {
			msg.setMonitoring(appPackage.getMonitoring());
		}
		if (msg.getAppClass() == AppClass.VNF) {
			msg.setVnfd(appPackage.getVnfDescriptor());
			msg.setScriptsLink(appPackage.getMetadata().getScriptsLink());
			msg.setVmImages(appPackage.getMetadata().getVmImages());
		} else if (msg.getAppClass() == AppClass.PNF) {
			msg.setPnfd(appPackage.getPnfAppDescriptor());
		} else {
			msg.setSdnd(appPackage.getSdnAppDescriptor());
			msg.setAppImplId(appPackage.getMetadata().getAppTypeOrderId());
		}
		msg.setConfiguration(appPackage.getConfiguration());
		
		try {
			//send message over topic appfamily.appclass
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.setSerializationInclusion(Include.NON_EMPTY);
			String json = mapper.writeValueAsString(msg);
			
			String key = msg.getAppFamily().toString() + "." + msg.getAppClass().toString();
			rabbitTemplate.convertAndSend(topicExchange.getName(), key, json);
			
			log.info("Sent AppOnboardNotificationMessage to the message bus (key " + key + "): \n" + json);
			
		} catch (Exception e) {
			log.error("Error while posting AppOnboardNotificationMessage");
			throw new Exception(e.getMessage());
		}
	}
	
	public void appEnableNotification(String packageId, AppFamily appFamily, AppClass appClass) throws Exception {
		log.info("Going to send App Enable Notification for app-id " + packageId);
		
		//create message to be sent over the message bus		
		AppEnableNotificationMessage msg = new AppEnableNotificationMessage(packageId);
		
		try {
			//send message over topic appfamily.appclass
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.setSerializationInclusion(Include.NON_EMPTY);
			String json = mapper.writeValueAsString(msg);
			
			String key = appFamily.toString() + "." + appClass.toString();
			rabbitTemplate.convertAndSend(topicExchange.getName(), key, json);
			
			log.info("Sent AppEnableNotificationMessage to the message bus (key " + key + "): \n" + json);
			
		} catch (Exception e) {
			log.error("Error while posting AppDisableNotificationMessage");
			throw new Exception(e.getMessage());
		}
	}
	
	public void appDisableNotification(String packageId, AppFamily appFamily, AppClass appClass) throws Exception {
		log.info("Going to send App Disable Notification for app-id " + packageId);
		
		//create message to be sent over the message bus		
		AppDisableNotificationMessage msg = new AppDisableNotificationMessage(packageId);
		
		try {
			//send message over topic appfamily.appclass
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.setSerializationInclusion(Include.NON_EMPTY);
			String json = mapper.writeValueAsString(msg);
			
			String key = appFamily.toString() + "." + appClass.toString();
			rabbitTemplate.convertAndSend(topicExchange.getName(), key, json);
			
			log.info("Sent AppDisableNotificationMessage to the message bus (key " + key + "): \n" + json);
			
		} catch (Exception e) {
			log.error("Error while posting AppDisableNotificationMessage");
			throw new Exception(e.getMessage());
		}
	}
	
	public void appOffboardNotification(String packageId, AppFamily appFamily, AppClass appClass) throws Exception {
		log.info("Going to send App Offboard Notification for app-id " + packageId);
		
		//create message to be sent over the message bus		
		AppOffboardNotificationMessage msg = new AppOffboardNotificationMessage(packageId);
		
		try {
			//send message over topic appfamily.appclass
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.setSerializationInclusion(Include.NON_EMPTY);
			String json = mapper.writeValueAsString(msg);
			
			String key = appFamily.toString() + "." + appClass.toString();
			
			rabbitTemplate.convertAndSend(topicExchange.getName(), key, json);
			
			log.info("Sent AppOffboardNotificationMessage to the message bus (key " + key + "): \n" + json);
			
		} catch (Exception e) {
			log.error("Error while posting AppOffboardNotificationMessage");
			throw new Exception(e.getMessage());
		}		
	}
	
	public void appUpdateNotification(String packageId, AppPackage newPackage) throws Exception {
		log.info("Going to send App Update Notification for app-id " + packageId);
		
		//create message to be sent over the message bus
		if (newPackage.getMetadata() == null) {
			log.error("Updated metadata info not found for App " + packageId);
			throw new Exception("Updated App Package corrupted");
		}
		
		AppUpdateNotificationMessage msg = new AppUpdateNotificationMessage(packageId,
																			newPackage.getMetadata().getAppName(),
																			newPackage.getMetadata().getAppVersion());
		
		//set specific app data
		if (newPackage.getMonitoring() != null) {
			msg.setMonitoring(newPackage.getMonitoring());
		}
		if (newPackage.getVnfDescriptor() != null) {
			msg.setVnfd(newPackage.getVnfDescriptor());
			
		}
		if (newPackage.getSdnAppDescriptor() != null) {
			msg.setSdnd(newPackage.getSdnAppDescriptor());
		}
		
		if (newPackage.getPnfAppDescriptor() != null) {
			msg.setPnfd(newPackage.getPnfAppDescriptor());
		}
		
		if (newPackage.getMetadata().getVmImages() != null) {
			msg.setVmImages(newPackage.getMetadata().getVmImages());
		}
		if (newPackage.getMetadata().getScriptsLink() != null) {
			msg.setScriptsLink(newPackage.getMetadata().getScriptsLink());
		}
		if (newPackage.getConfiguration() != null) {
			msg.setConfiguration(newPackage.getConfiguration());
		}
		if (newPackage.getMetadata().getAppClass() == AppClass.SDN_APP ||
			newPackage.getMetadata().getAppClass() == AppClass.SDN_CTRL_APP ) {
			msg.setAppImplId(newPackage.getMetadata().getAppTypeOrderId());
		}
		
		try {
			//send message over topic appfamily.appclass
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.setSerializationInclusion(Include.NON_EMPTY);
			String json = mapper.writeValueAsString(msg);
			
			String key = newPackage.getMetadata().getAppFamily().toString() + "." + newPackage.getMetadata().getAppClass().toString();
			
			rabbitTemplate.convertAndSend(topicExchange.getName(), key, json);
			
			log.info("Sent AppUpdateNotificationMessage to the message bus (key " + key + "): \n" + json);
			
		} catch (Exception e) {
			log.error("Error while posting AppUpdateNotificationMessage");
			throw new Exception(e.getMessage());
		}
	}
}
