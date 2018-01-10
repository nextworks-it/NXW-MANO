package eu.selfnet5g.onboarding.engine;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.io.ByteArrayInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.selfnet5g.onboarding.exception.EntityNotFoundException;
import eu.selfnet5g.onboarding.exception.InternalServerErrorException;
import eu.selfnet5g.onboarding.exception.MethodNotAllowedException;
import eu.selfnet5g.onboarding.interfaces.AppNotification;
import eu.selfnet5g.onboarding.interfaces.AppOnboarding;
import eu.selfnet5g.onboarding.interfaces.AppRegistration;
import eu.selfnet5g.onboarding.model.AppArchive;
import eu.selfnet5g.onboarding.model.AppClass;
import eu.selfnet5g.onboarding.model.AppConfiguration;
import eu.selfnet5g.onboarding.model.AppFamily;
import eu.selfnet5g.onboarding.model.AppMetadata;
import eu.selfnet5g.onboarding.model.AppMonitoring;
import eu.selfnet5g.onboarding.model.AppPackage;
import eu.selfnet5g.onboarding.model.AppPackageStatus;
import eu.selfnet5g.onboarding.model.ConfigurationCommunication;
import eu.selfnet5g.onboarding.model.ConfigurationEndpoint;
import eu.selfnet5g.onboarding.model.ConfigurationParameter;
import eu.selfnet5g.onboarding.model.ConfigurationValue;
import eu.selfnet5g.onboarding.model.LifecycleAction;
import eu.selfnet5g.onboarding.model.MetricValue;
import eu.selfnet5g.onboarding.model.MonitoringCommunication;
import eu.selfnet5g.onboarding.model.MonitoringEndpoint;
import eu.selfnet5g.onboarding.model.MonitoringMetric;
import eu.selfnet5g.onboarding.model.PNFAppDescriptor;
import eu.selfnet5g.onboarding.model.SDNAppDescriptor;
import eu.selfnet5g.onboarding.model.VMImage;
import eu.selfnet5g.onboarding.repo.AppArchiveRepository;
import eu.selfnet5g.onboarding.repo.AppConfigurationRepository;
import eu.selfnet5g.onboarding.repo.AppMetadataRepository;
import eu.selfnet5g.onboarding.repo.AppMonitoringRepository;
import eu.selfnet5g.onboarding.repo.AppPackageRepository;
import eu.selfnet5g.onboarding.repo.ConfigurationCommunicationRepository;
import eu.selfnet5g.onboarding.repo.ConfigurationEndpointRepository;
import eu.selfnet5g.onboarding.repo.ConfigurationParameterRepository;
import eu.selfnet5g.onboarding.repo.ConfigurationValueRepository;
import eu.selfnet5g.onboarding.repo.LifecycleActionRepository;
import eu.selfnet5g.onboarding.repo.MetricValueRepository;
import eu.selfnet5g.onboarding.repo.MonitoringCommunicationRepository;
import eu.selfnet5g.onboarding.repo.MonitoringEndpointRepository;
import eu.selfnet5g.onboarding.repo.MonitoringMetricRepository;
import eu.selfnet5g.onboarding.repo.PNFAppDescriptorRepository;
import eu.selfnet5g.onboarding.repo.SDNAppDescriptorRepository;
import eu.selfnet5g.onboarding.repo.VMImageRepository;

public class AppOnboardingManager implements AppOnboarding {

	private Logger log = LoggerFactory.getLogger(AppOnboardingManager.class);
	
	@Autowired
	private AppPackageRepository appPackageRepository;
	
	@Autowired
	private AppMetadataRepository appMetadataRepository;
	
	@Autowired
	private VMImageRepository vmImageRepository;
	
	@Autowired
	private AppArchiveRepository appArchiveRepository;
	
	@Autowired
	private AppMonitoringRepository appMonitoringRepository;

	@Autowired
	private MonitoringCommunicationRepository monitoringCommunicationRepository;
	
	@Autowired 
	private MonitoringEndpointRepository monitoringEndpointRepository;
	
	@Autowired
	private MonitoringMetricRepository monitoringMetricRepository;
	
	@Autowired
	private MetricValueRepository metricValueRepository;
	
	@Autowired
	private SDNAppDescriptorRepository sdnAppDescriptorRepository;
	
	@Autowired
	private PNFAppDescriptorRepository pnfAppDescriptorRepository;
	
	@Autowired
	private LifecycleActionRepository lifecycleActionRepository;
	
	@Autowired
	private AppConfigurationRepository appConfigurationRepository;
	
	@Autowired
	private ConfigurationCommunicationRepository configurationCommunicationRepository;
	
	@Autowired
	private ConfigurationEndpointRepository configurationEndpointRepository;
	
	@Autowired
	private ConfigurationParameterRepository configurationParameterRepository;
	
	@Autowired
	private ConfigurationValueRepository configurationValueRepository;
	
	@Autowired
	private AppNotification appNotificationManager;
	
