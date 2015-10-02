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
package logdruid.ui.mainpanel;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JTable;
import javax.swing.BoxLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicBorders.SplitPaneBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Color;

import javax.swing.JButton;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import java.text.ParseException;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logdruid.data.DataVault;
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
//import logdruid.ui.editor.EventRecordingEditor;
//import logdruid.ui.editor.MetadataRecordingEditor;
//import logdruid.ui.editor.ReportRecordingEditor;
//import logdruid.ui.editor.StatRecordingEditor;
import logdruid.util.DataMiner;

import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import javax.swing.JSplitPane;
import javax.swing.JCheckBox;

public class ReportPanel extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	public static JTable reportList;
	JPanel jPanelDetail;
	private ReportPanel thiis = this;
	boolean DEBUG = false;
	static Pattern sepPattern = Pattern.compile("(.*), (.*)");
	static Pattern equalPattern = Pattern.compile("(.*)=(.*)");
	static Matcher m;
	static ArrayList records = null;
	private String[] header;
	private String[] headerRecords;
	// 0-> sum of time for success matching of given
	// recording ; 1-> sum of time for failed
	// matching ; 2-> count of match attempts,
	// 3->count of success attempts
	private Vector<Object[]> data = new Vector<Object[]>();
	private Repository repository = null;
	private JPanel recEditor = null;
	public MyTableModel2 model;
	public RecordReportTableModel recordReportTableModel;
	long[] stats;
	private JTable reportDetails;
	private Map<List<Object>, Long> occurenceReportMap;
	private Map<List<Object>, Double> sumReportMap;
	private SortedMap<Double,List<Object>> top100ReportMap;

	/**
	 * Create the panel.
	 */
	public ReportPanel(final Repository rep, final MineResultSet mineResultSet1) {

		if (Preferences.getPreference("timings").equals("true") && Preferences.getPreference("matches").equals("true") ) {
			header = (String[]) new String[] { "name", "regexp", "type", "rows", "success time", "failed time", "match attempt", "success match" };
		} else if (Preferences.getPreference("timings").equals("false") && Preferences.getPreference("matches").equals("true") ) {
			header = (String[]) new String[] { "name", "regexp", "type", "rows", "match attempt", "success match"  };
		} else if (Preferences.getPreference("timings").equals("true") && Preferences.getPreference("matches").equals("false") ) {
			header = (String[]) new String[] { "name", "regexp", "type", "rows" ,"success time", "failed time", };
		} else {
			header = (String[]) new String[] { "name", "regexp", "type", "rows" };
		}
		records = rep.getRecordings(ReportRecording.class,true);
		Iterator it = records.iterator();
		while (it.hasNext()) {
			Recording record = (Recording) it.next();
			logger.debug(record.getName());
			
			stats = DataVault.getRecordingStats(record.getName());
			if (record.getIsActive() ) {
				if (stats != null) {
					if (Preferences.getPreference("timings").equals("false") && Preferences.getPreference("matches").equals("true") ) {
					data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), record.getIsActive(), stats[2],
							stats[3] });
					} else if (Preferences.getPreference("timings").equals("true") && Preferences.getPreference("matches").equals("false") ) {
					data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), record.getIsActive(), stats[0], stats[1]});
					} else if (Preferences.getPreference("timings").equals("true") && Preferences.getPreference("matches").equals("true") ) {
						data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), record.getIsActive(), stats[0], stats[1], stats[2],
								stats[3] });
						} 
				} else {
					data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), record.getIsActive(), 0, 0, 0, 0 });
				}
			}
		}
		repository = rep;
		model = new MyTableModel2(data, header);
		JPanel panel_1 = new JPanel();
		panel_1.setMinimumSize(new Dimension(0, 150));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.insets = new Insets(5, 0, 5, 5);
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 0;
		panel_1.setLayout(new BorderLayout(0, 0));
		reportList = new JTable(model);
		reportList.setPreferredScrollableViewportSize(new Dimension(0, 150));
		reportList.setFillsViewportHeight(true);
		// Set up column sizes.
		initColumnSizes(reportList);
		reportList.setAutoCreateRowSorter(true);
		// RowSorter sorter = table.getRowSorter();
		// sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0,
		// SortOrder.ASCENDING)));
		if (model.getRowCount() > 0) {
			reportList.getRowSorter().toggleSortOrder(2);
			reportList.getRowSorter().toggleSortOrder(0);
		}
		reportList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				int selectedRow = ((reportList.getSelectedRow() != -1) ? reportList.convertRowIndexToModel(reportList.getSelectedRow()) : -1);
				logger.debug("ListSelectionListener - selectedRow: " + selectedRow);
				ArrayList<List<Object>> data1 = new ArrayList<List<Object>>();
				ArrayList<Object[]> rIArrayList = new ArrayList<Object[]>();
				if (selectedRow >= 0 )  {
					if (((ReportRecording)repository.getRecording(ReportRecording.class, selectedRow,true)).getSubType().equals("histogram"))
					{
					Iterator<RecordingItem> rIIte = repository.getRecording(ReportRecording.class, selectedRow,true).getRecordingItem().iterator();
					rIArrayList.add(new Object[] { "Source", String.class });
					while (rIIte.hasNext()) {
						RecordingItem ri = (RecordingItem) rIIte.next();
						Class c = String.class;
						if (ri.isSelected()) {
							if (ri.getType().equals("string") || ri.getType().equals("stringminimum") || ri.getType().equals("word") || ri.getType().equals("date")) {
								c = String.class;
							} else if (ri.getType().equals("long")) {
								c = Long.class;
							} else if (ri.getType().equals("double")) {
								c = double.class;
							}
							rIArrayList.add(new Object[] { ri.getName(), c });
						}
					}
					rIArrayList.add(new Object[] { "count", Long.class });
					
					if (mineResultSet1.getOccurenceReport() != null) {
						Iterator sourcesIterator=rep.getSources().iterator();
						while (sourcesIterator.hasNext())
						{
							Source src=(Source) sourcesIterator.next();
							if (src.getActive()  && mineResultSet1.getOccurenceReport().containsKey(src)){
						occurenceReportMap = mineResultSet1.getOccurenceReport().get(src).get(repository.getRecording(ReportRecording.class, selectedRow,true));
						if (occurenceReportMap != null) {
							Iterator<List<Object>> oRMIte = occurenceReportMap.keySet().iterator();
							while (oRMIte.hasNext()) {
								List<Object> obj = new ArrayList<Object>();
								List<Object> tempObj=oRMIte.next();
								obj.add(src.getSourceName());
								obj.addAll(1, tempObj);
								obj.add(occurenceReportMap.get(tempObj));
								data1.add(obj);
							}
							// logger.info(""+headerRecords.length
							// +Arrays.deepToString(headerRecords));
						}}
						}
						recordReportTableModel = new RecordReportTableModel(data1, rIArrayList);
						reportDetails = new JTable(recordReportTableModel);
					}
					
					}
					else if (((ReportRecording)repository.getRecording(ReportRecording.class, selectedRow,true)).getSubType().equals("top100"))
					{
						Iterator<RecordingItem> rIIte = repository.getRecording(ReportRecording.class, selectedRow,true).getRecordingItem().iterator();
						rIArrayList.add(new Object[] { "Source", String.class });
						while (rIIte.hasNext()) {
							RecordingItem ri = (RecordingItem) rIIte.next();
							Class c = null;
							if (ri.isSelected() && ri.getProcessingType().equals("capture")) {
								if (ri.getType().equals("string") || ri.getType().equals("stringminimum") || ri.getType().equals("word") || ri.getType().equals("date")) {
									c = String.class;
								} else if (ri.getType().equals("long")) {
									c = Long.class;
								} else if (ri.getType().equals("double")) {
									c = double.class;
								}
								rIArrayList.add(new Object[] { ri.getName(), c });
							}
						}
						rIArrayList.add(new Object[] { "value", Double.class });	
						if (mineResultSet1.getTop100Report() != null) {
							Iterator sourcesIterator=rep.getSources().iterator();
							while (sourcesIterator.hasNext())
							{
								Source src=(Source) sourcesIterator.next();
								if (src.getActive()  && mineResultSet1.getTop100Report().containsKey(src)){
									top100ReportMap = mineResultSet1.getTop100Report().get(src).get(repository.getRecording(ReportRecording.class, selectedRow,true));
							if (top100ReportMap != null) {
								Iterator<Double> oRMIte = top100ReportMap.keySet().iterator();
								while (oRMIte.hasNext()) {
									List<Object> obj = new ArrayList<Object>();
									Double tempObj=oRMIte.next();
									obj.add(src.getSourceName());
									obj.addAll(1, top100ReportMap.get(tempObj));
									obj.add(tempObj);
									data1.add(obj);
								}
								// logger.info(""+headerRecords.length
								// +Arrays.deepToString(headerRecords));
							}}
							}
							recordReportTableModel = new RecordReportTableModel(data1, rIArrayList);
							reportDetails = new JTable(recordReportTableModel);
						}
					}
					
					else if (((ReportRecording)repository.getRecording(ReportRecording.class, selectedRow,true)).getSubType().equals("sum"))
					{
						Iterator<RecordingItem> rIIte = repository.getRecording(ReportRecording.class, selectedRow,true).getRecordingItem().iterator();
						rIArrayList.add(new Object[] { "Source", String.class });
						while (rIIte.hasNext()) {
							RecordingItem ri = (RecordingItem) rIIte.next();
							Class c = null;
							if (ri.isSelected() && ri.getProcessingType().equals("capture")) {
								if (ri.getType().equals("string") || ri.getType().equals("stringminimum") || ri.getType().equals("word") || ri.getType().equals("date")) {
									c = String.class;
								} else if (ri.getType().equals("long")) {
									c = Long.class;
								} else if (ri.getType().equals("double")) {
									c = double.class;
								}
								rIArrayList.add(new Object[] { ri.getName(), c });
							} else if (ri.isSelected() && ri.getProcessingType().equals("sum")){
								c = Double.class;
								rIArrayList.add(new Object[] { "sum of "+ ri.getName(), c });
							}
						}	
						if (mineResultSet1.getSumReport() != null) {
							Iterator sourcesIterator=rep.getSources().iterator();
							while (sourcesIterator.hasNext())
							{
								Source src=(Source) sourcesIterator.next();
								if (src.getActive()  && mineResultSet1.getSumReport().containsKey(src)){
									sumReportMap = mineResultSet1.getSumReport().get(src).get(repository.getRecording(ReportRecording.class, selectedRow,true));
							if (sumReportMap != null) {
								Iterator<List<Object>> oRMIte = sumReportMap.keySet().iterator();
								while (oRMIte.hasNext()) {
									List<Object> obj = new ArrayList<Object>();
									List<Object> tempObj=oRMIte.next();
									obj.add(src.getSourceName());
									obj.addAll(1, tempObj);
									obj.add(sumReportMap.get(tempObj));
									data1.add(obj);
								}
								// logger.info(""+headerRecords.length
								// +Arrays.deepToString(headerRecords));
							}}
							}
							recordReportTableModel = new RecordReportTableModel(data1, rIArrayList);
							reportDetails = new JTable(recordReportTableModel);
						}
					}
					// headerRecords=(String[]) rIArrayList.toArray(new
					// String[rIArrayList.size()]);

					// logger.info(""+headerRecords.length +headerRecords);

				}

				// logger.info(mineResultSet1.getOccurenceReport().values());
				// logger.info(repository.getRecording(ReportRecording.class,selectedRow));
				reportDetails.setPreferredScrollableViewportSize(new Dimension(0, 150));
				reportDetails.setFillsViewportHeight(true);
				reportDetails.setAutoCreateRowSorter(true);
