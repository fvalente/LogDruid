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
package logdruid.data.record;

import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DataFormatException;

import logdruid.util.DataMiner;

import org.apache.log4j.Logger;

public abstract class Recording {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private String name;
	private String regexp;
	protected ArrayList recordingItem;
	private String FastDateFormat;
	private String exampleLine;
	private Boolean isActive;
	protected String id;
	private String dateFormatID;

	public String getId() {
		return id;
	}

	public String generate() {
		String generatedUniqueId = UUID.randomUUID().toString();
		logger.info("unique ID: " + generatedUniqueId);
		return generatedUniqueId;
	}

	public ArrayList getRecordingItem() {
		// logger.info("***********"+recordingItem.toString()+recordingItem.size());
		// Thread.currentThread().dumpStack();
		return recordingItem;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public String getExampleLine() {
		return exampleLine;
	}

	public void setExampleLine(String exampleLine) {
		this.exampleLine = exampleLine;
	}

	// public abstract Recording duplicate();
	@SuppressWarnings("rawtypes")
	public abstract String getType();

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	abstract public Recording duplicate();

	public String getDateFormatID() {
		return dateFormatID;
	}

	public void setDateFormatID(String dateFormatID) {
		this.dateFormatID = dateFormatID;
	}

}
