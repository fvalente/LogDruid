
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
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

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * RecordingEditorTable.java requires no other files.
 */

/**
 * RecordingEditorTable is just like TableDemo, except that it explicitly
 * initializes column sizes and it uses a combo box as an editor for the Sport
 * column.
 */

public class StatRecordingEditorTable extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private boolean DEBUG = false;
	static Pattern sepPattern = Pattern.compile("(.*), (.*)");
	static Pattern equalPattern = Pattern.compile("(.*)=(.*)");
	static Matcher m;
	static Vector records = null;
	private MyTableModel model;
	private String[] header = { "Name", "Before", "Type", "After", "selected","Value" };
	private ArrayList<Object[]> data = new ArrayList<Object[]>();
	JTable table = null;
	private String theLine = "";
	private JTextPane examplePane;
	private Repository rep=null;
	private Recording recording;
	/**
	 * @wbp.parser.constructor
	 */
	@SuppressWarnings("unchecked")
	public StatRecordingEditorTable(JTextPane textPane) {
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

		// Fiddle with the Sport column's cell editors/renderers.
		setUpSportColumn(table, table.getColumnModel().getColumn(2));

		// Add the scroll pane to this panel.
		add(scrollPane);
		Add();
		FixValues();

	}

	public StatRecordingEditorTable(Repository repo, Recording re, JTextPane textPane) {
		super(new GridLayout(1, 0));
		this.examplePane = textPane;
		model = new MyTableModel(data, header);
		table = new JTable(model);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);
		rep=repo;
		this.theLine = textPane.getText();
		recording=re;
		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);
		// Set up column sizes.
		initColumnSizes(table);
		// Fiddle with the Sport column's cell editors/renderers.
		setUpSportColumn(table, table.getColumnModel().getColumn(2));
		// Add the scroll pane to this panel.
		add(scrollPane);
		
		if (re.getClass()==EventRecording.class)
		{
			records = ((EventRecording)re).getRecordingItem();
		} else if(re.getClass()==StatRecording.class) 
		{
			records = ((StatRecording)re).getRecordingItem();			
		} else if (re.getClass()==MetadataRecording.class) 
		{
			records = ((MetadataRecording)re).getRecordingItem();			
		}
		// Collections.sort(records);
		if (records != null){
		Iterator it = records.iterator();

		while (it.hasNext()) {
			RecordingItem rI = (RecordingItem) it.next();
			data.add(new Object[] { rI.getName(),rI.getBefore(), rI.getType(),rI.getAfter(), rI.isSelected(),"" });
		}
		FixValues();
		}
	}

	public void FixValues() {
		String patternString = "";
		Matcher matcher;
		
		Iterator it = data.iterator();
		Object[] obj;

		while (it.hasNext()) {
			obj = (Object[]) it.next();
			String stBefore = (String) obj[1];
			String stType = (String) obj[2];
			String stAfter = (String) obj[3];
			logger.info("stType: " +stType);
			if(stType.equals("date") && rep.getDateFormat(recording.getDateFormatID()).getPattern()!=null){
				patternString += stBefore + "(" + rep.getDateFormat(recording.getDateFormatID()).getPattern() + ")" + stAfter;
				logger.info("getTypeString(stType) getPattern -: " +rep.getDateFormat(recording.getDateFormatID()).getPattern());
				logger.info("getTypeString(stType) getDateFormat -: " +rep.getDateFormat(recording.getDateFormatID()).getDateFormat());
			}else {
				patternString += stBefore + "(" + DataMiner.getTypeString(stType) + ")" + stAfter;
				logger.info("getTypeString(stType) -: " +DataMiner.getTypeString(stType) );
			}
		//	logger.info("getTypeString(stType) -: " +DataMiner.getTypeString(stType) );
	//		patternString += stBefore + "(" + DataMiner.getTypeString(stType) + ")"+stAfter;
		}

		try {
			logger.info("theLine: "+examplePane.getText());
			logger.info("patternString: " +patternString);
			matcher = PatternCache.getPattern(patternString).matcher(examplePane.getText());
			Highlighter h = examplePane.getHighlighter();
			h.removeAllHighlights();	
			int currIndex =0;
			if (matcher.find()) {
				//int currIndex = 0;
				// doc.insertString(doc.getLength(),line+"\n", null);
				

				for (int i=1;i<=matcher.groupCount();i++){
					model.setValueAt(matcher.group(i), i-1, 5);
					h.addHighlight(matcher.start(i),	 + matcher.end(i), new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE));
					logger.info("matcher.start(i): " +matcher.start(i) + "matcher.end(i): "+matcher.end(i));
				}
				
			}

		} catch (Exception e1) {
			e1.printStackTrace();
//			System.exit(1);
		}

	}



	
	/*
	 * This method picks good column sizes. If all column heads are wider than
	 * the column's cells' contents, then you can just use
	 * column.sizeWidthToFit().
	 */
	private void initColumnSizes(JTable theTable) {
		MyTableModel model = (MyTableModel) theTable.getModel();
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int cellWidth = 0;
		// Object[] longValues = model.longValues;
		TableCellRenderer headerRenderer = theTable.getTableHeader()
				.getDefaultRenderer();

		for (int i = 0; i < 6; i++) {
			column = theTable.getColumnModel().getColumn(i);

			comp = headerRenderer.getTableCellRendererComponent(null,
					column.getHeaderValue(), false, false, 0, 0);
			headerWidth = comp.getPreferredSize().width;

			/*
			 * comp = table.getDefaultRenderer(model.getColumnClass(i)).
			 * getTableCellRendererComponent( table, longValues[i], false,
			 * false, 0, i);
			 */
			cellWidth = comp.getPreferredSize().width;

			if (DEBUG) {
				logger.info("Initializing width of column " + i + ". "
						+ "headerWidth = " + headerWidth + "; cellWidth = "
						+ cellWidth);
			}

			column.setPreferredWidth(Math.max(headerWidth, cellWidth));
		}
	}

	

	public void setUpSportColumn(JTable theTable, TableColumn sportColumn) {
		// Set up the editor for the sport cells.
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("word");
		comboBox.addItem("string");
		comboBox.addItem("integer");
		comboBox.addItem("long");
		comboBox.addItem("date");
		comboBox.addItem("percent");
		sportColumn.setCellEditor(new DefaultCellEditor(comboBox));

		// Set up tool tips for the sport cells.
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click for combo box");
		sportColumn.setCellRenderer(renderer);
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
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matt&er where the cell appears onscreen.
			if (col > 4) {
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
		 * logger.info("Setting value at " + row + "," + col + " to " +
		 * value + " (an instance of " + value.getClass() + ")"); }
		 * 
		 * data[row][col] = value; fireTableCellUpdated(row, col);
		 * 
		 * if (DEBUG) { logger.info("New value of data:");
		 * printDebugData(); } }
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
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("RecordingEditorTable");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		StatRecordingEditorTable newContentPane = new StatRecordingEditorTable(
				new JTextPane());
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	public Vector<RecordingItem> getRecordingItems() {
		Vector<RecordingItem> toReturn = new Vector<RecordingItem>();
		for (int i = 0; i < data.size(); i++) { // model.getRowCount()
			toReturn.add(new RecordingItem(
					(String) model.getValueAt(i, 0),
					(String) model.getValueAt(i, 1),
					(String) model.getValueAt(i, 2),
					(String) model.getValueAt(i, 3),
					(Boolean)model.getValueAt(i, 4),
					(String) model.getValueAt(i, 5)));
		}
		return toReturn;
	}
	
	
	public void Add() {
		data.add(new Object[] { "", ".*", "integer", "", Boolean.TRUE, "" });
		table.repaint();
	}

	public void Remove() {
		data.remove(table.getSelectedRow());
		table.repaint();
	}

/*	private Vector<RecordingItem> findRecordItems(String theLine) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"EEE MM/dd/yy HH:mm:ss");
		String[] rIString = theLine.split(", ");
		Vector<RecordingItem> rI = new Vector<RecordingItem>();
		for (int i = 0; i < rIString.length; i++) {
			if (i == 0) {
				String[] splitted = rIString[i].split(": ");
				String date = splitted[0];
				try {
					simpleDateFormat.parse(date);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				if (rIString[i].contains("=")) {
					String[] splitted = rIString[i].split("=");
					String name = splitted[0];
					String value = splitted[1];
					// rI.add(new RecordingItem(name,value,true));
					logger.info(name + " " + value);
				}
			}
		}
		return rI;
	}
*/
}