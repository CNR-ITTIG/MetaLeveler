package it.cnr.ittig.leveler;

import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.editor.util.UtilEditor;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn2owl.OWLManager;
import it.cnr.ittig.leveler.txt.TxtParser;
import it.cnr.ittig.leveler.xls.XlsParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Leveler {
	
	
	public static Map<String,Concetto> appSynsets = null;
	
	private static OWLManager manager = null;

	public static void main(String[] args) {
		
		appSynsets = new HashMap<String,Concetto>(2048, 0.70f);
		
		initAppDataDir();
		
		System.out.println("DATA_DIR: " + EditorConf.DATA_DIR);
		
		TxtParser parser = new TxtParser();
		XlsParser xParser = new XlsParser();
		
		try {
			
			System.out.println("Creating synsets...");
			parser.createSynsets();
			
			System.out.println("Adding ipo/iper...");
			parser.addIpo();
				
			System.out.println("Adding related...");
			parser.addRelated();
			
			System.out.println("Adding references...");
			parser.addRif();
			
			System.out.println("Adding ontologies classifications...");
			xParser.classify();
			//xParser.createMappingClass();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Adesso si salvano su DB i modelli popolati con gli oggetti synset
		manager = new OWLManager();
		
		owlProcessing("create");
		owlProcessing("reset");
		owlProcessing("add");
		
		//System.out.println("Serializing...");
		owlProcessing("write");
		
		System.out.println("Done.");
	}
		
	private static void owlProcessing(String function) {
		
		if(function.equals("create")) {
			manager.addModel(EditorConf.MODEL_URI, true);
		}
		
		if(function.equals("reset")) {
			manager.resetModel(EditorConf.MODEL_URI);
		}
		
		if(function.equals("add")) {
			manager.addIndividuals(EditorConf.MODEL_URI, appSynsets.values());
		}

		if(function.equals("write")) {			
			manager.addModel(EditorConf.onto_ind, true);
			manager.addModel(EditorConf.onto_indw, true);
//			manager.addModel(EditorConf.onto_ind_clo, true);
			manager.addModel(EditorConf.onto_ind_claw, true);
			manager.addModel(EditorConf.onto_concepts, true);
			manager.addModel(EditorConf.onto_types, true);
			manager.addModel(EditorConf.onto_sources, true);
			
			manager.writeModel(EditorConf.onto_ind,
					EditorConf.local_onto_ind);
			manager.writeModel(EditorConf.onto_indw,
					EditorConf.local_onto_indw);
//			manager.writeModel(EditorConf.onto_ind_clo,
//					EditorConf.local_onto_ind_clo);
			manager.writeModel(EditorConf.onto_ind_claw,
					EditorConf.local_onto_ind_claw);
			manager.writeModel(EditorConf.onto_concepts,
					EditorConf.local_onto_concepts);
			manager.writeModel(EditorConf.onto_types,
					EditorConf.local_onto_types);
			manager.writeModel(EditorConf.onto_sources,
					EditorConf.local_onto_sources);
		}
			
	}

	//  public static void main(String[] args) {
	//  //Schedule a job for the event-dispatching thread:
	//  //creating and showing this application's GUI.
	//  javax.swing.SwingUtilities.invokeLater(new Runnable() {
	//      public void run() {
	//          createAndShowGUI();
	//      }
	//  });
	//}
	
	private static void initAppDataDir() {
	
		//Il nome dovrebbe essere diverso secondo il s.o.
		//es. linux: .jwnEditor winXp: Application Data\jwnEditor ?
		File dataDir = null;
		//File homeDir = new File(EditorConf.HOME_DIR);
		
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.indexOf("windows") > -1) {
			//Windows environment
			File appData = new File(EditorConf.HOME_DIR + "/Application Data");
			if(appData.exists()) {
				dataDir = new File(EditorConf.HOME_DIR + "/Application Data/MetaLeveler");
			} else {
				//negli altri casi si prova a mettere tutto sotto c:\
				dataDir = new File("c:/MetaLevelerData");
			}
		} else {
			//Linux e Mac sempre sotto /home/utente/.xyz ?
			dataDir = new File(EditorConf.HOME_DIR + "/.MetaLeveler");
		}
		
		UtilEditor.debug("OS info:");
		UtilEditor.debug(System.getProperty("os.arch"));
		UtilEditor.debug(System.getProperty("os.name"));
		UtilEditor.debug(System.getProperty("os.version"));
		UtilEditor.debug(System.getProperty("sun.desktop"));
		UtilEditor.debug(System.getProperty("user.name"));
		UtilEditor.debug(System.getProperty("user.dir"));
		UtilEditor.debug(System.getProperty("user.home"));
		UtilEditor.debug(System.getProperty("user.language"));
		UtilEditor.debug(System.getProperty("user.country"));
		UtilEditor.info("Setting up application directory: " 
							+ dataDir.getPath() + "...");
		
		if(dataDir.exists()) {
			if(!dataDir.isDirectory()) {
				UtilEditor.error(dataDir.getPath() + " is not a directory!");
			}
		} else {
			//Se non esiste crea la directory
			if(!dataDir.mkdir()) {
				UtilEditor.error("Error creating directory " + 
									dataDir.getPath() + " !");
			}
		}
		
		//Aggiorna il valore della directory in EditorConf
		EditorConf.DATA_DIR = dataDir.getPath();
		//EditorConf.PREF_FILE = EditorConf.DATA_DIR + "/" + EditorConf.PREF;
		EditorConf.OWL_FILE = EditorConf.DATA_DIR + "/" + EditorConf.OWL;
		//EditorConf. = "file:" + EditorConf.OWL; // "file://" + EditorConf.OWL_FILE;
		EditorConf.MODEL_URI = EditorConf.onto_work;
		
		EditorConf.local_onto_ind = EditorConf.DATA_DIR + "/" + "individuals.owl";
		EditorConf.local_onto_indw = EditorConf.DATA_DIR + "/" + "individuals-word.owl";
//		EditorConf.local_onto_ind_clo = EditorConf.DATA_DIR + "/" + "ind-to-corelegal.owl";
		EditorConf.local_onto_ind_claw = EditorConf.DATA_DIR + "/" + "ind-to-consumer.owl";
		EditorConf.local_onto_work = EditorConf.DATA_DIR + "/" + "jurWordNet.owl";
		EditorConf.local_onto_concepts = EditorConf.DATA_DIR + "/" + "concepts.owl";
		EditorConf.local_onto_types = EditorConf.DATA_DIR + "/" + "types.owl";
		EditorConf.local_onto_sources = EditorConf.DATA_DIR + "/" + "sources.owl";
		
	//	EditorConf.PREFS_FILE = EditorConf.DATA_DIR + "/" + EditorConf.PREFS_FILE_NAME;
	//	//init preferences class
	//	File prFile = new File(EditorConf.PREFS_FILE);
	//	EditorPrefs prefsClass = null;
	//	if(prFile.exists()) {
	//		 prefsClass = new EditorPrefs(prFile);			
	//	} else {
	//		prefsClass = new EditorPrefs();
	//	}
	//	UtilEditor.prefs = prefsClass;
		
		//init external data
	//	File imgFile = new File(EditorConf.DATA_DIR + "/" + EditorConf.SYNSET_GENERIC_IMG);
	//	if(!imgFile.exists()) {		
	//		UtilEditor.downloadFile(EditorConf.JWS_IMG + EditorConf.SYNSET_GENERIC_IMG,
	//				EditorConf.DATA_DIR + "/" + EditorConf.SYNSET_GENERIC_IMG);
	//	}
	//	imgFile = new File(EditorConf.DATA_DIR + "/" + EditorConf.SYNSET_ONTO_IMG);
	//	if(!imgFile.exists()) {		
	//		UtilEditor.downloadFile(EditorConf.JWS_IMG + EditorConf.SYNSET_ONTO_IMG,
	//				EditorConf.DATA_DIR + "/" + EditorConf.SYNSET_ONTO_IMG);
	//	}
	}

}
