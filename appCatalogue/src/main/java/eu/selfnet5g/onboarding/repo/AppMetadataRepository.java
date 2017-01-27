package eu.selfnet5g.onboarding.repo;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.AppClass;
import eu.selfnet5g.onboarding.model.AppMetadata;

public interface AppMetadataRepository extends JpaRepository<AppMetadata, String>  {
	Optional<AppMetadata> findById(String Id);
	Collection<AppMetadata> findByAppClassAndAppType(AppClass appClass, String appType);
}

