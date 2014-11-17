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

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class FileMineResult {
	// HashMap<Recording, Boolean> activeRecordingOnSourceCache = new
	// HashMap<Recording, Boolean>();
	private static Logger logger = Logger.getLogger(FileMineResult.class.getName());
	public HashMap<String, TimeSeries> statGroupTimeSeries;
	public HashMap<String, TimeSeries> eventGroupTimeSeries;
	private Date startDate;
	private Date endDate;

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public FileMineResult(HashMap<String, TimeSeries> _statGroupTimeSeries, HashMap<String, TimeSeries> _eventGroupTimeSeries, Date startDate2, Date endDate2) {
		startDate = startDate2;
		endDate = endDate2;
		statGroupTimeSeries = _statGroupTimeSeries;
		eventGroupTimeSeries = _eventGroupTimeSeries;
		if (logger.isDebugEnabled()) {
			logger.debug("start date: " + startDate2 + " end date: " + endDate2);
		}

	}
}
