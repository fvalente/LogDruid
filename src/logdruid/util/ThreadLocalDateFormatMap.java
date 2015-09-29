package logdruid.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ThreadLocalDateFormatMap {

	
	protected static ThreadLocalDateFormatMap INSTANCE = new ThreadLocalDateFormatMap();

	public static ThreadLocalDateFormatMap getInstance() {
	return INSTANCE;
	}

	protected ThreadLocal<Map> localDateFormatMap = new ThreadLocal<Map>() {
		
	protected Map initialValue() {
	return new HashMap();
	}

	};
	protected DateFormat createSimpleDateFormat(String pattern) {
	DateFormat result = new SimpleDateFormat(pattern);
	putDateFormat(pattern, result);
	return result;
	}

	public DateFormat putDateFormat(String pattern, DateFormat format) {
	return (DateFormat) localDateFormatMap.get().put(pattern, format);
	}

	public DateFormat getDateFormat(String pattern) {
	DateFormat format = (DateFormat) localDateFormatMap.get().get(pattern);
	if (format == null) {
	format = createSimpleDateFormat(pattern);
	}
	return format;
	}
}
