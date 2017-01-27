package eu.selfnet5g.onboarding.interfaces.plugins;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV2;
import org.openstack4j.model.common.Payload;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.image.ContainerFormat;
import org.openstack4j.model.image.DiskFormat;
import org.openstack4j.model.image.Image;
import org.openstack4j.openstack.OSFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import eu.selfnet5g.onboarding.interfaces.AppRegistration;
import eu.selfnet5g.onboarding.model.AppMetadata;
import eu.selfnet5g.onboarding.model.VMImage;
import eu.selfnet5g.onboarding.model.Vim;
import eu.selfnet5g.onboarding.repo.VimRepository;

public class OpenStackGlancePlugin implements AppRegistration {

	private Logger log = LoggerFactory.getLogger(OpenStackGlancePlugin.class);

	@Value("${openstack.glance.skip:false}")
	private boolean skipGlance;
	
	@Autowired
	VimRepository vimRepository;
	
	private DiskFormat diskFormat(eu.selfnet5g.onboarding.model.DiskFormat disk) {
		
		switch (disk) {
			case RAW:
				return DiskFormat.RAW;
			case VHD:
				return DiskFormat.VHD;
			case VMDK:
				return DiskFormat.VMDK;
			case VDI:
				return DiskFormat.VDI;
			case ISO:
				return DiskFormat.ISO;
			case QCOW2:
				return DiskFormat.QCOW2;
			case AKI:
				return DiskFormat.AKI;
			case ARI:
				return DiskFormat.ARI;
			case AMI:
				return DiskFormat.AMI;
			default:
				return DiskFormat.QCOW2;
		}
		
	}
	
private ContainerFormat containerFormat(eu.selfnet5g.onboarding.model.ContainerFormat container) {
		
		switch (container) {
			case BARE:
				return ContainerFormat.BARE;
			case OVF:
				return ContainerFormat.OVF;
			case DOCKER:
				return ContainerFormat.DOCKER;
			case AKI:
				return ContainerFormat.AKI;
			case ARI:
				return ContainerFormat.ARI;
			case AMI:
				return ContainerFormat.AMI;
			default:
				return ContainerFormat.BARE;
		}
		
	}
	
	private OSClientV2 getOpenStack(String vimName) throws Exception {
		
		Optional<Vim> vim = vimRepository.findByName(vimName);
		if (!vim.isPresent()) {
			throw new Exception("VIM " + vimName + "not configured.");
		}
			
		try {
		
			OSClientV2 os = OSFactory.builderV2()
	                .endpoint(vim.get().getUrl())
	                .credentials(vim.get().getUsername(), vim.get().getPassword())
	                .tenantName(vim.get().getTenant())             
	                .authenticate();
			  
			return os;
			
		} catch (Exception e) {
			log.error("Cannot authenticate to Openstack: " + e.getMessage());
			throw new Exception(e.getMessage());
		}
	}
	
	private String getImageId(OSClientV2 os, String vmName) throws Exception {
		
		List<? extends Image> images = os.images().list();
		for (Image image : images) {
			if (image.getName().equalsIgnoreCase(vmName)) {
				return image.getId();
			}
		}
		return null;
	}
	
	public String registerNewApp(AppMetadata metadata) throws Exception {
		
		log.info("Going to upload images for VNF " + metadata.getAppType() + " to OpenStack instances " + metadata.getVims());
		
		if (skipGlance) {
			log.info("Skipping interaction with OpenStack for test purposes");
			return "NULL";
		}
		
		try {
			
			for (VMImage vm : metadata.getVmImages()) {
				log.info("Creating image " + vm.getName());
			
				Payload<URL> payload = Payloads.create(new URL(vm.getLink()));
				
				for (String vimName : metadata.getVims()) {
					
					OSClientV2 os = getOpenStack(vimName);
					
					String imageId = getImageId(os, vm.getName());
					
					if (metadata.getUpload() && (imageId != null)) {
						throw new Exception("Image " + vm.getName() + " already uploaded");
					}
					if (!metadata.getUpload() && (imageId == null)) {
						throw new Exception("Image " + vm.getName() + " not already uploaded");
					}
					
					Image image = os.images().create(Builders.image()
			                .name(vm.getName())
			                .isPublic(metadata.getIsPublic())
			                .containerFormat(containerFormat(vm.getContainerFormat()))
			                .diskFormat(diskFormat(vm.getDiskFormat()))
			                .build(), payload);
					
					log.info("Created image " + image.getId() + " to OpenStack instance " + vimName);					
				}				
			}
			
			return "OK";
			
		} catch (Exception e) {
			log.error("Error while creating image: " + e.getMessage());
			throw new Exception(e.getMessage());
		}
	}
	
	public void unregisterApp(AppMetadata metadata) throws Exception {
		
		log.info("Going to delete images for VNF " + metadata.getAppType() + " to OpenStack instances " + metadata.getVims());
		
		if (skipGlance) {
			log.info("Skipping interaction with OpenStack for test purposes");
			return;
		}
		
		try {
			for (VMImage vm : metadata.getVmImages()) {
				log.info("Deleting image " + vm.getName());
							
				for (String vimName : metadata.getVims()) {
					OSClientV2 os = getOpenStack(vimName);
					
					String imageId = getImageId(os, vm.getName());
					
					if (imageId == null) {
						log.info("Image " + vm.getName() + "  already deleted");
						continue;
					}
					
					os.images().delete(imageId);
					
					log.info("Delete image " + imageId + " to OpenStack instance " + vimName);					
				}				
			}
			
		} catch (Exception e) {
			log.error("Error while deleting image: " + e.getMessage());
			throw new Exception(e.getMessage());
		}
	}
	
}
	
	
	
	
    