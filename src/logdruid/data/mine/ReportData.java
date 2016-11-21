package logdruid.data.mine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

import logdruid.data.Source;
import logdruid.data.record.Recording;

public class ReportData {
	public static final Map<Source, Map<Recording, Map<List<Object>, Long>>> occurenceReport = new HashMap<Source, Map<Recording, Map<List<Object>, Long>>>();
	public static final Map<Source, Map<Recording, Map<List<Object>, Double>>> sumReport = new HashMap<Source, Map<Recording, Map<List<Object>, Double>>>();
	public static final Map<Source, Map<Recording, SortedMap<Double, List<Object>>>> top100Report = new HashMap<Source, Map<Recording, SortedMap<Double, List<Object>>>>();
	
	public ReportData() {
		// TODO Auto-generated constructor stub
	}
	public void clear(){
		occurenceReport.clear();
		sumReport.clear();
		top100Report.clear();
	}
}
