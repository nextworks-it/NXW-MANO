package eu.selfnet5g.onboarding.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
@JsonSubTypes({
	@Type(value = AppOnboardNotificationMessage.class, 		name = "ONBOARD"),
	@Type(value = AppEnableNotificationMessage.class, 		name = "ENABLE"),
	@Type(value = AppDisableNotificationMessage.class,   	name = "DISABLE"),
	@Type(value = AppUpdateNotificationMessage.class,   	name = "UPDATE"),
	@Type(value = AppOffboardNotificationMessage.class,   	name = "DELETE")
})

public abstract class AppNotificationMessage {

	protected String appId;
		
	@JsonProperty("app-id")
	public String getAppId() {
		return appId;
	}
	
}

