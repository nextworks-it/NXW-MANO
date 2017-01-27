package eu.selfnet5g.onboarding.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class MonitoringEndpoint {

	@Id
	@JsonIgnore
	protected String id;
	
	@JsonIgnore
	@ManyToOne
	private MonitoringCommunication monitoringCommunication;
	
	private String metric;
	
	private String value;
	
	public MonitoringEndpoint() {
		//JPA only
	}
	
	public MonitoringEndpoint(MonitoringCommunication monitoringCommunication,
					          String metric,
					          String value) {
		this.monitoringCommunication = monitoringCommunication;
		this.metric = metric;
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

	@JsonProperty("metric")
	public String getMetric() {
		return metric;
	}

	@JsonProperty("value")
	public String getValue() {
		return value;
	}
	
	
}
