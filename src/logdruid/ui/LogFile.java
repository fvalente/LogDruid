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
