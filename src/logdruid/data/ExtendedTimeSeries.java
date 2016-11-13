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

import logdruid.data.record.RecordingItem;

import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;

public class ExtendedTimeSeries {

	TimeSeries timeSeries;
	RecordingItem recordingItem;
	int[] stat={0,0};

	public ExtendedTimeSeries(TimeSeries ts, int[] stats) {
		stat = stats;
		timeSeries = ts;
		// TODO Auto-generated constructor stub
	}

	public ExtendedTimeSeries(RecordingItem recItem2, Class<FixedMillisecond> class1) {
		timeSeries = new TimeSeries(recItem2.getName(), class1);
		recordingItem=recItem2;
		// TODO Auto-generated constructor stub
	}

	public TimeSeries getTimeSeries() {
		return timeSeries;
	}

	public void setTimeSeries(TimeSeries timeSeries) {
		this.timeSeries = timeSeries;
	}

	public int[] getStat() {
		return stat;
	}

	public void setStat(int[] stat) {
		this.stat = stat;
	}
	
	public RecordingItem getRecordingItem() {
		return recordingItem;
	}

}
