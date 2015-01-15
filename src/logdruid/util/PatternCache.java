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

import logdruid.data.ExtendedTimeSeries;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class PatternCache {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private static Map<String, Pattern> pattern = new HashMap<String, Pattern>();

	synchronized public static Pattern getPattern(String regexp) {
		if (!pattern.containsKey(regexp)) {
			pattern.put(regexp, Pattern.compile(regexp));
		}
		return pattern.get(regexp);
	}

	synchronized public static int getSize() {
		logger.info("pattern cache content" + pattern);
		return pattern.size();

	}

}