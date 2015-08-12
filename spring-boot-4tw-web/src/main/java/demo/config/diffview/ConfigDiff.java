package demo.config.diffview;

import java.util.LinkedList;
import java.util.List;

public class ConfigDiff {

	private String previousVersion;

	private String nextVersion;

	private final List<GroupDiff> groups = new LinkedList<>();

	public ConfigDiff(String previousVersion, String nextVersion) {
		this.previousVersion = previousVersion;
		this.nextVersion = nextVersion;
	}

	public String getPreviousVersion() {
		return previousVersion;
	}

	public String getNextVersion() {
		return nextVersion;
	}

	public List<GroupDiff> getGroups() {
		return groups;
	}

	public void addGroup(GroupDiff group) {
		this.groups.add(group);
	}
}
