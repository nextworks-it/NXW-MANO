package eu.selfnet5g.onboarding.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class ConfigurationEndpoint {
	
	@Id
	@JsonIgnore
	protected String id;
	
	@JsonIgnore
	@ManyToOne
	private ConfigurationCommunication configurationCommunication;
	
	private String configAction;
	
	@Column(length = 65536)
	private String value;
	
	public ConfigurationEndpoint() {
		//JPA only
	}
	
	public ConfigurationEndpoint(ConfigurationCommunication configurationCommunication,
								 String configAction,
								 String value) {
		this.configurationCommunication = configurationCommunication;
		this.configAction = configAction;
		this.value = value;
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

	@JsonProperty("config-action")
	public String getConfigAction() {
		return configAction;
	}

	@JsonProperty("value")
	public String getValue() {
		return value;
	}
}
