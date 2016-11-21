package logdruid.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import logdruid.data.Source;
import logdruid.data.mine.ReportData;
import logdruid.data.mine.ReportItem;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.data.record.ReportRecording;

public class ReportProcessor implements Runnable {
	private final BlockingQueue<ReportItem> queue;
	private ReportData reportData;
	private static Logger logger = Logger.getLogger(ReportProcessor.class.getName());
	private int count2 = 0;

	public ReportProcessor(BlockingQueue<ReportItem> queue1, ReportData repData) {
		queue = queue1;
		reportData = repData;
	}

	public void run() {
		while (true) {
			try {
				consume(queue.take());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void consume(ReportItem reportItem) {
		count2++;
		if (count2 >= 10000) {
			logger.debug("consumed 10k");
			count2 = 0;
		}
		ReportRecording rec = reportItem.getRecording();
		Matcher matcher2 = reportItem.getMatcher();
		Source source = reportItem.getSource();
		mapCheck(source);

		int count = 0;
		if (((ReportRecording) rec).getSubType().equals("histogram") && rec.getIsActive()) {
			List<Object> temp = new ArrayList<Object>();
			Iterator<RecordingItem> recItemIte2 = rec.getRecordingItem().iterator();
			while (recItemIte2.hasNext()) {
				RecordingItem recItem2 = recItemIte2.next();
				if (recItem2.isSelected()) {
					try {
						temp.add(matcher2.group(count + 1));
					} catch (StringIndexOutOfBoundsException siobe) {
						logger.debug("not matched: " + rec.getName() + reportItem.getLine());
						temp.add("");
					} catch (IllegalStateException e) {
						logger.debug("not matched2: " + rec.getName() + reportItem.getLine());
						temp.add("");
					}
				}
				count++;
			}
			Map<List<Object>, Long> occMap = reportData.occurenceReport.get(source).get(rec);
			if (occMap == null) {
				reportData.occurenceReport.get(source).put(rec, new ConcurrentHashMap<List<Object>, Long>());
				occMap = reportData.occurenceReport.get(source).get(rec);
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

			SortedMap<Double, List<Object>> t100 = reportData.top100Report.get(source).get(rec);
			if (t100 == null) {
				reportData.top100Report.get(source).put(rec, Collections.synchronizedSortedMap(new TreeMap<Double, List<Object>>()));
				t100 = reportData.top100Report.get(source).get(rec);
			}
			try {
				itemIndex = (double) Double.valueOf(matcher2.group(((ReportRecording) rec).getTop100RecordID() + 1));
			} catch (NullPointerException npe) {
				// nothing
			} catch (NumberFormatException nfe) {
				// nothing
				logger.info(matcher2.group(0));
				// logger.info(matcher2.group(((ReportRecording)
				// rec).getTop100RecordID() + 1));
			}
			synchronized (t100) {
				if (t100.size() < 100) {
					List<Object> temp = new ArrayList<Object>();
					Iterator<RecordingItem> recItemIte2 = rec.getRecordingItem().iterator();
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
						Iterator<RecordingItem> recItemIte2 = rec.getRecordingItem().iterator();
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
			Iterator<RecordingItem> recItemIte2 = rec.getRecordingItem().iterator();
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
			Map<List<Object>, Double> sumMap = reportData.sumReport.get(source).get(rec);
			if (sumMap == null) {
				reportData.sumReport.get(source).put(rec, new ConcurrentHashMap<List<Object>, Double>());
				sumMap = reportData.sumReport.get(source).get(rec);
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

	// Check that a key exists in the map for the source
	private void mapCheck(Source source) {
		if (!ReportData.occurenceReport.containsKey(source)) {
			ReportData.occurenceReport.put(source, new ConcurrentHashMap<Recording, Map<List<Object>, Long>>());
		}
		if (!ReportData.top100Report.containsKey(source)) {
			ReportData.top100Report.put(source, new ConcurrentHashMap<Recording, SortedMap<Double, List<Object>>>());
		}

		if (!ReportData.sumReport.containsKey(source)) {
			ReportData.sumReport.put(source, new ConcurrentHashMap<Recording, Map<List<Object>, Double>>());
		}
	}

}
