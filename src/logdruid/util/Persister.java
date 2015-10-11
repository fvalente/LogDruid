/*******************************************************************************
 * LogDruid : chart statistics and events retrieved in logs files through configurable regular expressions
 * Copyright (C) 2014, 2015 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package logdruid.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class Persister {
	private static Logger logger = Logger.getLogger(Persister.class.getName());
	static XStream xstream = new XStream(new StaxDriver());

	public static void save(File file, Object object) {

		FileOutputStream fos = null;
		try {
			String xml = xstream.toXML(object);
			fos = new FileOutputStream(file);
			// fos.write("<?xml version=\"1.0\"?>".getBytes("UTF-8"));
			byte[] bytes = xml.getBytes("UTF-8");
			fos.write(bytes);

		} catch (Exception e) {
			logger.info("Error in XML Write: " + e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Object open(File file) {
		XStream xstream = new XStream(new StaxDriver());
		FileOutputStream fos = null;
		Object returnedObject = null;
		try {
			returnedObject = (Object) xstream.fromXML(file);
			/*
			 * fos = new FileOutputStream(file);
			 * 
			 * byte[] bytes = xml.getBytes("UTF-8"); fos.write(bytes);
			 */

		} catch (Exception e) {
			logger.info("No existing preference file: " + e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return returnedObject;
	}

	public Persister() {

	}

}
