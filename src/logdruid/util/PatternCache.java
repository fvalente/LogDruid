/*******************************************************************************
 * LogDruid : Generate charts and reports using data gathered in log files
 * Copyright (C) 2016 Frederic Valente (frederic.valente@gmail.com)
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.log4j.Logger;

public class PatternCache {
//	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private Map<String, Pattern> pattern = new HashMap<String, Pattern>();
	private Map<String, Matcher> matcher = new HashMap<String, Matcher>();

	public Pattern getPattern(String regexp, boolean caseSensitive) {
		Pattern temp = pattern.get(regexp+Boolean.toString(caseSensitive));
		if (temp!=null){
			return temp;
		} else{
			if (caseSensitive){
				pattern.put(regexp+caseSensitive, Pattern.compile(regexp));
			}else{
				pattern.put(regexp+caseSensitive, Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
			}			
		}
		return pattern.get(regexp+caseSensitive);
	}
	
	public Matcher getMatcher(String regexp, boolean caseSensitive,String str) {
		Matcher tempMatcher=matcher.get(regexp+caseSensitive);
		if (tempMatcher!=null){
			return tempMatcher.reset(str);
		} else{
			if (caseSensitive){
				matcher.put(regexp+caseSensitive, getPattern(regexp,caseSensitive).matcher(str));
			}else{
				matcher.put(regexp+caseSensitive, getPattern(regexp,caseSensitive).matcher(str));	
			}
		}
		return matcher.get(regexp+caseSensitive);
	}
	
	public Matcher getNewMatcher(String regexp, boolean caseSensitive,String str) {
		return getPattern(regexp,caseSensitive).matcher(str);
	}
	
}