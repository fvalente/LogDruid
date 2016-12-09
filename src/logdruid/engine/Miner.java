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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.lang.management.ManagementFactory;


import org.apache.log4j.Logger;

import logdruid.data.ExtendedTimeSeries;
import logdruid.data.Preferences;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.mine.ChartData;
import logdruid.data.mine.FileLine;
import logdruid.data.mine.FileMineResult;
import logdruid.data.mine.FileMineResultSet;
import logdruid.data.mine.FileRecord;
import logdruid.data.mine.MineData;
import logdruid.data.mine.MineItem;
import logdruid.data.mine.MineResult;
import logdruid.data.mine.MineResultSet;
import logdruid.data.mine.ReportData;
import logdruid.data.mine.ReportItem;
import logdruid.data.record.Recording;

import logdruid.ui.MainFrame;
import logdruid.util.DataMiner;
import logdruid.util.PatternCache;

public class Miner {
	private static Logger logger = Logger.getLogger(Miner.class.getName());
	private static ExecutorService ThreadPool_MineProcessorWorkers = null;
	private static ExecutorService ThreadPool_SourceGroupWorkers = null;
	static long estimatedTime = 0;
	static long startTime = 0;
	static List<File> listOfFiles = null;
	static ReportData reportData=new ReportData();
	static Thread consumer;

	static final BlockingQueue<ReportItem> reportQueue=new LinkedBlockingQueue<ReportItem>();
	
