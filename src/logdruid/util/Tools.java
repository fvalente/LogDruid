package logdruid.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import logdruid.data.DateFormat;
import logdruid.data.Preferences;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.mine.ChartData;
import logdruid.data.mine.FileRecord;
import logdruid.data.record.Recording;

public class Tools {
	private static Logger logger = Logger.getLogger(Tools.class.getName());
	public Tools() {
		// TODO Auto-generated constructor stub
	}

	public static boolean generateFlatFiles(Repository repo, File folderLocation){
		
	//	PatternCache patternCache = new PatternCache();
		FileReader flstr = null;
		BufferedReader buf1st = null;
		Map<Recording, String> recMatch = new HashMap<Recording, String>();
		Matcher matcher;
		Matcher matcher2;
		BufferedWriter bufWriter = null;
		if (repo.getBaseSourcePath() == null)
			return false;
		File folder = new File(repo.getBaseSourcePath());
		if (repo != null && repo.getBaseSourcePath() != null) {
			ChartData cd = DataMiner.gatherSourceData(repo,true);
			ArrayList sources = repo.getSources();
			Iterator sourceArrayListIte = sources.iterator();
			while (sourceArrayListIte.hasNext()) {
				cd.sourceArrayList = repo.getSources();
				Iterator<Source> sourceIterator = cd.sourceArrayList.iterator();
				Source src = (Source) sourceArrayListIte.next();
				Map<String, ArrayList<FileRecord>> hm = cd.getGroupFilesMap(src);
				logger.info("Source: "+ src.getSourceName());
				if (hm != null && hm.entrySet() != null) {
					Iterator it = hm.entrySet().iterator();
					while (it.hasNext()) {
						final Map.Entry pairs = (Map.Entry) it.next();
						// removing file separators so that files are stored inside one folder and to prevent no such directory error
						String noSeparatorString=pairs.getKey().toString().replace(File.separator,"-");
						File oneFile= new File(folderLocation.getAbsolutePath()+File.separator+noSeparatorString);
						FileWriter fileWriter = null;
						try {
							fileWriter = new FileWriter(oneFile);
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						bufWriter=new BufferedWriter(fileWriter);

						logger.info("Source group: " + pairs.getKey());
						ArrayList<FileRecord> grouFile = (ArrayList<FileRecord>) pairs.getValue();
						// return DataMiner.mine((String) pairs.getKey(),
						// (ArrayList<String>) pairs.getValue(), repo, source,
						// repo.isStats(), repo.isTimings());
						Iterator<FileRecord> fileArrayListIterator = grouFile.iterator();
						while (fileArrayListIterator.hasNext()) {
							final FileRecord fileName = fileArrayListIterator.next();
							try {
								flstr = new FileReader(new File(fileName.getCompletePath()));
							} catch (FileNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							buf1st = new BufferedReader(flstr);
							String line;
							logger.info("wrote file:" + fileName.getCompletePath());
							//try {
								try {
									while ((line = buf1st.readLine()) != null) {
										bufWriter.write(line);
										bufWriter.newLine();
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								}
					}
					try {
						buf1st.close();
						bufWriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}}}
		return true;
		
	}
	//ordering files by date of lines
	public static ArrayList<FileRecord> orderFiles(ArrayList<FileRecord> groupfiles, Source src){
		//	String test = Preferences.getPreference("ThreadPool_fileOrder");
			FileReader flstr = null;
			BufferedReader buf1st=null;
			Map<Date, FileRecord> tempDateFile= new HashMap<Date, FileRecord>();
			ArrayList<FileRecord> finaList= new ArrayList<FileRecord>();
			DateFormat df = src.getDateFormat();
			if (df!=null){
			Pattern srcDatePattern= Pattern.compile(df.getPattern()+".*");
			logger.debug(df.getPattern());
			java.text.DateFormat fastDateFormat =ThreadLocalDateFormatMap.getInstance().createSimpleDateFormat(df.getDateFormat());
			if (groupfiles!= null){
				if (df.getDateFormat()!=""){
			for (FileRecord fileRecord:groupfiles){
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("looking for first date in file: " + (String) fileRecord.getCompletePath().toString());
					}
					flstr = new FileReader(fileRecord.getCompletePath());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buf1st = new BufferedReader(flstr);
				String line;
				int lineCount=0;
				try {
					//recMatch = getRegexp(repo, source);
						//checking lines until a date is found
					while ((line = buf1st.readLine()) != null && lineCount <1000) {
						 Matcher matcher=srcDatePattern.matcher(line);
						if (matcher.find())
						{	//date found, now parsing to date
							try {
								Date date1 = fastDateFormat.parse(matcher.group(0));
								tempDateFile.put(date1, fileRecord);
								break;
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								logger.info(df.getPattern());
							}
						}
				}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally{
					try {
						buf1st.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}} else{
				return groupfiles;
			}
			}
				
	Iterator<Date> ite=tempDateFile.keySet().iterator();
	
	while (ite.hasNext())
	{
		Date date=(Date)ite.next();
		logger.debug("BEFORE ORDERING - date: "+ date+"File: "+ ((FileRecord)tempDateFile.get(date)).getCompletePath());
		
	}
	List<Date> test1=new ArrayList<Date>();
	
	//sorting list of dates 
	test1.addAll(tempDateFile.keySet());
	Collections.sort(test1, new Comparator<Date>() {
		  public int compare(Date o1, Date o2) {
		      return Long.compare(o1.getTime(),o2.getTime());
		  }
		});
	Iterator<Date> ite2=test1.iterator();
	
	while (ite2.hasNext())
	{
		Date date=(Date)ite2.next();
		finaList.add(tempDateFile.get(date));
		logger.debug("ORDERED - date: "+ date+"File: "+ ((FileRecord)tempDateFile.get(date)).getCompletePath());
		
	}
	
	return finaList;}
			else return groupfiles;
			
		}

}
