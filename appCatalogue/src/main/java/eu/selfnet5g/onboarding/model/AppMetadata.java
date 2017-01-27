package eu.selfnet5g.onboarding.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class AppMetadata {
	
	@Id
	@JsonIgnore
	protected String id; 
	
	private AppFamily appFamily;
	
	private AppClass appClass;
	
	private String appType;
	
	//populated by SDNO after registration
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String appTypeOrderId;
	
	private String appName;
	
	private String appVersion;
	
	private boolean isPublic;
	
	private boolean upload;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
	@JsonIgnore
	//@JoinColumn(name="app_package_id")
	//@OnDelete(action = OnDeleteAction.CASCADE)
	private AppPackage appPackage;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@OneToMany(fetch=FetchType.EAGER, mappedBy = "appMetadata", cascade=CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<VMImage> vmImages = new HashSet<>();
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@OneToOne(fetch=FetchType.EAGER, mappedBy = "appMetadata", cascade=CascadeType.ALL, orphanRemoval=true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private AppArchive appArchive;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String scriptsLink;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@ElementCollection(fetch=FetchType.EAGER)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Set<String> vims = new HashSet<>();
	
	public AppMetadata() {
		//JPA only
	}
	
	public AppMetadata(AppPackage appPackage,
					   AppFamily appFamily,
					   AppClass appClass,
					   String  appType,
					   String  appTypeOrderId,
					   String appName,
					   String appVersion,
					   boolean isPublic,
					   boolean upload,
					   String scriptsLink,
					   Set<String> vims) {
		this.appPackage = appPackage;
		this.appFamily = appFamily;
		this.appClass = appClass;
		this.appType = appType;
		this.appTypeOrderId = appTypeOrderId;
		this.appName = appName;
		this.appVersion = appVersion;
		this.isPublic = isPublic;
		this.upload = upload;
		this.scriptsLink = scriptsLink;
		this.vims = vims;
	}
		
	@JsonIgnore
	@PrePersist
	public void ensureId() {
		id = UUID.randomUUID().toString();
	}
	
	@JsonProperty("app-family")
	public AppFamily getAppFamily() {
		return appFamily;
	}

	@JsonIgnore
	public String getId() {
		return id;
	}

	@JsonProperty("app-class")
	public AppClass getAppClass() {
		return appClass;
	}

	@JsonProperty("app-type")
	public String getAppType() {
		return appType;
	}
	
	@JsonProperty("app-name")
	public String getAppName() {
		return appName;
	}

	@JsonProperty("app-version")
	public String getAppVersion() {
		return appVersion;
	}

	@JsonProperty("public")
	public boolean getIsPublic() {
		return isPublic;
	}

	@JsonProperty("upload")
	public boolean getUpload() {
		return upload;
	}
	
	@JsonProperty("vm-images")
	public Set<VMImage> getVmImages() {
		return vmImages;
	}
	
	@JsonProperty("app-archive")
	public AppArchive getAppArchive() {
		return appArchive;
	}

	@JsonProperty("scripts-link")
	public String getScriptsLink() {
		return scriptsLink;
	}
	
	@JsonProperty("vims")
	public Set<String> getVims() {
		return vims;
	}
	
	@JsonProperty("app-impl-id")
	public String getAppTypeOrderId() {
		return appTypeOrderId;
	}
	
	@JsonIgnore
	public void setAppTypeOrderId(String orderId) {
		this.appTypeOrderId = orderId;
	}
	
	@JsonIgnore
	public AppPackage getAppPackage() {
		return appPackage;
	}
	
	@JsonIgnore
	public void setAppPackage(AppPackage pckg) {
		this.appPackage = pckg;
	}
}
