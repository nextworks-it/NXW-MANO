package eu.selfnet5g.onboarding.interfaces.plugins;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV2;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
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
	
	private boolean isV2(String vimName) throws Exception {
		
		Optional<Vim> vim = vimRepository.findByName(vimName);
		if (!vim.isPresent()) {
			throw new Exception("VIM " + vimName + "not configured.");
		}
		
		if (vim.get().getUrl().endsWith("v2.0") ||
			vim.get().getUrl().endsWith("v2.0/")) {
			return true;
		}
		
		return false;
	}

	private OSClientV2 getOpenStackV2(String vimName) throws Exception {
		
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

	private OSClientV3 getOpenStackV3(String vimName) throws Exception {
		
		Optional<Vim> vim = vimRepository.findByName(vimName);
		if (!vim.isPresent()) {
			throw new Exception("VIM " + vimName + "not configured.");
		}
			
		try {
		
			OSClientV3 os = OSFactory.builderV3()
                    .endpoint(vim.get().getUrl())
                    .credentials(vim.get().getUsername(), vim.get().getPassword(), Identifier.byName("default"))
                    .scopeToProject(Identifier.byId(vim.get().getTenant()))
                    .authenticate();
			  
			return os;
			
		} catch (Exception e) {
			log.error("Cannot authenticate to Openstack: " + e.getMessage());
			throw new Exception(e.getMessage());
		}
	}
	
	private String getImageId(OSClientV3 os, String vmName) throws Exception {
		
		List<? extends Image> images = os.images().list();

		for (Image image : images) {
			if (image.getName().equalsIgnoreCase(vmName)) {
				return image.getId();
			}
		}
		return null;
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
	
	private boolean getImage(OSClientV3 os, String vmId) throws Exception {
		
		Image image = os.images().get(vmId);
		if (image == null) {
			return false;
		}
		return true;
	}
	
	private boolean getImage(OSClientV2 os, String vmId) throws Exception {
		
		Image image = os.images().get(vmId);
		if (image == null) {
			return false;
		}
		return true;
	}
	
	
	private void manageRegisterImagev2(String vimName, VMImage vm, boolean upload, boolean isPublic, Map<String,String> res) throws Exception {
		String imageId = null;
		
		OSClientV2 os = getOpenStackV2(vimName);
		
		if (vm.getVimId() != null) {
			if (upload) {
				throw new Exception("Cannot register image " + vm.getVimId() + ". Upload flag set to true.");
			}
			//the ckeck of upload==false has been already performed in the onboarding manager
			if (!getImage(os, vm.getVimId())) {
				throw new Exception("Image " + vm.getVimId() + " not already uploaded");
			}
			log.info("Image " + vm.getName() + ":" + vm.getVimId() + " already in OpenStack instance " + vimName + " as expected");
			res.put(vm.getName(), vm.getVimId());
			return;
		} else {

			//no vimId in onboarding request
			imageId = getImageId(os, vm.getName());

			if (upload && (imageId != null)) {
				throw new Exception("Image " + vm.getName() + " already uploaded");
			}
			if (!upload && (imageId == null)) {
				throw new Exception("Image " + vm.getName() + " not already uploaded");
			}
			if (!upload && (imageId != null)) {
				log.info("Image " + vm.getName() + " already in OpenStack instance " + vimName + " as expected with id " + imageId);
				res.put(vm.getName(), imageId);
				return;
			}
		}
		
		Payload<URL> payload = Payloads.create(new URL(vm.getLink()));
		
		Image image = os.images().create(Builders.image()
                .name(vm.getName())
                .isPublic(isPublic)
                .containerFormat(containerFormat(vm.getContainerFormat()))
                .diskFormat(diskFormat(vm.getDiskFormat()))
                .build(), payload);
		
		log.info("Created image " + vm.getName() + ":" + image.getId() + " to OpenStack instance " + vimName);
		res.put(vm.getName(), image.getId());		
	}
	
	private void manageRegisterImagev3(String vimName, VMImage vm, boolean upload, boolean isPublic, Map<String,String> res) throws Exception {
		
		String imageId = null;
		
		OSClientV3 os = getOpenStackV3(vimName);
		
		if (vm.getVimId() != null) {
			//the ckeck of upload==false has been already performed in the onboarding manager
			if (!getImage(os, vm.getVimId())) {
				throw new Exception("Image " + vm.getVimId() + " not already uploaded");
			}
			log.info("Image " + vm.getName() + ":" + vm.getVimId() + " already in OpenStack instance " + vimName + " as expected");
			res.put(vm.getName(), vm.getVimId());
			return;
		} else {

			//no vimId in onboarding request
			imageId = getImageId(os, vm.getName());

			if (upload && (imageId != null)) {
				throw new Exception("Image " + vm.getName() + " already uploaded");
			}
			if (!upload && (imageId == null)) {
				throw new Exception("Image " + vm.getName() + " not already uploaded");
			}
			if (!upload && (imageId != null)) {
				log.info("Image " + vm.getName() + " already in OpenStack instance " + vimName + " as expected with id " + imageId);
				res.put(vm.getName(), imageId);
				return;
			}
		}
		
		Payload<URL> payload = Payloads.create(new URL(vm.getLink()));
		
		Image image = os.images().create(Builders.image()
                .name(vm.getName())
                .isPublic(isPublic)
                .containerFormat(containerFormat(vm.getContainerFormat()))
                .diskFormat(diskFormat(vm.getDiskFormat()))
                .build(), payload);
		
		log.info("Created image " + vm.getName() + ":" + image.getId() + " to OpenStack instance " + vimName);
		res.put(vm.getName(), image.getId());
	}
	
	public Map<String,String> registerNewApp(AppMetadata metadata) throws Exception {
		
		log.info("Going to upload images for VNF " + metadata.getAppType() + " to OpenStack instances " + metadata.getVims());
		
		Map<String,String> res = new HashMap<>();
		
		if (skipGlance) {
			log.info("Skipping interaction with OpenStack for test purposes");
			
			for (VMImage vm : metadata.getVmImages()) {
				res.put(vm.getName(), UUID.randomUUID().toString());
			
			}
			return res;
		}
		
		try {
			
			for (VMImage vm : metadata.getVmImages()) {
				log.info("Creating image " + vm.getName());
				
				for (String vimName : metadata.getVims()) {
					
					if (isV2(vimName)) {
						manageRegisterImagev2(vimName, vm, metadata.getUpload(), metadata.getIsPublic(), res);
					} else {
						manageRegisterImagev3(vimName, vm, metadata.getUpload(), metadata.getIsPublic(), res);
					}				
				}				
			}
			
			return res;
			
		} catch (Exception e) {
			log.error("Error while creating image: " + e.getMessage());
			throw new Exception(e.getMessage());
		}
	}
	
	private void unregisterImageV2(String vimName, String imageId) throws Exception {
		OSClientV2 os = getOpenStackV2(vimName);
		
		if (!getImage(os, imageId)) {
			log.info("Image " + imageId + "  already deleted");
			return;
		}
		
		os.images().delete(imageId);
		
		log.info("Deleted image " + imageId + " to OpenStack instance " + vimName);
	}
	
	private void unregisterImageV3(String vimName, String imageId) throws Exception {
		OSClientV3 os = getOpenStackV3(vimName);
		
		if (!getImage(os, imageId)) {
			log.info("Image " + imageId + "  already deleted");
			return;
		}
		
		os.images().delete(imageId);
		
		log.info("Deleted image " + imageId + " to OpenStack instance " + vimName);
	}
	
	public void unregisterApp(AppMetadata metadata) throws Exception {
		
		log.info("Going to delete images for VNF " + metadata.getAppType() + " to OpenStack instances " + metadata.getVims());
		
		if (skipGlance) {
			log.info("Skipping interaction with OpenStack for test purposes");
			return;
		}
		
		if (!metadata.getUpload()) {
			log.info("Images were not uploaded by app-catalogue. Skip delete.");
			return;
		}
		
		try {
			for (VMImage vm : metadata.getVmImages()) {
				log.info("Deleting image " + vm.getName());
							
				for (String vimName : metadata.getVims()) {
					
					if (isV2(vimName)) {
						unregisterImageV2(vimName, vm.getVimId());
					} else {
						unregisterImageV3(vimName, vm.getVimId());
					}					
				}				
			}
			
		} catch (Exception e) {
			log.error("Error while deleting image: " + e.getMessage());
			throw new Exception(e.getMessage());
		}
	}
	
}
	
	
	
	
    