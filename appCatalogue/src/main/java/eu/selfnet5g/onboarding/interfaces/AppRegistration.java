package eu.selfnet5g.onboarding.interfaces;

import java.util.Map;

import eu.selfnet5g.onboarding.model.AppMetadata;

public interface AppRegistration {

	public Map<String,String> registerNewApp(AppMetadata metadata) throws Exception;
	
	public void unregisterApp(AppMetadata metadata) throws Exception;
}
