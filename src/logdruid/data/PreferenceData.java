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
			preferences.put("editorCommand", "gvim -R +$line $file");
		
		// TODO Auto-generated constructor stub
	}

}
