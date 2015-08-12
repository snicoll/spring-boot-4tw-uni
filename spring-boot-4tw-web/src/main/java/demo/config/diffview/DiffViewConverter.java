package demo.config.diffview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import demo.config.diff.ConfigDiffResult;
import demo.config.diff.ConfigDiffType;
import demo.config.diff.ConfigGroupDiff;
import demo.config.diff.ConfigPropertyDiff;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.stereotype.Component;

@Component
public class DiffViewConverter {

	private static final Comparator<ConfigGroupDiff> GROUP_COMPARATOR = new GroupComparator();

	private static final Comparator<demo.config.diff.ConfigPropertyDiff> PROPERTY_COMPARATOR = new PropertyComparator();

	public ConfigDiff convert(ConfigDiffResult original) {

		String previousVersion = original.getLeftVersion();
		String nextVersion = original.getRightVersion();

		ConfigDiff configDiff = new ConfigDiff(previousVersion, nextVersion);

		Map<String, ConfigDiffType> groupIdToDiffType = mapGroupIdToDiffType(original);
		Collection<ConfigGroupDiff> allGroups = sortGroups(original.getAllGroups());

		for (ConfigGroupDiff originalGroup : allGroups) {
			GroupDiff groupDiff = new GroupDiff(originalGroup.getId(), previousVersion, nextVersion,
					groupIdToDiffType.get(originalGroup.getId()));
			Map<String, ConfigDiffType> propertyIdToDiffType = mapPropertyIdToDiffType(originalGroup);
			Collection<ConfigPropertyDiff> allProperties = sortProperties(originalGroup.getAllProperties());
			for (ConfigPropertyDiff originalProperty : allProperties) {
				String propertyId = originalProperty.getId();
				PropertyDiff propertyDiff = new PropertyDiff(propertyIdToDiffType.get(propertyId),
						convertProperty(originalProperty.getLeft()),
						convertProperty(originalProperty.getRight()));
				groupDiff.addProperty(propertyDiff);
			}
			configDiff.addGroup(groupDiff);
		}
		return configDiff;
	}

	private Property convertProperty(ConfigurationMetadataProperty original) {
		if (original == null) {
			return null;
		}
		Property property = new Property(original.getId(), original.getName(), original.getType(), original.getDescription());
		if (original.isDeprecated()) {
			property.setDeprecation(original.getDeprecation().getReason(), original.getDeprecation().getReplacement());
		}
		return property;
	}

	private Map<String, ConfigDiffType> mapGroupIdToDiffType(ConfigDiffResult original) {
		Map<String, ConfigDiffType> result = new HashMap<>();
		for (ConfigDiffType type : ConfigDiffType.values()) {
			original.getGroupsDiffFor(type).forEach(g -> result.put(g.getId(), type));
		}
		return result;
	}

	private Map<String, ConfigDiffType> mapPropertyIdToDiffType(ConfigGroupDiff original) {
		Map<String, ConfigDiffType> result = new HashMap<>();
		for (ConfigDiffType type : ConfigDiffType.values()) {
			original.getPropertiesDiffFor(type).forEach(g -> result.put(g.getId(), type));
		}
		return result;
	}

	private List<ConfigGroupDiff> sortGroups(Collection<ConfigGroupDiff> groups) {
		List<ConfigGroupDiff> result
				= new ArrayList<>(groups);
		Collections.sort(result, GROUP_COMPARATOR);
		return result;
	}

	private List<demo.config.diff.ConfigPropertyDiff> sortProperties(Collection<demo.config.diff.ConfigPropertyDiff> properties) {
		List<demo.config.diff.ConfigPropertyDiff> result =
				new ArrayList<>(properties);
		Collections.sort(result, PROPERTY_COMPARATOR);
		return result;
	}

	private static class GroupComparator implements Comparator<ConfigGroupDiff> {

		@Override
		public int compare(ConfigGroupDiff o1, ConfigGroupDiff o2) {
			if (ConfigurationMetadataRepository.ROOT_GROUP.equals(o1.getId())) {
				return -1;
			}
			if (ConfigurationMetadataRepository.ROOT_GROUP.equals(o2.getId())) {
				return 1;
			}
			return o1.getId().compareTo(o2.getId());
		}
	}

	private static class PropertyComparator implements Comparator<demo.config.diff.ConfigPropertyDiff> {
		@Override
		public int compare(demo.config.diff.ConfigPropertyDiff o1, ConfigPropertyDiff o2) {
			return o1.getId().compareTo(o2.getId());
		}
	}
}
