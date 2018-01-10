package eu.selfnet5g.onboarding.repo;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.AppClass;
import eu.selfnet5g.onboarding.model.AppPackage;

public interface AppPackageRepository extends JpaRepository<AppPackage, String> {
	Optional<AppPackage> findById(String Id);
	Collection<AppPackage> findByMetadataAppType(String appType);
	Collection<AppPackage> findByMetadataAppClass(AppClass appClass);
}
