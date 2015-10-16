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
package logdruid.data.mine;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class FileRecord {
	private static Logger logger = Logger.getLogger(FileRecord.class.getName());
int id;
File file;

	public FileRecord() {
		// TODO Auto-generated constructor stub
	}

	public FileRecord(int _id, File _file) {
		id=_id;
		file=_file;
	}

	public int getId() {
		return id;
	}

	public String getCompletePath() {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "OUCH";
		}
	}
	
	public String toString(){
		try {
			return file.getCanonicalPath()+" "+id;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
				}
	public File getFile() {
	return file;
}
	
}
