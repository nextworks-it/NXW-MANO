package eu.selfnet5g.onboarding.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class SDNAppDescriptor {

	@Id
	@JsonIgnore
	protected String id;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
	@JsonIgnore
	//@JoinColumn(name="app_package_id")
	//@OnDelete(action = OnDeleteAction.CASCADE)
	private AppPackage appPackage;
	
	private String vendor;
	
	private String name;
	
	private SDNAppType type;
	
	private String version;
	
	private SDNControllerType controller;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@ElementCollection
	private Map<String,String> metadata = new HashMap<>();
	
	public SDNAppDescriptor(AppPackage appPackage,
						    String vendor,
						    String name,
						    SDNAppType type,
						    String version,
						    SDNControllerType controller) {
		this.appPackage = appPackage;
		this.vendor = vendor;
		this.name = name;
		this.type = type;
		this.version = version;
		this.controller = controller;
	}
	
	public SDNAppDescriptor() {
		
	}
	
	@JsonIgnore
	@PrePersist
	public void ensureId() {
		id = UUID.randomUUID().toString();
	}

	@JsonIgnore
	public String getId() {
		return id;
	}

	@JsonProperty("vendor")
	public String getVendor() {
		return vendor;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("type")
	public SDNAppType getType() {
		return type;
	}

	@JsonProperty("version")
	public String getVersion() {
		return version;
	}

	@JsonProperty("controller")
	public SDNControllerType getController() {
		return controller;
	}
	
	@JsonIgnore
	public void setAppPackage(AppPackage pckg) {
		this.appPackage = pckg;
	}
	
	@JsonIgnore
	public AppPackage getAppPackage() {
		return appPackage;
	}

	@JsonProperty("metadata")
	public Map<String,String> getMetadata() {
		return metadata;
	}
	
	@JsonIgnore
	public void addMetadataKeyValue(String key, String value) {
		this.metadata.put(key, value);
	}
	
}
