package eu.selfnet5g.onboarding.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InternalServerErrorException(String reason) {
		super ("Internal Server Error: " + reason);
	}
}

