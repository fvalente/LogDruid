package logdruid.engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.mine.FileRecord;
import logdruid.data.record.Recording;
import logdruid.util.PatternCache;


public class Reader {
	private static Logger logger = Logger.getLogger(Reader.class.getName());
	BlockingQueue queue = new ArrayBlockingQueue(1024);
	FileReader flstr = null;
	BufferedReader buf1st;
	public Reader() {
		// TODO Auto-generated constructor stub
	}
	
	void processFile (ArrayList<FileRecord> files, Source src,Map<Recording, String> recMatch1, Repository repo){
		Matcher matcher;
		PatternCache patternCache=new PatternCache();
		Iterator iterator = files.iterator();
		while (iterator.hasNext()){
			FileRecord fileRecord=(FileRecord) iterator.next();
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("++file: " + repo.getBaseSourcePath() + " + " + (String) fileRecord.getCompletePath().toString());
					}
					flstr = new FileReader(fileRecord.getCompletePath());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buf1st = new BufferedReader(flstr);
				String line;
				try {
					//recMatch = getRegexp(repo, source);
					int nbLines=0;
					String lastDate= null;
					while (nbLines<20000){
						String[][]dataBlock=new String[20000][2];
					while ((line = buf1st.readLine()) != null) {
						matcher = patternCache.getPattern((String) (src.getDateFormat().getPattern()),false).matcher(line);
						if (matcher.matches()){
							dataBlock[nbLines][0]=matcher.group(0);
							lastDate=matcher.group(0);
							logger.info(dataBlock[nbLines][0]);
							dataBlock[nbLines][1]=line;
							logger.info(dataBlock[nbLines][1]);
						} else {
							dataBlock[nbLines][0]=lastDate;
							logger.info(dataBlock[nbLines][0]);
							dataBlock[nbLines][1]=line;
							logger.info(dataBlock[nbLines][1]);							
						}
						Iterator<Entry<Recording, String>> recMatchIte = recMatchStruct.entrySet().iterator();
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
						nbLines++;
					}
					}		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					buf1st.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					int lineCount = 1;
				}
			}
				}}	
		
		
	}


