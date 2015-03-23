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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class MineResultSet {
	private static Logger logger = Logger.getLogger(MineResultSet.class.getName());
	public Map<Source, Map<String, MineResult>> mineResults;
	private Date startDate;
	private Date endDate;

	public synchronized Date getStartDate() {
		logger.info("start date: " + startDate);
		return startDate;
	}

	public synchronized void updateStartDate(Date startDate) {
		if (this.startDate != null) {
			if (startDate != null) {
				if (startDate.before(this.startDate)) {
					this.startDate = startDate;
				}
			}
		} else {
			this.startDate = startDate;
		}
	}

	public synchronized Date getEndDate() {
		logger.info("end date: " + endDate);
		return endDate;
	}

	public synchronized void updateEndDate(Date endDate) {
		if (this.endDate != null) {
			if (endDate != null) {
				if (endDate.after(this.endDate)) {
					this.endDate = endDate;
				}
			}
		} else {
			this.endDate = endDate;
		}
	}

	public MineResultSet() {
		mineResults = new HashMap<Source, Map<String, MineResult>>();
	}

}
