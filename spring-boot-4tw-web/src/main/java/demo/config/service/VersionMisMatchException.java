package demo.config.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VersionMisMatchException extends RuntimeException {

	public VersionMisMatchException(String previousVersion, String nextVersion) {
		super(previousVersion + " is not prior to " + nextVersion);
	}
}
