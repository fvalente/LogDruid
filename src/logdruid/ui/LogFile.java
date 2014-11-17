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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

//import com.Ostermiller.Syntax.Colorer;

public class LogFile extends JScrollPane {
	String fileName;
	String filePath;
	public JTextArea logTextArea;

	// TextArea logTextArea;
	// Colorer colorer;

	public LogFile(File file) {

		// this.setDoubleBuffered(true);
		logTextArea = new JTextArea();
		// PropertyManager pm;
		// logTextArea = new TextArea(pm);
		this.setViewportView(logTextArea);
		try {
			String strLine;
			FileInputStream in = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			while ((strLine = br.readLine()) != null) {
				logTextArea.append(strLine + "\n");
			}
			/*
			 * FileReader in = new FileReader(file); logTextArea.read(in,
			 * file.toString());
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method initializes jTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	public JTextArea getlogTextArea() {
		if (logTextArea == null) {
			logTextArea = new JTextArea();
		}
		return logTextArea;
	}
}
