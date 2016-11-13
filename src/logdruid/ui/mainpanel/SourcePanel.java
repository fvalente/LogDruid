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
package logdruid.ui.mainpanel;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultRowSorter;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.RowSorter;
import javax.swing.SortOrder;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import logdruid.data.DateFormat;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.ui.MainFrame;
import logdruid.ui.dialog.DateSelector;
import logdruid.ui.dialog.FileChooserDialog;
import logdruid.util.DataMiner;
import logdruid.util.FileListing;
import logdruid.util.PatternCache;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.awt.Color;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.awt.Font;

import javax.swing.JSplitPane;

public class SourcePanel extends JPanel {
	private static Logger logger = Logger.getLogger(SourcePanel.class.getName());
	private JTextField basePathTextField;
	boolean DEBUG = false;
	private JTable table;
	private String[] header = { "name", "regexp", "Date Format", "active", "nb files", "size(MB)" };
	private Repository repository;
	private ArrayList<Object[]> data = new ArrayList<Object[]>();
	public MyTableModel2 model;
	static ArrayList<Source> sourceArrayList = null;
	private JTextPane txtpnTest = null;
	private Document doc;
	private MainFrame mainFrame;
	JCheckBox chckbxSubfolders = null;
	JCheckBox chckbxOnlyMatches = null;
	Map<Source, long[]> srcFileSummary = new HashMap<Source, long[]>();

	/**
	 * Create the panel.
	 */
	public SourcePanel(final Repository repo, final MainFrame _mainFrame) {
		mainFrame = _mainFrame;
		model = new MyTableModel2(data, header);
		repository = repo;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 575, 0 };
		gridBagLayout.rowHeights = new int[] { 45, 338, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JPanel descriptionPanel = new JPanel();
		GridBagConstraints gbc_descriptionPanel = new GridBagConstraints();
		gbc_descriptionPanel.insets = new Insets(0, 0, 5, 0);
		gbc_descriptionPanel.fill = GridBagConstraints.BOTH;
		gbc_descriptionPanel.gridx = 0;
		gbc_descriptionPanel.gridy = 0;
		add(descriptionPanel, gbc_descriptionPanel);
		descriptionPanel.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		descriptionPanel.add(panel, BorderLayout.EAST);

		JButton btnFolder = new JButton("Folder...");
		panel.add(btnFolder);

		JPanel panel_2 = new JPanel();
		descriptionPanel.add(panel_2, BorderLayout.CENTER);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 499 };
		gbl_panel_2.rowHeights = new int[] { 2, 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 1.0 };
		gbl_panel_2.rowWeights = new double[] { 1.0, 0.0, 1.0, 1.0 };
		panel_2.setLayout(gbl_panel_2);

		JPanel panel_5 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_5.getLayout();
		flowLayout.setVgap(2);
		flowLayout.setHgap(2);
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.insets = new Insets(0, 0, 5, 0);
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 0;
		gbc_panel_5.gridy = 0;
		panel_2.add(panel_5, gbc_panel_5);

