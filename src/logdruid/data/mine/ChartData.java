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
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import logdruid.data.Source;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class ChartData {
	public ArrayList<Source> sourceArrayList;
	public Map<Source, Map<Integer,FileRecord>> sourceFileArrayListMap = new HashMap<Source, Map<Integer,FileRecord>>();
	public Map<Integer,FileRecord> selectedSourceFiles;
	public TimeSeriesCollection dataset = new TimeSeriesCollection();
	public ArrayList<String> disabledSeries= new ArrayList<String>();
	Map<Source, Map<String, ArrayList<FileRecord>>> MapSourceGroupFilesArrayList = new HashMap<Source, Map<String, ArrayList<FileRecord>>>();

	public void setGroupFilesArrayListMap(Source src, Map<String, ArrayList<FileRecord>> sourceFileGroup) {
		MapSourceGroupFilesArrayList.put(src, sourceFileGroup);
		// TODO Auto-generated method stub
	}

	public Map<String, ArrayList<FileRecord>> getGroupFilesMap(Source src) {
		return MapSourceGroupFilesArrayList.get(src);
	}

	public void addDisabledSeries(String name) {
		disabledSeries.add(name);
		
	}

	public void removeDisabledSeries(String name) {
		disabledSeries.remove(name);
		
	}
	
	
}
