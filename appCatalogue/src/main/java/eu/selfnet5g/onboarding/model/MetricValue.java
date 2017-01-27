package eu.selfnet5g.onboarding.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class MetricValue {

	@Id
	@JsonIgnore
	protected String id;
	
	@JsonIgnore
	@ManyToOne
	private MonitoringMetric monitoringMetric;
	
	private String name;
	
	private String resource;
	
	private String unit;
	
	private MetricValueType type;
	
	public MetricValue(MonitoringMetric monitoringMetric,
			           String name,
			           String resource,
			           String unit,
			           MetricValueType type) {
		this.monitoringMetric = monitoringMetric;
		this.name = name;
		this.resource = resource;
		this.unit = unit;
		this.type = type;
	}
	
	public MetricValue() {
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

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("resource")
	public String getResource() {
		return resource;
	}

	@JsonProperty("unit")
	public String getUnit() {
		return unit;
	}

	@JsonProperty("type")
	public MetricValueType getType() {
		return type;
	}
	
	
}
