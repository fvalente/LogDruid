package logdruid.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
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

import logdruid.data.Repository;
import logdruid.data.Source;
import logdruid.ui.NewRecordingList;
import logdruid.ui.chart.GraphPanel;
import logdruid.ui.mainpanel.EventRecordingSelectorPanel;
import logdruid.ui.mainpanel.MetadataRecordingSelectorPanel;
import logdruid.ui.mainpanel.SourcePanel;
import logdruid.ui.mainpanel.StatRecordingSelectorPanel;
import logdruid.util.DataMiner;
import logdruid.util.Persister;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import org.apache.log4j.Logger;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Date;
import java.util.Vector;

public class MainFrame extends JFrame {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private String treeSelected="";
	private GraphPanel graphPanel =null;
JPanel panel_1;
JPanel panel_2;
final JTree tree;
	private JPanel contentPane;
	private Repository repository;
	private DefaultMutableTreeNode DMTnode_sources;
	private File configFile; 
	private JSpinner startTimeSpinner;
	private JSpinner.DateEditor timeEditor;
	private JSpinner endTimeSpinner;
	private JSpinner.DateEditor timeEditor2;
	

private MainFrame thiis;
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
		
		thiis=this;
		repository= new Repository();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmNewSource = new JMenuItem("NewSource");
		mnFile.add(mntmNewSource);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileSaverDialog fileChooserDialog= new FileSaverDialog();
				 if ((fileChooserDialog != null) && (fileChooserDialog.isValidate())) {
						//repository.open(fileChooserDialog.getSelectedFile());
						TreePath tp=tree.getSelectionPath();
						File file=fileChooserDialog.getSelectedFile();
					 repository=(Repository)Persister.open(fileChooserDialog.getSelectedFile());
									updateTreeSources(repository.getSources());
									tree.setSelectionPath(tp);
								    
									configFile=fileChooserDialog.getSelectedFile();
									thiis.setTitle(file.getName()+ " on " +repository.getBaseSourcePath());
									treeSelected();
																		 
					   }
			}
		});
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					 Persister.save(configFile,(Repository)repository);
						//repository.open(fileChooserDialog.getSelectedFile());
						//			repository=(Repository)persister.open(fileChooserDialog.getSelectedFile());
			}
		});	
		mnFile.add(mntmSave);
		
		JMenuItem mntmSaveAs = new JMenuItem("Save as");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileSaverDialog fileChooserDialog= new FileSaverDialog();
				 if ((fileChooserDialog != null) && (fileChooserDialog.isValidate())) {
					 Persister.save(fileChooserDialog.getSelectedFile(),(Repository)repository);
						thiis.setTitle(fileChooserDialog.getSelectedFile().getName()+ " on " +repository.getBaseSourcePath());
						configFile=fileChooserDialog.getSelectedFile();
						//repository.open(fileChooserDialog.getSelectedFile());
						//			repository=(Repository)persister.open(fileChooserDialog.getSelectedFile());
					   }
			}
		});	
	
		mnFile.add(mntmSaveAs);
		
		JMenuItem mntmExportData = new JMenuItem("Export Data");
		mntmExportData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileSaverDialog fileChooserDialog= new FileSaverDialog();
				 if ((fileChooserDialog != null) && (fileChooserDialog.isValidate())) {
					 Persister.save(fileChooserDialog.getSelectedFile(),DataMiner.exportData(repository));
						//repository.open(fileChooserDialog.getSelectedFile());
						//			repository=(Repository)persister.open(fileChooserDialog.getSelectedFile());
			}
			}});
		mnFile.add(mntmExportData);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmPreferences = new JMenuItem("Preferences");
		mnEdit.add(mntmPreferences);
		
		JMenu mnChartMenu = new JMenu("Chart");
		menuBar.add(mnChartMenu);
		
		JMenuItem mntmVisualize = new JMenuItem("Visualize");
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
		
		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane);
		
		tree = new JTree();
	    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
