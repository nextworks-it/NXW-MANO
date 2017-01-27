package eu.selfnet5g.onboarding.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import eu.selfnet5g.onboarding.model.AppConfiguration;
import eu.selfnet5g.onboarding.model.AppMonitoring;
import eu.selfnet5g.onboarding.model.SDNAppDescriptor;

public class AppUpdateNotificationMessage extends AppNotificationMessage {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String appImplId;
	
	private String appName;
	
	private String appVersion;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonRawValue
	private String vnfd;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String scriptsLink;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private SDNAppDescriptor sdnd;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private AppConfiguration configuration;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private AppMonitoring monitoring;
	
	public AppUpdateNotificationMessage(String appId,
										String appName,
										String appVersion) {
		this.appId = appId;
		this.appName = appName;
		this.appVersion = appVersion;
	}

	@JsonProperty("new-app-name")
	public String getAppName() {
		return appName;
	}

	@JsonProperty("new-app-version")
	public String getAppVersion() {
		return appVersion;
	}

	@JsonProperty("new-vnf-descriptor")
	public String getVnfd() {
		return vnfd;
	}

	@JsonIgnore
	public void setVnfd(String vnfd) {
		this.vnfd = vnfd;
	}

	@JsonProperty("new-scripts-link")
	public String getScriptsLink() {
		return scriptsLink;
	}

	@JsonIgnore
	public void setScriptsLink(String scriptsLink) {
		this.scriptsLink = scriptsLink;
	}

	@JsonProperty("new-sdn-descriptor")
	public SDNAppDescriptor getSdnd() {
		return sdnd;
	}

	@JsonIgnore
	public void setSdnd(SDNAppDescriptor sdnd) {
		this.sdnd = sdnd;
	}

	@JsonProperty("new-configuration")
	public AppConfiguration getConfiguration() {
		return configuration;
	}

	@JsonIgnore
	public void setConfiguration(AppConfiguration configuration) {
		this.configuration = configuration;
	}

	@JsonProperty("new-monitoring")
	public AppMonitoring getMonitoring() {
		return monitoring;
	}

	@JsonIgnore
	public void setMonitoring(AppMonitoring monitoring) {
		this.monitoring = monitoring;
	}
	
	@JsonProperty("app-impl-id")
	public String getAppImplId() {
		return appImplId;
	}
	
	@JsonIgnore
	public void setAppImplId(String appImplId) {
		this.appImplId = appImplId;
	}
	
	
}
