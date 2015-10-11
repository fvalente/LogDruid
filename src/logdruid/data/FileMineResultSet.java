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

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class FileMineResultSet {
	private static Logger logger = Logger.getLogger(FileMineResultSet.class.getName());
	public Map<String, ExtendedTimeSeries> statGroupTimeSeries;
	public Map<String, ExtendedTimeSeries> eventGroupTimeSeries;
	public Map<String,Map<Date, FileLine>> fileLineDateMap;
	public Map<String, long[]> matchingStats; // 0-> sum of time for success
												// matching of given recording ;
												// 1-> sum of time for failed
												// matching ; 2-> count of match
												// attempts, 3->count of success
												// attempts
	private Date startDate;
	private Date endDate;

	private ArrayList<Object[]> fileDates = new ArrayList<Object[]>();

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public ArrayList<Object[]> getFileDates() {
		return fileDates;
	}

	public FileMineResultSet(ArrayList<Object[]> _fileDates, Map<String, ExtendedTimeSeries> statMap, Map<String, ExtendedTimeSeries> eventMap,
			Map<String, long[]> _timingStatsMap, Map<String, Map<Date, FileLine>> fileLineMap, Date startDate2, Date endDate2) {
		startDate = startDate2;
		endDate = endDate2;
		fileDates = _fileDates;
		statGroupTimeSeries = statMap;
		eventGroupTimeSeries = eventMap;
		fileLineDateMap=fileLineMap;
		matchingStats = _timingStatsMap;
		if (logger.isDebugEnabled()) {
			logger.debug("start date: " + startDate2 + " end date: " + endDate2);
		}

	}
}
