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

import java.util.Comparator;

import demo.config.diff.support.AetherDependencyResolver;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.util.StringUtils;

public class ConfigDiffSample {

	private static final String NEW_LINE = System.getProperty("line.separator");

	public static void main(String[] args) throws Exception {
		ConfigDiffResult configDiffResult = new ConfigDiffGenerator(AetherDependencyResolver
				.withAllRepositories(false))
				.generateDiff("1.3.0.M1", "1.3.0.M3");

		System.out.println(formatDiff(configDiffResult));
	}

	private static String formatDiff(ConfigDiffResult diff) {
		StringBuilder out = new StringBuilder();
		out.append("Configuration properties change between `").append(diff.getLeftVersion())
				.append("` and `").append(diff.getRightVersion()).append("`").append(NEW_LINE);
		out.append(NEW_LINE);

		diff.getAllGroups().stream().sorted(groupComparator()).forEach(groupDiff -> {
			out.append("Group '").append(groupDiff.getId()).append("'").append(NEW_LINE);
			out.append("===========================================================").append(NEW_LINE);
			for (ConfigPropertyDiff propertyDiff : groupDiff.getAllProperties()) {
				if (propertyDiff.getLeft() == null && propertyDiff.getRight() != null) {
					appendProperty(out, propertyDiff.getRight(), true).append(NEW_LINE);
				}
				else if (propertyDiff.getLeft() != null && propertyDiff.getRight() == null) {
					appendProperty(out, propertyDiff.getLeft(), false).append(NEW_LINE);
				}
			}
			out.append(NEW_LINE);
		});
		return out.toString();
	}

	private static StringBuilder appendProperty(StringBuilder sb, ConfigurationMetadataProperty property, boolean add) {
		String symbol = add ? "[+]" : "[-]";
		sb.append(symbol).append(" ").append(property.getId());
		String shortDescription = property.getShortDescription();
		if (StringUtils.hasText(shortDescription)) {
			sb.append(" - ").append(shortDescription);
		}
		return sb;
	}

	private static Comparator<ConfigGroupDiff> groupComparator() {
		return (o1, o2) -> {
			if (ConfigurationMetadataRepository.ROOT_GROUP.equals(o1.getId())) {
				return -1;
			}
			if (ConfigurationMetadataRepository.ROOT_GROUP.equals(o2.getId())) {
				return 1;
			}
			return o1.getId().compareTo(o2.getId());
		};
	}
}
