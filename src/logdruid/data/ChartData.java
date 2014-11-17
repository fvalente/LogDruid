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

import java.util.HashMap;
import java.util.Vector;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class ChartData {
	String name = "test";
	public Vector<Source> sourceVector;
	public HashMap<Source, Vector<String>> sourceFileVectorHashMap = new HashMap<Source, Vector<String>>();
	public Vector<String> selectedSourceFiles;

	// Vector<HashMap<String, TimeSeries>> hashMapVector = new
	// Vector<HashMap<String, TimeSeries>>();
	public TimeSeriesCollection dataset = new TimeSeriesCollection();

	HashMap<Source, HashMap<String, Vector<String>>> hashMapSourceGroupFilesVector = new HashMap<Source, HashMap<String, Vector<String>>>();

	public void setGroupFilesVectorHashMap(Source src, HashMap<String, Vector<String>> sourceFileGroup) {
		hashMapSourceGroupFilesVector.put(src, sourceFileGroup);
		// TODO Auto-generated method stub

	}

	public HashMap<String, Vector<String>> getGroupFilesHashMap(Source src) {
		return hashMapSourceGroupFilesVector.get(src);
	}
}
