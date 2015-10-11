/*******************************************************************************
 * LogDruid : chart statistics and events retrieved in logs files through configurable regular expressions
 * Copyright (C) 2015 Frederic Valente (frederic.valente@gmail.com)
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
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import logdruid.data.Preferences;
import logdruid.data.Repository;
import logdruid.ui.WrapLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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
		JPanel panel_2 = new JPanel();
		add(panel_2, BorderLayout.CENTER);
		
		//HashMap<String, String> 
		if (Preferences.getPreferences()!=null){
		Iterator ite=Preferences.getPreferences().keySet().iterator();
		while (ite.hasNext()){
			String key=(String) ite.next();
			JPanel panel = new JPanel();
			panel_1.add(panel,WrapLayout.LEFT);
			JLabel lblKey = new JLabel(key);
			panel.add(lblKey);
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
				while (i<comp.length){
					logger.info(comp.toString());
					logger.info(Preferences.getPreferences());
					logger.info(((JLabel)((JPanel)comp[i]).getComponents()[0]).getText()+", "+((JTextField)((JPanel)comp[i]).getComponents()[1]).getText());
					Preferences.setPreference(((JLabel)((JPanel)comp[i]).getComponents()[0]).getText(), ((JTextField)((JPanel)comp[i]).getComponents()[1]).getText());
					i++;
				}
				Preferences.persist();
			}
		});
		panel_2.setLayout(new WrapLayout(WrapLayout.CENTER, 5, 5));
		panel_2.add(btnSave);

		add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new WrapLayout());

	}
}
