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

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import logdruid.data.Repository;
import logdruid.data.mine.DataVault;
import logdruid.data.record.EventRecording;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.ui.dialog.DateSelector;
import logdruid.ui.mainpanel.EventRecordingSelectorPanel;
import logdruid.ui.mainpanel.RecordingList;
import logdruid.ui.mainpanel.RecordingList.MyTableModel2;
import logdruid.ui.table.EventRecordingEditorTable;
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
import java.awt.CardLayout;

public class EventRecordingEditor extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private JPanel _this = this;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtName;
	private JTextField txtRegularExp;
	private JTextField txtDate;
	JPanel panel2;
	private EventRecordingEditorTable eventRecordingEditorTablePanel;
	private Repository repository;
	DefaultListModel listModel;
	JTextPane examplePane = new JTextPane();
	private EventRecording recording;
	Document doc;
	JCheckBox chckbxActive;
	JCheckBox chckbxCaseSensitive;
	private JLabel nameLabel;
	private JLabel regularExpressionLabel;
	private JLabel dateFormatLabel;
	JTextPane textPane ;
	JButton dateFormatButton;
	final JCheckBox chckbxDefault;
	// private JList recordingList;

	/*
	 * public EventRecordingEditor(Repository repository2, String exampleLine,
	 * String regexp, EventRecording eventRecording) { this(null, repository2,
	 * exampleLine, regexp, eventRecording); }
	 */

	/**
	 * Create the dialog.
	 */
	public EventRecordingEditor(final JPanel newRecordingList, EventRecording re, Repository repo) {
		this(newRecordingList, repo, re.getExampleLine(), re.getRegexp(), re);
	}

	public EventRecordingEditor(Repository repository2, String exampleLine, String regexp, EventRecording eventRecording) {
		this(null, repository2, exampleLine, regexp, eventRecording);
	}
	/**
	 * Create the dialog.
	 * 
	 * @wbp.parser.constructor
	 */
	public EventRecordingEditor(final JPanel newRecordingList, Repository repo, String theLine, String regex, final EventRecording re) {
		// setBounds(0, 0, 1015, 467);

		repository = repo;
		recording = re;
		// logger.debug("myTableModel2 null? "+(newRecordingList==null));
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
					JPanel panel_2 = new JPanel();
					panelTop.add(panel_2, BorderLayout.WEST);
					panel_2.setBorder(null);
					GridBagLayout gbl_panel_2 = new GridBagLayout();
					gbl_panel_2.columnWidths = new int[] {93, 110, 200, 200};
					gbl_panel_2.rowHeights = new int[] {23};
					gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0};
					gbl_panel_2.rowWeights = new double[]{1.0};
					panel_2.setLayout(gbl_panel_2);
					{
						dateFormatLabel = new JLabel("Date Format:");
						dateFormatLabel.setHorizontalAlignment(SwingConstants.CENTER);
						GridBagConstraints gbc_dateFormatLabel = new GridBagConstraints();
						gbc_dateFormatLabel.anchor = GridBagConstraints.WEST;
						gbc_dateFormatLabel.fill = GridBagConstraints.VERTICAL;
						gbc_dateFormatLabel.insets = new Insets(0, 0, 0, 5);
						gbc_dateFormatLabel.gridx = 0;
						gbc_dateFormatLabel.gridy = 0;
						panel_2.add(dateFormatLabel, gbc_dateFormatLabel);
					}
					{
						dateFormatLabel.setLabelFor(txtDate);
					}
					{
						JPanel panel = new JPanel();
						panel.setBorder(null);
						GridBagConstraints gbc_panel = new GridBagConstraints();
						gbc_panel.gridwidth = 2;
						gbc_panel.fill = GridBagConstraints.BOTH;
						gbc_panel.insets = new Insets(0, 0, 0, 5);
						gbc_panel.gridx = 1;
						gbc_panel.gridy = 0;
						panel_2.add(panel, gbc_panel);
						panel.setLayout(new BorderLayout(0, 0));
						{
							txtDate = new JTextField();
							panel.add(txtDate, BorderLayout.CENTER);
							txtDate.setEditable(false);
							txtDate.setText("date format");
							txtDate.setColumns(15);
						}
						{
							dateFormatButton = new JButton("...");
							dateFormatButton.setHorizontalAlignment(SwingConstants.RIGHT);
							panel.add(dateFormatButton, BorderLayout.EAST);
							dateFormatButton.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									DateSelector dateDialog = new DateSelector(repository, txtDate, re);
									dateDialog.validate();
									dateDialog.setResizable(true);
									dateDialog.setModal(false);
									dateDialog.setVisible(true);
								}
							});
							dateFormatButton.setFont(new Font("Dialog", Font.BOLD, 6));
						}
					}
					{
						chckbxDefault = new JCheckBox("Default date");
						chckbxDefault.setSelected(re.getUseSourceDateFormat());
						dateFormatButton.setEnabled(!re.getUseSourceDateFormat());
						txtDate.setEnabled(!re.getUseSourceDateFormat());
						chckbxDefault.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								if (chckbxDefault.isSelected())
								{
									dateFormatButton.setEnabled(false);
									txtDate.setEnabled(false);
									
								}
								else{
									dateFormatButton.setEnabled(true);
									txtDate.setEnabled(true);
								}
							}
						});
						GridBagConstraints gbc_chckbxDefault = new GridBagConstraints();
						gbc_chckbxDefault.anchor = GridBagConstraints.WEST;
						gbc_chckbxDefault.gridx = 3;
						gbc_chckbxDefault.gridy = 0;
						panel_2.add(chckbxDefault, gbc_chckbxDefault);
					}
					{
						JPanel panel_1a = new JPanel();
						panelTop.add(panel_1a, BorderLayout.NORTH);
						GridBagLayout gbl_panel_1a = new GridBagLayout();
						gbl_panel_1a.columnWidths = new int[]{222, 208, 141, 0};
						gbl_panel_1a.rowHeights = new int[]{24, 0};
						gbl_panel_1a.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
						gbl_panel_1a.rowWeights = new double[]{0.0, Double.MIN_VALUE};
						panel_1a.setLayout(gbl_panel_1a);
						{
							JPanel namePanel = new JPanel();
							GridBagConstraints gbc_namePanel = new GridBagConstraints();
							gbc_namePanel.fill = GridBagConstraints.BOTH;
							gbc_namePanel.insets = new Insets(0, 0, 0, 5);
							gbc_namePanel.gridx = 0;
							gbc_namePanel.gridy = 0;
							panel_1a.add(namePanel, gbc_namePanel);
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
							GridBagConstraints gbc_regularExpressionPanel = new GridBagConstraints();
							gbc_regularExpressionPanel.fill = GridBagConstraints.BOTH;
							gbc_regularExpressionPanel.insets = new Insets(0, 0, 0, 5);
							gbc_regularExpressionPanel.gridx = 1;
							gbc_regularExpressionPanel.gridy = 0;
							panel_1a.add(regularExpressionPanel, gbc_regularExpressionPanel);
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
											logger.debug("line: "+lines[i]);
											
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
							JPanel panel = new JPanel();
							GridBagConstraints gbc_panel = new GridBagConstraints();
							gbc_panel.gridx = 2;
							gbc_panel.gridy = 0;
							panel_1a.add(panel, gbc_panel);
							panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
							{
								chckbxCaseSensitive = new JCheckBox("case sens.");
								panel.add(chckbxCaseSensitive);
								chckbxCaseSensitive.setSelected(re.isCaseSensitive());
							}
							{
								chckbxActive = new JCheckBox("active");
								panel.add(chckbxActive);
								chckbxActive.setSelected(true);
							}
							{
								JCheckBox chckbxMultiLine = new JCheckBox("multi line");
								panel.add(chckbxMultiLine);
								chckbxMultiLine.setEnabled(false);
							}
						}
					}
					{
						JPanel panel_1 = new JPanel();
						panelTop.add(panel_1, BorderLayout.SOUTH);
						panel_1.setLayout(new BorderLayout(5, 5));
						panel2 = new JPanel();
						panel2.setPreferredSize(new Dimension(0,180));
						panel_1.add(panel2, BorderLayout.CENTER);
						panel2.setLayout(new BorderLayout(0, 0));

						{
							JPanel panel_2a = new JPanel();
							panel_1.add(panel_2a, BorderLayout.SOUTH);
							FlowLayout flowLayout = (FlowLayout) panel_2a.getLayout();
							flowLayout.setAlignment(FlowLayout.LEFT);
							{
								JButton btnAddButton = new JButton("Add");
								btnAddButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										eventRecordingEditorTablePanel.Add();
									}
								});
								btnAddButton.setHorizontalAlignment(SwingConstants.LEFT);
								panel_2a.add(btnAddButton);
							}
							{
								JButton btnRemoveButton = new JButton("Remove");
								btnRemoveButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										eventRecordingEditorTablePanel.Remove();
									}
								});
								{
									JButton btnInsert = new JButton("Insert");
									btnInsert.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											eventRecordingEditorTablePanel.Insert();
										}
									});
									panel_2a.add(btnInsert);
								}
								btnRemoveButton.setHorizontalAlignment(SwingConstants.LEFT);
								panel_2a.add(btnRemoveButton);
							}
							{
								JButton btnCheck = new JButton("Check");
								btnCheck.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										logger.debug("check");
										eventRecordingEditorTablePanel.FixValues();

									}
								});
								panel_2a.add(btnCheck);
							}
							{
								JPanel buttonPane = new JPanel();
								panel_2a.add(buttonPane);
								buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
								{
									JButton okButton = new JButton("Save");
									okButton.setForeground(Color.BLUE);
									okButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent arg0) {
											ArrayList<RecordingItem> rIs = eventRecordingEditorTablePanel.getRecordingItems();
											logger.debug(rIs.size());
											if (newRecordingList.getClass()==RecordingList.class) {
												if (recording == null){
												logger.debug("RecordingEditor - ok 1");
												Recording r = new EventRecording(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(), chckbxActive
														.isSelected(),chckbxDefault.isSelected(),chckbxCaseSensitive.isSelected(), rIs);
												repository.addRecording(r);
												logger.debug("RecordingEditor - ok 1");
													logger.debug("RecordingEditor - ok 1");
												if (((RecordingList) newRecordingList).model != null) {
													logger.debug("RecordingEditor - ok 1");
													((RecordingList) newRecordingList).model.addRow(new Object[] { txtName.getText(), txtRegularExp.getText(), chckbxActive.isSelected() });
													((RecordingList) newRecordingList).model.fireTableDataChanged();
												
											}} else {
												int selectedRow = ((((RecordingList) newRecordingList).table.getSelectedRow() != -1) ? ((((RecordingList) newRecordingList).table.getSelectedRow())) : -1);
												//int selectedRow = ((((ReportPanel) newRecordingList).table.getSelectedRow() != -1) ? ((ReportPanel) newRecordingList).table.convertRowIndexToModel(((ReportPanel) newRecordingList).table.getSelectedRow()) : -1);
												((EventRecording) recording).update(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(),
														chckbxActive.isSelected(),chckbxDefault.isSelected(), chckbxCaseSensitive.isSelected(), rIs);
												((RecordingList) newRecordingList).model.fireTableDataChanged();
												logger.debug("RecordingEditor - row Updated");
												((RecordingList) newRecordingList).table.setRowSelectionInterval(selectedRow, selectedRow);
											}}
											else
											{
												if (recording == null){
													int selectedRow = ((((EventRecordingSelectorPanel) newRecordingList).table.getSelectedRow() != -1) ? ((EventRecordingSelectorPanel) newRecordingList).table.convertRowIndexToModel(((EventRecordingSelectorPanel) newRecordingList).table.getSelectedRow()) : -1);
												if (((EventRecordingSelectorPanel) newRecordingList).model != null) {
													logger.debug("RecordingEditor - ok 1");
													((EventRecordingSelectorPanel) newRecordingList).model.addRow(new Object[] { txtName.getText(), txtRegularExp.getText(), chckbxActive.isSelected() });
													((EventRecordingSelectorPanel) newRecordingList).model.fireTableDataChanged();
													((EventRecordingSelectorPanel) newRecordingList).table.setRowSelectionInterval(selectedRow, selectedRow);
												}}
											 else {
												int selectedRow = ((((EventRecordingSelectorPanel) newRecordingList).table.getSelectedRow() != -1) ? (((EventRecordingSelectorPanel) newRecordingList).table.getSelectedRow()) : -1);
												logger.debug("selectedRow: " + selectedRow);
												logger.debug(((EventRecordingSelectorPanel) newRecordingList).table.getRowCount());
												((EventRecording) recording).update(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(),
														chckbxActive.isSelected(),chckbxDefault.isSelected(),chckbxCaseSensitive.isSelected(), rIs);
												logger.debug("RecordingEditor - NEVER HERE row Updated");
												((EventRecordingSelectorPanel) newRecordingList).model.fireTableDataChanged();
												((EventRecordingSelectorPanel) newRecordingList).table.setRowSelectionInterval(selectedRow, selectedRow);
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
			txtDate.setText(((EventRecording) re).getDateFormat());
			examplePane.setText(re.getExampleLine());
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
					eventRecordingEditorTablePanel = new EventRecordingEditorTable(examplePane);
					logger.debug("RecordingEditor - re=null");
				} else {
					eventRecordingEditorTablePanel = new EventRecordingEditorTable(repo, re, examplePane);
					logger.debug("RecordingEditor - re!=null");
				}
				eventRecordingEditorTablePanel.setBackground(UIManager.getColor("Panel.background"));
				eventRecordingEditorTablePanel.setOpaque(true);
				eventRecordingEditorTablePanel.setVisible(true);
			}
		}
		JScrollPane scrollPaneEventRecordingEditorTablePanel = new JScrollPane(eventRecordingEditorTablePanel);
		panel2.add(scrollPaneEventRecordingEditorTablePanel);
		eventRecordingEditorTablePanel.FixValues();
	}
}
