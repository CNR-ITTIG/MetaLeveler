package it.cnr.ittig.bacci.classifier.gui;

import it.cnr.ittig.bacci.classifier.DataManager;
import it.cnr.ittig.bacci.classifier.resource.BasicResource;
import it.cnr.ittig.bacci.classifier.resource.OntologicalClass;
import it.cnr.ittig.bacci.util.Conf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Gui extends JFrame 
	implements ActionListener, ListSelectionListener {
	
	private DataManager dm;
	
	private JButton setupButton;

	private JButton addButton;
	private JButton removeButton;
	private JButton cancelButton;
	private JButton okButton;
	
	private JRadioButton allRB;
	private JRadioButton linkedRB;
	private JRadioButton unlinkedRB;
	
	private JList resourceList;
	private JList classList;
	private JList linkedResourceList;
	private JList linkedClassList;

	//Selection list reference
	private JList selectedResourceList;
	private JList selectedClassList;

	public Gui() {
		
		super("Meta Classifier");
		
	    //Create and set up the window.
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setLocation(50,50);
	    
	    getContentPane().add(createLabelPanel(), 
	    		BorderLayout.NORTH);
	    
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

	    addButton = new JButton("Add");
	    addButton.addActionListener(this);
	    panel.add(addButton);

	    removeButton = new JButton("Remove");
	    removeButton.addActionListener(this);
	    panel.add(removeButton);
	    
	    okButton = new JButton("Ok");
	    okButton.addActionListener(this);
	    panel.add(okButton);
	    
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(this);
	    panel.add(cancelButton);
	    
	    selectedResourceList = resourceList;
	    selectedClassList = classList;
	    
		pack();
	    setVisible(true);    
	}
	
	private Component createLabelPanel() {
		
		JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
		
		JPanel labPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labPanel.add(new JLabel("Resources"));
		panel.add(labPanel);
		
		labPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labPanel.add(new JLabel("Classes"));
		panel.add(labPanel);
		
		labPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labPanel.add(new JLabel("Previews"));
		panel.add(labPanel);
		
		return panel;
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
		
	    classList = new JList();
	    classList.addListSelectionListener(this);
	    JScrollPane scroll = new JScrollPane(classList);

	    panel.add(scroll, BorderLayout.CENTER);

	    return panel;
	}

	private Component createOntologyPanel() {
		
		JPanel panel = new JPanel(new FlowLayout());
				
		panel.add(new JLabel("Selected Ontology:"));
		JTextField ontoText = new JTextField();
		ontoText.setText(Conf.DOMAIN_ONTO);
		panel.add(ontoText);
		
		return panel;
	}
	
	private Component createLinkedPanel() {
		
		JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
		
		panel.add(createLinkedClassPanel());
		panel.add(createLinkedResourcePanel());
		
	    return panel;
	}
	
	private Component createLinkedClassPanel() {
		
	    linkedClassList = new JList();
	    linkedClassList.addListSelectionListener(this);
	    JScrollPane scroll = new JScrollPane(linkedClassList);	    
	    
	    return scroll;
	}
	
	private Component createLinkedResourcePanel() {
		
	    linkedResourceList = new JList();
	    linkedResourceList.addListSelectionListener(this);
	    JScrollPane scroll = new JScrollPane(linkedResourceList);	    
	    
	    return scroll;
	}
	
	public void actionPerformed(ActionEvent e) {

		if(e.getSource() == setupButton) {			
			//TODO Setup data directory
			
			dm = new DataManager();
			refresh();
		}

		if(e.getSource() == addButton) {
			add();
		}

		if(e.getSource() == removeButton) {
			remove();
		}

		if(e.getSource() == okButton) {
			dm.save();
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
			if(data.size() > 0) {
				refresh(null, null, data, null);
			} else {
				Vector<String> noData = new Vector<String>();
				noData.add("(No linked classes!)");
				refresh(null, null, noData, null);
			}
		}
		
		if(values.length > 1) {
			refresh(null, null, new Vector(), null);
		}
	}
	
	private void classSelected(Object[] values) {
		
		if(values.length == 1) {
			OntologicalClass oc = (OntologicalClass) values[0];
			Collection<BasicResource> data = dm.getResources(oc);
			if(data.size() > 0) {
				refresh(null, null, null, data);
			} else {
				Vector<String> noData = new Vector<String>();
				noData.add("(No linked resources!)");
				refresh(null, null, null, noData);
			}
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
}