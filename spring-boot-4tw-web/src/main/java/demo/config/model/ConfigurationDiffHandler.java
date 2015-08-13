package demo.config.model;

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

public class ConfigurationDiffHandler {

	private static final Comparator<ConfigGroupDiff> GROUP_COMPARATOR = new GroupComparator();

	private static final Comparator<ConfigPropertyDiff> PROPERTY_COMPARATOR = new PropertyComparator();

	public ConfigurationDiff handle(ConfigDiffResult original) {
		ConfigurationDiff diff = new ConfigurationDiff();
		diff.setFromVersion(original.getLeftVersion());
		diff.setToVersion(original.getRightVersion());
		Map<String, ConfigDiffType> groupIdToDiffType = mapGroupIdToDiffType(original);
		Collection<ConfigGroupDiff> allGroups = sortGroups(original.getAllGroups());
		for (ConfigGroupDiff originalGroup : allGroups) {
			String groupId = originalGroup.getId();
			ConfigDiffType diffType = groupIdToDiffType.get(groupId);
			if (diffType != ConfigDiffType.EQUALS) {
				ConfigurationGroupDiff groupDiff = new ConfigurationGroupDiff();
				groupDiff.setId(groupId);
				groupDiff.setDiffType(diffType);
				Map<String, ConfigDiffType> propertyIdToDiffType = mapPropertyIdToDiffType(originalGroup);
				Collection<ConfigPropertyDiff> allProperties = sortProperties(originalGroup.getAllProperties());
				for (ConfigPropertyDiff originalProperty : allProperties) {
					String propertyId = originalProperty.getId();
					ConfigurationPropertyDiff propertyDiff = new ConfigurationPropertyDiff();
					propertyDiff.setId(propertyId);
					propertyDiff.setDiffType(propertyIdToDiffType.get(propertyId));
					propertyDiff.setFrom(convertConfigMetadata(originalProperty.getLeft()));
					propertyDiff.setTo(convertConfigMetadata(originalProperty.getRight()));
					groupDiff.getProperties().add(propertyDiff);
				}

				diff.getGroups().add(groupDiff);
			}
		}

		return diff;
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
				= new ArrayList<ConfigGroupDiff>(groups);
		Collections.sort(result, GROUP_COMPARATOR);
		return result;
	}

	private List<ConfigPropertyDiff> sortProperties(Collection<ConfigPropertyDiff> properties) {
		List<ConfigPropertyDiff> result =
				new ArrayList<ConfigPropertyDiff>(properties);
		Collections.sort(result, PROPERTY_COMPARATOR);
		return result;
	}

	private ConfigurationMetadata convertConfigMetadata(ConfigurationMetadataProperty source) {
		ConfigurationMetadata destination = new ConfigurationMetadata();
		if (source != null) {
			destination.setDefaultValue(source.getDefaultValue());
			destination.setId(source.getId());
			destination.setName(source.getName());
			destination.setType(source.getType());
			destination.setDescription(source.getDescription());
			destination.setShortDescription(source.getShortDescription());
			destination.setDeprecated(source.isDeprecated());
			if (source.getDeprecation() != null) {
				destination.setDeprecationReason(source.getDeprecation().getReason());
				destination.setDeprecationReplacement(source.getDeprecation().getReplacement());
			}
			destination.getValueHints().addAll(source.getValueHints());
			destination.getValueProviders().addAll(source.getValueProviders());
		}
		return destination;
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

	private static class PropertyComparator implements Comparator<ConfigPropertyDiff> {
		@Override
		public int compare(ConfigPropertyDiff o1, ConfigPropertyDiff o2) {
			return o1.getId().compareTo(o2.getId());
		}
	}
}
