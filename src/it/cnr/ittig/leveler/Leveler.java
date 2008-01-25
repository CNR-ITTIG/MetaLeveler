package it.cnr.ittig.leveler;

import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.editor.util.UtilEditor;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn2owl.OWLManager;
import it.cnr.ittig.leveler.importer.CeliOdbcImporter;
import it.cnr.ittig.leveler.importer.CeliTablesImporter;
import it.cnr.ittig.leveler.importer.ILCTxtImporter;
import it.cnr.ittig.leveler.importer.MetaImporter;
import it.cnr.ittig.leveler.mapper.XlsMapper;

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
		
		MetaImporter parser = null;
		if(EditorConf.TYPE_INPUT.equalsIgnoreCase("txt")) {
			parser = new ILCTxtImporter();
		} else if(EditorConf.TYPE_INPUT.equalsIgnoreCase("xls")) {
			parser = new CeliTablesImporter();
		} else if(EditorConf.TYPE_INPUT.equalsIgnoreCase("mdb")) {
			parser = new CeliOdbcImporter();
		} else {
			System.err.println("Unknown input type.");
			return;
		}
		
		XlsMapper xParser = new XlsMapper();
		
		try {
			
			System.out.println("Creating synsets...");
			parser.createSynsets();
			
			System.out.println("Adding ipo/iper...");
			parser.addIpo();
				
			System.out.println("Adding related...");
			parser.addRelated();
			
			System.out.println("Adding references...");
			parser.addRif();
			
			System.out.println("Adding alignment...");
			parser.addAlignment();

//			xParser.fill();
//			return;
			
			if(EditorConf.LINK_TO_ONTOLOGY) {
				System.out.println("Adding ontologies classifications...");
				xParser.classify();
			}

			//xParser.addDefinitions(); //Le def. vengono aggiunte in un secondo momento!
			
			//xParser.createMappingClass();
			//return;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Adesso si salvano su DB i modelli popolati con gli oggetti synset
		long t1 = System.currentTimeMillis();
		manager = new OWLManager();
		
		owlProcessing("create");
		owlProcessing("reset");
		owlProcessing("add");
		
		//System.out.println("Serializing...");
		owlProcessing("write");
		
		long t2 = System.currentTimeMillis();
		long t3 = (t2 - t1) / 1000;
		System.out.println("OWL processing done in " + t3 + "s.");
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
					EditorConf.local_onto_ind, EditorConf.onto_ind);
			manager.writeModel(EditorConf.onto_indw,
					EditorConf.local_onto_indw, EditorConf.onto_indw);
			manager.writeModel(EditorConf.onto_ind_claw,
					EditorConf.local_onto_ind_claw, EditorConf.onto_ind_claw);
			manager.writeModel(EditorConf.onto_concepts,
					EditorConf.local_onto_concepts, EditorConf.onto_concepts);
			manager.writeModel(EditorConf.onto_types,
					EditorConf.local_onto_types, EditorConf.onto_types);
			manager.writeModel(EditorConf.onto_sources,
					EditorConf.local_onto_sources, EditorConf.onto_sources);
		}			
	}

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
		EditorConf.MODEL_URI = EditorConf.onto_work;
		
		EditorConf.local_onto_ind = EditorConf.DATA_DIR 
			+ File.separatorChar + "individuals.owl";
		EditorConf.local_onto_indw = EditorConf.DATA_DIR 
			+ File.separatorChar + "individuals-word.owl";
		EditorConf.local_onto_ind_claw = EditorConf.DATA_DIR 
			+ File.separatorChar + "ind-to-consumer.owl";
		EditorConf.local_onto_work = EditorConf.DATA_DIR 
			+ File.separatorChar + "jurWordNet.owl";
		EditorConf.local_onto_concepts = EditorConf.DATA_DIR 
			+ File.separatorChar + "concepts.owl";
		EditorConf.local_onto_types = EditorConf.DATA_DIR 
			+ File.separatorChar + "types.owl";
		EditorConf.local_onto_sources = EditorConf.DATA_DIR 
			+ File.separatorChar + "sources.owl";		
	}

}
