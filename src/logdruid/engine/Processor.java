package logdruid.engine;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Map.Entry;

import logdruid.data.record.Recording;
import logdruid.data.record.StatRecording;

public class Processor {

	public Processor() {
		// TODO Auto-generated constructor stub
	}

}


Iterator<Entry<Recording, String>> recMatchIte = recMatch.entrySet().iterator();
while (recMatchIte.hasNext()) {
	if (timings) {
		recordingMatchStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}
	Entry<Recording, String> me = recMatchIte.next();
	Recording rec = (Recording) me.getKey();
	matcher = patternCache.getMatcher((String) (rec.getRegexp()),rec.isCaseSensitive(),line);
	if (matcher.find()) {
		Boolean isStatRecording = classCache.getClass(rec).equals(StatRecording.class);
		if (stats) {
			if (isStatRecording) {
				statMatch++;
			} else {
				eventMatch++;
			}