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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 *
 * @author Stephane Nicoll
 */
@SuppressWarnings("serial")
public class ConfigDiffResult implements Serializable {

	private final String leftVersion;

	private final String rightVersion;

	private final MultiValueMap<ConfigDiffType, ConfigGroupDiff> groups =
			new LinkedMultiValueMap<>();

	public ConfigDiffResult(String leftVersion, String rightVersion) {
		this.leftVersion = leftVersion;
		this.rightVersion = rightVersion;
	}

	public String getLeftVersion() {
		return leftVersion;
	}

	public String getRightVersion() {
		return rightVersion;
	}

	public List<ConfigGroupDiff> getGroupsDiffFor(ConfigDiffType type) {
		List<ConfigGroupDiff> content = this.groups.get(type);
		if (content == null) {
			return Collections.emptyList();
		}
		return content;
	}

	public Collection<ConfigGroupDiff> getAllGroups() {
		Collection<ConfigGroupDiff> result = new ArrayList<>();
		for (List<ConfigGroupDiff> configGroupDiffs : this.groups.values()) {
			result.addAll(configGroupDiffs);
		}
		return result;
	}

	void register(ConfigDiffType diffType, ConfigGroupDiff groupDiff) {
		this.groups.add(diffType, groupDiff);
	}

}
