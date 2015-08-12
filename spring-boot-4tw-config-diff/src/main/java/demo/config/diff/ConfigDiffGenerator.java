/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.config.diff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import demo.config.diff.support.AetherDependencyResolver;
import demo.config.diff.support.ConfigurationMetadataRepositoryLoader;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;

/**
 *
 * @author Stephane Nicoll
 */
public class ConfigDiffGenerator {

	private final ConfigurationMetadataRepositoryLoader loader;

	public ConfigDiffGenerator(AetherDependencyResolver dependencyResolver) {
		this(new ConfigurationMetadataRepositoryLoader(dependencyResolver));
	}

	public ConfigDiffGenerator(ConfigurationMetadataRepositoryLoader loader) {
		this.loader = loader;
	}

	public ConfigDiffResult generateDiff(String leftVersion, String rightVersion) throws IOException {
		ConfigurationMetadataRepository left = loader.load(leftVersion);
		ConfigurationMetadataRepository right = loader.load(rightVersion);

		ConfigDiffResult result = new ConfigDiffResult(loader.resolveVersion(leftVersion),
				loader.resolveVersion(rightVersion));
		diffGroup(result, left, right);
		return result;
	}

	protected ConfigDiffGenerator diffGroup(ConfigDiffResult result,
			ConfigurationMetadataRepository left, ConfigurationMetadataRepository right) {
		List<String> matches = new ArrayList<>();
		Map<String, ConfigurationMetadataGroup> leftGroups = left.getAllGroups();
		Map<String, ConfigurationMetadataGroup> rightGroups = right.getAllGroups();
		for (ConfigurationMetadataGroup leftGroup : leftGroups.values()) {
			String id = leftGroup.getId();
			ConfigurationMetadataGroup rightGroup = rightGroups.get(id);
			if (rightGroup == null) {
				ConfigGroupDiff groupDiff = new ConfigGroupDiff(id, leftGroup, null);
				for (ConfigurationMetadataProperty property : leftGroup.getProperties().values()) {
					groupDiff.register(ConfigDiffType.DELETE,
							new ConfigPropertyDiff(property.getId(), property, null));
				}
				result.register(ConfigDiffType.DELETE, groupDiff);
			}
			else {
				matches.add(id);
				ConfigDiffType diffType = (equals(leftGroup, rightGroup) ? ConfigDiffType.EQUALS : ConfigDiffType.MODIFY);
				result.register(diffType, generateGroupDiff(leftGroup, rightGroup));
			}
		}
		for (ConfigurationMetadataGroup rightGroup : rightGroups.values()) {
			if (!matches.contains(rightGroup.getId())) {
				ConfigGroupDiff groupDiff = new ConfigGroupDiff(rightGroup.getId(), null, rightGroup);
				for (ConfigurationMetadataProperty property : rightGroup.getProperties().values()) {
					groupDiff.register(ConfigDiffType.ADD,
							new ConfigPropertyDiff(property.getId(), null, property));
				}
				result.register(ConfigDiffType.ADD, groupDiff);
			}
		}
		return this;
	}

	protected ConfigGroupDiff generateGroupDiff(ConfigurationMetadataGroup left, ConfigurationMetadataGroup right) {
		ConfigGroupDiff group = new ConfigGroupDiff(left.getId(), left, right);
		List<String> matches = new ArrayList<>();
		Map<String, ConfigurationMetadataProperty> leftProperties = left.getProperties();
		Map<String, ConfigurationMetadataProperty> rightProperties = right.getProperties();
		for (ConfigurationMetadataProperty leftProperty : leftProperties.values()) {
			String id = leftProperty.getId();
			ConfigurationMetadataProperty rightProperty = rightProperties.get(id);
			if (rightProperty == null) {
				group.register(ConfigDiffType.DELETE,
						new ConfigPropertyDiff(leftProperty.getId(), leftProperty, null));
			}
			else {
				matches.add(id);
				group.register(ConfigDiffType.EQUALS, // NICE: handle diff in property def
						new ConfigPropertyDiff(leftProperty.getId(), leftProperty, rightProperty));
			}
		}
		for (ConfigurationMetadataProperty rightProperty : rightProperties.values()) {
			if (!matches.contains(rightProperty.getId())) {
				group.register(ConfigDiffType.ADD,
						new ConfigPropertyDiff(rightProperty.getId(), null, rightProperty));
			}
		}
		return group;
	}

	private boolean equals(ConfigurationMetadataGroup left, ConfigurationMetadataGroup right) {
		if (left.getProperties().size() != right.getProperties().size()) {
			return false;
		}
		for (ConfigurationMetadataProperty property : left.getProperties().values()) {
			if (!right.getProperties().containsKey(property.getId())) {
				return false;
			}
		}
		return true;
	}

}