	public static MineResultSet gatherMineResultSet(ChartData cd, final Repository repo, final MainFrame mainFrame) {
		int ini = Integer.parseInt(Preferences.getPreference("ThreadPool_SourceGroup"));
		logger.debug("gatherMineResultSet parallelism: " + ini);
		ThreadPool_SourceGroupWorkers = Executors.newFixedThreadPool(ini);
		int ini2 =Integer.parseInt(Preferences.getPreference("ThreadPool_MineProcessor"));
		ThreadPool_MineProcessorWorkers = Executors.newFixedThreadPool(ini2);
		Collection<Callable<MineResult>> tasks = new ArrayList<Callable<MineResult>>();
		MineResultSet mineResultSet = new MineResultSet();
		reportData.clear();
		ReportProcessor reportProcessor= new ReportProcessor(reportQueue, reportData);
		consumer = new Thread(reportProcessor);
		consumer.start();
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
				final Map<Recording, String> recMatch1 = DataMiner.getAllRegexSingleMap(repo, source);
				Iterator<Entry<String, ArrayList<FileRecord>>> it = cd.getGroupFilesMap(source).entrySet().iterator();
				while (it.hasNext()) {
					final Map.Entry<String, ArrayList<FileRecord>> pairs = it.next();
					//testing as a group might have no files
					if (pairs.getValue() != null) {
						progressCount = progressCount + pairs.getValue().size();
						if (logger.isDebugEnabled())
							logger.info("Source:" + source.getSourceName() + ", group: " + pairs.getKey() + " = " + pairs.getValue().toString());
						tasks.add(new Callable<MineResult>() {
							public MineResult call() throws Exception {
								return Miner.mine(pairs.getKey(), pairs.getValue(), repo, source,recMatch1, Preferences.isStats(), Preferences.isTimings(),
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
			results = ThreadPool_SourceGroupWorkers.invokeAll(tasks, 100000, TimeUnit.SECONDS);
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
		
		mineResultSet.setOccurenceReport(reportData.occurenceReport);
		mineResultSet.setTop100Report(reportData.top100Report);
		mineResultSet.setSumReport(reportData.sumReport);
		return mineResultSet;

	}

	// handle gathering for ArrayList of file for one source-group
	public static MineResult mine(String group, ArrayList<FileRecord> arrayList, final Repository repo, final Source source, Map<Recording, String> recMatch1, final boolean stats,
			final boolean timings, final boolean matches, final MainFrame mainFrame) {
		// attempt to reduce memory footprint
		BlockingQueue<MineItem> mineQueue=new LinkedBlockingQueue<MineItem>(100);
		MineData mineData=new MineData();
		int ini =Integer.parseInt(Preferences.getPreference("ThreadPool_ProcessorByGroup"));
		ArrayList<MineProcessor> mineProcessorList= new ArrayList<MineProcessor>();
		for (int i=0;i<ini;i++){
			MineProcessor mineProcessor= new MineProcessor(mineQueue,reportQueue, mineData);
			ThreadPool_MineProcessorWorkers.execute(mineProcessor);
			mineProcessorList.add(mineProcessor);
		}
		long mineStartTime =ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		final int miningChunk = Integer.parseInt(Preferences.getPreference("MiningFileChunk"));
		Date startDate = null;
		Date endDate = null;
		FileReader flstr = null;
		BufferedReader buf1st;
		Matcher matcher = null;
		int optionBlock = 0;
		Map<String, ExtendedTimeSeries> statMap = new HashMap<String, ExtendedTimeSeries>();
		Map<String, ExtendedTimeSeries> eventMap = new HashMap<String, ExtendedTimeSeries>();
		Map<String, long[]> timingStatsMap = new HashMap<String, long[]>();
		Map<String, Map<Date, FileLine>> fileLine = new HashMap<String, Map<Date, FileLine>>();
		//to be improved

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
			optionBlock =0;
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
						try {
							mineQueue.put(new MineItem(reportQueue,offset1 * miningChunk, fileRecord, tempBlock, recMatch1, repo, source, stats, timings, matches));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						dataBlock.clear();
						nbLines = 0;
						offset++;			
					}

				}
				if (nbLines > 0) {
					logger.debug("chunkMine rest called at line" + nbLines);
					final ArrayList<String[]> tempBlock = new ArrayList<String[]>(dataBlock);
					final int offset1 = offset;
					try {
						mineQueue.put(new MineItem(reportQueue,offset1 * miningChunk, fileRecord, tempBlock, recMatch1, repo, source, stats, timings, matches));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					optionBlock=1;
					dataBlock.clear();
					nbLines = 0;
				}
				//increase the number of blocks present in mineData
				int temp=mineData.getBlocks()+offset+optionBlock;
				mineData.setBlocks(temp);
				logger.debug("temp: "+ temp); 
				logger.debug("blocks: "+(mineData.getBlocks()+offset+optionBlock)+ " "+mineData.getBlocks()+" "+optionBlock + " temp: "+temp);
				
				buf1st.close();
				mainFrame.progress();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		
		while (mineQueue.peek()!=null ){
			try {
				Thread.sleep (50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Iterator it22=mineProcessorList.iterator();
		while (it22.hasNext()){
			MineProcessor mp =(MineProcessor) it22.next();
			mp.shutdown();
		}
while (mineData.getBlocks()!= mineData.fileMineResultArray.size()){
	try {
		Thread.sleep (50);
		logger.debug(mineData.fileMineResultArray.size()+" -- "+mineData.getBlocks());
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}


		ArrayList<Object[]> fileDates = new ArrayList<Object[]>();
		Iterator<FileMineResult> mapArrayListIterator = mineData.fileMineResultArray.iterator();
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
				if (logger.isTraceEnabled()) {
					logger.trace("1: " + fMR.getStartDate() + "2: " + fMR.getEndDate() + "3: " + fMR.getFile());
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
				if (logger.isTraceEnabled()) {
					logger.trace("Entry<String,Map<Date, FileLine>> : " + pairs);
				}
				if (!fileLine.containsKey(pairs.getKey())) {
					fileLine.put(pairs.getKey(), pairs.getValue());
					if (logger.isTraceEnabled()) {
						logger.trace("groupFileLineMap.put " + pairs.getKey() + " -> " + pairs.getValue());
					}
				} else {
					Map<Date, FileLine> ts = fileLine.get(pairs.getKey());
					Map<Date, FileLine> newDateFileLineEntries = pairs.getValue();
					Iterator it2 = newDateFileLineEntries.entrySet().iterator();
					while (it2.hasNext()) {
						Map.Entry<Date, FileLine> pairs2 = (Map.Entry<Date, FileLine>) it2.next();
						fileLine.get(pairs.getKey()).put(pairs2.getKey(), pairs2.getValue());
						if (logger.isTraceEnabled()) {
							logger.trace("groupFileLineMap.put " + pairs2.getKey() + " -> " + pairs2.getValue().getFileId() + ":"
									+ pairs2.getValue().getLineNumber());
						}
					}
				}

			}

		}
		
		long postMineStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		logger.info((postMineStartTime - mineStartTime)/1000000+"ms for source " + source.getSourceName() + " on group " + group);
		FileMineResultSet fMRS = new FileMineResultSet(fileDates, statMap, eventMap, timingStatsMap, fileLine, startDate, endDate);
		return new MineResult(group, fMRS, arrayList, repo, source);
	}

	
}
