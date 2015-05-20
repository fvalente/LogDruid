package logdruid.ui;

import java.io.File;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import logdruid.data.Preferences;
import logdruid.data.Repository;

final public class FileSaverDialog {
	private static Logger logger = Logger.getLogger(FileSaverDialog.class.getName());
	/**
	 * The previous location were the last file has been opened.
	 */
	static private File previousOpenLocation = new File(".");

	/**
	 * The JFileChooser used to display the open fle dialog box.
	 */
	private JFileChooser dialog = new JFileChooser();

	/**
	 * The JFileChooser result.
	 */
	private int result = JFileChooser.CANCEL_OPTION;

	/*--------------------------------------------------------------------------
	 * Constructor(s)
	 *--------------------------------------------------------------------------*/
	/**
	 * Construct a File Chooser and display the file selection dialog
	 */
	public FileSaverDialog(Repository repo) {

		if (Preferences.getPreference("lastPath")!=null) {
			File temp= new File(Preferences.getPreference("lastPath"));
			if (temp.exists())
			previousOpenLocation=new File (Preferences.getPreference("lastPath"));
		}
		dialog.setCurrentDirectory(previousOpenLocation);
		dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		dialog.setMultiSelectionEnabled(false);

		// Multi Selection mode.
		// NOTE This mode is not yet implemented in the java 1.2 L&Fs.
		// _dialog.setMultiSelectionEnabled(true);

		result = dialog.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {

			// Memorisation des valeurs pour les afficher lors du prochain
			// affichage d'un dialogue d'ouverture de fichier.
			previousOpenLocation = dialog.getCurrentDirectory();
			Preferences.setPreference("lastPath", previousOpenLocation.getAbsolutePath());
			Preferences.persist();
		}
	}

	/*
	 * -------------------------------------------------------------------------
	 * Public methods
	 * -------------------------------------------------------------------------
	 */
	/**
	 * Returns the selected file in mono-file selection mode
	 */
	public File getSelectedFile() {
		return dialog.getSelectedFile();
	}

	/**
	 * Returns a list of selected files in the multi-selection mode.
	 * <p>
	 * <b>Warning:</b> This mode is not yet implemented in the java 1.2 L&Fs.
	 */
	public File[] getSelectedFiles() {
		File[] returnValue;

		if (dialog.isMultiSelectionEnabled()) {
			returnValue = dialog.getSelectedFiles();
		} else {
			returnValue = null;
		}
		return returnValue;
	}

	/**
	 * Returns true is the user choose the approve button of the file chooser
	 * dialog box.
	 */
	public boolean isValidate() {
		return (result == JFileChooser.APPROVE_OPTION);
	}

	/**
	 * Returns true if multiple files can be selected.
	 * <p>
	 * <b>Warning:</b> This functionnality is not yet implemented in the java
	 * 1.2 L&Fs.
	 */
	public boolean isMultiSelectionEnabled() {
		return dialog.isMultiSelectionEnabled();
	}

	/*
	 * -------------------------------------------------------------------------
	 * Private methods
	 * -------------------------------------------------------------------------
	 */

}
