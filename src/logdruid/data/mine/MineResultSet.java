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
package logdruid.data.mine;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

import logdruid.data.Source;
import logdruid.data.record.Recording;

import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class MineResultSet {
	private static Logger logger = Logger.getLogger(MineResultSet.class.getName());
	public Map<Source, Map<String, MineResult>> mineResults;
	private Date startDate;
	private Date endDate;
	Map<Source, Map<Recording, Map<List<Object>, Long>>> occurenceReport ;
	Map<Source, Map<Recording, Map<List<Object>, Double>>> sumReport ;
	Map<Source, Map<Recording, SortedMap<Double,List<Object>>>> top100Report;
	
	public  Map<Source, Map<Recording, Map<List<Object>, Double>>> getSumReport() {
		return sumReport;
	}

	
	public  Map<Source, Map<Recording, SortedMap<Double, List<Object>>>> getTop100Report() {
		return top100Report;
	}

	public  Map<Source, Map<Recording, Map<List<Object>, Long>>> getOccurenceReport() {
		return occurenceReport;
	}

	public  void setOccurenceReport(Map<Source, Map<Recording, Map<List<Object>, Long>>> occurencereport2) {
		this.occurenceReport = occurencereport2;
	}

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

	public void setTop100Report(Map<Source, Map<Recording, SortedMap<Double, List<Object>>>> top100Report2) {
		this.top100Report=top100Report2;
		
	}

	public void setSumReport(Map<Source, Map<Recording, Map<List<Object>, Double>>> sumreport2) {
		this.sumReport=sumreport2;
		
	}

}