treeSelected();
			}


				

		});
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String comp= e.getComponent().toString();
				//logger.info(comp);

			}
		});
		tree.setForeground(Color.WHITE);
		tree.setEditable(true);
		DefaultTreeModel defaultTreeModel;

		DMTnode_sources = new DefaultMutableTreeNode("Sources");
		//defaultTreeModel.reload(node);
		tree.setModel(
				
				new DefaultTreeModel(
			new DefaultMutableTreeNode("All") {
				{
					DefaultMutableTreeNode DMTnode_1;
					DefaultMutableTreeNode DMTnode_2;

					DefaultMutableTreeNode DMTconfiguration;
					DMTnode_1 = new DefaultMutableTreeNode("Configuration");
					DMTnode_1.add(new DefaultMutableTreeNode("DateFormat"));
					DMTnode_1.add(new DefaultMutableTreeNode("Recordings"));
					DMTnode_1.add(new DefaultMutableTreeNode("Chartting"));
					DMTnode_1.add(new DefaultMutableTreeNode("Reporting"));
					DMTnode_1.add(new DefaultMutableTreeNode("Advanced"));
					add(DMTnode_1);
					add(DMTnode_1);
					add(DMTnode_sources);
					add(new DefaultMutableTreeNode("Chart"));
					add(new DefaultMutableTreeNode("Report"));
					
				}
			}
		));
		splitPane.setLeftComponent(tree);
		
		panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		contentPane.add(panel_2, BorderLayout.NORTH);
		
		JButton btnMain = new JButton("Discovery");
		btnMain.setHorizontalAlignment(SwingConstants.LEFT);
		panel_2.add(btnMain);
		tree.setSelectionRow(0);


		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		panel_2.add(separator);
		
		startTimeSpinner = new JSpinner( new SpinnerDateModel() );
		timeEditor = new JSpinner.DateEditor(startTimeSpinner, "dd-MM-yyyy HH:mm:ss");
		startTimeSpinner.setEditor(timeEditor);
		startTimeSpinner.setValue(new Date()); 
		panel_2.add(startTimeSpinner);
		
		endTimeSpinner = new JSpinner( new SpinnerDateModel() );
		timeEditor2 = new JSpinner.DateEditor(endTimeSpinner, "dd-MM-yyyy HH:mm:ss");
		endTimeSpinner.setEditor(timeEditor2);
		endTimeSpinner.setValue(new Date()); 
		panel_2.add(endTimeSpinner);
		
		
		JCheckBox chckbxRelative = new JCheckBox("relative");
		chckbxRelative.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (treeSelected.equals("Chart")){
					graphPanel.load(panel_2);;
				}
			}
		});
		
		JButton btnReset_1 = new JButton("Reset");
		btnReset_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (treeSelected.equals("Chart")){
					graphPanel.resetTimePeriod(panel_2);
					graphPanel.load(panel_2);;
				}
			}
		});
		panel_2.add(btnReset_1);
		panel_2.add(chckbxRelative);
		
		JButton btnReset = new JButton("Refresh");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (treeSelected.equals("Chart")){
					graphPanel.load(panel_2);;
				}
			}
		});
		panel_2.add(btnReset);
		tree.setSelectionRow(0);
		
		JPanel panel_3 = new JPanel();
		MemInspector mi= new MemInspector();
		panel_3.add(mi,BorderLayout.EAST);
		contentPane.add(panel_3, BorderLayout.SOUTH);
		
		

		
		
	}

	public void updateTreeSources(Vector<Source> sources){
		TreePath initialTreePath=tree.getSelectionPath();
		DefaultMutableTreeNode DMTNsources_child;
		DMTnode_sources.removeAllChildren();
	//    ((DefaultTreeModel)tree.getModel()).reload();
	    for (int i = 0; i < sources.size(); i++) {
	    	if (((Source)sources.get(i)).getActive()){
	    	String name= ((Source)sources.get(i)).getSourceName();
	    	DMTNsources_child = new DefaultMutableTreeNode(name);
	    	DMTNsources_child.add(new DefaultMutableTreeNode("Identification"));
			DMTNsources_child.add(new DefaultMutableTreeNode("Data"));
			DMTNsources_child.add(new DefaultMutableTreeNode("Event"));
			DMTnode_sources.add(DMTNsources_child);	    
			DMTnode_sources.getParent();
			}
	    }
	    
	    //DMTnode_sources.
	//    ((DefaultTreeModel)tree.getModel()).reload();
	//    ((DefaultTreeModel)tree.getModel()).nodeStructureChanged(DMTnode_sources.getPath());
	    TreeNode[] t =DMTnode_sources.getPath();
	    ((DefaultTreeModel)tree.getModel()).nodeStructureChanged(t[(t.length-1)]);
	    tree.setSelectionPath(initialTreePath);	   

	    tree.expandPath( initialTreePath);
//	    tree.fireTreeExpanded(initialTreePath);
	    
	    tree.revalidate();
	    tree.repaint();
	}

	
	private void treeSelected() {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	    if (node!=null){
	    logger.info(node.toString());
		treeSelected=node.toString();
		switch (node.toString()){
		case "Sources": panel_1.removeAll();
		panel_1.add(new SourcePanel(repository,thiis));
		panel_1.revalidate();
		break;
		case "Recordings": panel_1.removeAll();
		panel_1.add(new NewRecordingList(repository));
		panel_1.revalidate();
		break;
		case "Data": panel_1.removeAll();
		panel_1.add(new StatRecordingSelectorPanel(repository,(Source)repository.getSource(node.getParent().toString())));
		panel_1.revalidate();
		break;
		case "Event": panel_1.removeAll();
		panel_1.add(new EventRecordingSelectorPanel(repository,(Source)repository.getSource(node.getParent().toString())));
		panel_1.revalidate();
		break;
		case "DateFormat": panel_1.removeAll();
		panel_1.add(new DateEditor(repository));
		panel_1.revalidate();
		break;
		case "Identification": panel_1.removeAll();
	   // logger.info("source: "+node.getParent().toString());
		panel_1.add(new MetadataRecordingSelectorPanel(repository,(Source)repository.getSource(node.getParent().toString())));
		panel_1.revalidate();
		break;
		/*case "Chart": panel_1.removeAll();
		logger.info("Chart panel loading ");
		panel_1.add(new BasicGraphPanel(repository));
		panel_1.revalidate();
		logger.info("Chart panel loaded ");
		break;*/
		case "Chart": panel_1.removeAll();
		logger.info("Chart panel loading ");
		graphPanel=new GraphPanel(repository,panel_2);
		panel_1.add(graphPanel);
		panel_1.revalidate();
		logger.info("Chart panel loaded ");
		break;
		}}}
	
	
	
}

/*

DefaultMutableTreeNode node_1;
DefaultMutableTreeNode node_2;
node_1 = new DefaultMutableTreeNode("Configuration");
	node_1.add(new DefaultMutableTreeNode("DateFormat"));
	node_1.add(new DefaultMutableTreeNode("Recordings"));
	node_1.add(new DefaultMutableTreeNode("Groups"));
	node_1.add(new DefaultMutableTreeNode("Chartting"));
	node_1.add(new DefaultMutableTreeNode("Reporting"));
	node_1.add(new DefaultMutableTreeNode("Advanced"));
add(node_1);
node_1 = new DefaultMutableTreeNode("Sources");
	node_2 = new DefaultMutableTreeNode("Method Servers");
		node_2.add(new DefaultMutableTreeNode("File Pattern"));
		node_2.add(new DefaultMutableTreeNode("Recordings"));
		node_2.add(new DefaultMutableTreeNode("Date Format"));
	node_1.add(node_2);
add(node_1);
add(new DefaultMutableTreeNode("Chart"));
add(new DefaultMutableTreeNode("Report"));

*/