		basePathTextField = new JTextField();
		basePathTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File temp = new File(basePathTextField.getText());
				if (temp.exists()) {
					repo.setBaseSourcePath(basePathTextField.getText());
					_mainFrame.setTitle("LogDruid - " + _mainFrame.configFile + " - " + basePathTextField.getText());
					// _mainFrame.setTitle("LogDruid - " +
					// _mainFrame.currentRepositoryFile + " - " +
					// basePathTextField.getText());
				}
			}
		});
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		panel_2.add(basePathTextField, gbc_textField);
		basePathTextField.setColumns(60);
		basePathTextField.setText(repo.getBaseSourcePath());

		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
		flowLayout_1.setHgap(2);
		flowLayout_1.setVgap(2);
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.insets = new Insets(0, 0, 5, 0);
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 2;
		panel_2.add(panel_4, gbc_panel_4);

		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_3.getLayout();
		flowLayout_2.setVgap(10);
		descriptionPanel.add(panel_3, BorderLayout.WEST);

		JLabel lblBasePath = new JLabel("Logs folder:");
		panel_3.add(lblBasePath);
		btnFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileChooserDialog fileChooserDialog;
				File temp = new File(basePathTextField.getText());
				if (temp.exists()) {
					fileChooserDialog = new FileChooserDialog(temp);
				} else {
					fileChooserDialog = new FileChooserDialog();
				}
				if ((fileChooserDialog != null) && (fileChooserDialog.isValidate())) {

					for (File file : fileChooserDialog.getSelectedFiles()) {
						repository.setBaseSourcePath(file.getAbsolutePath());
						basePathTextField.setText(file.getAbsolutePath());
						repo.setBaseSourcePath(file.getAbsolutePath());
						_mainFrame.setTitle("LogDruid - " + _mainFrame.configFile + " - " + basePathTextField.getText());
					}
				}
			}
		});

		JPanel tablePanel = new JPanel();
		GridBagConstraints gbc_tablePanel = new GridBagConstraints();
		gbc_tablePanel.fill = GridBagConstraints.BOTH;
		gbc_tablePanel.gridx = 0;
		gbc_tablePanel.gridy = 1;
		add(tablePanel, gbc_tablePanel);
		tablePanel.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		tablePanel.add(splitPane, BorderLayout.CENTER);

		JPanel fileMatchPanel = new JPanel();
		splitPane.setRightComponent(fileMatchPanel);
		fileMatchPanel.setLayout(new BorderLayout(0, 0));
		JPanel panel_1 = new JPanel();
		fileMatchPanel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		txtpnTest = new JTextPane();

		JScrollPane scrollPane = new JScrollPane(txtpnTest);
		panel_1.add(scrollPane);

		JPanel panel_6 = new JPanel();
		panel_6.setMinimumSize(new Dimension(0, 200));
		panel_6.setPreferredSize(new Dimension(0, 300));
		splitPane.setLeftComponent(panel_6);
		panel_6.setLayout(new BorderLayout(0, 0));

		JPanel buttonsPanel = new JPanel();
		panel_6.add(buttonsPanel, BorderLayout.SOUTH);

		JButton btnNew = new JButton("New");
		buttonsPanel.add(btnNew);

		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
				repository.deleteSource(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
			//	reloadTable();
				if (selectedRow == table.getRowCount()) {
					table.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
				} else if (selectedRow > 0) {
					table.setRowSelectionInterval(selectedRow, selectedRow);
				} else {
					table.setRowSelectionInterval(0, 0);
				}
				mainFrame.updateTreeSources(repository.getSources());
				refreshList();
				logger.info("selectedRow: " + selectedRow + " row count: " + table.getRowCount());

			}
		});
		buttonsPanel.add(btnDelete);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		buttonsPanel.add(separator);
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
			//	updateSources();
			//	reloadTable();
				if (selectedRow > 0) {
					table.setRowSelectionInterval(selectedRow, selectedRow);
				}
				mainFrame.updateTreeSources(repository.getSources());
				refreshList();
			//	model.fireTableDataChanged();

			}
		});

		chckbxSubfolders = new JCheckBox("sub-folders");
		chckbxSubfolders.setFont(new Font("Dialog", Font.BOLD, 11));
		chckbxSubfolders.setSelected(repo.isRecursiveMode());
		chckbxSubfolders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repo.setRecursiveMode(chckbxSubfolders.isSelected());
			}
		});
		buttonsPanel.add(chckbxSubfolders);

		chckbxOnlyMatches = new JCheckBox("only matches");
		chckbxOnlyMatches.setFont(new Font("Dialog", Font.BOLD, 11));
		chckbxOnlyMatches.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repo.setOnlyMatches(chckbxOnlyMatches.isSelected());
			}
		});
		buttonsPanel.add(chckbxOnlyMatches);
		chckbxOnlyMatches.setSelected(repo.isOnlyMatches());
		btnRefresh.setForeground(Color.BLUE);
		buttonsPanel.add(btnRefresh);

		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		buttonsPanel.add(separator_1);

		JButton btnCheck = new JButton("Get samplings");
		btnCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataMiner.populateRecordingSamples(repo);
			}
		});
		buttonsPanel.add(btnCheck);

		// scrollPane.add(txtpnTest);

		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int rowCount = table.getRowCount();
				if (rowCount==-1){rowCount=0;}
				//int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
				logger.info(repository);	
				logger.info(rowCount);
				model.addRow(new Object[] { "default", ".*", "none", Boolean.TRUE, 0, 0 });
				model.fireTableDataChanged();//(rowCount, rowCount);
				table.setRowSelectionInterval(rowCount, rowCount);

			//	 table.repaint();
			//	reloadTable();
