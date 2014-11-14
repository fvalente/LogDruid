package logdruid.data;

import java.util.HashMap;
import java.util.Vector;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class ChartData {
	String name="test";
	public Vector<Source> sourceVector;
	public HashMap<Source, Vector<String>> sourceFileVectorHashMap = new HashMap<Source, Vector<String>>();
	public Vector<String> selectedSourceFiles;
	
//	Vector<HashMap<String, TimeSeries>> hashMapVector = new Vector<HashMap<String, TimeSeries>>();
	public TimeSeriesCollection dataset = new TimeSeriesCollection();
	
	HashMap<Source,HashMap<String, Vector<String>>> hashMapSourceGroupFilesVector=new HashMap<Source,HashMap<String, Vector<String>>>();
	
	public void setGroupFilesVectorHashMap(Source src, HashMap<String, Vector<String>> sourceFileGroup) {
		hashMapSourceGroupFilesVector.put(src, sourceFileGroup);
		// TODO Auto-generated method stub
		
	}
	
	public HashMap<String, Vector<String>> getGroupFilesHashMap(Source src) {
		return hashMapSourceGroupFilesVector.get(src);
	}
}
