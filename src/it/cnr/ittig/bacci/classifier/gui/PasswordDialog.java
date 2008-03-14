package it.cnr.ittig.bacci.classifier.gui;

import it.cnr.ittig.bacci.util.Conf;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class PasswordDialog extends JDialog 
	implements ActionListener {
	
	private JButton okButton;
	
	private JTextField userText;
	
	private JPasswordField passText;

	public PasswordDialog(JFrame parent) {
		
		super(parent, true);
		
		setTitle("Import Db");
		setLocation(100,100);
		
		JPanel panel = new JPanel(new GridLayout(2,2,5,5));
		
		panel.add(new JLabel("Enter username: "));
		
		userText = new JTextField(12);
		panel.add(userText);
		
		panel.add(new JLabel("Enter password: "));
		
		passText = new JPasswordField(12);
		panel.add(passText);
						
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		okButton = new JButton("Ok");
		okButton.addActionListener(this);
		bottomPanel.add(okButton);
		
		getContentPane().add(panel, BorderLayout.CENTER);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		pack();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == okButton) {
			
			char[] input = passText.getPassword();
			String value = String.valueOf(input);
			Conf.dbPass = value.trim();
			Conf.dbUser = userText.getText().trim();
			dispose();
		}
	}

}
