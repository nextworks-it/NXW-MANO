package eu.selfnet5g.onboarding.interfaces;

import eu.selfnet5g.onboarding.model.AppClass;
import eu.selfnet5g.onboarding.model.AppFamily;
import eu.selfnet5g.onboarding.model.AppPackage;

public interface AppNotification {

	public void appOnboardNotification(AppPackage appPackage) throws Exception;
	
	public void appOffboardNotification(String packageId, AppFamily appFamily, AppClass appClass) throws Exception;
	
	public void appDisableNotification(String packageId, AppFamily appFamily, AppClass appClass) throws Exception;
	
	public void appEnableNotification(String packageId, AppFamily appFamily, AppClass appClass) throws Exception;
	
	public void appUpdateNotification(String packageId, AppPackage newPackage) throws Exception;
}
