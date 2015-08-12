package demo.config.diff.support;

/**
 * Thrown when an unknown Spring Boot version is used.
 *
 * @author Stephane Nicoll
 */
@SuppressWarnings("serial")
public class UnknownSpringBootVersion extends RuntimeException {

	private final String version;

	public UnknownSpringBootVersion(String message, String version) {
		super(message);
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

}
