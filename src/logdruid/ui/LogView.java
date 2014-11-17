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
package logdruid.ui;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class LogView {

	JTabbedPane jTabbedPane;
	int paneNumber = 0;
	public LogFile curLogFile = null;

	/**
	 * This is the default constructor
	 */
	public LogView() {
		jTabbedPane = new JTabbedPane();
		initialize();
	}

	public void add(File file) {
		StringBuffer strBuf;
		strBuf = new StringBuffer(file.getName());
		LogFile logFile = new LogFile(file);
		logFile.setName(strBuf.toString());
		jTabbedPane.add(logFile);
		curLogFile = logFile;
		// jTabbedPane.setComponentAt(paneNumber,new
		// TabButton(strBuf.toString()));
		jTabbedPane.setToolTipTextAt(paneNumber, file.getAbsolutePath());
		paneNumber++;
		// jTabbedPane.indexOfTabComponent(logFile);
	}

	/**
	 * This method initializes logView
	 * 
	 * @return javax.swing.LogView
	 */
	public JTabbedPane getLogView() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {

	}

	class TabButton extends JPanel implements ActionListener {

		public TabButton(String label) {

			super(new FlowLayout());
			add(new JLabel(label));
			JButton button = new JButton("X");
			// button.addActionListener( this );
			// button.setMargin(new Insets(0,0,0,0));
			// add( button );
		}

		public void actionPerformed(ActionEvent ae) {
			// close the tab which was clicked
			jTabbedPane.remove(jTabbedPane.indexOfTabComponent(this));
		}
	}
}// @jve:decl-index=0:visual-constraint="10,10"
