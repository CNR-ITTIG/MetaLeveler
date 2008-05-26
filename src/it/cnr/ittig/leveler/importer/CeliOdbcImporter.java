package it.cnr.ittig.leveler.importer;

import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn.Lemma;
import it.cnr.ittig.jwneditor.jwn.Riferimento;
import it.cnr.ittig.jwneditor.jwn2owl.OWLUtil;
import it.cnr.ittig.leveler.Leveler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.vocabulary.RDF;

public class CeliOdbcImporter  extends ImporterUtil
	implements MetaImporter {
	
	private String mdbFileName = EditorConf.DATA_DIR + File.separatorChar 
		+ EditorConf.MDB_FILE_NAME;

	private String termsTBL = "TD_Terms";
	private String corpusTBL = "TD_Corpus";
	//private String internationalTBL = "TD_DocumentsInternational";
	private String nationalTBL = "TD_DocumentsNational";
	private String interlinguisticTBL = "TD_InterlinguisticRelations";
	private String intralinguisticTBL = "TD_IntralinguisticRelations";
	private String termdocumentTBL = "TD_TermDocumentRelations";
	
	private OntModel conceptModel;
	private OntModel indModel;
	private String NS_CONC = EditorConf.onto_concepts + "#";
	private OntClass conceptClass;
	private Map<String, OntModel> langToTypeModel; 
	
	private Map<String, Lemma> protoLangToLemma;
	private Map<Lemma, Collection<Lemma>> lemmaToLemmi;
	private Map<Lemma, Lemma> lemmaToConcept;
	private Map<Lemma, String> lemmaToLang;
	
	//SOURCES !!
	private Map<String,Riferimento> codeToRif;
	
	public void createSynsets() throws IOException {
		
		Connection c = openConnection();		
		
		String sql = "select * from " + termsTBL;
		Vector<String[]> results = eseguiQuery(c, sql);
		
		System.out.println("Results size: " + results.size());
		
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();
			
			String lang = row[0].trim();
			String id = row[1].trim();
			String lexical = row[2].trim();
			//String proto = row[3].trim(); //IGNORARE !!!!!
			
			if(!lang.equalsIgnoreCase(EditorConf.LANGUAGE)) {
				continue;
			}

			Concetto conc = new Concetto();
			conc.setID(id);
			//Lemma lemma = new Lemma(proto);
			Lemma lemma = new Lemma(lexical);
			lemma.setLemmaLang(EditorConf.LANGUAGE);
			conc.add(lemma);
			Leveler.appSynsets.put(id, conc);
			
			//aggiungi le varianti dalla tabella AL LEMMA !! <-- NON SONO QUI !! 
			//lemma.variants.add(lexical);
		}		
		
		System.out.println("Concepts: " + Leveler.appSynsets.values().size());
		closeConnection(c);
		
		//addDefinition();
	}

	public void addIpo() throws IOException {
		
		Connection c = openConnection();		
		
		String sql = "select * from " + intralinguisticTBL;
		Vector<String[]> results = eseguiQuery(c, sql);

		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();

			String lang = row[0].trim();
			String relName = row[2].trim();
			String idFrom = row[3].trim();
			String idTo = row[4].trim();
			
			if(!lang.equalsIgnoreCase(EditorConf.LANGUAGE)) {
				continue;
			}

			Concetto c1 = Leveler.appSynsets.get(idFrom);
			Concetto c2 = Leveler.appSynsets.get(idTo);
			if(c1 == null || c2 == null) {
				System.out.println(">>WARNING<< NULL - " + c1 + " " + c2);
				continue;
			}
			//Add new relation
			if(!addSingleRelation(c1, c2, relName)) {				
				break;
			}
		}
		closeConnection(c);
	}
	
	public void addRelated() throws IOException {	
	}
	
	public void addRif() throws IOException {
		
		codeToRif = new HashMap<String, Riferimento>();
		
		Connection c = openConnection();		

		//Crea una source anche per le Definizioni
		String sql = "SELECT T1.TD_TE_IdTerm, T2.DN_NationalCode, T3.CO_Text, T3.CO_filePath, T1.TD_OT_IdRelType FROM " + termdocumentTBL + 
			" T1, (SELECT DN_NationalCode, DN_IdDocumentNational FROM " + nationalTBL + 
			") AS T2, (SELECT CO_filePath, CO_NationalCode, CO_Text FROM " + corpusTBL + 
			" ) AS T3 WHERE T1.TD_DN_IdDocumentNational = T2.DN_IdDocumentNational " +
			"AND T2.DN_NationalCode = T3.CO_NationalCode " +
			"AND T1.TD_LG_IdLanguage = '" + EditorConf.LANGUAGE + "' " +
			"";

		Vector<String[]> results = eseguiQuery(c, sql);

		int counter = 0;
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();

			//String lang = row[0].trim();
			String id = row[0].trim();
			String part = row[1].trim();
			String text = row[2].trim();
			String fileName = row[3].trim();
			String type = row[4].trim();

//			if(!lang.equalsIgnoreCase(EditorConf.LANGUAGE)) {
//				continue;
//			}
			
//			if(counter < 10) {
//				System.out.println("id: " + id + " part: " + part);
//			}
			counter++;

			Concetto conc = Leveler.appSynsets.get(id);
			if(conc == null) {
				System.out.println(">>WARNING<< NULL - " + id + " " + conc);
				continue;
			}
			
//			String code = fileName + part + text;
//			//Add a new reference
//			Riferimento rif = codeToRif.get(code);
//			if( rif == null ) {
				Riferimento rif = new Riferimento();
				rif.setText(text);
				rif.setCode(part);
				rif.setFileName(fileName);				
//				codeToRif.put(code, rif);
//			}
				if(type.equalsIgnoreCase("definition")) {
					rif.setDefinition(true);
				}
			conc.addRiferimento(rif);
		}
		
		closeConnection(c);
	}
	

	public void addAlignment() throws IOException {
		
		protoLangToLemma = new HashMap<String, Lemma>();
		lemmaToLemmi = new HashMap<Lemma, Collection<Lemma>>();
		lemmaToConcept = new HashMap<Lemma, Lemma>();
		lemmaToLang = new HashMap<Lemma, String>();

		ModelMaker maker = ModelFactory.createMemModelMaker();
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		spec.setImportModelMaker(maker);
		conceptModel = ModelFactory.createOntologyModel(spec, null);
		conceptClass = conceptModel.createClass(NS_CONC + "Concept");
		indModel = ModelFactory.createOntologyModel(spec, null);
		langToTypeModel = new HashMap<String, OntModel>();
		for(int i = 0; i < EditorConf.languages.length; i++) {
			String lang = EditorConf.languages[i];
			langToTypeModel.put(lang, 
				ModelFactory.createOntologyModel(spec, null));			
		}				
		
		Connection c = openConnection();		
		
		String sql = "SELECT T1.IL_RT_IdRelType, T1.IL_LG_IdLanguage_From, T1.IL_LG_IdLanguage_To, T2.TE_SurfaceForm, T3.TE_SurfaceForm, T2.TE_Lemma, T3.TE_Lemma " +
			"FROM " + interlinguisticTBL + " T1, (SELECT TE_IdTerm, TE_SurfaceForm, TE_Lemma FROM " + termsTBL + 
			" ) AS T2, (SELECT TE_IdTerm, TE_SurfaceForm, TE_Lemma FROM " + termsTBL + " ) AS T3 " +
			"WHERE T1.IL_TE_IdTerm_From = T2.TE_IdTerm " +
			"AND T1.IL_TE_IdTerm_To = T3.TE_IdTerm " +
			//"AND T1.TD_OT_IdRelType = 'equivalent' " +
			"";	

		Vector<String[]> results = eseguiQuery(c, sql);

		int counter = 0;
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();

			String relType = row[0].trim();
			String langFrom = row[1].trim();
			String langTo = row[2].trim();
			String protoFrom = row[3].trim();
			String protoTo = row[4].trim();
			String conceptFrom = row[5].trim();
			String conceptTo = row[6].trim();
						
			if(!relType.equalsIgnoreCase("equivalent")) { //Ignorare le altre?
				continue;
			}

			if(counter < 10) {
				System.out.println("proto: " + protoFrom);
			}
			counter++;

//			Concetto c1 = Leveler.appSynsets.get(idFrom);
//			if(c1 == null ) {
//				System.out.println(">>WARNING<< NULL - " + c1);
//				continue;
//			}

			Lemma lemmaFrom = getLemma(protoFrom, langFrom, conceptFrom);
			Lemma lemmaTo = getLemma(protoTo, langTo, conceptTo);
			
			lemmaToLang.put(lemmaFrom, langFrom);
			lemmaToLang.put(lemmaTo, langTo);
			
			addLemmaConcept(lemmaFrom, lemmaTo);
		}				
		closeConnection(c);
		
		prepareConcepts();
		
		saveModels();		
	}

	public void addInterAlignment() {
		
		Connection c = openConnection();		
		
		String sql = "SELECT T1.IL_RT_IdRelType, T1.IL_LG_IdLanguage_From, T1.IL_LG_IdLanguage_To, T1.IL_TE_IdTerm_From, T1.IL_TE_IdTerm_To " +
			"FROM " + interlinguisticTBL + " T1 " +
			"WHERE T1.IL_RT_IdRelType <> 'equivalent' " +
			"";	

		Vector<String[]> results = eseguiQuery(c, sql);

		int counter = 0;
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();

			String relType = row[0].trim();
			String langFrom = row[1].trim();
			String langTo = row[2].trim();
			String idFrom = row[3].trim();
			String idTo = row[4].trim();
						
			if(relType.equalsIgnoreCase("equivalent")) {
				continue;
			}

			if(counter < 10) {
				System.out.println("proto: " + idFrom);
			}
			counter++;

//			Concetto c1 = Leveler.appSynsets.get(idFrom);
//			if(c1 == null ) {
//				System.out.println(">>WARNING<< NULL - " + c1);
//				continue;
//			}

//			Lemma lemmaFrom = getLemma(protoFrom, langFrom);
//			Lemma lemmaTo = getLemma(protoTo, langTo);
//			
//			lemmaToLang.put(lemmaFrom, langFrom);
//			lemmaToLang.put(lemmaTo, langTo);
//			
//			addLemmaConcept(lemmaFrom, lemmaTo);
		}
		closeConnection(c);
	}

