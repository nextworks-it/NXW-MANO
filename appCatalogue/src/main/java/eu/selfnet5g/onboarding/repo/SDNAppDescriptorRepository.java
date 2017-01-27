package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.SDNAppDescriptor;

public interface SDNAppDescriptorRepository extends JpaRepository<SDNAppDescriptor, String> {
	Optional<SDNAppDescriptor> findById(String Id);
}