	@Autowired
	@Qualifier("sdnoAppRegistration")
	private AppRegistration sdnoAppRegistration;
	
	@Autowired
	@Qualifier("openstackGlanceService")
	private AppRegistration openstackGlanceService;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	public AppOnboardingManager() {
		//do nothing
	}
	
	public String appOnboard(byte[] appPackage) throws Exception {
		
		log.debug("Going to process and validate the App Package");		
				
		//init archive contents
		AppMetadata metadata = null;
		SDNAppDescriptor sdnd = null;
		PNFAppDescriptor pnfd = null;
		VirtualNetworkFunctionDescriptor vnfd = null;
		String vnfdJson = null;
		AppMonitoring appMonitoring = null;
		AppConfiguration appConfiguration = null;
		
		InputStream tarStreamFirst;
		InputStream tarStreamSecond;
		ArchiveInputStream myTarFileFirst;
		ArchiveInputStream myTarFileSecond;
		
		try {
			tarStreamFirst = new ByteArrayInputStream(appPackage);
			tarStreamSecond = new ByteArrayInputStream(appPackage);
			myTarFileFirst = new ArchiveStreamFactory().createArchiveInputStream("tar", tarStreamFirst);
			myTarFileSecond = new ArchiveStreamFactory().createArchiveInputStream("tar", tarStreamSecond);
		} catch (ArchiveException e) {
			throw new Exception(e.getMessage());
		}

		TarArchiveEntry entry;
			
		while ((entry = (TarArchiveEntry) myTarFileFirst.getNextEntry()) != null) {
			// Scan all the tar archive entries
			if (entry.isFile() && entry.getName().equalsIgnoreCase("metadata.json")) {
				// it is the metadata.json - it contains generic info of the package
				log.debug("File inside tar: " + entry.getName());
				
				if (metadata != null) {
					String err = "Validation failed: multpile metadata.json in the App Package";
					log.error(err);
					throw new Exception(err);
				}
				
				byte[] content = new byte[(int) entry.getSize()];
				myTarFileFirst.read(content, 0, content.length);
				
				//convert metadata to json
				String json = new String(content);
				log.debug("Content of json is: " + json);
				try {
					metadata = mapper.readValue(json, AppMetadata.class);
				} catch (Exception e) {
					log.error("Failed mapping metadata.json into AppMetadata: " + e.getMessage());
					throw new Exception(e.getMessage());
				}
				log.debug("Created AppMetadata");
				break;
			}
		}
		//check consistency
		if (metadata == null) {
			String err = "Validation failed: missing metadata.json in the App Package";
			log.error(err);
			throw new Exception(err);
		}
		if ((metadata.getAppClass() == AppClass.VNF) &&
			(metadata.getVmImages().isEmpty() || metadata.getVims().isEmpty())) {
			String err = "Validation failed: missing VMs info into metadata.json within the VNF App Package";
			log.error(err);
			throw new Exception(err);
		}
		if (((metadata.getAppClass() == AppClass.SDN_APP) ||
			(metadata.getAppClass() == AppClass.SDN_CTRL_APP)) &&
			(metadata.getAppArchive() == null)) {
			String err = "Validation failed: missing app-archive info into metadata.json within the SDN App Package";
			log.error(err);
			throw new Exception(err);
		}
		
		//validation against already onboarded apps
		validateAppPackage(metadata); //throws Exception
		
		//second round of scan
		while ((entry = (TarArchiveEntry) myTarFileSecond.getNextEntry()) != null) {
			// Scan all the tar archive entries
			log.debug("File inside tar: " + entry.getName());
			
			byte[] content = new byte[(int) entry.getSize()];
			myTarFileSecond.read(content, 0, content.length);
			
			if (entry.getName().startsWith("app-descriptor/") && entry.getName().endsWith(".json")) {
		
				//check if it is SDN or VNF
				if (metadata.getAppClass() == AppClass.VNF) {
					//convert to vnfd
					vnfdJson = new String(content);
					log.debug("Content of json is: " + vnfdJson);
					try {			
						vnfd =	mapper.readValue(vnfdJson, VirtualNetworkFunctionDescriptor.class);
					} catch (Exception e) {
						log.error("Failed mapping app-descriptor into VNFD: " + e.getMessage());
						throw new Exception(e.getMessage());
					}
					log.debug("Created VNFD");
				} else if (metadata.getAppClass() == AppClass.PNF) {
					//convert to pnfd
					String pnfdJson = new String(content);
					log.debug("Content of json is: " + pnfdJson);
					try {			
						pnfd =	mapper.readValue(pnfdJson, PNFAppDescriptor.class);
					} catch (Exception e) {
						log.error("Failed mapping app-descriptor into PNFD: " + e.getMessage());
						throw new Exception(e.getMessage());
					}
					log.debug("Created PNF App Descriptor");
				} else {
					//convert to sdn-d
					String sdndJson = new String(content);
					log.debug("Content of json is: " + sdndJson);
					try {
						sdnd =	mapper.readValue(sdndJson, SDNAppDescriptor.class);
					} catch (Exception e) {
						log.error("Failed mapping app-descriptor into SDNAppDescriptor: " + e.getMessage());
						throw new Exception(e.getMessage());
					}
					log.debug("Created SDN App Descriptor");
				}
			} else if (entry.getName().startsWith("monitoring/") && entry.getName().endsWith(".json")) {
				
				//convert to monitoring
				String monitoringJson = new String(content);
				log.debug("Content of json is: " + monitoringJson);
				try {
					appMonitoring =	mapper.readValue(monitoringJson, AppMonitoring.class);
				} catch (Exception e) {
					log.error("Failed mapping monitoring info into AppMonitoring: " + e.getMessage());
					throw new Exception(e.getMessage());
				}
				log.debug("Created Monitoring info data");

			} else if (entry.getName().startsWith("configuration/") && entry.getName().endsWith(".json")) {
				
				//convert to monitoring
				String configJson = new String(content);
				log.trace("Content of json is: " + configJson);
				try {
					appConfiguration =	mapper.readValue(configJson, AppConfiguration.class);
				} catch (Exception e) {
					log.error("Failed mapping configuration info into AppConfiguration: " + e.getMessage());
					throw new Exception(e.getMessage());
				}
				log.debug("Created Configuration info data");
				
			} else if (entry.isDirectory() || (entry.isFile() && entry.getName().equalsIgnoreCase("metadata.json"))) {
				//Skip
			} else {
				log.error("Validation failed: unrecognized App Package content (" + entry.getName() + ")");
				throw new Exception("Unrecognized App Package content (" + entry.getName() + ")");
			}
		}//end while
		
		//check consistency
		if ((metadata.getAppFamily() == AppFamily.SENSOR) &&
		    (appMonitoring == null)) {
			log.error("Validation failed: missing monitoring information");
			throw new Exception("Missing monitoring information");
		}
		if ((metadata.getAppFamily() == AppFamily.ACTUATOR) &&
			(appConfiguration == null)) {
			log.error("Validation failed: missing configuration information");
			throw new Exception("Missing configuration information");
		}
		if (metadata.getAppClass() == AppClass.VNF) {
			if (vnfd == null) {
				log.error("Validation failed: missing VNFD");
				throw new Exception("Missing VNFD");
			}
			if (metadata.getVims().isEmpty()) {
				log.error("Validation failed: missing VIM info");
				throw new Exception("Missing VIM info");
			}
		}
		if ((metadata.getAppClass() == AppClass.SDN_APP ||
			metadata.getAppClass() == AppClass.SDN_CTRL_APP) &&
			(sdnd == null)) {
			log.error("Validation failed: missing SDN App Descriptor");
			throw new Exception("Missing SDN App Descriptor");
		}
		if (metadata.getAppClass() == AppClass.PNF  &&
			pnfd == null) {
				log.error("Validation failed: missing PNF App Descriptor");
				throw new Exception("Missing PNF App Descriptor");
			}
		
		try {
			//register the app (VIM vs. SDNO depending on appClass)
			if (metadata.getAppClass() == AppClass.VNF) {
				Map<String,String> res = openstackGlanceService.registerNewApp(metadata);
				
				for (Map.Entry<String,String> id : res.entrySet()) {
					for (VMImage vmI : metadata.getVmImages()) {
						if (vmI.getName().equalsIgnoreCase(id.getKey())) {
							vmI.setVimId(id.getValue());
						}
					}
				}
				
			} else if (metadata.getAppClass() == AppClass.SDN_APP ||
					   metadata.getAppClass() == AppClass.SDN_CTRL_APP) {
				Map<String,String> res = sdnoAppRegistration.registerNewApp(metadata);
				//set orderId to metadata to be properly persisted below
				metadata.setAppTypeOrderId(res.get("orderId"));
			}
			
			//persist package
			AppPackage pckg = persistAppPackage(metadata, appMonitoring, appConfiguration, vnfdJson, sdnd, pnfd);
		
			//going to send notification over the message bus
			//need to explicitely set package content as they are not included in db entity.
			pckg.setMetadata(metadata);
			pckg.setMonitoring(appMonitoring);
			pckg.setConfiguration(appConfiguration);
			pckg.setVnfDescriptor(vnfdJson);
			pckg.setSdnAppDescriptor(sdnd);
			pckg.setPnfAppDescriptor(pnfd);
			
			appNotificationManager.appOnboardNotification(pckg);
			
			log.info("App Package " + pckg.getId() + " onboarded");
			
			return pckg.getId();
			
		} catch (Exception e) {
			log.error("Error while managing App Package onboard");
			throw new Exception(e.getMessage());
		}
		
	}
	
