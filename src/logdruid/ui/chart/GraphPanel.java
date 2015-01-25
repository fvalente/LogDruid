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

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

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
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
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

import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;

import logdruid.data.DataVault;
import logdruid.data.ExtendedTimeSeries;
import logdruid.data.MineResult;
import logdruid.data.MineResultSet;
import logdruid.data.Repository;
import logdruid.data.record.Recording;
import logdruid.ui.WrapLayout;
import logdruid.util.AlphanumComparator;
import logdruid.util.DataMiner;
import logdruid.util.Persister;

import javax.swing.JScrollPane;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import javax.swing.BoxLayout;

public final class GraphPanel extends JPanel {
	private Date minimumDate;
	private Date maximumDate;
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private JScrollPane scrollPane;
	private JPanel panel;
	private Repository repo;
	private Color[] colors = { Color.blue, new Color(65, 90, 220), new Color(70, 200, 62), new Color(171, 130, 255), new Color(255, 40, 40),
			new Color(0, 205, 205), Color.magenta, Color.orange, Color.pink, new Color(65, 90, 220), new Color(107, 255, 102), new Color(0, 178, 238),
			new Color(60, 179, 113) };
	// private Color[] colors = { new Color(65,171,93),new Color(254,196,79),new
	// Color(65,171,93), new Color(239,59,44), new Color(65,182,196),new
	// Color(5,112,176), new Color(254,178,76),Color.blue, new Color(255, 40,
	// 40), new Color(65, 90, 220), new Color(70, 255, 62), Color.magenta,
	// Color.orange, Color.pink,
	// new Color(65, 90, 220), new Color(107, 255, 102) };

	JSpinner startDateJSpinner;
	JSpinner endDateJSPinner;
	MineResultSet mineResultSet;

	long estimatedTime = 0;
	long startTime = 0;

	XYPlot plot = null;
	JFreeChart chart = null;
	final StandardChartTheme chartTheme = (StandardChartTheme) org.jfree.chart.StandardChartTheme.createJFreeTheme();
	final Font oldSmallFont = chartTheme.getSmallFont();

	final DecimalFormat form = new DecimalFormat("#,##0.00");

	// new DecimalFormat("00.0");

	/**
	 * Create the panel.
	 * 
	 * @param panel_2
	 */

