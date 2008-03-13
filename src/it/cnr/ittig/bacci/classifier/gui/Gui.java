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
import java.awt.Dimension;
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
	private JRadioButton candidateRB;
	
	private JLabel ontoText;
	
	private JList resourceList;
	private JList classList;
	private JList linkedResourceList;
	private JList linkedClassList;

	//Selection list reference
	private JList selectedResourceList;
	private JList selectedClassList;
	
	private String LINKED_RES = "linked resources";
	private String LINKED_CLASS = "linked classes"; 
	
	public static Properties appProperties;

	public Gui() {
		
		super("Meta Classifier");
		
		dm = new DataManager();

		try {
			initProperties();
		} catch (FileNotFoundException e) {
			System.err.println(
				"Application properties file has to be initialized...");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Set last frame size
		String w = (String) appProperties.get("width");
		String h = (String) appProperties.get("height");		
		if(w != null && h != null) {
			setLocation(0,0);
			int ww = Integer.valueOf(w);
			int hh = Integer.valueOf(h);
			setPreferredSize(new Dimension(ww,hh));
		}
		
	    //Create and set up the window.
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    JPanel centralPanel = new JPanel(new BorderLayout());
	    getContentPane().add(centralPanel, BorderLayout.CENTER);
	    
	    JPanel panel = new JPanel(new GridLayout(1,3,10,10));
	    centralPanel.add(panel, BorderLayout.CENTER);
	    panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));	    
	    
	    panel.add(createResourcePanel());
	    
	    panel.add(createClassPanel());
	    
	    panel.add(createLinkedPanel());
	    
	    //centralPanel.add(createInfoPanel(), BorderLayout.SOUTH);
	    
	    panel = new JPanel(new GridLayout(1,7,30,30));
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
	    getContentPane().add(panel, BorderLayout.SOUTH);
	    
	    setupButton = new JButton("Setup");
	    setupButton.addActionListener(this);
	    panel.add(setupButton);

	    loadButton = new JButton("Load");
	    loadButton.addActionListener(this);
	    //loadButton.setEnabled(false);
	    panel.add(loadButton);

	    importButton = new JButton("Import DB");
	    importButton.addActionListener(this);
	    //importButton.setEnabled(false);
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
	    
	    panel.add(createFilterPanel(), BorderLayout.SOUTH);

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
		candidateRB = new JRadioButton("Candidate");
		candidateRB.setActionCommand("Candidate");
		candidateRB.addActionListener(this);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(allRB);
		bg.add(linkedRB);
		bg.add(unlinkedRB);
		bg.add(candidateRB);
		
		panel.add(allRB);
		panel.add(linkedRB);
		panel.add(unlinkedRB);
		panel.add(candidateRB);		
		
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

	    panel.add(createOntologyPanel(), BorderLayout.SOUTH);
	    
	    return panel;
	}

	private Component createOntologyPanel() {
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ontoText = new JLabel();
		ontoText.setSize(50, 25);
		String ontoStr = appProperties.getProperty("ontoText");		
		ontoText.setText(ontoStr);
		//ontoText.setPreferredSize(new Dimension(200,25));
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
		classPrevLabel = new JLabel(LINKED_CLASS);
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
		resourcePrevLabel = new JLabel(LINKED_RES);
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
			
			new SetupDialog(this);
//			if(setup()) {
//				dm = new DataManager();
//				Conf.DOMAIN_ONTO = ontoText.getText();
//				Conf.DOMAIN_ONTO_NS = ontoText.getText() + "#";
//				appProperties.setProperty("ontoText", 
//						ontoText.getText());
//				activateMainButtons();
//			}
		}
		
		if(e.getSource() == loadButton) {
			
			if(!checkPreferences()) {
				checkFailedMsg();
				return;
			}
			waitingState();
			if(!dm.init()) {					
			} else {
				refresh();
				activateEditButtons();
			}
			activeState();
		}

		if(e.getSource() == importButton) {
			if(!askConfirmation(
					"This will overwrite local data.\nContinue?",
					"Import from Db")) {
				return;
			}
			waitingState();
			if(!importFromDb()) {
				activeState();
				connectionFailedMsg();
			} else {
				activeState();
				showDoneMsg("Import from Db");
			}			
			System.exit(0);
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
			closeApp();
		}
		
		if(e.getSource() == cancelButton) {
			closeApp();
		}
		
		if(e.getSource() == allRB || 
				e.getSource() == linkedRB ||
				e.getSource() == candidateRB ||
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
		okButton.setEnabled(true);
	}
	
	private void refresh() {
		
		resourceList.setListData(dm.getResources().toArray());		
		classList.setListData(dm.getClasses().toArray());
		linkedResourceList.setListData(new Object[]{});
		linkedClassList.setListData(new Object[]{});
	}

	private void refresh(Collection resData, Collection classData, 
			Collection linkedClassData, Collection linkedResourceData) {
		
		refresh(resData, classData, linkedClassData, linkedResourceData,
				null, null);
	}
		
	private void refresh(Collection resData, Collection classData, 
			Collection linkedClassData, Collection linkedResourceData,
			BasicResource br, OntologicalClass oc) {
		
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
		
		refreshLabels(br, oc);
	}
	
	private void refreshLabels(BasicResource br, OntologicalClass oc) {
		
		resourceLabel.setText(
				" (" + resourceList.getModel().getSize() + ") ");			
		classLabel.setText(
				" (" + classList.getModel().getSize() + ")");

		if(br != null) {
			classPrevLabel.setText(linkedClassList.getModel().getSize() 
					+ " " + LINKED_CLASS + " for \"" + br + "\"");			
		} else {
			classPrevLabel.setText(LINKED_CLASS);
		}
		
		if(oc != null) {
			resourcePrevLabel.setText(linkedResourceList.getModel().getSize()
					+ " " + LINKED_RES + " for \"" + oc + "\"");
		} else {
			resourcePrevLabel.setText(LINKED_RES);	
		}		
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
			refresh(null, null, data, null, br, null);
		}
		
		if(values.length > 1) {
			refresh(null, null, new Vector(), null);
		}
	}
	
	private void classSelected(Object[] values) {
		
		if(values.length == 1) {
			OntologicalClass oc = (OntologicalClass) values[0];
			Collection<BasicResource> data = dm.getResources(oc);
			refresh(null, null, null, data, null, oc);
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
			refresh(null, null, dm.getClasses(br), null, br, null);
		}
		if(classValues.length == 1) {
			OntologicalClass oc = (OntologicalClass) classValues[0];
			refresh(null, null, null, dm.getResources(oc), null, oc);
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
			refresh(null, null, dm.getClasses(br), null, br, null);
		}
		if(classValues.length == 1) {
			OntologicalClass oc = (OntologicalClass) classValues[0];
			refresh(null, null, null, dm.getResources(oc), null, oc);
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
		if(type.equals("Candidate")) {
			refresh(dm.getCandidateResources(), null, null, null);
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
	    	//Conf.DATA_DIRECTORY = selectedDir.getAbsolutePath();
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
	
	private void checkFailedMsg() {
		JOptionPane.showMessageDialog(this, 
				"Preferences not set.", "Load",
				JOptionPane.WARNING_MESSAGE);
	}
	
	private boolean askConfirmation(String msg, String title) {
	
		Object returnVal = JOptionPane.showConfirmDialog(this, msg, title, 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.WARNING_MESSAGE);
		if(returnVal == null || ( returnVal instanceof Integer ) == false) {
			return false;
		}
		
		if( (Integer) returnVal == JOptionPane.OK_OPTION) {
			return true;
		}
		return false;
	}
	
	private boolean checkPreferences() {
		
		String value = (String) appProperties.get("ontoText");
		if(value == null || value.trim().length() < 1) {
			return false;
		}
		value = (String) appProperties.get("ontoNs");
		if(value == null || value.trim().length() < 1) {
			return false;
		}
		value = (String) appProperties.get("resDir");
		if(value == null || value.trim().length() < 1) {
			return false;
		}
		value = (String) appProperties.get("resNs");
		if(value == null || value.trim().length() < 1) {
			return false;
		}
		return true;		
	}
	
	private void initProperties() throws FileNotFoundException, IOException {
		
		//set up default properties
		Properties defProperties = new Properties();
//		defProperties.setProperty("ontoText", 
//			"http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl");
//		defProperties.setProperty("ontoNs", 
//			"http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl#");
		defProperties.setProperty("ontoText", 
			"");
		defProperties.setProperty("ontoNs", 
			"");
		defProperties.setProperty("resDir", 
			"");
		defProperties.setProperty("resNs", 
			"");
		
		//set up real properties
		appProperties = new Properties(defProperties);
		FileInputStream appStream = new FileInputStream("appProperties");
		appProperties.load(appStream);
		appStream.close();
		
	}
	
	private void closeApp() {
		
		try {
			saveProperties();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.exit(0);
	}
	
	private void saveProperties() throws IOException {
		
		//Save frame size
		Dimension dim = getSize();
		appProperties.setProperty("width", String.valueOf((int) dim.getWidth()));
		appProperties.setProperty("height", String.valueOf((int) dim.getHeight()));
		
		FileOutputStream fos = new FileOutputStream("appProperties");
		appProperties.store(fos, "---No Comment---");
		fos.close();
	}
}
