package it.cnr.ittig.jwneditor.editor.util;

import it.cnr.ittig.jwneditor.editor.EditorConf;

import javax.swing.JOptionPane;

public class UtilEditor {	
		
	/**
	 * Gestisce la stampa della stringa in base al livello
	 * di debug configurato per l'applicazione
	 * (qui usa il debug level).
	 * @param stringa di debug
	 */
	public static void debug(String debug) {
		debug(debug, 2);
	}
	
	/**
	 * Gestisce la stampa della stringa in base al livello
	 * di debug configurato per l'applicazione
	 * (qui usa l'info level).
	 * @param stringa info
	 */
	public static void info(String info) {
		debug(info, 1);
	}
	
	/**
	 * Gestisce la stampa della stringa in base al livello
	 * di debug configurato per l'applicazione
	 * (qui usa l'error level).
	 * @param stringa info
	 */
	public static void error(String error) {
		debug(error, 0);
	}
	
	/**
	 * Gestisce la stampa della stringa in base al livello
	 * di debug configurato per l'applicazione.
	 * Livelli:
	 * 0 error msg
	 * 1 info msg
	 * 2 debug msg
	 * @param debug La stringa da stampare
	 * @param level Il livello di debug per la stringa
	 */
	public static void debug(String debug, int level) {
		
		if(level == 0) {
			System.err.println(debug);
			JOptionPane.showMessageDialog(null, debug, "Have a look here...", 
											JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(EditorConf.DEBUG_LEVEL >= level) {
			System.out.println(debug);
		}
	}
}
