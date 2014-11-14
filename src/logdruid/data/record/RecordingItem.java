package logdruid.data.record;

public class RecordingItem {
	private String processingType;
	protected String name;
	protected String before;
	protected String after;
	protected String type;
	protected String value;
	protected boolean selected; 

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
	public String getProcessingType() {
		return processingType;
	}

	public void setProcessingType(String processingType) {
		this.processingType = processingType;
	}

	public RecordingItem(String name, String before, String type, String processingType,String after, Boolean isSelected, String value) {
		this.name=name;
		this.before=before;
		this.after=after;
		this.value=value;
		this.type=type;
		this.selected=isSelected;
		this.processingType=processingType;
	//	super(name, before, type, after, isSelected, value);
		// TODO Auto-generated constructor stub
	}

	public RecordingItem(String name, String before,String type, String after,Boolean isSelected,String value) {
	this.name=name;
	this.before=before;
	this.after=after;
	this.value=value;
	this.type=type;
	this.selected=isSelected;
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
