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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
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
import logdruid.ui.NewRecordingList;
import logdruid.ui.NewRecordingList.MyTableModel2;
import logdruid.ui.editor.EventRecordingEditor;
import logdruid.ui.editor.MetadataRecordingEditor;
import logdruid.ui.editor.StatRecordingEditor;
import logdruid.util.DataMiner;

import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

public class StatRecordingSelectorPanel extends JPanel {
	private static Logger logger = Logger.getLogger(StatRecordingSelectorPanel.class.getName());
	public static JTable table;
	JPanel jPanelDetail;
	boolean DEBUG = false;
	static Pattern sepPattern = Pattern.compile("(.*), (.*)");
	static Pattern equalPattern = Pattern.compile("(.*)=(.*)");
	static Matcher m;
	static Vector records = null;
	private String[] header = { "name", "regexp", "type", "active" };
	private ArrayList<Object[]> data = new ArrayList<Object[]>();
	private Repository repository = null;
	private JPanel recEditor = null;
	public logdruid.ui.mainpanel.StatRecordingSelectorPanel.MyTableModel model;
	private Source source;

	/**
	 * Create the panel.
	 */
	public StatRecordingSelectorPanel(final Repository rep, Source src) {
		repository = rep;
		model = new logdruid.ui.mainpanel.StatRecordingSelectorPanel.MyTableModel(data, header);
		source = src;
		logger.info("source is " + ((source == null) ? "null" : src.getSourceName()));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 15, 550, 15 };
		gridBagLayout.rowHeights = new int[] { 152, 0, 0, 300 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0 };
		setLayout(gridBagLayout);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.insets = new Insets(5, 0, 5, 5);
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 0;
		add(panel_1, gbc_panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table);
		panel_1.add(scrollPane, BorderLayout.CENTER);

		table.setPreferredScrollableViewportSize(new Dimension(0, 0));
		table.setFillsViewportHeight(true);

		// Set up column sizes.
		initColumnSizes(table);
		reloadTable();
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setVgap(2);
		flowLayout.setHgap(2);
		panel_1.add(panel, BorderLayout.SOUTH);

		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.anchor = GridBagConstraints.SOUTHWEST;
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.insets = new Insets(0, 0, 5, 5);
		gbc_separator.gridx = 1;
		gbc_separator.gridy = 1;
		add(separator, gbc_separator);

		JSeparator separator_1 = new JSeparator();
		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.insets = new Insets(0, 0, 5, 5);
		gbc_separator_1.gridx = 1;
		gbc_separator_1.gridy = 2;
		add(separator_1, gbc_separator_1);

		jPanelDetail = new JPanel();
		GridBagConstraints gbc_jPanelDetail = new GridBagConstraints();
		gbc_jPanelDetail.insets = new Insets(0, 0, 0, 5);
		gbc_jPanelDetail.fill = GridBagConstraints.BOTH;
		gbc_jPanelDetail.gridx = 1;
		gbc_jPanelDetail.gridy = 3;
		add(jPanelDetail, gbc_jPanelDetail);
		jPanelDetail.setLayout(new BorderLayout(0, 0));
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				// table.getSelectedRow()
				// persist repository
				// display selected row
				int selectedRow = table.getSelectedRow();

