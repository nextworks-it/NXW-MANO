package eu.selfnet5g.onboarding.interfaces;

import eu.selfnet5g.onboarding.model.AppMetadata;

public interface AppRegistration {

	public String registerNewApp(AppMetadata metadata) throws Exception;
	
	public void unregisterApp(AppMetadata metadata) throws Exception;
}
