package eu.selfnet5g.onboarding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import eu.selfnet5g.onboarding.engine.AppOnboardingManager;
import eu.selfnet5g.onboarding.interfaces.AppNotification;
import eu.selfnet5g.onboarding.interfaces.AppOnboarding;
import eu.selfnet5g.onboarding.interfaces.AppRegistration;
import eu.selfnet5g.onboarding.interfaces.plugins.OpenStackGlancePlugin;
import eu.selfnet5g.onboarding.interfaces.plugins.RabbitNotificationPlugin;
import eu.selfnet5g.onboarding.interfaces.plugins.SDNORestPlugin;

//@ComponentScan(basePackages="eu.selfnet5g.onboarding")
@SpringBootApplication
public class AppCatalogueApplication {
	
	@Value("${sdno.rest.server.ip:127.0.0.1}")
	private String sdnoIp;
	
	@Value("${sdno.rest.server.port:8181}")
	private String sdnoPort;
	
	@Bean
	AppOnboarding appOnboarding() {
		return new AppOnboardingManager();
	}
	
	@Bean
	AppNotification appNotification() {
		return new RabbitNotificationPlugin();
	}
	
	@Bean(name="sdnoAppRegistration")
	AppRegistration sdnoAppRegistration() {
		return new SDNORestPlugin(sdnoIp, sdnoPort);
	}
	
	@Bean(name="openstackGlanceService")
	AppRegistration openstackGlanceService() {
		return new OpenStackGlancePlugin();
	}

	public static void main(String[] args) {
		SpringApplication.run(AppCatalogueApplication.class, args);
	}
}

