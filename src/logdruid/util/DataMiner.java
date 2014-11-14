package logdruid.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logdruid.data.ChartData;
import logdruid.data.DateFormat;
import logdruid.data.FileMineResult;
import logdruid.data.MineResult;
import logdruid.data.MineResultSet;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.record.EventRecording;
//import logdruid.data.record.EventRecordingItem;
import logdruid.data.record.MetadataRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.data.record.StatRecording;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.FixedMillisecond;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DataMiner {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());

	static List<File> listOfFiles = null;
	private static final ExecutorService workers = Executors.newFixedThreadPool(8);
	private static final ExecutorService workers1 = Executors.newFixedThreadPool(8);
	static DataMiner miner = new DataMiner();

	public static MineResultSet gatherMineResultSet(final Repository repo) {
		ChartData cd = new ChartData();
		Collection<Callable<MineResult>> tasks = new ArrayList<Callable<MineResult>>();
		MineResultSet mineResultSet = new MineResultSet();
		List<File> listOfFiles = null;
		final DataMiner miner = new DataMiner();
		HashMap hashMap;
		logger.info("Base file path: " + repo.getBaseSourcePath());

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
		logger.info("number of files: " + listOfFiles.size());
		// int[][] fileListMatches = new int[listOfFiles.size()][3];
		cd.sourceVector = repo.getSources();

		Iterator sourceIterator = cd.sourceVector.iterator();

		while (sourceIterator.hasNext()) {
			final Source source = (Source) sourceIterator.next();
			// source.timeSeriesHashMapVector= new Vector< HashMap<String,
			// TimeSeries>>();
			cd.selectedSourceFiles = new Vector<String>();
			// sourceFiles contains all the matched files for a given source

			if (source.getActive()) {

				for (int i = 0; i < listOfFiles.size(); i++) {
					if (listOfFiles.get(i).isFile()) {
						// logger.info("File " +
						// listOfFiles.get(i).getName());
						String s1 = source.getSourcePattern();
						Matcher matcher = PatternCache.getPattern(s1).matcher(listOfFiles.get(i).getName());
						// logger.info("matching with pattern: " + s1);
						// logger.info("matching file: " +
						// listOfFiles.get(i).getName());
						if (matcher.find()) {
							try {
								cd.selectedSourceFiles.add(new File(repo.getBaseSourcePath()).toURI()
										.relativize(new File(listOfFiles.get(i).getCanonicalPath()).toURI()).getPath());
								if (logger.isDebugEnabled())
									logger.debug("Source: " + source.getSourceName() + " file: " + listOfFiles.get(i).getCanonicalPath());
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
				if (logger.isEnabledFor(Level.INFO))
					logger.info("matched file: " + cd.selectedSourceFiles.size() + " to source  " + source.getSourceName());
			}
			cd.sourceFileVectorHashMap.put(source, cd.selectedSourceFiles);
		}
		HashMap<String, Vector<String>> sourceFileGroup = null;
		Iterator ite = cd.sourceFileVectorHashMap.entrySet().iterator();
		while (ite.hasNext()) {
			final Map.Entry sourcePairs = (Map.Entry) ite.next();

			final Source src = (Source) sourcePairs.getKey();
			Vector<String> sourceFiles = (Vector<String>) sourcePairs.getValue();
			sourceFileGroup = miner.getSourceFileGroup(sourceFiles, src, repo);
			if (logger.isEnabledFor(Level.INFO))
				logger.info("matched groups: " + sourceFileGroup.keySet().size() + " for source " + src.getSourceName() + " and nb values"
						+ sourceFileGroup.values().size());

			cd.setGroupFilesVectorHashMap(src, sourceFileGroup);
		}
		if (logger.isEnabledFor(Level.INFO))
			logger.info("Vector sourceFileGroup" + sourceFileGroup);
		Iterator sourceIterator2 = repo.getSources().iterator();

		while (sourceIterator2.hasNext()) {
			final Source source = (Source) sourceIterator2.next();
			// sourceFiles contains all the matched files for a given source

			if (source.getActive()) {

				// cd.getGroupFilesHashMap(source);
				Iterator it = cd.getGroupFilesHashMap(source).entrySet().iterator();
				while (it.hasNext()) {
					final Map.Entry pairs = (Map.Entry) it.next();
					// logger.info("HEEERE1:" + pairs.getKey().toString() +
					// " = " + pairs.getValue());
					tasks.add(new Callable<MineResult>() {

						public MineResult call() throws Exception {
							// logger.info("HEEERE2:"+miner.fastMine((Vector<String>)
							// pairs.getValue(), repo, source));
							return miner.mine((String) pairs.getKey(), (Vector<String>) pairs.getValue(), repo, source);

						}

					});

				}
			}
		}
		/*
		 * invokeAll blocks until all service requests complete, or a max of
		 * 1000 seconds.
		 */
		List<Future<MineResult>> results = null;
		try {
			results = workers1.invokeAll(tasks, 1000, TimeUnit.SECONDS);
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
				if (!mineResultSet.mineResults.keySet().contains(mineRes.getSourceID())) {
					mineResultSet.mineResults.put(mineRes.getSourceID(), new Vector<MineResult>());
				}
				mineResultSet.mineResults.get(mineRes.getSourceID()).add(mineRes);

				// put(mineRes.getSource(), mineRes)
				// source.timeSeriesHashMapVector.add((MineResult) mineRes);
				// logger.info("HEEERE" + mineRes.toString());
			}
		}
		logger.info(PatternCache.getSize());
		return mineResultSet;

	}

	public MineResult mine(String group, Vector<String> fileVector, Repository repo, Source source) {

		FileMineResult hm = fastMine(fileVector, repo, source);
		MineResult mineResult = new MineResult(group, hm, fileVector, repo, source);

		return mineResult;

	}

	// handle gathering for vector of file
	public FileMineResult fastMine(Vector<String> fileVector, final Repository repo, final Source source) {
		Date startDate = null;
		Date endDate = null;
		HashMap<String, TimeSeries> statHashMap = new HashMap<String, TimeSeries>();
		HashMap<String, TimeSeries> eventHashMap = new HashMap<String, TimeSeries>();
		Collection<Callable<FileMineResult>> tasks = new ArrayList<Callable<FileMineResult>>();
		// patternVector = new Vector<Pattern>();

		Vector<Object> mapVector;
		mapVector = new Vector<>();
		if (logger.isEnabledFor(Level.INFO))
			logger.info("mine called on " + source.getSourceName());
		Iterator<String> fileVectorIterator = fileVector.iterator();
		while (fileVectorIterator.hasNext()) {
			// oneFile=;
			final String fileName = fileVectorIterator.next();
			tasks.add(new Callable<FileMineResult>() {

				public FileMineResult call() throws Exception {
					// logger.info("HEEERE2:"+miner.mine((Vector<String>)
					// pairs.getValue(), repo, source));
					return fileMine(new File(repo.getBaseSourcePath() + "/" + fileName), repo, source);
				}

			});

		}
		List<Future<FileMineResult>> results = null;
		try {
			results = workers.invokeAll(tasks, 1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Future<FileMineResult> f : results) {
			FileMineResult mineRes = null;
			try {
				mineRes = f.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (mineRes != null) {
				mapVector.add(mineRes);
			}

		}

		Iterator<Object> mapVectorIterator = mapVector.iterator();
		while (mapVectorIterator.hasNext()) {
			FileMineResult fMR = (FileMineResult) mapVectorIterator.next();

			if (startDate == null) {
				startDate = fMR.getStartDate();
			}
			if (endDate == null) {
				endDate = fMR.getEndDate();
			}
			if (fMR.getEndDate() != null)
				if (fMR.getEndDate().after(endDate)) {
					endDate = fMR.getEndDate();
				} else if (fMR.getStartDate().before(startDate)) {
					startDate = fMR.getStartDate();
				}

			HashMap tempStatHashMap = fMR.statGroupTimeSeries;
			tempStatHashMap.entrySet();
			Iterator it = tempStatHashMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, TimeSeries> pairs = (Map.Entry<String, TimeSeries>) it.next();
				if (!statHashMap.containsKey(pairs.getKey())) {
					statHashMap.put(pairs.getKey(), pairs.getValue());
				} else {
					TimeSeries ts = statHashMap.get(pairs.getKey());
					ts.addAndOrUpdate(pairs.getValue());
					statHashMap.put(pairs.getKey(), ts);
				}
			}

			HashMap tempEventHashMap = fMR.eventGroupTimeSeries;
			tempEventHashMap.entrySet();
			it = tempEventHashMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, TimeSeries> pairs = (Map.Entry<String, TimeSeries>) it.next();
				if (!eventHashMap.containsKey(pairs.getKey())) {
					eventHashMap.put(pairs.getKey(), pairs.getValue());
				} else {
					TimeSeries ts = eventHashMap.get(pairs.getKey());
					ts.addAndOrUpdate(pairs.getValue());
					eventHashMap.put(pairs.getKey(), ts);
				}
			}
		}
		return new FileMineResult(statHashMap, eventHashMap, startDate, endDate);
	}

	// handle gathering for a single file
	public FileMineResult fileMine(File file, Repository repo, Source source) {
		Date startDate = null;
		Date endDate = null;
		HashMap<Recording, Boolean> activeRecordingOnSourceCache = new HashMap<Recording, Boolean>();
		HashMap<String, TimeSeries> statHashMap = null;
		HashMap<String, TimeSeries> eventHashMap = null;
		Vector<Recording> recordings;
		SimpleDateFormat simpleDateFormat;
		FileReader flstr = null;
		BufferedReader buf1st;
		Matcher matcher;
		Matcher matcher2;
		recordings = repo.getRecordings(StatRecording.class);
		recordings.addAll(repo.getRecordings(EventRecording.class));
		DateFormat df = null;
		try {
			if (logger.isDebugEnabled()) {
				// logger.debug("++file: "+repo.getBaseSourcePath()+"/"+(String)fileVectorIterator.next());
			}

			flstr = new FileReader(file);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		buf1st = new BufferedReader(flstr);
		String line;
		try {
			statHashMap = new HashMap<String, TimeSeries>();
			eventHashMap = new HashMap<String, TimeSeries>();
			while ((line = buf1st.readLine()) != null) {

				// Iterator patternIt = patternVector.iterator();
				// check against one Recording pattern at a tim
				Iterator<Recording> recordingIterator = recordings.iterator();
				while (recordingIterator.hasNext()) {
					Recording rec = recordingIterator.next();
					if (!activeRecordingOnSourceCache.containsKey(rec)) {
						activeRecordingOnSourceCache.put(rec, source.isActiveRecordingOnSource(rec));
					}
					if (activeRecordingOnSourceCache.get(rec)) {
						if (rec.getIsActive() == true) {
							// logger.info("1**** regexp: " +
							// rec.getRegexp());
							// Pattern pat = Pattern.compile((String)
							// (rec.getRegexp()));
							// Matcher matcher =
							// PatternCache.getPattern(s1).matcher(listOfFiles.get(i).getName());
							matcher = PatternCache.getPattern((String) (rec.getRegexp())).matcher(line);
							if (matcher.find()) {
								// logger.info("1**** matched: " + line);
								Vector recordingItem = ((Recording) rec).getRecordingItem();
								Iterator recItemIte = recordingItem.iterator();
								String patternString = "";

								int cnt = 0;
								while (recItemIte.hasNext()) {
									RecordingItem recItem = (RecordingItem) recItemIte.next();
									String stBefore = (String) recItem.getBefore();
									String stType = (String) recItem.getType();
									String stAfter = (String) recItem.getAfter();
									if (stType.equals("date")) {
										patternString += stBefore + "(" + repo.getDateFormat(rec.getDateFormatID()).getPattern() + ")" + stAfter;
									} else {
										patternString += stBefore + "(" + getTypeString(stType) + ")" + stAfter;
									}
								}
								// logger.info("2**** regexp: "
								// +rec.getRegexp());
								if (logger.isDebugEnabled()) {
									logger.debug("2**** " + patternString);
									logger.debug("line: " + line);
								}
								matcher2 = PatternCache.getPattern(patternString).matcher(line);
								if (matcher2.find()) {
									// logger.info("3**** " + line);
									int count = 1;
									Date date1 = null;
									Iterator recItemIte2 = recordingItem.iterator();
									while (recItemIte2.hasNext()) {
										RecordingItem recItem2 = (RecordingItem) recItemIte2.next();
										// logger.info("3A**** " +
										// recItem2.getType());
										if (recItem2.getType().equals("date")) {
											try {
												df = repo.getDateFormat(rec.getDateFormatID());
												if (logger.isDebugEnabled())
													logger.debug("4**** rec name" + rec.getName() + " df: " + df.getId());
												simpleDateFormat = new SimpleDateFormat(df.getDateFormat());
												date1 = simpleDateFormat.parse(matcher2.group(count));
												if (logger.isDebugEnabled())
													logger.debug("4b**** " + df.getDateFormat() + " date: " + date1.toString());
												// logger.info("4**** " +
												// date1.toString());
											} catch (ParseException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										} else {
											if (recItem2.isSelected()) {

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
												TimeSeries ts = null;
												if (rec.getClass().equals(StatRecording.class)) {
													if (statHashMap.containsKey(recItem2.getName())) {
														ts = statHashMap.get(recItem2.getName());
													} else {
														ts = new TimeSeries(recItem2.getName(), FixedMillisecond.class);
														if (logger.isDebugEnabled())
															logger.debug("5**** Adding record to hashMap: " + recItem2.getName());
													}
													// logger.info("5A**** date1: "
													// + date1);
													FixedMillisecond fMS = new FixedMillisecond(date1);
													ts.addOrUpdate((new TimeSeriesDataItem(fMS, Double.parseDouble(matcher2.group(count)))));
													// logger.info("6**** "
													// +
													// Double.parseDouble(matcher2.group(count))
													// + " " +
													// fMS.toString());
													statHashMap.put(recItem2.getName(), ts);
													// performance add the
													// TimeSeriesDataItem to
													// the
													// TimeSeries instead of
													// updating
													// the TimeSeries in the
													// HashMap

												} else { // rec.getClass().equals(EventRecording.class)
													if (eventHashMap.containsKey(recItem2.getName())) {
														ts = eventHashMap.get(recItem2.getName());
													} else {
														ts = new TimeSeries(recItem2.getName(), FixedMillisecond.class);
														if (logger.isDebugEnabled())
															logger.debug("5**** Adding record to hashMap: " + recItem2.getName());
													}
													FixedMillisecond fMS = new FixedMillisecond(date1);
													if (((RecordingItem) recItem2).getProcessingType().equals("occurrences")) {
														ts.addOrUpdate((new TimeSeriesDataItem(fMS, 100)));
													} else {
														if (!recItem2.getType().equals("date"))
															ts.addOrUpdate((new TimeSeriesDataItem(fMS, Double.parseDouble(matcher2.group(count)))));
														// ts.addOrUpdate((new
														// TimeSeriesDataItem(fMS,
														// 100)));
													}

													eventHashMap.put(recItem2.getName(), ts);
												}
											}

										}
										count++;
									}
								}
							}
						}
					}
				}
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
		return new FileMineResult(statHashMap, eventHashMap, startDate, endDate);
	}

	public HashMap serialMine(Vector<String> fileVector, Repository repo, Source source) {
		SimpleDateFormat simpleDateFormat;
		File oneFile;
		FileReader flstr = null;
		BufferedReader buf1st;
		HashMap hashMap = new HashMap();
		Vector<Recording> recordings;
		recordings = repo.getRecordings(StatRecording.class);

		Matcher matcher;
		Matcher matcher2;
		/*
		 * while (recIt.hasNext()) { Recording record=(Recording)recIt.next();
		 * regexpVector.add(record.getRegexp());
		 * patternVector.add(Pattern.compile(record.getRegexp())); }
		 */
		if (logger.isEnabledFor(Level.INFO))
			logger.info("mine called on " + source.getSourceName());
		Iterator<String> fileVectorIterator = fileVector.iterator();
		while (fileVectorIterator.hasNext()) {
			try {
				if (logger.isDebugEnabled()) {
					if (logger.isDebugEnabled())
						logger.debug("++file: " + repo.getBaseSourcePath() + "/" + fileVectorIterator.next());
				}
				oneFile = new File(repo.getBaseSourcePath() + "/" + fileVectorIterator.next());
				flstr = new FileReader(oneFile);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			buf1st = new BufferedReader(flstr);
			String line;

			// read one line at a time of the file - will need to be improved to
			// allow multi
			try {
				hashMap = new HashMap();
				while ((line = buf1st.readLine()) != null) {

					// Iterator patternIt = patternVector.iterator();
					// check against one Recording pattern at a tim
					Iterator<Recording> recordingIterator = recordings.iterator();
					while (recordingIterator.hasNext()) {
						Recording rec = recordingIterator.next();
						if (source.isActiveRecordingOnSource(rec)) {
							if (rec.getIsActive() == true) {
								// logger.info("1**** regexp: " +
								// rec.getRegexp());
								// Pattern pat = Pattern.compile((String)
								// (rec.getRegexp()));

								matcher = PatternCache.getPattern(rec.getRegexp()).matcher(line);
								if (matcher.find()) {
									// logger.info("1**** matched: " + line);
									Vector recordingItem = ((StatRecording) rec).getRecordingItem();
									Iterator recItemIte = recordingItem.iterator();
									String patternString = "";

									int cnt = 0;
									while (recItemIte.hasNext()) {
										RecordingItem recItem = (RecordingItem) recItemIte.next();
										String stBefore = (String) recItem.getBefore();
										String stType = (String) recItem.getType();
										String stAfter = (String) recItem.getAfter();
										if (stType == "date") {
											patternString += stBefore + "(" + repo.getDateFormat(rec.getDateFormatID()) + ")" + stAfter;
										} else {
											patternString += stBefore + "(" + getTypeString(stType) + ")" + stAfter;
										}
									}
									// logger.info("2**** regexp: "
									// +rec.getRegexp());
									if (logger.isEnabledFor(Level.INFO))
										logger.info("2**** " + patternString);
									if (logger.isEnabledFor(Level.INFO))
										logger.info("line: " + line);
									// Pattern pat2 =
									// Pattern.compile(patternString);
									// matcher2 = pat2.matcher(line);
									matcher2 = PatternCache.getPattern(patternString).matcher(line);
									if (matcher2.find()) {
										// logger.info("3**** " + line);
										int count = 1;
										Date date1 = null;
										Iterator recItemIte2 = recordingItem.iterator();
										while (recItemIte2.hasNext()) {
											RecordingItem recItem2 = (RecordingItem) recItemIte2.next();
											// logger.info("3A**** " +
											// recItem2.getType());
											if (recItem2.getType().equals("date")) {
												try {
													DateFormat df = repo.getDateFormat(rec.getDateFormatID());
													if (logger.isEnabledFor(Level.INFO))
														logger.info("4**** rec name" + rec.getName() + " df: " + df.getId());
													simpleDateFormat = new SimpleDateFormat(df.getDateFormat());
													if (logger.isEnabledFor(Level.INFO))
														logger.info("4b**** " + df.getDateFormat());
													date1 = simpleDateFormat.parse(matcher2.group(count));
													if (logger.isEnabledFor(Level.INFO))
														logger.info("4b**** " + df.getDateFormat() + " date: " + date1.toString());
													// logger.info("4**** " +
													// date1.toString());
												} catch (ParseException e) {
													// TODO Auto-generated catch
													// block
													e.printStackTrace();
												}
											} else {
												if (recItem2.isSelected()) {
													if (!hashMap.containsKey(recItem2.getName())) {
														hashMap.put(recItem2.getName(), new TimeSeries(recItem2.getName(), FixedMillisecond.class));
														logger.info("5**** Adding record to hashMap: " + recItem2.getName());
													} else {

														TimeSeries ts = (TimeSeries) hashMap.get(recItem2.getName());

														// logger.info("5A**** date1: "
														// + date1);
														FixedMillisecond fMS = new FixedMillisecond(date1);

														ts.add((new TimeSeriesDataItem(fMS, Double.parseDouble(matcher2.group(count)))));
														// logger.info("6**** "
														// +
														// Double.parseDouble(matcher2.group(count))
														// + " " +
														// fMS.toString());
														hashMap.put(recItem2.getName(), ts);
														// performance add the
														// TimeSeriesDataItem to
														// the
														// TimeSeries instead of
														// updating
														// the TimeSeries in the
														// HashMap
													}
												}

											}
											count++;
										}
									}
								}
							}
						}
					}
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
					flstr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// logger.info("6**** " + hashMap.toString());
		// logger.info("found group "+key + "with "+groupedFiles.size()
		// +" files in source "+ src.getSourceName());
		// logger.info("mine HashMap size: " + hashMap.size());
		// logger.info("mine HashMap : " + hashMap);
		return hashMap;

	}

	/*
	 * public HashMap<String,Vector> getSourceFileGroup(Vector<String>
	 * sourceFiles,Source src) { String patternString = ""; Vector<SourceItem>
	 * sourceItemVector=src.getSourceItem(); Iterator it=
	 * sourceItemVector.iterator(); HashMap<String,Vector> hashMap=new
	 * HashMap<String,Vector>(); while (it.hasNext()){ it.next(); }}
	 * 
	 * returns HashMap with group id in key and a Vector of matching files in
	 * value.
	 * 
	 * @param repo
	 */
	public HashMap<String, Vector<String>> getSourceFileGroup(Vector<String> sourceFiles, Source src, Repository repo) {
		String patternString = "";
		HashMap<String, Vector<String>> sourceFileGroup = new HashMap<String, Vector<String>>();
		Vector<String> groupedFiles = new Vector<String>();
		// Vector<SourceItem> sourceItemVector = src.getSourceItem();
		Vector<Recording> recordings = (Vector<Recording>) repo.getRecordings(MetadataRecording.class);
		Matcher matcher = null;
		if (recordings != null) {
			Iterator<Recording> it = recordings.iterator();
			// logger.info("recordings not null ");
			while (it.hasNext()) {
				Recording rec = it.next();
				if (src.isActiveRecordingOnSource(rec)) {
					Vector<RecordingItem> rIV = ((MetadataRecording) rec).getRecordingItem();
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
					Iterator<String> sourceFileIterator = sourceFiles.iterator();
					String key = "";
					// tempV = new Vector<String>();
					while (sourceFileIterator.hasNext()) {
						groupedFiles.removeAllElements();

						String fileName = sourceFileIterator.next();
						// logger.info("file: "+fileName);
						try {

							if (logger.isDebugEnabled())
								logger.debug("patternString: " + patternString);
							if (logger.isDebugEnabled())
								logger.debug("filename: " + fileName);
							// Pattern pattern = Pattern.compile(patternString +
							// ".*");
							// Matcher matcher = pattern.matcher(fileName);
							matcher = PatternCache.getPattern(patternString + ".*").matcher(fileName);

							if (matcher.find()) {
								if (logger.isDebugEnabled())
									logger.debug("found filename " + fileName + " with group");

								key = "";
								int i = 1;
								for (i = 1; i <= matcher.groupCount(); i++) {
									if (recordings.get(i).getIsActive()) {
										if (logger.isDebugEnabled())
											logger.debug("one : " + matcher.group(i));
										key += " - " + matcher.group(i);
									}
								}
								if (logger.isDebugEnabled())
									logger.debug("i : " + i + " nbRec: " + nbRec);
								if (i - 1 == nbRec) {
									if (logger.isDebugEnabled())
										logger.debug(" passed!");
									if (!sourceFileGroup.containsKey(key)) {
										Vector<String> v = new Vector<String>();
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
		case "long":
			typeString = "\\d+.{1}\\d+";
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

	public static Vector<HashMap> exportData(Repository repo) {
		Matcher matcher = null;
		Vector<HashMap> expVec = new Vector<HashMap>();
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
			Vector<String> sourceFiles = new Vector<String>();
			// sourceFiles contains all the matched files for a given source

			if (r.getActive()) {

				for (int i = 0; i < listOfFiles.size(); i++) {
					if (listOfFiles.get(i).isFile()) {
						// logger.info("File " +
						// listOfFiles.get(i).getName());
						String s1 = r.getSourcePattern();
						matcher = PatternCache.getPattern(s1).matcher(listOfFiles.get(i).getName());
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
			HashMap<String, Vector<String>> sourceFileGroup = miner.getSourceFileGroup(sourceFiles, r, repo);
			expVec.add(sourceFileGroup);
			logger.info("matched groups: " + sourceFileGroup.keySet().size() + " for source " + r.getSourceName() + " and nb values"
					+ sourceFileGroup.values().size());
			Iterator it = sourceFileGroup.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				logger.info(pairs.getKey().toString() + " = " + pairs.getValue());
				// it.remove(); // avoids a ConcurrentModificationException
				HashMap hashMap2;
				hashMap2 = miner.serialMine((Vector<String>) pairs.getValue(), repo, r);
				expVec.add(hashMap2);
			}
		}
		return expVec;
	}

}
