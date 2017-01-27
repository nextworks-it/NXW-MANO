package eu.selfnet5g.onboarding.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class AppMonitoring {

	@Id
	@JsonIgnore
	protected String id;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
	@JsonIgnore
	//@JoinColumn(name="app_package_id")
	//@OnDelete(action = OnDeleteAction.CASCADE)
	private AppPackage appPackage;
	
	@OneToMany(fetch=FetchType.EAGER, mappedBy = "appMonitoring", cascade=CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<MonitoringMetric> metrics = new HashSet<>();
	
	@OneToOne(fetch=FetchType.EAGER, mappedBy = "appMonitoring", cascade=CascadeType.ALL, orphanRemoval=true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private MonitoringCommunication communication;
	
	public AppMonitoring() {
		//JPA only
	}
	
	public AppMonitoring(AppPackage appPackage) {
		this.appPackage = appPackage;
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

	@JsonProperty("metrics")
	public Set<MonitoringMetric> getMetrics() {
		return metrics;
	}

	@JsonProperty("communication")
	public MonitoringCommunication getCommunication() {
		return communication;
	}
	
	@JsonIgnore
	public void setAppPackage(AppPackage pckg) {
		this.appPackage = pckg;
	}
	
	@JsonIgnore
	public AppPackage getAppPackage() {
		return appPackage;
	}
}
