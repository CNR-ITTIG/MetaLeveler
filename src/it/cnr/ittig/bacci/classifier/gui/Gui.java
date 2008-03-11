package it.cnr.ittig.bacci.classifier.gui;

import it.cnr.ittig.bacci.classifier.DataManager;
import it.cnr.ittig.bacci.classifier.resource.BasicResource;
import it.cnr.ittig.bacci.classifier.resource.OntologicalClass;
import it.cnr.ittig.bacci.database.DatabaseManager;
import it.cnr.ittig.bacci.database.query.ClassifierQuery;
import it.cnr.ittig.bacci.util.Conf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Gui extends JFrame 
	implements ActionListener, ListSelectionListener {
	
	private DataManager dm;
	
	private JButton setupButton;
	private JButton loadButton;
	private JButton importButton;
	private JButton addButton;
	private JButton removeButton;
	private JButton cancelButton;
	private JButton okButton;
	
	private JLabel resourceLabel;
	private JLabel classLabel;
	private JLabel resourcePrevLabel;
	private JLabel classPrevLabel;
	
	private JRadioButton allRB;
	private JRadioButton linkedRB;
	private JRadioButton unlinkedRB;
	
	private JTextField ontoText;
	
	private JList resourceList;
	private JList classList;
	private JList linkedResourceList;
	private JList linkedClassList;

	//Selection list reference
	private JList selectedResourceList;
	private JList selectedClassList;
	
	private Properties appProperties;

	public Gui() {
		
		super("Meta Classifier");
		
		try {
			initProperties();
		} catch (FileNotFoundException e) {
			System.err.println(
					"Application properties file has to be initialized...");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    //Create and set up the window.
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setLocation(50,50);
	    
//	    getContentPane().add(createLabelPanel(), 
//	    		BorderLayout.NORTH);
	    
	    JPanel centralPanel = new JPanel(new BorderLayout());
	    getContentPane().add(centralPanel, BorderLayout.CENTER);
	    
	    JPanel panel = new JPanel(new GridLayout(1,3,10,10));
	    centralPanel.add(panel, BorderLayout.CENTER);
	    panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));	    
	    
	    panel.add(createResourcePanel());
	    
	    panel.add(createClassPanel());
	    
	    panel.add(createLinkedPanel());
	    
	    centralPanel.add(createInfoPanel(), BorderLayout.SOUTH);
	    
	    panel = new JPanel(new GridLayout(1,5,30,30));
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
	    getContentPane().add(panel, BorderLayout.SOUTH);
	    
	    setupButton = new JButton("Setup");
	    setupButton.addActionListener(this);
	    panel.add(setupButton);

	    loadButton = new JButton("Load");
	    loadButton.addActionListener(this);
	    loadButton.setEnabled(false);
	    panel.add(loadButton);

	    importButton = new JButton("Import DB");
	    importButton.addActionListener(this);
	    importButton.setEnabled(false);
	    panel.add(importButton);

	    addButton = new JButton("Add");
	    addButton.addActionListener(this);
	    addButton.setEnabled(false);
	    panel.add(addButton);

	    removeButton = new JButton("Remove");
	    removeButton.addActionListener(this);
	    removeButton.setEnabled(false);
	    panel.add(removeButton);
	    
	    okButton = new JButton("Save");
	    okButton.addActionListener(this);
	    okButton.setEnabled(false);
	    panel.add(okButton);
	    
	    cancelButton = new JButton("Close");
	    cancelButton.addActionListener(this);
	    panel.add(cancelButton);
	    
	    selectedResourceList = resourceList;
	    selectedClassList = classList;
	    
		pack();
	    setVisible(true);    
	}
	
