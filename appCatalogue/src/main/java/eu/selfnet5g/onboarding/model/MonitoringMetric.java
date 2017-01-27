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
public class MonitoringMetric {

	@Id
	@JsonIgnore
	protected String id;
	
	@JsonIgnore
	@ManyToOne
	private AppMonitoring appMonitoring;
	
	private String metricName;
	
	@OneToMany(fetch=FetchType.EAGER, mappedBy = "monitoringMetric", cascade=CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<MetricValue> values = new HashSet<>();
	
	public MonitoringMetric(AppMonitoring appMonitoring,
							String metricName) {
		this.appMonitoring = appMonitoring;
		this.metricName = metricName;
	}
	
	public MonitoringMetric() {
		
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

	@JsonProperty("metric-name")
	public String getMetricName() {
		return metricName;
	}

	@JsonProperty("metric-values")
	public Set<MetricValue> getValues() {
		return values;
	}
	
	
}
