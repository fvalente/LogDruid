/*******************************************************************************
 * LogDruid : chart statistics and events retrieved in logs files through configurable regular expressions
 * Copyright (C) 2014 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package logdruid.data.record;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class ReportRecording extends Recording {
	private static Logger logger = Logger.getLogger(ReportRecording.class.getName());
	private String dateFormat;
	private String subType;

	public ReportRecording(String _name, String _regexp, String _exampleLine, String _dateFormat, Boolean _isActive, ArrayList<RecordingItem> _recordingItem,String subType1) {
		setName(_name);
		setRegexp(_regexp);
		setExampleLine(_exampleLine);
		setIsActive(_isActive);
		dateFormat = _dateFormat;
		recordingItem = _recordingItem;
		subType=subType1;
		super.id = generate();
		logger.info("New ReportRecording name: " + _name + ", regexp: " + _regexp + ", id: " + super.id);
		if (recordingItem != null)
			logger.info("New ReportRecording with recordingItem ArrayList: " + recordingItem.toString());
	}

	public String getType() {
		return "Report";
	}

	public void setSubType(String subType1) {
		this.subType=subType1;
	}
	public String getSubType() {
		return subType;
	}
	
	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void setRecordingItem(ArrayList<RecordingItem> recordingItem) {
		this.recordingItem = recordingItem;
	}

	public void update(String txtName, String txtRegularExp, String exampleLine, String _dateFormat, Boolean _isActive, ArrayList<RecordingItem> _recordingItem, String subType1) {
		setName(txtName);
		setRegexp(txtRegularExp);
		setExampleLine(exampleLine);
		setIsActive(_isActive);
		dateFormat = _dateFormat;
		subType=subType1;
		recordingItem = _recordingItem;
		if (this.id == null) {
			super.id = generate();
		}
	}

	public Recording duplicate() {

		ArrayList<RecordingItem> _recordingItem = null;
		// might need put back .toString() to those?? *** TBR
		String _name = getName().toString();
		String _regexp = getRegexp().toString();
		String _exampleLine = getExampleLine().toString();
		String _dateFormat = dateFormat.toString();
		String _subType=subType.toString();

		Boolean _isActive = getIsActive().booleanValue();
		if (recordingItem != null) {
			_recordingItem = (ArrayList<RecordingItem>) recordingItem.clone();
		}
		ReportRecording eR=new ReportRecording(_name, _regexp, _exampleLine, _dateFormat, _isActive, _recordingItem, _subType);
		eR.setDateFormatID(this.getDateFormatID());
		return eR;
	}
}