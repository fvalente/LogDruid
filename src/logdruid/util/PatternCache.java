/*******************************************************************************
 * LogDruid : chart statistics and events retrieved in logs files through configurable regular expressions
 * Copyright (C) 2014 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package logdruid.util;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

//import org.apache.log4j.Logger;

public class PatternCache {
//	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private static Map<String, Pattern> pattern = new HashMap<String, Pattern>();

	public Pattern getPattern(String regexp, boolean caseSensitive) {
		if (!pattern.containsKey(regexp)) {
			if (caseSensitive){
				pattern.put(regexp, Pattern.compile(regexp));
			}else{
				pattern.put(regexp, Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));	
			}
		}
		return pattern.get(regexp);
	}

 public static int getSize() {
//		logger.info("pattern cache content" + pattern);
		return pattern.size();

	}

}