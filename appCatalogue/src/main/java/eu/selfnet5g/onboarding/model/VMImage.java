package eu.selfnet5g.onboarding.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class VMImage {

	@Id
	@JsonIgnore
	protected String id;
	
	@JsonIgnore
	@ManyToOne
	private AppMetadata appMetadata;
	
	private String name;
	
	private String link;
	
	private ContainerFormat containerFormat;
	
	private DiskFormat diskFormat;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String minDisk;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String minCpu;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String minRam;
	
	public VMImage() {
		//JPA only
	}

	public VMImage(AppMetadata appMetadata,
				   String name,
				   String link,
				   DiskFormat diskFormat,
				   String minDisk,
				   String minCpu,
				   String minRam) {
		this.appMetadata = appMetadata;
		this.name = name;
		this.link = link;
		this.diskFormat = diskFormat;
		this.minDisk = minDisk;
		this.minCpu = minCpu;
		this.minRam = minRam;
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

	@JsonProperty("link")
	public String getLink() {
		return link;
	}
	
	@JsonProperty("container-format")
	public ContainerFormat getContainerFormat() {
		return containerFormat;
	}
	
	@JsonProperty("disk-format")
	public DiskFormat getDiskFormat() {
		return diskFormat;
	}

	@JsonProperty("min-disk")
	public String getMinDisk() {
		return minDisk;
	}

	@JsonProperty("min-cpu")
	public String getMinCpu() {
		return minCpu;
	}

	@JsonProperty("min-ram")
	public String getMinRam() {
		return minRam;
	}

}

