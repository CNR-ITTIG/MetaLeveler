package it.cnr.ittig.bacci.classifier.gui;

import it.cnr.ittig.bacci.util.Conf;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.TextField;
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
	
	private JPasswordField passText;

	public PasswordDialog(JFrame parent) {
		
		super(parent, true);
		
		setTitle("Import Db");
		setLocation(100,100);
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		panel.add(new JLabel("Enter password: "));
		
		passText = new JPasswordField(12);
		panel.add(passText);
				
		okButton = new JButton("Ok");
		okButton.addActionListener(this);
		panel.add(okButton);
		
		getContentPane().add(panel);
		
		pack();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == okButton) {
			
			char[] input = passText.getPassword();
			String value = String.valueOf(input);
			Conf.dbPass = value.trim();
			dispose();
		}
	}

}