	private void validateAppPackage(AppMetadata metadata) throws Exception {
		Collection<AppMetadata> datas = appMetadataRepository.findByAppClassAndAppType(metadata.getAppClass(), metadata.getAppType());
		
		for (AppMetadata data : datas) {
			if (data.getAppName().equalsIgnoreCase(metadata.getAppName()) &&
				data.getAppVersion().equalsIgnoreCase(metadata.getAppVersion())) {
				throw new Exception("Failed valiation - Same implementation of the App (" + 
							        ":" + metadata.getAppClass().toString() + ":" + 
							        ":" + metadata.getAppType().toString() + ") is already onboarded (app-id:" 
							        + data.getAppPackage().getId() + ")" );
			}
		}
	}
	
	private AppPackage persistAppPackage(AppMetadata metadata, AppMonitoring appMonitoring,
							       		 AppConfiguration appConfiguration, String vnfdJson,
							       		 SDNAppDescriptor sdnd, PNFAppDescriptor pnfd) throws Exception {
		
		try {
			//going to save into db
			AppPackage appPckg = new AppPackage(vnfdJson);
			
			appPackageRepository.save(appPckg);
			
			//metadata
			persistAppMetadata(metadata, appPckg);
			
			//monitoring info
			if (metadata.getAppFamily() == AppFamily.SENSOR) {
				persistAppMonitoring(appMonitoring, appPckg);
			}
			
			//configuration info
			persistAppConfiguration(appConfiguration, appPckg);
			
			//sdn-descriptor
			if (metadata.getAppClass() == AppClass.SDN_APP ||
				metadata.getAppClass() == AppClass.SDN_CTRL_APP) {
				persistSdnAppDescriptor(sdnd, appPckg);
			}
			
			//pnf-descriptor
			if (metadata.getAppClass() == AppClass.PNF) {
				persistPnfAppDescriptor(pnfd, appPckg);
			}
						
			log.info("App Package " + appPckg.getId() + " stored into DB");

			return appPckg;
			
		} catch (Exception e) {
			log.error("Error while persisting new App Package");
			throw new Exception(e.getMessage());
		}
	}
		
