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
package logdruid.ui.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Label;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JTextPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

import java.awt.Color;

import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import logdruid.data.DataVault;
import logdruid.data.Repository;
import logdruid.data.record.ReportRecording;
import logdruid.data.record.Recording;
import logdruid.ui.dialog.DateSelector;
import logdruid.ui.mainpanel.RecordingList;
import logdruid.ui.mainpanel.ReportRecordingSelectorPanel;
import logdruid.ui.mainpanel.RecordingList.MyTableModel2;
import logdruid.ui.table.ReportRecordingEditorTable;
import logdruid.util.DataMiner;
import logdruid.util.PatternCache;

import javax.swing.SwingConstants;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;

import org.apache.log4j.Logger;

import java.awt.Font;

import javax.swing.border.EtchedBorder;
import javax.swing.border.BevelBorder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JSplitPane;
import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class ReportRecordingEditor extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private JPanel _this = this;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtName;
	private JTextField txtRegularExp;
	private JTextField txtDate;
	JPanel panel2;
	private ReportRecordingEditorTable reportRecordingEditorTablePanel;
	private Repository repository;
	DefaultListModel listModel;
	JTextPane examplePane = new JTextPane();
	private ReportRecording recording;
	Document doc;
	JCheckBox chckbxActive;
	JCheckBox chckbxCaseSensitive;
	private JLabel nameLabel;
	private JLabel regularExpressionLabel;
	private JLabel dateFormatLabel;
	JTextPane textPane ;
	JComboBox comboBox ;
	// private JList recordingList;

	/*
	 * public EventRecordingEditor(Repository repository2, String exampleLine,
	 * String regexp, ReportRecording eventRecording) { this(null, repository2,
	 * exampleLine, regexp, eventRecording); }
	 */

	/**
	 * Create the dialog.
	 */
	public ReportRecordingEditor(final JPanel newRecordingList, ReportRecording re, Repository repo) {
		this(newRecordingList, repo, re.getExampleLine(), re.getRegexp(), re, re.getSubType());
	}

	public ReportRecordingEditor(Repository repository2, String exampleLine, String regexp, ReportRecording reportRecording) {
		this(null, repository2, exampleLine, regexp, reportRecording, regexp);
	}
	/**
	 * Create the dialog.
	 * @param typeString 
	 * @param string 
	 * 
	 * @wbp.parser.constructor
	 */
	public ReportRecordingEditor(final JPanel newRecordingList, Repository repo, String theLine, String regex, final ReportRecording re, String typeString) {
		// setBounds(0, 0, 1015, 467);

		repository = repo;
		recording = re;
		// logger.info("myTableModel2 null? "+(newRecordingList==null));
		BorderLayout borderLayout = new BorderLayout();
		this.setLayout(borderLayout);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		{
			JSplitPane splitPane = new JSplitPane();
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			contentPanel.add(splitPane, BorderLayout.CENTER);
			{
				JPanel panelTop = new JPanel();
				splitPane.setLeftComponent(panelTop);
				{
					panelTop.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_1a = new JPanel();
						panelTop.add(panel_1a, BorderLayout.NORTH);
						panel_1a.setLayout(new GridLayout(0, 4, 5, 2));
						{
							JPanel namePanel = new JPanel();
							panel_1a.add(namePanel);
							namePanel.setLayout(new BorderLayout(0, 0));
							{
								nameLabel = new JLabel("name:");
								namePanel.add(nameLabel, BorderLayout.WEST);
							}
							nameLabel.setLabelFor(txtName);
							{
								txtName = new JTextField();
								namePanel.add(txtName);
								txtName.setText("name");
								txtName.setColumns(5);
							}
						}
						{
							JPanel regularExpressionPanel = new JPanel();
							panel_1a.add(regularExpressionPanel);
							regularExpressionPanel.setLayout(new BorderLayout(0, 0));
							{
								regularExpressionLabel = new JLabel("reg. exp.:");
								regularExpressionPanel.add(regularExpressionLabel, BorderLayout.WEST);
							}
							{
								txtRegularExp = new JTextField();
								regularExpressionLabel.setLabelFor(txtRegularExp);
								regularExpressionPanel.add(txtRegularExp);
								txtRegularExp.addCaretListener(new CaretListener() {
									public void caretUpdate(CaretEvent e) {
										doc = examplePane.getDocument();
										PatternCache patternCache = new PatternCache();
										Highlighter h = examplePane.getHighlighter();
										h.removeAllHighlights();
										Pattern pattern = patternCache.getPattern(txtRegularExp.getText(),re.isCaseSensitive());
										String[] lines = examplePane.getText().split(System.getProperty("line.separator"));
										int currIndex = 0;
										if (lines.length>=1){
										for (int i=0; i<lines.length ; i++)
										{
											logger.info("line: "+lines[i]);
											
											Matcher matcher = pattern.matcher(lines[i]);
											if (matcher.find()) {
												// int currIndex=doc.getLength();
												// doc.insertString(doc.getLength(),line+"\n",
												// null);
												try {
													h.addHighlight(currIndex+matcher.start(), currIndex+matcher.end(), DefaultHighlighter.DefaultPainter);
												} catch (BadLocationException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
											}
											logger.debug("currIndex: "+currIndex+",length: "+lines[i].length());
											currIndex +=lines[i].length()+1 ;
										}
										}
									}
								});
								txtRegularExp.setText(regex);
								txtRegularExp.setColumns(10);
							}
						}
						{
							JPanel panel_2 = new JPanel();
							panel_1a.add(panel_2);
							panel_2.setLayout(new BorderLayout(0, 0));
							{
								dateFormatLabel = new JLabel("Date Format:\n");
								panel_2.add(dateFormatLabel, BorderLayout.WEST);
							}
							{
								txtDate = new JTextField();
								dateFormatLabel.setLabelFor(txtDate);
								txtDate.setEditable(false);
								panel_2.add(txtDate, BorderLayout.CENTER);
								txtDate.setText("date format");
								txtDate.setColumns(10);
							}
							{
								JButton button = new JButton("...");
								button.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										DateSelector dateDialog = new DateSelector(repository, txtDate, re);
										dateDialog.validate();
										dateDialog.setResizable(true);
										dateDialog.setModal(false);
										dateDialog.setVisible(true);
									}
								});
								button.setFont(new Font("Dialog", Font.BOLD, 6));
								panel_2.add(button, BorderLayout.EAST);
							}
						}
						{
							chckbxActive = new JCheckBox("active");
							chckbxActive.setSelected(true);
							panel_1a.add(chckbxActive);
						}
						{
							comboBox = new JComboBox();
							comboBox.setModel(new DefaultComboBoxModel(new String[] {"histogram", "top100","sum"}));
							panel_1a.add(comboBox);
						}
						{
							chckbxCaseSensitive = new JCheckBox("case sensitive");
							chckbxCaseSensitive.setSelected(re.isCaseSensitive());
							panel_1a.add(chckbxCaseSensitive);
						}
						{
							JCheckBox chckbxMultiLine = new JCheckBox("multi line");
							chckbxMultiLine.setEnabled(false);
							panel_1a.add(chckbxMultiLine);
						}
					}
					{
						JPanel panel_1 = new JPanel();
						panelTop.add(panel_1, BorderLayout.CENTER);
						panel_1.setLayout(new BorderLayout(5, 5));
						panel2 = new JPanel();
						panel_1.add(panel2, BorderLayout.CENTER);
						panel2.setLayout(new BorderLayout(0, 0));

						{
							JPanel panel_2 = new JPanel();
							panel_1.add(panel_2, BorderLayout.SOUTH);
							FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
							flowLayout.setAlignment(FlowLayout.LEFT);
							{
								JButton btnAddButton = new JButton("Add");
								btnAddButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										reportRecordingEditorTablePanel.Add();
									}
								});
								btnAddButton.setHorizontalAlignment(SwingConstants.LEFT);
								panel_2.add(btnAddButton);
							}
							{
								JButton btnRemoveButton = new JButton("Remove");
								btnRemoveButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										reportRecordingEditorTablePanel.Remove();
									}
								});
								{
									JButton btnInsert = new JButton("Insert");
									btnInsert.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											reportRecordingEditorTablePanel.Insert();
										}
									});
									panel_2.add(btnInsert);
								}
								btnRemoveButton.setHorizontalAlignment(SwingConstants.LEFT);
								panel_2.add(btnRemoveButton);
							}
							{
								JButton btnCheck = new JButton("Check");
								btnCheck.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										logger.info("check");
										reportRecordingEditorTablePanel.FixValues();

									}
								});
								panel_2.add(btnCheck);
							}
							{
								JPanel buttonPane = new JPanel();
								panel_2.add(buttonPane);
								buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
								{
									JButton okButton = new JButton("Save");
									okButton.setForeground(Color.BLUE);
									okButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent arg0) {
											ArrayList rIs = reportRecordingEditorTablePanel.getRecordingItems();
											if (newRecordingList.getClass()==RecordingList.class) {
												if (recording == null){
												logger.info("RecordingEditor - ok 1");
												Recording r = new ReportRecording(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(), chckbxActive
														.isSelected(), rIs,comboBox.getSelectedItem().toString(),chckbxCaseSensitive.isSelected());
												repository.addRecording(r);
												logger.info("RecordingEditor - ok 1");
												if (newRecordingList.getClass()==RecordingList.class){
													logger.info("RecordingEditor - ok 1");
												if (((RecordingList) newRecordingList).model != null) {
													logger.info("RecordingEditor - ok 1");
													((RecordingList) newRecordingList).model.addRow(new Object[] { txtName.getText(), txtRegularExp.getText(), chckbxActive.isSelected() });
													((RecordingList) newRecordingList).model.fireTableDataChanged();
												
											}}} else {
												int selectedRow = ((((RecordingList) newRecordingList).table.getSelectedRow() != -1) ? ((((RecordingList) newRecordingList).table.getSelectedRow())) : -1);
												//int selectedRow = ((((ReportPanel) newRecordingList).table.getSelectedRow() != -1) ? ((ReportPanel) newRecordingList).table.convertRowIndexToModel(((ReportPanel) newRecordingList).table.getSelectedRow()) : -1);
												((ReportRecording) recording).update(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(),
														chckbxActive.isSelected(), rIs,comboBox.getSelectedItem().toString(),chckbxCaseSensitive.isSelected());
												((RecordingList) newRecordingList).model.fireTableDataChanged();
												logger.info("RecordingEditor - row Updated");
												((RecordingList) newRecordingList).table.setRowSelectionInterval(selectedRow, selectedRow);
											}}
											else
											{
												int rowCount = ((ReportRecordingSelectorPanel) newRecordingList).table.getRowCount();
												if (recording == null){
													int selectedRow = ((((ReportRecordingSelectorPanel) newRecordingList).table.getSelectedRow() != -1) ? ((ReportRecordingSelectorPanel) newRecordingList).table.convertRowIndexToModel(((ReportRecordingSelectorPanel) newRecordingList).table.getSelectedRow()) : -1);
												if (((ReportRecordingSelectorPanel) newRecordingList).model != null) {
													logger.info("RecordingEditor - ok 1");
													((ReportRecordingSelectorPanel) newRecordingList).model.addRow(new Object[] { txtName.getText(), txtRegularExp.getText(), chckbxActive.isSelected() });
													((ReportRecordingSelectorPanel) newRecordingList).model.fireTableDataChanged();
													((ReportRecordingSelectorPanel) newRecordingList).table.setRowSelectionInterval(selectedRow, selectedRow);
												}}
											 else {
												int selectedRow = ((((ReportRecordingSelectorPanel) newRecordingList).table.getSelectedRow() != -1) ? (((ReportRecordingSelectorPanel) newRecordingList).table.getSelectedRow()) : -1);
												((ReportRecording) recording).update(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(),
														chckbxActive.isSelected(), rIs,comboBox.getSelectedItem().toString(),chckbxCaseSensitive.isSelected());
												logger.info("RecordingEditor - NEVER HERE row Updated");
												((ReportRecordingSelectorPanel) newRecordingList).model.fireTableDataChanged();
												((ReportRecordingSelectorPanel) newRecordingList).table.setRowSelectionInterval(selectedRow, selectedRow);
											}
											}
							/*				if (contentPanel.getParent().getParent().getParent().getParent().getClass().equals(JDialog.class)) {
												((JDialog) contentPanel.getParent().getParent().getParent().getParent()).dispose();
											}*/
											//
										}
									});
									{
										JSeparator separator = new JSeparator();
										buttonPane.add(separator);
									}
									okButton.setActionCommand("OK");
									buttonPane.add(okButton);
									// getRootPane().setDefaultButton(okButton);
								}
							}
						}
					}
				}

			}
			{
				JPanel panel_1 = new JPanel();
				splitPane.setRightComponent(panel_1);
		
				{
					
					GridBagLayout gbl_panel_1 = new GridBagLayout();
					gbl_panel_1.columnWidths = new int[]{0, 0};
					gbl_panel_1.rowHeights = new int[]{0, 0, 0};
					gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panel_1.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
					panel_1.setLayout(gbl_panel_1);
					{
						JPanel panela = new JPanel();
						GridBagConstraints gbc_panela = new GridBagConstraints();
						gbc_panela.fill = GridBagConstraints.BOTH;
						gbc_panela.insets = new Insets(0, 0, 5, 0);
						gbc_panela.gridx = 0;
						gbc_panela.gridy = 0;
						panel_1.add(panela, gbc_panela);
						panela.setLayout(new BorderLayout(0, 0));
						{
							JScrollPane scrollPane = new JScrollPane();
							panela.add(scrollPane);
							{
								examplePane = new JTextPane();
								examplePane.setText(theLine);
								scrollPane.setViewportView(examplePane);
							}
						}
					}
					{
						JPanel panelb = new JPanel();
						GridBagConstraints gbc_panelb = new GridBagConstraints();
						gbc_panelb.fill = GridBagConstraints.BOTH;
						gbc_panelb.gridx = 0;
						gbc_panelb.gridy = 1;
						panel_1.add(panelb, gbc_panelb);
							panelb.setLayout(new BorderLayout(0, 0));
						
						
							textPane = new JTextPane();
							JScrollPane scrollPane = new JScrollPane(textPane);
							panelb.add(scrollPane);
						
					}
				}
			}
		}
		if (re != null) {
			txtName.setText(re.getName());
			txtRegularExp.setText(re.getRegexp());
			txtDate.setText(((ReportRecording) re).getDateFormat());
			examplePane.setText(re.getExampleLine());
			comboBox.setSelectedItem(re.getSubType());
			if (DataVault.getMatchedLines(re)!=null && !DataVault.getMatchedLines(re).equals("") ){
				examplePane.setText(DataVault.getMatchedLines(re));
			}
			if (DataVault.getUnmatchedLines(re)!=null  && !DataVault.getUnmatchedLines(re).equals("") ){
				textPane.setText(DataVault.getUnmatchedLines(re));
			}
			  
		}
		
		{
			{
				if (re == null) {
					reportRecordingEditorTablePanel = new ReportRecordingEditorTable(examplePane);
					logger.info("RecordingEditor - re=null");
				} else {
					reportRecordingEditorTablePanel = new ReportRecordingEditorTable(repo, re, examplePane);
					logger.info("RecordingEditor - re!=null");
				}
				reportRecordingEditorTablePanel.setBackground(UIManager.getColor("Panel.background"));
				reportRecordingEditorTablePanel.setOpaque(true);
				reportRecordingEditorTablePanel.setVisible(true);
			}
		}
		JScrollPane scrollPaneEventRecordingEditorTablePanel = new JScrollPane(reportRecordingEditorTablePanel);
		panel2.add(scrollPaneEventRecordingEditorTablePanel);
		reportRecordingEditorTablePanel.FixValues();
	}
}
