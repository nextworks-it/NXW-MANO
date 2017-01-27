package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.VMImage;

public interface VMImageRepository extends JpaRepository<VMImage, String> {
	Optional<VMImage> findById(String Id);
}
