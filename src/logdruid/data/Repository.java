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
package logdruid.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicLong;

import logdruid.data.mine.MineResultSet;
import logdruid.data.record.MetadataRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.ReportRecording;
import logdruid.util.DataMiner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class Repository {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private ArrayList<Recording> recordings;
	private ArrayList<Source> sources;
	private String baseSourcePath;
	private ArrayList<DateFormat> dates;
	private boolean recursiveMode;
	private boolean onlyMatches;
	private boolean stats; // unused
	private boolean timings; // unused
	private HashMap<String, String> preferences;

	public Repository() {
		preferences = new HashMap<String, String>();
		preferences.put("timings", "false");
		preferences.put("stats", "true");
		preferences.put("chartSize", "350");
		preferences.put("ThreadPool_Group", "4");
		preferences.put("ThreadPool_File", "8");
		preferences.put("editorCommand", "gvim -R +$line $file");
		preferences.put("gatherstats", "true");
		preferences.put("gatherevents", "true");
		preferences.put("gatherreports", "true");
		recordings = new ArrayList<Recording>();
		dates = new ArrayList<DateFormat>();
		sources = new ArrayList<Source>();
		recursiveMode = false;
		// logger.info("repository ArrayList initialized");
	}

	public boolean isRecursiveMode() {
		return recursiveMode;
	}

	public void setPreference(String key, String value) {
		preferences.put(key, value);
	}

	public String getPreference(String key) {
		return getPreferences().get(key);
	}

	public void setRecursiveMode(boolean recursiveMode) {
		// logger.info("recursive mode is :"+recursiveMode);
		this.recursiveMode = recursiveMode;
	}

	public ArrayList<Source> getSources() {
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

	public void setSources(ArrayList<Source> sources) {
		this.sources = sources;
	}

	public void addSource(Source s) {
		if (sources == null) {
			sources = new ArrayList<Source>();
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

	public ArrayList<Recording> getRecordings() {
		// logger.info(xstream.toXML(recordings));
		return recordings;
	}

	public ArrayList<Recording> getRecordings(Class _class, boolean onlyActive) {

		ArrayList<Recording> statRecordingArrayList = new ArrayList<Recording>();
		Iterator recordingIterator = recordings.iterator();
		int cnt = 0;
		while (recordingIterator.hasNext()) {
			Recording r = (Recording) recordingIterator.next();
			if (r.getClass().equals(_class)) { 
				if (onlyActive){
					if (r.getIsActive())
					statRecordingArrayList.add(r);	
				} else {
				statRecordingArrayList.add(r);
				}
			}
		}

		return (ArrayList<Recording>) statRecordingArrayList;
	}


	public ArrayList getReportRecordings(MineResultSet mineResultSet1, boolean b) {
		ArrayList<Recording> temp = new ArrayList<Recording>();
		ArrayList<Recording> returned= new ArrayList<Recording>();		
		Iterator<Map<Recording, Map<List<Object>, Long>>> it1 = mineResultSet1.getOccurenceReport().values().iterator();
		while (it1.hasNext()){
			Iterator test = (Iterator) it1.next().keySet().iterator();
			while (test.hasNext())
			{
			temp.add((Recording) test.next());
			}
		}
		
		Iterator<Map<Recording, Map<List<Object>, Double>>> it2 =mineResultSet1.getSumReport().values().iterator();
		while (it2.hasNext()){
			Iterator test = (Iterator) it2.next().keySet().iterator();
			while (test.hasNext())
			{
			temp.add((Recording) test.next());
			}
		}
		
		Iterator<Map<Recording, SortedMap<Double, List<Object>>>> it3 =mineResultSet1.getTop100Report().values().iterator();
		while (it3.hasNext()){
			Iterator test = (Iterator) it3.next().keySet().iterator();
			while (test.hasNext())
			{
			temp.add((Recording) test.next());
			}
		}
		
		ArrayList<Recording> aL = getRecordings(ReportRecording.class, b);
		Iterator ite = aL.iterator();
		while (ite.hasNext()){
			Recording rec= (Recording) ite.next();
			if (temp.contains(rec))	{
			returned.add(rec);	
			}
		}
		return returned;
	}
	
	
	public Recording getRecording(Class _class, int id, boolean onlyActive) {
		return getRecordings(_class,onlyActive).get(id);
	}

	public HashMap<String, String> getPreferences() {
		if (preferences == null) {
			preferences = new HashMap<String, String>();
			logger.info("new preferences");
		}
		if (!preferences.containsKey("timings")) {
			preferences.put("timings", "false");
		}

		if (!preferences.containsKey("stats")) {
			logger.info("stats set to true");
			preferences.put("stats", "true");
		}

		if (!preferences.containsKey("chartSize")) {
			preferences.put("chartSize", "350");
		}

		if (!preferences.containsKey("ThreadPool_Group")) {
			preferences.put("ThreadPool_Group", "4");
		}
		if (!preferences.containsKey("ThreadPool_File")) {
			preferences.put("ThreadPool_File", "8");
		}
		if (!preferences.containsKey("editorCommand")) {
			preferences.put("editorCommand", "gvim -R +$line $file");
		}
		
		return preferences;
	}

	public void addRecording(Recording r) {
		recordings.add(r);
		// logger.info(xstream.toXML(recordings));<
	}

	public int getRecordingCount() {
		return recordings.size();
	}

	public void deleteRecording(int id) {
		recordings.remove(id);
		ArrayList<Source> sources= this.getSources();
		Iterator<Source> ite=sources.iterator();
		while (ite.hasNext()){
			Source src=(Source) ite.next();
			src.removeActiveRecording(recordings.get(id));
		}
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

	public void duplicateRecording(int id) {
		Recording newRecording = recordings.get(id).duplicate();
		this.addRecording(newRecording);
		ArrayList<Source> sources= this.getSources();
		Iterator<Source> ite=sources.iterator();
		if (!MetadataRecording.class.isInstance(recordings.get(id))){
		while (ite.hasNext()){
			Source src=(Source) ite.next();
			if (src.isActiveRecordingOnSource(recordings.get(id))){
				src.toggleActiveRecording(newRecording);
			}
		}}
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
			recordings = (ArrayList<Recording>) new XStream(new StaxDriver()).fromXML(file);
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

	public void setRecordings(ArrayList recordings) {
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

	public ArrayList<DateFormat> getDates() {
		return dates;
	}

	public void setDates(ArrayList<DateFormat> hm) {
		dates = hm;
	}

	public void addDateFormat(DateFormat df) {
		if (dates == null) {
			dates = new ArrayList<DateFormat>();
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
			if (df.getId() == id) {
				return df;
			}
		}
		return (DateFormat) null;
	}

	public void setOnlyMatches(boolean selected) {
		onlyMatches = selected;

	}

	public boolean isOnlyMatches() {
		return onlyMatches;
	}

	public boolean isStats() {
		return Boolean.parseBoolean(getPreference("stats"));
	}

	public void setStats(boolean stats) {
		setPreference("stats", Boolean.toString(stats));
	}

	public boolean isTimings() {
		return Boolean.parseBoolean(getPreference("timings"));
	}

	public void setTimings(boolean timings) {
		setPreference("timings", Boolean.toString(timings));
	}

}
