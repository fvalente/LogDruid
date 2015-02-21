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
package logdruid.ui;

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
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logdruid.data.DataVault;
import logdruid.data.Repository;
import logdruid.data.record.EventRecording;
import logdruid.data.record.MetadataRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.data.record.StatRecording;
import logdruid.ui.editor.EventRecordingEditor;
import logdruid.ui.editor.MetadataRecordingEditor;
import logdruid.ui.editor.StatRecordingEditor;
import logdruid.util.DataMiner;

import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import javax.swing.JSplitPane;

public class RecordingList extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	public static JTable table;
	JPanel jPanelDetail;
	private RecordingList thiis = this;
	boolean DEBUG = false;
	static Pattern sepPattern = Pattern.compile("(.*), (.*)");
	static Pattern equalPattern = Pattern.compile("(.*)=(.*)");
	static Matcher m;
	static ArrayList records = null;
	private String[] header ;
	// 0-> sum of time for success matching of given
	// recording ; 1-> sum of time for failed
	// matching ; 2-> count of match attempts,
	// 3->count of success attempts
	private Vector<Object[]> data = new Vector<Object[]>();
	private Repository repository = null;
	private JPanel recEditor = null;
	public MyTableModel2 model;
	long[] stats;
	
	/**
	 * Create the panel.
	 */
	public RecordingList(final Repository rep) {
		
	if (rep.getPreference("timings").equals("true")){
		header= (String[]) new String[]{ "name", "regexp", "type", "active", "success time", "failed time", "match attempt", "success match" };}
	else {
		header= (String[]) new String[]{ "name", "regexp", "type", "active"};
	}
	
		records = rep.getRecordings();
		// Collections.sort(records);
		Iterator it = records.iterator();
		while (it.hasNext()) {
			Recording record = (Recording) it.next();
			stats = DataVault.getRecordingStats(record.getName());
			if (stats != null) {
				data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), record.getIsActive(), stats[0], stats[1], stats[2], stats[3] });
			} else {
				data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), record.getIsActive(), 0, 0, 0, 0 });
			}
		}
		
		
		repository = rep;
		model = new MyTableModel2(data, header);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.insets = new Insets(5, 0, 5, 5);
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 0;
		panel_1.setLayout(new BorderLayout(0, 0));

		table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table);int column;
		panel_1.add(scrollPane);

		table.setPreferredScrollableViewportSize(new Dimension(0, 150));
		table.setFillsViewportHeight(true);

		// Set up column sizes.
		initColumnSizes(table);
		table.setAutoCreateRowSorter(true);
	//	RowSorter sorter = table.getRowSorter();