	private void persistSdnAppDescriptor(SDNAppDescriptor sdnd, AppPackage appPckg) throws Exception {
		SDNAppDescriptor sdndDb = new SDNAppDescriptor(appPckg,
													   sdnd.getVendor(),
													   sdnd.getName(),
													   sdnd.getType(),
													   sdnd.getVersion(),
													   sdnd.getController());
		for (Map.Entry<String,String> e : sdnd.getMetadata().entrySet()) {
			sdndDb.addMetadataKeyValue(e.getKey(), e.getValue());
		}
		sdndDb = sdnAppDescriptorRepository.save(sdndDb);
	}
	
	private void persistAppConfiguration(AppConfiguration configuration, AppPackage appPckg) throws Exception {
		AppConfiguration configurationDb = new AppConfiguration(appPckg);
		
		configurationDb = appConfigurationRepository.save(configurationDb);
		
		ConfigurationCommunication commDb = new ConfigurationCommunication(configurationDb,
														                   configuration.getCommunication().getProtocol());

		for (Map.Entry<String,String> e : configuration.getCommunication().getMetadata().entrySet()) {
			commDb.addMetadataKeyValue(e.getKey(), e.getValue());
		}
		
		commDb = configurationCommunicationRepository.save(commDb);
		
		for (ConfigurationEndpoint endpoint : configuration.getCommunication().getEndpoints()) {
			ConfigurationEndpoint endpointDb = new ConfigurationEndpoint(commDb,
																   		 endpoint.getConfigAction(),
																   		 endpoint.getValue());
			configurationEndpointRepository.save(endpointDb);
		}
		
		for (ConfigurationParameter param: configuration.getParameters()) {
			ConfigurationParameter paramDb = new ConfigurationParameter(configurationDb,
						                                     		    param.getConfigAction());
			paramDb = configurationParameterRepository.save(paramDb);
			
			for (ConfigurationValue value : param.getValues()) {
				ConfigurationValue valueDb = new ConfigurationValue(paramDb,
													  			   value.getParameter(),
							                                       value.getName());
				configurationValueRepository.save(valueDb);
			}
		}
	}
	
	private void persistAppMonitoring(AppMonitoring monitoring, AppPackage appPckg) throws Exception {
		AppMonitoring monitoringDb = new AppMonitoring(appPckg);
		
		monitoringDb = appMonitoringRepository.save(monitoringDb);
		
		MonitoringCommunication commDb = new MonitoringCommunication(monitoringDb,
																	 monitoring.getCommunication().getProtocol(),
																	 monitoring.getCommunication().getMethod());
		for (Map.Entry<String,String> e : monitoring.getCommunication().getMetadata().entrySet()) {
			commDb.addMetadataKeyValue(e.getKey(), e.getValue());
		}
		commDb = monitoringCommunicationRepository.save(commDb);
		
		for (MonitoringEndpoint endpoint : monitoring.getCommunication().getEndpoints()) {
			MonitoringEndpoint endpointDb = new MonitoringEndpoint(commDb,
																   endpoint.getMetric(),
																   endpoint.getValue());
			monitoringEndpointRepository.save(endpointDb);
		}
		
		for (MonitoringMetric metric: monitoring.getMetrics()) {
			MonitoringMetric metricDb = new MonitoringMetric(monitoringDb,
						                                     metric.getMetricName());
			metricDb = monitoringMetricRepository.save(metricDb);
			
			for (MetricValue value : metric.getValues()) {
				MetricValue valueDb = new MetricValue(metricDb,
							                          value.getName(),
							                          value.getResource(),
							                          value.getUnit(),
							                          value.getType());
				metricValueRepository.save(valueDb);
			}
		}
		
	}
	
