/*******************************************************************************
 * LogDruid : Generate charts and reports using data gathered in log files
 * Copyright (C) 2016 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package logdruid.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.lang.management.ManagementFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeriesDataItem;

import logdruid.data.DateFormat;
import logdruid.data.ExtendedTimeSeries;
import logdruid.data.Preferences;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.mine.ChartData;
import logdruid.data.mine.FileLine;
import logdruid.data.mine.FileMineResult;
import logdruid.data.mine.FileMineResultSet;
import logdruid.data.mine.FileRecord;
import logdruid.data.mine.MineResult;
import logdruid.data.mine.MineResultSet;
import logdruid.data.record.EventRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.data.record.ReportRecording;
import logdruid.data.record.StatRecording;
import logdruid.ui.MainFrame;
import logdruid.util.ClassCache;
import logdruid.util.PatternCache;
import logdruid.util.DataMiner;
import logdruid.util.ThreadLocalDateFormatMap;

public class Miner {
	private static Logger logger = Logger.getLogger(Miner.class.getName());
	private static ExecutorService ThreadPool_FileWorkers = null;
	private static ExecutorService ThreadPool_GroupWorkers = null;
	static long estimatedTime = 0;
	static long startTime = 0;
	static List<File> listOfFiles = null;
	static final Map<Source, Map<Recording, Map<List<Object>, Long>>> occurenceReport = new ConcurrentHashMap<Source, Map<Recording, Map<List<Object>, Long>>>();
	static final Map<Source, Map<Recording, Map<List<Object>, Double>>> sumReport = new ConcurrentHashMap<Source, Map<Recording, Map<List<Object>, Double>>>();
	static final Map<Source, Map<Recording, SortedMap<Double, List<Object>>>> top100Report = new ConcurrentHashMap<Source, Map<Recording, SortedMap<Double, List<Object>>>>();;

	public static MineResultSet gatherMineResultSet(ChartData cd, final Repository repo, final MainFrame mainFrame) {
		String test = Preferences.getPreference("ThreadPool_Group");
		int ini = Integer.parseInt(test);
		logger.info("gatherMineResultSet parallelism: " + ini);
		ThreadPool_GroupWorkers = Executors.newFixedThreadPool(ini);
		Collection<Callable<MineResult>> tasks = new ArrayList<Callable<MineResult>>();
		MineResultSet mineResultSet = new MineResultSet();
		occurenceReport.clear();
		sumReport.clear();
		top100Report.clear();

		startTime = System.currentTimeMillis();
		/*
		 * try { cd = DataMiner.gatherSourceData(repo); } catch (Exception e) {
		 * return null; }
		 */

		Iterator<Source> sourceIterator2 = repo.getSources().iterator();
		int progressCount = 0;
		while (sourceIterator2.hasNext()) {
			final Source source = sourceIterator2.next();
			// sourceFiles contains all the matched files for a given source
			if (source.getActive() && source.getActiveMetadata() != null) {
				Iterator<Entry<String, ArrayList<FileRecord>>> it = cd.getGroupFilesMap(source).entrySet().iterator();
				while (it.hasNext()) {
					final Map.Entry<String, ArrayList<FileRecord>> pairs = it.next();
					//testing as a group might have no files
					if (pairs.getValue() != null) {
						progressCount = progressCount + pairs.getValue().size();
						logger.debug("Source:" + source.getSourceName() + ", group: " + pairs.getKey() + " = " + pairs.getValue().toString());
						tasks.add(new Callable<MineResult>() {
							public MineResult call() throws Exception {
								return Miner.mine(pairs.getKey(), pairs.getValue(), repo, source, Preferences.isStats(), Preferences.isTimings(),
										Preferences.isMatches(), mainFrame);
							}

						});
					}

				}
			}
		}
		mainFrame.setMaxProgress(progressCount);
		// logger.info("progressCount "+ progressCount);
		/*
		 * invokeAll blocks until all service requests complete, or a max of
		 * 1000 seconds.
		 */
		List<Future<MineResult>> results = null;
		try {
			results = ThreadPool_GroupWorkers.invokeAll(tasks, 100000, TimeUnit.SECONDS);
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

		mineResultSet.setOccurenceReport(occurenceReport);
		mineResultSet.setTop100Report(top100Report);
		mineResultSet.setSumReport(sumReport);
		return mineResultSet;

	}

	// handle gathering for ArrayList of file for one source-group
	public static MineResult mine(String group, ArrayList<FileRecord> arrayList, final Repository repo, final Source source, final boolean stats,
			final boolean timings, final boolean matches, final MainFrame mainFrame) {
		logger.info("call to mine for source " + source.getSourceName() + " on group " + group);
		ThreadPool_FileWorkers = Executors.newFixedThreadPool(Integer.parseInt(Preferences.getPreference("ThreadPool_File")));
		final int miningChunk = Integer.parseInt(Preferences.getPreference("MiningFileChunk"));
		Date startDate = null;
		Date endDate = null;
		FileReader flstr = null;
		BufferedReader buf1st;
		Matcher matcher = null;

		Map<String, ExtendedTimeSeries> statMap = new HashMap<String, ExtendedTimeSeries>();
		Map<String, ExtendedTimeSeries> eventMap = new HashMap<String, ExtendedTimeSeries>();
		Map<String, long[]> timingStatsMap = new HashMap<String, long[]>();
		Map<String, Map<Date, FileLine>> fileLine = new HashMap<String, Map<Date, FileLine>>();
		Collection<Callable<FileMineResult>> tasks = new ArrayList<Callable<FileMineResult>>();

		final Map<Recording, String> recMatch1 = DataMiner.getAllRegexSingleMap(repo, source);

		ArrayList<Object> mapArrayList;
		mapArrayList = new ArrayList<>();
		PatternCache patternCache = new PatternCache();
		Iterator<FileRecord> iterator = arrayList.iterator();
		int nbLines = 0;
		while (iterator.hasNext()) {
			final FileRecord fileRecord = (FileRecord) iterator.next();
			ArrayList<String[]> dataBlock = new ArrayList<String[]>(miningChunk);
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("working on file: " + repo.getBaseSourcePath() + " + " + (String) fileRecord.getCompletePath().toString());
				}
				flstr = new FileReader(fileRecord.getCompletePath());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buf1st = new BufferedReader(flstr);
			String line;

			// String lastDate= null;
			// logger.debug("chunkMine source pattern" +
			// source.getDateFormat().getPattern());
			int offset = 0;
			try {
				while ((line = buf1st.readLine()) != null) {
					matcher = null;
					nbLines++;
					if (source.getDateFormat() != null) {
						matcher = patternCache.getPattern((String) ".*(" + (source.getDateFormat().getPattern()) + ").*", false).matcher(line);
					}
					if (matcher == null) {
						dataBlock.add(new String[] { null, line });
					} else if (matcher.matches()) {
						dataBlock.add(new String[] { "date", line });
					} else {
						dataBlock.add(new String[] { null, null });
					}
					if (nbLines == miningChunk) {
						// logger.info("chunkMine called at line" + totaLines);
						final ArrayList<String[]> tempBlock = new ArrayList<String[]>(dataBlock);
						final int offset1 = offset;
						tasks.add(new Callable<FileMineResult>() {
							public FileMineResult call() throws Exception {
								return chunkMine(offset1 * miningChunk, fileRecord, tempBlock, recMatch1, repo, source, stats, timings, matches, mainFrame);
							}
						});
						dataBlock.clear();
						nbLines = 0;
						offset++;
					}

				}
				if (nbLines > 0) {
					logger.debug("chunkMine rest called at line" + nbLines);
					final ArrayList<String[]> tempBlock = new ArrayList<String[]>(dataBlock);
					final int offset1 = offset;
					tasks.add(new Callable<FileMineResult>() {
						public FileMineResult call() throws Exception {
							return chunkMine(offset1 * miningChunk, fileRecord, tempBlock, recMatch1, repo, source, stats, timings, matches, mainFrame);
						}
					});
					dataBlock.clear();
					nbLines = 0;
				}
				buf1st.close();
				mainFrame.progress();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		List<Future<FileMineResult>> results = null;
		try {
			results = ThreadPool_FileWorkers.invokeAll(tasks, 100000, TimeUnit.SECONDS);
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
			it = fMR.fileLineDateMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Map<Date, FileLine>> pairs = (Map.Entry<String, Map<Date, FileLine>>) it.next();
				if (logger.isDebugEnabled()) {
					logger.debug("Entry<String,Map<Date, FileLine>> : " + pairs);
				}
				if (!fileLine.containsKey(pairs.getKey())) {
					fileLine.put(pairs.getKey(), pairs.getValue());
					if (logger.isDebugEnabled()) {
						logger.debug("groupFileLineMap.put " + pairs.getKey() + " -> " + pairs.getValue());
					}
				} else {
					Map<Date, FileLine> ts = fileLine.get(pairs.getKey());
					Map<Date, FileLine> newDateFileLineEntries = pairs.getValue();
					Iterator it2 = newDateFileLineEntries.entrySet().iterator();
					while (it2.hasNext()) {
						Map.Entry<Date, FileLine> pairs2 = (Map.Entry<Date, FileLine>) it2.next();
						fileLine.get(pairs.getKey()).put(pairs2.getKey(), pairs2.getValue());
						if (logger.isDebugEnabled()) {
							logger.debug("groupFileLineMap.put " + pairs2.getKey() + " -> " + pairs2.getValue().getFileId() + ":"
									+ pairs2.getValue().getLineNumber());
						}
					}
				}

			}

		}

		FileMineResultSet fMRS = new FileMineResultSet(fileDates, statMap, eventMap, timingStatsMap, fileLine, startDate, endDate);
		return new MineResult(group, fMRS, arrayList, repo, source);
	}

	// handle gathering for a single file chunk
	public static FileMineResult chunkMine(int offset, FileRecord fileRecord, ArrayList<String[]> dataBlock, Map<Recording, String> recMatch1, Repository repo,
			Source source, boolean stats, boolean timings, boolean matches, final MainFrame mainFrame) {
		ExtendedTimeSeries ts = null;
		PatternCache patternCache = new PatternCache();
		ClassCache classCache = new ClassCache();
		Date startDate = null;
		Date endDate = null;
		DecimalFormat decimalFormat = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
		java.text.DateFormat fastDateFormat = null;
		java.text.DateFormat sourceDateFormat = null;
		FileReader flstr = null;
		BufferedReader buf1st;
		Matcher matcher;
		Matcher matcher2;
		FixedMillisecond fMS = null;
		DateFormat df = null;
		int statHit = 0;
		int statMatch = 0;
		int eventHit = 0;
		int eventMatch = 0;
		long[] arrayBefore;
		long match0 = 0;
		long match1 = 0;
		long timing0 = 0;
		long timing1 = 0;
		Map<Recording, String> recMatch = new HashMap<Recording, String>(recMatch1);
		Map<String, ExtendedTimeSeries> statMap = new HashMap<String, ExtendedTimeSeries>();
		Map<String, ExtendedTimeSeries> eventMap = new HashMap<String, ExtendedTimeSeries>();
		Map<String, Map<Date, FileLine>> RIFileLineDateMap = new HashMap<String, Map<Date, FileLine>>();
		Map<String, long[]> matchTimings = new HashMap<String, long[]>();
		boolean gatherStats = Preferences.getBooleanPreference("gatherstats");
		boolean gatherReports = Preferences.getBooleanPreference("gatherreports");
		boolean gatherEvents = Preferences.getBooleanPreference("gatherevents");
		boolean forceSourceDateFormat = Preferences.getBooleanPreference("ForceSourceDateFormat");
		long recordingMatchStart = 0;
		long recordingMatchEnd = 0;
		if (forceSourceDateFormat == true) {
			sourceDateFormat = ThreadLocalDateFormatMap.getInstance().getDateFormat(source.getDateFormat().getDateFormat());
		}

		logger.debug("chunkMine on " + fileRecord.getCompletePath() + " - offset:" + offset);
		if (!occurenceReport.containsKey(source)) {
			occurenceReport.put(source, new ConcurrentHashMap<Recording, Map<List<Object>, Long>>());
		}
		if (!top100Report.containsKey(source)) {
			top100Report.put(source, new ConcurrentHashMap<Recording, SortedMap<Double, List<Object>>>());
		}

		if (!sumReport.containsKey(source)) {
			sumReport.put(source, new ConcurrentHashMap<Recording, Map<List<Object>, Double>>());
		}

		String line;
		try {
			// recMatch = getRegexp(repo, source);
			int lineCount = 1;

			// when there is no date, get previous

			Iterator dataBlockIterator = dataBlock.iterator();
			while (dataBlockIterator.hasNext()) {
				String[] dataBlockRecord = (String[]) dataBlockIterator.next();

				line = dataBlockRecord[1];
				if (line != null) {

					// check against one Recording pattern at a tim
					// if (logger.isDebugEnabled()) {
					// logger.info("lineCount " + lineCount);
					// }
					Iterator<Entry<Recording, String>> recMatchIte = recMatch.entrySet().iterator();
					while (recMatchIte.hasNext()) {
						if (timings) {
							recordingMatchStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
						}
						Entry<Recording, String> me = recMatchIte.next();
						Recording rec = (Recording) me.getKey();
						matcher = patternCache.getMatcher((String) (rec.getRegexp()), rec.isCaseSensitive(), line);
						if (matcher.find()) {
							Boolean isStatRecording = classCache.getClass(rec).equals(StatRecording.class);
							if (stats) {
								if (isStatRecording) {
									statMatch++;
									// logger.info("statMatch " );
								} else {
									eventMatch++;
								}
							}
							// logger.info("1**** matched: " + line);
							ArrayList<RecordingItem> recordingItem = ((Recording) rec).getRecordingItem();
							matcher2 = patternCache.getMatcher((String) me.getValue(), rec.isCaseSensitive(), line);
							if (matcher2.find()) {
								if (stats) {
									if (isStatRecording) {
										statHit++;
									} else {
										eventHit++;
									}
								}
								if (!classCache.getClass(rec).equals(ReportRecording.class)) {
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
												if (forceSourceDateFormat || rec.getUseSourceDateFormat()) {
													date1 = sourceDateFormat.parse(matcher2.group(count));
												} else {
													df = repo.getDateFormat(rec.getDateFormatID());
													fastDateFormat = ThreadLocalDateFormatMap.getInstance().getDateFormat(df.getDateFormat());
													date1 = fastDateFormat.parse(matcher2.group(count));
												}
												if (logger.isDebugEnabled())
													logger.debug("4**** rec name" + rec.getName() + " df: " + df.getId());
												// fastDateFormat =
												// FastDateFormat.getInstance(df.getDateFormat());
												if (logger.isDebugEnabled())
													logger.debug("4b**** " + df.getDateFormat() + " date: " + date1.toString());
											} catch (ParseException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										} else if (date1 != null) {
											if (recItem2.isSelected()) {
												if (logger.isDebugEnabled()) {
													logger.debug("FileRecord: " + offset + fileRecord.getFile().getName() + ", Source: "
															+ source.getSourceName() + ", " + recItem2.getName() + ", " + fileRecord.getFile().getName() + ", "
															+ ((int) offset + lineCount));
												}
												// recording line of match in
												// file
												// in map RIFileLineDateMap -
												// note
												// the FileLine object use an
												// int to
												// identify the files to save
												// memory
												Map<Date, FileLine> dateFileLineMap = null;
												// change this to recItem2 to
												// differentiate recording items
												// with same name ?? TBD
												// if
												// (RIFileLineDateMap.containsKey(recItem2.getName()))
												// {
												dateFileLineMap = RIFileLineDateMap.get(recItem2.getName());
												if (dateFileLineMap == null) {
													dateFileLineMap = new HashMap<Date, FileLine>();
												}
												dateFileLineMap.put(date1, new FileLine(fileRecord.getId(), ((int) offset + lineCount)));
												// if (logger.isDebugEnabled())
												// {
												// logger.info(recItem2.getName()+
												// " / " +
												// fileRecord.getFile().getName()
												// +
												// " dateFileLineMap put: " +
												// date1
												// + "groupFileLineMap: "
												// + fileRecord.getId() + " "
												// +offset+ " - " +lineCount);
												// logger.info(fileRecord.getFile().getName()
												// + " FileRecord: " +
												// fileRecord.getFile().getName()
												// + ", RIFileLineDateMap.put: "
												// +
												// recItem2.getName() +
												// ", line: " +
												// offset+lineCount
												// + " RIFileLineDateMap size: "
												// +
												// RIFileLineDateMap.size() +
												// " dateFileLineMap size: "
												// + dateFileLineMap.size());
												// }
												RIFileLineDateMap.put(recItem2.getName(), dateFileLineMap);

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

												if (isStatRecording && (gatherStats)) {
													ts = statMap.get(recItem2.getName());
													if (ts == null) {
														ts = new ExtendedTimeSeries(recItem2, FixedMillisecond.class);
														if (logger.isDebugEnabled())
															logger.debug("5**** Adding record to Map: " + recItem2.getName());
													}
													fMS = new FixedMillisecond(date1);
													if (matcher2.group(count) == null) {
														logger.info("null in match on " + recItem2.getName() + " at " + fileRecord.getFile().getName()
																+ " line cnt:" + offset + lineCount);
														logger.info("line : " + line);
													}
													if (recItem2.getType().equals("long")) {
														ts.getTimeSeries().addOrUpdate(
																(new TimeSeriesDataItem(fMS, Long.valueOf((String) matcher2.group(count)))));
													} else {
														try {
															ts.getTimeSeries().addOrUpdate(
																	(new TimeSeriesDataItem(fMS, Double.parseDouble(String.valueOf(decimalFormat
																			.parse((String) matcher2.group(count).replace(',', '.')))))));
														} catch (Exception e) {
															e.printStackTrace();
														}

													}

													if (stats) {
														int[] array = ts.getStat();
														array[1] = array[1] + 1;
														array[0] = array[0] + 1;
														ts.setStat(array);
														if (logger.isDebugEnabled())
															logger.debug("stats " + array[0] + " " + array[1]);
													}
													statMap.put(recItem2.getName(), ts);
													// performance: add the
													// TmeSeriesDataItem to the
													// TimeSeries instead of
													// updating
													// the TimeSeries in the Map

												} else if (classCache.getClass(rec).equals(EventRecording.class) && (gatherEvents)) {
													ts = eventMap.get(recItem2.getName());
													if (ts == null) {
														ts = new ExtendedTimeSeries(recItem2, FixedMillisecond.class);
														if (logger.isDebugEnabled())
															logger.debug("5**** Adding record to Map: " + recItem2.getName());
													}
													// SimpleTimePeriod stp =
													// new
													// SimpleTimePeriod(date1,DateUtils.addMilliseconds(date1,1));
													fMS = new FixedMillisecond(date1);

													if (((RecordingItem) recItem2).getProcessingType().equals("occurrences")) {
														TimeSeriesDataItem t = ts.getTimeSeries().getDataItem(fMS);
														if (t != null) {
															ts.getTimeSeries().addOrUpdate((new TimeSeriesDataItem(fMS, (double) t.getValue() + 1))); // +
															// (double)t.getValue()
															// need some way to
															// show
															// several
															// occurrences
														} else {
															ts.getTimeSeries().add((new TimeSeriesDataItem(fMS, 1)));
														}

													} else if (((RecordingItem) recItem2).getProcessingType().equals("duration")) {
														try {
															ts.getTimeSeries().addOrUpdate(
																	(new TimeSeriesDataItem(fMS, Double.parseDouble(String.valueOf(decimalFormat.parse(matcher2
																			.group(count)))))));
														} catch (ParseException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														}

														// ts.addOrUpdate((new
														// TimeSeriesDataItem(fMS,
														// 100)));
													} else if (((RecordingItem) recItem2).getProcessingType().equals("sum")) {
														TimeSeriesDataItem t = ts.getTimeSeries().getDataItem(fMS);
														if (t != null) {
															if (!recItem2.getType().equals("date")) {
																try {
																	ts.getTimeSeries().addOrUpdate(
																			(new TimeSeriesDataItem(fMS, Double.parseDouble(String.valueOf(decimalFormat
																					.parse(matcher2.group(count)))
																					+ ts.getTimeSeries().getDataItem(fMS).getValue()))));
																	logger.info(ts.getTimeSeries().getDataItem(fMS).getValue());
																} catch (ParseException e) {
																	// TODO
																	// Auto-generated
																	// catch
																	// block
																	e.printStackTrace();
																}
															}
														} else {
															try {
																// to improve -
																// should use
																// the
																// right type
																// here
																ts.getTimeSeries().add(
																		(new TimeSeriesDataItem(fMS, Double.parseDouble(String.valueOf(decimalFormat
																				.parse(matcher2.group(count)))))));
															} catch (ParseException e) {
																// TODO
																// Auto-generated
																// catch block
																e.printStackTrace();
															}
														}

													} else if (((RecordingItem) recItem2).getProcessingType().equals("capture")) {

													}
													// logger.debug(recItem2.getName()
													// +
													// " " +
													// Double.parseDouble((matcher2.group(count))));
													if (stats) {
														int[] array = ts.getStat();
														array[1] = array[1] + 1;
														array[0] = array[0] + 1;
														ts.setStat(array);
														if (logger.isDebugEnabled())
															logger.debug("stats " + array[0] + " " + array[1]);
													}
													eventMap.put(recItem2.getName(), ts);

												}

											}
										} // rec.getClass().equals(ReportRecording.class)

										count++;
										// logger.info("event statistics: "+eventMatch
										// +
										// " and " +eventHit +
										// " ; stat statistics: "+statMatch +
										// " and "
										// +statHit);
									}
								} else {
									if (gatherReports) {
										int count = 0;
										if (((ReportRecording) rec).getSubType().equals("histogram") && rec.getIsActive()) {
											List<Object> temp = new ArrayList<Object>();
											Iterator<RecordingItem> recItemIte2 = recordingItem.iterator();
											while (recItemIte2.hasNext()) {
												RecordingItem recItem2 = recItemIte2.next();
												if (recItem2.isSelected()) {
													temp.add(matcher2.group(count + 1));
												}
												count++;
											}
											Map<List<Object>, Long> occMap = occurenceReport.get(source).get(rec);
											if (occMap == null) {
												occurenceReport.get(source).put(rec, new ConcurrentHashMap<List<Object>, Long>());
												occMap = occurenceReport.get(source).get(rec);
											}
											synchronized (occMap) {
												Object occ = occMap.get(temp);

												if (occ == null) {
													occMap.put(temp, (long) 1);
												} else {
													occMap.put(temp, (long) occ + 1);
												}
											}
										} else if (((ReportRecording) rec).getSubType().equals("top100") && rec.getIsActive()) {
											double itemIndex = 0;

											SortedMap<Double, List<Object>> t100 = top100Report.get(source).get(rec);
											if (t100 == null) {
												top100Report.get(source).put(rec, Collections.synchronizedSortedMap(new TreeMap<Double, List<Object>>()));
												t100 = top100Report.get(source).get(rec);
											}
											try {
												itemIndex = (double) Double.valueOf(matcher2.group(((ReportRecording) rec).getTop100RecordID() + 1));
											} catch (NullPointerException npe) {
												// nothing
											} catch (NumberFormatException nfe) {
												// nothing
												logger.info(matcher2.group(0));
												logger.info(matcher2.group(((ReportRecording) rec).getTop100RecordID() + 1));
											}
											synchronized (t100) {
												if (t100.size() < 100) {
													List<Object> temp = new ArrayList<Object>();
													Iterator<RecordingItem> recItemIte2 = recordingItem.iterator();
													while (recItemIte2.hasNext()) {
														RecordingItem recItem2 = recItemIte2.next();
														if (recItem2.isSelected()) {
															if (recItem2.getProcessingType().equals("top100")) {
																itemIndex = (double) Double.valueOf(matcher2.group(count + 1));
															} else {
																temp.add(matcher2.group(count + 1));
															}
														}
														count++;
													}
													t100.put(itemIndex, temp);
												} else if (t100.size() == 100) {
													if (itemIndex > t100.firstKey()) {
														List<Object> temp = new ArrayList<Object>();
														Iterator<RecordingItem> recItemIte2 = recordingItem.iterator();
														while (recItemIte2.hasNext()) {
															RecordingItem recItem2 = recItemIte2.next();
															if (recItem2.isSelected()) {
																if (recItem2.getProcessingType().equals("top100")) {
																	itemIndex = (double) Double.valueOf(matcher2.group(count + 1));
																} else {
																	temp.add(matcher2.group(count + 1));
																}
															}
															count++;
														}
														t100.remove(t100.firstKey());
														t100.put(itemIndex, temp);
													}
												}

											}
										}

										else if (((ReportRecording) rec).getSubType().equals("sum") && rec.getIsActive()) {
											double itemIndex = 0;
											List<Object> temp = new ArrayList<Object>();
											Iterator<RecordingItem> recItemIte2 = recordingItem.iterator();
											while (recItemIte2.hasNext()) {
												RecordingItem recItem2 = recItemIte2.next();
												if (recItem2.isSelected()) {
													if (recItem2.getProcessingType().equals("sum")) {
														itemIndex = (double) Double.valueOf(matcher2.group(count + 1));
													} else {
														temp.add(matcher2.group(count + 1));
													}
												}
												count++;
											}
											Map<List<Object>, Double> sumMap = sumReport.get(source).get(rec);
											if (sumMap == null) {
												sumReport.get(source).put(rec, new ConcurrentHashMap<List<Object>, Double>());
												sumMap = sumReport.get(source).get(rec);
											}
											synchronized (sumMap) {
												Object sum = sumMap.get(temp);

												if (sum == null) {
													sumMap.put(temp, (double) itemIndex);
												} else {
													sumMap.put(temp, (double) sum + itemIndex);
												}
											}
										}
									}
								}
							}
							if (timings || matches) {
								arrayBefore = matchTimings.get(rec.getName());
								if (arrayBefore != null) {
									// logger.info(file.getName() + " contains "
									// +
									// arrayBefore);
									// 0-> sum of time for success matching of
									// given
									// recording ; 1-> sum of time for failed
									// matching ; 2-> count of match attempts,
									// 3->count of success attempts
									if (timings) {
										recordingMatchEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
										timing0 = arrayBefore[0] + recordingMatchEnd - recordingMatchStart;
										timing1 = arrayBefore[1];
									}
									if (matches) {
										match0 = arrayBefore[2] + 1;
										match1 = arrayBefore[3] + 1;
									}
									long[] array = { timing0, timing1, match0, match1 };
									matchTimings.put(rec.getName(), array);
								} else {
									if (timings) {
										recordingMatchEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
										long[] array = { recordingMatchEnd - recordingMatchStart, 0, 1, 1 };
										matchTimings.put(rec.getName(), array);
									} else {
										long[] array = { 0, 0, 1, 1 };
										matchTimings.put(rec.getName(), array);
									}
								}
							}
						} else {
							if (timings || matches) {
								arrayBefore = matchTimings.get(rec.getName());
								if (arrayBefore != null) {
									// logger.info(file.getName() + " contains "
									// +
									// arrayBefore);
									// 0-> sum of time for success matching of
									// given
									// recording ; 1-> sum of time for failed
									// matching ; 2-> count of match attempts,
									// 3->count of success attempts
									if (timings) {
										recordingMatchEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
										timing0 = arrayBefore[0];
										timing1 = arrayBefore[1] + recordingMatchEnd - recordingMatchStart;
									}
									if (matches) {
										match0 = arrayBefore[2] + 1;
										match1 = arrayBefore[3];
									}
									long[] array = { timing0, timing1, match0, match1 };
									matchTimings.put(rec.getName(), array);
								} else {
									if (timings) {
										recordingMatchEnd = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
										long[] array = { 0, recordingMatchEnd - recordingMatchStart, 1, 0 };
										matchTimings.put(rec.getName(), array);
									} else {
										long[] array = { 0, 0, 1, 0 };
										matchTimings.put(rec.getName(), array);
									}
								}
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
		}
		return new FileMineResult(fileRecord, statMap, eventMap, matchTimings, RIFileLineDateMap, startDate, endDate);
	}

}
