package logdruid.ui.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Label;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JTextPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

import java.awt.Color;

import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import logdruid.data.Repository;
//import logdruid.newui.MetadataSelectorPanel.MyTableModel2;

import logdruid.data.record.MetadataRecording;
import logdruid.data.record.Recording;
import logdruid.ui.DateSelector;
import logdruid.ui.NewRecordingList;
import logdruid.ui.NewRecordingList.MyTableModel2;
import logdruid.ui.table.StatRecordingEditorTable;
import logdruid.util.DataMiner;

import javax.swing.SwingConstants;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;

import org.apache.log4j.Logger;

import java.awt.Font;

public class MetadataRecordingEditor extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private JPanel _this=this;
	private JPanel panel_1;
	private final JPanel contentPanel = new JPanel();
	private JScrollPane scrollPane;
	private JTextField txtName;
	private JTextField txtRegularExp;
	private JTextField txtDate;
	private StatRecordingEditorTable recordingEditorTablePanel;
	private Repository repository;
	DefaultListModel listModel ;
	JTextPane examplePane= new JTextPane();
	private MetadataRecording recording;
	Document doc;
	JCheckBox chckbxActive;
//	private JList recordingList;
	
	/**
	 * Create the dialog.
	 */
	public MetadataRecordingEditor(logdruid.ui.NewRecordingList.MyTableModel2 myTableModel2, MetadataRecording re, Repository repo) {
		this(myTableModel2, repo,re.getExampleLine(),re.getRegexp(),re);
	}

	public MetadataRecordingEditor(Repository repo, String theLine,String regex, MetadataRecording re) {
		this( null,  repo, theLine, regex, re);
	}
	
	/**
	 * Create the dialog.
	 * @wbp.parser.constructor
	 */
	public MetadataRecordingEditor( final logdruid.ui.NewRecordingList.MyTableModel2 myTableModel2, Repository repo, String theLine,String regex, final MetadataRecording re) {
		//setBounds(0, 0, 1015, 467);
		
		repository=repo;
		recording=re;
	
		BorderLayout borderLayout = new BorderLayout();
		this.setLayout(borderLayout);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.NORTH);
				panel_1.setLayout(new GridLayout(0, 3, 0, 0));
				{
					txtName = new JTextField();
					panel_1.add(txtName);
					txtName.setText("name");
					txtName.setColumns(10);
				}
				{
					txtRegularExp = new JTextField();
					txtRegularExp.addCaretListener(new CaretListener() {
						public void caretUpdate(CaretEvent e) {
							doc = examplePane.getDocument();
							
							  Pattern pattern = Pattern.compile(txtRegularExp.getText());				  
							    Matcher matcher = pattern.matcher(examplePane.getText());
							    Highlighter h = examplePane.getHighlighter();
							    h.removeAllHighlights();
							    if (matcher.find()){
							  //  	int currIndex=doc.getLength();
							//		doc.insertString(doc.getLength(),line+"\n", null);
									
									try {
										h.addHighlight(matcher.start(),matcher.end(), DefaultHighlighter.DefaultPainter);
									} catch (BadLocationException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
							    	
						}}
					});
					panel_1.add(txtRegularExp);
					txtRegularExp.setText(regex);
					txtRegularExp.setColumns(10);
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
						txtDate = new JTextField();
						txtDate.setEditable(false);
						panel_2.add(txtDate, BorderLayout.CENTER);
						txtDate.setText("date format");
						txtDate.setColumns(10);
					}
					{
						JButton button = new JButton("...");
						button.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								DateSelector dateDialog= new DateSelector(repository,txtDate,re);
								dateDialog.validate();
								dateDialog.setResizable(true);
								dateDialog.setModal(false);
								dateDialog.setVisible(true);
							}
						});
						button.setFont(new Font("Dialog", Font.BOLD, 6));
						panel_2.add(button, BorderLayout.EAST);
					}
				}
				{
					chckbxActive = new JCheckBox("active");
					chckbxActive.setSelected(true);
					panel_1.add(chckbxActive);
				}
			}
			{
				JPanel panel_2 = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
				flowLayout.setAlignment(FlowLayout.LEFT);
				panel.add(panel_2, BorderLayout.SOUTH);
				{
					JButton btnAddButton = new JButton("Add");
					btnAddButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							recordingEditorTablePanel.Add();
						}
					});
					btnAddButton.setHorizontalAlignment(SwingConstants.LEFT);
					panel_2.add(btnAddButton);
				}
				{
					JButton btnRemoveButton = new JButton("Remove");
					btnRemoveButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							recordingEditorTablePanel.Remove();
						}
					});
					btnRemoveButton.setHorizontalAlignment(SwingConstants.LEFT);
					panel_2.add(btnRemoveButton);
				}
				{
					JButton btnCheck = new JButton("Check");
					btnCheck.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							logger.info("check");
							recordingEditorTablePanel.FixValues();
							
						}
					});
					panel_2.add(btnCheck);
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					scrollPane = new JScrollPane();
					panel_1.add(scrollPane);
					{
						examplePane = new JTextPane();
						examplePane.setText(theLine);
						scrollPane.setViewportView(examplePane);
					}
				}
			}
		}
		{
			{
				JPanel panel2 = new JPanel();
				contentPanel.add(panel2);
				panel2.setLayout(new BorderLayout(0, 0));
				if (re==null){
					recordingEditorTablePanel = new StatRecordingEditorTable(examplePane);
					logger.info("RecordingEditor - re=null");
				}
				else {
				recordingEditorTablePanel = new StatRecordingEditorTable(repo,re,examplePane);
				logger.info("RecordingEditor - re!=null");
				}
				recordingEditorTablePanel.setBackground(UIManager.getColor("Panel.background"));
				panel2.add(recordingEditorTablePanel);
				recordingEditorTablePanel.setOpaque(true);
				recordingEditorTablePanel.setVisible(true);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			this.add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						Vector rIs=recordingEditorTablePanel.getRecordingItems();
						if (recording==null){
							Recording r= new MetadataRecording(txtName.getText(),txtRegularExp.getText(),examplePane.getText(),txtDate.getText(),chckbxActive.isSelected() ,rIs );
							repository.addRecording(r);
							if (myTableModel2!=null) {
								logger.info("RecordingEditor - ok 1");
							myTableModel2.addRow(new Object[] { txtName.getText(),txtRegularExp.getText(), chckbxActive.isSelected() });
							}
						}
						else {
							((MetadataRecording)recording).update(txtName.getText(),txtRegularExp.getText(),examplePane.getText(),txtDate.getText(),chckbxActive.isSelected(),rIs);
							logger.info("RecordingEditor - ok 2");
							if (myTableModel2!=null){
								myTableModel2.updateRow(NewRecordingList.table.getSelectedRow(),
									new Object[] { txtName.getText(),
								txtRegularExp.getText(),
								chckbxActive.isSelected() });
							//	myTableModel2.fireTableDataChanged();
						}}

						if (contentPanel.getParent().getParent().getParent().getParent().getClass().equals(JDialog.class)){
							((JDialog)contentPanel.getParent().getParent().getParent().getParent()).dispose();	
						}
			//			
					    }
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
		//		getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if (contentPanel.getParent().getParent().getParent().getParent().getClass().equals(JDialog.class)){
							((JDialog)contentPanel.getParent().getParent().getParent().getParent()).dispose();	
						}
					    }
				});
				buttonPane.add(cancelButton);
			}
		}
		if (re!=null) {
			txtName.setText(re.getName());
			txtRegularExp.setText(re.getRegexp());
			txtDate.setText(((MetadataRecording)re).getDateFormat());
			examplePane.setText(re.getExampleLine());
			scrollPane.repaint();
			panel_1.repaint();
			
		}
	}
	
void refreshList(){
//	listModel.clear();
	// Enumeration<Recording> recordingEnum = repository.getRecordings().elements();
	   for (Enumeration<Recording> e = (Enumeration<Recording>)((Vector<Recording>)repository.getRecordings(MetadataRecording.class)).elements(); e.hasMoreElements();)
			{
		   Recording localRecording = e.nextElement();
		   listModel.addElement(localRecording.getName()+" | "+localRecording.getRegexp());
			}
	}
	
}
