package eu.selfnet5g.onboarding.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class MethodNotAllowedException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public MethodNotAllowedException(String reason) {
		super ("Method not allowed: " + reason);
	}
}