//	private Component createLabelPanel() {
//		
//		JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
//		
//		JPanel labPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		labPanel.add(new JLabel("Resources"));
//		panel.add(labPanel);
//		resourceLabel = new JLabel("");		
//		panel.add(resourceLabel);
//		
//		labPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		labPanel.add(new JLabel("Classes"));
//		panel.add(labPanel);
//		classLabel = new JLabel("");		
//		panel.add(classLabel);
//		
//		labPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		labPanel.add(new JLabel("Previews"));
//		panel.add(labPanel);
//		
//		return panel;
//	}
	
	private Component createInfoPanel() {
		
		JPanel panel = new JPanel(new GridLayout(1,3,10,10));
		
		panel.add(createFilterPanel());
		panel.add(createOntologyPanel());
		panel.add(new JLabel(""));
		
		return panel;
	}
	
	private Component createResourcePanel() {
		
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel labPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labPanel.add(new JLabel("Resources"));
		resourceLabel = new JLabel("");		
		labPanel.add(resourceLabel);
		panel.add(labPanel);
		panel.add(labPanel, BorderLayout.NORTH);
		
	    resourceList = new JList();
	    resourceList.addListSelectionListener(this);
	    JScrollPane scroll = new JScrollPane(resourceList);
	    panel.add(scroll, BorderLayout.CENTER);

	    return panel;
	}
	
	private Component createFilterPanel() {
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		allRB = new JRadioButton("All");
		allRB.setActionCommand("All");
		allRB.addActionListener(this);
		allRB.setSelected(true);
		linkedRB = new JRadioButton("Linked");
		linkedRB.setActionCommand("Linked");
		linkedRB.addActionListener(this);
		unlinkedRB = new JRadioButton("Unlinked");
		unlinkedRB.setActionCommand("Unlinked");
		unlinkedRB.addActionListener(this);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(allRB);
		bg.add(linkedRB);
		bg.add(unlinkedRB);
		
		panel.add(allRB);
		panel.add(linkedRB);
		panel.add(unlinkedRB);
		
		
		return panel;
	}
	
	private Component createClassPanel() {
		
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel labPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labPanel.add(new JLabel("Classes"));
		classLabel = new JLabel("");		
		labPanel.add(classLabel);
		panel.add(labPanel);
		panel.add(labPanel, BorderLayout.NORTH);

	    classList = new JList();
	    classList.addListSelectionListener(this);
	    JScrollPane scroll = new JScrollPane(classList);

	    panel.add(scroll, BorderLayout.CENTER);

	    return panel;
	}

	private Component createOntologyPanel() {
		
		JPanel panel = new JPanel(new FlowLayout());
				
		panel.add(new JLabel("Selected Ontology:"));
		ontoText = new JTextField();
		//TODO preferences?
		String ontoStr = appProperties.getProperty("ontoText");		
		ontoText.setText(ontoStr);
		panel.add(ontoText);
		
		return panel;
	}
	
	private Component createLinkedPanel() {
	
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel labPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labPanel.add(new JLabel("Previews"));
		panel.add(labPanel);
		panel.add(labPanel, BorderLayout.NORTH);

		JPanel gridPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		gridPanel.add(createLinkedClassPanel());
		gridPanel.add(createLinkedResourcePanel());
		panel.add(gridPanel, BorderLayout.CENTER);
		
	    return panel;
	}
	
	private Component createLinkedClassPanel() {
		
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		labelPanel.add(new JLabel("Linked classes"));
		classPrevLabel = new JLabel("");
		labelPanel.add(classPrevLabel);
		panel.add(labelPanel, BorderLayout.NORTH);
		
	    linkedClassList = new JList();
	    linkedClassList.addListSelectionListener(this);
	    JScrollPane scroll = new JScrollPane(linkedClassList);
	    panel.add(scroll, BorderLayout.CENTER);
	    
	    return panel;
	}
	
	private Component createLinkedResourcePanel() {
		
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		labelPanel.add(new JLabel("Linked resources"));
		resourcePrevLabel = new JLabel("");
		labelPanel.add(resourcePrevLabel);
		panel.add(labelPanel, BorderLayout.NORTH);
		
	    linkedResourceList = new JList();
	    linkedResourceList.addListSelectionListener(this);
	    JScrollPane scroll = new JScrollPane(linkedResourceList);	    
	    panel.add(scroll, BorderLayout.CENTER);
	    
	    return panel;
	}
	
	public void actionPerformed(ActionEvent e) {

		if(e.getSource() == setupButton) {
			//Setup data directory and init data
			if(setup()) {
				Conf.DOMAIN_ONTO = ontoText.getText();
				Conf.DOMAIN_ONTO_NS = ontoText.getText() + "#";
				appProperties.setProperty("ontoText", 
						ontoText.getText());
				activateMainButtons();
			}
		}
		
		if(e.getSource() == loadButton) {			
			waitingState();
			dm = new DataManager();
			if(!dm.init()) {					
			} else {
				refresh();
				activateEditButtons();
			}
			activeState();
		}

		if(e.getSource() == importButton) {
			if(!importFromDb()) {
				connectionFailedMsg();
			} else {
				showDoneMsg("Import from Db");
				System.exit(0);
			}
		}
		
		if(e.getSource() == addButton) {
			add();
		}

		if(e.getSource() == removeButton) {
			remove();
		}

		if(e.getSource() == okButton) {
			waitingState();
			dm.save();
			activeState();
			showDoneMsg("Save");
			try {
				saveProperties();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		}
		
		if(e.getSource() == cancelButton) {
			//Exit without saving changes
			System.exit(0);
		}
		
		if(e.getSource() == allRB || 
				e.getSource() == linkedRB ||
				e.getSource() == unlinkedRB ) {
			filter(e.getActionCommand());
		}
	}
	
	private void activateMainButtons() {
	
		loadButton.setEnabled(true);
		importButton.setEnabled(true);
		okButton.setEnabled(true);
	}
	
	private void activateEditButtons() {
		
		addButton.setEnabled(true);
		removeButton.setEnabled(true);
	}
	
	private void refresh() {
		
		resourceList.setListData(dm.getResources().toArray());		
		classList.setListData(dm.getClasses().toArray());
		linkedResourceList.setListData(new Object[]{});
		linkedClassList.setListData(new Object[]{});
	}

	private void refresh(Collection resData, Collection classData, 
			Collection linkedClassData, Collection linkedResourceData) {
		
		if(resData != null) {
			resourceList.setListData(resData.toArray());		
		}
		if(classData != null) {
			classList.setListData(classData.toArray());			
		}
		if(linkedClassData != null) {			
			linkedClassList.setListData(linkedClassData.toArray());
		}
		if(linkedResourceData != null) {			
			linkedResourceList.setListData(linkedResourceData.toArray());
		}
		
		refreshLabels();
	}
	
	private void refreshLabels() {
		
		resourceLabel.setText(" (" + resourceList.getModel().getSize() + ")");
		classLabel.setText(" (" + classList.getModel().getSize() + ")");
		resourcePrevLabel.setText(" (" + linkedResourceList.getModel().getSize() + ")");
		classPrevLabel.setText(" (" + linkedClassList.getModel().getSize() + ")");
	}

	public void valueChanged(ListSelectionEvent e) {
		
		if(e.getValueIsAdjusting()) {
			return;
		}
		
		if(e.getSource() == resourceList ||
				e.getSource() == linkedResourceList ) {
			
			selectedResourceList = (JList) e.getSource();					
			resourceSelected(selectedResourceList.getSelectedValues());
		}
		
		if(e.getSource() == classList ||
				e.getSource() == linkedClassList) {

			selectedClassList = (JList) e.getSource();					
			classSelected(selectedClassList.getSelectedValues());
		}	
	}
	
	private void resourceSelected(Object[] values) {
		
		if(values.length == 1) {
			BasicResource br = (BasicResource) values[0];
			Collection<OntologicalClass> data = dm.getClasses(br);
			refresh(null, null, data, null);
		}
		
		if(values.length > 1) {
			refresh(null, null, new Vector(), null);
		}
	}
	
	private void classSelected(Object[] values) {
		
		if(values.length == 1) {
			OntologicalClass oc = (OntologicalClass) values[0];
			Collection<BasicResource> data = dm.getResources(oc);
			refresh(null, null, null, data);
		}
		if(values.length > 1) {
			refresh(null, null, null, new Vector());
		}
	}
	
	private void add() {
		Object[] resValues = selectedResourceList.getSelectedValues();
		Object[] classValues = selectedClassList.getSelectedValues();
		
		if(resValues.length < 1 || classValues.length < 1) {
			return;
		}
		
		for(int i = 0; i < resValues.length; i++) {
			BasicResource br = (BasicResource) resValues[i];
			for(int k = 0; k < classValues.length; k++) {
				OntologicalClass oc = (OntologicalClass) classValues[k];
				dm.addClass(br, oc);
			}
		}
		
		//Show differences...
		if(resValues.length == 1) {
			BasicResource br = (BasicResource) resValues[0];
			refresh(null, null, dm.getClasses(br), null);
		}
		if(classValues.length == 1) {
			OntologicalClass oc = (OntologicalClass) classValues[0];
			refresh(null, null, null, dm.getResources(oc));
		}
	}
	
	private void remove() {
		Object[] resValues = selectedResourceList.getSelectedValues();
		Object[] classValues = selectedClassList.getSelectedValues();
		
		if(resValues.length < 1 || classValues.length < 1) {
			return;
		}
		
		for(int i = 0; i < resValues.length; i++) {
			BasicResource br = (BasicResource) resValues[i];
			for(int k = 0; k < classValues.length; k++) {
				OntologicalClass oc = (OntologicalClass) classValues[k];
				dm.removeClass(br, oc);
			}
		}
		
		//Show differences...
		if(resValues.length == 1) {
			BasicResource br = (BasicResource) resValues[0];
			refresh(null, null, dm.getClasses(br), null);
		}
		if(classValues.length == 1) {
			OntologicalClass oc = (OntologicalClass) classValues[0];
			refresh(null, null, null, dm.getResources(oc));
		}
	}
	
	private void filter(String type) {
		
		if(type.equals("All")) {
			refresh(dm.getResources(), null, null, null);
		}
		if(type.equals("Linked")) {
			refresh(dm.getLinkedResources(), null, null, null);
		}
		if(type.equals("Unlinked")) {
			refresh(dm.getUnlinkedResources(), null, null, null);
		}
	}
	
	private boolean setup() {
		
		//Select a directory with a JFileChooser
		//(http://www.rgagnon.com/javadetails/java-0370.html)
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Input Directory");
	    chooser.setAcceptAllFileFilterUsed(false);
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	File selectedDir = chooser.getSelectedFile();
	    	Conf.DATA_DIRECTORY = selectedDir.getAbsolutePath();
	    	return true;
	    }
	    return false;
	}
	
	private boolean importFromDb() {
		
		DatabaseManager dbm = new DatabaseManager();
		dbm.addQueryResolver(new ClassifierQuery());
		String[] params = new String[]{
				Conf.dbUser,
				Conf.dbPass,
				Conf.dbName,
				Conf.dbAddress,
				Conf.dbType
		};
		if(dbm.initDatabase(params)) {
			dm.processDb(dbm);
			return true;
		}
		
		return false;
	}
	
	private void waitingState() {
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	}
	
	private void activeState() {
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	private void showDoneMsg(String title) {
		
		JOptionPane.showMessageDialog(this, "Done.", title,
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void connectionFailedMsg() {
		JOptionPane.showMessageDialog(this, 
				"Database not found", "DB Connection",
				JOptionPane.WARNING_MESSAGE);
	}
	
	private void initProperties() throws FileNotFoundException, IOException {
		
		//set up default properties
		Properties defProperties = new Properties();
		defProperties.setProperty("ontoText", 
			"http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl");
		
		//set up real properties
		appProperties = new Properties(defProperties);
		FileInputStream appStream = new FileInputStream("appProperties");
		appProperties.load(appStream);
		appStream.close();
		
	}
	
	private void saveProperties() throws IOException {
		
		FileOutputStream fos = new FileOutputStream("appProperties");
		appProperties.store(fos, "---No Comment---");
		fos.close();
	}
	
}
