package logdruid.data;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class FileMineResult {
	// HashMap<Recording, Boolean> activeRecordingOnSourceCache = new
	// HashMap<Recording, Boolean>();
	private static Logger logger = Logger.getLogger(FileMineResult.class.getName());
	public HashMap<String, TimeSeries> statGroupTimeSeries;
	public HashMap<String, TimeSeries> eventGroupTimeSeries;
	private Date startDate;
	private Date endDate;

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public FileMineResult(HashMap<String, TimeSeries> _statGroupTimeSeries, HashMap<String, TimeSeries> _eventGroupTimeSeries, Date startDate2, Date endDate2) {
		startDate = startDate2;
		endDate = endDate2;
		statGroupTimeSeries = _statGroupTimeSeries;
		eventGroupTimeSeries = _eventGroupTimeSeries;
		if (logger.isDebugEnabled()) {
			logger.debug("start date: " + startDate2 + " end date: " + endDate2);
		}

	}
}
