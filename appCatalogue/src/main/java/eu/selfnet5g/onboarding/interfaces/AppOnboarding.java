package eu.selfnet5g.onboarding.interfaces;

import java.util.Collection;

import eu.selfnet5g.onboarding.exception.EntityNotFoundException;
import eu.selfnet5g.onboarding.exception.InternalServerErrorException;
import eu.selfnet5g.onboarding.exception.MethodNotAllowedException;
import eu.selfnet5g.onboarding.model.AppClass;
import eu.selfnet5g.onboarding.model.AppPackage;

public interface AppOnboarding {

	public String appOnboard(byte[] appPackage) throws Exception;
	
	public void appOffboard(String packageId) throws EntityNotFoundException, MethodNotAllowedException, InternalServerErrorException;
	
	public AppPackage appGet(String packageId) throws EntityNotFoundException;
	
	public Collection<AppPackage> appsGet() throws Exception;
	
	public Collection<AppPackage> appsGetByClass(AppClass appClass) throws Exception;
	
	public Collection<AppPackage> appsGetByType(String appType) throws Exception;
		
	public void appDisable(String packageId) throws EntityNotFoundException, InternalServerErrorException;
	
	public void appEnable(String packageId) throws EntityNotFoundException, InternalServerErrorException;
	
	public void appUpdate(String packageId, AppPackage pckg) throws EntityNotFoundException, InternalServerErrorException;
	
}
