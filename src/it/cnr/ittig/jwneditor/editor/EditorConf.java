package it.cnr.ittig.jwneditor.editor;

public class EditorConf {

	public static String APP_DIR = System.getProperty("user.dir");
	
	public static String HOME_DIR = System.getProperty("user.home");

//	//Da configurare a runtime:
	public static String DATA_DIR;
	public static String PREFS_FILE;
	public static String OWL_FILE;
	public static String MODEL_URI;

	public static String LANGUAGE = "IT";

	private static String JWS = "http://turing.ittig.cnr.it/jwn/";
	
	public static String JWS_URL = JWS + "editor/";
	public static String JWS_IMG = JWS + "editor/img/";
	
	//DATABASE RDF:
//	public final static String OWL_ADDRESS = "jdbc:mysql://172.16.0.12/jena";  //eulero
//	public final static String OWL_USERNAME = "juris";
//	public final static String OWL_PASSWORD = ".juris.";
	public final static String OWL_ADDRESS = "jdbc:mysql://127.0.0.1/jena";
	public final static String OWL_USERNAME = "root";
	public final static String OWL_PASSWORD = "";
	public final static String OWL_TYPE = "MySQL";
	public final static String OWL_DRIVER = "com.mysql.jdbc.Driver";

	public static boolean addClassesHierarchy = true;
	
	public static String ownSchema = JWS + "ontologies/owns.owl";
	public static String langSchema = JWS + "ontologies/language-properties-full.owl";
	public static String sourceSchema = JWS + "ontologies/metasources.owl";

	public static String clawModel = "http://turing.ittig.cnr.it/jwn/ontologies/consumer-law-merge.owl"; //merge?
	public static String clawModelNs = "http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl#";
		
	public static String dalos_ns = "http://localhost/dalos/";
	public static String onto_ns =  dalos_ns + LANGUAGE + "/";
	
	public static String onto_ind = onto_ns + "individuals.owl";
	public static String onto_indw = onto_ns + "individuals-word.owl";
	public static String onto_ind_claw = onto_ns + "ind-to-consumer.owl";
	public static String onto_work = onto_ns + "work.owl";
	public static String onto_types = onto_ns + "types.owl";
	public static String onto_sources = onto_ns + "sources.owl";
	public static String onto_concepts = dalos_ns + "concepts.owl";
	
	public static String local_onto_ind;
	public static String local_onto_indw;
	public static String local_onto_ind_claw;
	public static String local_onto_work;
	public static String local_onto_concepts;
	public static String local_onto_types;
	public static String local_onto_sources;
	
	//output level
	public final static int DEBUG_LEVEL = 2; //0 error msg, 1 info msg, 2 debug msg
	
	//immagini
	public final static String SYNSET_GENERIC_IMG = "kontact_journal.png";
	public final static String SYNSET_ONTO_IMG = "up.png";
	
	//Nomi relazioni significative
	public static String iperonimia = "has_hyperonym";
	public static String iponimia = "has_hyponym";
	public static String belongs = "belongs_to_class";
	public static String related = "fuzzynym";
	
}
