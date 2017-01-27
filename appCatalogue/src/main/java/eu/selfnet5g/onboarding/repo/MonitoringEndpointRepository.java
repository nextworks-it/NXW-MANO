package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.MonitoringEndpoint;

public interface MonitoringEndpointRepository extends JpaRepository<MonitoringEndpoint, String> {
	Optional<MonitoringEndpoint> findById(String Id);
}