//	public void addDefinition() throws IOException {
//		
//		Connection c = openConnection();		
//
//		String sql = "SELECT T1.TD_TE_IdTerm, T2.DN_NationalCode, T3.CO_Text FROM " + termdocumentTBL + 
//			" T1, (SELECT DN_NationalCode, DN_IdDocumentNational FROM " + nationalTBL + 
//			") AS T2, (SELECT CO_NationalCode, CO_Text FROM " + corpusTBL + 
//			" ) AS T3 WHERE T1.TD_DN_IdDocumentNational = T2.DN_IdDocumentNational " +
//			"AND T2.DN_NationalCode = T3.CO_NationalCode " +
//			"AND T1.TD_LG_IdLanguage = '" + EditorConf.LANGUAGE + "' " +
//			"AND T1.TD_OT_IdRelType = 'definition'" +
//			"";
//			
//		Vector<String[]> results = eseguiQuery(c, sql);
//
//		int counter = 0;
//		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
//			String[] row = i.next();
//
//			//String lang = row[0].trim();
//			String id = row[0].trim();
//			String part = row[1].trim();
//			//String code = row[2].trim();
//			String text = row[2].trim();
//
////			if(counter < 10) {
////				System.out.println("id: " + id + " part: " + part);
////			}
//			counter++;
//
//			Concetto conc = Leveler.appSynsets.get(id);
//			if(conc == null) {
//				System.out.println(">>WARNING<< NULL - " + id + " " + conc);
//				continue;
//			}
//
//			//Add definition
//			conc.setDefinizione(text);
//			//System.out.println("CONCETTO: " + conc + " DEF: " + text);
//		}
//		closeConnection(c);
//	}

	private Lemma getLemma(String proto, String lang, String conceptName) {
		
		String key = proto + lang;
		Lemma lemma = protoLangToLemma.get(key);
		if(lemma == null) {
			lemma = new Lemma(proto);
			protoLangToLemma.put(key, lemma);
			lemma.setLemmaLang(lang);
			lemma.setTempConceptName(conceptName);
		}
		return lemma;
	}	
	
	private void prepareConcepts() {
				
		Collection<Lemma> finalConceptsLemma = new HashSet<Lemma>();
		Collection<Lemma> lemmas = lemmaToLemmi.keySet();
		System.out.println("Preparing... lsize: " + lemmas.size());
		for(Iterator<Lemma> i = lemmas.iterator(); i.hasNext(); ) {
			Lemma lemma = i.next();						
			Collection<Lemma> concepts = lemmaToLemmi.get(lemma);
			if(concepts.isEmpty()) {
				System.err.println("ERROR! empty concepts for lemma: " + lemma);
				continue;
			} 
			Lemma mainConcept = getMainConcept(concepts); //Oppure mettere subito URI artificiali?
			finalConceptsLemma.add(mainConcept);
			lemmaToConcept.put(lemma, mainConcept);
		}
		
		//Add final concepts to concept model
		System.out.println("fcsize: " + finalConceptsLemma.size());
		for(Iterator<Lemma> i = finalConceptsLemma.iterator(); i.hasNext(); ) {
			Lemma concept = i.next();
			createConceptClass(concept);
		}
		
		if(EditorConf.ADD_ALIGNMENT) {
			//Add inter-linguistic relations between concepts...
			//addInterAlignment();
		}
		
		//Add types triples
		for(Iterator<Lemma> i = lemmas.iterator(); i.hasNext(); ) {
			Lemma lemma = i.next();
			Lemma concept = lemmaToConcept.get(lemma);
			String lang = lemmaToLang.get(lemma);
			OntClass oc = getConceptClass(concept);
			addType(oc, lemma, lang);			
		}
		
		//Set concept lemma in Concetto object
		Collection<Concetto> concetti = Leveler.appSynsets.values();
		for(Iterator<Concetto> i = concetti.iterator(); i.hasNext();) {
			Concetto item = i.next();
			Lemma lemma = item.getPrimario();
			Lemma concept = lemmaToConcept.get(lemma);
			item.conceptLemma = concept;
		}
	}
	
	private Lemma getMainConcept(Collection<Lemma> concepts) {
		//Search main concept depending on language (en, it, ...)
		
		for(Iterator<Lemma> i = concepts.iterator(); i.hasNext();) {
			Lemma item = i.next();
			if(item.getLemmaLang().equalsIgnoreCase("en")) {
				return item;
			}
		}
		for(Iterator<Lemma> i = concepts.iterator(); i.hasNext();) {
			Lemma item = i.next();
			if(item.getLemmaLang().equalsIgnoreCase("it")) {
				return item;
			}
		}
		for(Iterator<Lemma> i = concepts.iterator(); i.hasNext();) {
			//This must be a sorted collection to get always the same result!
			return i.next();
		}
		return null;
	}
	
	private void addLemmaConcept(Lemma lemmaA, Lemma lemmaB) {
		
		Collection<Lemma> conceptsA = lemmaToLemmi.get(lemmaA);
		Collection<Lemma> conceptsB = lemmaToLemmi.get(lemmaB);
		
		if(conceptsA == null) {
			conceptsA = new TreeSet<Lemma>();
			conceptsA.add(lemmaA);
			lemmaToLemmi.put(lemmaA, conceptsA);
		}
		if(conceptsB == null) {
			conceptsB = new TreeSet<Lemma>();
			conceptsB.add(lemmaB);
			lemmaToLemmi.put(lemmaB, conceptsB);
		}
		//Merge concepts collection
		conceptsA.addAll(conceptsB);
		conceptsB = conceptsA;
	}
	
	private void saveModels() {
		
		serialize(conceptModel, 
				EditorConf.DATA_DIR + File.separatorChar + "global-concepts.owl",
				NS_CONC);
		
		for(int i = 0; i < EditorConf.languages.length; i++) {
			String lang = EditorConf.languages[i];
			OntModel typeModel = langToTypeModel.get(lang);
			serialize(typeModel,
					EditorConf.DATA_DIR + File.separatorChar + "types-" + lang + ".owl",
					EditorConf.dalos_ns + lang + "/types.owl#");					
		}
	}
	
	private void serialize(OntModel om, String outputFile, String ns) {
		
		System.out.println("Serializing ontology model to " + outputFile + "...");

		RDFWriter writer = om.getWriter("RDF/XML");
		
		String relativeOutputFileName = "file://" + outputFile;
		if(ns == null ||ns.equals("")) {
			writer.setProperty("xmlbase", relativeOutputFileName);
		} else {
			writer.setProperty("xmlbase", ns);
		}
		
		try {
			OutputStream out = new FileOutputStream(outputFile);
			//Write down the BASE model only (don't follow imports...)
			writer.write(om.getBaseModel(), out, relativeOutputFileName);
			out.close();
		} catch(Exception e) {
			System.err.println("Exception serializing model:" + e.getMessage());
			e.printStackTrace();
		}
	}	

	private Vector<String[]> eseguiQuery(Connection c, String query) {
		System.out.println("Exec query: " + query);
		Vector<String[]> v = null;
		String[] record;
		int colonne = 0;
		try {
			Statement stmt = (Statement) c.createStatement();
			ResultSet rs = (ResultSet) stmt.executeQuery(query);
			v = new Vector<String[]>();
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			colonne = rsmd.getColumnCount();

			while(rs.next()) {
				record = new String[colonne];
				for (int i = 0; i < colonne; i++) {
					record[i] = rs.getString(i+1);
				}
				//Always avoid clone() method ??
				//v.add( record.clone() );
				v.add(record);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return v;
	}

	private Connection openConnection() {
		
		Connection c = null;
		Properties prop = new Properties();
		//prop.setProperty("DB2e_ENCODING", "CP-1252");
		prop.setProperty("DB2e_ENCODING", "Windows-1252");
		
		/*
		 *
		 * http://bugs.mysql.com/bug.php?id=25022
		 * 
[28 Mar 2007 9:40] Freddy Kaiser

Hi, similar issues with MS-SQL when the DB doesn't have default Collation (Latin... but
French_CI_AS).
I was able to resolve the migration with this:
  The default/implicit jdbc connection is:
jdbc:jtds:sqlserver://<ip>:1433/<db>;user=<userid>;password=<pwd>;charset=utf-8;domain=
  => With this it failed
  By using advanced on the Source Database I put this:
jdbc:jtds:sqlserver://<ip>:1433/<db>;user=<userid>;password=<pwd>;domain=
  => With this it was all fine (no other special option)
   Based on the MS JDBC documentation their driver will choose the right charset
Fix:
  Either adding a dropdown/entryfield where this could be defined like the other options
or removing the implicit charset=utf-8


[30 Apr 2007 11:47] Michael G. Zinner

The problem with MS Access is, that it does not support charsets and whatever is stored
inside the database depends on the local system settings.

When accessing the data via the JDBC/ODBC bridge there is no way to tell the encoding of
the data stored in the MS Access database. Therefore we cannot correctly convert the
data.

The workaround of going through the MS SQL server is a good one, as there charsets are
handled as expected.

The charset handling in the connection dialog for the MS SQL server is limited due to the
fact that the available list of charsets depends on the MS SQL server installation and is
therefor custom. Manually stating the charset in the Advanced Option is the correct way to
deal with this scenario.


---------------------------
Hi,
I want to migrate the data (tables) of a MS SQL (Server 2000) database to MySQL (5.0.23).

For this, i`m using the "MySQL Migration Toolkit" (v. 1.1.4).
My problem is the encoding of the "special characters" like the german umlauts (e.g. ä,ü,ö).
In the target MySQl database (resp. in the the generated script file) the umlauts for "Gemeindebhörden" are displayed as "Gemeindebehï¿½rden".

The collation of the MS SQL database is Latin1_General_CP1_CI_AS.
In step 3 of the migration (Object Mapping), i tried several charset/collation-settings (e.g. latin1/latin1_german1_ci, latin1/latin1_swedish_ci, utf8/utf8_general_ci).

Does anybody have an idea how to migrate my data with the correct encoding settings ?
Thanks in advance!

Hi Pat!

I have exact the same problem to migrate the data between MS SQL and MySQL. I will solve this customizing the jdbc connection string, adding useUnicode param in both connections (MSSQL and MySQL).

It is in the first step, when select the source and target database, in Advanced >> option. I use the next jdbc connection string for MSSQL:

jdbc:jtds:sqlserver://server:1433/database;user=user;password=password;useUnicode=true;domain=

And the next one for MySQL:

jdbc:mysql://server:3306/?user=user&password=password&useServerPrepStmts=false&useUnicode=true

Replace with your database servers information, and try it ;)

P.D: Finally in the Object Mapping step I use the option multilanguage for the MySQL Schema and tables, for use utf-8 charsets in the target database.

		 */
		
		try {
			String strConn = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + mdbFileName;
			Driver d = (Driver)Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
			c = DriverManager.getConnection(strConn, prop);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return c;
	}
	
	private void closeConnection(Connection c) {
		
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private OntClass createConceptClass(Lemma proto) {
		//GET OR CREATE
		
		OntClass synsetClass = getConceptClass(proto);
		if(synsetClass != null) {
			return synsetClass;
		}

		//Add a new concept class
		String name = OWLUtil.getConceptClassName(proto);
		synsetClass = conceptModel.createClass(NS_CONC + name);
		synsetClass.addSuperClass(conceptClass);
		
		//Link this synset to the concept class
		//addType(synsetClass, proto, lang);
		
		return synsetClass;
	}
	
	private void addType(OntClass cc, Lemma proto, String lang) {
		
		OntModel typeModel = langToTypeModel.get(lang);		
		OntResource syn = createSynsetIndividual(proto, lang);
		typeModel.add(syn, RDF.type, cc);
	}

	private OntClass getConceptClass(Lemma proto) {
		//GET
		
		String name = OWLUtil.getConceptClassName(proto);
		return conceptModel.getOntClass(NS_CONC + name);
	}
	
	private OntResource createSynsetIndividual(Lemma proto, String lang) {
		//GET OR CREATE
		
		String name = OWLUtil.getSynsetName(proto);
		String uri = EditorConf.dalos_ns + lang + "/individuals.owl#" + name;
		OntResource res = indModel.getOntResource(uri);
		if(res == null) {
			res = indModel.createOntResource(uri);	
		}
		return res;
	}
	
}
