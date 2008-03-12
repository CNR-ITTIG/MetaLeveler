package it.cnr.ittig.bacci.classifier.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SetupDialog extends JDialog implements ActionListener {
	
	private JButton okButton;
	private JButton closeButton;
	
	private JButton changeDataDir;
	private JButton changeOnto;
	
	private JTextField resDirText;
	private JTextField resNsText;
	private JTextField ontoText;
	private JTextField ontoNsText;
	
	public SetupDialog(JFrame parent) {
		
		super(parent, true);

		setTitle("Setup");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocation(50,60);
		
		JPanel centralPanel = new JPanel(new GridLayout(2,1,5,5));
		getContentPane().add(centralPanel, BorderLayout.CENTER);
		
		//resources panel
		centralPanel.add(createResourcePanel());
		
		//ontology panel
		centralPanel.add(createOntologyPanel());		
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		okButton = new JButton("Ok");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		pack();
		setVisible(true);
	}

	private Component createResourcePanel() {
		
		JPanel panel = new JPanel(new GridLayout(2,1,0,0));
		panel.setBorder(BorderFactory.createTitledBorder(" Resources "));
		
		JPanel fpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("Data Dir: ");
		fpanel.add(label);
		
		resDirText = new JTextField();
		resDirText.setPreferredSize(new Dimension(400,25));
		String value = (String) Gui.appProperties.get("resDir");
		resDirText.setText(value);
		fpanel.add(resDirText);
		
		changeDataDir = new JButton("Change..");
		changeDataDir.addActionListener(this);
		fpanel.add(changeDataDir);
		
		panel.add(fpanel);
		
		fpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Namespace: ");
		fpanel.add(label);
		
		resNsText = new JTextField();
		resNsText.setPreferredSize(new Dimension(420,25));
		value = (String) Gui.appProperties.get("resNs");
		resNsText.setText(value);
		fpanel.add(resNsText);
		
		panel.add(fpanel);
		
		return panel;
	}

	private Component createOntologyPanel() {
		
		JPanel panel = new JPanel(new GridLayout(2,1,0,0));
		panel.setBorder(BorderFactory.createTitledBorder(" Ontology "));
		
		JPanel fpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("Location: ");
		fpanel.add(label);
		
		ontoText = new JTextField();
		ontoText.setPreferredSize(new Dimension(400,25));
		String value = (String) Gui.appProperties.get("ontoText");
		ontoText.setText(value);
		fpanel.add(ontoText);
		
		changeOnto = new JButton("Change..");
		changeOnto.addActionListener(this);
		fpanel.add(changeOnto);
		
		panel.add(fpanel);
		
		fpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Namespace: ");
		fpanel.add(label);
		
		ontoNsText = new JTextField();
		ontoNsText.setPreferredSize(new Dimension(420,25));
		value = (String) Gui.appProperties.get("ontoNs");
		ontoNsText.setText(value);
		fpanel.add(ontoNsText);
		
		panel.add(fpanel);
		
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == changeDataDir) {
			if(setupDataDir()) {
				
			}
		}

		if(e.getSource() == changeOnto) {
			
			if(setupOnto()) {
				
			}
		}
		
		if(e.getSource() == okButton) {
			
			//Set preferences
			Gui.appProperties.setProperty("ontoText", 
				ontoText.getText().trim());
			Gui.appProperties.setProperty("ontoNs", 
				ontoNsText.getText().trim());
			Gui.appProperties.setProperty("resDir", 
				resDirText.getText().trim());
			Gui.appProperties.setProperty("resNs", 
				resNsText.getText().trim());
			
			dispose();
		}

		if(e.getSource() == closeButton) {
			dispose();
		}
	}
	
	private boolean setupDataDir() {
		
		//Select a directory with a JFileChooser
		//(http://www.rgagnon.com/javadetails/java-0370.html)
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Input Directory");
	    chooser.setAcceptAllFileFilterUsed(false);
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	File selectedDir = chooser.getSelectedFile();
	    	String value = selectedDir.getAbsolutePath();
	    	resDirText.setText(value);
	    	if(resNsText.getText().trim().length() < 1) {
	    		resNsText.setText(value + File.separatorChar 
	    				+ "individuals.owl#");
	    	}
	    	return true;
	    }
	    return false;
	}
	
	private boolean setupOnto() {
		
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Ontology File");
	    chooser.setAcceptAllFileFilterUsed(false);
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	File selected = chooser.getSelectedFile();
	    	String value = selected.getAbsolutePath();
	    	ontoText.setText(value);
	    	if(ontoNsText.getText().trim().length() < 1) {
	    		ontoNsText.setText(value + "#");
	    	}	    	
	    	return true;
	    }
	    return false;
	}
	

}
