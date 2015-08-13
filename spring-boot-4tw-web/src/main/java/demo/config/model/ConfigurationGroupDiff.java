package demo.config.model;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import demo.config.diff.ConfigDiffType;

public class ConfigurationGroupDiff {

	@JsonView(DiffView.Summary.class)
	private String id;

	@JsonView(DiffView.Summary.class)
	private ConfigDiffType diffType;

	@JsonView(DiffView.Summary.class)
	private final List<ConfigurationPropertyDiff> properties = new LinkedList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ConfigDiffType getDiffType() {
		return diffType;
	}

	public void setDiffType(ConfigDiffType diffType) {
		this.diffType = diffType;
	}

	public List<ConfigurationPropertyDiff> getProperties() {
		return properties;
	}

}
