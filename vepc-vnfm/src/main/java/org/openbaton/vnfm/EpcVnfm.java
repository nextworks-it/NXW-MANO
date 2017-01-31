package org.openbaton.vnfm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.common.vnfm_sdk.rest.AbstractVnfmSpringReST;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement;
import org.openbaton.plugin.utils.RabbitPluginBroker;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.vim.drivers.VimDriverCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.openbaton")
public class EpcVnfm extends AbstractVnfmSpringReST{
	
	private final Logger log = LoggerFactory.getLogger(EpcVnfm.class);
	
	@Autowired
    private ConfigurableApplicationContext context;

	private ResourceManagement resourceManagement;

    private VimDriverCaller client;
	
	public static void main(String[] args){
		SpringApplication.run(EpcVnfm.class);
    }
	
	public void printBeans() {
		System.out.println(Arrays.asList(context.getBeanDefinitionNames()));
	}

    @Override
    protected void setup() {
    	log.info("Setting up vEPC VNFM");
        super.setup();
        printBeans();
        log.info("vEPC VNFM setup");
        
        log.info("Starting Resource Management");
        //Fetching the OpenstackVim using the openstack-plugin
        resourceManagement = (ResourceManagement) context.getBean("openstackVIM", 19345, "15672");
        //resourceManagement = (ResourceManagement) context.getBean("openstackVIM", 19345, "15672");
        log.info("Created resource management");
        
        //Using the openstack-plugin directly
        client = (VimDriverCaller) ((RabbitPluginBroker) context.getBean("rabbitPluginBroker")).
        		getVimDriverCaller("localhost", "admin", "openbaton", 5672, "openstack", "openstack", "15672");
        log.info("Created VIM driver caller");	
    }

