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
package logdruid.ui.table;

import javax.swing.JPanel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logdruid.data.Repository;
import logdruid.data.record.EventRecording;
import logdruid.data.record.MetadataRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.data.record.StatRecording;
import logdruid.util.DataMiner;
import logdruid.util.PatternCache;

public class MetadataRecordingEditorTable extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private boolean DEBUG = false;
	static Pattern sepPattern = Pattern.compile("(.*), (.*)");
	static Pattern equalPattern = Pattern.compile("(.*)=(.*)");
	static Matcher m;
	static ArrayList records = null;
	private MyTableModel model;
	private String[] header = { "Name", "Before", "Type", "After", "selected", "Value" };
	private ArrayList<Object[]> data = new ArrayList<Object[]>();
	JTable table = null;
	private String theLine = "";
	private JTextPane examplePane;
	private Repository rep = null;
	private Recording recording;

	/**
	 * @wbp.parser.constructor
	 */
	@SuppressWarnings("unchecked")
	public MetadataRecordingEditorTable(JTextPane textPane) {
		super(new GridLayout(1, 0));

		model = new MyTableModel(data, header);
		table = new JTable(model);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);
		this.theLine = textPane.getText();
		this.examplePane = textPane;
		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		// Set up column sizes.
		initColumnSizes(table);

		// Fiddle with the Type column's cell editors/renderers.
		setUpTypeColumn(table, table.getColumnModel().getColumn(2));

		// Add the scroll pane to this panel.
		add(scrollPane);
		Add();
		FixValues();

	}

	public MetadataRecordingEditorTable(Repository repo, Recording re, JTextPane textPane) {
		super(new GridLayout(1, 0));
		this.examplePane = textPane;
		model = new MyTableModel(data, header);
		table = new JTable(model);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);
		rep = repo;
		this.theLine = textPane.getText();
		recording = re;
		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);
		// Set up column sizes.
		initColumnSizes(table);
		// Fiddle with the Type column's cell editors/renderers.
		setUpTypeColumn(table, table.getColumnModel().getColumn(2));
		// Add the scroll pane to this panel.
		add(scrollPane);

		if (re.getClass() == EventRecording.class) {
			records = ((EventRecording) re).getRecordingItem();
		} else if (re.getClass() == StatRecording.class) {
			records = ((StatRecording) re).getRecordingItem();
		} else if (re.getClass() == MetadataRecording.class) {
			records = ((MetadataRecording) re).getRecordingItem();
		}
		// Collections.sort(records);
		if (records != null) {
			Iterator it = records.iterator();

			while (it.hasNext()) {
				RecordingItem rI = (RecordingItem) it.next();
				data.add(new Object[] { rI.getName(), rI.getBefore(), rI.getType(), rI.getAfter(), rI.isSelected(), "" });
			}
			FixValues();
		}
	}

	public void FixValues() {
		String patternString = "";
		Matcher matcher;
		PatternCache patternCache=new PatternCache();
		Iterator it = data.iterator();
		Object[] obj;

		while (it.hasNext()) {
			obj = (Object[]) it.next();
			String stBefore = (String) obj[1];
			String stType = (String) obj[2];
			String stAfter = (String) obj[3];
			logger.info("stType: " + stType);
			if (stType.equals("date") && rep.getDateFormat(recording.getDateFormatID()).getPattern() != null) {
				patternString += stBefore + "(" + rep.getDateFormat(recording.getDateFormatID()).getPattern() + ")" + stAfter;
				logger.info("getTypeString(stType) getPattern -: " + rep.getDateFormat(recording.getDateFormatID()).getPattern());
				logger.info("getTypeString(stType) getDateFormat -: " + rep.getDateFormat(recording.getDateFormatID()).getDateFormat());
			} else {
				patternString += stBefore + "(" + DataMiner.getTypeString(stType) + ")" + stAfter;
				logger.info("getTypeString(stType) -: " + DataMiner.getTypeString(stType));
			}
		}

		try {
			logger.info("theLine: " + examplePane.getText());
			logger.info("patternString: " + patternString);
			matcher = patternCache.getPattern(patternString,recording.isCaseSensitive()).matcher(examplePane.getText());
			Highlighter h = examplePane.getHighlighter();
			h.removeAllHighlights();
			int currIndex = 0;
			if (matcher.find()) {
				// int currIndex = 0;
				// doc.insertString(doc.getLength(),line+"\n", null);

				for (int i = 1; i <= matcher.groupCount(); i++) {
					model.setValueAt(matcher.group(i), i - 1, 5);
					h.addHighlight(matcher.start(i), +matcher.end(i), new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE));
					logger.info("matcher.start(i): " + matcher.start(i) + "matcher.end(i): " + matcher.end(i));
				}

			}

		} catch (Exception e1) {
			e1.printStackTrace();
			// System.exit(1);
		}

	}

	private void initColumnSizes(JTable theTable) {
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int cellWidth = 0;
		TableCellRenderer headerRenderer = theTable.getTableHeader().getDefaultRenderer();

		for (int i = 0; i < 6; i++) {
			column = theTable.getColumnModel().getColumn(i);

			comp = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
			headerWidth = comp.getPreferredSize().width;

			cellWidth = comp.getPreferredSize().width;

			if (DEBUG) {
				logger.info("Initializing width of column " + i + ". " + "headerWidth = " + headerWidth + "; cellWidth = " + cellWidth);
			}

			column.setPreferredWidth(Math.max(headerWidth, cellWidth));
		}
	}

	public void setUpTypeColumn(JTable theTable, TableColumn TypeColumn) {
		// Set up the editor for the Type cells.
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("word");
		comboBox.addItem("string");
		comboBox.addItem("stringminimum");
		comboBox.addItem("long");
		comboBox.addItem("double");
		comboBox.addItem("date");
		comboBox.addItem("percent");
		TypeColumn.setCellEditor(new DefaultCellEditor(comboBox));

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click for combo box");
		TypeColumn.setCellRenderer(renderer);
	}

	class MyTableModel extends AbstractTableModel {
		private String[] header;
		private ArrayList<Object[]> data;

		public MyTableModel(ArrayList<Object[]> data, String[] header) {
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

		@Override
		public Object getValueAt(int row, int column) {
			return data.get(row)[column];
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			data.get(row)[column] = value;
			fireTableCellUpdated(row, column);
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matt&er where the cell appears onscreen.
			if (col > 4) {
				return false;
			} else {
				return true;
			}
		}
	}

	public ArrayList<RecordingItem> getRecordingItems() {
		ArrayList<RecordingItem> toReturn = new ArrayList<RecordingItem>();
		for (int i = 0; i < data.size(); i++) { // model.getRowCount()
			toReturn.add(new RecordingItem((String) model.getValueAt(i, 0), (String) model.getValueAt(i, 1), (String) model.getValueAt(i, 2), (String) model
					.getValueAt(i, 3), (Boolean) model.getValueAt(i, 4), (String) model.getValueAt(i, 5)));
		}
		return toReturn;
	}

	public void Add() {
		data.add(new Object[] { "", ".*", "long", "", Boolean.TRUE, "" });
		table.repaint();
	}

	public void Insert() {
		data.add(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1), new Object[] { "", ".*", "long", "",
				Boolean.TRUE, "" });
		table.repaint();
	}

	public void Remove() {
		data.remove(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
		table.repaint();
	}

}