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
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.record.EventRecording;
import logdruid.data.record.MetadataRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.data.record.StatRecording;
import logdruid.ui.editor.EventRecordingEditor;
import logdruid.ui.editor.MetadataRecordingEditor;
import logdruid.ui.editor.StatRecordingEditor;
import logdruid.ui.mainpanel.RecordingList.MyTableModel2;
import logdruid.util.DataMiner;

import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import javax.swing.JSplitPane;

public class MetadataRecordingSelectorPanel extends JPanel {
	private static Logger logger = Logger.getLogger(MetadataRecordingSelectorPanel.class.getName());
	public static JTable table;
	JPanel jPanelDetail;
	boolean DEBUG = false;
	static Pattern sepPattern = Pattern.compile("(.*), (.*)");
	static Pattern equalPattern = Pattern.compile("(.*)=(.*)");
	static Matcher m;
	static ArrayList records = null;
	private String[] header = { "name", "regexp", "type", "active" };
	private Vector<Object[]> data = new Vector<Object[]>();
	private Repository repository = null;
	private JPanel recEditor = null;
	public logdruid.ui.mainpanel.MetadataRecordingSelectorPanel.MyTableModel model;
	private Source source;

	/**
	 * Create the panel.
	 */
	public MetadataRecordingSelectorPanel(Repository rep, Source src) {
		repository = rep;
		source = src;
		records = rep.getRecordings(MetadataRecording.class);
		// Collections.sort(records);
		Iterator it = records.iterator();
		while (it.hasNext()) {
			Recording record = (Recording) it.next();
				data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), src.isActiveRecordingOnSource(record) });
			}
		
		model = new logdruid.ui.mainpanel.MetadataRecordingSelectorPanel.MyTableModel(data, header);

		logger.info("source is " + ((source == null) ? "null" : src.getSourceName()));
		setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_1.setLayout(new BorderLayout(0, 0));

		table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table);
		panel_1.add(scrollPane, BorderLayout.CENTER);

		table.setPreferredScrollableViewportSize(new Dimension(0, 150));
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);

		// Set up column sizes.
		initColumnSizes(table);
		reloadTable();

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);

		jPanelDetail = new JPanel();
		splitPane.setBottomComponent(jPanelDetail);
		splitPane.setTopComponent(panel_1);

		jPanelDetail.setLayout(new BorderLayout(0, 0));
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				// ((table.getSelectedRow()!=-1)?table.convertRowIndexToModel(table.getSelectedRow()):-1)
				// persist repository
				// display selected row
				int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);

				logger.info("ListSelectionListener - selectedRow: " + selectedRow);
				if (selectedRow >= 0) {

					if (jPanelDetail != null) {
						logger.debug("ListSelectionListener - valueChanged");
						jPanelDetail.removeAll();
						recEditor = getEditor(repository.getRecording(MetadataRecording.class, selectedRow));
						if (recEditor != null) {
							jPanelDetail.add(recEditor, BorderLayout.CENTER);
						}
						reloadTable();
						jPanelDetail.revalidate();
					}
				}
			}
		});
		if (repository.getRecordings(MetadataRecording.class).size() > 0) {
			recEditor = getEditor(repository.getRecording(MetadataRecording.class, 0));
			jPanelDetail.add(recEditor, BorderLayout.CENTER);
			table.setRowSelectionInterval(0, 0);
		}
		if (model.getRowCount()>0){
			table.getRowSorter().toggleSortOrder(0);
			table.setRowSelectionInterval(0, 0);
		}
		//reloadTable();
		// jPanelDetail.revalidate();

	}

	private JPanel getEditor(Recording rec) {
		return new MetadataRecordingEditor(this,repository, rec.getExampleLine(), rec.getRegexp(), ((MetadataRecording) rec));
	}

	public void reloadTable() {
		int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
		records = repository.getRecordings(MetadataRecording.class);
		logger.info("reloadTable - nb records : " + records.size());
		Iterator it = records.iterator();
		int count = 0;
		data.clear();
		// this.repaint();

		logger.debug("reloadTable - 1");
		while (it.hasNext()) {
			Boolean bool = false;
			Recording record = (Recording) it.next();
			if (source != null) {
				bool = source.isActiveRecordingOnSource(record);
				logger.debug("ReloadTable with " + record.getName() + " with isActiveRecordingOnSource: " + source.isActiveRecordingOnSource(record));
			}
			data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), bool });
			logger.info("name: " + record.getName() + "regexp: " + record.getRegexp() + "isActive: " + record.getIsActive());
		}
		// model.fireTableDataChanged();
		logger.debug("reloadTable - 2");
		// this.repaint();
		table.repaint();

		logger.debug("reloadTable - 3");
		this.revalidate();

	}

	private void initColumnSizes(JTable theTable) {
		MyTableModel model = (MyTableModel) theTable.getModel();
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

	public class MyTableModel extends AbstractTableModel {
		private String[] header;
		private Vector<Object[]> data;

		public MyTableModel(Vector<Object[]> data2, String[] header) {
			this.header = header;
			this.data = data2;
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
			return repository.getRecordings(MetadataRecording.class).size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0 ) {
				return repository.getRecording(MetadataRecording.class,row).getName();
			} else if (column == 1 ) {
				return repository.getRecording(MetadataRecording.class,row).getRegexp();
			} else if (column == 2 ) {
				return repository.getRecording(MetadataRecording.class,row).getType();
			} else if (column == 3 ) {
				logger.debug("getvalueat name" + ((MetadataRecording) repository.getRecording(MetadataRecording.class, row)).getName());
				logger.debug("getvalueat is active" + source.isActiveRecordingOnSource(repository.getRecording(MetadataRecording.class, row)));
				return source.isActiveRecordingOnSource(repository.getRecording(MetadataRecording.class, row));
			}
			else return 0;
		}

		public void addRow(Object[] obj) {
			data.add(obj);
	//		table.repaint();
		}

		public void updateRow(int rowId, Object[] obj) {
			data.set(rowId, obj);
	//		table.repaint();
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column == 3 && source != null) {
				logger.debug("setValueAt calls setActiveRecording");
				source.toggleActiveRecording(repository.getRecording(MetadataRecording.class, row));
				fireTableCellUpdated(row, column);
				// logger.info("control of setValueAt: "+source.isActiveRecordingOnSource(repository.getRecording(MetadataRecording.class,
				// row)));
			} else {

				data.get(row)[column] = value;
				fireTableCellUpdated(row, column);
			}
			table.repaint();
		}

		/*
		 * @Override public void setValueAt(Object value, int row, int column) {
		 * data.get(row)[column] = value; fireTableCellUpdated(row, column); }
		 */
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
			if (col > 2) {
				return true;
			} else {
				return false;
			}
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */

		/*
		 * public void setValueAt(Object value, int row, int col) { if (DEBUG) {
		 * logger.info("Setting value at " + row + "," + col + " to " + value +
		 * " (an instance of " + value.getClass() + ")"); } if(4 == col) {}
		 * 
		 * }
		 */
		/*
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

	/*
	 * public ArrayList<RecordingItem> getRecordingItems() {
	 * ArrayList<RecordingItem> toReturn = new ArrayList<RecordingItem>(); for
	 * (int i = 0; i < data.size(); i++) { // model.getRowCount()
	 * toReturn.add(new RecordingItem((String) model.getValueAt(i, 0), (String)
	 * model.getValueAt(i, 1), (String) model.getValueAt(i, 2), (String) model
	 * .getValueAt(i, 3), (Boolean) model.getValueAt(i, 4), (String)
	 * model.getValueAt(i, 5))); } return toReturn; }
	 */

	public void Add() {
		data.add(new Object[] { "", ".*", "long", Boolean.FALSE });
		table.repaint();
	}

	public void Remove() {
		data.remove(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
		table.repaint();
	}
}
