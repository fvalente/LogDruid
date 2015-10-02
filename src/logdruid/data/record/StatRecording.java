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

public class StatRecording extends Recording {
	private static Logger logger = Logger.getLogger(StatRecording.class.getName());
	private ArrayList recordingItem;
	private String dateFormat;

	public String getType() {
		return "Stat";
	}

	public StatRecording(String _name, String _regexp, String _exampleLine, String _dateFormat, Boolean _isActive, Boolean _caseSensitive, ArrayList _recordingItem) {
		setName(_name);
		setRegexp(_regexp);
		setExampleLine(_exampleLine);
		setIsActive(_isActive);
		setCaseSensitive(_caseSensitive);
		dateFormat = _dateFormat;
		recordingItem = _recordingItem;
		super.id = generate();
		// logger.info("New EventRecording name: "+_name + ", regexp: "+ _regexp
		// + ", id: "+ super.id +recordingItem.toString());
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDelimitator(String delimitator) {
	//	this.dateFormat = dateFormat;
	}

	public ArrayList getRecordingItem() {
		// logger.info("***********"+recordingItem.toString()+recordingItem.size());
		// Thread.currentThread().dumpStack();
		return recordingItem;
	}

	public void setRecordingItem(ArrayList recordingItem) {
		this.recordingItem = recordingItem;
	}

	public void update(String txtName, String txtRegularExp, String exampleLine, String _dateFormat, Boolean _isActive, Boolean _caseSensitive, ArrayList _recordingItem) {
		setName(txtName);
		setRegexp(txtRegularExp);
		setExampleLine(exampleLine);
		setIsActive(_isActive);
		setCaseSensitive(_caseSensitive);
		dateFormat = _dateFormat;
		recordingItem = _recordingItem;
	}

	public Recording duplicate() {

		ArrayList _recordingItem = null;
		// might need put back .toString() to those?? *** TBR
		String _name = getName().toString();
		String _regexp = getRegexp().toString();
		String _exampleLine = getExampleLine().toString();
		String _dateFormat = dateFormat.toString();

		Boolean _isActive = getIsActive().booleanValue();
		Boolean _caseSensitive = isCaseSensitive();
		if (recordingItem != null) {
			_recordingItem = (ArrayList) recordingItem.clone();
		}
		StatRecording eR=new StatRecording(_name, _regexp, _exampleLine, _dateFormat, _isActive, _caseSensitive, _recordingItem);
		eR.setDateFormatID(this.getDateFormatID());
		return eR;
		
	}
}
