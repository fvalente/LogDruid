/*******************************************************************************
 * LogDruid : chart statistics and events retrieved in logs files through configurable regular expressions
 * Copyright (C) 2015 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package logdruid.data;

import java.util.HashMap;

public class PreferenceData {
	public HashMap<String, String> preferences;
	private boolean stats; // unused
	private boolean timings; // unused
	
	public PreferenceData() {
			preferences = new HashMap<String, String>();
			preferences.put("timings", "false");
			preferences.put("stats", "true");
			preferences.put("chartSize", "350");
			preferences.put("ThreadPool_Group", "4");
			preferences.put("ThreadPool_File", "8");
			preferences.put("editorCommand", "gvim -R +$line \"$file\"");
		
		// TODO Auto-generated constructor stub
	}

}