				logger.info("ListSelectionListener - selectedRow: " + selectedRow);
				if (selectedRow >= 0) {

					if (jPanelDetail != null) {
						logger.info("ListSelectionListener - valueChanged");
						jPanelDetail.removeAll();
						recEditor = getEditor(repository.getRecording(StatRecording.class, selectedRow));
						if (recEditor != null) {
							jPanelDetail.add(recEditor, BorderLayout.CENTER);
						}
						reloadTable();
						jPanelDetail.revalidate();
					}
				}
			}
		});
		if (repository.getRecordings(StatRecording.class).size() > 0) {
			recEditor = getEditor(repository.getRecording(0));
			jPanelDetail.add(recEditor, BorderLayout.CENTER);
			table.setRowSelectionInterval(0, 0);
		}
		reloadTable();
		// jPanelDetail.revalidate();

	}

	private JPanel getEditor(Recording rec) {
		JPanel editorPanel = null;
		if (rec.getClass() == StatRecording.class) {
			editorPanel = new StatRecordingEditor(repository, rec.getExampleLine(), rec.getRegexp(), ((StatRecording) rec));
		} else if (rec.getClass() == MetadataRecording.class) {
			editorPanel = new MetadataRecordingEditor(repository, rec.getExampleLine(), rec.getRegexp(), ((MetadataRecording) rec));
		} else if (rec.getClass() == EventRecording.class) {
			editorPanel = new EventRecordingEditor((logdruid.ui.NewRecordingList.MyTableModel2) table.getModel(), repository, rec.getExampleLine(),
					rec.getRegexp(), ((EventRecording) rec));
		}
		return editorPanel;
	}

	public void reloadTable() {
		int selectedRow = table.getSelectedRow();
		records = repository.getRecordings(StatRecording.class);
		logger.info("reloadTable - nb records : " + records.size());
		// Collections.sort(records);
		Iterator it = records.iterator();
		int count = 0;
		data.clear();
		// this.repaint();

		logger.info("reloadTable - 1");
		while (it.hasNext()) {
			Recording record = (Recording) it.next();
			data.add(new Object[] { record.getName(), record.getRegexp(), record.getType(), record.getIsActive() });
			logger.info(count + record.getName() + record.getRegexp() + record.getIsActive());
		}
		// model.fireTableDataChanged();
		logger.info("reloadTable - 2");
		// this.repaint();
		table.repaint();

		logger.info("reloadTable - 3");
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

			if (DEBUG) {
				logger.info("Initializing width of column " + i + ". " + "headerWidth = " + headerWidth + "; cellWidth = " + cellWidth);
			}

			column.setPreferredWidth(Math.max(headerWidth, cellWidth));
		}
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
			if (source != null) {
				// logger.info("source not null");
			}
			if (column == 3 && source != null) {
				logger.info("getvalueat name" + ((StatRecording) repository.getRecording(StatRecording.class, row)).getName());
				logger.info("getvalueat is active" + source.isActiveRecordingOnSource(repository.getRecording(StatRecording.class, row)));
				return source.isActiveRecordingOnSource(repository.getRecording(StatRecording.class, row));
			} else {
				// logger.info("source null");
				return data.get(row)[column];
			}
		}

		public void addRow(Object[] obj) {
			data.add(obj);
			table.repaint();
		}

		public void updateRow(int rowId, Object[] obj) {
			data.set(rowId, obj);
			table.repaint();
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column == 3 && source != null) {
				logger.info("setValueAt calls setActiveRecording");
				source.toggleActiveRecording(repository.getRecording(StatRecording.class, row));
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
	}

	public void Add() {
		data.add(new Object[] { "", ".*", "integer", Boolean.FALSE });
		table.repaint();
	}

	/*
	 * public void Remove() { data.remove(table.getSelectedRow());
	 * table.repaint(); }
	 */

	public void Remove() {
		data.remove(table.getSelectedRow());
		repository.deleteRecording(table.getSelectedRow());
		table.repaint();
	}

	/*
	 * private Vector<RecordingItem> findRecordItems(String theLine) {
	 * SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
	 * "EEE MM/dd/yy HH:mm:ss"); String[] rIString = theLine.split(", ");
	 * Vector<RecordingItem> rI = new Vector<RecordingItem>(); for (int i = 0; i
	 * < rIString.length; i++) { if (i == 0) { String[] splitted =
	 * rIString[i].split(": "); String date = splitted[0]; try {
	 * simpleDateFormat.parse(date); } catch (ParseException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } } else { if
	 * (rIString[i].contains("=")) { String[] splitted = rIString[i].split("=");
	 * String name = splitted[0]; String value = splitted[1]; // rI.add(new
	 * RecordingItem(name,value,true)); logger.info(name + " " + value); } } }
	 * return rI; }
	 */

}
