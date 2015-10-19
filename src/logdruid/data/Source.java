/*******************************************************************************
 * LogDruid : chart statistics and events retrieved in logs files through configurable regular expressions
 * Copyright (C) 2014, 2015 Frederic Valente (frederic.valente@gmail.com)
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

import logdruid.data.record.MetadataRecording;
import logdruid.data.record.Recording;
import logdruid.util.DataMiner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class Source implements Comparable{
	private static Logger logger = Logger.getLogger(Source.class.getName());
	private String sourceName;
	private String sourcePattern;
	private Boolean active;
	private MetadataRecording activeMetadataRecording;
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

	public void setActiveMetadata(MetadataRecording _activeMetadataRecording){
		activeMetadataRecording=_activeMetadataRecording;
	}

	public MetadataRecording getActiveMetadata(){
		return activeMetadataRecording;
	}

/*	public int getActiveMetadataId(){
		if ((sourceItemArrayList!=null) && (activeMetadataRecording!=null)){
			return sourceItemArrayList.indexOf(activeMetadataRecording);	
		}
		else{
			return -1;
		}
	}*/
	
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

	public void removeActiveRecording(Recording recording) {
		// logger.info("toggleActiveRecording of "+recording.getName());
		if (selectedRecordingIds.contains(recording.getId())) {
			selectedRecordingIds.remove(recording.getId());
			if (logger.isEnabledFor(Level.TRACE))
				logger.trace("toggleActiveRecording removed " + recording.getName() + " now size " + selectedRecordingIds.size());
		} 
	}
	
	
	
	public Boolean isActiveRecordingOnSource(Recording recording) {
		if (MetadataRecording.class.isInstance(recording)){
				if (getActiveMetadata()!=null && getActiveMetadata()==recording){
					return true;
				}
				else return false;
		}
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

	@Override
	public int compareTo(Object o) {
		String local = getSourceName();
		String remote = ((Source)o).getSourceName();
		//return remote.compareTo(local);
		return local.compareTo(remote);

	}

}