	public GraphPanel(final Repository repo, JPanel panel_2) {

		setLayout(new BorderLayout(0, 0));
		startTime = System.currentTimeMillis();
		this.repo=repo;
		mineResultSet = DataMiner.gatherMineResultSet(repo);
		DataVault.mineResultSet = mineResultSet;
		panel = new JPanel();
		if (mineResultSet == null) {
			panel.add(new JLabel(" No Data "), BorderLayout.CENTER);
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
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);
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
			Map mrArrayList = (Map<String, MineResult>) pairs.getValue();

			ArrayList<String> mineResultGroup = new ArrayList<String>();
			Set<String> mrss = mrArrayList.keySet();
			mineResultGroup.addAll(mrss);
			Collections.sort(mineResultGroup, new AlphanumComparator());
			Iterator mineResultGroupIterator = mineResultGroup.iterator();

			// Collections.sort(mrArrayList);
			Iterator mrArrayListIterator = mineResultGroup.iterator();
			while (mrArrayListIterator.hasNext()) {

				String key = (String) mrArrayListIterator.next();
				final MineResult mr = (MineResult) mrArrayList.get(key);
				Map<String, ExtendedTimeSeries> statMap = mr.getStatTimeseriesMap();
				Map<String, ExtendedTimeSeries> eventMap = mr.getEventTimeseriesMap();
				// logger.info("mineResultSet hash size: "
				// +mr.getTimeseriesMap().size());
				// logger.info("mineResultSet hash content: " +
				// mr.getStatTimeseriesMap());
				logger.debug("mineResultSet mr.getStartDate(): " + mr.getStartDate() + " mineResultSet mr.getEndDate(): " + mr.getEndDate());
				logger.debug("mineResultSet (Date)jsp.getValue(): " + (Date) startDateJSpinner.getValue());
				logger.debug("mineResultSet (Date)jsp2.getValue(): " + (Date) endDateJSPinner.getValue());
				if (mr.getStartDate() != null && mr.getEndDate() != null) {
					if ((mr.getStartDate().before((Date) endDateJSPinner.getValue())) && (mr.getEndDate().after((Date) startDateJSpinner.getValue()))) {
						Iterator statMapIterator = statMap.entrySet().iterator();
						if (!statMap.entrySet().isEmpty() || !eventMap.entrySet().isEmpty()) {
							JPanel checkboxPanel = new JPanel(new WrapLayout());

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
							TextTitle my_Chart_title = new TextTitle(mr.getSourceID() + " " + mr.getGroup(), new Font("Verdana", Font.BOLD, 17));
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
							XYToolTipGenerator tt1 = new XYToolTipGenerator() {
								public String generateToolTip(XYDataset dataset, int series, int item) {
									StringBuffer sb = new StringBuffer();
									String htmlStr = "<html>";
									Number x;
									FastDateFormat sdf = FastDateFormat.getInstance("dd-MMM-yyyy HH:mm:ss");
									x = dataset.getX(series, item);
									sb.append(htmlStr);
									if (x != null) {
										sb.append("<p style='color:#000000;'>" + (sdf.format(x)) + "</p>");
										sb.append("<p style='color:#000000;'>" + dataset.getSeriesKey(series).toString() + ": "
												+ form.format(dataset.getYValue(0, item)) + "</p>");
										sb.append("<p style='color:#0000FF;'>" + mr.getFileForDate(new Date(x.longValue())) + "</p>");
									}
									return sb.toString();
								}
							};

							while (statMapIterator.hasNext()) {

								TimeSeriesCollection dataset = new TimeSeriesCollection();
								Map.Entry me = (Map.Entry) statMapIterator.next();
								ExtendedTimeSeries ts = (ExtendedTimeSeries) me.getValue();
								// logger.info(((TimeSeries)
								// me.getValue()).getMaxY());
								if (((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMaxY() > 0)
									dataset.addSeries(ts.getTimeSeries());
								logger.debug("mineResultSet group: " + mr.getGroup() + ", key: " + me.getKey() + " nb records: "
										+ ((ExtendedTimeSeries) me.getValue()).getTimeSeries().getItemCount());
								logger.debug("(((TimeSeries) me.getValue()).getMaxY(): " + (((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMaxY()));
								logger.debug("(((TimeSeries) me.getValue()).getMinY(): " + (((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMinY()));
								XYPlot plot1 = chart.getXYPlot();
						//		LogarithmicAxis axis4 = new LogarithmicAxis(me.getKey().toString());
								NumberAxis axis4 = new NumberAxis(me.getKey().toString());
								axis4.setAutoRange(true);
								axis4.setAxisLineVisible(true);
								axis4.setAutoRangeIncludesZero(false);
								plot1.setDomainCrosshairVisible(true);
								plot1.setRangeCrosshairVisible(true);
								axis4.setRange(new Range(((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMinY(), ((ExtendedTimeSeries) me.getValue())
										.getTimeSeries().getMaxY()));
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
								if ((((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMaxY() - ((ExtendedTimeSeries) me.getValue()).getTimeSeries()
										.getMinY()) > 0) {

									// renderer.setToolTipGenerator(new
									// StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,new
									// FastDateFormat("d-MMM-yyyy HH:mm:ss"),
									// new DecimalFormat("#,##0.00")));
								}
								renderer.setSeriesPaint(0, colors[count]);
								renderer.setSeriesVisible(0, true);
								renderer.setSeriesToolTipGenerator(0, tt1);
								plot1.setRenderer(count, renderer);
								int hits = 0; // ts.getStat()[1]
								if (((ExtendedTimeSeries) me.getValue()).getStat() != null) {
									hits = ((ExtendedTimeSeries) me.getValue()).getStat()[1];
								}
								JCheckBox jcb = new JCheckBox(new VisibleAction(renderer, me.getKey().toString() + "(" + hits + ")", 0));
								Boolean selected = true;
								jcb.setSelected(true);
								jcb.setBackground(Color.white);
								jcb.setBorderPainted(true);
								jcb.setBorder(BorderFactory.createLineBorder(colors[count], 1, true));
								jcb.setFont(new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize()));
								checkboxPanel.add(jcb);
								count++;
							}
							Iterator eventMapIterator = eventMap.entrySet().iterator();
							while (eventMapIterator.hasNext()) {
								TimeSeriesCollection dataset = new TimeSeriesCollection();
								Map.Entry me = (Map.Entry) eventMapIterator.next();
								// if (dataset.getEndXValue(series, item))
								if (((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMaxY() > 0)
									dataset.addSeries(((ExtendedTimeSeries) me.getValue()).getTimeSeries());

								logger.debug("mineResultSet group: " + mr.getGroup() + ", key: " + me.getKey() + " nb records: "
										+ ((ExtendedTimeSeries) me.getValue()).getTimeSeries().getItemCount());
								logger.debug("mineResultSet hash content: " + mr.getEventTimeseriesMap());
								logger.debug("(((TimeSeries) me.getValue()).getMaxY(): " + (((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMaxY()));
								logger.debug("(((TimeSeries) me.getValue()).getMinY(): " + (((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMinY()));
								XYPlot plot2 = chart.getXYPlot();
							//	LogarithmicAxis axis4 = new LogarithmicAxis(me.getKey().toString());
								NumberAxis axis4 = new NumberAxis(me.getKey().toString());
								axis4.setAutoRange(true);
								// axis4.setInverted(true);
								axis4.setAxisLineVisible(true);
								axis4.setAutoRangeIncludesZero(true);

								// axis4.setRange(new Range(((TimeSeries)
								// axis4.setRange(new Range(((TimeSeries)
								// me.getValue()).getMinY(), ((TimeSeries)
								// me.getValue()).getMaxY()));
								axis4.setLabelPaint(colors[count]);
								axis4.setTickLabelPaint(colors[count]);
								plot2.setRangeAxis(count, axis4);
								final ValueAxis domainAxis = domainAxis1;

								// domainAxis.setLowerMargin(0.001);
								// domainAxis.setUpperMargin(0.0);
								plot2.setDomainCrosshairVisible(true);
								plot2.setRangeCrosshairVisible(true);
								plot2.setDomainAxis(domainAxis);
								plot2.setForegroundAlpha(0.5f);
								plot2.setDataset(count, dataset);
								plot2.mapDatasetToRangeAxis(count, count);
								XYBarRenderer rend = new XYBarRenderer(); // XYErrorRenderer
																			//
								rend.setShadowVisible(false);
								rend.setDrawBarOutline(true);
								Stroke stroke = new BasicStroke(5);
								rend.setBaseStroke(stroke);
								final XYItemRenderer renderer = rend;
								renderer.setSeriesToolTipGenerator(0, tt1);
								// renderer.setItemLabelsVisible(true);
								renderer.setSeriesPaint(0, colors[count]);
								renderer.setSeriesVisible(0, true);
								plot2.setRenderer(count, renderer);
								int hits = 0;
								if (((ExtendedTimeSeries) me.getValue()).getStat() != null) {
									hits = ((ExtendedTimeSeries) me.getValue()).getStat()[1];
								}
								// ((ExtendedTimeSeries)
								// me.getValue()).getStat()[1]
								JCheckBox jcb = new JCheckBox(new VisibleAction(rend, me.getKey().toString() + "(" + hits + ")", 0));
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
							pan.setPreferredSize(new Dimension(600, Integer.parseInt((String)repo.getPreference("chartSize"))));
							// pan.setPreferredSize(panelSize);
							panel.add(pan);
							final ChartPanel cpanel = new ChartPanel(chart);
							cpanel.setMinimumDrawWidth(0);
							cpanel.setMinimumDrawHeight(0);
							cpanel.setMaximumDrawWidth(1920);
							cpanel.setMaximumDrawHeight(1200);
							// cpanel.setInitialDelay(0);
							cpanel.setPreferredSize(new Dimension(600, 350));
							// cpanel.restoreAutoBounds(); fix the tooltip
							// missing problem but then relative display is
							// broken
							panel.add(new JSeparator(SwingConstants.HORIZONTAL));
							pan.add(cpanel, BorderLayout.CENTER);
							// checkboxPanel.setPreferredSize(new Dimension(600,
							// 0));
							cpanel.addChartMouseListener(new ChartMouseListener() {

								public void chartMouseClicked(ChartMouseEvent chartmouseevent) {
									// chartmouseevent.getEntity().

									ChartEntity entity = chartmouseevent.getEntity();
									if (entity instanceof XYItemEntity) {
										XYItemEntity item = ((XYItemEntity) entity);
										if (item.getDataset() instanceof TimeSeriesCollection) {
											TimeSeriesCollection data = (TimeSeriesCollection) item.getDataset();
											TimeSeries series = data.getSeries(item.getSeriesIndex());
											TimeSeriesDataItem dataitem = series.getDataItem(item.getItem());

											// logger.info(" Serie: "+series.getKey().toString()
											// +
											// " Period : "+dataitem.getPeriod().toString());
											// mr.getFileForDate(new Date
											// (x.longValue())
											;
											int x = chartmouseevent.getTrigger().getX();
											// logger.info(mr.getFileForDate(dataitem.getPeriod().getEnd()));
											int y = chartmouseevent.getTrigger().getY();
											String myString = "";
											if (dataitem.getPeriod() != null) {
												myString = mr.getFileForDate(dataitem.getPeriod().getEnd()).toString();
												StringSelection stringSelection = new StringSelection(myString);
												Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
												clpbrd.setContents(stringSelection, null);
												cpanel.getGraphics().drawString("file name copied", x - 5, y - 5);
												try {
													Thread.sleep(500);
												} catch (InterruptedException e) {
													// TODO Auto-generated catch
													// block
													e.printStackTrace();
												}
											}

											// logger.info(mr.getFileForDate(dataitem.getPeriod().getStart()));
										}
									}
								}

								public void chartMouseMoved(ChartMouseEvent e) {
								}

							});

							pan.add(checkboxPanel, BorderLayout.SOUTH);

						}
					}
				} else {
					logger.debug("mr dates null: " + mr.getGroup() + mr.getSourceID() + mr.getLogFiles());
				}
			}
		}
		// Map=miner.mine(sourceFiles,repo);
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
