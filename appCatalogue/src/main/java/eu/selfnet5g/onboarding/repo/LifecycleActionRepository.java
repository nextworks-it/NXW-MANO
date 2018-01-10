package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.LifecycleAction;

public interface LifecycleActionRepository extends JpaRepository<LifecycleAction, String> {
	Optional<LifecycleAction> findById(String Id);
}
