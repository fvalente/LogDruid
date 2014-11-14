package logdruid.data;

public class SourceItem {

	private String name;
	private String before;
	private String after;
	private String type;
	private String value;
	private boolean selected;

	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	public SourceItem(String name, String before, String type, String after, Boolean isSelected, String value) {
		this.name = name;
		this.before = before;
		this.after = after;
		this.value = value;
		this.type = type;
		this.selected = isSelected;
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
