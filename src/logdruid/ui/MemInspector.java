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
