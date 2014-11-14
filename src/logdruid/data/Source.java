package logdruid.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

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
private Vector<SourceItem> sourceItemVector;
private Vector<String> selectedRecordingIds;

public Source (String name, String pattern,Boolean _active, Vector<SourceItem> _sourceItemVector ){
	sourceName=name;
	sourcePattern=pattern;
	sourceItemVector=_sourceItemVector;
	setActive(_active);
	selectedRecordingIds= new Vector<String>();
	
	
logger.info("Source loaded");

}
public String getSourcePattern() {
	return sourcePattern;
}
public void setSourcePattern(String sourcePattern) {
	this.sourcePattern = sourcePattern;
}
/*public void setActiveRecording(Recording recording){
	selectedRecordingIds.add(recording.getId());
	
}
public void unsetActiveRecording(Recording recording){
	selectedRecordingIds.remove(recording.getId());
}*/

public void toggleActiveRecording(Recording recording){
	//logger.info("toggleActiveRecording of "+recording.getName());
	if (selectedRecordingIds.contains(recording.getId())){
		selectedRecordingIds.remove(recording.getId());
		if (logger.isEnabledFor(Level.TRACE)) logger.trace("toggleActiveRecording removed "+recording.getName()+ " now size " + selectedRecordingIds.size() );
	}else {
		selectedRecordingIds.add(recording.getId());	
		if (logger.isEnabledFor(Level.TRACE)) logger.trace("toggleActiveRecording added "+recording.getName()+ " now size " + selectedRecordingIds.size() );
	}
	
}
public Boolean isActiveRecordingOnSource(Recording recording){
	
	//logger.info("***isActiveRecordingOnSource of "+recording.getName());
	Boolean b=false;
	Iterator it = selectedRecordingIds.iterator();
	String recordingId= recording.getId();
	while (it.hasNext()){
	//	String temp=(String)it.next();
		if ((String) it.next() ==recordingId){
			//logger.info("***isActiveRecordingOnSource of "+recording.getName() + " with id: "+ temp);			
			b=true;
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

public void setSourceItem(Vector<SourceItem> sourceItem) {
	this.sourceItemVector = sourceItem;
}

public Vector<SourceItem> getSourceItem() {
	return sourceItemVector;
}
public void setFilePattern(Vector<SourceItem> _sourceItem) {
	sourceItemVector = _sourceItem;
}
public Boolean getActive() {
	return active;
}
public void setActive(Boolean active) {
	this.active = active;
}


}
