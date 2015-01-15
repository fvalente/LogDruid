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
package logdruid.data;

import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;

import logdruid.data.record.Recording;
import logdruid.util.DataMiner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class Source {
	private static Logger logger = Logger.getLogger(Source.class.getName());
	private String sourceName;
	private String sourcePattern;
	private Boolean active;
	private ArrayList<SourceItem> sourceItemArrayList;
	private ArrayList<String> selectedRecordingIds;

	public Source(String name, String pattern, Boolean _active, ArrayList<SourceItem> _sourceItemArrayList) {
		sourceName = name;
		sourcePattern = pattern;
		sourceItemArrayList = _sourceItemArrayList;
		setActive(_active);
		selectedRecordingIds = new ArrayList<String>();

		logger.info("Source loaded");

	}

	public String getSourcePattern() {
		return sourcePattern;
	}

	public void setSourcePattern(String sourcePattern) {
		this.sourcePattern = sourcePattern;
	}

	/*
	 * public void setActiveRecording(Recording recording){
	 * selectedRecordingIds.add(recording.getId());
	 * 
	 * } public void unsetActiveRecording(Recording recording){
	 * selectedRecordingIds.remove(recording.getId()); }
	 */

	public void toggleActiveRecording(Recording recording) {
		// logger.info("toggleActiveRecording of "+recording.getName());
		if (selectedRecordingIds.contains(recording.getId())) {
			selectedRecordingIds.remove(recording.getId());
			if (logger.isEnabledFor(Level.TRACE))
				logger.trace("toggleActiveRecording removed " + recording.getName() + " now size " + selectedRecordingIds.size());
		} else {
			selectedRecordingIds.add(recording.getId());
			if (logger.isEnabledFor(Level.TRACE))
				logger.trace("toggleActiveRecording added " + recording.getName() + " now size " + selectedRecordingIds.size());
		}

	}

	public Boolean isActiveRecordingOnSource(Recording recording) {

		// logger.info("***isActiveRecordingOnSource of "+recording.getName());
		Boolean b = false;
		Iterator it = selectedRecordingIds.iterator();
		String recordingId = recording.getId();
		while (it.hasNext()) {
			// String temp=(String)it.next();
			if ((String) it.next() == recordingId) {
				// logger.info("***isActiveRecordingOnSource of "+recording.getName()
				// + " with id: "+ temp);
				b = true;
			}
		}
		return b;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public void setSourceItem(ArrayList<SourceItem> sourceItem) {
		this.sourceItemArrayList = sourceItem;
	}

	public ArrayList<SourceItem> getSourceItem() {
		return sourceItemArrayList;
	}

	public void setFilePattern(ArrayList<SourceItem> _sourceItem) {
		sourceItemArrayList = _sourceItem;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
