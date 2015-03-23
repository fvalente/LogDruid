package logdruid.data;

public class FileLine {
	int fileId;
	int lineNumber;

	public FileLine() {
		// TODO Auto-generated constructor stub
	}

	public FileLine(int _fileId, int _lineNumber) {
		lineNumber = _lineNumber;
		fileId = _fileId;
	}

	public int getFileId() {
		return fileId;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	public String toString(){
		return ""+fileId+":"+lineNumber;
	}
}
