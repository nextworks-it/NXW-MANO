package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.MonitoringMetric;


public interface MonitoringMetricRepository extends JpaRepository<MonitoringMetric, String> {
	Optional<MonitoringMetric> findById(String Id);
}
