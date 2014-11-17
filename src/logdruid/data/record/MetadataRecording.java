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

import java.util.Vector;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

public class MetadataRecording extends Recording {
	private static Logger logger = Logger.getLogger(MetadataRecording.class.getName());
	private Vector recordingItem;
	private String dateFormat;

	public String getType() {
		return "Metadata";
	}

	public MetadataRecording(String _name, String _regexp, String _exampleLine, String _dateFormat, Boolean _isActive, Vector _recordingItem) {
		setName(_name);
		setRegexp(_regexp);
		setExampleLine(_exampleLine);
		setIsActive(_isActive);
		dateFormat = _dateFormat;
		recordingItem = _recordingItem;
		super.id = generate();
		logger.info("New MetadataRecording name: " + _name + ", regexp: " + _regexp + ", id: " + super.id);
		if (recordingItem != null)
			logger.info("New MetadataRecording with recordingItem vector: " + recordingItem.toString());
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDelimitator(String delimitator) {
		this.dateFormat = dateFormat;
	}

	public Vector getRecordingItem() {
		// logger.info("***********"+recordingItem.toString()+recordingItem.size());
		// Thread.currentThread().dumpStack();
		return (Vector) recordingItem;
	}

	public void setRecordingItem(Vector recordingItem) {
		this.recordingItem = recordingItem;
	}

	public void update(String txtName, String txtRegularExp, String exampleLine, String _dateFormat, Boolean _isActive, Vector _recordingItem) {
		setName(txtName);
		setRegexp(txtRegularExp);
		setExampleLine(exampleLine);
		setIsActive(_isActive);
		dateFormat = _dateFormat;
		recordingItem = _recordingItem;
	}

	public Recording duplicate() {

		Vector _recordingItem = null;
		// might need put back .toString() to those?? *** TBR
		String _name = getName().toString();
		String _regexp = getRegexp().toString();
		String _exampleLine = getExampleLine().toString();
		String _dateFormat = getDateFormat().toString();

		Boolean _isActive = getIsActive().booleanValue();
		if (getRecordingItem() != null) {
			_recordingItem = (Vector) getRecordingItem().clone();
		}
		return new MetadataRecording(_name, _regexp, _exampleLine, _dateFormat, _isActive, _recordingItem);
	}

}
