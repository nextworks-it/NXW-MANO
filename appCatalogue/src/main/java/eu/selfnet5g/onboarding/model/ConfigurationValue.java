package eu.selfnet5g.onboarding.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class ConfigurationValue {
	@Id
	@JsonIgnore
	protected String id;
	
	@JsonIgnore
	@ManyToOne
	private ConfigurationParameter configurationParameter;
	
	private String parameter;
	
	private String name;
	
	public ConfigurationValue() {
		//JPA only
	}
	
	public ConfigurationValue(ConfigurationParameter configurationParameter,
							  String parameter,
							  String name) {
		this.configurationParameter = configurationParameter;
		this.parameter = parameter;
		this.name = name;
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

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("parameter")
	public String getParameter() {
		return parameter;
	}
}
