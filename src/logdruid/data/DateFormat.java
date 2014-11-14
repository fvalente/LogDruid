package logdruid.data;
import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.Vector;

import logdruid.util.DataMiner;

import org.apache.log4j.Logger;


public class DateFormat {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	String name;
	String pattern;
	String dateFormat;
	String regExp;
	String id;
	
	public String generate() {
		String generatedUniqueId=UUID.randomUUID().toString();
		logger.info("unique ID: "+generatedUniqueId);
		return generatedUniqueId;
	}

	public DateFormat(String _name, String _pattern,String _dateFormat){
		name=_name;
		pattern=_pattern;
		dateFormat=_dateFormat;
		id=generate();
	}
	
	public String getId(){
		if (id==null){
			id=generate();
		}
		return id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public void update(String _name, String _pattern,String _dateFormat ){
		name=_name;
		pattern=_pattern;
		dateFormat=_dateFormat;
		if (id==null){
			id=generate();
		}
	}	
}