if (reportDetails.getColumnCount()>1){
				reportDetails.getRowSorter().toggleSortOrder(reportDetails.getColumnCount() - 1);
				reportDetails.getRowSorter().toggleSortOrder(reportDetails.getColumnCount() - 1);
}
				if (reportDetails != null) {
					jPanelDetail.removeAll();
					JScrollPane scrollPane_1 = new JScrollPane(reportDetails);
					jPanelDetail.add(scrollPane_1, BorderLayout.CENTER);
				}
				jPanelDetail.revalidate();
			}

		});
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setVgap(2);
		flowLayout.setHgap(2);
		panel_1.add(panel, BorderLayout.SOUTH);

		JCheckBox chckbxNewCheckBox = new JCheckBox("New check box");
		panel.add(chckbxNewCheckBox);
		setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		add(panel_2, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);

		jPanelDetail = new JPanel();
		GridBagConstraints gbc_jPanelDetail = new GridBagConstraints();
		gbc_jPanelDetail.insets = new Insets(0, 0, 0, 5);
		gbc_jPanelDetail.fill = GridBagConstraints.BOTH;
		gbc_jPanelDetail.gridx = 1;
		gbc_jPanelDetail.gridy = 4;
		splitPane.setBottomComponent(jPanelDetail);
		splitPane.setTopComponent(panel_1);
		jPanelDetail.setLayout(new BorderLayout(0, 0));

		reportDetails = new JTable();
		reportDetails.setPreferredScrollableViewportSize(new Dimension(0, 150));
		reportDetails.setFillsViewportHeight(true);
		reportDetails.setAutoCreateRowSorter(true);
		JScrollPane scrollPane_1 = new JScrollPane(reportDetails);
		jPanelDetail.add(scrollPane_1, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(reportList);
		panel_1.add(scrollPane, BorderLayout.CENTER);

		// scrollPane_1.setViewportView(reportDetails);
		if (repository.getRecordings(ReportRecording.class,true).size() > 0) {
			// recEditor = getEditor(repository.getRecording(0));
			// jPanelDetail.add(recEditor, BorderLayout.CENTER);
			reportList.setRowSelectionInterval(0, 0);
		}
		jPanelDetail.revalidate();
	}

	/*
	 * private JPanel getEditor(Recording rec) { JPanel editorPanel = null; if
	 * (rec.getClass() == StatRecording.class) { editorPanel = new
	 * StatRecordingEditor(thiis, repository, rec.getExampleLine(),
	 * rec.getRegexp(), ((StatRecording) rec)); } else if (rec.getClass() ==
	 * MetadataRecording.class) { editorPanel = new
	 * MetadataRecordingEditor(thiis, repository, rec.getExampleLine(),
	 * rec.getRegexp(), ((MetadataRecording) rec)); } else if (rec.getClass() ==
	 * EventRecording.class) { editorPanel = new EventRecordingEditor(thiis,
	 * repository, rec.getExampleLine(), rec.getRegexp(), ((EventRecording)
	 * rec)); } else if (rec.getClass() == ReportRecording.class) { editorPanel
	 * = new ReportRecordingEditor(thiis, repository, rec.getExampleLine(),
	 * rec.getRegexp(), ((ReportRecording) rec)); } return editorPanel; }
	 */

	private void initColumnSizes(JTable theTable) {
		MyTableModel2 model = (MyTableModel2) theTable.getModel();
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int cellWidth = 0;
		// Object[] longValues = model.longValues;
		TableCellRenderer headerRenderer = theTable.getTableHeader().getDefaultRenderer();

		for (int i = 0; i < 4; i++) {
			column = theTable.getColumnModel().getColumn(i);
			comp = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
			headerWidth = comp.getPreferredSize().width;
			cellWidth = comp.getPreferredSize().width;
			column.setPreferredWidth(Math.max(headerWidth, cellWidth));
		}
	}

	public class MyTableModel2 extends AbstractTableModel {
		private String[] header;
		private Vector data = new Vector();

		public MyTableModel2(Vector<Object[]> data, String[] header) {
			this.header = header;
			this.data = data;
		}

		@Override
		public int getColumnCount() {
			if (header == null) {
				return 0;
			} else
				return header.length;
		}

		@Override
		public String getColumnName(int column) {
			return header[column];
		}

		@Override
		public int getRowCount() {
			return repository.getRecordings(ReportRecording.class,true).size();
		}

		public void addRow(Object[] obj) {
			data.add(obj);
		}

		public void updateRow(int rowId, Object[] obj) {
			data.set(rowId, obj);
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) {
				return repository.getRecording(ReportRecording.class, row,true).getName();
			} else if (column == 1) {
				return repository.getRecording(ReportRecording.class, row,true).getRegexp();
			} else if (column == 2) {
				return ((ReportRecording) repository.getRecording(ReportRecording.class, row,true)).getSubType();
			} else if (column == 3) {
				int rows=0;
				if (MineResultSet.getOccurenceReport() != null) {
					Iterator sourcesIterator=repository.getSources().iterator();
					while (sourcesIterator.hasNext())
					{
						Source src=(Source) sourcesIterator.next();
						if (src.getActive()  && MineResultSet.getOccurenceReport().containsKey(src)){
							if (MineResultSet.getOccurenceReport().get(src).containsKey(repository.getRecording(ReportRecording.class, row,true))){
							rows=rows+MineResultSet.getOccurenceReport().get(src).get(repository.getRecording(ReportRecording.class, row,true)).size();
							}
						}}
				}
				if (MineResultSet.getTop100Report() != null) {
						Iterator sourcesIterator=repository.getSources().iterator();
						while (sourcesIterator.hasNext())
						{
							Source src=(Source) sourcesIterator.next();
							if (src.getActive()  && MineResultSet.getTop100Report().containsKey(src)){
								if (MineResultSet.getTop100Report().get(src).containsKey(repository.getRecording(ReportRecording.class, row,true))){
								rows=rows+MineResultSet.getTop100Report().get(src).get(repository.getRecording(ReportRecording.class, row,true)).size();
								}
							}}
				}
				if (MineResultSet.getSumReport() != null) {
					Iterator sourcesIterator=repository.getSources().iterator();
					while (sourcesIterator.hasNext())
					{
						Source src=(Source) sourcesIterator.next();
						if (src.getActive()  && MineResultSet.getSumReport().containsKey(src)){
							if (MineResultSet.getSumReport().get(src).containsKey(repository.getRecording(ReportRecording.class, row,true))){
							rows=rows+MineResultSet.getSumReport().get(src).get(repository.getRecording(ReportRecording.class, row,true)).size();
							}
						}}
			}
					return (int) rows;
			} else if (column > 3 && column < 9) {
				stats = DataVault.getRecordingStats(repository.getRecording(ReportRecording.class, row,true).getName());
				if (stats != null) {
					if (Preferences.getPreference("timings").equals("false") && Preferences.getPreference("matches").equals("true") ) {
						return stats[column - 2];
					} else {
						return stats[column - 4];
						} 
				} else
					return 0;
			} else
				return 0;

		}

		@Override
		public void setValueAt(Object value, int row, int column) {

			if (column == 3) {
				logger.info("setValueAt calls setActiveRecording");
				repository.getRecording(row).setIsActive((Boolean) value);
				// .toggleActiveRecording(repository.getRecording(MetadataRecording.class,
				// row));
				fireTableCellUpdated(row, column);
				// logger.info("control of setValueAt: "+source.isActiveRecordingOnSource(repository.getRecording(MetadataRecording.class,
				// row)));
			} else {
				((Object[]) data.get(row))[column] = value;
				// logger.info("setValueAt"+row+","+column);
				fireTableCellUpdated(row, column);
			}
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col != 3) {
				return false;
			} else {
				return true;
			}
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		/*
		 * public void setValueAt(Object value, int row, int col) { if (DEBUG) {
		 * logger.info("Setting value at " + row + "," + col + " to " + value +
		 * " (an instance of " + value.getClass() + ")"); }
		 * 
		 * data[row][col] = value; fireTableCellUpdated(row, col);
		 * 
		 * if (DEBUG) { logger.info("New value of data:"); printDebugData(); } }
		 * 
		 * private void printDebugData() { int numRows = getRowCount(); int
		 * numCols = getColumnCount();
		 * 
		 * for (int i=0; i < numRows; i++) { System.out.print("    row " + i +
		 * ":"); for (int j=0; j < numCols; j++) { System.out.print("  " +
		 * data[i][j]); } logger.info(); }
		 * logger.info("--------------------------"); }
		 */
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 * 
	 * private static void createAndShowGUI() { // Create and set up the window.
	 * JFrame frame = new JFrame("RecordingEditorTable");
	 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 * 
	 * // Create and set up the content pane. ReportPanel newContentPane = new
	 * ReportPanel(null, repository); newContentPane.setOpaque(true); // content
	 * panes must be opaque frame.setContentPane(newContentPane);
	 * 
	 * // Display the window. frame.pack(); frame.setVisible(true); }
	 */

	/*
	 * public ArrayList<RecordingItem> getRecordingItems() {
	 * ArrayList<RecordingItem> toReturn = new ArrayList<RecordingItem>(); for
	 * (int i = 0; i < data.size(); i++) { // model.getRowCount()
	 * toReturn.add(new RecordingItem((String) model.getValueAt(i, 0),(String)
	 * model.getValueAt(i, 1), (String) model.getValueAt(i, 2), (Boolean)
	 * model.getValueAt(i, 3)); } return toReturn; }
	 */

	/*
	 * public void Add() { data.add(new Object[] { " ", "", "long", "",
	 * Boolean.TRUE, "" }); table.repaint(); }
	 */

	public void Remove() {
		data.remove(((reportList.getSelectedRow() != -1) ? reportList.convertRowIndexToModel(reportList.getSelectedRow()) : -1));
		repository.deleteRecording(((reportList.getSelectedRow() != -1) ? reportList.convertRowIndexToModel(reportList.getSelectedRow()) : -1));
		reportList.repaint();
	}

	public class RecordReportTableModel extends AbstractTableModel {
		private ArrayList<Object[]> header;
		private ArrayList<List<Object>> data;

		public RecordReportTableModel(ArrayList<List<Object>> data1, ArrayList<Object[]> rIArrayList) {
			this.header = rIArrayList;
			this.data = data1;
		}

		@Override
		public int getColumnCount() {
			if (header == null) {
				return 0;
			} else
				return header.size();
		}

		@Override
		public String getColumnName(int column) {
			return (String) header.get(column)[0];
		}

		@Override
		public int getRowCount() {
			return data.size();

		}

		public void addRow(List<Object> obj) {
			data.add(obj);
		}

		public void updateRow(int rowId, List<Object> obj) {
			data.set(rowId, obj);
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column <= (data.get(row).size())) {
				// logger.info("row: " + row + ", column "+column +",header.get(column "+header.get(column) );
				 //" ,col: "+column + "data: "
				 //+data.get(row).get(column));
				return ((Class) header.get(column)[1]).cast(data.get(row).get(column));
			} else if (column == (data.get(row).size())) {

			}
			return ((Class) header.get(column)[1]).cast((Double) 0.0);

		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return (Class) header.get(c)[1];
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			return false;
		}

	}

}
