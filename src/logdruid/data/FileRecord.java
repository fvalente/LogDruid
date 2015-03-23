package logdruid.data;

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
