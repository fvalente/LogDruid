package logdruid.data.record;

import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DataFormatException;

import logdruid.util.DataMiner;

import org.apache.log4j.Logger;

public abstract class Recording {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private String name;
	private String regexp;
	protected Vector recordingItem;
	private String simpleDateFormat;
	private String exampleLine;
	private Boolean isActive;
	protected String id;
	private String dateFormatID;

	public String getId() {
		return id;
	}

	public String generate() {
		String generatedUniqueId = UUID.randomUUID().toString();
		logger.info("unique ID: " + generatedUniqueId);
		return generatedUniqueId;
	}

	public Vector getRecordingItem() {
		// logger.info("***********"+recordingItem.toString()+recordingItem.size());
		// Thread.currentThread().dumpStack();
		return recordingItem;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public String getExampleLine() {
		return exampleLine;
	}

	public void setExampleLine(String exampleLine) {
		this.exampleLine = exampleLine;
	}

	// public abstract Recording duplicate();
	@SuppressWarnings("rawtypes")
	public abstract String getType();

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	abstract public Recording duplicate();

	public String getDateFormatID() {
		return dateFormatID;
	}

	public void setDateFormatID(String dateFormatID) {
		this.dateFormatID = dateFormatID;
	}

}