	private void persistPnfAppDescriptor(PNFAppDescriptor pnfd, AppPackage appPckg) throws Exception {
		
		PNFAppDescriptor pnfdDb = new PNFAppDescriptor(appPckg, pnfd.getVendor(), pnfd.getName(), pnfd.getVersion());
		
		for (Map.Entry<String,String> e : pnfd.getMetadata().entrySet()) {
			pnfdDb.addMetadataKeyValue(e.getKey(), e.getValue());
		}
		
		pnfdDb = pnfAppDescriptorRepository.save(pnfdDb);
		
		for (LifecycleAction action : pnfd.getLifecycleActions()) {
			
			LifecycleAction actionDb = new LifecycleAction(pnfdDb,
					                                       action.getEvent(),
														   action.getAction());
			lifecycleActionRepository.save(actionDb);

		}		
	}
	
	private void persistAppMetadata(AppMetadata metadata, AppPackage appPckg) throws Exception {
		
		AppMetadata metadataDb = new AppMetadata(appPckg,
												 metadata.getAppFamily(),
												 metadata.getAppClass(),
												 metadata.getAppType(),
												 metadata.getAppTypeOrderId(),
												 metadata.getAppName(),
												 metadata.getAppVersion(),
												 metadata.getIsPublic(),
												 metadata.getUpload(),
												 metadata.getScriptsLink(),
												 metadata.getVims());
		
		metadataDb = appMetadataRepository.save(metadataDb);
		
		if (metadata.getAppClass() == AppClass.VNF) {
			for (VMImage vm : metadata.getVmImages()) {				
				VMImage vmDb = new VMImage(metadataDb,
										   vm.getName(),
										   vm.getVimId(),
										   vm.getLink(),
										   vm.getDiskFormat(),
										   vm.getMinDisk(),
										   vm.getMinCpu(),
										   vm.getMinRam());				
				vmDb = vmImageRepository.save(vmDb);				
			}
		} else if (metadata.getAppArchive() != null) {
			AppArchive archiveDb = new AppArchive(metadataDb,
												  metadata.getAppArchive().getName(),
												  metadata.getAppArchive().getType(),
												  metadata.getAppArchive().getLink());
			archiveDb = appArchiveRepository.save(archiveDb);
		}
		
	}
	
