package demo.config.validation;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class VersionValidator implements ConstraintValidator<Version, String> {

	private static final Pattern versionPattern = Pattern.compile(
			"^(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.([^0-9]+)(\\d+)?)?$");

	@Override
	public void initialize(Version constraintAnnotation) {
	}

	@Override
	public boolean isValid(String version, ConstraintValidatorContext context) {
		return version == null || versionPattern.matcher(version).matches();
	}
}
