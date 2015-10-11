/*******************************************************************************
 * LogDruid : chart statistics and events retrieved in logs files through configurable regular expressions
 * Copyright (C) 2015 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
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
