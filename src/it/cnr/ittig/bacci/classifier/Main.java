package it.cnr.ittig.bacci.classifier;

import it.cnr.ittig.bacci.classifier.gui.Gui;

public class Main {
	
	public Main() {
		
		new Gui();
	}

	/**
     * Create the GUI and show it. For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        
        //Create and set up the window.
        new Main();
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
