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

import java.io.File;
import javax.swing.JFileChooser;

final public class FileChooserDialog {

	/*
	 * -------------------------------------------------------------------------
	 * Public attributes
	 * -------------------------------------------------------------------------
	 */

	/*
	 * -------------------------------------------------------------------------
	 * Private attributes
	 * -------------------------------------------------------------------------
	 */
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
	public FileChooserDialog() {

		dialog.setCurrentDirectory(previousOpenLocation);
		dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dialog.setMultiSelectionEnabled(true);

		// Multi Selection mode.
		// NOTE This mode is not yet implemented in the java 1.2 L&Fs.
		// _dialog.setMultiSelectionEnabled(true);

		result = dialog.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {

			// Memorisation des valeurs pour les afficher lors du prochain
			// affichage d'un dialogue d'ouverture de fichier.
			previousOpenLocation = dialog.getCurrentDirectory();
		}
	}

	/*--------------------------------------------------------------------------
	 * Constructor(s)
	 *--------------------------------------------------------------------------*/
	/**
	 * Construct a File Chooser and display the file selection dialog
	 */
	public FileChooserDialog(File previousLocation) {

		if (previousLocation.exists() && previousLocation.isDirectory()) {
			dialog.setCurrentDirectory(previousLocation);
		} else {
			dialog.setCurrentDirectory(previousOpenLocation);
		}
		dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dialog.setMultiSelectionEnabled(true);

		// Multi Selection mode.
		// NOTE This mode is not yet implemented in the java 1.2 L&Fs.
		// _dialog.setMultiSelectionEnabled(true);

		result = dialog.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {

			// Memorisation des valeurs pour les afficher lors du prochain
			// affichage d'un dialogue d'ouverture de fichier.
			previousOpenLocation = dialog.getCurrentDirectory();
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
