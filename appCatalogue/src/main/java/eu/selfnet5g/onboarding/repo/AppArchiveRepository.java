package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.AppArchive;


public interface AppArchiveRepository extends JpaRepository<AppArchive, String> {
	Optional<AppArchive> findById(String Id);	
}