//		sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
		if (model.getRowCount()>0){
			table.getRowSorter().toggleSortOrder(0);
		table.getRowSorter().toggleSortOrder(2);
		}
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
				;
				logger.info("ListSelectionListener - selectedRow: " + selectedRow);
				if (selectedRow >= 0) {
					if (jPanelDetail != null) {
						logger.info("ListSelectionListener - valueChanged");
						jPanelDetail.removeAll();
						recEditor = getEditor(repository.getRecording(selectedRow));
						if (recEditor != null) {
							jPanelDetail.add(recEditor, BorderLayout.CENTER);
						}
						jPanelDetail.revalidate();
					}
				}
			}
		});
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setVgap(2);
		flowLayout.setHgap(2);
		panel_1.add(panel, BorderLayout.SOUTH);

		JButton btnNewMeta = new JButton("New Meta");
		panel.add(btnNewMeta);
		btnNewMeta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int rowCount = table.getRowCount();
				jPanelDetail.removeAll();
				Recording re = new MetadataRecording("name", "regex", "example line", "", true, null);
				recEditor = new MetadataRecordingEditor(thiis, repository, "the line", "regex",
						(MetadataRecording) re);
				jPanelDetail.add(recEditor, BorderLayout.CENTER);
				repository.addRecording(re);
				model.addRow(new Object[] { re.getName(), re.getRegexp(), re.getType(), re.getIsActive(), 0, 0, 0, 0 });
				model.fireTableRowsInserted(rowCount, rowCount);
				table.setRowSelectionInterval(rowCount, rowCount);
				logger.info("New record - row count : " + rowCount);
			}
		});

		JButton btnDuplicate = new JButton("Duplicate");
		btnDuplicate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int rowCount = table.getRowCount();
				int selectRow = ((table.getSelectedRow() != -1) ? table.getSelectedRow() : -1);
				int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
					repository.addRecording(repository.getRecording(table.convertRowIndexToModel(table.getSelectedRow())).duplicate());
					model.fireTableRowsInserted(rowCount, rowCount);
					table.setRowSelectionInterval(selectRow, selectRow);
			}
		});

		JButton btnNewStat = new JButton("New Stat");
		btnNewStat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int rowCount = table.getRowCount();
				jPanelDetail.removeAll();
				Recording re = new StatRecording("name", "regex", "example line", "", true, null);
				recEditor = new StatRecordingEditor(thiis, repository, "the line", "regex", (StatRecording) re);
				jPanelDetail.add(recEditor, BorderLayout.CENTER);
				repository.addRecording(re);
				model.addRow(new Object[] { re.getName(), re.getRegexp(), re.getType(), re.getIsActive(), 0, 0, 0, 0 });
				model.fireTableRowsInserted(rowCount, rowCount);
				table.setRowSelectionInterval(rowCount, rowCount);
				logger.info("New record - row count : " + rowCount);
			}
		});
		panel.add(btnNewStat);

		JButton btnNewEvent = new JButton("New Event");
		btnNewEvent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int rowCount = table.getRowCount();
				logger.info("table.getRowCount()" + table.getRowCount());
				jPanelDetail.removeAll();
				Recording re = new EventRecording("name", "regex", "example line", "", true, null);
				recEditor = new EventRecordingEditor(thiis, repository, "the line", "regex", (EventRecording) re);
				jPanelDetail.add(recEditor, BorderLayout.CENTER);
				repository.addRecording(re);
				model.addRow(new Object[] { re.getName(), re.getRegexp(), re.getType(), re.getIsActive(), 0, 0, 0, 0 });
				model.fireTableRowsInserted(rowCount, rowCount);
				table.setRowSelectionInterval(rowCount, rowCount);
				logger.info("New record - row count : " + rowCount);
			}
		});
		panel.add(btnNewEvent);
		panel.add(btnDuplicate);

		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int rowCount = table.getRowCount();
				int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
				;
				int realSelectedRow=table.getSelectedRow() ;
				logger.info("selectedRow : " + selectedRow + ", row count: " + table.getRowCount());
				if (rowCount!=0){
				repository.deleteRecording(selectedRow);
				model.fireTableRowsDeleted(selectedRow, selectedRow);
	//			table.remove(selectedRow);
				if (realSelectedRow!= -1){
					if (realSelectedRow!= 0)table.setRowSelectionInterval(realSelectedRow-1, realSelectedRow-1);
					else if (realSelectedRow >0)table.setRowSelectionInterval(realSelectedRow, realSelectedRow);
					else if (rowCount>1) table.setRowSelectionInterval(0, 0);
				}
/*				if (table.getRowCount() > 0) {
					if (selectedRow == table.getRowCount()) {
						table.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
					} else
						table.setRowSelectionInterval(selectedRow, selectedRow);
				}*/
			}}
		});
		panel.add(btnDelete);
		setLayout(new BorderLayout(0, 0));

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
		if (repository.getRecordingCount() > 0) {
			//recEditor = getEditor(repository.getRecording(0));
			//jPanelDetail.add(recEditor, BorderLayout.CENTER);
			table.setRowSelectionInterval(0, 0);
		}
		jPanelDetail.revalidate();

	}

	private JPanel getEditor(Recording rec) {
		JPanel editorPanel = null;
		if (rec.getClass() == StatRecording.class) {
			editorPanel = new StatRecordingEditor(thiis, repository, rec.getExampleLine(), rec.getRegexp(), ((StatRecording) rec));
		} else if (rec.getClass() == MetadataRecording.class) {
			editorPanel = new MetadataRecordingEditor(thiis, repository, rec.getExampleLine(), rec.getRegexp(),
					((MetadataRecording) rec));
		} else if (rec.getClass() == EventRecording.class) {
			editorPanel = new EventRecordingEditor(thiis, repository, rec.getExampleLine(), rec.getRegexp(), ((EventRecording) rec));
		}
		return editorPanel;
	}

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

	public  class MyTableModel2 extends AbstractTableModel {
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
			return repository.getRecordingCount();
		}
		
		public void addRow(Object[] obj) {
			data.add(obj);
		}

		public void updateRow(int rowId, Object[] obj) {
			data.set(rowId, obj);
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0 ) {
				return repository.getRecording(row).getName();
			} else if (column == 1 ) {
				return repository.getRecording(row).getRegexp();
			} else if (column == 2 ) {
				return repository.getRecording(row).getType();
			} else if (column == 3 ) {
				logger.info("setValueAt calls setActiveRecording");
				return repository.getRecording(row).getIsActive();
			} else if (column >3 && column<9) {
				stats = DataVault.getRecordingStats(repository.getRecording(row).getName());
				if (stats != null) {
						return stats[column-4];
				} else return 0;
			}
			else return 0;

		}

		@Override
		public void setValueAt(Object value, int row, int column) {
		
			if (column == 3 ) {
				logger.info("setValueAt calls setActiveRecording");
				repository.getRecording(row).setIsActive((Boolean)value);
				//.toggleActiveRecording(repository.getRecording(MetadataRecording.class, row));
				fireTableCellUpdated(row, column);
				// logger.info("control of setValueAt: "+source.isActiveRecordingOnSource(repository.getRecording(MetadataRecording.class,
				// row)));
			} else {
				((Object[]) data.get(row))[column] = value;
				//	logger.info("setValueAt"+row+","+column);
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
			// no matt&er where the cell appears onscreen.
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
	 * // Create and set up the content pane. RecordingList newContentPane = new
	 * RecordingList(null, repository); newContentPane.setOpaque(true); //
	 * content panes must be opaque frame.setContentPane(newContentPane);
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
		data.remove(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
		repository.deleteRecording(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
		table.repaint();
	}
}
