package demo.config.diffview;

public class Property {

	private String id;

	private String name;

	private String type;

	private String description = "";

	private boolean deprecated = false;

	private String reason;

	private String replacement;

	public Property(String id, String name, String type, String description) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.description = description != null ? description : "";
	}

	public void setDeprecation(String reason, String replacement) {
		this.deprecated = true;
		this.reason = reason != null ? reason : "this property is deprecated";
		this.replacement = replacement != null ? replacement : "";
	}
}
