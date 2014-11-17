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
package logdruid.ui.chart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import logdruid.data.FileMineResult;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.record.Recording;
import logdruid.util.DataMiner;
import logdruid.util.FileListing;

import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.ScrollPaneConstants;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

public class BasicGraphPanel extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private DataMiner miner;
	private JScrollPane scrollPane;
	private JPanel panel;

	/**
	 * Create the panel.
	 */

	public BasicGraphPanel(Repository repo) {
		setLayout(new BorderLayout(0, 0));
		panel = new JPanel();
		scrollPane = new JScrollPane(panel);
		add(scrollPane, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		List<File> listOfFiles = null;
		XYPlot plot;
		JFreeChart chart = null;
		DataMiner miner = new DataMiner();
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
						Pattern pattern = Pattern.compile(s1);
						Matcher matcher = pattern.matcher(listOfFiles.get(i).getName());
						// logger.info("matching with pattern: " + s1);
						// logger.info("matching file: " +
						// listOfFiles.get(i).getName());
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
			logger.info("matched groups: " + sourceFileGroup.keySet().size() + " for source " + r.getSourceName());
			Iterator it = sourceFileGroup.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				logger.info(pairs.getKey().toString() + " = " + pairs.getValue());
				// it.remove(); // avoids a ConcurrentModificationException
				FileMineResult fMR = miner.fastMine((Vector<String>) pairs.getValue(), repo, r);
				HashMap hashMap2;
				hashMap2 = fMR.statGroupTimeSeries;

				TimeSeriesCollection dataset = new TimeSeriesCollection();

				Iterator it2 = hashMap2.entrySet().iterator();
				while (it2.hasNext()) {
					dataset = new TimeSeriesCollection();
					Map.Entry me = (Map.Entry) it2.next();
					logger.info(me.toString());
					// dataset.addSeries((TimeSeries)me.getValue());
					dataset.addSeries((TimeSeries) me.getValue());

					/*
					 * JFreeChart chart = ChartFactory.createTimeSeriesChart
					 * (r.getSourceName()+"."+pairs.getKey().toString(), //
					 * Title "Day", // X-Axis label "Number Of Users", // Y-Axis
					 * label dataset, // Dataset true, // Show legend true,
					 * //tooltips false //url );
					 */

					chart = ChartFactory.createXYAreaChart(r.getSourceName() + " " + pairs.getKey().toString(), // Title
							"Day", // X-Axis label
							"Number Of Users", // Y-Axis label
							dataset, // Dataset
							PlotOrientation.VERTICAL, true, // Show legend
							true, // tooltips
							false // url
							);
					/*
					 * XYDataset xyDataset = new XYSeriesCollection(series);
					 * JFreeChart chart = ChartFactory.createXYLineChart
					 * ("Sample XY Chart", // Title "Height", // X-Axis label
					 * "Weight", // Y-Axis label xyDataset // Dataset );
					 */
					plot = chart.getXYPlot();

					NumberAxis axis4 = new NumberAxis("Range Axis 4");
					axis4.setLabelPaint(Color.green);
					axis4.setTickLabelPaint(Color.green);
					plot.setRangeAxis(3, axis4);

					final ValueAxis domainAxis = new DateAxis("Time");
					domainAxis.setLowerMargin(0.0);
					domainAxis.setUpperMargin(0.0);
					plot.setDomainAxis(domainAxis);
					plot.setForegroundAlpha(0.5f);

					final XYItemRenderer renderer = plot.getRenderer();
					renderer.setToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new SimpleDateFormat(
							"d-MMM-yyyy"), new DecimalFormat("#,##0.00")));

					JPanel pan = new JPanel();
					pan.setLayout(new BorderLayout());
					pan.setPreferredSize(new Dimension(900, 300));
					panel.add(pan);
					ChartPanel cpanel = new ChartPanel(chart);
					cpanel.setPreferredSize(new Dimension(900, 300));
					pan.add(cpanel, BorderLayout.WEST);

				}

			}

			// hashMap=miner.mine(sourceFiles,repo);
		}
	}
}
