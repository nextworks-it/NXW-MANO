package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.ConfigurationCommunication;

public interface ConfigurationCommunicationRepository extends JpaRepository<ConfigurationCommunication, String> {
	Optional<ConfigurationCommunication> findById(String Id);
}

