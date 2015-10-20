/*******************************************************************************
 * LogDruid : chart statistics and events retrieved in logs files through configurable regular expressions
 * Copyright (C) 2014, 2015 Frederic Valente (frederic.valente@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package logdruid.ui.chart;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.TreeMap;
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
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
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
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

//import sun.awt.X11.ColorData;

import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;

//import logdruid.data.DataVault;
import logdruid.data.ExtendedTimeSeries;
import logdruid.data.Preferences;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.mine.ChartData;
import logdruid.data.mine.DataVault;
import logdruid.data.mine.FileLine;
import logdruid.data.mine.MineResult;
import logdruid.data.mine.MineResultSet;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.ui.MainFrame;
import logdruid.ui.WrapLayout;
import logdruid.util.AlphanumComparator;
import logdruid.util.DataMiner;
import logdruid.util.Persister;

import javax.swing.JScrollPane;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.BoxLayout;

public class GraphPanel extends JPanel {
	private Date minimumDate;
	private Date maximumDate;
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private JScrollPane scrollPane;
	private JPanel panel;
	private JPanel panel_1;
	private Repository repo;
	String htmlStr = "<html>";
	private Color[] colors = { Color.blue, new Color(65, 90, 220), new Color(70, 200, 62), new Color(171, 130, 255), new Color(255, 40, 40),
			new Color(0, 205, 205), Color.magenta, Color.orange, Color.pink, new Color(65, 90, 220), new Color(107, 255, 102), new Color(0, 178, 238),
			new Color(60, 179, 113),new Color(179,60 , 113),new Color(179,113, 60 ),new Color(70, 62, 200), new Color(255,171, 130 ), new Color( 40, 255,40),
			new Color(65,171,93),new Color(239,59,44), new Color(65,182,196),new Color(254,178,76),new Color(180,180,180),new Color(93,65,171) };
	// private Color[] colors = { new Color(65,171,93),new Color(254,196,79),new
	// Color(65,171,93), new Color(239,59,44), new Color(65,182,196),new
	// Color(5,112,176), new Color(254,178,76),Color.blue, new Color(255, 40,
	// 40), new Color(65, 90, 220), new Color(70, 255, 62), Color.magenta,
	// Color.orange, Color.pink,
	// new Color(65, 90, 220), new Color(107, 255, 102) };
	private HashMap<String, Boolean> groupCheckBox = new HashMap<String, Boolean>();
	MineResultSet mineResultSet;
	ChartData cd;
	JSpinner startDateJSpinner;
	JSpinner endDateJSPinner;

	long estimatedTime = 0;
	long startTime = 0;

	XYPlot plot = null;
	JFreeChart chart = null;
	final StandardChartTheme chartTheme = (StandardChartTheme) org.jfree.chart.StandardChartTheme.createJFreeTheme();
	final Font oldSmallFont = chartTheme.getSmallFont();

	final DecimalFormat form = new DecimalFormat("#,##0.00");
	//ArrayList<String> disabledSeries;
	private JPanel panel_2;
	// new DecimalFormat("00.0");
	static MainFrame mainFrame1;
	/**
	 * Create the panel.
	 * 
	 * @param panel_2
	 */

	public GraphPanel(final Repository repo, final JPanel panel_2, final MineResultSet mineResultSet1, final ChartData cd1, final MainFrame _mainFrame) {


		this.repo=repo;
		this.panel_2=panel_2;
		mineResultSet=mineResultSet1;
		cd=cd1;
		mainFrame1=_mainFrame;
		init();
	}
	
	public void init(){
		setLayout(new BorderLayout(0, 0));
		startTime = System.currentTimeMillis();		
		panel_1 = new JPanel(new WrapLayout());
		add(panel_1, BorderLayout.NORTH);
		
		//DataVault.mineResultSet = mineResultSet;
		panel = new JPanel();
		if (mineResultSet.mineResults.isEmpty()) {
			logger.info("mineResultSet null");
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
			loadGroupCheckbox(panel_2);
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

	public void loadGroupCheckbox(final JPanel panel_2){
		panel_1.removeAll();
		//Iterator mineResultSetIterator = mineResultSet.mineResults.entrySet().iterator();
		
		Map<Source, Map<String, MineResult>> treeMap=new TreeMap<Source, Map<String, MineResult>>(mineResultSet.mineResults) ;
		Iterator mineResultSetIterator = treeMap.entrySet().iterator();
		
		logger.debug("mineResultSet size: " + mineResultSet.mineResults.size());
		while (mineResultSetIterator.hasNext()) {
			
			int groupCount=0;
			int totalGroupCount=0;
			Map.Entry pairs = (Map.Entry) mineResultSetIterator.next();
			Map mrArrayList = (Map<String, MineResult>) pairs.getValue();
			ArrayList<String> mineResultGroup = new ArrayList<String>();
			Set<String> mrss = mrArrayList.keySet();
			mineResultGroup.addAll(mrss);
			Collections.sort(mineResultGroup, new AlphanumComparator());

			Iterator mrArrayListIterator = mineResultGroup.iterator();
			while (mrArrayListIterator.hasNext()) {
				String key = (String) mrArrayListIterator.next();
				final MineResult mr = (MineResult) mrArrayList.get(key);
				Map<String, ExtendedTimeSeries> statMap = mr.getStatTimeseriesMap();
				Map<String, ExtendedTimeSeries> eventMap = mr.getEventTimeseriesMap();
				if (!statMap.entrySet().isEmpty() || !eventMap.entrySet().isEmpty()) {
				if (mr.getStartDate() != null && mr.getEndDate() != null) {
					if ((mr.getStartDate().before((Date) endDateJSPinner.getValue())) && (mr.getEndDate().after((Date) startDateJSpinner.getValue()))) {
						groupCount++;
						}
					}}
			}
			Iterator mrArrayListIterator2 = mineResultGroup.iterator();
			while (mrArrayListIterator2.hasNext()) {
				String key = (String) mrArrayListIterator2.next();
				final MineResult mr = (MineResult) mrArrayList.get(key);
				Map<String, ExtendedTimeSeries> statMap = mr.getStatTimeseriesMap();
				Map<String, ExtendedTimeSeries> eventMap = mr.getEventTimeseriesMap();
				if (!statMap.entrySet().isEmpty() || !eventMap.entrySet().isEmpty()) {
				if (mr.getStartDate() != null && mr.getEndDate() != null) {
					if ((mr.getStartDate().before((Date) maximumDate)) && (mr.getEndDate().after((Date) minimumDate))) {
						totalGroupCount++;
						}
					}}
			}
			boolean selected=true;
			if (groupCheckBox.containsKey(((Source)pairs.getKey()).getSourceName())){
				selected=groupCheckBox.get(((Source)pairs.getKey()).getSourceName());
			} else {
				groupCheckBox.put(((Source)pairs.getKey()).getSourceName(),selected);
				
			}
			
			JCheckBox chckbxGroup = new JCheckBox(((Source)pairs.getKey()).getSourceName()+"("+groupCount+"/"+totalGroupCount+")");
			chckbxGroup.setFont(new Font("Dialog", Font.BOLD, 11));
			chckbxGroup.setSelected(selected);				
			panel_1.add(chckbxGroup);
			chckbxGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JCheckBox chkBox= ((JCheckBox)e.getSource());
					groupCheckBox.put((String)chkBox.getText().substring(0, ((String)chkBox.getText()).indexOf("(")),!groupCheckBox.get((String)chkBox.getText().substring(0, ((String)chkBox.getText()).indexOf("("))));
					loadGroupCheckbox(panel_2);
					load(panel_2);
					//logger.info("checkBox:"+ chkBox.getText()+","+chkBox.isSelected()+","+groupCheckBox);
					//logger.info("checkBox2:"+((String)chkBox.getText())+", "+((String)chkBox.getText()).indexOf("(")+", "+groupCheckBox.get((String)chkBox.getText().substring(0, ((String)chkBox.getText()).indexOf("(")))); 
				}
			});
		}
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
		startTime = System.currentTimeMillis();
		Map<Source, Map<String, MineResult>> treeMap=new TreeMap<Source, Map<String, MineResult>>(mineResultSet.mineResults) ;
		Iterator mineResultSetIterator = treeMap.entrySet().iterator();
		int ite=0;
		logger.debug("mineResultSet size: " + mineResultSet.mineResults.size());
		while (mineResultSetIterator.hasNext()) {
			final Map.Entry pairs = (Map.Entry) mineResultSetIterator.next();
			logger.debug("mineResultSet key/source: " + ((Source)pairs.getKey()).getSourceName());
			JCheckBox checkBox=(JCheckBox)panel_1.getComponent(ite++);
			logger.debug("checkbox: "+checkBox.getText()+", "+ checkBox.isSelected() );
			if (checkBox.isSelected()){
				
			Map mrArrayList = (Map<String, MineResult>) pairs.getValue();
			ArrayList<String> mineResultGroup = new ArrayList<String>();
			Set<String> mrss = mrArrayList.keySet();
			
			mineResultGroup.addAll(mrss);
			Collections.sort(mineResultGroup, new AlphanumComparator());
			Iterator mrArrayListIterator = mineResultGroup.iterator();
			while (mrArrayListIterator.hasNext()) {

				String key = (String) mrArrayListIterator.next();
				logger.debug(key);
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
						
						ArrayList<String> mineResultGroup2 = new ArrayList<String>();
						Set<String> mrss2 = statMap.keySet();
						mineResultGroup2.addAll(mrss2);
						Collections.sort(mineResultGroup2, new AlphanumComparator());
						Iterator statMapIterator = mineResultGroup2.iterator();
						
						if (!statMap.entrySet().isEmpty() || !eventMap.entrySet().isEmpty()) {
							JPanel checkboxPanel = new JPanel(new WrapLayout());

							checkboxPanel.setBackground(Color.white);

							int count = 1;
							chart = ChartFactory.createXYAreaChart(// Title
									"",// +
									null, // X-Axis
									// label
									null, // Y-Axis label
									null, // Dataset
									PlotOrientation.VERTICAL, false, // Show
																		// legend
									true, // tooltips
									false // url
									);
			//				TextTitle my_Chart_title = new TextTitle(mr.getSourceID() + " " + mr.getGroup(), new Font("Verdana", Font.BOLD, 17));

							
							final DateAxis domainAxis1 = new DateAxis();
							domainAxis1.setTickLabelsVisible(true);
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
									Number x= dataset.getX(series, item);
									FastDateFormat sdf = FastDateFormat.getInstance("dd-MMM-yyyy HH:mm:ss");
									sb.append(htmlStr);
									if (x != null) {
										sb.append((sdf.format(x)));
										sb.append("<br>");
										sb.append(dataset.getSeriesKey(series).toString());
										sb.append(": ");
										sb.append(form.format(dataset.getYValue(0, item)) );
										if (mr.getFileLineForDate(new Date(x.longValue()),dataset.getSeriesKey(series).toString())!=null){
											sb.append("<p style='color:#0000FF;'>");
											sb.append(cd.sourceFileArrayListMap.get(pairs.getKey()).get(mr.getFileLineForDate(new Date(x.longValue()),
												dataset.getSeriesKey(series).toString()).getFileId()).getFile().getName());
											sb.append(":");
											sb.append(mr.getFileLineForDate(new Date(x.longValue()),dataset.getSeriesKey(series).toString()).getLineNumber());
											sb.append("</p>");
										
										}
									}
									return sb.toString();
								}
							};

							XYPlot plot1 = (XYPlot) chart.getXYPlot();
							ValueAxis range = plot1.getRangeAxis();
							range.setVisible(false);
							final ValueAxis domainAxis = domainAxis1;
							domainAxis.setLowerMargin(0.0);
							domainAxis.setUpperMargin(0.0);
							plot1.setDomainAxis(0,domainAxis);
							plot1.setDomainCrosshairVisible(true);
							plot1.setRangeCrosshairVisible(true);
							plot1.mapDatasetToDomainAxis(0, 1);
							while (statMapIterator.hasNext()) {

								TimeSeriesCollection dataset = new TimeSeriesCollection();
								String me = (String) statMapIterator.next();
								
								ExtendedTimeSeries ts = (ExtendedTimeSeries) statMap.get(me);
						//		if (ts.getTimeSeries().getMaxY() > 0)
									dataset.addSeries(ts.getTimeSeries());
								logger.debug("mineResultSet group: " + mr.getGroup() + ", key: " + me + " nb records: "
										+ ts.getTimeSeries().getItemCount());
								logger.debug("(((TimeSeries) me.getValue()).getMaxY(): " + (ts.getTimeSeries().getMaxY()));
								logger.debug("(((TimeSeries) me.getValue()).getMinY(): " + (ts.getTimeSeries().getMinY()));
								 
							//	LogarithmicAxis axis4 = new LogarithmicAxis(me.toString());
								NumberAxis axis4 = new NumberAxis(me.toString());
								axis4.setAutoRange(true);
								axis4.setAxisLineVisible(true);
								axis4.setAutoRangeIncludesZero(false);
								axis4.setRange(new Range(ts.getTimeSeries().getMinY(), ts
										.getTimeSeries().getMaxY()));
								if (count<colors.length){
								axis4.setLabelPaint(colors[count]);
								axis4.setTickLabelPaint(colors[count]);
								}
								plot1.setRangeAxis(count, axis4);
								axis4.setVisible(((RecordingItem)ts.getRecordingItem()).isShow());
								plot1.setForegroundAlpha(0.3f);
								if (ts.getTimeSeries().getMaxY()!=ts.getTimeSeries().getMinY()){
								plot1.setDataset(count, dataset);
								}
								plot1.mapDatasetToRangeAxis(count, count);
								final XYAreaRenderer renderer = new XYAreaRenderer(); // XYAreaRenderer2
																						// also
																						// nice
								if (count<colors.length){
								renderer.setSeriesPaint(0, colors[count]);
								}
								logger.debug(((RecordingItem)ts.getRecordingItem()).getName());
								renderer.setSeriesVisible(0, ((RecordingItem)ts.getRecordingItem()).isShow()); //!cd.disabledSeries.contains(me.toString()
								renderer.setSeriesToolTipGenerator(0, tt1);
								plot1.setRenderer(count, renderer);
								int hits = 0; // ts.getStat()[1]
								int matchs=0;
								if (((ExtendedTimeSeries) statMap.get(me)).getStat() != null) {
									hits = ts.getStat()[1];
								//	matchs= ((ExtendedTimeSeries) statMap.get(me)).getStat()[0];
								}
								JCheckBox jcb = new JCheckBox(new VisibleAction(cd,panel,checkboxPanel,axis4, me.toString() + "(" + hits + ")", 0));
								jcb.setSelected(((RecordingItem)ts.getRecordingItem()).isShow());
								jcb.setBackground(Color.white);
								jcb.setBorderPainted(true);
								if (count<colors.length){
								jcb.setBorder(BorderFactory.createLineBorder(colors[count], 1, true));
								}
								jcb.setFont(new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize()));
								logger.debug(me.toString()+", "+((RecordingItem)ts.getRecordingItem()).isShow());
					//			if (MainFrame.cd.disabledSeries.contains(me.toString())){
					//				jcb.setSelected(false);
					//			}
								checkboxPanel.add(jcb);
								count++;
							}
		//					XYPlot plot1 = chart.getXYPlot();				
		//					final ValueAxis domainAxis = domainAxis1;
		//					final ValueAxis domainAxisa = domainAxis2;
		//					plot1.setDomainCrosshairVisible(true);
		//					plot1.setRangeCrosshairVisible(true);
		//					plot1.setDomainAxis(0,domainAxis);
		//					plot1.setDomainAxis(1,domainAxisa);
							
							Iterator eventMapIterator = eventMap.entrySet().iterator();
							while (eventMapIterator.hasNext()) {
								
							//	HistogramDataset histoDataSet=new HistogramDataset();

								TimeSeriesCollection dataset = new TimeSeriesCollection();
								Map.Entry me = (Map.Entry) eventMapIterator.next();
								ExtendedTimeSeries ts = (ExtendedTimeSeries) me.getValue();
								// if (dataset.getEndXValue(series, item))
								if (((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMaxY() > 0)
									dataset.addSeries(((ExtendedTimeSeries) me.getValue()).getTimeSeries());

								logger.debug("mineResultSet group: " + mr.getGroup() + ", key: " + me.getKey() + " nb records: "
										+ ((ExtendedTimeSeries) me.getValue()).getTimeSeries().getItemCount());
								logger.debug("mineResultSet hash content: " + mr.getEventTimeseriesMap());
								logger.debug("(((TimeSeries) me.getValue()).getMaxY(): " + (((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMaxY()));
								logger.debug("(((TimeSeries) me.getValue()).getMinY(): " + (((ExtendedTimeSeries) me.getValue()).getTimeSeries().getMinY()));

							//	LogarithmicAxis axis4 = new LogarithmicAxis(me.toString());
								NumberAxis axis4 = new NumberAxis(me.getKey().toString());
								axis4.setAutoRange(true);
								// axis4.setInverted(true);
								axis4.setAxisLineVisible(true);
								axis4.setAutoRangeIncludesZero(true);
								// axis4.setRange(new Range(((TimeSeries)
								// axis4.setRange(new Range(((TimeSeries)
								// me.getValue()).getMinY(), ((TimeSeries)
								// me.getValue()).getMaxY()));
								if (count<colors.length){
								axis4.setLabelPaint(colors[count]);
								axis4.setTickLabelPaint(colors[count]);
								}
								axis4.setVisible( ((RecordingItem)ts.getRecordingItem()).isShow());
								// domainAxis.setLowerMargin(0.001);
								// domainAxis.setUpperMargin(0.0);
								//plot1.setRangeCrosshairLockedOnData(true);
								plot1.setRangeAxis(count, axis4);
								plot1.setForegroundAlpha(0.5f);
								plot1.setDataset(count, dataset);
								plot1.mapDatasetToRangeAxis(count, count);
								XYBarRenderer rend = new XYBarRenderer(); // XYErrorRenderer
								rend.setShadowVisible(false);
								rend.setDrawBarOutline(true);
								Stroke stroke = new BasicStroke(5);
								rend.setBaseStroke(stroke);
								final XYItemRenderer renderer = rend;
								renderer.setSeriesToolTipGenerator(0, tt1);
								// renderer.setItemLabelsVisible(true);
								if (count<colors.length){
								renderer.setSeriesPaint(0, colors[count]);}
								renderer.setSeriesVisible(0, true);
								plot1.setRenderer(count, renderer);
								int hits = 0;
								int matchs=0;
								
								if (((ExtendedTimeSeries) me.getValue()).getStat() != null) {
									hits = ((ExtendedTimeSeries) me.getValue()).getStat()[1];
								//	matchs= ((ExtendedTimeSeries) me.getValue()).getStat()[0];
								}
								JCheckBox jcb = new JCheckBox(new VisibleAction(cd,panel,checkboxPanel,axis4, me.getKey().toString() + "(" + hits + ")", 0));
								jcb.setSelected(((RecordingItem)ts.getRecordingItem()).isShow());
								jcb.setBackground(Color.white);
								jcb.setBorderPainted(true);
								if (count<colors.length){
								jcb.setBorder(BorderFactory.createLineBorder(colors[count], 1, true));}
								jcb.setFont(new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize()));
								checkboxPanel.add(jcb);
								count++;
							}

							JPanel pan = new JPanel();
							
							pan.setLayout(new BorderLayout());
							pan.setMaximumSize(new Dimension(20000,Integer.parseInt((String)Preferences.getPreference("chartSize"))));
							pan.setPreferredSize(new Dimension(600, Integer.parseInt((String)Preferences.getPreference("chartSize"))));
							// pan.setPreferredSize(panelSize);
							panel.add(pan);
							final ChartPanel cpanel = new ChartPanel(chart);
							cpanel.setMinimumDrawWidth(0);
							cpanel.setMinimumDrawHeight(0);
							cpanel.setMaximumDrawWidth(1920);
							cpanel.setMaximumDrawHeight(Integer.parseInt((String)Preferences.getPreference("chartSize")));
							// cpanel.setInitialDelay(0);
							cpanel.setDismissDelay(9999999);
							cpanel.setInitialDelay(50);
							cpanel.setReshowDelay(200);
							cpanel.setPreferredSize(new Dimension(600, Integer.parseInt((String)Preferences.getPreference("chartSize"))));
							// cpanel.restoreAutoBounds(); fix the tooltip
							// missing problem but then relative display is
							// broken
							panel.add(new JSeparator(SwingConstants.HORIZONTAL));
							
							JPanel panTitle = new JPanel();
							panTitle.setLayout(new BorderLayout(0, 0));
							panTitle.setPreferredSize(new Dimension(600, 20));
							JPanel chartTitlePanel1 = new JPanel();
							chartTitlePanel1.setBackground(Color.WHITE);
							panTitle.add(chartTitlePanel1, BorderLayout.WEST);
							chartTitlePanel1.setLayout(new GridLayout(0, 1, 0, 0));
							FastDateFormat sdf = FastDateFormat.getInstance("dd-MMM-yyyy HH:mm:ss");
							JLabel chartTitleStartDateLbl = new JLabel("    "+sdf.format(mr.getStartDate()));
							chartTitleStartDateLbl.setFont(new Font("Verdana", Font.PLAIN, 10));
						    chartTitleStartDateLbl.setVerticalAlignment(SwingConstants.BOTTOM);
							chartTitlePanel1.add(chartTitleStartDateLbl);
							JPanel chartTitlePanel2 = new JPanel();
							FlowLayout flowLayout = (FlowLayout) chartTitlePanel2.getLayout();
							flowLayout.setAlignment(FlowLayout.LEFT);
							chartTitlePanel2.setBackground(Color.WHITE);
							panTitle.add(chartTitlePanel2, BorderLayout.CENTER);
							JLabel chartTitleSourceLbl = new JLabel("         "+mr.getSourceID() + " " + mr.getGroup());
							chartTitleSourceLbl.setFont(new Font("Verdana", Font.BOLD, 12));
							chartTitlePanel2.add(chartTitleSourceLbl);							
							pan.add(panTitle, BorderLayout.NORTH);
							
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
												logger.info(dataitem.getPeriod().getEnd());
//												myString = mr.getFileForDate(dataitem.getPeriod().getEnd()).toString();
												String lineString=""+mr.getFileLineForDate(dataitem.getPeriod().getEnd(),item.getDataset().getSeriesKey(item.getSeriesIndex()).toString()).getLineNumber();
												String fileString=cd.sourceFileArrayListMap.get(pairs.getKey()).get(mr.getFileLineForDate(dataitem.getPeriod().getEnd(),item.getDataset().getSeriesKey(item.getSeriesIndex()).toString()).getFileId()).getFile().getAbsolutePath();
												String command=Preferences.getPreference("editorCommand");
												command=command.replace("$line", lineString);
												command=command.replace("$file", fileString);
												logger.info(command);
												Runtime rt = Runtime.getRuntime();
												try {
													rt.exec(command);
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
												StringSelection stringSelection = new StringSelection(fileString);
												Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
												clpbrd.setContents(stringSelection, null);
										//		cpanel.getGraphics().drawString("file name copied", x - 5, y - 5);
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
		}}
		// Map=miner.mine(sourceFiles,repo);
		JPanel panEnd = new JPanel();
		
		panEnd.setLayout(new BorderLayout());
		panEnd.setPreferredSize(new Dimension(600, 0));
		// pan.setPreferredSize(panelSize);
		panel.add(panEnd);
		estimatedTime = System.currentTimeMillis() - startTime;
		revalidate();
		logger.info("display time: " + estimatedTime);
	   	//panel.transferFocus();
	  //	panel.requestFocus();
	}

	private static class VisibleAction extends AbstractAction {

		NumberAxis localAxis;
		private int i;
		JPanel jpanel;
		String name;
		JPanel checkBoxPanel;
		ChartData cd;
		
		public VisibleAction(ChartData cd1,JPanel panel,JPanel checkBoxPanel,NumberAxis axis, String name, int i) {
			super(name);
			this.localAxis=axis;
			this.i = i;
			this.name=name.substring(0, ((name).indexOf("(")));
			this.checkBoxPanel=checkBoxPanel;
			jpanel=panel;
			cd=cd1;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
		/*	if (!mainFrame1.cd.disabledSeries.contains(name)){
				if (logger.isDebugEnabled()){
					logger.debug("add "+ name);
				}
				mainFrame1.cd.addDisabledSeries(name);
			}else {
				if (logger.isDebugEnabled()){
					logger.debug("remove "+ name);
				}
				mainFrame1.cd.removeDisabledSeries(name);
			}
			if (logger.isDebugEnabled()){
				logger.debug("cd.disabledSeries: "+ mainFrame1.cd.disabledSeries );
			}*/
		//	renderer.setSeriesVisible(i, !renderer.getSeriesVisible(i));
		//	localAxis.setVisible(!localAxis.isVisible());
			Component[] comp=jpanel.getComponents();
			int i=0;
			while (i<comp.length-1){
				if (comp[i].getClass().equals(JPanel.class)){
				//	logger.info(i+" / "+comp.length+ " is Jpanel " + ((JPanel)comp[i]).getName() + " and " + ((JPanel)comp[i]).getComponent(1));
					if (((JPanel)comp[i]).getComponent(1).getClass().equals(ChartPanel.class)){
				//		logger.debug("is ChartPanel");
					int nbAxis=((ChartPanel)((JPanel)comp[i]).getComponent(1)).getChart().getXYPlot().getRangeAxisCount();
					if (logger.isDebugEnabled()){
						logger.debug(nbAxis);
						logger.debug(((ChartPanel)((JPanel)comp[i]).getComponent(1)).getChart().getTitle().getText());
					}					
					int i2=0;
					while (i2<nbAxis){
						if (logger.isDebugEnabled()){
							logger.debug(localAxis.getLabel());
							logger.debug(localAxis.getLabel());
						}

						if (((ChartPanel)((JPanel)comp[i]).getComponent(1)).getChart().getXYPlot().getRangeAxis(i2).getLabel()!=null && ((ChartPanel)((JPanel)comp[i]).getComponent(1)).getChart().getXYPlot().getRangeAxis(i2).getLabel().toString().equals(localAxis.getLabel().toString())){
							((ChartPanel)((JPanel)comp[i]).getComponent(1)).getChart().getXYPlot().getRangeAxis(i2).setVisible(!((ChartPanel)((JPanel)comp[i]).getComponent(1)).getChart().getXYPlot().getRangeAxis(i2).isVisible());;
							((ChartPanel)((JPanel)comp[i]).getComponent(1)).getChart().getXYPlot().getRenderer(i2).setSeriesVisible(0, !((ChartPanel)((JPanel)comp[i]).getComponent(1)).getChart().getXYPlot().getRenderer(i2).isSeriesVisible(0));	
						}
						i2++;
						}
					Component[] comp2=((JPanel)((JPanel)comp[i]).getComponent(2)).getComponents();
					int i3=0;
					while (i3<comp2.length){;
						if ( ((JCheckBox)comp2[i3]).getText().substring(0, ((((JCheckBox)comp2[i3]).getText()).indexOf("("))).equals(name)&& !((JPanel)((JPanel)comp[i]).getComponent(2)).equals(checkBoxPanel)){
							((JCheckBox)comp2[i3]).setSelected(!((JCheckBox)comp2[i3]).isSelected());
						}
						i3++;
						}
				}}
				i++;
			}
		}

	}
}
