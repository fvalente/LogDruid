package logdruid.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import logdruid.data.record.Recording;
import logdruid.util.DataMiner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class Repository {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private Vector<Recording> recordings;
	private Vector<Source> sources;
	private String baseSourcePath;
	private Vector<DateFormat> dates;
	private boolean recursiveMode;
	private boolean onlyMatches;
	


	public Repository() {
		recordings = new Vector<Recording>();
		dates = new Vector<DateFormat>();
		sources = new Vector<Source>();
		recursiveMode = false;
		// logger.info("repository vector initialized");
	}

	public boolean isRecursiveMode() {
		return recursiveMode;
	}

	public void setRecursiveMode(boolean recursiveMode) {
		// logger.info("recursive mode is :"+recursiveMode);
		this.recursiveMode = recursiveMode;
	}

	public Vector<Source> getSources() {
		return sources;
	}

	public Source getSource(String name) {
		Iterator sourceIterator = sources.iterator();
		int cnt = 0;
		while (sourceIterator.hasNext()) {
			Source src = (Source) sourceIterator.next();
			if (src.getSourceName() == name) {
				return src;
			}
		}
		return (Source) null;
	}

	public void setSources(Vector<Source> sources) {
		this.sources = sources;
	}

	public void addSource(Source s) {
		if (sources == null) {
			sources = new Vector<Source>();
		}
		sources.add(s);
	}

	public void deleteSource(int id) {
		sources.remove(id);
	}

	public Source getSource(int id) {
		return sources.get(id);
	}

	public void updateSource(int id, String txtName, String txtRegularExp, Boolean active) {
		sources.get(id).setSourceName(txtName);
		sources.get(id).setSourcePattern(txtRegularExp);
		sources.get(id).setActive(active);
	}

	public Vector<Recording> getRecordings() {
		// logger.info(xstream.toXML(recordings));
		return recordings;
	}

	public Vector<Recording> getRecordings(Class _class) {

		Vector<Recording> statRecordingVector = new Vector<Recording>();
		Iterator recordingIterator = recordings.iterator();

		int cnt = 0;
		while (recordingIterator.hasNext()) {
			Recording r = (Recording) recordingIterator.next();
			if (r.getClass().equals(_class)) {
				// logger.info("getRecordings for type " + _class +
				// " add " + r.toString());
				statRecordingVector.add(r);
			}
		}

		return (Vector<Recording>) statRecordingVector;
	}

	public Recording getRecording(Class _class, int id) {

		Vector<Recording> statRecordingVector = new Vector<Recording>();
		Iterator recordingIterator = recordings.iterator();

		int cnt = 0;
		while (recordingIterator.hasNext()) {
			Recording r = (Recording) recordingIterator.next();
			if (r.getClass().equals(_class)) {
				// logger.info("getRecording for type " + _class +
				// " with id: "+id +", adds " + r.toString());
				statRecordingVector.add(r);
			}
		}

		return statRecordingVector.get(id);
	}

	public void addRecording(Recording r) {
		recordings.add(r);
		// logger.info(xstream.toXML(recordings));
	}

	public int getRecordingCount() {
		return recordings.size();
	}

	public void deleteRecording(int id) {
		recordings.remove(id);
	}

	public Recording getRecording(int id) {
		return recordings.get(id);
	}

	public Recording getRecording(String _id) {
		Iterator recordingIterator = recordings.iterator();
		Recording recReturn = null;
		int cnt = 0;
		while (recordingIterator.hasNext()) {
			Recording r = (Recording) recordingIterator.next();
			if (r.getId() == _id) {
				recReturn = r;
			}
		}
		return recReturn;
	}

	public Recording duplicateRecording(int id) {
		return ((Recording) (recordings.get(id)).duplicate());
	}

	public void update(Repository repo) {
		recordings = repo.getRecordings();
	}

	public void save(File file) {

		FileOutputStream fos = null;
		try {
			String xml = new XStream(new StaxDriver()).toXML(recordings);
			fos = new FileOutputStream(file);
			// fos.write("<?xml version=\"1.0\"?>".getBytes("UTF-8"));
			byte[] bytes = xml.getBytes("UTF-8");
			fos.write(bytes);

		} catch (Exception e) {
			System.err.println("Error in XML Write: " + e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void open(File file) {
		// XStream xstream = new XStream(new StaxDriver());
		FileOutputStream fos = null;
		try {
			recordings = (Vector<Recording>) new XStream(new StaxDriver()).fromXML(file);
			/*
			 * fos = new FileOutputStream(file);
			 * 
			 * byte[] bytes = xml.getBytes("UTF-8"); fos.write(bytes);
			 */
			logger.info(new XStream(new StaxDriver()).toXML(recordings));
		} catch (Exception e) {
			System.err.println("Error in XML Write: " + e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void setRecordings(Vector recordings) {
		this.recordings = recordings;
	}

	public void save() {

	}

	public String getBaseSourcePath() {
		return baseSourcePath;
	}

	public void setBaseSourcePath(String baseSourcePath) {
		this.baseSourcePath = baseSourcePath;
	}

	public Vector<DateFormat> getDates() {
		return dates;
	}

	public void setDates(Vector<DateFormat> hm) {
		dates = hm;
	}

	public void addDateFormat(DateFormat df) {
		if (dates == null) {
			dates = new Vector<DateFormat>();
		}
		dates.add(df);
		// logger.info(xstream.toXML(recordings));
	}

	public void deleteDateFormat(int id) {
		dates.remove(id);
	}

	public DateFormat getDateFormat(int id) {
		return dates.get(id);
	}

	public DateFormat getDateFormat(String id) {
		Iterator dateFormatIterator = dates.iterator();
		int cnt = 0;
		while (dateFormatIterator.hasNext()) {
			DateFormat df = (DateFormat) dateFormatIterator.next();
			if (logger.isEnabledFor(Level.TRACE)) logger.trace("getDateFormat "+ df.getId());
			if (df.getId() == id) {
				if (logger.isEnabledFor(Level.TRACE)) logger.trace("getDateFormat found");
				return df;
			}
		}
		return (DateFormat) null;
	}

	public void setOnlyMatches(boolean selected) {
		onlyMatches=selected;
		
	}
	public boolean isOnlyMatches() {
		return onlyMatches;
	}
}
