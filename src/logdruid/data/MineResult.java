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
import java.util.Map;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

import logdruid.util.DataMiner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class MineResult implements Comparable {
	private static Logger logger = Logger.getLogger(MineResult.class.getName());
	private Date startDate;
	private Date endDate;
	String source; // Source source; (tuning data size)
	ArrayList<String> logFiles;
	String group;
	Map<String, ExtendedTimeSeries> statTimeSeriesMap;
	Map<String, ExtendedTimeSeries> eventTimeSeriesMap;
	Map<String, long[]> matchingStats; // 0-> sum of time for success matching
										// of given recording ; 1-> sum of time
										// for failed matching ; 2-> count of
										// match attempts, 3->count of success
										// attempts

	private ArrayList<Object[]> fileDates = new ArrayList<Object[]>();

	public MineResult(String _group, FileMineResultSet hm, ArrayList<String> fileArrayList, Repository repo, Source _source) {
		logFiles = fileArrayList;
		source = _source.getSourceName();
		statTimeSeriesMap = hm.statGroupTimeSeries;
		eventTimeSeriesMap = hm.eventGroupTimeSeries;
		matchingStats = hm.matchingStats;
		startDate = hm.getStartDate();
		endDate = hm.getEndDate();
		fileDates = hm.getFileDates();
		group = _group;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getSourceID() {
		return source;
	}

	public ArrayList<String> getLogFiles() {
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
				logger.debug("1: " + (Date) obj[0] + "2: " + (Date) obj[1] + "3: " + (File) obj[2]);
			if (date.after((Date) obj[0]) && date.before((Date) obj[1]))
				return (File) obj[2];
		}
		return null;
	}

	@Override
	public int compareTo(Object o) {
		String local = source + group;
		String remote = ((MineResult) o).getSourceID() + ((MineResult) o).getGroup();
		return remote.compareTo(local);
	}
}