	public void appUpdate(String packageId, AppPackage newPckg) throws EntityNotFoundException, InternalServerErrorException {
		log.info("Updating App Package Id: " + packageId);
		
		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
		
		//metadata is mandatory
		AppMetadata metadata = newPckg.getMetadata();
		if (metadata == null) {
			log.error("New App Package metadata is missing.");
			throw new InternalServerErrorException("New App Package metadata is missing.");
		}
		
		//validation
		if (metadata.getAppVersion().equalsIgnoreCase(pckg.get().getMetadata().getAppVersion())) {
			log.error("New App Package must be different version of the old one.");
			throw new InternalServerErrorException("New App Package must be different version of the old one.");
		}
		if (metadata.getAppFamily() != pckg.get().getMetadata().getAppFamily()) {
			log.error("New App Family must be the same of the old one.");
			throw new InternalServerErrorException("New App Family must be the same of the old one.");
		}
		if (metadata.getAppClass() != pckg.get().getMetadata().getAppClass()) {
			log.error("New App Class must be the same of the old one.");
			throw new InternalServerErrorException("New App Family must be the same of the old one.");
		}
		if (!metadata.getAppType().equalsIgnoreCase(pckg.get().getMetadata().getAppType())) {
			log.error("New App Type must be the same of the old one.");
			throw new InternalServerErrorException("New App Type must be the same of the old one.");
		}
		if (newPckg.getVnfDescriptor() != null &&
			pckg.get().getMetadata().getAppClass() != AppClass.VNF) {
			log.error("New App Package contains VNFD but odl package is not VNF.");
			throw new InternalServerErrorException("New App Package contains VNFD but odl package is not VNF.");
		}
		if (newPckg.getSdnAppDescriptor() != null &&
			pckg.get().getMetadata().getAppClass() != AppClass.SDN_APP &&
			pckg.get().getMetadata().getAppClass() != AppClass.SDN_CTRL_APP) {
			log.error("New App Package contains SDND but odl package is  not SDN App or SDN Ctrl App.");
			throw new InternalServerErrorException("New App Package contains SDND but odl package is not SDN App or SDN Ctrl App.");
		}
		if (newPckg.getPnfAppDescriptor() != null &&
			pckg.get().getMetadata().getAppClass() != AppClass.PNF) {
			log.error("New App Package contains VNFD but odl package is not VNF.");
			throw new InternalServerErrorException("New App Package contains PNFD but odl package is not PNF.");
		}
		//
		
		
		try {
			// first, check if new software has to be updated
			if (metadata.getAppArchive() != null ||
			    !metadata.getVmImages().isEmpty()) {
				//register the new app (VIM vs. SDNO depending on appClass)
				if (metadata.getAppClass() == AppClass.VNF) {
					Map<String,String> res = openstackGlanceService.registerNewApp(metadata);
					
					for (Map.Entry<String,String> id : res.entrySet()) {
						for (VMImage vmI : metadata.getVmImages()) {
							if (vmI.getName().equalsIgnoreCase(id.getKey())) {
								vmI.setVimId(id.getValue());
							}
						}
					}
					
				} else if (metadata.getAppClass() == AppClass.SDN_APP ||
						   metadata.getAppClass() == AppClass.SDN_CTRL_APP) {
					Map<String,String> res = sdnoAppRegistration.registerNewApp(metadata);
					//set orderId to metadata to be properly persisted below
					metadata.setAppTypeOrderId(res.get("orderId"));
				}
			}
			
			//update metadata into DB
			updateAppMetadata(pckg.get().getMetadata().getId(), metadata, packageId);
			
			//second, check if monitoring is updated
			if (newPckg.getMonitoring() != null) {				
				updateAppMonitoring(pckg.get().getMonitoring().getId(), newPckg.getMonitoring(), packageId);
			}
			
			//third, check if configuration is updated
			if (newPckg.getConfiguration() != null) {
				updateAppConfiguration(pckg.get().getConfiguration().getId(), newPckg.getConfiguration(), packageId);
			}
			
			//fourth, check if sdnd is updated
			if (newPckg.getSdnAppDescriptor() != null) {
				updateSDNAppDescriptor(pckg.get().getSdnAppDescriptor().getId(), newPckg.getSdnAppDescriptor(), packageId);
			}
			
			//fifth, check if vnfd is updated
			if (newPckg.getVnfDescriptor() != null) {
				updateVnfDescriptor(newPckg.getVnfDescriptor(), packageId);
			}
			
			//sixth, check if vnfd is updated
			if (newPckg.getPnfAppDescriptor() != null) {
				updatePNFAppDescriptor(newPckg.getPnfAppDescriptor().getId(), newPckg.getPnfAppDescriptor(), packageId);
			}
						
			//send notification over message bus
			appNotificationManager.appUpdateNotification(packageId, newPckg);
			
			log.info("App Package: " + packageId + " updated");
			
		} catch (Exception e) {
			log.error("Error while managing App Package update");
			throw new InternalServerErrorException(e.getMessage());
		}
	}
	
	private void updateVnfDescriptor(String newVnfd, String packageId) throws Exception {
		
		log.info("Updating App Package VNFD");
		
		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
		
		pckg.get().setVnfDescriptor(newVnfd);
		appPackageRepository.save(pckg.get());
	}
	
	private void updateAppMetadata(String oldMetadataId, AppMetadata newMetadata, String packageId) throws Exception {
		
		log.info("Updating App Package metadata");
		
		Optional<AppMetadata> metadata = appMetadataRepository.findById(oldMetadataId);
		if (!metadata.isPresent()) {
			throw new Exception("Cannot delete metadata info. Id not found");
		}
		
		metadata.get().getAppPackage().setMetadata(null);
		metadata.get().setAppPackage(null);
		appMetadataRepository.save(metadata.get());
		appMetadataRepository.delete(metadata.get());	
		
		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
		
		AppMetadata metadataDb = new AppMetadata(pckg.get(),
												newMetadata.getAppFamily(),
												newMetadata.getAppClass(),
												newMetadata.getAppType(),
												newMetadata.getAppTypeOrderId(),
												newMetadata.getAppName(),
												newMetadata.getAppVersion(),
												newMetadata.getIsPublic(),
												newMetadata.getUpload(),
												newMetadata.getScriptsLink(),
												newMetadata.getVims());
		
		metadataDb = appMetadataRepository.save(metadataDb);

		if (newMetadata.getAppClass() == AppClass.VNF) {
			for (VMImage vm : newMetadata.getVmImages()) {				
				VMImage vmDb = new VMImage(metadataDb,
						vm.getName(),
						vm.getVimId(),
						vm.getLink(),
						vm.getDiskFormat(),
						vm.getMinDisk(),
						vm.getMinCpu(),
						vm.getMinRam());				
				vmDb = vmImageRepository.save(vmDb);				
			}
		} else if (newMetadata.getAppArchive() != null) {
			AppArchive archiveDb = new AppArchive(metadataDb,
												newMetadata.getAppArchive().getName(),
												newMetadata.getAppArchive().getType(),
												newMetadata.getAppArchive().getLink());
			archiveDb = appArchiveRepository.save(archiveDb);
		}
	}
	
