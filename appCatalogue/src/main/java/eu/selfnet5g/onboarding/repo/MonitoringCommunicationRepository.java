package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.MonitoringCommunication;

public interface MonitoringCommunicationRepository extends JpaRepository<MonitoringCommunication, String> {
	Optional<MonitoringCommunication> findById(String Id);
}
