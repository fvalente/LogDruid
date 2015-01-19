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
import logdruid.data.record.EventRecording;
import logdruid.data.record.Recording;
import logdruid.ui.DateSelector;
import logdruid.ui.RecordingList;
import logdruid.ui.RecordingList.MyTableModel2;
import logdruid.ui.table.EventRecordingEditorTable;
import logdruid.util.DataMiner;

import javax.swing.SwingConstants;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;

import org.apache.log4j.Logger;

import java.awt.Font;

import javax.swing.border.EtchedBorder;
import javax.swing.border.BevelBorder;

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
	private JLabel nameLabel;
	private JLabel regularExpressionLabel;
	private JLabel dateFormatLabel;

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

	/**
	 * Create the dialog.
	 * 
	 * @wbp.parser.constructor
	 */
	public EventRecordingEditor(final JPanel newRecordingList, Repository repo, String theLine, String regex, final EventRecording re) {
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
			JPanel panel_1 = new JPanel();
			contentPanel.add(panel_1, BorderLayout.CENTER);

			panel_1.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel = new JPanel();
				panel_1.add(panel, BorderLayout.CENTER);
				panel.setLayout(new BorderLayout(0, 0));
				{
					JScrollPane scrollPane = new JScrollPane();
					panel.add(scrollPane);
					{
						examplePane = new JTextPane();
						examplePane.setText(theLine);
						scrollPane.setViewportView(examplePane);
					}
				}
			}
		}
		{
			JPanel panelTop = new JPanel();
			contentPanel.add(panelTop, BorderLayout.NORTH);
			panelTop.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panelTop.add(panel_1, BorderLayout.NORTH);
				panel_1.setLayout(new GridLayout(0, 3, 0, 0));
				{
					JPanel namePanel = new JPanel();
					panel_1.add(namePanel);
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
					{
						JPanel panel = new JPanel();
						namePanel.add(panel, BorderLayout.EAST);
					}
				}
				{
					JPanel regularExpressionPanel = new JPanel();
					panel_1.add(regularExpressionPanel);
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

								Pattern pattern = Pattern.compile(txtRegularExp.getText());
								Matcher matcher = pattern.matcher(examplePane.getText());
								Highlighter h = examplePane.getHighlighter();
								h.removeAllHighlights();
								if (matcher.find()) {
									// int currIndex=doc.getLength();
									// doc.insertString(doc.getLength(),line+"\n",
									// null);

									try {
										h.addHighlight(matcher.start(), matcher.end(), DefaultHighlighter.DefaultPainter);
									} catch (BadLocationException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}

								}
							}
						});
						txtRegularExp.setText(regex);
						txtRegularExp.setColumns(10);
					}
					{
						JPanel panel = new JPanel();
						regularExpressionPanel.add(panel, BorderLayout.EAST);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
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
					JCheckBox chckbxNewCheckBox = new JCheckBox("incident only");
					panel_1.add(chckbxNewCheckBox);
				}
				{
					JCheckBox chckbxMultiLine = new JCheckBox("multi line");
					panel_1.add(chckbxMultiLine);
				}
				{
					chckbxActive = new JCheckBox("active");
					chckbxActive.setSelected(true);
					panel_1.add(chckbxActive);
				}
			}
			{
				JPanel panel = new JPanel();
				panelTop.add(panel, BorderLayout.CENTER);
				panel.setLayout(new BorderLayout(5, 5));
				panel2 = new JPanel();
				panel.add(panel2, BorderLayout.CENTER);
				panel2.setLayout(new BorderLayout(0, 0));

				{
					JPanel panel_2 = new JPanel();
					panel.add(panel_2, BorderLayout.SOUTH);
					FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
					flowLayout.setAlignment(FlowLayout.LEFT);
					{
						JButton btnAddButton = new JButton("Add");
						btnAddButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								eventRecordingEditorTablePanel.Add();
							}
						});
						btnAddButton.setHorizontalAlignment(SwingConstants.LEFT);
						panel_2.add(btnAddButton);
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
								eventRecordingEditorTablePanel.FixValues();

							}
						});
						panel_2.add(btnCheck);
					}
				}
			}
		}
		{
			{
				if (re == null) {
					eventRecordingEditorTablePanel = new EventRecordingEditorTable(examplePane);
					logger.info("RecordingEditor - re=null");
				} else {
					eventRecordingEditorTablePanel = new EventRecordingEditorTable(repo, re, examplePane);
					logger.info("RecordingEditor - re!=null");
				}
				eventRecordingEditorTablePanel.setBackground(UIManager.getColor("Panel.background"));
				eventRecordingEditorTablePanel.setOpaque(true);
				eventRecordingEditorTablePanel.setVisible(true);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			this.add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ArrayList rIs = eventRecordingEditorTablePanel.getRecordingItems();
						if (recording == null) {
							Recording r = new EventRecording(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(), chckbxActive
									.isSelected(), rIs);
							repository.addRecording(r);
							if (((RecordingList) newRecordingList).model != null) {
								logger.info("RecordingEditor - ok 1");
								((RecordingList) newRecordingList).model.addRow(new Object[] { txtName.getText(), txtRegularExp.getText(),
										chckbxActive.isSelected() });
								((RecordingList) newRecordingList).model.fireTableDataChanged();
							}
						} else {
							((EventRecording) recording).update(txtName.getText(), txtRegularExp.getText(), examplePane.getText(), txtDate.getText(),
									chckbxActive.isSelected(), rIs);
							((RecordingList) newRecordingList).model.fireTableDataChanged();
							logger.info("RecordingEditor - ok 2");
						}

						if (contentPanel.getParent().getParent().getParent().getParent().getClass().equals(JDialog.class)) {
							((JDialog) contentPanel.getParent().getParent().getParent().getParent()).dispose();
						}
						//
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				// getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if (contentPanel.getParent().getParent().getParent().getParent().getClass().equals(JDialog.class)) {
							((JDialog) contentPanel.getParent().getParent().getParent().getParent()).dispose();
						}
					}
				});
				buttonPane.add(cancelButton);
			}
		}
		if (re != null) {
			txtName.setText(re.getName());
			txtRegularExp.setText(re.getRegexp());
			txtDate.setText(((EventRecording) re).getDateFormat());
			examplePane.setText(re.getExampleLine());
		}
		JScrollPane scrollPaneEventRecordingEditorTablePanel = new JScrollPane(eventRecordingEditorTablePanel);
		// scrollPaneEventRecordingEditorTablePanel.setViewportView(eventRecordingEditorTablePanel);
		panel2.add(scrollPaneEventRecordingEditorTablePanel);
		eventRecordingEditorTablePanel.FixValues();
	}

}
