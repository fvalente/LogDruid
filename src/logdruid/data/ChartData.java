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
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class ChartData {
	// String name = "test";
	public ArrayList<Source> sourceArrayList;
	public Map<Source, ArrayList<String>> sourceFileArrayListMap = new HashMap<Source, ArrayList<String>>();
	public ArrayList<String> selectedSourceFiles;

	// ArrayList<Map<String, TimeSeries>> MapArrayList = new
	// ArrayList<Map<String, TimeSeries>>();
	public TimeSeriesCollection dataset = new TimeSeriesCollection();

	Map<Source, Map<String, ArrayList<String>>> MapSourceGroupFilesArrayList = new HashMap<Source, Map<String, ArrayList<String>>>();

	public void setGroupFilesArrayListMap(Source src, Map<String, ArrayList<String>> sourceFileGroup) {
		MapSourceGroupFilesArrayList.put(src, sourceFileGroup);
		// TODO Auto-generated method stub

	}

	public Map<String, ArrayList<String>> getGroupFilesMap(Source src) {
		return MapSourceGroupFilesArrayList.get(src);
	}
}
