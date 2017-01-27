package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.MetricValue;

public interface MetricValueRepository extends JpaRepository<MetricValue, String> {
	Optional<MetricValue> findById(String Id);
}
