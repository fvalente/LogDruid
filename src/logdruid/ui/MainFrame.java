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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicBorders.SplitPaneBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.Color;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import logdruid.data.ChartData;
import logdruid.data.DataVault;
import logdruid.data.MineResultSet;
import logdruid.data.Preferences;
import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.ui.RecordingList;
import logdruid.ui.chart.GraphPanel;
import logdruid.ui.mainpanel.EventRecordingSelectorPanel;
import logdruid.ui.mainpanel.MetadataRecordingSelectorPanel;
import logdruid.ui.mainpanel.PreferencePanel;
import logdruid.ui.mainpanel.ReportPanel;
import logdruid.ui.mainpanel.ReportRecordingSelectorPanel;
import logdruid.ui.mainpanel.SourceInfoPanel;
import logdruid.ui.mainpanel.SourcePanel;
import logdruid.ui.mainpanel.StatRecordingSelectorPanel;
import logdruid.util.DataMiner;
import logdruid.util.Persister;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.awt.Font;

import javax.swing.JProgressBar;
import javax.swing.border.MatteBorder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.BoxLayout;

import java.awt.Insets;

public class MainFrame extends JFrame {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private String treeSelected = "";
	private GraphPanel graphPanel = null;
	private ReportPanel reportPanel = null;
	JPanel panel_1;
	JPanel panel_2;
	final JTree tree;
	private JPanel contentPane;
	private Repository repository;
	private DefaultMutableTreeNode DMTnode_sources;
	public File configFile;
	private JSpinner startTimeSpinner;
	private JSpinner.DateEditor timeEditor;
	private JSpinner endTimeSpinner;
	private JSpinner.DateEditor timeEditor2;
	public String currentRepositoryFile = "New";
//	MineResultSet mineResultSet;
	public static ChartData cd;
	private MainFrame thiis;
	JProgressBar progressBar;
	int progressBarValue;
	 boolean working=false;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {

		thiis = this;
		repository = new Repository();
		cd = new ChartData();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1024, 768);
		Preferences.load();
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmNewSource = new JMenuItem("New");
		mntmNewSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repository = new Repository();
				currentRepositoryFile = "New";
				thiis.setTitle("LogDruid  - " + currentRepositoryFile);
				updateTreeSources(repository.getSources());
				treeSelected();
			}
		});
		mnFile.add(mntmNewSource);
		thiis.setTitle("LogDruid");
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileSaverDialog fileChooserDialog = new FileSaverDialog(repository);
				if ((fileChooserDialog != null) && (fileChooserDialog.isValidate())) {
					// repository.open(fileChooserDialog.getSelectedFile());
					TreePath tp = tree.getSelectionPath();
					File file = fileChooserDialog.getSelectedFile();
					configFile = fileChooserDialog.getSelectedFile();
					thiis.setTitle("LogDruid - " + file.getName() + " - " + repository.getBaseSourcePath());
					repository = (Repository) Persister.open(fileChooserDialog.getSelectedFile());
					updateTreeSources(repository.getSources());
	//				tree.setSelectionPath(tp);

						Thread t =new Thread()  {
							public void run() {
								if (working==false){
				            	working=true;
				            	try{
								if (treeSelected.equals("Chart")) {
									graphPanel=null;
									reportPanel=null;
									thiis.setValueNow(0);
									progressBarValue=0;
									File test=new File (repository.getBaseSourcePath());
									if (test.exists()){
				            	DataVault.setMineResultSet(DataMiner.gatherMineResultSet(repository,thiis));
								cd = DataMiner.gatherSourceData(repository);
								}
								thiis.setValueNow(progressBarValue);
								} else if (treeSelected.equals("Reports")) {
									reportPanel=null;
									graphPanel=null;
									thiis.setValueNow(0);
									progressBarValue=0;
									File test=new File (repository.getBaseSourcePath());
									if (test.exists()){
										DataVault.setMineResultSet(DataMiner.gatherMineResultSet(repository,thiis));
								cd = DataMiner.gatherSourceData(repository);}
								thiis.setValueNow(progressBarValue);
								}
				            	} catch (Exception e){
									logger.error("exception: ", e);
								}
								working=false;
								treeSelected();
				            }}
				        };
				        t.start();
				}
			}
		});
		mnFile.add(mntmOpen);

		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Persister.save(configFile, (Repository) repository);
				// repository.open(fileChooserDialog.getSelectedFile());
				// repository=(Repository)persister.open(fileChooserDialog.getSelectedFile());
			}
		});
		mnFile.add(mntmSave);

		JMenuItem mntmSaveAs = new JMenuItem("Save as");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileSaverDialog fileChooserDialog = new FileSaverDialog(repository);
				if ((fileChooserDialog != null) && (fileChooserDialog.isValidate())) {
					Persister.save(fileChooserDialog.getSelectedFile(), (Repository) repository);
					thiis.setTitle("LogDruid - " + fileChooserDialog.getSelectedFile().getName() + " - " + repository.getBaseSourcePath());
					configFile = fileChooserDialog.getSelectedFile();
					// repository.open(fileChooserDialog.getSelectedFile());
					// repository=(Repository)persister.open(fileChooserDialog.getSelectedFile());
				}
			}
		});

		mnFile.add(mntmSaveAs);

		/*
		 * JMenuItem mntmExportData = new JMenuItem("Export Data");
		 * mntmExportData.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) { FileSaverDialog fileChooserDialog =
		 * new FileSaverDialog(); if ((fileChooserDialog != null) &&
		 * (fileChooserDialog.isValidate())) {
		 * Persister.save(fileChooserDialog.getSelectedFile(),
		 * DataMiner.exportData(repository)); //
		 * repository.open(fileChooserDialog.getSelectedFile()); //
		 * repository=(Repository
		 * )persister.open(fileChooserDialog.getSelectedFile()); } } });
		 * mnFile.add(mntmExportData);
		 */
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);

		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(NORMAL);
			}
		});

		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		JMenuItem mntmPreferences = new JMenuItem("Preferences");
		mnEdit.add(mntmPreferences);

		mntmPreferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tree.setSelectionRow(2);
				treeSelected();
			}
		});

		JMenu mnChartMenu = new JMenu("Chart");
		menuBar.add(mnChartMenu);

		JMenuItem mntmVisualize = new JMenuItem("Visualize");
		mntmVisualize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tree.setSelectionRow(tree.getRowCount() - 1);
				treeSelected();
			}
		});
		mnChartMenu.add(mntmVisualize);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		contentPane.add(panel);

		JButton btnNewButton = new JButton("New button");
		panel.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("New button");
		panel.add(btnNewButton_1);

		JPanel panel_7 = new JPanel();

		panel_1 = new JPanel();
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		contentPane.add(splitPane, BorderLayout.CENTER);
		splitPane.setRightComponent(panel_1);
		splitPane.setLeftComponent(panel_7);
		panel_1.setLayout(new BorderLayout(0, 0));
		panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		contentPane.add(panel_2, BorderLayout.NORTH);
		DefaultTreeModel defaultTreeModel;
		DMTnode_sources = new DefaultMutableTreeNode("Sources");
		panel_7.setLayout(new BorderLayout(0, 0));
		panel_7.setSize(150, 10000);
		panel_7.setMinimumSize(new Dimension(130, 1000));
		tree = new JTree();
		panel_7.add(tree);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				treeSelected();
			}

		});
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String comp = e.getComponent().toString();
				// logger.info(comp);

			}
		});
		tree.setForeground(Color.WHITE);
		// defaultTreeModel.reload(node);
		tree.setModel(

		new DefaultTreeModel(new DefaultMutableTreeNode("All") {
			{
				DefaultMutableTreeNode DMTnode_1;
				DefaultMutableTreeNode DMTnode_2;

				DefaultMutableTreeNode DMTconfiguration;
				DMTnode_1 = new DefaultMutableTreeNode("Configuration");
				DMTnode_1.add(new DefaultMutableTreeNode("DateFormat"));
				DMTnode_1.add(new DefaultMutableTreeNode("Preferences"));
				DMTnode_1.add(new DefaultMutableTreeNode("Recordings"));
				// DMTnode_1.add(new DefaultMutableTreeNode("Chartting"));
				// DMTnode_1.add(new DefaultMutableTreeNode("Reporting"));
				// DMTnode_1.add(new DefaultMutableTreeNode("Advanced"));
				add(DMTnode_1);
				add(DMTnode_1);
				add(DMTnode_sources);
				add(new DefaultMutableTreeNode("Chart"));
				add(new DefaultMutableTreeNode("Reports"));

			}
		}));
		tree.setSelectionRow(0);
		tree.setSelectionRow(0);
		tree.setRootVisible(false);
		tree.setSelectionRow(1);

		JButton btnMain = new JButton("Gather");
		btnMain.setForeground(Color.BLUE);
		btnMain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Thread t =new Thread()  {
					public void run() {
						if (working==false){
							working=true;
		            	try{
							reportPanel=null;
							graphPanel=null;
							thiis.setValueNow(0);
							progressBarValue=0;
							File test=new File (repository.getBaseSourcePath());
							if (test.exists()){
								DataVault.setMineResultSet(DataMiner.gatherMineResultSet(repository,thiis));
						cd = DataMiner.gatherSourceData(repository);}
						thiis.setValueNow(progressBarValue);
		            	} catch (Exception e){
							logger.error("exception: ", e);
							working=false;
						}
						working=false;
						tree.setSelectionRow(tree.getRowCount() - 2);
						treeSelected();
		            }
		        }};
		        t.start();
