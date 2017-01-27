package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.AppConfiguration;

public interface AppConfigurationRepository extends JpaRepository<AppConfiguration, String> {
	Optional<AppConfiguration> findById(String Id);
}

