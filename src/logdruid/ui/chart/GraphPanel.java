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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

//import sun.awt.X11.ColorData;



import logdruid.data.MineResult;
import logdruid.data.MineResultSet;
import logdruid.data.Repository;
import logdruid.data.record.Recording;
import logdruid.util.DataMiner;
import logdruid.util.Persister;

import javax.swing.JScrollPane;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;

public final class GraphPanel extends JPanel {
	private Date minimumDate;
	private Date maximumDate;
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private JScrollPane scrollPane;
	private JPanel panel;
	private Color[] colors = { Color.blue, new Color(255, 40, 40), new Color(65, 90, 220), new Color(70, 255, 62), Color.magenta, Color.orange, Color.pink,	new Color(65, 90, 220), new Color(107, 255, 102) };
	//private Color[] colors = { new Color(65,171,93),new Color(254,196,79),new Color(65,171,93), new Color(239,59,44), new Color(65,182,196),new Color(5,112,176), new Color(254,178,76),Color.blue, new Color(255, 40, 40), new Color(65, 90, 220), new Color(70, 255, 62), Color.magenta, Color.orange, Color.pink,
		//	new Color(65, 90, 220), new Color(107, 255, 102) };	
    
    
	JSpinner startDateJSpinner;
	JSpinner endDateJSPinner;
	MineResultSet mineResultSet;

	long estimatedTime = 0;
	long startTime = 0;

	XYPlot plot = null;
	JFreeChart chart = null;
	final StandardChartTheme chartTheme = (StandardChartTheme) org.jfree.chart.StandardChartTheme.createJFreeTheme();
	final Font oldSmallFont = chartTheme.getSmallFont();

	/**
	 * Create the panel.
	 * 
	 * @param panel_2
	 */

	public GraphPanel(final Repository repo, JPanel panel_2) {
		setLayout(new BorderLayout(0, 0));
		startTime = System.currentTimeMillis();
		mineResultSet = DataMiner.gatherMineResultSet(repo);
		panel = new JPanel();
		if (mineResultSet==null){
			panel.add(new JLabel(" No Data "));
		} else {
			startDateJSpinner = (JSpinner) panel_2.getComponent(2);
			endDateJSPinner = (JSpinner) panel_2.getComponent(3);
			minimumDate = mineResultSet.getStartDate();
			maximumDate = mineResultSet.getEndDate();
			if (minimumDate != null)
				startDateJSpinner.setValue(minimumDate);
			if (maximumDate != null)
				endDateJSPinner.setValue(maximumDate);
			// removeAll();
			scrollPane = new JScrollPane(panel);
			load(panel_2);			
		}

	}

	public void resetTimePeriod(JPanel panel_2) {
		startDateJSpinner = (JSpinner) panel_2.getComponent(2);
		endDateJSPinner = (JSpinner) panel_2.getComponent(3);
		minimumDate = mineResultSet.getStartDate();
		maximumDate = mineResultSet.getEndDate();
		if (minimumDate != null)
			startDateJSpinner.setValue(minimumDate);
		if (maximumDate != null)
			endDateJSPinner.setValue(maximumDate);
	}

