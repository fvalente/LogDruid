package logdruid.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.data.time.TimeSeries;

public class MineResultSet {
private static Logger logger = Logger.getLogger(MineResultSet.class.getName());
public HashMap<String,Vector<MineResult>> mineResults;
private Date startDate;
private Date endDate;

public synchronized Date getStartDate() {
	logger.info("start date: "+ startDate);
	return startDate;
}

public synchronized void updateStartDate(Date startDate) {
	if (this.startDate!= null) {
		if (startDate!=null){
		if (startDate.before(this.startDate )){
		this.startDate = startDate;}}}
		else {
		this.startDate = startDate;
	}
}

public synchronized Date getEndDate() {
	logger.info("end date: "+ endDate);
	return endDate;
}

public synchronized void updateEndDate(Date endDate) {
	if (this.endDate!= null) {
		if (endDate!=null){
		if (endDate.after(this.endDate )){
		this.endDate = endDate;}}}
		else {
		this.endDate = endDate;
	}
}

public MineResultSet(){
	mineResults=new HashMap<String,Vector<MineResult>>();	
}

}
