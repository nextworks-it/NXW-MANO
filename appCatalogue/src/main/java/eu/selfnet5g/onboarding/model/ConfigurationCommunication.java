package eu.selfnet5g.onboarding.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class ConfigurationCommunication {
	@Id
	@JsonIgnore
	protected String id;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
	@JsonIgnore
	@JoinColumn(name="app_configuration_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private AppConfiguration appConfiguration;
	
	private String protocol;
	
	@OneToMany(fetch=FetchType.EAGER, mappedBy = "configurationCommunication", cascade=CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<ConfigurationEndpoint> endpoints = new HashSet<>();
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@ElementCollection
	private Map<String,String> metadata = new HashMap<>();
	
	public ConfigurationCommunication(AppConfiguration appConfiguration,
									  String protocol) {
		this.appConfiguration = appConfiguration;
		this.protocol = protocol;
	}
	
	public ConfigurationCommunication() {
		//JPA only
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

	@JsonProperty("protocol")
	public String getProtocol() {
		return protocol;
	}

	@JsonProperty("endpoints")
	public Set<ConfigurationEndpoint> getEndpoints() {
		return endpoints;
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
