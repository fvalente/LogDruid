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
package logdruid.ui.table;

import javax.swing.JPanel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
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
import java.util.regex.Matcher;

import logdruid.data.Repository;
import logdruid.data.record.EventRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.ui.RegexTableRenderer;
import logdruid.util.DataMiner;
import logdruid.util.PatternCache;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ListSelectionModel;

public class EventRecordingEditorTable extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private boolean DEBUG = false;
	static Matcher m;
	static ArrayList<RecordingItem> records = null;
	private MyTableModel model;
	private String[] header = { "Name", "Processing", "Before", "Inside type", "Inside regex", "After", "Active", "Show", "Value" };
	private ArrayList<Object[]> data = new ArrayList<Object[]>();
	JTable table = null;
	private JTextPane examplePane;
	private Repository rep = null;
	private Recording recording;

	/**
	 * @wbp.parser.constructor
	 */
	@SuppressWarnings("unchecked")
	public EventRecordingEditorTable(JTextPane textPane) {
		super(new GridLayout(1, 0));
		model = new MyTableModel(data, header);
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFont(new Font("SansSerif", Font.PLAIN, 11));
		// table.setPreferredScrollableViewportSize(new Dimension(500, 200));
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setFillsViewportHeight(true);
		textPane.getText();
		this.examplePane = textPane;
		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		// Set up column sizes.
		initColumnSizes(table);

		// Fiddle with the type and processing column's cell editors/renderers.
		setUpProcessingColumn(table, table.getColumnModel().getColumn(1));
		setUpTypeColumn(table, table.getColumnModel().getColumn(3));
		setUpInsideRegexColumn(table, table.getColumnModel().getColumn(4));
		setUpInsideRegexColumn(table, table.getColumnModel().getColumn(2));
		setUpInsideRegexColumn(table, table.getColumnModel().getColumn(5));
		// Add the scroll pane to this panel.
		add(scrollPane);
	//	Add();
		FixValues();

	}

	public EventRecordingEditorTable(Repository repo, Recording re, JTextPane textPane) {
		super(new GridLayout(1, 0));
		this.examplePane = textPane;
		model = new MyTableModel(data, header);
		table = new JTable(model);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);
		rep = repo;
		textPane.getText();
		recording = re;
		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);
		// Set up column sizes.
		initColumnSizes(table);
		// Fiddle with the type and processing column's cell editors/renderers.
		setUpTypeColumn(table, table.getColumnModel().getColumn(3));
		setUpProcessingColumn(table, table.getColumnModel().getColumn(1));
		setUpInsideRegexColumn(table, table.getColumnModel().getColumn(4));
		setUpInsideRegexColumn(table, table.getColumnModel().getColumn(2));
		setUpInsideRegexColumn(table, table.getColumnModel().getColumn(5));
		// Add the scroll pane to this panel.
		add(scrollPane);

		records = ((EventRecording) re).getRecordingItem();
		// Collections.sort(records);
		if (records != null) {
			Iterator it = records.iterator();
			while (it.hasNext()) {
				RecordingItem rI = (RecordingItem) it.next();
				String inside="";
						inside= DataMiner.getMainRegex( rI.getType(),rI.getInside(), repo.getDateFormat(re.getDateFormatID())) ;
						logger.info("inside: " + inside);
				data.add(new Object[] { rI.getName(), rI.getProcessingType(), rI.getBefore(), rI.getType(), inside,rI.getAfter(), rI.isSelected(),rI.isShow(), "" });
				logger.info("added: " + rI.getName());
			}
			FixValues();
		}
	}

	public void FixValues() {
		String patternString = "";
		Matcher matcher = null;
		PatternCache patternCache=new PatternCache();
		Iterator<Object[]> it = data.iterator();
		Object[] obj;

		while (it.hasNext()) {
			obj = (Object[]) it.next();
			String stBefore = (String) obj[2];
			String stType = (String) obj[3];
			String stInside = (String) obj[4];
			String stAfter = (String) obj[5];
			logger.info("stType: " + stType);
			if (stType.equals("date") && rep.getDateFormat(recording.getDateFormatID()).getPattern() != null) {
				patternString += stBefore + "(" + rep.getDateFormat(recording.getDateFormatID()).getPattern() + ")" + stAfter;
				logger.info("getTypeString(stType) getPattern -: " + rep.getDateFormat(recording.getDateFormatID()).getPattern());
				logger.info("getTypeString(stType) getDateFormat -: " + rep.getDateFormat(recording.getDateFormatID()).getDateFormat());
			} else {
				if (stType.equals("manual")){
					patternString += stBefore + "(" + stInside + ")" + stAfter;	
					logger.info("getTypeString(stType) -: " + stInside);					
				}else{
					patternString += stBefore + "(" + DataMiner.getTypeString(stType) + ")" + stAfter;
					logger.info("getTypeString(stType) -: " + DataMiner.getTypeString(stType));
				}
			}
		}

		try {
			logger.info("theLine: " + examplePane.getText());
			logger.info("patternString: " + patternString);
			Highlighter h = examplePane.getHighlighter();
			h.removeAllHighlights();
			int currIndex = 0;
			
			String[] lines = examplePane.getText().split(System.getProperty("line.separator"));
			if (lines.length>=1){
			for (int i=0; i<lines.length ; i++){
				matcher = patternCache.getPattern(patternString,recording.isCaseSensitive()).matcher(lines[i]);
			if (matcher.find()) {
				// int currIndex = 0;
				// doc.insertString(doc.getLength(),line+"\n", null);

				for (int i2 = 1; i2 <= matcher.groupCount(); i2++) {
					model.setValueAt(matcher.group(i2), i2 - 1, 8);
					h.addHighlight(currIndex+matcher.start(i2), +currIndex+matcher.end(i2), new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE));
				}
				}
			logger.info("currIndex: " + currIndex + "matcher.end(i2): " + lines[i].length()+",l: "+lines[i]);
			currIndex += lines[i].length()+1 ;
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

		for (int i = 0; i < 9; i++) {
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

	public void setUpProcessingColumn(JTable theTable, TableColumn typeColumn) {
		JComboBox functionComboBox = new JComboBox();
		functionComboBox.addItem("capture");
		functionComboBox.addItem("duration");
		functionComboBox.addItem("occurrences");
		functionComboBox.addItem("sum");
		typeColumn.setCellEditor(new DefaultCellEditor(functionComboBox));
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click for combo box");
		typeColumn.setCellRenderer(renderer);
	}

	public void setUpInsideRegexColumn(JTable theTable, TableColumn typeColumn) {
		DefaultTableCellRenderer renderer = new RegexTableRenderer();
	//	renderer.setBackground(Color.GRAY);
		typeColumn.setCellRenderer(renderer);
	}
	
	
	public void setUpTypeColumn(JTable theTable, TableColumn typeColumn) {
		// Set up the editor for the type cells.
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addItem("date");
		comboBox.addItem("double");
		comboBox.addItem("long");
		comboBox.addItem("manual");
		comboBox.addItem("percent");
		comboBox.addItem("string");
		comboBox.addItem("stringminimum");
		comboBox.addItem("word");
		typeColumn.setCellEditor(new DefaultCellEditor(comboBox));

		// Set up tool tips for the type cells.
		DefaultTableCellRenderer renderer = new RegexTableRenderer();
		renderer.setToolTipText("Click for combo box");
		typeColumn.setCellRenderer(renderer);
		comboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				 JComboBox combo = (JComboBox) e.getSource();
				   Object selected = combo.getSelectedItem();
				   if (table.getSelectedRow()!=-1){
						logger.info(DataMiner.getMainRegex( (String) selected.toString(),(String) model.getValueAt(table.getSelectedRow(), 4), rep.getDateFormat(recording.getDateFormatID())));
						model.setValueAt(DataMiner.getMainRegex( (String) selected.toString(),(String) model.getValueAt(table.getSelectedRow(), 4), rep.getDateFormat(recording.getDateFormatID())), table.getSelectedRow(), 4);
						model.fireTableCellUpdated(table.getSelectedRow(), 4);
					}
				   }
				}
				);
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

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			if (getValueAt(0, c)!=null){
			return getValueAt(0, c).getClass();
			} else return String.class;
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matt&er where the cell appears onscreen.
			if (col > 7) {
				return false;
			} else {if (col==4 && !data.get(row)[3].equals("manual")){
				return false;
			} else{
				return true;
			}
			}
		}

	}

	public ArrayList<RecordingItem> getRecordingItems() {
		ArrayList<RecordingItem> toReturn = new ArrayList<RecordingItem>();
		for (int i = 0; i <data.size(); i++) { // model.getRowCount()
			logger.info((String) model.getValueAt(i, 0));
			//RecordingItem(String name, String before, String type, String processingType,            String insideRegex, String after, Boolean isSelected, Boolean show, String value) {
			toReturn.add(new RecordingItem((String) model.getValueAt(i, 0), (String) model.getValueAt(i, 2),
					(String) model.getValueAt(i, 3), (String) model.getValueAt(i, 1),
					(String) model.getValueAt(i, 4),(String) model.getValueAt(i, 5),
					(Boolean) model.getValueAt(i, 6), (Boolean) model.getValueAt(i, 7), (String) model.getValueAt(i, 8)));
			//private String[] header = { "Name", "Processing", "Before", "Inside type",            "Inside regex", "After", "Active", "Show", "Value" };
			//					rI.getName(), rI.getProcessingType(), rI.getBefore(), rI.getType(), inside,rI.getAfter(), rI.isSelected(),rI.isShow(), "" }); 
		}
		return toReturn;
	}

	public void Add() {
		data.add(new Object[] { "default", "occurrences", ".*", "long", DataMiner.getTypeString("long"), "", Boolean.TRUE,  Boolean.TRUE,"" });
		table.repaint();
	}

	public void Insert() {
		data.add(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1), new Object[] { "default", "occurrences", ".*", "long",
				 DataMiner.getTypeString("long"),"", Boolean.TRUE, Boolean.TRUE, "" });
		table.repaint();
	}

	public void Remove() {
		data.remove(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
		table.repaint();
	}
}