/*				if (working==false){
				Thread t =new Thread()
		        {  
					
					public void run() {
						working=true;
					try {	
						graphPanel = null;
						reportPanel = null;
						thiis.setValueNow(0);
						progressBarValue=0;
						DataVault.setMineResultSet(DataMiner.gatherMineResultSet(repository,thiis));
						cd = DataMiner.gatherSourceData(repository);
						thiis.setValueNow(progressBarValue);
	            	} catch (Exception e){
						logger.error("exception: ", e);
					}
						working=false;
						tree.setSelectionRow(tree.getRowCount() - 2);
		            }
		        };
		        t.start();
				}*/
			}
		});
		btnMain.setFont(new Font("Dialog", Font.BOLD, 11));
		btnMain.setHorizontalAlignment(SwingConstants.LEFT);
		// btnMain.setVisible(false);
		panel_2.add(btnMain);

		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		panel_2.add(separator);

		startTimeSpinner = new JSpinner(new SpinnerDateModel());
		timeEditor = new JSpinner.DateEditor(startTimeSpinner, "dd-MM-yyyy HH:mm:ss");
		startTimeSpinner.setEditor(timeEditor);
		startTimeSpinner.setValue(new Date());

		panel_2.add(startTimeSpinner);

		endTimeSpinner = new JSpinner(new SpinnerDateModel());
		timeEditor2 = new JSpinner.DateEditor(endTimeSpinner, "dd-MM-yyyy HH:mm:ss");
		endTimeSpinner.setEditor(timeEditor2);
		endTimeSpinner.setValue(new Date());
		panel_2.add(endTimeSpinner);

		JCheckBox chckbxRelative = new JCheckBox("relative");
		chckbxRelative.setFont(new Font("Dialog", Font.BOLD, 11));
		chckbxRelative.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (treeSelected.equals("Chart")) {
					graphPanel.load(panel_2);
				}
			}
		});

		JButton btnRefresh = new JButton("Reset");
		btnRefresh.setFont(new Font("Dialog", Font.BOLD, 11));
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (treeSelected.equals("Chart")) {
					graphPanel.resetTimePeriod(panel_2);
					graphPanel.loadGroupCheckbox(panel_2);
					graphPanel.load(panel_2);
				}
			}
		});
		panel_2.add(btnRefresh);
		panel_2.add(chckbxRelative);

		JButton btnReset = new JButton("Refresh");
		btnReset.setFont(new Font("Dialog", Font.BOLD, 11));
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (treeSelected.equals("Chart")) {
					graphPanel.loadGroupCheckbox(panel_2);
					graphPanel.load(panel_2);
				}
			}
		});
		panel_2.add(btnReset);

		JPanel panel_3 = new JPanel();
		panel_3.setLayout(new BorderLayout(0, 0));
		contentPane.add(panel_3, BorderLayout.SOUTH);

		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
		flowLayout_1.setVgap(0);
		panel_3.add(panel_4, BorderLayout.EAST);
		MemInspector mi = new MemInspector();
		panel_4.add(mi);

		JPanel panel_5 = new JPanel();
		panel_3.add(panel_5, BorderLayout.CENTER);
		panel_5.setLayout(new BorderLayout(0, 0));

		JPanel panel_6 = new JPanel();
		panel_5.add(panel_6);
		GridBagLayout gbl_panel_6 = new GridBagLayout();
		gbl_panel_6.columnWidths = new int[] { 100, 100,300,0  };
		gbl_panel_6.rowHeights = new int[] { 20, 0 };
		gbl_panel_6.columnWeights = new double[] { 1.0,1.0,1.0,Double.MIN_VALUE };
		gbl_panel_6.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_6.setLayout(gbl_panel_6);

		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.insets = new Insets(0, 0, 0, 5);
		gbc_progressBar.gridx = 2;
		gbc_progressBar.gridy = 0;
		progressBar.setVisible(true);
		panel_6.add(progressBar, gbc_progressBar);
		/*
		 * startTimeSpinner.setEnabled(false); endTimeSpinner.setEnabled(false);
		 * btnReset.setEnabled(false); btnRefresh.setEnabled(false);
		 * chckbxRelative.setEnabled(false);
		 */
		tree.expandPath(tree.getPathForRow(0));
	}
	
    void updateProgress(final int newValue) {
        thiis.setValueNow(newValue);
    }
    
    void updateProgress2() {
    	progressBar.setValue(progressBarValue++);
        logger.debug("progressBarValue " + progressBarValue);
    }
    
    public void setMaxProgress(final int newValue) {
        progressBar.setMaximum(newValue);
    }
    
    public void setValue(final int j) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(j);
                logger.info("setValue " + j);
            }
        });
    }
    
    public void setValueNow(final int j) {
    		progressBar.setValue(j);
              /*  logger.info("setValueNow " + j);
                StackTraceElement[] elements = Thread.currentThread().getStackTrace();

                for(int i=0; i<elements.length; i++) {
                	logger.debug(elements[i]);
                }*/
                
    }
    
    public void progress() {
    	 SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                updateProgress2();
             }
             });
    }
    
    
	public void updateTreeSources(ArrayList<Source> sources) {
		TreePath initialTreePath = tree.getSelectionPath();
		DefaultMutableTreeNode DMTNsources_child;
		DMTnode_sources.removeAllChildren();
		// ((DefaultTreeModel)tree.getModel()).reload();
		for (int i = 0; i < sources.size(); i++) {
			if (((Source) sources.get(i)).getActive()) {
				String name = ((Source) sources.get(i)).getSourceName();
				DMTNsources_child = new DefaultMutableTreeNode(name);
				DMTNsources_child.add(new DefaultMutableTreeNode("Identification"));
				DMTNsources_child.add(new DefaultMutableTreeNode("Data"));
				DMTNsources_child.add(new DefaultMutableTreeNode("Event"));
				DMTNsources_child.add(new DefaultMutableTreeNode("Report"));
				DMTnode_sources.add(DMTNsources_child);
				DMTnode_sources.getParent();
			}
		}

		// DMTnode_sources.
		// ((DefaultTreeModel)tree.getModel()).reload();
		// ((DefaultTreeModel)tree.getModel()).nodeStructureChanged(DMTnode_sources.getPath());
		TreeNode[] t = DMTnode_sources.getPath();
		((DefaultTreeModel) tree.getModel()).nodeStructureChanged(t[(t.length - 1)]);
		tree.setSelectionPath(initialTreePath);
		tree.expandPath(initialTreePath);
		// tree.fireTreeExpanded(initialTreePath);
		tree.revalidate();
		tree.repaint();
	}

	private void treeSelected() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node != null) {
			logger.info(node.toString());
			treeSelected = node.toString();
			if (treeSelected.equals("Sources")) {
				panel_1.removeAll();
				panel_1.add(new SourcePanel(repository, thiis));
				panel_1.revalidate();
			} else if (treeSelected.equals("Recordings")) {
				panel_1.removeAll();
				panel_1.add(new RecordingList(repository));
				panel_1.revalidate();
			} else if (treeSelected.equals("Preferences")) {
				panel_1.removeAll();
				panel_1.add(new PreferencePanel(repository));
				panel_1.revalidate();
			} else if (treeSelected.equals("Data")) {
				panel_1.removeAll();
				panel_1.add(new StatRecordingSelectorPanel(repository, (Source) repository.getSource(node.getParent().toString())));
				panel_1.revalidate();
			} else if (treeSelected.equals("Event")) {
				panel_1.removeAll();
				panel_1.add(new EventRecordingSelectorPanel(repository, (Source) repository.getSource(node.getParent().toString())));
				panel_1.revalidate();
			} else if (treeSelected.equals("Report")) {
				panel_1.removeAll();
				panel_1.add(new ReportRecordingSelectorPanel(repository, (Source) repository.getSource(node.getParent().toString())));
				panel_1.revalidate();
			} else if (treeSelected.equals("Reports")) {

				Thread t =new Thread()
		        {
		            public void run() {
						if (working==false){
		            	working=true;
		            	try {
						panel_1.removeAll();
						logger.info("Reports panel loading ");				
						if (DataVault.getMineResultSet() == null) {
						thiis.setValueNow(0);
						progressBarValue=0;	
						DataVault.setMineResultSet(DataMiner.gatherMineResultSet(repository,thiis));
						cd = DataMiner.gatherSourceData(repository);
						logger.info("gathering source data");
		            	thiis.setValueNow(progressBarValue);
						}
						if (reportPanel == null) {
							logger.info(" new graph Panel");
							reportPanel = new ReportPanel(repository, DataVault.getMineResultSet());
						}

						panel_1.add(reportPanel);
						panel_1.validate();
						panel_1.repaint();
						logger.info("Report panel loaded ");
		            	} catch (Exception e){
							logger.error("exception: ", e);
							working=false;
						}
						working=false;
		            }
		            }};
		        t.start();
				
			} else if (treeSelected.equals("Identification")) {
				panel_1.removeAll();
				panel_1.add(new MetadataRecordingSelectorPanel(repository, (Source) repository.getSource(node.getParent().toString())));
				panel_1.revalidate();
			} else if (treeSelected.equals("DateFormat")) {
				panel_1.removeAll();
				panel_1.add(new DateEditor(repository));
				panel_1.revalidate();
			} else if (treeSelected.equals("Chart")) {
				if (working==false){
					Thread t =new Thread()
			        {
			            public void run() {
			            	working=true;
			            	try{
							panel_1.removeAll();
							logger.info("Chart panel loading ");				
							if (DataVault.getMineResultSet() == null) {
							thiis.setValueNow(0);
							progressBarValue=0;	
							DataVault.setMineResultSet(DataMiner.gatherMineResultSet(repository,thiis));
							cd = DataMiner.gatherSourceData(repository);
							logger.info("gathering source data");
			            	thiis.setValueNow(progressBarValue);
							}
							if (graphPanel == null) {
								logger.info(" new graph Panel");
								graphPanel = new GraphPanel(repository, panel_2, DataVault.getMineResultSet(), cd, thiis);
							}
							panel_1.add(graphPanel);
							panel_1.validate();
							panel_1.repaint();
							logger.info("Chart panel loaded ");
		            	} catch (Exception e){
							logger.error("exception: ", e);
						}
							working=false;
			            }
			        };
			        t.start();
				}
			} else {
				ArrayList sources = repository.getSources();
				Iterator it = sources.iterator();
				while (it.hasNext()) {
					Source src = (Source) it.next();
					if (src.getSourceName().equals(treeSelected)) {
						panel_1.removeAll();
						panel_1.add(new SourceInfoPanel(repository, src));
						panel_1.revalidate();
					}
				}
			}

		}
	}
}

/*
 * 
 * DefaultMutableTreeNode node_1; DefaultMutableTreeNode node_2; node_1 = new
 * DefaultMutableTreeNode("Configuration"); node_1.add(new
 * DefaultMutableTreeNode("DateFormat")); node_1.add(new
 * DefaultMutableTreeNode("Recordings")); node_1.add(new
 * DefaultMutableTreeNode("Groups")); node_1.add(new
 * DefaultMutableTreeNode("Chartting")); node_1.add(new
 * DefaultMutableTreeNode("Reporting")); node_1.add(new
 * DefaultMutableTreeNode("Advanced")); add(node_1); node_1 = new
 * DefaultMutableTreeNode("Sources"); node_2 = new
 * DefaultMutableTreeNode("Method Servers"); node_2.add(new
 * DefaultMutableTreeNode("File Pattern")); node_2.add(new
 * DefaultMutableTreeNode("Recordings")); node_2.add(new
 * DefaultMutableTreeNode("Date Format")); node_1.add(node_2); add(node_1);
 * add(new DefaultMutableTreeNode("Chart")); add(new
 * DefaultMutableTreeNode("Report"));
 */