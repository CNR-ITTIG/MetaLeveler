package it.cnr.ittig.bacci.tables;

public class Main {

	private static String VERSION = "0.1";
	
	public Main() {
		
		System.out.println("Table Reader (" + VERSION + ")");
		System.out.println("Lorenzo Bacci (2009)");
		System.out.println("lorenzo.bacci@gmail.com");		
		
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
