package logdruid.data.record;

import java.util.Vector;

import org.apache.log4j.Logger;

public class EventRecording extends Recording {
	private static Logger logger = Logger.getLogger(EventRecording.class.getName());
	private String dateFormat;
	

	public EventRecording(String _name, String _regexp,String _exampleLine, String _dateFormat, Boolean _isActive, Vector _recordingItem ){
		setName(_name);
		setRegexp(_regexp);
		setExampleLine(_exampleLine);
		setIsActive(_isActive);
		dateFormat=_dateFormat;
		recordingItem = _recordingItem;
		super.id=generate();
		logger.info("New EventRecording name: "+_name + ", regexp: "+ _regexp + ", id: "+ super.id );
		if (recordingItem!=null)
			logger.info("New EventRecording with recordingItem vector: "+recordingItem.toString());	
	}

	public String getType()
	{ 
		return "Event";
	}
	
	
	public String getDateFormat() {
		return dateFormat;
	}
	
	public void setDelimitator(String delimitator) {
		this.dateFormat = dateFormat;
	}
	
	

	public void setRecordingItem(Vector recordingItem) {
		this.recordingItem = recordingItem;
	}
	
	public void update(String txtName,String txtRegularExp, String exampleLine, String _dateFormat,Boolean _isActive, Vector _recordingItem){
		setName(txtName);
		setRegexp(txtRegularExp);
		setExampleLine(exampleLine);
		setIsActive(_isActive);
		dateFormat = _dateFormat;
		recordingItem = _recordingItem;
		if (this.id==null) {
			super.id=generate();
		}
	}

	public Recording duplicate(){

		Vector _recordingItem=null;
//might need put back .toString() to those?? *** TBR
		String _name=getName().toString();
		String _regexp=getRegexp().toString();
		String _exampleLine=getExampleLine().toString();
		String _dateFormat=dateFormat.toString();
		
		Boolean _isActive=getIsActive().booleanValue();
		if (recordingItem!=null){
			_recordingItem=(Vector)recordingItem.clone();
		}
		return new EventRecording (_name,_regexp,_exampleLine,_dateFormat,_isActive,_recordingItem);
	}
}
