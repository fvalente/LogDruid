/*******************************************************************************
 * LogDruid : Generate charts and reports using data gathered in log files
 * Copyright (C) 2016 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package logdruid.data.record;

public class RecordingItem {
	private String processingType;
	protected String name;
	protected String before;
	protected String after;
	protected String type;
	protected String inside;
	protected String value;
	protected boolean selected;
	protected boolean show;

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

	public RecordingItem(String name, String before, String type, String processingType, String insideRegex, String after, Boolean isSelected, Boolean show, String value) {
		this.name = name;
		this.before = before;
		this.after = after;
		this.value = value;
		this.type = type;
		this.selected = isSelected;
		this.processingType = processingType;
		this.inside=insideRegex;
		this.show=show;
		// super(name, before, type, after, isSelected, value);
		// TODO Auto-generated constructor stub
	}

	public RecordingItem(String name, String before, String type, String insideRegex, String after, Boolean isSelected, Boolean show, String value) {
		this.name = name;
		this.before = before;
		this.after = after;
		this.value = value;
		this.type = type;
		this.selected = isSelected;
		this.inside=insideRegex;
		this.show=show;
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

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public String getInside() {
		return inside;
	}

	public void setInside(String _inside) {
		this.inside = _inside;
	}
	
	

}
