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
public class MonitoringCommunication {

	@Id
	@JsonIgnore
	protected String id;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
	@JsonIgnore
	@JoinColumn(name="app_monitoring_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private AppMonitoring appMonitoring;
	
	private String protocol;
	
	@OneToMany(fetch=FetchType.EAGER, mappedBy = "monitoringCommunication", cascade=CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<MonitoringEndpoint> endpoints = new HashSet<>();;
	
	private CommunicationMethod method;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@ElementCollection
	private Map<String,String> metadata = new HashMap<>();
	
	public MonitoringCommunication(AppMonitoring appMonitoring,
					               String protocol,
					               CommunicationMethod method) {
		this.appMonitoring = appMonitoring;
		this.protocol = protocol;
		this.method = method;
	}
	
	public MonitoringCommunication() {
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
	public Set<MonitoringEndpoint> getEndpoints() {
		return endpoints;
	}

	@JsonProperty("method")
	public CommunicationMethod getMethod() {
		return method;
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