	/**
	   * This method needs to set all the parameter specified in the VNFDependency.parameters
	   *
	   * @param virtualNetworkFunctionRecord
	   */
	@Override
	protected void fillSpecificProvides(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
		log.debug("================================= Invoked fillSpecificProvides method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		//TODO:
	}
	
	/**
     * This operation allows creating a VNF instance.
     *
     * @param virtualNetworkFunctionRecord
     * @param scripts
     * @param vimInstances
     */
	@Override
	public VirtualNetworkFunctionRecord instantiate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
			Object scripts, Map<String, Collection<VimInstance>> vimInstances) throws Exception {
		
		log.debug("================================= Invoked instantiate method for VNFR \n " + virtualNetworkFunctionRecord);
		log.debug("========================================================================================");
		  log.debug("Processing allocation of Resources for vnfr: " + virtualNetworkFunctionRecord.getName());
		  
		  /**
		     * Allocation of Resources
		     *  the grant operation is already done before this method
		     */
		    
		  String userdata = "";			//User data are used for cloud initi
		    
		    Iterator<ConfigurationParameter> configIterator = virtualNetworkFunctionRecord.getConfigurations().getConfigurationParameters().iterator();
		    while (configIterator.hasNext()) {
		        ConfigurationParameter configurationParameter = configIterator.next();
		        log.debug("Configuration Parameter: " + configurationParameter);
		        
		        //TODO: Add processing for specific configuration parameters 
		        //The following is just an example to write cloud init scripts
		        /*
		        if (configurationParameter.getConfKey().equals("GROUP_NAME")) {
		            userdata = "export GROUP_NAME=" + configurationParameter.getValue() + "\n";
		            userdata += "echo $GROUP_NAME > /home/ubuntu/group_name.txt\n";
		        }
		        */
		    }
		    userdata += getUserData();
		    
		    //TODO: change gw IP
//		    userdata += "runcmd:\n";
		    userdata += "ip route del default\n";
		    userdata += "ip route add default via 192.168.122.1";
		    
		    log.debug("Userdata: \n" + userdata);

		    log.debug("Processing allocation of Recources for vnfr: \n" + virtualNetworkFunctionRecord);
		    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
		    	
		        VimInstance vimInstance = vimInstances.get(vdu.getParent_vdu()).iterator().next();
		        log.debug("Creating " + vdu.getVnfc().size() + " VMs");
		        for (VNFComponent vnfComponent : vdu.getVnfc()) {
		            Map<String, String> floatingIps = new HashMap<>();
		            for (VNFDConnectionPoint connectionPoint : vnfComponent.getConnection_point()) {
		                if (connectionPoint.getFloatingIp() != null && !connectionPoint.getFloatingIp().equals("")) {
		                    floatingIps.put(connectionPoint.getVirtual_link_reference(), connectionPoint.getFloatingIp());
		                }
		            }
		            try {
		                VNFCInstance vnfcInstance = resourceManagement.allocate(vimInstance, vdu, 
		                		virtualNetworkFunctionRecord, vnfComponent, userdata, floatingIps, new HashSet<Key>()).get();
		                log.debug("Created VNFCInstance with id: " + vnfcInstance);
		                vdu.getVnfc_instance().add(vnfcInstance);
		            } catch (VimException e) {
		                log.error(e.getMessage());
		                if (log.isDebugEnabled())
		                    log.error(e.getMessage(), e);
		            }
		        }
		    }
		    log.debug("Allocated all Resources for vnfr: " + virtualNetworkFunctionRecord);
		    log.debug("=================================================End of instantiate method");
		    log.debug("========================================================================================");
		    return virtualNetworkFunctionRecord;
	}

	/**
     * This operation allows providing notifications on state changes
     * of a VNF instance, related to the VNF Lifecycle.
     */
	@Override
	public void NotifyChange() {
		// TODO Auto-generated method stub
		
	}

	/**
     * This operation allows retrieving
     * VNF instance state and attributes.
     */
	@Override
	public void query() {
		log.debug("================================= Invoked query method");
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		
	}

	@Override
	public VirtualNetworkFunctionRecord scale(Action scaleInOrOut,
			VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFComponent component, Object scripts,
			VNFRecordDependency dependency) throws Exception {
		// TODO Auto-generated method stub
		log.debug("================================= Invoked scale method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		return virtualNetworkFunctionRecord;
	}

	/**
     * This operation allows verifying if
     * the VNF instantiation is possible.
     */
	@Override
	public void checkInstantiationFeasibility() {
		log.debug("================================= Invoked checkInstantiationFeasibility method");
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		
	}

	/**
     * This operation is called when one the VNFs fails
     */
	@Override
	public VirtualNetworkFunctionRecord heal(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
			VNFCInstance component, String cause) throws Exception {
		log.debug("================================= Invoked heal method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		return virtualNetworkFunctionRecord;
	}

	/**
     * This operation allows applying a minor/limited
     * software update (e.g. patch) to a VNF instance.
     */
	@Override
	public VirtualNetworkFunctionRecord updateSoftware(Script script,
			VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception {
		log.debug("================================= Invoked updateSoftware method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		return virtualNetworkFunctionRecord;
	}

	/**
     * This operation allows making structural changes
     * (e.g. configuration, topology, behavior,
     * redundancy model) to a VNF instance.
     *
     * @param virtualNetworkFunctionRecord
     * @param dependency
     */
	@Override
	public VirtualNetworkFunctionRecord modify(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
			VNFRecordDependency dependency) throws Exception {
		log.debug("================================= Invoked modify method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		return virtualNetworkFunctionRecord;
	}

	 /**
     * This operation allows deploying a new
     * software release to a VNF instance.
     */
	@Override
	public void upgradeSoftware() {
		log.debug("================================= Invoked upgradeSoftware method");
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		
	}

	/**
     * This operation allows terminating gracefully
     * or forcefully a previously created VNF instance.
     *
     * @param virtualNetworkFunctionRecord
     */
	@Override
	public VirtualNetworkFunctionRecord terminate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
			throws Exception {
		log.debug("================================= Invoked terminate method");
		  log.info("Terminating vnfr with id " + virtualNetworkFunctionRecord.getId());
		  String projectId = virtualNetworkFunctionRecord.getProjectId();
		  boolean sslEnabled = false;
		    NFVORequestor nfvoRequestor = new NFVORequestor("admin", "openbaton", projectId, sslEnabled, "localhost", "8080", "1");
		    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
		        Set<VNFCInstance> vnfciToRem = new HashSet<>();
		        List<VimInstance> vimInstances = new ArrayList<>();
		        VimInstance vimInstance = null;
		        try {
		            vimInstances = nfvoRequestor.getVimInstanceAgent().findAll();
		        } catch (SDKException e) {
		            log.error(e.getMessage(), e);
		        } catch (ClassNotFoundException e) {
		            log.error(e.getMessage(), e);
		        }
		        for (VimInstance vimInstanceFind : vimInstances) {
		            if (vdu.getVimInstanceName().contains(vimInstanceFind.getName())) {
		                vimInstance = vimInstanceFind;
		                break;
		            }
		        }
		        for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
		            log.debug("Releasing resources for vdu with id " + vdu.getId());
		            try {
		                resourceManagement.release(vnfcInstance, vimInstance);
		            } catch (VimException e) {
		                log.error(e.getMessage(), e);
		                throw new RuntimeException(e.getMessage(), e);
		            }
		            vnfciToRem.add(vnfcInstance);
		            log.debug("Released resources for vdu with id " + vdu.getId());
		        }
		        vdu.getVnfc_instance().removeAll(vnfciToRem);
		    }
		    log.info("Terminated vnfr with id " + virtualNetworkFunctionRecord.getId());
		    return virtualNetworkFunctionRecord;
	}

	@Override
	public void handleError(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
		// TODO Auto-generated method stub
		log.debug("================================= Invoked handleError method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		
	}

	@Override
	protected void checkEMS(String hostname) {
		// TODO Auto-generated method stub
		log.warn("EMS is not supported by this VNFM");
	}

	@Override
	protected void checkEmsStarted(String vduHostname) {
		// TODO Auto-generated method stub
		log.warn("EMS is not supported by this VNFM");
	}

	@Override
	public VirtualNetworkFunctionRecord start(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
			throws Exception {
		log.debug("================================= Invoked start method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		return virtualNetworkFunctionRecord;
	}

	@Override
	public VirtualNetworkFunctionRecord stop(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
			throws Exception {
		log.debug("================================= Invoked stop method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		return virtualNetworkFunctionRecord;
	}

	@Override
	public VirtualNetworkFunctionRecord startVNFCInstance(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
			VNFCInstance vnfcInstance) throws Exception {
		log.debug("================================= Invoked startVNFCInstance method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		return virtualNetworkFunctionRecord;
	}

	@Override
	public VirtualNetworkFunctionRecord stopVNFCInstance(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
			VNFCInstance vnfcInstance) throws Exception {
		log.debug("================================= Invoked stopVNFVInstance for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		return virtualNetworkFunctionRecord;
	}

	@Override
	public VirtualNetworkFunctionRecord configure(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
			throws Exception {
		log.debug("================================= Invoked configure method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		return virtualNetworkFunctionRecord;
	}

	@Override
	public VirtualNetworkFunctionRecord resume(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
			VNFCInstance vnfcInstance, VNFRecordDependency dependency) throws Exception {
		log.debug("================================= Invoked resume method for VNFR " + virtualNetworkFunctionRecord);
		log.debug("Method to be implemented.");
		// TODO Auto-generated method stub
		return virtualNetworkFunctionRecord;
	}
	
}
