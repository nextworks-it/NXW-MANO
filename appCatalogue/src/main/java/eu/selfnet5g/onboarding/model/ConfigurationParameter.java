package eu.selfnet5g.onboarding.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class ConfigurationParameter {
	@Id
	@JsonIgnore
	protected String id;
	
	@JsonIgnore
	@ManyToOne
	private AppConfiguration appConfiguration;
	
	private String configAction;
	
	@OneToMany(fetch=FetchType.EAGER, mappedBy = "configurationParameter", cascade=CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<ConfigurationValue> values = new HashSet<>();
	
	public ConfigurationParameter() {
		//JPA only
	}
	
	public ConfigurationParameter(AppConfiguration appConfiguration,
							      String configAction) {
		this.appConfiguration = appConfiguration;
		this.configAction = configAction;
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

	@JsonProperty("config-values")
	public Set<ConfigurationValue> getValues() {
		return values;
	}
	
}
