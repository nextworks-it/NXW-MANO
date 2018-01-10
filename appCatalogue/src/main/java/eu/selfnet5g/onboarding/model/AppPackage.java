package eu.selfnet5g.onboarding.model;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Entity
public class AppPackage {

	@Id
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	protected String id; 

	@OneToOne(fetch=FetchType.EAGER, mappedBy = "appPackage", cascade=CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private AppMetadata metadata;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@OneToOne(fetch=FetchType.EAGER, mappedBy = "appPackage", cascade=CascadeType.ALL, orphanRemoval=true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private AppMonitoring monitoring;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@OneToOne(fetch=FetchType.EAGER, mappedBy = "appPackage", cascade=CascadeType.ALL, orphanRemoval=true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private AppConfiguration configuration;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@OneToOne(fetch=FetchType.EAGER, mappedBy = "appPackage", cascade=CascadeType.ALL, orphanRemoval=true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private SDNAppDescriptor sdnAppDescriptor;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@OneToOne(fetch=FetchType.EAGER, mappedBy = "appPackage", cascade=CascadeType.ALL, orphanRemoval=true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private PNFAppDescriptor pnfAppDescriptor;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private AppPackageStatus status;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonRawValue
	@Column(length = 65536)
	private String vnfDescriptor; //it is stored as a json string
	
	public AppPackage() {
		//JPA Only
	}
	
	public AppPackage(String vnfDescriptor) {
		this.vnfDescriptor = vnfDescriptor;
		this.status = AppPackageStatus.ENABLED; //by default it's enabled
	}
	
	public AppPackage(AppMetadata metadata,
					  AppMonitoring monitoring,
					  AppConfiguration configuration,
					  SDNAppDescriptor sdnAppDescriptor,
					  PNFAppDescriptor pnfAppDescriptor,
					  String vnfDescriptor) {
		this.metadata = metadata;
		this.vnfDescriptor = vnfDescriptor;
		this.monitoring = monitoring;
		this.configuration = configuration;
		this.sdnAppDescriptor = sdnAppDescriptor;
		this.pnfAppDescriptor = pnfAppDescriptor;
		this.vnfDescriptor = vnfDescriptor;
		this.status = AppPackageStatus.ENABLED; //by default it's enabled
	}

	@JsonIgnore
	@PrePersist
	public void ensureId() {
		id = UUID.randomUUID().toString();
	}

	@JsonProperty("metadata")
	public AppMetadata getMetadata() {
		return metadata;
	}

	@JsonIgnore
	public void setMetadata(AppMetadata metadata) {
		this.metadata = metadata;
	}
	
	@JsonProperty("app-status")
	public AppPackageStatus getStatus() {
		return status;
	}

	@JsonIgnore
	public void setStatus(AppPackageStatus status) {
		this.status = status;
	}

	@JsonProperty("monitoring")
	public AppMonitoring getMonitoring() {
		return monitoring;
	}

	@JsonIgnore
	public void setMonitoring(AppMonitoring monitoring) {
		this.monitoring = monitoring;
	}

	@JsonProperty("configuration")
	public AppConfiguration getConfiguration() {
		return configuration;
	}

	@JsonIgnore
	public void setConfiguration(AppConfiguration configuration) {
		this.configuration = configuration;
	}

	@JsonProperty("sdn-descriptor")
	public SDNAppDescriptor getSdnAppDescriptor() {
		return sdnAppDescriptor;
	}

	@JsonIgnore
	public void setSdnAppDescriptor(SDNAppDescriptor sdnAppDescriptor) {
		this.sdnAppDescriptor = sdnAppDescriptor;
	}
	
	@JsonProperty("pnf-descriptor")
	public PNFAppDescriptor getPnfAppDescriptor() {
		return pnfAppDescriptor;
	}

	@JsonIgnore
	public void setPnfAppDescriptor(PNFAppDescriptor PnfAppDescriptor) {
		this.pnfAppDescriptor = pnfAppDescriptor;
	}

	@JsonProperty("vnf-descriptor")
	public String getVnfDescriptor() {
		return vnfDescriptor;
	}

	@JsonIgnore
	public VirtualNetworkFunctionDescriptor getVnfDescriptorObj() {
		
		VirtualNetworkFunctionDescriptor vnfd = null;
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			vnfd = mapper.readValue(vnfDescriptor, VirtualNetworkFunctionDescriptor.class);
		} catch (Exception e) {
			//do nothing?
		}
		
		return vnfd;
	}

	@JsonIgnore
	public void setVnfDescriptor(String vnfDescriptor) {
		this.vnfDescriptor = vnfDescriptor;
	}
	
	@JsonProperty("vnf-descriptor")
	public void setVnfDescriptor(VirtualNetworkFunctionDescriptor vnfDescriptor) {
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
			mapper.setSerializationInclusion(Include.NON_EMPTY);
			String json = mapper.writeValueAsString(vnfDescriptor);
			
			this.vnfDescriptor = json;
			
		} catch (Exception e) {
			//do nothing?
		}
	}

	@JsonProperty("app-id")
	public String getId() {
		return id;
	}
	
}
