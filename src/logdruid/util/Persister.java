package logdruid.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class Persister {

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
			System.err.println("Error in XML Write: " + e.getMessage());
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
			System.err.println("Error in XML Write: " + e.getMessage());
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
