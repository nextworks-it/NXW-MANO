package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.PNFAppDescriptor;

public interface PNFAppDescriptorRepository extends JpaRepository<PNFAppDescriptor, String> {
	Optional<PNFAppDescriptor> findById(String Id);
}
