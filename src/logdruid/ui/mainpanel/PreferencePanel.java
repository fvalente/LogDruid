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
		if (repository.getPreferences()!=null){
		Iterator ite=repository.getPreferences().keySet().iterator();
		while (ite.hasNext()){
			String key=(String) ite.next();
			JPanel panel = new JPanel();
			panel_1.add(panel,WrapLayout.LEFT);
			JLabel lblKey = new JLabel(key);
			panel.add(lblKey);
			textField = new JTextField();
			panel.add(textField);
			textField.setColumns(30);
			textField.setText(repository.getPreference(key));
		}
		}
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component[] comp=panel_1.getComponents();
				int i=0;
				while (i<comp.length){
					logger.info(comp.toString());
				repository.setPreference(((JLabel)((JPanel)comp[i]).getComponents()[0]).getText(), ((JTextField)((JPanel)comp[i]).getComponents()[1]).getText());
			
					i++;
				}
			}
		});
		panel_2.setLayout(new WrapLayout(WrapLayout.CENTER, 5, 5));
		panel_2.add(btnSave);

		add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new WrapLayout());

	}
}
