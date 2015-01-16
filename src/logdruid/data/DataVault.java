package logdruid.data;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DataVault {
	static public MineResultSet mineResultSet;
	private static Logger logger = Logger.getLogger(DataVault.class.getName());

	public static MineResultSet getMineResultSet() {
		return mineResultSet;
	}

	public static void setMineResultSet(MineResultSet mineResultSet) {
		DataVault.mineResultSet = mineResultSet;
	}

	public DataVault() {
		// TODO Auto-generated constructor stub
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