	public void load(JPanel panel_2) {
		startDateJSpinner = (JSpinner) panel_2.getComponent(2);
		endDateJSPinner = (JSpinner) panel_2.getComponent(3);
		// scrollPane.setV
		panel.removeAll();
		Dimension panelSize = this.getSize();
		add(scrollPane, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		// scrollPane.set trying to replace scroll where it was
		JCheckBox relativeCheckBox = (JCheckBox) panel_2.getComponent(5);
		estimatedTime = System.currentTimeMillis() - startTime;
		logger.info("gathering time: " + estimatedTime);
		startTime = System.currentTimeMillis();
		// Persister.save(new File("/home/fred/git/LogDruid/mineResultSet"),
		// mineResultSet);
		Iterator mineResultSetIterator = mineResultSet.mineResults.entrySet().iterator();

		logger.info("mineResultSet size: " + mineResultSet.mineResults.size());
		while (mineResultSetIterator.hasNext()) {
			Map.Entry pairs = (Map.Entry) mineResultSetIterator.next();
			logger.info("mineResultSet key/source: " + pairs.getKey());
			Vector<MineResult> mrVector = (Vector<MineResult>) pairs.getValue();
			Iterator mrVectorIterator = mrVector.iterator();
			// Collections.sort(mrVectorIterator);
			while (mrVectorIterator.hasNext()) {
				MineResult mr = (MineResult) mrVectorIterator.next();
				HashMap<String, TimeSeries> statHashMap = mr.getStatTimeseriesHashMap();
				HashMap<String, TimeSeries> eventHashMap = mr.getEventTimeseriesHashMap();
				// logger.info("mineResultSet hash size: "
				// +mr.getTimeseriesHashMap().size());
				// logger.info("mineResultSet hash content: " +
				// mr.getStatTimeseriesHashMap());
				logger.info("mineResultSet mr.getStartDate(): " + mr.getStartDate());
				logger.info("mineResultSet mr.getEndDate(): " + mr.getEndDate());
				logger.debug("mineResultSet (Date)jsp.getValue(): " + (Date) startDateJSpinner.getValue());
				logger.debug("mineResultSet (Date)jsp2.getValue(): " + (Date) endDateJSPinner.getValue());
				if (mr.getStartDate() != null && mr.getEndDate() != null) {
					if ((mr.getStartDate().before((Date) endDateJSPinner.getValue())) && (mr.getEndDate().after((Date) startDateJSpinner.getValue()))) {
						Iterator statHashMapIterator = statHashMap.entrySet().iterator();
						if (!statHashMap.entrySet().isEmpty() || !eventHashMap.entrySet().isEmpty()) {
							JPanel checkboxPanel = new JPanel(new FlowLayout());
							
							checkboxPanel.setBackground(Color.white);

							int count = 1;
							chart = ChartFactory.createXYAreaChart(// Title
									mr.getSourceID() + " " + mr.getGroup(),// +
									null, // X-Axis
									// label
									null, // Y-Axis label
									null, // Dataset
									PlotOrientation.VERTICAL, false, // Show
																		// legend
									true, // tooltips
									false // url
									);
							TextTitle my_Chart_title=new TextTitle(mr.getSourceID() + " " + mr.getGroup(), new Font ("Verdana", Font.BOLD , 17));
							chart.setTitle(my_Chart_title);
			                
							XYPlot plot = (XYPlot) chart.getPlot();
							ValueAxis range = plot.getRangeAxis();
							range.setVisible(false);
							
							final DateAxis domainAxis1 = new DateAxis();
							domainAxis1.setTickLabelsVisible(true);
							// domainAxis1.setTickMarksVisible(true);

							logger.debug("getRange: " + domainAxis1.getRange());
							if (relativeCheckBox.isSelected()) {
								domainAxis1.setRange((Date) startDateJSpinner.getValue(), (Date) endDateJSPinner.getValue());
							} else {
								Date startDate = mr.getStartDate();
								Date endDate = mr.getEndDate();
								if (mr.getStartDate().before((Date) startDateJSpinner.getValue())) {
									startDate = (Date) startDateJSpinner.getValue();
									logger.debug("setMinimumDate: " + (Date) startDateJSpinner.getValue());
								}
								if (mr.getEndDate().after((Date) endDateJSPinner.getValue())) {
									endDate = (Date) endDateJSPinner.getValue();
									logger.debug("setMaximumDate: " + (Date) endDateJSPinner.getValue());
								}
								if (startDate.before(endDate)) {
									domainAxis1.setRange(startDate, endDate);
								}
							}
							while (statHashMapIterator.hasNext()) {

								TimeSeriesCollection dataset = new TimeSeriesCollection();
								Map.Entry me = (Map.Entry) statHashMapIterator.next();
								TimeSeries ts = (TimeSeries) me.getValue();

								dataset.addSeries(ts);
								logger.debug("getRange: " + domainAxis1.getRange());
								logger.info("mineResultSet group: " + mr.getGroup() + ", key: " + me.getKey() + " nb records: "
										+ ((TimeSeries) me.getValue()).getItemCount());
								logger.debug("(((TimeSeries) me.getValue()).getMaxY(): " + (((TimeSeries) me.getValue()).getMaxY()));
								logger.debug("(((TimeSeries) me.getValue()).getMinY(): " + (((TimeSeries) me.getValue()).getMinY()));
								XYPlot plot1 = chart.getXYPlot();

								NumberAxis axis4 = new NumberAxis(me.getKey().toString());
								axis4.setAutoRange(true);
								axis4.setAxisLineVisible(true);
								axis4.setAutoRangeIncludesZero(false);
								axis4.setRange(new Range(((TimeSeries) me.getValue()).getMinY(), ((TimeSeries) me.getValue()).getMaxY()));
								axis4.setLabelPaint(colors[count]);
								axis4.setTickLabelPaint(colors[count]);
								plot1.setRangeAxis(count, axis4);
								final ValueAxis domainAxis = domainAxis1;
								domainAxis.setLowerMargin(0.0);
								domainAxis.setUpperMargin(0.0);
								plot1.setDomainAxis(domainAxis);
								plot1.setForegroundAlpha(0.5f);
								plot1.setDataset(count, dataset);
								plot1.mapDatasetToRangeAxis(count, count);
								final XYAreaRenderer renderer = new XYAreaRenderer(); // XYAreaRenderer2
																						// also
																						// nice
								if ((((TimeSeries) me.getValue()).getMaxY() - ((TimeSeries) me.getValue()).getMinY()) > 0) {
								//	   FileReferenceToolTipGenerator myxytooltipgenerator = new FileReferenceToolTipGenerator();
								//	      renderer.setToolTipGenerator(myxytooltipgenerator);
									//renderer.setToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
										//	new SimpleDateFormat("d-MMM-yyyy HH:mm:ss"), new DecimalFormat("#,##0.00")));
									renderer.setToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,new SimpleDateFormat("d-MMM-yyyy HH:mm:ss"), new DecimalFormat("#,##0.00")));
								}
								renderer.setSeriesPaint(0, colors[count]);
								renderer.setSeriesVisible(0, true);
								plot1.setRenderer(count, renderer);

								JCheckBox jcb = new JCheckBox(new VisibleAction(renderer, me.getKey().toString(), 0));
								jcb.setSelected(true);
								jcb.setBackground(Color.white);
								jcb.setBorderPainted(true);
								jcb.setBorder(BorderFactory.createLineBorder(colors[count], 1, true));
								jcb.setFont(new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize()));
								checkboxPanel.add(jcb);

								count++;
							}
							Iterator eventHashMapIterator = eventHashMap.entrySet().iterator();
							while (eventHashMapIterator.hasNext()) {
								TimeSeriesCollection dataset = new TimeSeriesCollection();
								Map.Entry me = (Map.Entry) eventHashMapIterator.next();
								dataset.addSeries((TimeSeries) me.getValue());

								logger.info("mineResultSet group: " + mr.getGroup() + ", key: " + me.getKey() + " nb records: "
										+ ((TimeSeries) me.getValue()).getItemCount());
								logger.debug("mineResultSet hash content: " + mr.getEventTimeseriesHashMap());
								logger.debug("(((TimeSeries) me.getValue()).getMaxY(): " + (((TimeSeries) me.getValue()).getMaxY()));
								logger.debug("(((TimeSeries) me.getValue()).getMinY(): " + (((TimeSeries) me.getValue()).getMinY()));
								XYPlot plot2 = chart.getXYPlot();

								NumberAxis axis4 = new NumberAxis(me.getKey().toString());
								axis4.setAutoRange(true);
								axis4.setAxisLineVisible(true);
								axis4.setAutoRangeIncludesZero(true);
								// axis4.setRange(new Range(((TimeSeries)
								axis4.setLabelPaint(colors[count]);
								axis4.setTickLabelPaint(colors[count]);
								plot2.setRangeAxis(count, axis4);
								final ValueAxis domainAxis = domainAxis1;
								domainAxis.setLowerMargin(0.0);
								domainAxis.setUpperMargin(0.0);
								plot2.setDomainAxis(domainAxis);
								plot2.setForegroundAlpha(0.5f);
								plot2.setDataset(count, dataset);
								plot2.mapDatasetToRangeAxis(count, count);
								XYBarRenderer rend = new XYBarRenderer(); // XYErrorRenderer
																			// ??
								rend.setShadowVisible(false);
								rend.setDrawBarOutline(true);
								Stroke stroke = new BasicStroke(5);
								rend.setBaseStroke(stroke);
								final XYItemRenderer renderer = rend;
								renderer.setToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
										new SimpleDateFormat("d-MMM-yyyy HH:mm:ss"), new DecimalFormat("#,##0.00")));

								// renderer.setItemLabelsVisible(true);
								renderer.setSeriesPaint(0, colors[count]);
								renderer.setSeriesVisible(0, true);
								plot2.setRenderer(count, renderer);

								JCheckBox jcb = new JCheckBox(new VisibleAction(rend, me.getKey().toString(), 0));
								jcb.setSelected(true);
								jcb.setBackground(Color.white);
								jcb.setBorderPainted(true);
								jcb.setBorder(BorderFactory.createLineBorder(colors[count], 1, true));
								jcb.setFont(new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize()));
								checkboxPanel.add(jcb);
								count++;
							}

							JPanel pan = new JPanel();
							pan.setLayout(new BorderLayout());
							pan.setPreferredSize(new Dimension(600, 350));
							// pan.setPreferredSize(panelSize);
							panel.add(pan);
							ChartPanel cpanel = new ChartPanel(chart);
							cpanel.setMinimumDrawWidth(0);
							cpanel.setMinimumDrawHeight(0);
							cpanel.setMaximumDrawWidth(1920);
							cpanel.setMaximumDrawHeight(1200);
							// cpanel.setInitialDelay(0);
							cpanel.setPreferredSize(new Dimension(600, 350));
							panel.add(new JSeparator(SwingConstants.HORIZONTAL));
							pan.add(cpanel, BorderLayout.CENTER);
							// checkboxPanel.setPreferredSize(new Dimension(600,
							// 0));

							pan.add(checkboxPanel, BorderLayout.SOUTH);

						}
					}
				} else {
					logger.error("mr dates null: " + mr.getGroup() + mr.getSourceID() + mr.getLogFiles());
				}
			}
		}
		// hashMap=miner.mine(sourceFiles,repo);
		estimatedTime = System.currentTimeMillis() - startTime;

		revalidate();
		logger.info("display time: " + estimatedTime);
	}

	private static class VisibleAction extends AbstractAction {

		private XYItemRenderer renderer;
		private int i;

		public VisibleAction(XYItemRenderer renderer, String name, int i) {
			super(name);
			this.renderer = renderer;
			this.i = i;
		}

		public VisibleAction(XYBarRenderer renderer, String name, int i) {
			super(name);
			this.renderer = renderer;
			this.i = i;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			renderer.setSeriesVisible(i, !renderer.getSeriesVisible(i));
		}

	}
}
