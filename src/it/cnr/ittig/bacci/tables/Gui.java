package it.cnr.ittig.bacci.tables;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Gui extends JFrame implements ActionListener {

	public JTextField fileText;
	
	public JButton chooseButton;
	public JButton runButton;
	
	
	public Gui() {
	
		super("Table Reader");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(100,100);
		
		JPanel panel = new JPanel(new GridLayout(2,2,10,10));
		panel.setBorder(BorderFactory.createEtchedBorder());
		getContentPane().add(panel, BorderLayout.CENTER);
		
		fileText = new JTextField("");
		panel.add(fileText);
		
		chooseButton = new JButton("Scegli");
		chooseButton.addActionListener(this);
		panel.add(chooseButton);
		
		JLabel label = new JLabel("Selezionare file:");
		panel.add(label);
				
		runButton = new JButton("OK");
		runButton.addActionListener(this);
		panel.add(runButton);
	
		pack();
		setVisible(true);
	}
	
	
	public void actionPerformed(ActionEvent e) {
	
		if(e.getSource() == runButton) {
		
			String plainFileName = fileText.getText();
			if(plainFileName.length() < 1 ) {
				JOptionPane.showMessageDialog(this, "Selezionare un file valido !", "Reader",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			//Lancia il reader con file input e output
			File plainFile = new File(plainFileName);
			File xmlFile = new File(plainFile.getParent() + File.separatorChar + plainFile.getName() + ".xml");
			
			Reader reader = new Reader();
			boolean res = reader.run(plainFileName, xmlFile.getAbsolutePath());
			
			if( res == false) {				
				JOptionPane.showMessageDialog(this, "Error!", "Reader",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			JOptionPane.showMessageDialog(this, "Done.", "Reader",
					JOptionPane.INFORMATION_MESSAGE);
			
			System.exit(-1);
		}
		
		if(e.getSource() == chooseButton) {
			
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(this);
			
			if( returnVal == JFileChooser.APPROVE_OPTION ) {
				File file = fc.getSelectedFile();
				String mdbFileName = file.getAbsolutePath();
				fileText.setText(mdbFileName);
			}
			
		}
		
	}
}
