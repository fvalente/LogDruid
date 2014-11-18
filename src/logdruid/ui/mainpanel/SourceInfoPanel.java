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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import logdruid.data.ChartData;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.util.DataMiner;
import java.awt.GridLayout;
import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class SourceInfoPanel extends JPanel {
	private Document groupDoc;
	private Document filesDoc;

	/**
	 * Create the panel.
	 */
	public SourceInfoPanel(Repository repo, Source src) {
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		JTextPane textPane = new JTextPane();
		textPane.setFont(new Font("Dialog", Font.PLAIN, 11));
		JScrollPane scrollPane = new JScrollPane(textPane);
		panel.add(scrollPane);

		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.WEST);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{142, 16, 0};
		gbl_panel_2.rowHeights = new int[]{25, 0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
				
						JPanel panel_3 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
						flowLayout.setAlignment(FlowLayout.LEFT);
						GridBagConstraints gbc_panel_3 = new GridBagConstraints();
						gbc_panel_3.anchor = GridBagConstraints.NORTHWEST;
						gbc_panel_3.insets = new Insets(0, 0, 5, 5);
						gbc_panel_3.gridx = 0;
						gbc_panel_3.gridy = 0;
						panel_2.add(panel_3, gbc_panel_3);
						
								JLabel nameLabel = new JLabel("Name :");
								panel_3.add(nameLabel);
								
										JLabel nameValueLabel = new JLabel(src.getSourceName());
										panel_3.add(nameValueLabel);
												
														JPanel panel_5 = new JPanel();
														GridBagConstraints gbc_panel_5 = new GridBagConstraints();
														gbc_panel_5.insets = new Insets(0, 0, 5, 5);
														gbc_panel_5.anchor = GridBagConstraints.WEST;
														gbc_panel_5.gridx = 0;
														gbc_panel_5.gridy = 1;
														panel_2.add(panel_5, gbc_panel_5);
														
																JLabel nbFilesLabel = new JLabel("Number of files :");
																panel_5.add(nbFilesLabel);
																
																		JLabel nbFilesValueLabel = new JLabel();
																		panel_5.add(nbFilesValueLabel);

										
												JPanel panel_4 = new JPanel();
												FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
												flowLayout_1.setAlignment(FlowLayout.LEFT);
												GridBagConstraints gbc_panel_4 = new GridBagConstraints();
												gbc_panel_4.anchor = GridBagConstraints.NORTH;
												gbc_panel_4.insets = new Insets(0, 0, 0, 5);
												gbc_panel_4.gridx = 0;
												gbc_panel_4.gridy = 2;
												panel_2.add(panel_4, gbc_panel_4);

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new TitledBorder(null, "Groups", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.add(panel_6, BorderLayout.CENTER);
		GridBagLayout gbl_panel_6 = new GridBagLayout();
		gbl_panel_6.columnWidths = new int[]{298, 0};
		gbl_panel_6.rowHeights = new int[]{200, 0};
		gbl_panel_6.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_6.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_6.setLayout(gbl_panel_6);
		
				JTextPane textPane_1 = new JTextPane();
				textPane_1.setFont(new Font("Dialog", Font.PLAIN, 11));
				// panel_6.add(textPane_1, BorderLayout.SOUTH);

				JScrollPane scrollPane_1 = new JScrollPane(textPane_1);
				GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
				gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
				gbc_scrollPane_1.gridx = 0;
				gbc_scrollPane_1.gridy = 0;
				panel_6.add(scrollPane_1, gbc_scrollPane_1);
				groupDoc = textPane_1.getDocument();

		ChartData cd = DataMiner.gatherSourceData(repo);
		HashMap<String, Vector<String>> hm = cd.getGroupFilesHashMap(src);
		filesDoc = textPane.getDocument();
		Iterator it = hm.entrySet().iterator();
		int nbFiles =0;
		while (it.hasNext()) {
			try {
				final Map.Entry sourcePairs = (Map.Entry) it.next();
				final String groupString = (String) sourcePairs.getKey();
				Vector files = (Vector) sourcePairs.getValue();
				nbFiles+=files.size();
				groupDoc.insertString(groupDoc.getLength(), groupString + "("+files.size()+")\n", null);
				filesDoc.insertString(filesDoc.getLength(), groupString + "\n", null);
				Iterator vecIt = files.iterator();
				while (vecIt.hasNext()) {
					filesDoc.insertString(filesDoc.getLength(), vecIt.next() + "\n", null);
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		nbFilesValueLabel.setText(""+nbFiles);

	}
}
