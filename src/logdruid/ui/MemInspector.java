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

// imports

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MemInspector extends JPanel {
	private JProgressBar progress;
	private Timer timer;

	/**
	 * Constructeur
	 */
	protected MemInspector() {
		super(new BorderLayout());
		initGUI();

		timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				long total = Runtime.getRuntime().totalMemory();
				long free = Runtime.getRuntime().freeMemory();
				long used = total - free;

				progress.setMaximum(asMegabytes(total));
				progress.setValue(asMegabytes(used));
				progress.setString("" + asMegabytes(used) + "/" + asMegabytes(total));
			}
		});
		start();
	}

	private void start() {
		timer.start();
	}

	public void stop() {
		// System.err.println("stopping memInspector");
		timer.stop();
	}

	private int asMegabytes(long free) {
		return (int) (free / 1024 / 1024);
	}

	private void initGUI() {
		progress = new JProgressBar();
		progress.setStringPainted(true);
		add(progress, BorderLayout.CENTER);
		JButton btGC = new JButton();
		btGC.setMargin(new Insets(0, 1, 0, 1));
		btGC.setText("GC");
		btGC.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btGC_actionPerformed();
			}
		});
		add(btGC, BorderLayout.EAST);
	}

	/**
	 * Lance le garbage collector.
	 */
	void btGC_actionPerformed() {
		System.gc();
	}
}