	private void updateAppConfiguration(String oldConfigurationId, AppConfiguration newConfiguration, String packageId) throws Exception {
		
		log.info("Updating App Package configuration");
		
		Optional<AppConfiguration> configuration = appConfigurationRepository.findById(oldConfigurationId);
		if (!configuration.isPresent()) {
			throw new Exception("Cannot delete configuration info. Id not found");
		}
		
		configuration.get().getAppPackage().setConfiguration(null);
		configuration.get().setAppPackage(null);
		appConfigurationRepository.save(configuration.get());
		appConfigurationRepository.delete(configuration.get());

		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
		
		AppConfiguration configurationDb = new AppConfiguration(pckg.get());
		
		configurationDb = appConfigurationRepository.save(configurationDb);
		
		ConfigurationCommunication commDb = new ConfigurationCommunication(configurationDb,
														                   newConfiguration.getCommunication().getProtocol());
		
		commDb = configurationCommunicationRepository.save(commDb);
		
		for (ConfigurationEndpoint endpoint : newConfiguration.getCommunication().getEndpoints()) {
			ConfigurationEndpoint endpointDb = new ConfigurationEndpoint(commDb,
																   		 endpoint.getConfigAction(),
																   		 endpoint.getValue());
			configurationEndpointRepository.save(endpointDb);
		}
		
		for (ConfigurationParameter param: newConfiguration.getParameters()) {
			ConfigurationParameter paramDb = new ConfigurationParameter(configurationDb,
						                                     		    param.getConfigAction());
			paramDb = configurationParameterRepository.save(paramDb);
			
			for (ConfigurationValue value : param.getValues()) {
				ConfigurationValue valueDb = new ConfigurationValue(paramDb,
													  			   value.getParameter(),
							                                       value.getName());
				configurationValueRepository.save(valueDb);
			}
		}
	}
	
	private void updateAppMonitoring(String oldMonitoringId, AppMonitoring newMonitoring, String packageId) throws Exception {
		
		log.info("Updating App Package monitoring");
		
		Optional<AppMonitoring> monitoring = appMonitoringRepository.findById(oldMonitoringId);
		if (!monitoring.isPresent()) {
			throw new Exception("Cannot delete configuration info. Id not found");
		}

		monitoring.get().getAppPackage().setMonitoring(null);
		monitoring.get().setAppPackage(null);
		appMonitoringRepository.save(monitoring.get());
		appMonitoringRepository.delete(monitoring.get());

		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
		
		AppMonitoring monitoringDb = new AppMonitoring(pckg.get());
		
		monitoringDb = appMonitoringRepository.save(monitoringDb);
		
		MonitoringCommunication commDb = new MonitoringCommunication(monitoringDb,
																	 newMonitoring.getCommunication().getProtocol(),
																	 newMonitoring.getCommunication().getMethod());
		commDb = monitoringCommunicationRepository.save(commDb);
		
		for (MonitoringEndpoint endpoint : newMonitoring.getCommunication().getEndpoints()) {
			MonitoringEndpoint endpointDb = new MonitoringEndpoint(commDb,
																   endpoint.getMetric(),
																   endpoint.getValue());
			monitoringEndpointRepository.save(endpointDb);
		}
		
		for (MonitoringMetric metric: newMonitoring.getMetrics()) {
			MonitoringMetric metricDb = new MonitoringMetric(monitoringDb,
						                                     metric.getMetricName());
			metricDb = monitoringMetricRepository.save(metricDb);
			
			for (MetricValue value : metric.getValues()) {
				MetricValue valueDb = new MetricValue(metricDb,
							                          value.getName(),
							                          value.getResource(),
							                          value.getUnit(),
							                          value.getType());
				metricValueRepository.save(valueDb);
			}
		}
		
	}
	
	private void updateSDNAppDescriptor(String oldDescriptorId, SDNAppDescriptor newDescriptor, String packageId) throws Exception {
		
		log.info("Updating App Package SDND");
		
		Optional<SDNAppDescriptor> descriptor = sdnAppDescriptorRepository.findById(oldDescriptorId);
		if (!descriptor.isPresent()) {
			throw new Exception("Cannot delete descriptor info. Id not found");
		}
		
		descriptor.get().getAppPackage().setSdnAppDescriptor(null);
		descriptor.get().setAppPackage(null);
		sdnAppDescriptorRepository.save(descriptor.get());
		sdnAppDescriptorRepository.delete(descriptor.get());

		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
		
		SDNAppDescriptor sdndDb = new SDNAppDescriptor(pckg.get(),
														newDescriptor.getVendor(),
														newDescriptor.getName(),
														newDescriptor.getType(),
														newDescriptor.getVersion(),
														newDescriptor.getController());
		sdndDb = sdnAppDescriptorRepository.save(sdndDb);
	}
	
