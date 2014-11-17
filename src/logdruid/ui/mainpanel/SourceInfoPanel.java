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
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));

		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_2.add(panel_3);

		JLabel nameLabel = new JLabel("Name :");
		panel_3.add(nameLabel);

		JLabel nameValueLabel = new JLabel(src.getSourceName());
		panel_3.add(nameValueLabel);

		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panel_2.add(panel_4);

		JLabel nbFilesLabel = new JLabel("Number of files :");
		panel_4.add(nbFilesLabel);

		JLabel nbFilesValueLabel = new JLabel();
		panel_4.add(nbFilesValueLabel);

		JPanel panel_5 = new JPanel();
		panel_2.add(panel_5);

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new TitledBorder(null, "Groups", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.add(panel_6, BorderLayout.CENTER);

		JTextPane textPane_1 = new JTextPane();
		textPane_1.setFont(new Font("Dialog", Font.PLAIN, 11));
		// panel_6.add(textPane_1, BorderLayout.SOUTH);

		JScrollPane scrollPane_1 = new JScrollPane(textPane_1);
		GroupLayout gl_panel_6 = new GroupLayout(panel_6);
		gl_panel_6.setAutoCreateGaps(true);
		gl_panel_6.setHorizontalGroup(
			gl_panel_6.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 298, GroupLayout.PREFERRED_SIZE)
		);
		gl_panel_6.setVerticalGroup(
			gl_panel_6.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
		);
		panel_6.setLayout(gl_panel_6);

		ChartData cd = DataMiner.gatherSourceData(repo);
		HashMap<String, Vector<String>> hm = cd.getGroupFilesHashMap(src);
		groupDoc = textPane_1.getDocument();
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
