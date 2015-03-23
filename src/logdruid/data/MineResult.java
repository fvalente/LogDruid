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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

import logdruid.util.DataMiner;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class MineResult implements Comparable {
	private static Logger logger = Logger.getLogger(MineResult.class.getName());
	private Date startDate;
	private Date endDate;
	Source source; // Source source; (tuning data size)
	ArrayList<FileRecord> logFiles;
	
	String group;
	Map<String, Map<Date,FileLine>> fileLine = new HashMap<String, Map<Date,FileLine>>();
	Map<String, ExtendedTimeSeries> statTimeSeriesMap;
	Map<String, ExtendedTimeSeries> eventTimeSeriesMap;
	Map<String, long[]> matchingStats; // 0-> sum of time for success matching
										// of given recording ; 1-> sum of time
										// for failed matching ; 2-> count of
										// match attempts, 3->count of success
										// attempts

	private ArrayList<Object[]> fileDates = new ArrayList<Object[]>();

	public MineResult(String _group, FileMineResultSet hm, ArrayList<FileRecord> arrayList, Repository repo, Source _source) {
		logFiles = arrayList;
		source = _source;
		statTimeSeriesMap = hm.statGroupTimeSeries;
		eventTimeSeriesMap = hm.eventGroupTimeSeries;
		matchingStats = hm.matchingStats;
		fileLine=hm.fileLineDateMap;
		startDate = hm.getStartDate();
		endDate = hm.getEndDate();
		fileDates = hm.getFileDates();
		group = _group;
	}

	public Source getSource() {
		return source;
	}
	public Date getStartDate() {
		return startDate;
	}

	public String getSourceID() {
		return source.getSourceName();
	}

	public ArrayList<FileRecord> getLogFiles() {
		return logFiles;
	}

	public String getGroup() {
		return group;
	}

	public Map<String, ExtendedTimeSeries> getStatTimeseriesMap() {
		return statTimeSeriesMap;
	}

	public Map<String, ExtendedTimeSeries> getEventTimeseriesMap() {
		return eventTimeSeriesMap;
	}

	public Map<String, long[]> getMatchingStats() {
		return matchingStats;
	}

	public Date getEndDate() {
		return endDate;

	}

	public File getFileForDate(Date date) {
		Iterator<Object[]> it = fileDates.iterator();
		while (it.hasNext()) {
			Object[] obj = it.next();
			if (logger.isDebugEnabled())
				logger.debug("1: " + (Date) obj[0] + "2: " + (Date) obj[1] + "3: " + ((FileRecord) obj[2]).getFile());
			if (date.after((Date) obj[0]) && date.before((Date) obj[1]))
				return (File) ((FileRecord) obj[2]).getFile();
		}
		return null;
	}

	public FileLine getFileLineForDate(Date date,String _group) {
	//	logger.info(group+","+date);
		if (date!=null && _group !=null){
			if(fileLine.containsKey(_group)){
			Map<Date,FileLine> map= fileLine.get(_group);	
			if(map.containsKey(date)){
			return fileLine.get(_group).get(date);		
			}
			}else 
		//		logger.info("null");
			return null;
				}
		// logger.info("null");
		 return null;
	}
	@Override
	public int compareTo(Object o) {
		String local = source.getSourceName() + group;
		String remote = ((MineResult) o).getSourceID() + ((MineResult) o).getGroup();
		//return remote.compareTo(local);
		return this.getStartDate().compareTo(((MineResult) o).getStartDate());

	}
}