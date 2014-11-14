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
import logdruid.data.record.EventRecording;
import logdruid.data.record.Recording;
import logdruid.ui.DateSelector;
import logdruid.ui.NewRecordingList;
import logdruid.ui.NewRecordingList.MyTableModel2;
import logdruid.ui.table.EventRecordingEditorTable;
import logdruid.util.DataMiner;

import javax.swing.SwingConstants;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;

import org.apache.log4j.Logger;

import java.awt.Font;

public class EventRecordingEditor extends JPanel {
	private static Logger logger = Logger.getLogger(DataMiner.class.getName());
	private JPanel _this=this;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtName;
	private JTextField txtRegularExp;
	private JTextField txtDate;
	private EventRecordingEditorTable eventRecordingEditorTablePanel;
	private Repository repository;
	DefaultListModel listModel ;
	JTextPane examplePane= new JTextPane();
	private EventRecording recording;
	Document doc;
	JCheckBox chckbxActive;
//	private JList recordingList;
	
	/**
	 * Create the dialog.
	 */
	public EventRecordingEditor(MyTableModel2 myTableModel2, EventRecording re, Repository repo) {
		this(myTableModel2, repo,re.getExampleLine(),re.getRegexp(),re);
	}

	/**
	 * Create the dialog.
	 * @wbp.parser.constructor
	 */
	public EventRecordingEditor( final MyTableModel2 myTableModel2, Repository repo, String theLine,String regex, final EventRecording re) {
		//setBounds(0, 0, 1015, 467);
		
		repository=repo;
		recording=re;
	
		BorderLayout borderLayout = new BorderLayout();
		this.setLayout(borderLayout);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panelTop = new JPanel();
			contentPanel.add(panelTop, BorderLayout.NORTH);
			panelTop.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panelTop.add(panel_1, BorderLayout.NORTH);
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
								DateSelector dateDialog= new DateSelector(repository,txtDate, re);
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
					JCheckBox chckbxNewCheckBox = new JCheckBox("incident only");
					panel_1.add(chckbxNewCheckBox);
				}
				{
					JCheckBox chckbxMultiLine = new JCheckBox("multi line");
					panel_1.add(chckbxMultiLine);
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
				panelTop.add(panel_2, BorderLayout.SOUTH);
				{
					JButton btnAddButton = new JButton("Add");
					btnAddButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							eventRecordingEditorTablePanel.Add();
						}
					});
					btnAddButton.setHorizontalAlignment(SwingConstants.LEFT);
					panel_2.add(btnAddButton);
				}
				{
					JButton btnRemoveButton = new JButton("Remove");
					btnRemoveButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							eventRecordingEditorTablePanel.Remove();
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
							eventRecordingEditorTablePanel.FixValues();
							
						}
					});
					panel_2.add(btnCheck);
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panelTop.add(panel_1);
				
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					panel_1.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BorderLayout(0, 0));
					{
						JScrollPane scrollPane = new JScrollPane();
						panel.add(scrollPane);
						{
							examplePane = new JTextPane();
							examplePane.setText(theLine);
							scrollPane.setViewportView(examplePane);
						}
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
					eventRecordingEditorTablePanel = new EventRecordingEditorTable(examplePane);
					logger.info("RecordingEditor - re=null");
				}
				else {
				eventRecordingEditorTablePanel = new EventRecordingEditorTable(repo,re,examplePane);
				logger.info("RecordingEditor - re!=null");
				}
				eventRecordingEditorTablePanel.setBackground(UIManager.getColor("Panel.background"));
				panel2.add(eventRecordingEditorTablePanel);
				eventRecordingEditorTablePanel.setOpaque(true);
				eventRecordingEditorTablePanel.setVisible(true);
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
						Vector rIs=eventRecordingEditorTablePanel.getRecordingItems();
						if (recording==null){
							Recording r= new EventRecording(txtName.getText(),txtRegularExp.getText(),examplePane.getText(),txtDate.getText(),chckbxActive.isSelected() ,rIs );
							repository.addRecording(r);
							if (myTableModel2!=null) {
								logger.info("RecordingEditor - ok 1");
							myTableModel2.addRow(new Object[] { txtName.getText(),txtRegularExp.getText(), chckbxActive.isSelected() });
							}
						}
						else {
							((EventRecording)recording).update(txtName.getText(),txtRegularExp.getText(),examplePane.getText(),txtDate.getText(),chckbxActive.isSelected(),rIs);
							logger.info("RecordingEditor - ok 2");
								//myTableModel2.updateRow(NewRecordingList.table.getSelectedRow(),new Object[] { txtName.getText(),txtRegularExp.getText(),chckbxActive.isSelected() });
						//		myTableModel2.fireTableDataChanged();
						}

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
			txtDate.setText(((EventRecording)re).getDateFormat());
			examplePane.setText(re.getExampleLine());
		}
	}
	
public EventRecordingEditor(Repository repository2, String exampleLine, String regexp, EventRecording eventRecording) {
	this( null,  repository2, exampleLine, regexp, eventRecording);
	}

void refreshList(){
//	listModel.clear();
	// Enumeration<Recording> recordingEnum = repository.getRecordings().elements();
	   for (Enumeration<Recording> e = (Enumeration<Recording>)((Vector<Recording>)repository.getRecordings(EventRecording.class)).elements(); e.hasMoreElements();)
			{
		   Recording localRecording = e.nextElement();
		   listModel.addElement(localRecording.getName()+" | "+localRecording.getRegexp());
			}
	}
	
}
