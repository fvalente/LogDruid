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
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import logdruid.util.DataMiner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class MineResult implements Comparable{
	private static Logger logger = Logger.getLogger(MineResult.class.getName());
	private Date startDate;
	private Date endDate;
	String source; // Source source; (tuning data size)
	Vector<File> logFiles;
	String group;
	HashMap<String, TimeSeries> statTimeSeriesHashMap;
	HashMap<String, TimeSeries> eventTimeSeriesHashMap;
	private ArrayList<Object[]> fileDates= new ArrayList<Object[]>();


	public MineResult(String _group, FileMineResultSet hm, Vector fileVector, Repository repo, Source _source) {
		logFiles = fileVector;
		source = _source.getSourceName();
		statTimeSeriesHashMap = hm.statGroupTimeSeries;
		eventTimeSeriesHashMap = hm.eventGroupTimeSeries;
		startDate = hm.getStartDate();
		endDate = hm.getEndDate();
		fileDates=hm.getFileDates();
		group = _group;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getSourceID() {
		return source;
	}

	public Vector<File> getLogFiles() {
		return logFiles;
	}

	public String getGroup() {
		return group;
	}

	public HashMap<String, TimeSeries> getStatTimeseriesHashMap() {
		return statTimeSeriesHashMap;
	}

	public HashMap<String, TimeSeries> getEventTimeseriesHashMap() {
		return eventTimeSeriesHashMap;
	}

	public Date getEndDate() {
		return endDate;
		
	}
	public File getFileForDate(Date date){
		Iterator it=fileDates.iterator();
		while (it.hasNext())
		{
			 Object[] obj=( Object[] )it.next();
			 if (logger.isDebugEnabled()) logger.debug("1: "+(Date)obj[0]+"2: "+(Date)obj[1]+"3: "+(File)obj[2]);
			if (date.after((Date)obj[0]) && date.before((Date)obj[1]))
				return (File)obj[2];
		}
		return null;
	}

	@Override
	public int compareTo(Object o) {
		String local = source+group;
		String remote=((MineResult)o).getSourceID()+((MineResult)o).getGroup();
		return local.compareTo(remote);
	}
}