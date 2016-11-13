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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import logdruid.data.Preferences;
import logdruid.data.Repository;
import logdruid.ui.WrapLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;

public class PreferencePanel extends JPanel {
	private static Logger logger = Logger.getLogger(EventRecordingSelectorPanel.class.getName());
	private JTextField textField;

	/**
	 * Create the panel.
	 * @param repository 
	 */
	public PreferencePanel(final Repository repository) {
		setLayout(new BorderLayout(0, 0));
		final JPanel panel_1 = new JPanel();
		panel_1.setLayout(new BorderLayout());
		JPanel panel_2 = new JPanel();
		add(panel_2, BorderLayout.EAST);
		
		//HashMap<String, String> 
		if (Preferences.getPreferences()!=null){
		SortedSet<String> prefSet = new TreeSet<String>();
		prefSet.addAll(Preferences.getPreferences().keySet());
		Iterator ite = prefSet.iterator();
		
		while (ite.hasNext()){
			String key=(String) ite.next();
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel_1.add(panel,BorderLayout.LINE_START);
			panel.setMaximumSize(new Dimension(600, 20));
			JLabel lblKey = new JLabel(key);
			panel.add(lblKey,BorderLayout.WEST);
			textField = new JTextField();
			panel.add(textField);
			textField.setColumns(30);
			textField.setText(Preferences.getPreference(key));
		}
		}
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component[] comp=panel_1.getComponents();
				int i=0;
				while (i<comp.length-1){
					logger.info(comp.toString());
					logger.info(Preferences.getPreferences());
					logger.info(((JLabel)((JPanel)comp[i]).getComponents()[0]).getText()+", "+((JTextField)((JPanel)comp[i]).getComponents()[1]).getText());
					Preferences.setPreference(((JLabel)((JPanel)comp[i]).getComponents()[0]).getText(), ((JTextField)((JPanel)comp[i]).getComponents()[1]).getText());
					i++;
				}
				Preferences.persist();
			}
		});
		WrapLayout wl_panel_2 = new WrapLayout(WrapLayout.LEFT, 5, 5);
		wl_panel_2.setAlignment(FlowLayout.CENTER);
		panel_2.setLayout(wl_panel_2);
		panel_2.add(btnSave);

		add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.PAGE_AXIS));
		
		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(50,0));
		panel_1.add(panel, BorderLayout.WEST);
		
		JPanel panel_3 = new JPanel();
		panel_3.setMinimumSize(new Dimension(0,40));
		add(panel_3, BorderLayout.NORTH);

	}
}
