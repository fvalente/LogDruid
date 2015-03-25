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

import logdruid.data.DataVault;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.data.record.EventRecording;
import logdruid.data.record.MetadataRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.data.record.StatRecording;
import logdruid.ui.RecordingList;
import logdruid.ui.RecordingList.MyTableModel2;
import logdruid.ui.editor.EventRecordingEditor;
import logdruid.ui.editor.MetadataRecordingEditor;
import logdruid.ui.editor.StatRecordingEditor;
import logdruid.util.DataMiner;

import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import javax.swing.JSplitPane;

public class EventRecordingSelectorPanel extends JPanel {
	private static Logger logger = Logger.getLogger(EventRecordingSelectorPanel.class.getName());
	public static JTable table;
	JPanel jPanelDetail;
	EventRecordingSelectorPanel thiis = this;
	boolean DEBUG = false;
	static Pattern sepPattern = Pattern.compile("(.*), (.*)");
	static Pattern equalPattern = Pattern.compile("(.*)=(.*)");
	static Matcher m;
	static ArrayList records = null;
	private String[] header = { "name", "regexp", "type", "active" };
	private Vector<Object[]> data = new Vector<Object[]>();
	private Repository repository = null;
	private JPanel recEditor = null;
	public logdruid.ui.mainpanel.EventRecordingSelectorPanel.MyTableModel model;
	private Source source;

	/**
	 * Create the panel.
	 */
	public EventRecordingSelectorPanel(final Repository rep, Source src) {
		repository = rep;
		source = src;
		records = rep.getRecordings(EventRecording.class);
		// Collections.sort(records);
		Iterator it = records.iterator();
		while (it.hasNext()) {
			Recording record = (Recording) it.next();
				data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), src.isActiveRecordingOnSource(record) });
			}
		
		model = new logdruid.ui.mainpanel.EventRecordingSelectorPanel.MyTableModel(data, header);
		logger.info("source is " + ((source == null) ? "null" : src.getSourceName()));
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.insets = new Insets(5, 0, 5, 5);
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 0;
		panel_1.setLayout(new BorderLayout(0, 0));

		table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table);
		panel_1.add(scrollPane, BorderLayout.CENTER);

		table.setPreferredScrollableViewportSize(new Dimension(0, 150));
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);

		// Set up column sizes.
		initColumnSizes(table);

		setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);
		jPanelDetail = new JPanel();
		splitPane.setBottomComponent(jPanelDetail);
		splitPane.setTopComponent(panel_1);

		GridBagConstraints gbc_jPanelDetail = new GridBagConstraints();
		gbc_jPanelDetail.insets = new Insets(0, 0, 0, 5);
		gbc_jPanelDetail.fill = GridBagConstraints.BOTH;
		gbc_jPanelDetail.gridx = 1;
		gbc_jPanelDetail.gridy = 3;
		jPanelDetail.setLayout(new BorderLayout(0, 0));
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
				logger.info("ListSelectionListener - selectedRow: " + selectedRow);
				if (selectedRow >= 0) {
					if (jPanelDetail != null) {
						logger.debug("ListSelectionListener - valueChanged");
						jPanelDetail.removeAll();
						recEditor = getEditor(repository.getRecording(EventRecording.class, selectedRow));
						if (recEditor != null) {
							jPanelDetail.add(recEditor, BorderLayout.CENTER);
						}
						jPanelDetail.revalidate();
					}
				}
			}
		});
		if (repository.getRecordings(EventRecording.class).size() > 0) {
			recEditor = getEditor(repository.getRecording(EventRecording.class, 0));
			jPanelDetail.add(recEditor, BorderLayout.CENTER);
			table.setRowSelectionInterval(0, 0);
		}
		if (model.getRowCount()>0){
			table.getRowSorter().toggleSortOrder(0);
			table.setRowSelectionInterval(0, 0);
		}
	}

	private JPanel getEditor(Recording rec) {
			return new EventRecordingEditor(this,repository, rec.getExampleLine(), rec.getRegexp(), ((EventRecording) rec));
	}
/*
	public void reloadTable() {
		int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
		records = repository.getRecordings(EventRecording.class);
		logger.info("reloadTable - nb records : " + records.size());
		Iterator it = records.iterator();
		int count = 0;
		data.clear();
		// this.repaint();

		logger.info("reloadTable - 1");
		while (it.hasNext()) {
			Boolean bool = false;
			Recording record = (Recording) it.next();
			if (source != null) {
				bool = source.isActiveRecordingOnSource(record);
				logger.info("ReloadTable with " + record.getName() + " with isActiveRecordingOnSource: " + source.isActiveRecordingOnSource(record));
			}
			data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), bool });
			logger.info("name: " + record.getName() + "regexp: " + record.getRegexp() + "isActive: " + record.getIsActive());
		}
		// model.fireTableDataChanged();
		logger.info("reloadTable - 2");
		// this.repaint();
		table.repaint();

		logger.info("reloadTable - 3");
		this.revalidate();

	}
*/
	private void initColumnSizes(JTable theTable) {
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int cellWidth = 0;
		TableCellRenderer headerRenderer = theTable.getTableHeader().getDefaultRenderer();

		for (int i = 0; i < 4; i++) {
			column = theTable.getColumnModel().getColumn(i);
			comp = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
			headerWidth = comp.getPreferredSize().width;
			cellWidth = comp.getPreferredSize().width;
			column.setPreferredWidth(Math.max(headerWidth, cellWidth));
		}
	}
	
	public void Remove() {
		data.remove(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
		repository.deleteRecording(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
		table.repaint();
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
			return repository.getRecordings(EventRecording.class).size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0 ) {
				return repository.getRecording(EventRecording.class,row).getName();
			} else if (column == 1 ) {
				return repository.getRecording(EventRecording.class,row).getRegexp();
			} else if (column == 2 ) {
				return repository.getRecording(EventRecording.class,row).getType();
			} else if (column == 3 ) {
				logger.debug("getvalueat name" + ((EventRecording) repository.getRecording(EventRecording.class, row)).getName());
				logger.debug("getvalueat is active" + source.isActiveRecordingOnSource(repository.getRecording(EventRecording.class, row)));
				return source.isActiveRecordingOnSource(repository.getRecording(EventRecording.class, row));
			}
			else return 0;
		}

		public void addRow(Object[] obj) {
			data.add(obj);
			//table.repaint();
		}

		public void updateRow(int rowId, Object[] obj) {
			data.set(rowId, obj);
			//table.repaint();
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column == 3 && source != null) {
				logger.debug("setValueAt calls setActiveRecording");
				source.toggleActiveRecording(repository.getRecording(EventRecording.class, row));
				fireTableCellUpdated(row, column);
				// logger.info("control of setValueAt: "+source.isActiveRecordingOnSource(repository.getRecording(MetadataRecording.class,
				// row)));
			} else {
				((Object[]) data.get(row))[column] = value;
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
	}


	public void Add() {
		data.add(new Object[] { "", ".*", "long", Boolean.FALSE });
		table.repaint();
	}
}
