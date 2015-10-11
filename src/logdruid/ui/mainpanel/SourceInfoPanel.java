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
package logdruid.ui.mainpanel;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.BoxLayout;

import java.awt.FlowLayout;
import java.awt.Component;
import java.util.Map;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import logdruid.data.ChartData;
import logdruid.data.FileRecord;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.ui.WrapLayout;
import logdruid.util.DataMiner;

import java.awt.GridLayout;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

public class SourceInfoPanel extends JPanel {
	private static Logger logger = Logger.getLogger(SourceInfoPanel.class.getName());
	private Document groupDoc;
	private Document filesDoc;

	/**
	 * Create the panel.
	 */
	public SourceInfoPanel(Repository repo, Source src) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{10, 665, 5, 0};
		gridBagLayout.rowHeights = new int[]{232, 257, 10, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
				JPanel panel_7 = new JPanel();
				GridBagConstraints gbc_panel_7 = new GridBagConstraints();
				gbc_panel_7.anchor = GridBagConstraints.NORTH;
				gbc_panel_7.fill = GridBagConstraints.HORIZONTAL;
				gbc_panel_7.insets = new Insets(0, 0, 5, 5);
				gbc_panel_7.gridx = 1;
				gbc_panel_7.gridy = 0;
				add(panel_7, gbc_panel_7);
										GridBagLayout gbl_panel_7 = new GridBagLayout();
										gbl_panel_7.columnWidths = new int[]{207, 19, 0};
										gbl_panel_7.rowHeights = new int[]{10, 230, 0};
										gbl_panel_7.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
										gbl_panel_7.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
										panel_7.setLayout(gbl_panel_7);
												
														JPanel panel_2 = new JPanel();
														GridBagConstraints gbc_panel_2 = new GridBagConstraints();
														gbc_panel_2.fill = GridBagConstraints.VERTICAL;
														gbc_panel_2.anchor = GridBagConstraints.WEST;
														gbc_panel_2.insets = new Insets(0, 0, 0, 5);
														gbc_panel_2.gridx = 0;
														gbc_panel_2.gridy = 1;
														panel_7.add(panel_2, gbc_panel_2);
														GridBagLayout gbl_panel_2 = new GridBagLayout();
														gbl_panel_2.columnWidths = new int[] { 142, 16, 0, 0 };
														gbl_panel_2.rowHeights = new int[] { 0, -63, 0, 0, 0, 0 };
														gbl_panel_2.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
														gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
														panel_2.setLayout(gbl_panel_2);
														
																JPanel panel_3 = new JPanel();
																FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
																flowLayout.setAlignment(FlowLayout.LEFT);
																GridBagConstraints gbc_panel_3 = new GridBagConstraints();
																gbc_panel_3.anchor = GridBagConstraints.NORTHWEST;
																gbc_panel_3.insets = new Insets(0, 0, 5, 5);
																gbc_panel_3.gridx = 0;
																gbc_panel_3.gridy = 1;
																panel_2.add(panel_3, gbc_panel_3);
																
																		JLabel nameLabel = new JLabel("Name :");
																		panel_3.add(nameLabel);
																		
																				JLabel nameValueLabel = new JLabel(src.getSourceName());
																				panel_3.add(nameValueLabel);
																				
																						JPanel panel_5 = new JPanel();
																						FlowLayout flowLayout_3 = (FlowLayout) panel_5.getLayout();
																						flowLayout_3.setAlignment(FlowLayout.LEFT);
																						GridBagConstraints gbc_panel_5 = new GridBagConstraints();
																						gbc_panel_5.insets = new Insets(0, 0, 5, 5);
																						gbc_panel_5.anchor = GridBagConstraints.WEST;
																						gbc_panel_5.gridx = 0;
																						gbc_panel_5.gridy = 2;
																						panel_2.add(panel_5, gbc_panel_5);
																						
																								JLabel nbFilesLabel = new JLabel("Number of files :");
																								panel_5.add(nbFilesLabel);
																								
																										JLabel nbFilesValueLabel = new JLabel();
																										panel_5.add(nbFilesValueLabel);
																										
																										JPanel panel_8 = new JPanel();
																										FlowLayout flowLayout_2 = (FlowLayout) panel_8.getLayout();
																										flowLayout_2.setAlignment(FlowLayout.LEFT);
																										GridBagConstraints gbc_panel_8 = new GridBagConstraints();
																										gbc_panel_8.anchor = GridBagConstraints.NORTH;
																										gbc_panel_8.insets = new Insets(0, 0, 5, 5);
																										gbc_panel_8.fill = GridBagConstraints.HORIZONTAL;
																										gbc_panel_8.gridx = 0;
																										gbc_panel_8.gridy = 3;
																										panel_2.add(panel_8, gbc_panel_8);
																										
																										JLabel lblSizeOfFiles = new JLabel("Size of files :");
																										panel_8.add(lblSizeOfFiles);
																										
																										JLabel filesSize = new JLabel();
																										panel_8.add(filesSize);
																										
																												JPanel panel_4 = new JPanel();
																												FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
																												flowLayout_1.setAlignment(FlowLayout.LEFT);
																												GridBagConstraints gbc_panel_4 = new GridBagConstraints();
																												gbc_panel_4.anchor = GridBagConstraints.NORTH;
																												gbc_panel_4.insets = new Insets(0, 0, 0, 5);
																												gbc_panel_4.gridx = 0;
																												gbc_panel_4.gridy = 4;
																												panel_2.add(panel_4, gbc_panel_4);
										
												JPanel panel_6 = new JPanel();
												GridBagConstraints gbc_panel_6 = new GridBagConstraints();
												gbc_panel_6.fill = GridBagConstraints.BOTH;
												gbc_panel_6.gridx = 1;
												gbc_panel_6.gridy = 1;
												panel_7.add(panel_6, gbc_panel_6);
												panel_6.setBorder(new TitledBorder(null, "Groups", TitledBorder.LEADING, TitledBorder.TOP, null, null));
												panel_6.setLayout(new BorderLayout(0, 0));
												
														JTextPane textPane_1 = new JTextPane();
														textPane_1.setEditable(false);
														textPane_1.setFont(new Font("Dialog", Font.PLAIN, 11));
														// panel_6.add(textPane_1, BorderLayout.SOUTH);

														JScrollPane scrollPane_1 = new JScrollPane(textPane_1);
														panel_6.add(scrollPane_1, BorderLayout.CENTER);
														groupDoc = textPane_1.getDocument();
		
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(null, "Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.insets = new Insets(0, 0, 5, 5);
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.gridx = 1;
				gbc_panel.gridy = 1;
				add(panel, gbc_panel);
				panel.setLayout(new BorderLayout(0, 0));
				JTextPane textPane = new JTextPane();
				textPane.setEditable(false);
				textPane.setFont(new Font("Dialog", Font.PLAIN, 11));
				JScrollPane scrollPane = new JScrollPane(textPane);
				panel.add(scrollPane);
		Highlighter h = textPane.getHighlighter();
		if (repo != null && repo.getBaseSourcePath() != null) {
			ChartData cd = DataMiner.gatherSourceData(repo);
			Map<String, ArrayList<FileRecord>> hm = cd.getGroupFilesMap(src);
			logger.info("source: "+src.getSourceName()+",  map: "+hm+",  map size: "+ hm.size());
			filesDoc = textPane.getDocument();
			Iterator it = hm.entrySet().iterator();
			int nbFiles = 0;
			long size=0;
			while (it.hasNext()) {
				try {
					int currGroupIndex = groupDoc.getLength();
					int currFilesIndex = filesDoc.getLength();
					final Map.Entry sourcePairs = (Map.Entry) it.next();
					final String groupString = (String) sourcePairs.getKey();
					logger.debug("groupString: "+groupString);
					ArrayList files = (ArrayList) sourcePairs.getValue();
					nbFiles += files.size();
					Iterator<FileRecord> iterator =files.iterator();
					while (iterator.hasNext())
						{
						FileRecord fr = iterator.next();
						size=size+((File)fr.getFile()).length();
						logger.info(fr.getFile().getName()+" "+((File)fr.getFile()).length());
						}
					groupDoc.insertString(groupDoc.getLength(), groupString + "(" + files.size() + " file"+(nbFiles>1?"s":"")+")\n", null);
					filesDoc.insertString(filesDoc.getLength(), groupString + "\n", null);

					SimpleAttributeSet sas = new SimpleAttributeSet(); 
					StyleConstants.setBold(sas, true);
					textPane.getStyledDocument().setCharacterAttributes(currFilesIndex , groupString.length()-1, sas, false);
					textPane_1.getStyledDocument().setCharacterAttributes(currGroupIndex , groupString.length()-1, sas, false);
					
					//h.addHighlight(currIndex , currIndex + groupString.length()-1,  DefaultHighlighter.DefaultPainter);
					Iterator vecIt = files.iterator();
					while (vecIt.hasNext()) {
						filesDoc.insertString(filesDoc.getLength(),"- "+ new File(repo.getBaseSourcePath()).toURI().relativize(new File(((FileRecord)vecIt.next()).getCompletePath()).toURI()).getPath()+ "\n", null);
						
					}
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			textPane.setCaretPosition(0);
			textPane_1.setCaretPosition(0);
			nbFilesValueLabel.setText("" + nbFiles);
			filesSize.setText(""+size/1024000+"MB");
		}

	}
}
