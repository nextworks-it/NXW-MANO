package eu.selfnet5g.onboarding.api;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import eu.selfnet5g.onboarding.interfaces.AppOnboarding;
import eu.selfnet5g.onboarding.model.AppClass;
import eu.selfnet5g.onboarding.model.AppConfiguration;
import eu.selfnet5g.onboarding.model.AppMetadata;
import eu.selfnet5g.onboarding.model.AppMonitoring;
import eu.selfnet5g.onboarding.model.AppPackage;
import eu.selfnet5g.onboarding.model.AppPackageStatus;
import eu.selfnet5g.onboarding.model.SDNAppDescriptor;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping(value="/app-catalogue/app-packages")
public class AppPackageRestController {

	private Logger log = LoggerFactory.getLogger(AppPackageRestController.class);

	@Autowired
	private AppOnboarding appOnboardingManager;
	
	@RequestMapping(method=RequestMethod.POST)
	@ApiOperation(value = "APP Package Onboard")
		@ApiResponses(value = { 
	        @ApiResponse(code = 201, message = "Created", response = String.class),
	        @ApiResponse(code = 400, message = "Bad Request")}) 
	public ResponseEntity<String> onboardAppPackage(@RequestParam("file") MultipartFile file) throws Exception {

		log.debug("Onboarding a new App Package");
		if (file.isEmpty()) {
			return new ResponseEntity<String>("Error message: File is empty!", HttpStatus.BAD_REQUEST);
		}

		byte[] appPackage = file.getBytes();

		String packageId = "NULL";

		try {
			packageId = appOnboardingManager.appOnboard(appPackage);
		} catch (Exception e) {
			log.error("Cannot onboard App Package: " + e.getMessage());
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<String>(packageId, HttpStatus.CREATED);
	}


	@RequestMapping(value="/{packageId}", method=RequestMethod.DELETE)
	@ApiOperation(value = "APP Package Offboard")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 405, message = "Method Not Allowed"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public ResponseEntity<String> offBoardAppPackage(@PathVariable String packageId) throws Exception {
		
		try {
			appOnboardingManager.appOffboard(packageId);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<String>(HttpStatus.OK);

	}
	
	@RequestMapping(value="/{packageId}", method=RequestMethod.PUT)
	@ApiOperation(value = "APP Package Update")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public ResponseEntity<String> updateAppPackage(@PathVariable String packageId,
											  @RequestBody AppPackage pckg) throws Exception {
		
		appOnboardingManager.appUpdate(packageId, pckg);
		return new ResponseEntity<String>(HttpStatus.OK);
	}
	
	@RequestMapping(value="/{packageId}/action", method=RequestMethod.PUT)
	@ApiOperation(value = "APP Package Status Change")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Entity Not Found"),
        @ApiResponse(code = 405, message = "Method Not Allowed"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public ResponseEntity<String> enableDisableAppPackage(@PathVariable String packageId,
													 @RequestParam("status") String action) throws Exception {
		
		if (action.equalsIgnoreCase("enable")) {
			appOnboardingManager.appEnable(packageId);
		} else if (action.equalsIgnoreCase("disable")) {
			appOnboardingManager.appDisable(packageId);
		} else {
			return new ResponseEntity<String>("Wrong action (" + action + "): use ENABLE or DISABLE",
										      HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<String>(HttpStatus.OK);

	}

	@RequestMapping(value="/{packageId}", method=RequestMethod.GET)
	@ApiOperation(value = "APP Package Query")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success",  response = AppPackage.class),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public AppPackage getAppPackage(@PathVariable String packageId) throws Exception {		
		return appOnboardingManager.appGet(packageId);	
	}
	
	@RequestMapping(value="/ids", method=RequestMethod.GET)
	@ApiOperation(value = "APP Packages Query (app short info list)")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public ArrayList<AppInfo> getAppPackagesIds() throws Exception {		
		Collection<AppPackage> pckgs = appOnboardingManager.appsGet();
		
		ArrayList<AppInfo> infos = new ArrayList<>();		
		for (AppPackage pckg : pckgs) {
			AppInfo info = new AppInfo(pckg.getId(), pckg.getMetadata().getAppName(),
					   				   pckg.getMetadata().getAppType(), pckg.getMetadata().getAppClass(),
					   				   pckg.getStatus());
			infos.add(info);
		}
		
		return infos;
	}
	
	@RequestMapping(value="/ids", method=RequestMethod.GET, params={"app-type"})
	@ApiOperation(value = "APP Packages Query - per app-type (app short-info list)")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public ArrayList<AppInfo> getAppPackagesIdsFilteredByType(@RequestParam("app-type") String appType) throws Exception {		
		Collection<AppPackage> pckgs = appOnboardingManager.appsGetByType(appType);
		
		ArrayList<AppInfo> infos = new ArrayList<>();		
		for (AppPackage pckg : pckgs) {	
			AppInfo info = new AppInfo(pckg.getId(), pckg.getMetadata().getAppName(),
	   				   pckg.getMetadata().getAppType(), pckg.getMetadata().getAppClass(),
	   				   pckg.getStatus());
			infos.add(info);
		}
		
		return infos;
	}
	
	@RequestMapping(value="/ids", method=RequestMethod.GET, params={"app-class"})
	@ApiOperation(value = "APP Packages Query - per app-class (app short-info list)")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public ArrayList<AppInfo> getAppPackagesIdsFilteredByClass(@RequestParam("app-class") AppClass appClass) throws Exception {		
		Collection<AppPackage> pckgs = appOnboardingManager.appsGetByClass(appClass);
		
		ArrayList<AppInfo> infos = new ArrayList<>();		
		for (AppPackage pckg : pckgs) {
			AppInfo info = new AppInfo(pckg.getId(), pckg.getMetadata().getAppName(),
	   				   pckg.getMetadata().getAppType(), pckg.getMetadata().getAppClass(),
	   				   pckg.getStatus());
			infos.add(info);
		}
		
		return infos;
	}
	
	@RequestMapping(method=RequestMethod.GET)
	@ApiOperation(value = "APP Packages Query")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public Collection<AppPackage> getAppPackages() throws Exception {		
		return appOnboardingManager.appsGet();	
	}
	
	@RequestMapping(value="/{packageId}/app-info", method=RequestMethod.GET)
	@ApiOperation(value = "APP Package Metadata Info Query")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = AppMetadata.class),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public AppMetadata getAppMetadata(@PathVariable String packageId) throws Exception {		
		return appOnboardingManager.appGet(packageId).getMetadata();	
	}
	
	@RequestMapping(value="/{packageId}/app-status", method=RequestMethod.GET)
	@ApiOperation(value = "APP Package Status Info Query")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = AppPackageStatus.class),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public AppPackageStatus getAppStatus(@PathVariable String packageId) throws Exception {		
		return appOnboardingManager.appGet(packageId).getStatus();	
	}
	
	@RequestMapping(value="/{packageId}/app-monitoring", method=RequestMethod.GET)
	@ApiOperation(value = "APP Package Monitoring Info Query")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = AppMonitoring.class),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public AppMonitoring getAppMonitoring(@PathVariable String packageId) throws Exception {		
		return appOnboardingManager.appGet(packageId).getMonitoring();	
	}
	
	@RequestMapping(value="/{packageId}/app-configuration", method=RequestMethod.GET)
	@ApiOperation(value = "APP Package Configuration Info Query")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = AppMonitoring.class),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public AppConfiguration getAppConfiguration(@PathVariable String packageId) throws Exception {		
		return appOnboardingManager.appGet(packageId).getConfiguration();	
	}
	
	@RequestMapping(value="/{packageId}/app-descriptor", method=RequestMethod.GET)
	@ApiOperation(value = "APP Package Descriptor Info Query")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Entity Not Found"),
		@ApiResponse(code = 500, message = "Internal Server Error")}) 
	public ResponseEntity<?> getAppDescriptor(@PathVariable String packageId) throws Exception {		
		AppPackage pckg = appOnboardingManager.appGet(packageId);
		if (pckg.getMetadata().getAppClass() == AppClass.VNF) {
			return new ResponseEntity<String>(pckg.getVnfDescriptor(), HttpStatus.OK);
		} else {
			return new ResponseEntity<SDNAppDescriptor>(pckg.getSdnAppDescriptor(), HttpStatus.OK);
		}
	}
}