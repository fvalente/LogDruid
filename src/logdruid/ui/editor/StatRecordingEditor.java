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

import logdruid.data.Repository;
import logdruid.data.mine.DataVault;
import logdruid.data.record.Recording;
import logdruid.data.record.RecordingItem;
import logdruid.data.record.StatRecording;
import logdruid.ui.dialog.DateSelector;
import logdruid.ui.mainpanel.RecordingList;
import logdruid.ui.mainpanel.StatRecordingSelectorPanel;
import logdruid.ui.mainpanel.RecordingList.MyTableModel2;
import logdruid.ui.table.StatRecordingEditorTable;
import logdruid.util.DataMiner;
import logdruid.util.PatternCache;

import javax.swing.SwingConstants;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;

import org.apache.log4j.Logger;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JSplitPane;

public class StatRecordingEditor extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private final JPanel contentPanel = new JPanel();
	private JTextField txtName;
	private JTextField txtRegularExp;
	private JTextField txtDate;
	JPanel panel2;
	private StatRecordingEditorTable recordingEditorTablePanel;
	private Repository repository;
	DefaultListModel listModel;
	JTextPane examplePane = new JTextPane();
	private StatRecording recording;
	Document doc;
	JCheckBox chckbxActive;
	JCheckBox chckbxCaseSensitive;
	JTextPane textPane;

	// private JList recordingList;

	/**
	 * Create the dialog.
	 */
	public StatRecordingEditor(final JPanel newRecordingList, StatRecording re, Repository repo) {
		this(newRecordingList, repo, re.getExampleLine(), re.getRegexp(), re);
	}

	/**
	 * Create the dialog.
	 * 
	 * @wbp.parser.constructor
	 */
	public StatRecordingEditor(final JPanel newRecordingList, Repository repo, String theLine, String regex, final StatRecording re) {
		// setBounds(0, 0, 1015, 467);

		repository = repo;
		recording = re;

		BorderLayout borderLayout = new BorderLayout();
		this.setLayout(borderLayout);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		txtName = new JTextField();
		txtName.setText("name");
		txtName.setColumns(10);
		txtRegularExp = new JTextField();



		{
			JSplitPane splitPane = new JSplitPane();
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			contentPanel.add(splitPane, BorderLayout.CENTER);
			
			JPanel topPanel = new JPanel();
			splitPane.setLeftComponent(topPanel);
			topPanel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1a = new JPanel();
				topPanel.add(panel_1a, BorderLayout.NORTH);
				panel_1a.setLayout(new GridLayout(0, 4, 5, 2));
					panel_1a.add(txtName);
				{

					txtRegularExp.addCaretListener(new CaretListener() {
						public void caretUpdate(CaretEvent e) {
							PatternCache patternCache= new PatternCache();
							doc = examplePane.getDocument();
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
							currIndex +=lines[i].length()+1 ;
							}
							
							}
						}
					});
					panel_1a.add(txtRegularExp);
					txtRegularExp.setText(regex);
					txtRegularExp.setColumns(10);
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1a.add(panel_2);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
						txtDate = new JTextField();
						txtDate.setEditable(false);
						panel_2.add(txtDate, BorderLayout.CENTER);
						txtDate.setText("date format");
						txtDate.setColumns(10);
					}
					{
						JButton button = new JButton("...");
						button.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								DateSelector dateDialog = new DateSelector(repository, txtDate, (Recording) re);
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
					chckbxActive.setSelected(re.getIsActive());
					panel_1a.add(chckbxActive);
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
			panel2 = new JPanel();
			panel2.setPreferredSize(new Dimension(0,180));
			topPanel.add(panel2, BorderLayout.CENTER);
			panel2.setLayout(new BorderLayout(0, 0));
			examplePane = new JTextPane();
			textPane = new JTextPane();
			if (re != null) {
				txtName.setText(re.getName());
				txtRegularExp.setText(re.getRegexp());
				txtDate.setText(((StatRecording) re).getDateFormat());
				examplePane.setText(re.getExampleLine());
				logger.debug("examplePane.setText with recording re.getExampleLine(): "+re.getExampleLine());
				if (DataVault.getMatchedLines(re)!=null && !DataVault.getMatchedLines(re).equals("")){
					examplePane.setText(DataVault.getMatchedLines(re));
					logger.debug("examplePane.setText with DataVault.getMatchedLines(re): "+DataVault.getMatchedLines(re));
				}
				if (DataVault.getUnmatchedLines(re)!=null && !DataVault.getUnmatchedLines(re).equals("")){
					textPane.setText(DataVault.getUnmatchedLines(re));
					logger.debug("examplePane.setText with DataVault.getUnmatchedLines(re): "+DataVault.getUnmatchedLines(re));
				}
			}
							{
								JPanel panel_2 = new JPanel();
								topPanel.add(panel_2, BorderLayout.SOUTH);
								FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
								flowLayout.setAlignment(FlowLayout.LEFT);
								{
									JButton btnAddButton = new JButton("Add");
									btnAddButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											recordingEditorTablePanel.Add();
										}
									});
									btnAddButton.setHorizontalAlignment(SwingConstants.LEFT);
									panel_2.add(btnAddButton);
								}
								{
									JButton btnRemoveButton = new JButton("Remove");
									btnRemoveButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											recordingEditorTablePanel.Remove();
										}
									});
									{
										JButton btnInsert = new JButton("Insert");
										btnInsert.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												recordingEditorTablePanel.Insert();
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
											logger.debug("check");
											recordingEditorTablePanel.FixValues();
			
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
												ArrayList<RecordingItem> rIs = recordingEditorTablePanel.getRecordingItems();
												if (newRecordingList.getClass()==RecordingList.class) {
													if (recording == null){
													logger.debug("RecordingEditor - ok 1");
													Recording r = new StatRecording(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(), chckbxActive
															.isSelected(),chckbxCaseSensitive.isSelected(), rIs);
													repository.addRecording(r);
													logger.debug("RecordingEditor - ok 1");
													if (newRecordingList.getClass()==RecordingList.class){
														logger.debug("RecordingEditor - ok 1");
													if (((RecordingList) newRecordingList).model != null) {
														logger.debug("RecordingEditor - ok 1");
														((RecordingList) newRecordingList).model.addRow(new Object[] { txtName.getText(), txtRegularExp.getText(), chckbxActive.isSelected() });
														((RecordingList) newRecordingList).model.fireTableDataChanged();
													
												}}} else {
													int selectedRow = ((((RecordingList) newRecordingList).table.getSelectedRow() != -1) ? ((((RecordingList) newRecordingList).table.getSelectedRow())) : -1);
													((StatRecording) recording).update(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(),
															chckbxActive.isSelected(),chckbxCaseSensitive.isSelected(), rIs);
													((RecordingList) newRecordingList).model.fireTableDataChanged();
													logger.debug("RecordingEditor - row Updated");
													((RecordingList) newRecordingList).table.setRowSelectionInterval(selectedRow, selectedRow);
												}}
												else
												{
													if (recording == null){
														int selectedRow = ((((StatRecordingSelectorPanel) newRecordingList).table.getSelectedRow() != -1) ? ((StatRecordingSelectorPanel) newRecordingList).table.convertRowIndexToModel(((StatRecordingSelectorPanel) newRecordingList).table.getSelectedRow()) : -1);
													if (((StatRecordingSelectorPanel) newRecordingList).model != null) {
														logger.debug("RecordingEditor - ok 1");
														((StatRecordingSelectorPanel) newRecordingList).model.addRow(new Object[] { txtName.getText(), txtRegularExp.getText(), chckbxActive.isSelected() });
														((StatRecordingSelectorPanel) newRecordingList).model.fireTableDataChanged();
														((StatRecordingSelectorPanel) newRecordingList).table.setRowSelectionInterval(selectedRow, selectedRow);
													}}
												 else {
													int selectedRow = ((((StatRecordingSelectorPanel) newRecordingList).table.getSelectedRow() != -1) ? ((((StatRecordingSelectorPanel) newRecordingList).table.getSelectedRow())) : -1);
													((StatRecording) recording).update(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(),
															chckbxActive.isSelected(), chckbxCaseSensitive.isSelected(), rIs);
													logger.debug("RecordingEditor - row Updated");
													((StatRecordingSelectorPanel) newRecordingList).model.fireTableDataChanged();
													((StatRecordingSelectorPanel) newRecordingList).table.setRowSelectionInterval(selectedRow, selectedRow);
												}
												}
			
			/*						if (contentPanel.getParent().getParent().getParent().getParent().getClass().equals(JDialog.class)) {
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
								//		 getRootPane().setDefaultButton(okButton);
									}
								}
							}
			{
				JPanel bottomPanel = new JPanel();
				splitPane.setRightComponent(bottomPanel);
				bottomPanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_1 = new JPanel();
					bottomPanel.add(panel_1);
					GridBagLayout gbl_panel_1 = new GridBagLayout();
					gbl_panel_1.columnWidths = new int[]{0, 0};
					gbl_panel_1.rowHeights = new int[]{0, 0, 0};
					gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panel_1.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
					panel_1.setLayout(gbl_panel_1);
					{
						JPanel panelB = new JPanel();
						GridBagConstraints gbc_panelB = new GridBagConstraints();
						gbc_panelB.fill = GridBagConstraints.BOTH;
						gbc_panelB.insets = new Insets(0, 0, 5, 0);
						gbc_panelB.gridx = 0;
						gbc_panelB.gridy = 0;
						panel_1.add(panelB, gbc_panelB);
						panelB.setLayout(new BorderLayout(0, 0));
						{
							JScrollPane scrollPane = new JScrollPane();
							panelB.add(scrollPane);
							{

								examplePane.setText(theLine);
								scrollPane.setViewportView(examplePane);
							}
						}
					}
					{
						JPanel panelA = new JPanel();
						GridBagConstraints gbc_panelA = new GridBagConstraints();
						gbc_panelA.fill = GridBagConstraints.BOTH;
						gbc_panelA.gridx = 0;
						gbc_panelA.gridy = 1;
						panel_1.add(panelA, gbc_panelA);
						panelA.setLayout(new BorderLayout(0, 0));

						JScrollPane scrollPane = new JScrollPane(textPane);
						panelA.add(scrollPane);
						
					}
				}
			}
		}
		{
			{
				if (re == null) {
					recordingEditorTablePanel = new StatRecordingEditorTable(examplePane);
					logger.debug("RecordingEditor - re=null");
				} else {
					recordingEditorTablePanel = new StatRecordingEditorTable(repo, re, examplePane);
					logger.debug("RecordingEditor - re!=null");
				}
				recordingEditorTablePanel.setBackground(UIManager.getColor("Panel.background"));
				recordingEditorTablePanel.setOpaque(true);
				recordingEditorTablePanel.setVisible(true);
			}
		}
		JScrollPane scrollPaneRecordingEditorTablePanel = new JScrollPane(recordingEditorTablePanel);
		panel2.add(scrollPaneRecordingEditorTablePanel);
		recordingEditorTablePanel.FixValues();
	}

	public StatRecordingEditor(Repository repository2, String exampleLine, String regexp, StatRecording statRecording) {
		this(null, repository2, exampleLine, regexp, statRecording);
	}
}
