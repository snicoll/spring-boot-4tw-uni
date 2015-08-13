package demo.config.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.boot.configurationmetadata.ValueProvider;

public class ConfigurationMetadata {

	@JsonView(DiffView.Summary.class)
	private String id;

	@JsonView(DiffView.Summary.class)
	private String name;

	private String type;

	private String description = "";

	@JsonView(DiffView.Summary.class)
	private String shortDescription = "";

	private Object defaultValue;

	@JsonView(DiffView.Summary.class)
	private boolean deprecated = false;

	@JsonView(DiffView.Summary.class)
	private String deprecationReason = "";

	@JsonView(DiffView.Summary.class)
	private String deprecationReplacement = "";

	private final List<ValueHint> valueHints = new ArrayList();

	private final List<ValueProvider> valueProviders = new ArrayList();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

	public String getDeprecationReason() {
		return deprecationReason;
	}

	public void setDeprecationReason(String deprecationReason) {
		this.deprecationReason = deprecationReason;
	}

	public String getDeprecationReplacement() {
		return deprecationReplacement;
	}

	public void setDeprecationReplacement(String deprecationReplacement) {
		this.deprecationReplacement = deprecationReplacement;
	}

	public List<ValueHint> getValueHints() {
		return valueHints;
	}

	public List<ValueProvider> getValueProviders() {
		return valueProviders;
	}
}
