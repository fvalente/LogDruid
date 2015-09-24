package logdruid.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import logdruid.data.record.Recording;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DataVault {
	static public MineResultSet mineResultSet;
	private static Logger logger = Logger.getLogger(DataVault.class.getName());
	static private  Map<Recording,ArrayList<String>> recordingMatchedLines = new HashMap<Recording,ArrayList<String>>();
	static private  Map<Recording,ArrayList<String>> recordingUnmatchedLines = new HashMap<Recording,ArrayList<String>>();
	public synchronized static MineResultSet getMineResultSet() {
		return mineResultSet;
	}

	public synchronized static void setMineResultSet(MineResultSet mineResultSet) {
		DataVault.mineResultSet = mineResultSet;
	}

	public DataVault() {
		// TODO Auto-generated constructor stub
	}

	public static String getMatchedLines(Recording rec){
		ArrayList aL=recordingMatchedLines.get(rec);
		String returned="";
		if (aL!=null){
		Iterator ite= aL.iterator();
		
		while (ite.hasNext()){
			returned+=ite.next()+System.getProperty("line.separator");
		}}
		return returned;
	}
	
	public static void addMatchedLines(Recording rec,String line){
		ArrayList<String> al=null;
		if (recordingMatchedLines.get(rec)==null)
				{
			al=new ArrayList<String>();
			recordingMatchedLines.put(rec,al);
				}
		if (recordingMatchedLines.get(rec)!=null ){
			if (recordingMatchedLines.get(rec).size()<5){
				recordingMatchedLines.get(rec).add(line);	
			}}
	//	recordingMatchedLines.put(rec,al);
	}
	
	public static String getUnmatchedLines(Recording rec){
		ArrayList aL=recordingUnmatchedLines.get(rec);

		String returned="";
		if (aL!=null){
			Iterator ite= aL.iterator();
			while (ite.hasNext()){
			returned+=ite.next()+System.getProperty("line.separator");;
		}}
		return returned;
	}
	public static void addUnmatchedLines(Recording rec,String line){
		ArrayList<String> al=null;
		if (recordingUnmatchedLines.get(rec)==null)
				{
			al=new ArrayList<String>();
			recordingUnmatchedLines.put(rec,al);
				}
		if (recordingUnmatchedLines.get(rec)!=null ){
			if (recordingUnmatchedLines.get(rec).size()<5){
				recordingUnmatchedLines.get(rec).add(line);	
			}
			
	//	recordingMatchedLines.put(rec,al);
		}
	}
	
	public static long[] getRecordingStats(String recording) {
		if (mineResultSet == null)
			return null;
		Iterator it = mineResultSet.mineResults.values().iterator();
		long[] tmpArray;
		long[] stats = { 0, 0, 0, 0 };// == new HashMap<String, long[]> ();
		while (it.hasNext()) {
			Map<String, MineResult> map = (Map<String, MineResult>) it.next();
			// logger.info("keys: "+map.keySet());
			// logger.info("Values: "+map.values());
			Iterator it2 = map.values().iterator();
			while (it2.hasNext()) {
				MineResult mr = (MineResult) it2.next();
				Map<String, long[]> tempStatMap = mr.getMatchingStats();
				if (tempStatMap.containsKey(recording)) {
					tmpArray = tempStatMap.get(recording);
					// logger.info(file.getName() + " contains " + arrayBefore);
					// 0-> sum of time for success matching of given
					// recording ; 1-> sum of time for failed
					// matching ; 2-> count of match attempts,
					// 3->count of success attempts
					stats[0] += tmpArray[0];
					stats[1] += tmpArray[1];
					stats[2] += tmpArray[2];
					stats[3] += tmpArray[3];
					// logger.info(file.getName() + " add success to" +
					// rec.getName() + " 0: "+ array[0] + " 1: "+ array[1]+
					// " 2: "+ array[2] +" 3: "+ array[3]);
				}
			}
		}
		return stats;

	}
}
