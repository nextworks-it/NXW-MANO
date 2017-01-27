package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.ConfigurationValue;

public interface ConfigurationValueRepository extends JpaRepository<ConfigurationValue, String> {
	Optional<ConfigurationValue> findById(String Id);
}

