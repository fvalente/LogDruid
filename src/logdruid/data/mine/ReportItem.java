package logdruid.data.mine;

import java.util.regex.Matcher;

import logdruid.data.Source;
import logdruid.data.record.Recording;
import logdruid.data.record.ReportRecording;

public class ReportItem {
	String line;
	ReportRecording recording;
	Matcher matcher;
	Source source;

	public ReportItem(Source _source, String _line, Matcher _matcher, ReportRecording _recording) {
		// TODO Auto-generated constructor stub
		line = _line;
		recording = _recording;
		matcher = _matcher;
		source = _source;
	}

	public String getLine() {
		return line;
	}

	public ReportRecording getRecording() {
		return recording;
	}

	public Matcher getMatcher() {
		return matcher;
	}

	public Source getSource() {
		return source;
	}

}
