package eu.selfnet5g.onboarding.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class LifecycleAction {

	@Id
	@JsonIgnore
	protected String id;
	
	@JsonIgnore
	@ManyToOne
	private PNFAppDescriptor pnfDescriptor;
	
	private LifecycleEventType event;
	
	private String action;

	//JPA
	public LifecycleAction() {
	}
	
	public LifecycleAction(PNFAppDescriptor pnfDescriptor,
						   LifecycleEventType event,
						   String action) {
		this.pnfDescriptor = pnfDescriptor;
		this.event = event;
		this.action = action;
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

	@JsonProperty("event")
	public LifecycleEventType getEvent() {
		return event;
	}

	@JsonProperty("action")
	public String getAction() {
		return action;
	}
}
