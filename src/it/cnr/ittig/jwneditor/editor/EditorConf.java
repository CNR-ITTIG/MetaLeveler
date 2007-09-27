package it.cnr.ittig.jwneditor.editor;

public class EditorConf {

//	public final static String APP_NAME = "jwnEditor";
//	public final static String APP_VERSION = "1.6.6";
//	public final static String TITLE_VERSION = "1.6";
//	public final static String JWN_STRUCTURE_VERSION = "1.1";
//	
//	public final static String IMG_ROOT = "/data/img/";
//
	//Usando JWS user.dir puo' puntare un po' ovunque...
	public static String APP_DIR = System.getProperty("user.dir");
	
	//Anche user.home cambia abbastanza, ma ha più senso
	//(es.: WinXP, c:\ Doc and Set \ username)
	public static String HOME_DIR = System.getProperty("user.home");
//	
//	public static String PREF = ".jwnEditor.pref";
	public static String OWL = "jurWordNet.owl";
//
//	//Da configurare a runtime:
	public static String DATA_DIR;
	public static String PREFS_FILE;
	public static String OWL_FILE;
	public static String MODEL_URI;
//
//	public static String PREFS_FILE_NAME = "preferences.xml";
//	
	private static String JWS = "http://turing.ittig.cnr.it/jwn/";
	
	public static String JWS_URL = JWS + "editor/";
	public static String JWS_IMG = JWS + "editor/img/";
	
	//owl
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
//	public static String jwnModel = JWS + "ontologies/jurWordNet-model.owl";
//	public static String jwnImportModel = JWS + "ontologies/jurWordNet-model-imports.owl";
//	public static String jwnImportNoClassesModel = JWS + "ontologies/jurWordNet-model-imports-noclasses.owl";

	//public static String cloModel = "http://www.loa-cnr.it/ontologies/CLO/CoreLegal.owl";
	public static String clawModel = "http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl"; //merge?
		
	public static String onto_ns = "http://localhost/jwn/";
	public static String onto_ind = onto_ns + "individuals.owl";
	public static String onto_indw = onto_ns + "individuals-word.owl";
	//public static String onto_ind_clo = onto_ns + "ind-to-clo.owl";
	public static String onto_ind_claw = onto_ns + "ind-to-consumer.owl";
	public static String onto_work = onto_ns + "jurWordNet.owl";
	public static String onto_concepts = onto_ns + "concepts.owl";
	public static String onto_types = onto_ns + "types.owl";
	
	public static String local_onto_ind;
	public static String local_onto_indw;
//	public static String local_onto_ind_clo;
	public static String local_onto_ind_claw;
	public static String local_onto_work;
	public static String local_onto_concepts;
	public static String local_onto_types;
	
	//output level
	public final static int DEBUG_LEVEL = 2; //0 error msg, 1 info msg, 2 debug msg
	
	//immagini
	public final static String SYNSET_GENERIC_IMG = "kontact_journal.png";
	public final static String SYNSET_ONTO_IMG = "up.png";
	
	//Queste informazioni possono (devono!) essere prese dal database
	//nelle relative tabelle:
	private static String[] types = new String[]{"WNET", "ONTO", "ILI", "SPEC"};
	private static String[] parti = new String[]{"N", "AG", "V", "AV", "NP"};
	
	//Nomi relazioni significative
	public static String iperonimia = "has_hyperonym";
	public static String iponimia = "has_hyponym";
	public static String belongs = "belongs_to_class";
	public static String related = "fuzzynym";
	
//	//DATABASE RELATIONS
//	public static String tblConcetti = "concetti";
//	public static String tblLemmi = "lemmi";
//	public static String tblRelazioni = "relazioni";
//	public static String tblCorrelazioni = "correlazioni";
//	public static String tblFonti = "fonti";
//	public static String tblOntoclassi = "ontoclassi";
//	public static String tblCollegamenti = "collegamenti";
//	public static String tblRestrizioni = "restrizioni";
//	public static String tblTipi = "tipi_relazione";
//	public static String tblParti = "parti_discorso";
//	
//	//DATABASE FIELDS
//	public static String concettoID = "concetto_id";
//	public static String ontoclasseID = "ontoclasse_id";
//	public static String definizione = "definizione";
//	public static String dataIns = "data_inserimento";
//	public static String dataMod = "data_modifica";
//	public static String lemmaID = "lemma_id";
//	public static String pos = "parte_discorso";
//	public static String lemma = "lemma";
//	public static String sense = "numero_accezione";
//	public static String ordine = "numero_variante";
//	public static String correlazioneID = "correlazione_id";
//	public static String concetto1 = "concetto_id_1";
//	public static String concetto2 = "concetto_id_2";
//	public static String relazioneID = "relazione_id";
//	public static String relazione = "relazione";
//	public static String tipo = "tipo_relazione";
//	public static String reciproca = "relazione_reciproca_id";
//	public static String restrizioneID = "restrizione_id";
//	public static String pos1 = "parte_discorso_1";
//	public static String pos2 = "parte_discorso_2";
//	public static String fonteID = "fonte_id";
//	public static String fonte = "fonte";
//	public static String descrizionePos = "descrizione_parte";
//	public static String descrizioneTipo = "descrizione_tipo";
//	public static String ontoclasse = "ontoclasse";
//	public static String collegamentoID = "collegamento_id";
	
	public static String[] getTypes() {
		return types;
	}
	
	public static String[] getParti() {
		return parti;
	}
	
	public static int getTypesSize() {
		return types.length;
	}

	public static int getPartiSize() {
		return parti.length;
	}
	
}
