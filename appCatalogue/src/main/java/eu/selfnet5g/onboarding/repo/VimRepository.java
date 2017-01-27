package eu.selfnet5g.onboarding.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.selfnet5g.onboarding.model.Vim;

public interface VimRepository extends JpaRepository<Vim, String> {
	Optional<Vim> findById(String Id);
	Optional<Vim> findByName(String vimName);
}
