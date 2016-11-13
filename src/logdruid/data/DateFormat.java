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
package logdruid.data;

import java.lang.String;
import org.apache.commons.lang3.time.FastDateFormat;
import java.util.UUID;
import java.util.ArrayList;

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
		String generatedUniqueId = UUID.randomUUID().toString();
		logger.info("unique ID: " + generatedUniqueId);
		return generatedUniqueId;
	}

	public DateFormat(String _name, String _pattern, String _dateFormat) {
		name = _name;
		pattern = _pattern;
		dateFormat = _dateFormat;
		id = generate();
	}

	public String getId() {
		if (id == null) {
			id = generate();
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

	public DateFormat clone (){
		String _name=name.toString();
		String _pattern =pattern.toString();
		String _dateFormat= dateFormat.toString();
		
		return new DateFormat(_name, _pattern, _dateFormat);
	}
	
	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void update(String _name, String _pattern, String _dateFormat) {
		name = _name;
		pattern = _pattern;
		dateFormat = _dateFormat;
		if (id == null) {
			id = generate();
		}
	}
}
