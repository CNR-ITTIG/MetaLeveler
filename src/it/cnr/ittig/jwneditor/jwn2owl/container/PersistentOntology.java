package it.cnr.ittig.jwneditor.jwn2owl.container;

import it.cnr.ittig.jwneditor.editor.EditorConf;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

public class PersistentOntology extends AbstractOntology {
	
	//String nsSchema = EditorConf.ownSchema + "#"; //schema namespace
	//String namespace = ""; //"file://" + EditorConf.OWL_FILE + "#";
	//String namespace = EditorConf.MODEL_URI + "#";
	String namespace = EditorConf.onto_work + "#";
	
	//import or non-import model
	private String loadModel = "";
	
	//specify core-legal ontology:
	//private String cloModel = EditorConf.cloModel;

	//Const default connection parameters
	private final static String ADDRESS = EditorConf.OWL_ADDRESS;
	private final static String USERNAME = EditorConf.OWL_USERNAME;
	private final static String PASSWORD = EditorConf.OWL_PASSWORD;
	private final static String TYPE = EditorConf.OWL_TYPE;
	private final static String DRIVER = EditorConf.OWL_DRIVER;

	//Actual connection parameters
	private String dbURL;
	private String dbUser;	
	private String dbPw;
	private String dbType;
	private String dbDriver;
	
	/*
	 * Load db drivers, initialize ModelMaker and load schema ontology.
	 */
	public PersistentOntology(String mName) {
	
		//Use default parameters:
		this(mName, ADDRESS, USERNAME, PASSWORD, TYPE, DRIVER);
	}
	
	public PersistentOntology(String mName, String URL, String User, String Pw, 
								String Type, String driver) {
		
		//setup initializing model
		//if( EditorConf.addClassesHierarchy) {
			//loadModel = EditorConf.jwnImportModel;
//		} else {
//			loadModel = EditorConf.jwnImportNoClassesModel;
//		}
		
		//Set-up connection parameters
		setup(URL, User, Pw, Type, driver);
		
		modelName = mName;
		
		try {
			Class.forName(dbDriver);
		} catch(Exception e) {
			System.err.println("Failed to load the driver for the database: " +
								e.getMessage() );
			//System.exit(-1);
		}
		
		//Answer a model maker
		maker = getModelMaker();
	}
	
//	public String getNsSchema() { 
//		 
//		return nsSchema;
//	}

	public String getNameSpace() { 
		 
		return namespace;
	}

	/*
	 * Clean and initialize database.
	 */
	public void resetModel() {

		//Clean database
		try {
			//Create database connection
			IDBConnection conn = new DBConnection(dbURL, dbUser, 
													dbPw, dbType);

			System.out.println("Cleaning persistent database at " +
									dbURL + " (dbUser)...");
			conn.cleanDB();
			
		} catch(Exception ex) {
			ex.printStackTrace();			
		}
			
		//Load schema ontology file
		loadSchema();
	}
	
	/*
	 * Returns an RDB Model Maker.
	 */
	public ModelMaker getModelMaker() {
		/*
		 * TODO
		 * Ogni volta si ri-crea l'oggetto ModelMaker in questo caso?
		 * Oppure si dovrebbe riutilizzare sempre la stessa istanza? 
		 */
		
		ModelMaker m = null;
		
		try {
			//Create database connection
			IDBConnection conn = new DBConnection(dbURL, dbUser, dbPw, dbType);

			//Answer a ModelMaker that accesses database-backed Models on the
			//database at the other end of the connection c with the usual
			//Standard reification style.
			m = ModelFactory.createModelRDBMaker(conn);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
			
		return m;
	}
	
	/*
	 * Load ontology schema file.
	 */
	protected void loadSchema() {

		Model base = maker.createModel(modelName, false);

		OntModel m = ModelFactory.createOntologyModel(getModelSpec(maker, false), base);
		
		//
		//System.out.println("Loading schema " + loadModel + "...");
		//m.read(loadModel);
	}
		
	private void setup(String URL, String User, String Pw, 
			String Type, String driver) {

		dbURL = URL;
		dbUser = User;
		dbPw = Pw;
		dbType = Type;
		dbDriver = driver;
	}

}
