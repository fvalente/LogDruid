package logdruid.engine;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeriesDataItem;

import logdruid.data.DateFormat;
import logdruid.data.ExtendedTimeSeries;
import logdruid.data.Preferences;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.mine.FileLine;
import logdruid.data.mine.FileMineResult;
import logdruid.data.mine.FileRecord;
import logdruid.data.mine.MineData;
import logdruid.data.mine.MineItem;
import logdruid.data.mine.ReportItem;
import logdruid.data.record.EventRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.data.record.ReportRecording;
import logdruid.data.record.StatRecording;
import logdruid.util.ClassCache;
import logdruid.util.PatternCache;
import logdruid.util.ThreadLocalDateFormatMap;

public class MineProcessor implements Runnable {
	private final BlockingQueue<MineItem> queue;
	private final  BlockingQueue<ReportItem> reportQueue;
	private MineData mineData;
	private static Logger logger = Logger.getLogger(MineProcessor.class.getName());
	private int count2 = 0;
	private final static MineItem POISON = new MineItem(null, 0, null, null, null, null, null, false, false, false); 

	public MineProcessor(BlockingQueue<MineItem> queue1, BlockingQueue<ReportItem> _reportQueue, MineData mineData1) {
		queue = queue1;
		mineData = mineData1;
		reportQueue = _reportQueue;
	}
	public void shutdown(){
	    try {
	        queue.put(POISON);
	    } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	public void run() {
		while (true) {
			try {
				MineItem mi=queue.take();
	            if (mi == POISON) {
	            	queue.put(mi); // keep in the queue so all workers stop
	                break;
	            }
				consume(mi);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void consume(MineItem mineItem) {
		count2++;
		if (count2 >= 10000) {
			logger.debug("consumed 10k");
			count2 = 0;
		}
		
		int offset = mineItem.getOffset();
		FileRecord fileRecord = mineItem.getFileRecord();
		ArrayList<String[]> dataBlock = mineItem.getDataBlock();
		Map<Recording, String> recMatch1 = mineItem.getRecMatch1();

		Repository repo = mineItem.getRepo();
		Source source = mineItem.getSource();
		boolean stats = mineItem.isStats();
		boolean timings = mineItem.isTimings();
		boolean matches = mineItem.isMatches();

		ExtendedTimeSeries ts = null;
		PatternCache patternCache = new PatternCache();
		ClassCache classCache = new ClassCache();
		Date startDate = null;
		Date endDate = null;
		DecimalFormat decimalFormat = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
		java.text.DateFormat fastDateFormat = null;
		java.text.DateFormat sourceDateFormat = null;
		Matcher matcher;

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
			sourceDateFormat = ThreadLocalDateFormatMap.getInstance()
					.getDateFormat(source.getDateFormat().getDateFormat());
		}
		if (logger.isTraceEnabled()) {
			logger.trace("chunkMine on " + fileRecord.getCompletePath() + " - offset:" + offset);
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
							Matcher matcher2 = patternCache.getNewMatcher((String) me.getValue(), rec.isCaseSensitive(),
									line);

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
													fastDateFormat = ThreadLocalDateFormatMap.getInstance()
															.getDateFormat(df.getDateFormat());
													date1 = fastDateFormat.parse(matcher2.group(count));
												}
												if (logger.isTraceEnabled()) {
													logger.trace("4**** rec name: " + rec.getName());
													logger.trace("4b*** date: " + date1.toString());
												}
											} catch (ParseException e) {
												// TODO Auto-generated catch
												// block
												logger.error("date format: "+df.getDateFormat());
												e.printStackTrace();
											}
										} else if (date1 != null) {
											if (recItem2.isSelected()) {
												if (logger.isTraceEnabled()) {
													logger.trace("Offset: " + offset + " FileRecord: "
															+ fileRecord.getFile().getName() + ", Source: "
															+ source.getSourceName() + ", RecordingItem: "
															+ recItem2.getName() + ", offset+linecount"
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
												dateFileLineMap.put(date1,
														new FileLine(fileRecord.getId(), ((int) offset + lineCount)));
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
												// stat Recording Processing
												if (isStatRecording && (gatherStats)) {
													ts = statMap.get(recItem2.getName());
													if (ts == null) {
														ts = new ExtendedTimeSeries(recItem2, FixedMillisecond.class);
														if (logger.isTraceEnabled())
															logger.trace("5**** Adding record to Map: "
																	+ recItem2.getName());
													}
													fMS = new FixedMillisecond(date1);
													if (matcher2.group(count) == null) {
														logger.info("null in match on " + recItem2.getName() + " at "
																+ fileRecord.getFile().getName() + " line cnt:" + offset
																+ lineCount);
														logger.info("line : " + line);
													}
													if (recItem2.getType().equals("long")) {
														ts.getTimeSeries().addOrUpdate((new TimeSeriesDataItem(fMS,
																Long.valueOf((String) matcher2.group(count)))));
													} else {
														try {
															ts.getTimeSeries().addOrUpdate((new TimeSeriesDataItem(fMS,
																	Double.parseDouble(String.valueOf(decimalFormat
																			.parse((String) matcher2.group(count)
																					.replace(',', '.')))))));
														} catch (Exception e) {
															e.printStackTrace();
														}

													}

													if (stats) {
														int[] array = ts.getStat();
														array[1] = array[1] + 1;
														array[0] = array[0] + 1;
														ts.setStat(array);
														if (logger.isTraceEnabled())
															logger.trace("stats " + array[0] + " " + array[1]);
													}
													statMap.put(recItem2.getName(), ts);
													// performance: add the
													// TmeSeriesDataItem to the
													// TimeSeries instead of
													// updating
													// the TimeSeries in the Map

												}
												// Event Record Processing
												else if (classCache.getClass(rec).equals(EventRecording.class)
														&& (gatherEvents)) {
													ts = eventMap.get(recItem2.getName());
													if (ts == null) {
														ts = new ExtendedTimeSeries(recItem2, FixedMillisecond.class);
														if (logger.isTraceEnabled())
															logger.trace("5**** Adding record to Map: "
																	+ recItem2.getName());
													}
													// SimpleTimePeriod stp =
													// new
													// SimpleTimePeriod(date1,DateUtils.addMilliseconds(date1,1));
													fMS = new FixedMillisecond(date1);

													if (((RecordingItem) recItem2).getProcessingType()
															.equals("occurrences")) {
														TimeSeriesDataItem t = ts.getTimeSeries().getDataItem(fMS);
														if (t != null) {
															ts.getTimeSeries().addOrUpdate((new TimeSeriesDataItem(fMS,
																	(double) t.getValue() + 1))); // +
															// (double)t.getValue()
															// need some way to
															// show
															// several
															// occurrences
														} else {
															ts.getTimeSeries().add((new TimeSeriesDataItem(fMS, 1)));
														}

													} else if (((RecordingItem) recItem2).getProcessingType()
															.equals("duration")) {
														try {
															ts.getTimeSeries().addOrUpdate((new TimeSeriesDataItem(fMS,
																	Double.parseDouble(String.valueOf(
																			decimalFormat.parse(matcher2.group(count)
																					.replace(',', '.')))))));
														} catch (ParseException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														}

														// ts.addOrUpdate((new
														// TimeSeriesDataItem(fMS,
														// 100)));
													} else if (((RecordingItem) recItem2).getProcessingType()
															.equals("sum")) {
														TimeSeriesDataItem t = ts.getTimeSeries().getDataItem(fMS);
														if (t != null) {
															if (!recItem2.getType().equals("date")) {
																try {
																	ts.getTimeSeries()
																			.addOrUpdate((new TimeSeriesDataItem(fMS,
																					Double.parseDouble(String
																							.valueOf(decimalFormat
																									.parse(matcher2
																											.group(count)))
																							+ ts.getTimeSeries()
																									.getDataItem(fMS)
																									.getValue()))));
																	logger.info(ts.getTimeSeries().getDataItem(fMS)
																			.getValue());
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
																ts.getTimeSeries().add((new TimeSeriesDataItem(fMS,
																		Double.parseDouble(String.valueOf(decimalFormat
																				.parse(matcher2.group(count)
																						.replace(',', '.')))))));
															} catch (ParseException e) {
																// TODO
																// Auto-generated
																// catch block
																e.printStackTrace();
															}
														}

													} else if (((RecordingItem) recItem2).getProcessingType()
															.equals("capture")) {

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
														if (logger.isTraceEnabled())
															logger.trace("stats " + array[0] + " " + array[1]);
													}
													eventMap.put(recItem2.getName(), ts);

												}

											}
										} // rec.getClass().equals(ReportRecording.class)

										count++;
										// logger.info("event statistics:
										// "+eventMatch
										// +
										// " and " +eventHit +
										// " ; stat statistics: "+statMatch +
										// " and "
										// +statHit);
									}
								} else {
									if (gatherReports) {
										try {
											reportQueue
													.put(new ReportItem(source, "", matcher2, (ReportRecording) rec));
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
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
										recordingMatchEnd = ManagementFactory.getThreadMXBean()
												.getCurrentThreadCpuTime();
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
										recordingMatchEnd = ManagementFactory.getThreadMXBean()
												.getCurrentThreadCpuTime();
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
										recordingMatchEnd = ManagementFactory.getThreadMXBean()
												.getCurrentThreadCpuTime();
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
										recordingMatchEnd = ManagementFactory.getThreadMXBean()
												.getCurrentThreadCpuTime();
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
			mineData.fileMineResultArray.add(
					new FileMineResult(fileRecord, statMap, eventMap, matchTimings, RIFileLineDateMap, startDate, endDate));
			e.printStackTrace();
		}
		mineData.fileMineResultArray.add(
				new FileMineResult(fileRecord, statMap, eventMap, matchTimings, RIFileLineDateMap, startDate, endDate));
	}

}
