package logdruid.data;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import logdruid.util.DataMiner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class MineResult {
	private static Logger logger = Logger.getLogger(MineResult.class.getName());
	private Date startDate;
	private Date endDate;
	String source; //Source source; (tuning data size)
	Vector<File> logFiles;
	String group;
	HashMap<String,TimeSeries> statTimeSeriesHashMap;
	HashMap<String,TimeSeries> eventTimeSeriesHashMap;



public MineResult(String _group,FileMineResult hm,Vector fileVector, Repository repo, Source _source){
	logFiles=fileVector;
	source=_source.getSourceName();
	statTimeSeriesHashMap=hm.statGroupTimeSeries;
	eventTimeSeriesHashMap=hm.eventGroupTimeSeries;
	startDate=hm.getStartDate();
	endDate=hm.getEndDate();
	group=_group;
}



public Date getStartDate() {
	return startDate;
}



public String getSourceID() {
	return source;
}



public Vector<File> getLogFiles() {
	return logFiles;
}



public String getGroup() {
	return group;
}



public HashMap<String, TimeSeries> getStatTimeseriesHashMap() {
	return statTimeSeriesHashMap;
}
public HashMap<String, TimeSeries> getEventTimeseriesHashMap() {
	return eventTimeSeriesHashMap;
}


public Date getEndDate() {
	return endDate;
}
	



}