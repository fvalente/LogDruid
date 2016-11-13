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

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class EventRecording extends Recording {
	private static Logger logger = Logger.getLogger(EventRecording.class.getName());
	private String dateFormat;

	public EventRecording(String _name, String _regexp, String _exampleLine, String _dateFormat, Boolean _isActive, Boolean _useSourceDateFormat, boolean _caseSensitive, ArrayList<RecordingItem> _recordingItem) {
		setName(_name);
		setRegexp(_regexp);
		setExampleLine(_exampleLine);
		setIsActive(_isActive);
		setUseSourceDateFormat(_useSourceDateFormat);
		dateFormat = _dateFormat;
		recordingItem = _recordingItem;
		setCaseSensitive(_caseSensitive);
		super.id = generate();
		logger.info("New EventRecording name: " + _name + ", regexp: " + _regexp + ", id: " + super.id);
		if (recordingItem != null)
			logger.info("New EventRecording with recordingItem ArrayList: " + recordingItem.toString());
	}

	public String getType() {
		return "Event";
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

	public void update(String txtName, String txtRegularExp, String exampleLine, String _dateFormat, Boolean _isActive, Boolean _useSourceDateFormat, Boolean _caseSensitive, ArrayList<RecordingItem> _recordingItem) {
		setName(txtName);
		setRegexp(txtRegularExp);
		setExampleLine(exampleLine);
		setIsActive(_isActive);
		setCaseSensitive(_caseSensitive);
		setUseSourceDateFormat(_useSourceDateFormat);
		dateFormat = _dateFormat;
		recordingItem = _recordingItem;
		logger.debug("RIArrayLit size: "+ recordingItem.size());
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

		Boolean _isActive = getIsActive().booleanValue();
		Boolean _caseSensitive = isCaseSensitive();
		Boolean _useSourceDateFormat=getUseSourceDateFormat();
		if (recordingItem != null) {
			_recordingItem = (ArrayList<RecordingItem>) recordingItem.clone();
		}
		EventRecording eR=new EventRecording(_name, _regexp, _exampleLine, _dateFormat, _isActive, _useSourceDateFormat, _caseSensitive, _recordingItem);
		eR.setDateFormatID(this.getDateFormatID());
		return eR;
	}
}
