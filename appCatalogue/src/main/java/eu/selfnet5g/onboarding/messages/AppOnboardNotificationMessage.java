package eu.selfnet5g.onboarding.messages;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import eu.selfnet5g.onboarding.model.AppClass;
import eu.selfnet5g.onboarding.model.AppConfiguration;
import eu.selfnet5g.onboarding.model.AppFamily;
import eu.selfnet5g.onboarding.model.AppMonitoring;
import eu.selfnet5g.onboarding.model.PNFAppDescriptor;
import eu.selfnet5g.onboarding.model.SDNAppDescriptor;
import eu.selfnet5g.onboarding.model.VMImage;

public class AppOnboardNotificationMessage extends AppNotificationMessage {

	private AppFamily appFamily;
	
	private AppClass appClass;
	
	private String appType;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String appImplId;
	
	private String appName;
	
	private String appVersion;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonRawValue
	private String vnfd;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Set<VMImage> vmImages = new HashSet<>();
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String scriptsLink;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private SDNAppDescriptor sdnd;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private PNFAppDescriptor pnfd;
	
	private AppConfiguration configuration;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private AppMonitoring monitoring;
	
	public AppOnboardNotificationMessage(String appId,
										 AppFamily appFamily,
										 AppClass appClass,
										 String appType,
										 String appName,
										 String appVersion) {
		this.appId = appId;
		this.appFamily = appFamily;
		this.appClass = appClass;
		this.appType = appType;
		this.appName = appName;
		this.appVersion = appVersion;
	}

	@JsonProperty("app-family")
	public AppFamily getAppFamily() {
		return appFamily;
	}

	@JsonProperty("app-class")
	public AppClass getAppClass() {
		return appClass;
	}

	@JsonProperty("app-type")
	public String getAppType() {
		return appType;
	}
	
	@JsonProperty("app-impl-id")
	public String getAppImplId() {
		return appImplId;
	}
	
	@JsonIgnore
	public void setAppImplId(String appImplId) {
		this.appImplId = appImplId;
	}
	

	@JsonProperty("app-name")
	public String getAppName() {
		return appName;
	}

	@JsonProperty("app-version")
	public String getAppVersion() {
		return appVersion;
	}

	@JsonProperty("vnf-descriptor")
	public String getVnfd() {
		return vnfd;
	}

	@JsonIgnore
	public void setVnfd(String vnfd) {
		this.vnfd = vnfd;
	}
	
	@JsonProperty("vm-images")
	public Set<VMImage> getVmImages() {
		return vmImages;
	}
	
	@JsonIgnore
	public void setVmImages(Set<VMImage> vmImages) {
		this.vmImages = vmImages;
	}

	@JsonProperty("scripts-link")
	public String getScriptsLink() {
		return scriptsLink;
	}

	@JsonIgnore
	public void setScriptsLink(String scriptsLink) {
		this.scriptsLink = scriptsLink;
	}

	@JsonProperty("sdn-descriptor")
	public SDNAppDescriptor getSdnd() {
		return sdnd;
	}

	@JsonIgnore
	public void setSdnd(SDNAppDescriptor sdnd) {
		this.sdnd = sdnd;
	}
	
	@JsonProperty("pnf-descriptor")
	public PNFAppDescriptor getPnfd() {
		return pnfd;
	}

	@JsonIgnore
	public void setPnfd(PNFAppDescriptor pnfd) {
		this.pnfd = pnfd;
	}

	@JsonProperty("configuration")
	public AppConfiguration getConfiguration() {
		return configuration;
	}

	@JsonIgnore
	public void setConfiguration(AppConfiguration configuration) {
		this.configuration = configuration;
	}

	@JsonProperty("monitoring")
	public AppMonitoring getMonitoring() {
		return monitoring;
	}

	@JsonIgnore
	public void setMonitoring(AppMonitoring monitoring) {
		this.monitoring = monitoring;
	}
}
