package demo.config.diff;

import java.io.Serializable;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 *
 * @author Stephane Nicoll
 */
@SuppressWarnings("serial")
public class ConfigPropertyDiff implements Serializable {

	private final String id;

	private final ConfigurationMetadataProperty left;

	private final ConfigurationMetadataProperty right;

	public ConfigPropertyDiff(String id, ConfigurationMetadataProperty left, ConfigurationMetadataProperty right) {
		this.left = left;
		this.right = right;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public ConfigurationMetadataProperty getLeft() {
		return left;
	}

	public ConfigurationMetadataProperty getRight() {
		return right;
	}
}
