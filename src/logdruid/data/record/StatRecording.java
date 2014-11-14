package logdruid.data.record;

import java.util.Vector;

import org.apache.log4j.Logger;

public class StatRecording extends Recording {
	private static Logger logger = Logger.getLogger(StatRecording.class.getName());
	private Vector recordingItem;
	private String dateFormat;

	public String getType() {
		return "Stat";
	}

	public StatRecording(String _name, String _regexp, String _exampleLine, String _dateFormat, Boolean _isActive, Vector _recordingItem) {
		setName(_name);
		setRegexp(_regexp);
		setExampleLine(_exampleLine);
		setIsActive(_isActive);
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
		this.dateFormat = dateFormat;
	}

	public Vector getRecordingItem() {
		// logger.info("***********"+recordingItem.toString()+recordingItem.size());
		// Thread.currentThread().dumpStack();
		return recordingItem;
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
		String _dateFormat = dateFormat.toString();

		Boolean _isActive = getIsActive().booleanValue();
		if (recordingItem != null) {
			_recordingItem = (Vector) recordingItem.clone();
		}
		return new StatRecording(_name, _regexp, _exampleLine, _dateFormat, _isActive, _recordingItem);
	}
}
