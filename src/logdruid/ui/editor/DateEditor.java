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
package logdruid.ui.editor;

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
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logdruid.data.Repository;
import logdruid.util.DataMiner;
import logdruid.data.DateFormat;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;

import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

public class DateEditor extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private JTable table;
	JPanel jPanelDetail;
	boolean DEBUG = false;
	static Matcher m;
	static ArrayList<DateFormat> dateFormats = null;
	private String[] header = { "name", "pattern", "FastDateFormat" };
	private ArrayList<Object[]> data = new ArrayList<Object[]>();
	private Repository repository = null;
	private StatRecordingEditor recEditor = null;
	public MyTableModel2 model;
	private JTextField textFieldName;
	private JTextField textFieldPattern;
	private JLabel labelPattern;
	private GridBagConstraints gbc_jPanelDetail;
	private JTextField textField;

	/**
	 * Create the panel.
	 */
	public DateEditor(Repository rep) {
		repository = rep;
		model = new MyTableModel2(data, header);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 15, 550, 15 };
		gridBagLayout.rowHeights = new int[] { 152, 300 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0 };
		setLayout(gridBagLayout);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.insets = new Insets(5, 0, 5, 5);
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 0;
		add(panel_1, gbc_panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		panel_1.setMinimumSize(new Dimension(0, 200));
		panel_1.setPreferredSize(new Dimension(0, 200));
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setBorder(UIManager.getBorder("TextPane.border"));
		table.setPreferredScrollableViewportSize(new Dimension(0, 0));
		table.setFillsViewportHeight(true);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				// ((table.getSelectedRow()!=-1)?table.convertRowIndexToModel(table.getSelectedRow()):-1)
				// persist repository
				// display selected row

				if (((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1) >= 0) {
					/*
					 * recEditor = new RecordingEditor(repository
					 * .getRecordings().get(((table.getSelectedRow()!=-1)?table.
					 * convertRowIndexToModel(table.getSelectedRow()):-1)),
					 * repository); jPanelDetail.removeAll();
					 */
					// jPanelDetail.add(recEditor, gbc_jPanelDetail);
					DateFormat df = repository.getDateFormat(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1)).clone();
					if (df != null) {
						textFieldName.setText((String) df.getName());
						textFieldPattern.setText((String) df.getPattern());
						textField.setText((String) df.getDateFormat());
					}
					// jPanelDetail.revalidate();
					// jPanelDetail.repaint();
					// jPanelDetail.setVisible(true);
					// reloadTable(); those 2 ********
					// jPanelDetail.revalidate();
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		panel_1.add(scrollPane, BorderLayout.CENTER);
		// Set up column sizes.
		initColumnSizes(table);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setVgap(2);
		flowLayout.setHgap(2);
		panel_1.add(panel, BorderLayout.SOUTH);

		JButton btnNew = new JButton("New");
		panel.add(btnNew);
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DateFormat df = new DateFormat("name", "\\w{3}\\s[0-9]{1}/[0-9]{2}/[0-9]{2}\\s\\d\\d:\\d\\d:\\d\\d", "EEE. MM/dd/yy HH:mm:ss");
				repository.addDateFormat(df);
				data.add(new Object[] { df.getName(), df.getPattern(), df.getDateFormat() });
				table.repaint();
			}
		});

		JButton btnDuplicate = new JButton("Duplicate");
		btnDuplicate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1) >= 0) {
					DateFormat df = repository.getDateFormat(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1)).clone();
					repository.addDateFormat(df);
					reloadTable();
					data.add(new Object[] { df.getName(), df.getPattern(), df.getDateFormat() });
					table.repaint();
				}
			}
		});
		panel.add(btnDuplicate);

		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
				if (selectedRow >= 0) {
					repository.deleteDateFormat(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
					data.remove(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
					reloadTable();
					table.setRowSelectionInterval(selectedRow, selectedRow);
					table.repaint();
				}
			}
		});
		panel.add(btnDelete);

		jPanelDetail = new JPanel();
		gbc_jPanelDetail = new GridBagConstraints();
		gbc_jPanelDetail.anchor = GridBagConstraints.NORTH;
		gbc_jPanelDetail.fill = GridBagConstraints.HORIZONTAL;
		gbc_jPanelDetail.gridx = 1;
		gbc_jPanelDetail.gridy = 1;
		add(jPanelDetail, gbc_jPanelDetail);
		GridBagLayout gbl_jPanelDetail = new GridBagLayout();
		gbl_jPanelDetail.columnWidths = new int[] { 169 };
		gbl_jPanelDetail.rowHeights = new int[] { 0, 0, 0, 0, 150, 0 };
		gbl_jPanelDetail.columnWeights = new double[] { 1.0, 0.0 };
		gbl_jPanelDetail.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 1.0, 0.0 };
		jPanelDetail.setLayout(gbl_jPanelDetail);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(null);
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.gridwidth = 2;
		gbc_panel_2.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		jPanelDetail.add(panel_2, gbc_panel_2);
		panel_2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel label = new JLabel("Name");
		panel_2.add(label);

		textFieldName = new JTextField();
		textFieldName.setColumns(20);
		panel_2.add(textFieldName);

		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panel_3.setBorder(null);
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.gridwidth = 2;
		gbc_panel_3.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 1;
		jPanelDetail.add(panel_3, gbc_panel_3);

		labelPattern = new JLabel("Pattern");
		panel_3.add(labelPattern);

		textFieldPattern = new JTextField();
		textFieldPattern.setColumns(40);
		panel_3.add(textFieldPattern);

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DateFormat df1 = repository.getDateFormat(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
				df1.update(textFieldName.getText(), textFieldPattern.getText(), textField.getText());
				reloadTable();
			}
		});

		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_4.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.anchor = GridBagConstraints.WEST;
		gbc_panel_4.insets = new Insets(0, 0, 5, 5);
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 2;
		jPanelDetail.add(panel_4, gbc_panel_4);

		JLabel lblFastDateFormat = new JLabel("FastDateFormat");
		panel_4.add(lblFastDateFormat);

		textField = new JTextField();
		panel_4.add(textField);
		textField.setColumns(30);

		JPanel panel_5 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_5.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.insets = new Insets(0, 0, 5, 5);
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 0;
		gbc_panel_5.gridy = 3;
		jPanelDetail.add(panel_5, gbc_panel_5);

		JLabel lblSampleLabel = new JLabel("Sample");
		panel_5.add(lblSampleLabel);

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.ipady = 1;
		gbc_panel_6.ipadx = 1;
		gbc_panel_6.insets = new Insets(0, 0, 5, 5);
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 4;
		jPanelDetail.add(panel_6, gbc_panel_6);
		panel_6.setLayout(new BorderLayout(0, 0));

		JTextPane textPane = new JTextPane();
		textPane.setBackground(UIManager.getColor("windowBorder"));
		panel_6.add(textPane);
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(0, 0, 0, 5);
		gbc_btnSave.gridx = 0;
		gbc_btnSave.gridy = 5;
		jPanelDetail.add(btnSave, gbc_btnSave);
		reloadTable();
	}

	public void reloadTable() {
		int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
		dateFormats = repository.getDates();
		// Collections.sort(records);
		if (dateFormats != null) {
			Iterator it = dateFormats.iterator();
			int count = 0;
			data.clear();
			// this.repaint();
			while (it.hasNext()) {
				DateFormat record = (DateFormat) it.next();
				data.add(new Object[] { record.getName(), record.getPattern(), record.getDateFormat() });
				logger.info(count + record.getName() + record.getPattern() + record.getDateFormat());
			}
			model.fireTableDataChanged();
			// this.repaint();
			// table.repaint();
			// table.revalidate(); ******************** removed to workaround
			// NPE
			if (selectedRow >= 0) {
				if (selectedRow <= table.getRowCount()) {
					table.setRowSelectionInterval(selectedRow, selectedRow);
				} else
					table.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
			} else if (table.getRowCount() > 0) {
				table.setRowSelectionInterval(0, 0);
			}
			this.revalidate();
		}
	}

	private void initColumnSizes(JTable theTable) {
		MyTableModel2 model = (MyTableModel2) theTable.getModel();
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int cellWidth = 0;
		// Object[] longValues = model.longValues;
		TableCellRenderer headerRenderer = theTable.getTableHeader().getDefaultRenderer();

		for (int i = 0; i < 3; i++) {
			column = theTable.getColumnModel().getColumn(i);

			comp = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
			headerWidth = comp.getPreferredSize().width;

			/*
			 * comp = table.getDefaultRenderer(model.getColumnClass(i)).
			 * getTableCellRendererComponent( table, longValues[i], false,
			 * false, 0, i);
			 */
			cellWidth = comp.getPreferredSize().width;

			if (DEBUG) {
				logger.info("Initializing width of column " + i + ". " + "headerWidth = " + headerWidth + "; cellWidth = " + cellWidth);
			}

			column.setPreferredWidth(Math.max(headerWidth, cellWidth));
		}
	}

	class MyTableModel2 extends AbstractTableModel {
		private String[] header;
		private ArrayList<Object[]> data;

		public MyTableModel2(ArrayList<Object[]> data, String[] header) {
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
			return data.size();
		}

		public void addRow(Object[] obj) {
			data.add(obj);
		}

		public void updateRow(int rowId, Object[] obj) {
			data.set(rowId, obj);
		}

		@Override
		public Object getValueAt(int row, int column) {
			return data.get(row)[column];
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			data.get(row)[column] = value;
			fireTableCellUpdated(row, column);
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
			if (col < 0) {
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
	 * ReportPanel(null, repository); newContentPane.setOpaque(true); //
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

	/*
	 * private ArrayList<DateFormat> findRecordItems(String theLine) {
	 * FastDateFormat FastDateFormat = new FastDateFormat(
	 * "EEE MM/dd/yy HH:mm:ss"); String[] rIString = theLine.split(", ");
	 * ArrayList<RecordingItem> rI = new ArrayList<RecordingItem>(); for (int i
	 * = 0; i < rIString.length; i++) { if (i == 0) { String[] splitted =
	 * rIString[i].split(": "); String date = splitted[0]; try {
	 * FastDateFormat.parse(date); } catch (ParseException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } } else { if
	 * (rIString[i].contains("=")) { String[] splitted = rIString[i].split("=");
	 * String name = splitted[0]; String value = splitted[1]; // rI.add(new
	 * RecordingItem(name,value,true)); logger.info(name + " " + value); } } }
	 * return rI; }
	 */
}
