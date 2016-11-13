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
package logdruid.util;

import java.util.*;
import java.io.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import logdruid.data.Repository;

public final class FileListing {
	private static Logger logger = Logger.getLogger(FileListing.class.getName());

	public static void main(String... aArgs) throws FileNotFoundException {
		File startingDirectory = new File(aArgs[0]);
		FileListing listing = new FileListing();
		List<File> files = listing.getFileListing(startingDirectory);

		// print out all file names, in the the order of File.compareTo()
		for (File file : files) {
			if (logger.isEnabledFor(Level.DEBUG))
				logger.debug(file);
		}
	}

	/**
	 * Recursively walk a directory tree and return a List of all Files found;
	 * the List is sorted using File.compareTo().
	 *
	 * @param aStartingDir
	 *            is a valid directory, which can be read.
	 */
	public static List<File> getFileListing(File aStartingDir) throws FileNotFoundException {
		validateDirectory(aStartingDir);
		List<File> result = getFileListingNoSort(aStartingDir);
		Collections.sort(result);
		return result;
	}

	// PRIVATE

	private static List<File> getFileListingNoSort(File aStartingDir) throws FileNotFoundException {
		List<File> result = new ArrayList<>();
		File[] filesAndDirs = aStartingDir.listFiles();
		if (filesAndDirs!=null){
			List<File> filesDirs = Arrays.asList(filesAndDirs);
			for (File file : filesDirs) {
				result.add(file); // always add, even if directory
				if (!file.isFile()) {
					// must be a directory
					// recursive call!
					List<File> deeperList = getFileListingNoSort(file);
					result.addAll(deeperList);
				}
			}			
		}
		return result;
	}

	/**
	 * Directory is valid if it exists, does not represent a file, and can be
	 * read.
	 */
	private static void validateDirectory(File aDirectory) throws FileNotFoundException {
		if (aDirectory == null) {
			throw new IllegalArgumentException("Directory should not be null.");
		}
		if (!aDirectory.exists()) {
			throw new FileNotFoundException("Directory does not exist: " + aDirectory);
		}
		if (!aDirectory.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: " + aDirectory);
		}
		if (!aDirectory.canRead()) {
			throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
		}
	}

	public static String getPath(Repository repository, File file) {
		// TODO Auto-generated method stub
		String temp = null;
		try {
			if (repository.isRecursiveMode()) {
				temp = new File(repository.getBaseSourcePath()).toURI().relativize(new File(file.getCanonicalPath()).toURI()).getPath();
			} else {
				temp = new File(repository.getBaseSourcePath()).toURI().relativize(new File(file.getCanonicalPath()).toURI()).getPath();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return temp;
	}
}
