package eu.selfnet5g.onboarding.model;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class AppArchive {

	@Id
	@JsonIgnore
	protected String id;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
	@JsonIgnore
	@JoinColumn(name="app_metadata_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private AppMetadata appMetadata;
	
	private String name;
	
	private ArchiveType type;
	
	private String link;
	
	public AppArchive() {
		//JPA only
	}
	
	public AppArchive(AppMetadata appMetadata,
					  String name,
					  ArchiveType type,
					  String link) {
		this.appMetadata = appMetadata;
		this.name = name;
		this.type = type;
		this.link = link;
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

	@JsonProperty("type")
	public ArchiveType getType() {
		return type;
	}

	@JsonProperty("link")
	public String getLink() {
		return link;
	}
	
	
}
