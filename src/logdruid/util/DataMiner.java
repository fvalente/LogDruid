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
package logdruid.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import logdruid.data.ChartData;
import logdruid.data.DataVault;
import logdruid.data.DateFormat;
import logdruid.data.ExtendedTimeSeries;
import logdruid.data.FileLine;
import logdruid.data.FileMineResult;
import logdruid.data.FileMineResultSet;
import logdruid.data.FileRecord;
import logdruid.data.MineResult;
import logdruid.data.MineResultSet;
import logdruid.data.Preferences;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.record.EventRecording;
import logdruid.data.record.MetadataRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.data.record.ReportRecording;
import logdruid.data.record.StatRecording;
import logdruid.ui.MainFrame;

import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.FixedMillisecond;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DataMiner {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	static List<File> listOfFiles = null;
	private static ExecutorService ThreadPool_FileWorkers = null;
	private static ExecutorService ThreadPool_GroupWorkers = null;
	static long estimatedTime = 0;
	static long startTime = 0;
	static final Map<Source,Map<Recording,Map<List<Object>, Long>>> occurenceReport = new ConcurrentHashMap<Source,Map<Recording,Map<List<Object>, Long>>>(); ;

	public static MineResultSet gatherMineResultSet(final Repository repo, final MainFrame mainFrame) {
		String test = Preferences.getPreference("ThreadPool_Group");
		int ini = Integer.parseInt(test);
		logger.info("gatherMineResultSet parallelism: "+ini);
		ThreadPool_GroupWorkers = Executors.newFixedThreadPool(ini);
		ChartData cd = new ChartData();
		Collection<Callable<MineResult>> tasks = new ArrayList<Callable<MineResult>>();
		MineResultSet mineResultSet = new MineResultSet();
		occurenceReport.clear();
		//tOP100Report = new ConcurrentHashMap<Recording,Map<String, Long>>();
		
		startTime = System.currentTimeMillis();
		try{
		cd=gatherSourceData(repo);
		}catch (Exception e){
			return null;
		}
	//	if (logger.isEnabledFor(Level.INFO))
	//		logger.info("ArrayList sourceFileGroup" + sourceFileGroup);
		Iterator<Source> sourceIterator2 = repo.getSources().iterator();
		int progressCount = 0;
		while (sourceIterator2.hasNext()) {
			final Source source = sourceIterator2.next();
			// sourceFiles contains all the matched files for a given source
			if (source.getActive()) {
				Iterator<Entry<String, ArrayList<FileRecord>>> it = cd.getGroupFilesMap(source).entrySet().iterator();
				while (it.hasNext()) {
					final Map.Entry<String, ArrayList<FileRecord>> pairs = (Map.Entry<String, ArrayList<FileRecord>>) it.next();
					progressCount=progressCount+pairs.getValue().size();
					 logger.debug("Source:" + source.getSourceName()+", group: " +  pairs.getKey() + " = " + pairs.getValue().toString());
					tasks.add(new Callable<MineResult>() {
						public MineResult call() throws Exception {
							return DataMiner.mine((String) pairs.getKey(), (ArrayList<FileRecord>) pairs.getValue(), repo, source, Preferences.isStats(), Preferences.isTimings(),Preferences.isMatches(),mainFrame);
						}

					});

				}
			}
		}
		mainFrame.setMaxProgress(progressCount);
		//logger.info("progressCount "+ progressCount);
		/*
		 * invokeAll blocks until all service requests complete, or a max of
		 * 1000 seconds.
		 */
		List<Future<MineResult>> results = null;
		try {
			results = ThreadPool_GroupWorkers.invokeAll(tasks, 1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Future<MineResult> f : results) {
			MineResult mineRes = null;
			try {
				// if (mineRes!=null)
				mineRes = f.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mineRes != null) {
				mineResultSet.updateStartDate(mineRes.getStartDate());
				mineResultSet.updateEndDate(mineRes.getEndDate());
				if (!mineResultSet.mineResults.keySet().contains(mineRes.getSource())) {
					mineResultSet.mineResults.put(mineRes.getSource(), new HashMap<String, MineResult>());
				}
				mineResultSet.mineResults.get(mineRes.getSource()).put(mineRes.getSource().getSourceName() + mineRes.getGroup(), mineRes);
			}
		}
		estimatedTime = System.currentTimeMillis() - startTime;
		logger.info("gathering time: " + estimatedTime);
/*		Iterator oRIte= occurenceReport.keySet().iterator();
		while (oRIte.hasNext())
		{
			String occString=(String) oRIte.next();
			logger.info("nb: "+ occurenceReport.get(occString)+" string: " +occString);
		}
	*/	
		mineResultSet.setOccurenceReport(occurenceReport);
		//logger.info(occurenceReport);
		return mineResultSet;

	}

	public static MineResult mine(String group, ArrayList<FileRecord> arrayList, Repository repo, Source source, boolean stats, boolean timings,boolean matches, MainFrame mainFrame) {
		logger.debug("call to mine for source " + source.getSourceName() + " on group " + group);
		FileMineResultSet fMRS = fastMine(arrayList, repo, source, stats, timings,matches,mainFrame);
		return new MineResult(group, fMRS, arrayList, repo, source);
	}

	// handle gathering for ArrayList of file for one source
	public static FileMineResultSet fastMine(ArrayList<FileRecord> arrayList, final Repository repo, final Source source, final boolean stats,
			final boolean timings, final boolean matches, final MainFrame mainFrame) {

		ThreadPool_FileWorkers = Executors.newFixedThreadPool(Integer.parseInt(Preferences.getPreference("ThreadPool_File")));
		Date startDate = null;
		Date endDate = null;
		// Map<String, ExtendedTimeSeries> statMap = HashObjObjMaps<String,
		// ExtendedTimeSeries>();
		Map<String, ExtendedTimeSeries> statMap = new HashMap<String, ExtendedTimeSeries>();
		Map<String, ExtendedTimeSeries> eventMap = new HashMap<String, ExtendedTimeSeries>();
		Map<String, long[]> timingStatsMap = new HashMap<String, long[]>();
		Map<String, Map<Date,FileLine>> fileLine = new HashMap<String, Map<Date,FileLine>> () ;
		Collection<Callable<FileMineResult>> tasks = new ArrayList<Callable<FileMineResult>>();

		ArrayList<Object> mapArrayList;
		mapArrayList = new ArrayList<>();
		if (logger.isEnabledFor(Level.INFO))
			logger.info("mine called on " + source.getSourceName());
		Iterator<FileRecord> fileArrayListIterator = arrayList.iterator();
		while (fileArrayListIterator.hasNext()) {
			final FileRecord fileRec = fileArrayListIterator.next();
			tasks.add(new Callable<FileMineResult>() {
				public FileMineResult call() throws Exception {
					logger.debug("file mine on "+ fileRec);
					return fileMine(fileRec, repo, source, stats, timings,matches,mainFrame);
				}

			});

		}
		List<Future<FileMineResult>> results = null;
		try {
			results = ThreadPool_FileWorkers.invokeAll(tasks, 1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Future<FileMineResult> f : results) {
			FileMineResult fileMineRes = null;
			try {
				if (f != null) {
					fileMineRes = f.get();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			if (fileMineRes != null) {
				mapArrayList.add(fileMineRes);
			}

		}
		ArrayList<Object[]> fileDates = new ArrayList<Object[]>();
		Iterator<Object> mapArrayListIterator = mapArrayList.iterator();
		while (mapArrayListIterator.hasNext()) {
			FileMineResult fMR = (FileMineResult) mapArrayListIterator.next();

			if (startDate == null) {
				startDate = fMR.getStartDate();
			}
			if (endDate == null) {
				endDate = fMR.getEndDate();
			}
			if (fMR.getEndDate() != null && fMR.getStartDate() != null) {
				if (fMR.getEndDate().after(endDate)) {
					endDate = fMR.getEndDate();
				} else if (fMR.getStartDate().before(startDate)) {
					startDate = fMR.getStartDate();
				}
				if (logger.isDebugEnabled()) {
					logger.debug("1: " + fMR.getStartDate() + "2: " + fMR.getEndDate() + "3: " + fMR.getFile());
				}
				fileDates.add(new Object[] { fMR.getStartDate(), fMR.getEndDate(), fMR.getFile() });
			}

			Map<String, ExtendedTimeSeries> tempStatMap = fMR.statGroupTimeSeries;
			tempStatMap.entrySet();
			Iterator it = tempStatMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ExtendedTimeSeries> pairs = (Map.Entry<String, ExtendedTimeSeries>) it.next();
				if (!statMap.containsKey(pairs.getKey())) {
					statMap.put(pairs.getKey(), pairs.getValue());
				} else {
					ExtendedTimeSeries ts = statMap.get(pairs.getKey());
					if (stats) {
						int[] array = { pairs.getValue().getStat()[0] + ts.getStat()[0], pairs.getValue().getStat()[1] + ts.getStat()[1] };
						ts.setStat(array);
					}
					ts.getTimeSeries().addAndOrUpdate(pairs.getValue().getTimeSeries());
					statMap.put(pairs.getKey(), ts);
					// logger.info(pairs.getKey());
				}
			}

			Map tempEventMap = fMR.eventGroupTimeSeries;
			tempEventMap.entrySet();
			it = tempEventMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ExtendedTimeSeries> pairs = (Map.Entry<String, ExtendedTimeSeries>) it.next();
				if (!eventMap.containsKey(pairs.getKey())) {
					eventMap.put(pairs.getKey(), pairs.getValue());
				} else {
					ExtendedTimeSeries ts = eventMap.get(pairs.getKey());
					if (stats) {
						int[] array = { pairs.getValue().getStat()[0] + ts.getStat()[0], pairs.getValue().getStat()[1] + ts.getStat()[1] };
						ts.setStat(array);
					}
					ts.getTimeSeries().addAndOrUpdate(pairs.getValue().getTimeSeries());
					eventMap.put(pairs.getKey(), ts);
				}
			}

			it = fMR.matchingStats.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, long[]> pairs = (Map.Entry<String, long[]>) it.next();
				if (!timingStatsMap.containsKey(pairs.getKey())) {
					timingStatsMap.put(pairs.getKey(), pairs.getValue());
				} else {
					long[] array = timingStatsMap.get(pairs.getKey());
					// 0-> sum of time for success matching of given
					// recording ; 1-> sum of time for failed
					// matching ; 2-> count of match attempts,
					// 3->count of success attempts
					long[] array2 = { pairs.getValue()[0] + array[0], pairs.getValue()[1] + array[1], pairs.getValue()[2] + array[2],
							pairs.getValue()[3] + array[3] };
					timingStatsMap.put(pairs.getKey(), array2);
				}
			}
			it=fMR.fileLineDateMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String,Map<Date, FileLine>> pairs = (Map.Entry<String,Map<Date, FileLine>>) it.next();
				if (logger.isDebugEnabled()) {
					logger.debug("Entry<String,Map<Date, FileLine>> : "+pairs);
				}
				if (!fileLine.containsKey(pairs.getKey())) {
					fileLine.put(pairs.getKey(), pairs.getValue());
					if (logger.isDebugEnabled()) {
						logger.debug("fileLine.put "+pairs.getKey()+" -> "+ pairs.getValue());
					}
				} else {
					Map<Date, FileLine> ts = fileLine.get(pairs.getKey());
					Map<Date, FileLine> newDateFileLineEntries= pairs.getValue();
					Iterator it2=newDateFileLineEntries.entrySet().iterator();
					while (it2.hasNext()){
						Map.Entry<Date,FileLine> pairs2= (Map.Entry<Date,FileLine>)it2.next();
						fileLine.get(pairs.getKey()).put(pairs2.getKey(), pairs2.getValue());
						if (logger.isDebugEnabled()) {
							logger.debug("fileLine.put "+pairs2.getKey()+" -> "+ pairs2.getValue().getFileId()+":"+pairs2.getValue().getLineNumber());
						}
					}
					//logger.info("cont2: "+fileLine.get(pairs.getKey()));
				}
				
			}

		}
		return new FileMineResultSet(fileDates, statMap, eventMap, timingStatsMap,fileLine, startDate, endDate);
	}

	
	public static String readFileLine(Source src, FileLine fileLine, ChartData cd){
		FileReader flstr = null;
		String line = "";
		FileRecord fileRecord = cd.sourceFileArrayListMap.get(src).get(fileLine.getFileId());
		try {
			flstr = new FileReader( fileRecord.getCompletePath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader r = new BufferedReader(flstr);
		for (int i = 0; i < fileLine.getLineNumber(); i++)
		{
		   try {
			line=r.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return line;
		
	}
	// handle gathering for a single file
	public static FileMineResult fileMine(FileRecord fileRecord, Repository repo, Source source, boolean stats, boolean timings, boolean matches, final MainFrame mainFrame) {
		ExtendedTimeSeries ts = null;
		PatternCache patternCache = new PatternCache();
		Date startDate = null;
		Date endDate = null;

		DecimalFormat decimalFormat = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));

		FastDateFormat fastDateFormat = null;
		FileReader flstr = null;
		BufferedReader buf1st;
		Matcher matcher;
		Matcher matcher2;
		FixedMillisecond fMS = null;
		Boolean successMatch = false;
		DateFormat df = null;
		int statHit = 0;
		int statMatch = 0;
		int eventHit = 0;
		int eventMatch = 0;
		long[] arrayBefore;
		long match0=0;
		long match1=0;
		long timing0=0;
		long timing1=0;
		Map<Recording, String> recMatch = new HashMap<Recording, String>();
		Map<String, ExtendedTimeSeries> statMap = new HashMap<String, ExtendedTimeSeries>();
		Map<String, ExtendedTimeSeries> eventMap = new HashMap<String, ExtendedTimeSeries>();
		Map<String, Map<Date,FileLine>>  RIFileLineDateMap= new HashMap<String, Map<Date,FileLine>> ();
		Map<String, long[]> matchTimings = new HashMap<String, long[]>();

		long recordingMatchStart = 0;
		long recordingMatchEnd = 0;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("++file: " + repo.getBaseSourcePath() + " + " + (String) fileRecord.getCompletePath().toString());
			}
			flstr = new FileReader( fileRecord.getCompletePath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		buf1st = new BufferedReader(flstr);
		String line;
		try {
			recMatch = getRegexp(repo, source);
			int lineCount = 1;
			while ((line = buf1st.readLine()) != null) {
				// check against one Recording pattern at a tim
				// if (logger.isDebugEnabled()) {
				// logger.debug("line " + line);
				// }
				Iterator recMatchIte = recMatch.entrySet().iterator();
				while (recMatchIte.hasNext()) {
					if (timings) {
						recordingMatchStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
					}
					Map.Entry me = (Map.Entry) recMatchIte.next();
					Recording rec = (Recording) me.getKey();
					matcher = patternCache.getPattern((String) (rec.getRegexp())).matcher(line);
					if (matcher.find()) {
						Boolean isStatRecording=rec.getClass().equals(StatRecording.class);
						if (stats) {
							if (isStatRecording) {
								statMatch++;
							} else {
								eventMatch++;
							}
						}
						// logger.info("1**** matched: " + line);
						ArrayList<RecordingItem> recordingItem = ((Recording) rec).getRecordingItem();
						int cnt = 0;
						matcher2 = patternCache.getPattern((String) me.getValue()).matcher(line);
						successMatch = false;
						if (matcher2.find()) {
							if (stats) {
								if (isStatRecording) {
									statHit++;
								} else {
									eventHit++;
								}
							}
							if (!rec.getClass().equals(ReportRecording.class)){
							int count = 1;
							Date date1 = null;
							// handling capture for each recording item
							Iterator<RecordingItem> recItemIte2 = recordingItem.iterator();
							while (recItemIte2.hasNext()) {
								RecordingItem recItem2 = recItemIte2.next();
								// logger.info("3A**** " +
								// recItem2.getType());
								if (recItem2.getType().equals("date")) {
									try {
										df = repo.getDateFormat(rec.getDateFormatID());
										if (logger.isDebugEnabled())
											logger.debug("4**** rec name" + rec.getName() + " df: " + df.getId());
										fastDateFormat = FastDateFormat.getInstance(df.getDateFormat());
										date1 = fastDateFormat.parse(matcher2.group(count));
										if (logger.isDebugEnabled())
											logger.debug("4b**** " + df.getDateFormat() + " date: " + date1.toString());
										// logger.info("4**** " +
										// date1.toString());
									} catch (ParseException e) {
										// TODO Auto-generated catch
										// block
										e.printStackTrace();
									}
								} else if (date1 != null) {
									if (recItem2.isSelected()) {
										if (logger.isDebugEnabled()) {
											logger.debug("FileRecord: "+fileRecord.getFile().getName()+", Source: "+source.getSourceName()+", "+recItem2.getName()+", "+fileRecord.getFile().getName()+", "+lineCount);
										}
										// recording line of match in file in map RIFileLineDateMap - note the FileLine object use an int to identify the files to save memory 
										Map<Date, FileLine> dateFileLineMap=null;
										if (RIFileLineDateMap.containsKey(recItem2.getName())) {
											dateFileLineMap = RIFileLineDateMap.get(recItem2.getName());
										} else {
											dateFileLineMap = new HashMap<Date,FileLine>();
										}
										dateFileLineMap.put(date1,new FileLine(fileRecord.getId(), lineCount));
										if (logger.isDebugEnabled()) {
											logger.debug(fileRecord.getFile().getName()+" dateFileLineMap put: "+date1+"fileLine: "+ new FileLine(fileRecord.getId(), lineCount));
											logger.debug(fileRecord.getFile().getName()+" FileRecord: "+fileRecord.getFile().getName()+", RIFileLineDateMap.put: "+recItem2.getName()+", line: "+lineCount+ " RIFileLineDateMap size: "+RIFileLineDateMap.size() + " dateFileLineMap size: "+dateFileLineMap.size());
										}
										RIFileLineDateMap.put(recItem2.getName(),dateFileLineMap);
										

										if (startDate == null) {
											startDate = date1;
										}
										if (endDate == null) {
											endDate = date1;
										}
										if (date1.after(startDate)) {
											endDate = date1;
										} else if (date1.before(startDate)) {
											startDate = date1;
										}

										if (isStatRecording) {
											if (statMap.containsKey(recItem2.getName())) {
												ts = statMap.get(recItem2.getName());
											} else {
												ts = new ExtendedTimeSeries(recItem2.getName(), FixedMillisecond.class);
												if (logger.isDebugEnabled())
													logger.debug("5**** Adding record to Map: " + recItem2.getName());
											}
											fMS = new FixedMillisecond(date1);
											if (matcher2.group(count) == null) {
												logger.info("null in match on " + recItem2.getName() + " at " + fileRecord.getFile().getName() + " line cnt:" + lineCount);
												logger.info("line : " + line);
											}
											if (recItem2.getType().equals("long")) {
													ts.getTimeSeries().addOrUpdate((new TimeSeriesDataItem(fMS, Long.valueOf((String) matcher2.group(count)))));
												} else {
													  try{
															ts.getTimeSeries().addOrUpdate(
																	(new TimeSeriesDataItem(fMS, Double.parseDouble(String.valueOf(decimalFormat
																			.parse((String) matcher2.group(count).replace(',', '.')))))));
													    }catch(Exception e){
																e.printStackTrace();
													    }

												}

											if (stats) {
												//int[] array = { statMatch, statHit };
												int[] array =ts.getStat();
												array[1] = array[1]+1;
												array[0] =array[0]+1;
												ts.setStat(array);
												if (logger.isDebugEnabled())
													logger.debug("stats " + array[0] + " " + array[1]);
											}

											statMap.put(recItem2.getName(), ts);
											// performance: add the
											// TmeSeriesDataItem to the
											// TimeSeries instead of updating
											// the TimeSeries in the Map

										} else if (rec.getClass().equals(EventRecording.class)){ 
											if (eventMap.containsKey(recItem2.getName())) {
												ts = eventMap.get(recItem2.getName());
											} else {
												ts = new ExtendedTimeSeries(recItem2.getName(), FixedMillisecond.class);
												if (logger.isDebugEnabled())
													logger.debug("5**** Adding record to Map: " + recItem2.getName());
											}
											fMS = new FixedMillisecond(date1);
											
											if (((RecordingItem) recItem2).getProcessingType().equals("occurrences")) {
												TimeSeriesDataItem t = ts.getTimeSeries().getDataItem(fMS);
												if (t != null) {
													ts.getTimeSeries().addOrUpdate((new TimeSeriesDataItem(fMS, 101))); // +
													// (double)t.getValue()
													// need some way to show several occurrences
												} else {
													ts.getTimeSeries().add((new TimeSeriesDataItem(fMS, 100)));
												}
												
											}  else if (((RecordingItem) recItem2).getProcessingType().equals("sum")) {
												TimeSeriesDataItem t = ts.getTimeSeries().getDataItem(fMS);
												if (t != null) {
													if (!recItem2.getType().equals("date")) {
														try {
															ts.getTimeSeries().addOrUpdate(
																	(new TimeSeriesDataItem(fMS, Double.parseDouble(String.valueOf(decimalFormat.parse(matcher2
																			.group(count))) + ts.getTimeSeries().getDataItem(fMS).getValue()))));
															logger.info(ts.getTimeSeries().getDataItem(fMS).getValue());
														} catch (ParseException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														}
													}
												} else {
													try {
														ts.getTimeSeries().add(
																(new TimeSeriesDataItem(fMS, Double.parseDouble(String.valueOf(decimalFormat.parse(matcher2
																		.group(count)))))));
													} catch (ParseException e) {
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
													}
												}

											} else if (((RecordingItem) recItem2).getProcessingType().equals("capture")) {

											} else {
												if (!recItem2.getType().equals("date")) {
													try {
														ts.getTimeSeries().addOrUpdate(
																(new TimeSeriesDataItem(fMS, Double.parseDouble(String.valueOf(decimalFormat.parse(matcher2
																		.group(count)))))));
													} catch (ParseException e) {
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
													}
												}
												// ts.addOrUpdate((new
												// TimeSeriesDataItem(fMS,
												// 100)));
											}
											// logger.debug(recItem2.getName() +
											// " " +
											// Double.parseDouble((matcher2.group(count))));
											if (stats) {
												int[] array =ts.getStat();
												array[1] = array[1]+1;
												array[0] =array[0]+1;
												ts.setStat(array);
												if (logger.isDebugEnabled())
													logger.debug("stats " + array[0] + " " + array[1]);
											}
											eventMap.put(recItem2.getName(), ts);

										}
										
									}
								}	 //rec.getClass().equals(ReportRecording.class)
									
								count++;
								// logger.info("event statistics: "+eventMatch +
								// " and " +eventHit +
								// " ; stat statistics: "+statMatch + " and "
								// +statHit);
							}}
							else {
								int count=0;
							//	logger.info("here ");
								if (((ReportRecording)rec).getSubType().equals("histogram") && rec.getIsActive()){
									//logger.info("here2");
									List<Object> temp = new ArrayList<Object>();
									Iterator<RecordingItem> recItemIte2 = recordingItem.iterator();
									while (recItemIte2.hasNext()) {
										RecordingItem recItem2 = recItemIte2.next();
										if (recItem2.isSelected()) {
											//logger.info("here "+((RecordingItem) recItem2).getProcessingType());
											temp.add(matcher2.group(count+1));
									//		logger.info(matcher2.group(count+1));
											//if (((RecordingItem) recItem2).getProcessingType().equals("histogram")) {
										}
										count++;
									}
									 if (!occurenceReport.containsKey(source)){
										 occurenceReport.put(source,new ConcurrentHashMap<Recording,Map<List<Object>,Long>>());
									 }
									 if (!occurenceReport.get(source).containsKey(rec)){
												 occurenceReport.get(source).put(rec,new ConcurrentHashMap<List<Object>,Long>());
											 }
											 	if (!occurenceReport.get(source).get(rec).containsKey(temp)){
											 		occurenceReport.get(source).get(rec).put(temp, (long) 1);
											 		//if(temp[0]!=null)
											 		//logger.info("added "+matcher2.group(count) +" to"+temp[0].toString());
											 	} else {
											 		occurenceReport.get(source).get(rec).put(temp, occurenceReport.get(source).get(rec).get(temp)+1);
											 //		logger.info("current value: "+occurenceReport.get(source).get(rec).get(temp) +" to"+temp);
											 	}
								} else if(((ReportRecording)rec).getSubType().equals("top100")){
									
								}
								}
						}
						if (timings || matches) {
							if (matchTimings.containsKey(rec.getName())) {
								arrayBefore = matchTimings.get(rec.getName());
								// logger.info(file.getName() + " contains " +
								// arrayBefore);
								// 0-> sum of time for success matching of given
								// recording ; 1-> sum of time for failed
								// matching ; 2-> count of match attempts,
								// 3->count of success attempts
								if (timings){
									recordingMatchEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
									timing0 =arrayBefore[0] + recordingMatchEnd - recordingMatchStart;
									timing1 =arrayBefore[1];
								}
								if (matches){
									match0=arrayBefore[2] + 1;
									match1=arrayBefore[3] + 1; 
									}
								long[] array = {timing0, timing1, match0,match1};
								matchTimings.put(rec.getName(), array);
							} else {
								long[] array = { recordingMatchEnd - recordingMatchStart, 0, 1, 1 };
								matchTimings.put(rec.getName(), array);
							}
						}
					} else {
						if (timings || matches) {
							if (matchTimings.containsKey(rec.getName())) {
								arrayBefore = matchTimings.get(rec.getName());
								// logger.info(file.getName() + " contains " +
								// arrayBefore);
								// 0-> sum of time for success matching of given
								// recording ; 1-> sum of time for failed
								// matching ; 2-> count of match attempts,
								// 3->count of success attempts
								if (timings){
									recordingMatchEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
									timing0 =arrayBefore[0];
									timing1 =arrayBefore[1] + recordingMatchEnd - recordingMatchStart;
								}
								if (matches){
									match0=arrayBefore[2] + 1;
									match1=arrayBefore[3] ; 
									}
								long[] array = {timing0, timing1, match0,match1};
								matchTimings.put(rec.getName(), array);
							} else {
								long[] array = { 0, recordingMatchEnd - recordingMatchStart, 1, 0 };
								matchTimings.put(rec.getName(), array);
							}
						}
					}
				}
				lineCount++;

				// timing
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				buf1st.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*
		 * if (logger.isInfoEnabled()) { Iterator Ite =
		 * matchTimings.entrySet().iterator(); long successTotalTime=0; long
		 * failedTotalTime=0; // 0-> sum of time for success matching of given
		 * // recording ; 1-> sum of time for failed // matching ; 2-> count of
		 * match attempts, // 3->count of success attempts // long[] array;
		 * while (Ite.hasNext()) { Map.Entry pairs = (Map.Entry) Ite.next();
		 * long[] array = (long[]) pairs.getValue(); logger.info(file.getName()
		 * + " - "+ pairs.getKey() + " / success all time: " + array[0] +
		 * " failed all time: " + array[1] + " attempt count: " + array[2] +
		 * " success count: " + array[3] + " failed count:"
		 * +(array[2]-array[3])); successTotalTime=successTotalTime+array[0];
		 * failedTotalTime=failedTotalTime+array[1]; } logger.info("success: "
		 * +successTotalTime + " failed: " + failedTotalTime); Ite =
		 * matchTimings.entrySet().iterator(); while (Ite.hasNext()) { Map.Entry
		 * pairs = (Map.Entry) Ite.next(); long[] array = (long[])
		 * pairs.getValue(); logger.info(file.getName() + " percents - "+
		 * pairs.getKey() + " / % success time: " + (( successTotalTime!=0) ?
		 * ((double)((double)array[0] / successTotalTime)*100) : 0 ) +
		 * " % failed time: " + (( failedTotalTime!=0) ?((double)array[1]/
		 * failedTotalTime)*100 :0) + " attempt cost: " + ((array[2]!=0) ?
		 * ((double)successTotalTime + failedTotalTime ) /array[2]:0 )+
		 * " success cost: " + ((array[3]!=0) ? ((double)successTotalTime )
		 * /array[3] : 0) + " failed cost:" + ((array[2]-array[3]!=0) ?
		 * ((double)failedTotalTime/(array[2]-array[3])) : 0) ); } }
		 */
		mainFrame.progress();
		return new FileMineResult(fileRecord, statMap, eventMap, matchTimings, RIFileLineDateMap, startDate, endDate);
	}
	/*
	 * public Map<String,ArrayList> getSourceFileGroup(ArrayList<String>
	 * sourceFiles,Source src) { String patternString = "";
	 * ArrayList<SourceItem> sourceItemArrayList=src.getSourceItem(); Iterator
	 * it= sourceItemArrayList.iterator(); Map<String,ArrayList> Map=new
	 * Map<String,ArrayList>(); while (it.hasNext()){ it.next(); }}
	 * 
	 * returns Map with group id in key and a ArrayList of matching files in
	 * value.
	 * 
	 * @param repo
	 */
	public static Map<String, ArrayList<FileRecord>> getSourceFileGroup(Map<Integer,FileRecord> sourceFiles, Source src, Repository repo) {
		PatternCache patternCache = new PatternCache();
		String patternString = "";
		Map<String, ArrayList<FileRecord>> sourceFileGroup = new HashMap<String, ArrayList<FileRecord>>();
		ArrayList<FileRecord> groupedFiles = new ArrayList<FileRecord>();
		// ArrayList<SourceItem> sourceItemArrayList = src.getSourceItem();
		ArrayList<Recording> recordings = (ArrayList<Recording>) repo.getRecordings(MetadataRecording.class);
		Matcher matcher = null;
		if (recordings != null) {
			Iterator<Recording> it = recordings.iterator();
			// logger.info("recordings not null ");
			while (it.hasNext()) {
				Recording rec = it.next();
				if (src.isActiveRecordingOnSource(rec)) {
					ArrayList<RecordingItem> rIV = ((MetadataRecording) rec).getRecordingItem();
					Iterator<RecordingItem> itV = rIV.iterator();
					int nbRec = 0;
					while (itV.hasNext()) {
						RecordingItem rI = itV.next();
						String type = rI.getType();
						if (type == "date") {
							patternString += rI.getBefore() + "(" + repo.getDateFormat(rec.getDateFormatID()) + ")" + rI.getAfter();
						} else {
							patternString += rI.getBefore() + "(" + DataMiner.getTypeString(type) + ")" + rI.getAfter();
						}
						// logger.info("patternString: " + patternString
						// + " getType: " +
						// DataMiner.getTypeString(rI.getType()));
						nbRec++;
					}
					Iterator<FileRecord> sourceFileIterator = sourceFiles.values().iterator();
					String key = "";
					// tempV = new ArrayList<String>();
					while (sourceFileIterator.hasNext()) {
						groupedFiles.clear();

						FileRecord fileName = sourceFileIterator.next();
						// logger.info("file: "+fileName);
						try {

							if (logger.isDebugEnabled())
								logger.debug("patternString: " + patternString);
							if (logger.isDebugEnabled())
								logger.debug("filename: " + fileName);
							// Pattern pattern = Pattern.compile(patternString +
							// ".*");
							// Matcher matcher = pattern.matcher(fileName);
							matcher = patternCache.getPattern(patternString + ".*").matcher(new File(repo.getBaseSourcePath()).toURI().relativize(new File(fileName.getFile().getCanonicalPath()).toURI()).getPath());
//***
							if (matcher.find()) {
								if (logger.isDebugEnabled())
									logger.debug("found filename " + fileName + " with group");

								key = "";
								int i = 0;
								for (i = 0; i < matcher.groupCount(); i++) {
									if (recordings.get(i).getIsActive()) {
										if (logger.isDebugEnabled())
											logger.debug("one : " + matcher.group(i));
										key += matcher.group(i+1) + " ";
									}
								}
								if (logger.isDebugEnabled())
									logger.debug("i : " + i + " nbRec: " + nbRec);
								if (i  == nbRec) {
									if (logger.isDebugEnabled())
										logger.debug(" passed!");
									if (!sourceFileGroup.containsKey(key)) {
										ArrayList<FileRecord> v = new ArrayList<FileRecord>();
										v.add(fileName);
										sourceFileGroup.put(key, v);
										if (logger.isDebugEnabled())
											logger.debug(" to key: " + key + " added : " + fileName);
									} else {
										sourceFileGroup.get(key).add(fileName);
										if (logger.isDebugEnabled())
											logger.debug(" to key: " + key + " added : " + fileName);

									}

								}
								/*
								 * if (tempV != null) { sourceFileGroup.put(key,
								 * tempV); logger.info("Added file " + fileName
								 * + " to group " + key.toString());
								 * logger.info("files " + tempV);
								 * 
								 * }
								 */
							}

						} catch (Exception e1) {
							e1.printStackTrace();
							// System.exit(1);
						}

						// logger.info("found group " + key + "with " +
						// groupedFiles.size() + " files in source " +
						// src.getSourceName());

					}
				}
			}
		}
		// TODO Auto-generated method stub
		return sourceFileGroup;
	}

	// public static get
	public static String getTypeString(String type) {
		String typeString = "";
		switch (type) {
		case "integer":
			typeString = "\\d+";
			break;
		case "percent":
			typeString = "\\d+";
			break;
		case "word":
			typeString = "\\w+";
			break;
		case "string":
			typeString = ".*";
			break;
		case "double":
			typeString = "[-+]?[0-9]*.?[0-9]+(?:[eE][-+]?[0-9]+)?";
			break;
		case "long": // keeping for compatibility with older templates
			typeString = "[-+]?[0-9]*.?[0-9]+(?:[eE][-+]?[0-9]+)?";
			break;
		/*
		 * case "date": typeString = repo.getDateFormat(rec.getDateFormatID());
		 * break;
		 */
		default:
			typeString = ".*";
			break;
		}
		return typeString;
	}

	private static Map<Recording, String> getRegexp(Repository repo, Source source) {
		Map<Recording, String> recMatch = new HashMap<Recording, String>();
		Map<Recording, Boolean> activeRecordingOnSourceCache = new HashMap<Recording, Boolean>();
		ArrayList<Recording> recordings;
		recordings = repo.getRecordings(StatRecording.class);
		recordings.addAll(repo.getRecordings(EventRecording.class));
		recordings.addAll(repo.getRecordings(ReportRecording.class));
		StringBuffer sb = new StringBuffer(100);

		Iterator<Recording> recordingIterator = recordings.iterator();
		while (recordingIterator.hasNext()) {
			Recording rec = recordingIterator.next();
			if (!activeRecordingOnSourceCache.containsKey(rec)) {
				activeRecordingOnSourceCache.put(rec, source.isActiveRecordingOnSource(rec));
			}
			if (activeRecordingOnSourceCache.get(rec)) {
				if (rec.getIsActive() == true) {
					ArrayList<RecordingItem> recordingItem = ((Recording) rec).getRecordingItem();
					Iterator<RecordingItem> recItemIte = recordingItem.iterator();
					if (logger.isDebugEnabled()) {
						logger.debug("Record: " + rec.getName());
					}
					sb.setLength(0);
					int cnt = 0;
					while (recItemIte.hasNext()) {
						RecordingItem recItem = recItemIte.next();
						String stBefore = (String) recItem.getBefore();
						String stType = (String) recItem.getType();
						String stAfter = (String) recItem.getAfter();
						if (stType.equals("date")) {
							sb.append(stBefore);
							sb.append("(");
							sb.append(repo.getDateFormat(rec.getDateFormatID()).getPattern());
							sb.append(")");
							sb.append(stAfter);
						} else {
							sb.append(stBefore);
							sb.append("(");
							sb.append(getTypeString(stType));
							sb.append(")");
							sb.append(stAfter);
						}
					}
					recMatch.put(rec, sb.toString());

					// logger.info("2**** regexp: "
					// +rec.getRegexp());
					if (logger.isDebugEnabled()) {
						logger.debug("Pattern: " + sb.toString());
					}
				}
			}
		}
		return recMatch;
	}						

	public static ChartData gatherSourceData(final Repository repo) {

		PatternCache patternCache = new PatternCache();
		ChartData cd = new ChartData();
		List<File> listOfFiles = null;
		logger.info("Base file path: " + repo.getBaseSourcePath());
		if (repo.getBaseSourcePath() == null)
			return null;
		File folder = new File(repo.getBaseSourcePath());
		try {
			if (repo.isRecursiveMode()) {
				listOfFiles = FileListing.getFileListing(folder);
			} else {
				listOfFiles = Arrays.asList(folder.listFiles());

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (listOfFiles!=null)
		logger.info("number of files: " + listOfFiles.size());
		cd.sourceArrayList = repo.getSources();
		Iterator<Source> sourceIterator = cd.sourceArrayList.iterator();

		while (sourceIterator.hasNext()) {
			final Source source = sourceIterator.next();
			cd.selectedSourceFiles = new HashMap<Integer,FileRecord>();
			// sourceFiles contains all the matched files for a given source
			if (source.getActive()) {
				for (int i = 0; i < listOfFiles.size(); i++) {
					if (listOfFiles.get(i).isFile()) {
						String s1 = source.getSourcePattern();
						try {	
							Matcher matcher = patternCache.getPattern(s1).matcher(new File(repo.getBaseSourcePath()).toURI().relativize(new File(listOfFiles.get(i).getCanonicalPath()).toURI()).getPath());
							
							if (logger.isDebugEnabled()) {
								logger.debug(i+" matching file: " + new File(repo.getBaseSourcePath()).toURI().relativize(new File(listOfFiles.get(i).getCanonicalPath()).toURI()).getPath() + " with pattern: " + s1);
							}
						
						if (matcher.find()) {
	
								FileRecord tempFileRecord=new FileRecord(i, new File((String) listOfFiles.get(i).getCanonicalPath()));
								cd.selectedSourceFiles.put(i,tempFileRecord);
								if (logger.isDebugEnabled()) {
									
									logger.debug("Source: " + source.getSourceName() + " file: " + listOfFiles.get(i).getCanonicalPath());
									logger.debug(" Graphpanel file: "
											+ new File(repo.getBaseSourcePath()).toURI().relativize(new File(listOfFiles.get(i).getCanonicalPath()).toURI()).getPath());
									logger.debug(tempFileRecord.getCompletePath());
								}

						}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
					}
				}
				if (logger.isEnabledFor(Level.INFO))
					logger.info("matched file: " + cd.selectedSourceFiles.size() + " to source  " + source.getSourceName());
			}
			cd.sourceFileArrayListMap.put(source, cd.selectedSourceFiles);
		}
		Map<String, ArrayList<FileRecord>> sourceFileGroup = null;
		Iterator<Entry<Source, Map<Integer,FileRecord>>> ite = cd.sourceFileArrayListMap.entrySet().iterator();
		while (ite.hasNext()) {
			final Map.Entry sourcePairs = ite.next();

			final Source src = (Source) sourcePairs.getKey();
			Map<Integer,FileRecord> sourceFiles = (Map<Integer,FileRecord>) sourcePairs.getValue();
			sourceFileGroup = getSourceFileGroup(sourceFiles, src, repo);
			if (logger.isEnabledFor(Level.INFO))
				logger.info("matched groups: " + sourceFileGroup.keySet().size() + " for source " + src.getSourceName());
			logger.debug(sourceFileGroup.toString());
			cd.setGroupFilesArrayListMap(src, sourceFileGroup);
		}
		return cd;
	}

	
	public static void populateRecordingSamples(Repository repo){
		PatternCache patternCache = new PatternCache();
		FileReader flstr = null;
		BufferedReader buf1st;
		Map<Recording, String> recMatch = new HashMap<Recording, String>();
		Matcher matcher;
		Matcher matcher2;
		logger.info("popu1");
		if (repo.getBaseSourcePath() == null) return;
		logger.info("popu1");
		File folder = new File(repo.getBaseSourcePath());
		try {
			if (repo.isRecursiveMode()) {
				listOfFiles = FileListing.getFileListing(folder);
				logger.info("popu1");
			} else {
				listOfFiles = Arrays.asList(folder.listFiles());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		}
		if (repo!= null && repo.getBaseSourcePath()!=null){
			ChartData cd = DataMiner.gatherSourceData(repo);
		
			logger.info("popu2");
	  ArrayList sources=repo.getSources(); 
	  Iterator sourceArrayListIte=sources.iterator(); 
	  while  (sourceArrayListIte.hasNext()){
	  
	//  Map<Recording, String> regMap=getRegexp(repo, src);
	  cd.sourceArrayList = repo.getSources();
		Iterator<Source> sourceIterator = cd.sourceArrayList.iterator();

		  Source src= (Source)sourceArrayListIte.next();
		  Map<String, ArrayList<FileRecord>> hm = cd.getGroupFilesMap(src); 
			logger.info("popu3");
	  Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry pairs = (Map.Entry) it.next();
			logger.info("popu4: "+pairs.getKey());
			ArrayList<FileRecord> grouFile=(ArrayList<FileRecord>) pairs.getValue();
		//	return DataMiner.mine((String) pairs.getKey(), (ArrayList<String>) pairs.getValue(), repo, source, repo.isStats(), repo.isTimings());
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
				logger.info("matched file:"+fileName);
					recMatch = getRegexp(repo, src);
					int lineCount = 0;
					try {
						while ((line = buf1st.readLine()) != null) {
							// check against one Recording pattern at a tim
							// if (logger.isDebugEnabled()) {
							// logger.debug("line " + line);
							// }
							Iterator recMatchIte = recMatch.entrySet().iterator();
							while (recMatchIte.hasNext()) {
								Map.Entry me = (Map.Entry) recMatchIte.next();
								Recording rec = (Recording) me.getKey();
								matcher = patternCache.getPattern((String) (rec.getRegexp())).matcher(line);
								if (matcher.find()) {
									// logger.info("1**** matched: " + line);
									ArrayList<RecordingItem> recordingItem = ((Recording) rec).getRecordingItem();
									int cnt = 0;
									matcher2 = patternCache.getPattern((String) me.getValue()).matcher(line);
									if (matcher2.find()) {

										DataVault.addMatchedLines(rec, line);
  }else {
	  DataVault.addUnmatchedLines( rec, line);
  }
}
  
}}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}}}}}}

		public static ArrayList<Map> exportData(Repository repo) {
			gatherMineResultSet(repo,null);
			return null;
	 
		}
/*	public static ArrayList<Map> exportData(Repository repo) {
		PatternCache patternCache = new PatternCache();
		Matcher matcher = null;
		ArrayList<Map> expVec = new ArrayList<Map>();
		File folder = new File(repo.getBaseSourcePath());
		try {
			if (repo.isRecursiveMode()) {
				listOfFiles = FileListing.getFileListing(folder);
			} else {
				listOfFiles = Arrays.asList(folder.listFiles());

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (logger.isEnabledFor(Level.INFO))
			logger.info("number of files: " + listOfFiles.size());
		// int[][] fileListMatches = new int[listOfFiles.size()][3];

		Iterator sourceIterator = repo.getSources().iterator();

		while (sourceIterator.hasNext()) {
			Source r = (Source) sourceIterator.next();
			ArrayList<String> sourceFiles = new ArrayList<String>();
			// sourceFiles contains all the matched files for a given source

			if (r.getActive()) {

				for (int i = 0; i < listOfFiles.size(); i++) {
					if (listOfFiles.get(i).isFile()) {
						// logger.info("File " +
						// listOfFiles.get(i).getName());
						String s1 = r.getSourcePattern();
						matcher = patternCache.getPattern(s1).matcher(listOfFiles.get(i).getName());
						if (matcher.find()) {
							try {
								sourceFiles.add(new File(repo.getBaseSourcePath()).toURI().relativize(new File(listOfFiles.get(i).getCanonicalPath()).toURI())
										.getPath());

								// logger.info(" Graphpanel file1: "+listOfFiles.get(i).getCanonicalPath());
								// logger.info(" Graphpanel file: "+new
								// File(repo.getBaseSourcePath()).toURI()
								// .relativize(new
								// File(listOfFiles.get(i).getCanonicalPath()).toURI()).getPath());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// sourceFiles.add(listOfFiles.get(i).getAbsolutePath()
							// + listOfFiles.get(i).getName());
						}
					}
				}
				logger.info("matched file: " + sourceFiles.size() + " to source group " + r.getSourceName());
			}
			Map<String, ArrayList<String>> sourceFileGroup = getSourceFileGroup(sourceFiles, r, repo);
			expVec.add(sourceFileGroup);
			logger.info("matched groups: " + sourceFileGroup.keySet().size() + " for source " + r.getSourceName());
			Iterator it = sourceFileGroup.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				logger.info(pairs.getKey().toString() + " = " + pairs.getValue());
				// it.remove(); // avoids a ConcurrentModificationException

				FileMineResultSet fMR = fastMine((ArrayList<String>) pairs.getValue(), repo, r, false, false);

				expVec.add(fMR.eventGroupTimeSeries);
				expVec.add(fMR.statGroupTimeSeries);
			}
		}
		return expVec;
	}
*/
}
