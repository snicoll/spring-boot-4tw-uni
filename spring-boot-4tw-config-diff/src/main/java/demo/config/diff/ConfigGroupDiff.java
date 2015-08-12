package demo.config.diff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 *
 * @author Stephane Nicoll
 */
@SuppressWarnings("serial")
public class ConfigGroupDiff implements Serializable {

	private final String id;

	private final ConfigurationMetadataGroup left;

	private final ConfigurationMetadataGroup right;

	private final MultiValueMap<ConfigDiffType, ConfigPropertyDiff> properties;

	public ConfigGroupDiff(String id, ConfigurationMetadataGroup left, ConfigurationMetadataGroup right) {
		this.id = id;
		this.left = left;
		this.right = right;
		this.properties = new LinkedMultiValueMap<>();
	}

	public String getId() {
		return id;
	}

	public ConfigurationMetadataGroup getLeft() {
		return left;
	}

	public ConfigurationMetadataGroup getRight() {
		return right;
	}

	public List<ConfigPropertyDiff> getPropertiesDiffFor(ConfigDiffType type) {
		List<ConfigPropertyDiff> content = this.properties.get(type);
		if (content == null) {
			return Collections.emptyList();
		}
		return content;
	}

	public Collection<ConfigPropertyDiff> getAllProperties() {
		Collection<ConfigPropertyDiff> result = new ArrayList<>();
		for (List<ConfigPropertyDiff> propertyDiffs : this.properties.values()) {
			result.addAll(propertyDiffs);
		}
		return result;
	}

	void register(ConfigDiffType diffType, ConfigPropertyDiff propertyDiff) {
		this.properties.add(diffType, propertyDiff);
	}

}
