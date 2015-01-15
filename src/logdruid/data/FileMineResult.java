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

import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class FileMineResult {
	private static Logger logger = Logger.getLogger(FileMineResult.class.getName());
	public Map<String, ExtendedTimeSeries> statGroupTimeSeries;
	public Map<String, ExtendedTimeSeries> eventGroupTimeSeries;
	public Map<String, long[]> matchingStats; // 0-> sum of time for success
												// matching of given recording ;
												// 1-> sum of time for failed
												// matching ; 2-> count of match
												// attempts, 3->count of success
												// attempts
	private Date startDate;
	private Date endDate;
	private File file;

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public FileMineResult(File file1, Map<String, ExtendedTimeSeries> _statGroupTimeSeries, Map<String, ExtendedTimeSeries> _eventGroupTimeSeries,
			Map<String, long[]> _matchingStats, Date startDate2, Date endDate2) {
		file = file1;
		startDate = startDate2;
		endDate = endDate2;
		statGroupTimeSeries = _statGroupTimeSeries;
		eventGroupTimeSeries = _eventGroupTimeSeries;
		matchingStats = _matchingStats;
		if (logger.isDebugEnabled()) {
			logger.debug("start date: " + startDate2 + " end date: " + endDate2);
		}

	}

	public File getFile() {
		return file;
	}
}
