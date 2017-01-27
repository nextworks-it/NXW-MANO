package eu.selfnet5g.onboarding.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public EntityNotFoundException(String id) {
		super ("Entity with ID " + id + " not found");
	}
}