	private void updatePNFAppDescriptor(String oldDescriptorId, PNFAppDescriptor newDescriptor, String packageId) throws Exception {
		
		log.info("Updating App Package PNFD");
		
		Optional<PNFAppDescriptor> descriptor = pnfAppDescriptorRepository.findById(oldDescriptorId);
		if (!descriptor.isPresent()) {
			throw new Exception("Cannot delete PNF descriptor info. Id not found");
		}
		
		descriptor.get().getAppPackage().setPnfAppDescriptor(null);
		descriptor.get().setAppPackage(null);
		pnfAppDescriptorRepository.save(descriptor.get());
		pnfAppDescriptorRepository.delete(descriptor.get());

		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
		
		PNFAppDescriptor pnfdDb = new PNFAppDescriptor(pckg.get(),
													   newDescriptor.getVendor(),
													   newDescriptor.getName(),
													   newDescriptor.getVersion());
		pnfdDb = pnfAppDescriptorRepository.save(pnfdDb);
		
		
		for (LifecycleAction action : newDescriptor.getLifecycleActions()) {
			
			LifecycleAction actionDb = new LifecycleAction(pnfdDb,
					                                       action.getEvent(),
														   action.getAction());
			lifecycleActionRepository.save(actionDb);

		}		
	} 
	
	public void appOffboard(String packageId) throws EntityNotFoundException, MethodNotAllowedException, InternalServerErrorException {
		log.info("Off-boarding App Package Id: " + packageId);
		
		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
		
		if (pckg.get().getStatus() == AppPackageStatus.ENABLED) {
			log.error("App Package status in ENABLED");
			throw new MethodNotAllowedException("AppPackage status is ENABLED");
		}
		
		try {
			//uregister the app (VIM vs. SDNO depending on appClass)
			if (pckg.get().getMetadata().getAppClass() == AppClass.VNF) {
				openstackGlanceService.unregisterApp(pckg.get().getMetadata());
			} else if (pckg.get().getMetadata().getAppClass() == AppClass.SDN_APP ||
					pckg.get().getMetadata().getAppClass() == AppClass.SDN_CTRL_APP) {
				sdnoAppRegistration.unregisterApp(pckg.get().getMetadata());
			} else {
				//PNF case
				//do nothing
			}
				
			//send notification over message bus
			appNotificationManager.appOffboardNotification(packageId,
														   pckg.get().getMetadata().getAppFamily(),
														   pckg.get().getMetadata().getAppClass());					
		} catch (Exception e) {
			log.error("Error while managing App Package offboard");
			throw new InternalServerErrorException(e.getMessage());
		}
		
		appPackageRepository.delete(pckg.get());
		
		log.info("App Package offboarded");
		
	}
	
	public AppPackage appGet(String packageId) throws EntityNotFoundException {
		log.info("Retrieving App Package Id: " + packageId);
		
		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
		
		log.info("App Package found in DB");
		return pckg.get();
	}
	
	public Collection<AppPackage> appsGet() throws Exception {
		log.info("Retrieving all App Packages");
		
		return appPackageRepository.findAll();
	}
	
	public Collection<AppPackage> appsGetByClass(AppClass appClass) throws Exception {
		log.info("Retrieving App Packages by Class");
		
		return appPackageRepository.findByMetadataAppClass(appClass);
	}
	
	public Collection<AppPackage> appsGetByType(String appType) throws Exception {
		log.info("Retrieving App Packages by Type");
		
		return appPackageRepository.findByMetadataAppType(appType);
	}
	
	public void appEnable(String packageId) throws EntityNotFoundException, InternalServerErrorException {
		log.info("Enabling App Package Id: " + packageId);
		
		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
				
		pckg.get().setStatus(AppPackageStatus.ENABLED);
		
		appPackageRepository.save(pckg.get());

		try {
			appNotificationManager.appEnableNotification(packageId,
					   									 pckg.get().getMetadata().getAppFamily(),
					   									 pckg.get().getMetadata().getAppClass());					
		} catch (Exception e) {
			log.error("Error while managing App Package enable");
			throw new InternalServerErrorException(e.getMessage());
		}
		
		log.info("App Package enabled");
		
	}
	
	public void appDisable(String packageId) throws EntityNotFoundException, InternalServerErrorException {
		log.info("Disabling App Package Id: " + packageId);
		
		Optional<AppPackage> pckg = appPackageRepository.findById(packageId);
		if (!pckg.isPresent()) {
			log.error("App Package not found in DB");
			throw new EntityNotFoundException(packageId);
		}
		
		pckg.get().setStatus(AppPackageStatus.DISABLED);
		
		appPackageRepository.save(pckg.get());

		try {
			appNotificationManager.appDisableNotification(packageId,
														  pckg.get().getMetadata().getAppFamily(),
					   									  pckg.get().getMetadata().getAppClass());					
		} catch (Exception e) {
			log.error("Error while managing App Package disable");
			throw new InternalServerErrorException(e.getMessage());
		}
		
		log.info("App Package disabled");
	}
	
}
