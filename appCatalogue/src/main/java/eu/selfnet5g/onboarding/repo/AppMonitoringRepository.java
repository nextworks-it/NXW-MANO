package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.AppMonitoring;

public interface AppMonitoringRepository extends JpaRepository<AppMonitoring, String> {
	Optional<AppMonitoring> findById(String Id);
}
