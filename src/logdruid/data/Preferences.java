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

import java.io.File;
import java.util.HashMap;

import logdruid.util.Persister;

import org.apache.log4j.Logger;

public class Preferences {
	private static Logger logger = Logger.getLogger(Preferences.class.getName());

	static PreferenceData prefData;
	
	public Preferences() {
		logger.info("Preferences()");
		//PreferenceData prefData= (PreferenceData) Persister.open(new File("Preference.xml"));
		//getPreferences();
	}
	public static void load() {
		prefData= (PreferenceData) Persister.open(new File("Preference.xml"));
		if (prefData ==null || prefData.preferences==null) {
			prefData= new PreferenceData();
			logger.debug(prefData+";"+prefData.preferences);
			Persister.save(new File("Preference.xml"), (PreferenceData) prefData);
			logger.info("Created Preference.xml");
		}
		getPreferences();
	}
	
	public static HashMap<String, String> getPreferences() {
		if (prefData.preferences == null) {
			prefData.preferences = new HashMap<String, String>();
			logger.info("new prefData.preferences");
		}
		if (!prefData.preferences.containsKey("timings")) {
			prefData.preferences.put("timings", "false");
		}
		if (!prefData.preferences.containsKey("matches")) {
			prefData.preferences.put("matches", "true");
		}
		if (!prefData.preferences.containsKey("stats")) {
			logger.info("stats set to true");
			prefData.preferences.put("stats", "true");
		}
		if (!prefData.preferences.containsKey("chartSize")) {
			prefData.preferences.put("chartSize", "350");
		}
		if (!prefData.preferences.containsKey("lastPath")) {
			prefData.preferences.put("lastPath", ".");
		}
		if (!prefData.preferences.containsKey("ThreadPool_Group")) {
			prefData.preferences.put("ThreadPool_Group", "4");
		}
		if (!prefData.preferences.containsKey("ThreadPool_File")) {
			prefData.preferences.put("ThreadPool_File", "8");
		}
		if (!prefData.preferences.containsKey("editorCommand")) {
			prefData.preferences.put("editorCommand", "gvim -R +$line \"$file\"");
		}
		if (!prefData.preferences.containsKey("gatherstats")) {
			prefData.preferences.put("gatherstats", "true");
		}
		if (!prefData.preferences.containsKey("gatherevents")) {
			prefData.preferences.put("gatherevents", "true");
		}
		if (!prefData.preferences.containsKey("gatherreports")) {
			prefData.preferences.put("gatherreports", "true");
		}
		if (!prefData.preferences.containsKey("eventsAsDots")) {
			prefData.preferences.put("eventsAsDots", "true");
		}
		if (!prefData.preferences.containsKey("MiningFileChunk")) {
			prefData.preferences.put("MiningFileChunk", "10000");
		}		
		if (!prefData.preferences.containsKey("ForceSourceDateFormat")) {
			prefData.preferences.put("ForceSourceDateFormat", "false");
		}				
		
		
		
		return prefData.preferences;
	}
	
	public static void setPreference(String key, String value) {
		prefData.preferences.put(key, value);
		logger.debug(prefData.preferences);
		Persister.save(new File("Preference.xml"), (PreferenceData) prefData);
	}

	public static String getPreference(String key) {
		return getPreferences().get(key);
	}
	
	public static boolean isStats() {
		return Boolean.parseBoolean(getPreference("stats"));
	}

	public void setStats(boolean stats) {
		setPreference("stats", Boolean.toString(stats));
	}

	public static boolean isTimings() {
		return Boolean.parseBoolean(getPreference("timings"));
	}

	public void setTimings(boolean timings) {
		setPreference("timings", Boolean.toString(timings));
	}
	public static boolean isMatches() {
		return Boolean.parseBoolean(getPreference("matches"));
	}
	
	public void setMatches(boolean matches) {
		setPreference("matches", Boolean.toString(matches));
	}
	
	public static boolean getBooleanPreference(String key){
		// TBD : implement a cache 
		return Boolean.parseBoolean(getPreferences().get(key));
	}

	public static void persist() {
		logger.debug(prefData);
		Persister.save(new File("Preference.xml"), (PreferenceData) prefData);
		// TODO Auto-generated method stub
		
	}

}
