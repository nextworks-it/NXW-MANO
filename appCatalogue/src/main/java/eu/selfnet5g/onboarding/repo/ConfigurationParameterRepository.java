package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.ConfigurationParameter;


public interface ConfigurationParameterRepository extends JpaRepository<ConfigurationParameter, String> {
	Optional<ConfigurationParameter> findById(String Id);
}