/*				if (selectedRow >= 0) {
					table.setRowSelectionInterval(selectedRow, selectedRow);
				} else {
					table.setRowSelectionInterval(0, 0);
				}*/
			//	table.repaint();
				refreshList();
				mainFrame.updateTreeSources(repository.getSources());
			}
		});
		table = new JTable(model);
		initColumnSizes(table);
	//	setUpProcessingColumn(table,table.getColumnModel().getColumn(2));
		//table.setAutoCreateRowSorter(true);

	
		JScrollPane scrollPane_1 = new JScrollPane(table);
		panel_6.add(scrollPane_1);

		// tablePanel.add(table, BorderLayout.CENTER);
		table.getModel().addTableModelListener(new TableModelListener() {

			public void tableChanged(TableModelEvent e) {
				// if (!e.getValueIsAdjusting()){
				// model.fireTableDataChanged();
			//	int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
			//	updateSources();
				mainFrame.updateTreeSources(repository.getSources());
			//	reloadTable();
			//	table.setRowSelectionInterval(selectedRow, selectedRow);
				refreshList();
				// }
			}
		});

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					// model.fireTableDataChanged();
		//			int selectedRow = ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1);
		//			updateSources();
					mainFrame.updateTreeSources(repository.getSources());
			//		reloadTable();
			//		table.setRowSelectionInterval(selectedRow, selectedRow);
					refreshList();
				}
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				   if (table.getSelectedRow()!=-1){
					   int x = me.getX();
			            int y = me.getY();
			            int row = table.rowAtPoint(new Point(x,y));
			            int col = table.columnAtPoint(new Point(x,y));
			            if (col == 2)
			            {
					   DateSelector ds = new DateSelector(repository);
					   ds.setVisible(true);
					   if (ds.isValidate() && (ds.getDateFormat()!=null)){
						model.setValueAt(ds.getDateFormat(), table.getSelectedRow(), 2);
						model.fireTableCellUpdated(table.getSelectedRow(), 2);
						repository.getSource(row).setDateFormat((DateFormat)ds.getDateFormat());
						logger.debug("updated with " + ds.getDateFormat().getName() );   
						}
						
			            }}
			}
			});
			

	

		reloadTable();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
		refreshList();
		
	/* if (model.getRowCount()>0){
			table.getRowSorter().toggleSortOrder(2);
			table.getRowSorter().toggleSortOrder(0);
			}*/
	}

	public void refreshList() {
		// ((table.getSelectedRow()!=-1)?table.convertRowIndexToModel(table.getSelectedRow()):-1)
		// persist repository
		// display selected row
		PatternCache patternCache = new PatternCache();
		Matcher matcher;
		List<File> listOfFiles = null;
		srcFileSummary = new HashMap<Source, long[]>();
		sourceArrayList = repository.getSources();
		if (((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1) >= 0 && repository.getBaseSourcePath() != null) {
			/*
			 * recEditor = new RecordingEditor(repository
			 * .getRecordings().get(((
			 * table.getSelectedRow()!=-1)?table.convertRowIndexToModel
			 * (table.getSelectedRow()):-1)), repository);
			 * jPanelDetail.removeAll();
			 */
			// jPanelDetail.add(recEditor, gbc_jPanelDetail);

			File folder = new File(repository.getBaseSourcePath());
			try {
				if (!folder.exists()) {
					if (logger.isEnabledFor(Level.INFO))
						logger.info("base path did, not exist defaulting to current: " + System.getProperty("user.dir"));
					folder = new File(System.getProperty("user.dir"));
					repository.setBaseSourcePath(System.getProperty("user.dir"));
					basePathTextField.setText(System.getProperty("user.dir"));
					repository.setRecursiveMode(false);
					chckbxSubfolders.setSelected(false);
				}
				if (repository.isRecursiveMode()) {
					long estimatedTime = 0;
					long startTime = System.currentTimeMillis();

					listOfFiles = FileListing.getFileListing(folder);
					estimatedTime = System.currentTimeMillis() - startTime;
					logger.debug("gathering time: " + estimatedTime);
				} else {
					listOfFiles = Arrays.asList(folder.listFiles());

				}
				// File[] listOfFiles = folder.listFiles();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int[][] fileListMatches = new int[listOfFiles.size()][3];
			doc = txtpnTest.getDocument();
			try {
				doc.remove(0, doc.getLength());
			} catch (BadLocationException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			int count = 0;
			for (int i = 0; i < listOfFiles.size(); i++) {
				fileListMatches[i][0] = 0;
				if (listOfFiles.get(i).isFile()) {
					if (logger.isDebugEnabled())
						logger.debug("source File " + listOfFiles.get(i).getName());
					if (sourceArrayList != null) {
						Iterator<Source> it = sourceArrayList.iterator();
						count = 0;
						while (it.hasNext()) {
							Source src = (Source) it.next();

							String s1 = (src).getSourcePattern();
							matcher = patternCache.getPattern(s1,true).matcher(FileListing.getPath(repository, listOfFiles.get(i)));
							if (logger.isDebugEnabled())
								logger.debug("matching with pattern: " + s1);
							if (logger.isDebugEnabled())
								logger.debug("matching file: " + FileListing.getPath(repository, listOfFiles.get(i)));

							if (matcher.find()) {
								if (logger.isDebugEnabled())
									logger.debug("match" + FileListing.getPath(repository, listOfFiles.get(i)));
								if (count == ((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1)) {
									if (src.getActive()) {
										fileListMatches[i][0] = 1;
									} else {
										fileListMatches[i][0] = 3;
									}
									fileListMatches[i][1] = matcher.start();
									fileListMatches[i][2] = matcher.end();
									if (logger.isDebugEnabled())
										logger.debug("matched file" + FileListing.getPath(repository, listOfFiles.get(i)));
								} else if (fileListMatches[i][0] == 0) {
									if (logger.isDebugEnabled())
										logger.debug("Already matched file" + FileListing.getPath(repository, listOfFiles.get(i)));
									if (src.getActive()) {
										fileListMatches[i][0] = 2;
									} else {
										fileListMatches[i][0] = 3;
									}

								}
								if (!srcFileSummary.containsKey(src)) {
									srcFileSummary.put(src, new long[] { 0, 0 });
								}
								long[] intTable = srcFileSummary.get(src);
								intTable[0] = intTable[0] + 1;
								intTable[1] = intTable[1] + listOfFiles.get(i).length();
								srcFileSummary.put(src, intTable);

								fileListMatches[i][1] = matcher.start();
								fileListMatches[i][2] = matcher.end();
							} else {
								// fileListMatches[i][0]=0;
								if (logger.isDebugEnabled())
									logger.debug("not matched file" + FileListing.getPath(repository, listOfFiles.get(i)));
							}

							count++;
						}
					}
				}
			}
			try {
				// txtpnTest.setText( "original text" );
				for (int i = 0; i < fileListMatches.length; i++) {
					int currIndex = doc.getLength();
					// String relative = new
					// File(repository.getBaseSourcePath()).toURI().relativize(new
					// File(listOfFiles.get(i).getCanonicalPath()+listOfFiles.get(i).getName()).toURI()).getPath();
					// logger.info("match added");
					Highlighter h = txtpnTest.getHighlighter();
					if (fileListMatches[i][0] == 1) {
						String fileName = FileListing.getPath(repository, listOfFiles.get(i));
						doc.insertString(doc.getLength(), fileName + "\n", null);
						txtpnTest.setCaretPosition(0);
						h.addHighlight(currIndex + fileListMatches[i][1], currIndex + fileListMatches[i][2], new DefaultHighlighter.DefaultHighlightPainter(
								Color.ORANGE));
						if (logger.isDebugEnabled())
							logger.debug("matched - l: " + i + "file" + FileListing.getPath(repository, listOfFiles.get(i)) + " currIndex : " + currIndex
									+ ", match start: " + fileListMatches[i][1] + ", match end: " + fileListMatches[i][2]);
					} else if (fileListMatches[i][0] == 2) {
						String fileName = FileListing.getPath(repository, listOfFiles.get(i));
						doc.insertString(doc.getLength(), fileName + "\n", null);
						txtpnTest.setCaretPosition(0);
						h.addHighlight(currIndex + fileListMatches[i][1], currIndex + fileListMatches[i][2], DefaultHighlighter.DefaultPainter);
						if (logger.isDebugEnabled())
							logger.debug("ALREADY matched - l: " + i + "file" + listOfFiles.get(i).getName() + " currIndex : " + currIndex + ", match start: "
									+ fileListMatches[i][1] + ", match end: " + fileListMatches[i][2]);

					} else if (fileListMatches[i][0] == 3) {
						String fileName = FileListing.getPath(repository, listOfFiles.get(i));
						doc.insertString(doc.getLength(), fileName + "\n", null);
						txtpnTest.setCaretPosition(0);
						h.addHighlight(currIndex + fileListMatches[i][1], currIndex + fileListMatches[i][2], new DefaultHighlighter.DefaultHighlightPainter(
								Color.lightGray));
						if (logger.isDebugEnabled())
							logger.debug("ALREADY matched - l: " + i + "file" + listOfFiles.get(i).getName() + " currIndex : " + currIndex + ", match start: "
									+ fileListMatches[i][1] + ", match end: " + fileListMatches[i][2]);

					} else if (!repository.isOnlyMatches()) {
						String fileName = FileListing.getPath(repository, listOfFiles.get(i));
						doc.insertString(doc.getLength(), fileName + "\n", null);
						txtpnTest.setCaretPosition(0);
					}
				}
				// txtpnTest.setText( "original text" );
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		sourceArrayList = repository.getSources();
		if (sourceArrayList != null) {
			Iterator<Source> it = sourceArrayList.iterator();
			int count = 0;
			data.clear();
			// this.repaint();
			while (it.hasNext()) {
				Source record = (Source) it.next();
				logger.debug("reloadTable - record reloaded : " + count + ", " + record + ", " + record.getSourcePattern() + ", " + record.getActive()
						+ " srcFileSummary.get(record): " + srcFileSummary.get(record));
				data.add(new Object[] { record.getSourceName(), record.getSourcePattern(), record.getDateFormat(),record.getActive(),
						srcFileSummary.get(record) != null ? srcFileSummary.get(record)[0] : 0,
						srcFileSummary.get(record) != null ? ((float) srcFileSummary.get(record)[1]) / 1024000 : 0 });
			}
			table.revalidate();
			table.repaint();
		}

	}

/*	public void updateSources() {
		// "name", "regexp", "active"
		Iterator it = data.iterator();
		Object[] obj;
		int count = 0;
		while (it.hasNext()) {
			obj = (Object[]) it.next();
			String stName = (String) obj[0];
			String stregexp = (String) obj[1];
			Boolean stActive = (Boolean) obj[2];
			// logger.info("stType: " +stType);
			// logger.info("getTypeString(stType) -: "
			// +Miner.getTypeString(stType) );
			// patternString += stBefore + "(" + Miner.getTypeString(stType) +
			// ")"+stAfter;
			Source source = repository.getSource(count);
			source.setSourceName(stName);
			source.setSourcePattern(stregexp);
			source.setActive(stActive);
			// repository.updateSource(count,stName,stregexp, stActive);
			logger.info("updateSources - Source updated : " + stName + ",regexp: " + stregexp);
			count++;
		}
	}*/

	public void reloadTable() {
		// int selectedRow = ((table.getSelectedRow() != -1) ?
		// table.convertRowIndexToModel(table.getSelectedRow()) : -1);
		sourceArrayList = repository.getSources();
		// Collections.sort(records);
		if (sourceArrayList != null) {
			Iterator<Source> it = sourceArrayList.iterator();
			int count = 0;
			data.clear();
			// this.repaint();
			while (it.hasNext()) {
				Source record = (Source) it.next();
				data.add(new Object[] { record.getSourceName(), record.getSourcePattern(), record.getActive(), 0, 0 });
				logger.debug("reloadTable - record reloaded : " + count + ", " + record + ", " + record.getSourcePattern() + ", " + record.getActive());
			}
			table.revalidate();
		}
	}

	private void initColumnSizes(JTable theTable) {
		// MyTableModel2 model = (MyTableModel2) theTable.getModel();
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int cellWidth = 0;
		TableCellRenderer headerRenderer = theTable.getTableHeader().getDefaultRenderer();

		for (int i = 0; i < 6; i++) {
			column = theTable.getColumnModel().getColumn(i);
			comp = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
			if (i < 3) {
				headerWidth = comp.getPreferredSize().width;
				cellWidth = comp.getPreferredSize().width;
				column.setPreferredWidth(400);
			} else {
				cellWidth = 140;
				column.setMaxWidth(cellWidth * 4);
				column.setPreferredWidth(cellWidth);
			}

		}	
	}

	class DateFormatRenderer extends JLabel implements TableCellRenderer {
		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public DateFormatRenderer(boolean isBordered) {
			this.isBordered = isBordered;
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
			Color newColor = (Color) color;
			setBackground(newColor);
			if (isBordered) {
				if (isSelected) {
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}

			setToolTipText("RGB value: " + newColor.getRed() + ", " + newColor.getGreen() + ", " + newColor.getBlue());
			return this;
		}
	}

/*	public void setUpProcessingColumn(JTable theTable, TableColumn typeColumn) {
		JTextField textField= new JTextField();
		typeColumn.setCellEditor(new DefaultCellEditor(textField));
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click");
		typeColumn.setCellRenderer(renderer);
		  

		textField.addMouseListener(new java.awt.event.MouseAdapter(){

			public void mouseClicked(java.awt.event.MouseEvent e){
				//		JTextField combo = (JTextField) e.getSource();
			//	   Object selected = combo.get();
				   if (table.getSelectedRow()!=-1){
					   DateSelector ds = new DateSelector(repository);
					   if (ds.getDateFormat()!=null)
						model.setValueAt(ds.getDateFormat(), table.getSelectedRow(), 4);
						model.fireTableCellUpdated(table.getSelectedRow(), 4);
					}
				   }
				}
				);
	}
*/
	
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
			Source s = new Source("default", ".*", null, Boolean.TRUE);
			repository.addSource(s);
			data.add(obj);
		}

		public void updateRow(int rowId, Object[] obj) {
			data.set(rowId, obj);
		}

		@Override
		public Object getValueAt(int row, int column) {
			//return data.get(row)[column];
			if (column == 0 ) {
				return repository.getSource(row).getSourceName();
			} else if (column == 1 ) {
				return repository.getSource(row).getSourcePattern();
			} else if (column == 2 ) {
				if ( repository.getSource(row).getDateFormat()!=null)
				return ((String) repository.getSource(row).getDateFormat().getName() + " | "+ repository.getSource(row).getDateFormat().getDateFormat() );
				else {
					return "none";
				}
			} else if (column == 3 ) {
				return repository.getSource(row).getActive();
			} else if (column >= 4 ) {
				return data.get(row)[column];
			} 
			else return 0;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column == 0 ) {
				 repository.getSource(row).setSourceName((String) value);
					fireTableCellUpdated(row, column);
			} else if (column == 1 ) {
				repository.getSource(row).setSourcePattern((String) value);
				fireTableCellUpdated(row, column);
			} else if (column == 2 ) {
				repository.getSource(row).setDateFormat((DateFormat)value);
				logger.info("set "+((DateFormat)value).getPattern());
				fireTableCellUpdated(row, column);
			} else if (column == 3 ) {
				repository.getSource(row).setActive((Boolean)value);
				fireTableCellUpdated(row, column);
			} else {
				((Object[]) data.get(row))[column] = value;
					fireTableCellUpdated(row, column);
			}
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			if (c==2){
				return String.class;
			} else {
			return getValueAt(0, c).getClass();
			}
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matt&er where the cell appears onscreen.
			if (col<4){
				return true;	
			} else return false;
		}
	}

	public void Remove() {
		data.remove(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
		repository.deleteRecording(((table.getSelectedRow() != -1) ? table.convertRowIndexToModel(table.getSelectedRow()) : -1));
		table.repaint();
	}
}
