package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.ConfigurationEndpoint;

public interface ConfigurationEndpointRepository extends JpaRepository<ConfigurationEndpoint, String> {
	Optional<ConfigurationEndpoint> findById(String Id);
}

