package it.cnr.ittig.leveler.importer;

import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn.Correlazione;
import it.cnr.ittig.jwneditor.jwn.Lemma;
import it.cnr.ittig.jwneditor.jwn.Relazione;
import it.cnr.ittig.jwneditor.jwn2owl.OWLUtil;
import it.cnr.ittig.jwneditor.jwn2owl.container.OntologyContainer;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.vocabulary.RDF;

public class CeliOdbcImporter implements MetaImporter {
	
	private String mdbFileName = EditorConf.DATA_DIR + File.separatorChar 
		+ EditorConf.MDB_FILE_NAME;

	private String termsTBL = "TD_Terms";
	private String corpusTBL = "TD_Corpus";
	private String internationalTBL = "TD_DocumentsInternational";
	private String nationalTBL = "TD_DocumentsNational";
	private String interlinguisticTBL = "TD_InterlinguisticRelations";
	private String intralinguisticTBL = "TD_IntralinguisticRelations";
	private String termdocumentTBL = "TD_TermDocumentRelations";
	
	private OntModel conceptModel;
	private OntModel indModel;
	private String NS_CONC = EditorConf.onto_concepts + "#";
	private OntClass conceptClass;
	private Map<String, OntModel> langToTypeModel; 
	
	public void createSynsets() throws IOException {
		
		Connection c = openConnection();		
		
		String sql = "select * from " + termsTBL;
		Vector<String[]> results = eseguiQuery(c, sql);
		
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();
			
			String lang = row[0].trim();
			String id = row[1].trim();
			String lexical = row[2].trim();
			String proto = row[3].trim();
			
			if(!lang.equalsIgnoreCase(EditorConf.LANGUAGE)) {
				continue;
			}

			Concetto conc = new Concetto();
			conc.setID(id);
			Lemma lemma = new Lemma(proto);
			lemma.protoForm = proto;
			conc.add(lemma);
			Leveler.appSynsets.put(id, conc);
			
			//aggiungi le varianti dalla tabella AL LEMMA !! 
			lemma.variants.add(proto);
			lemma.variants.add(lexical);
		}		
		closeConnection(c);
		
		addDefinition();
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
				System.err.println("Relation not found: " + relName + " !");
				break;
			}
		}
		closeConnection(c);
	}
	
	private boolean addSingleRelation(Concetto c1, Concetto c2, String relName) {

		Relazione ipo = new Relazione(EditorConf.iponimia);
		Relazione iper = new Relazione(EditorConf.iperonimia);
		Relazione fuzzy = new Relazione(EditorConf.related);
		Relazione thisRel = null;
		Relazione invRel = null;

		if(relName.equalsIgnoreCase("has_hyponym")) {
			thisRel = ipo;
			invRel = iper;
		} else if(relName.equalsIgnoreCase("has_hyperonym")) {
			thisRel = iper;
			invRel = ipo;
		} else if(relName.equalsIgnoreCase("fuzzynym")) {
			thisRel = fuzzy;
			invRel = fuzzy;
		} else {			
			return false;
		}
		
		Correlazione cor = new Correlazione(c2, thisRel);
		c1.add(cor);
		cor = new Correlazione(c1, invRel);
		c2.add(cor);
		return true;
	}
	
	public void addRelated() throws IOException {	
	}
	
	public void addRif() throws IOException {
		
		Connection c = openConnection();		

		String sql = "SELECT T1.TD_TE_IdTerm, T2.DN_NationalCode FROM " + termdocumentTBL + 
			" T1, (SELECT DN_NationalCode, DN_IdDocumentNational FROM " + nationalTBL + 
			" ) AS T2 WHERE T1.TD_DN_IdDocumentNational = T2.DN_IdDocumentNational " +
			"AND T1.TD_LG_IdLanguage = '" + EditorConf.LANGUAGE + "' " +
			"AND T1.TD_OT_IdRelType = 'mention'" +
			"";
			
		Vector<String[]> results = eseguiQuery(c, sql);

		int counter = 0;
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();

			//String lang = row[0].trim();
			String id = row[0].trim();
			String part = row[1].trim();

//			if(!lang.equalsIgnoreCase(EditorConf.LANGUAGE)) {
//				continue;
//			}
			
			if(counter < 10) {
				System.out.println("id: " + id + " part: " + part);
			}
			counter++;

			Concetto conc = Leveler.appSynsets.get(id);
			if(conc == null) {
				System.out.println(">>WARNING<< NULL - " + id + " " + conc);
				continue;
			}

			//Add a new reference
			conc.riferimenti.add(part);			
		}
		closeConnection(c);
	}
	

	public void addAlignment() throws IOException {
		
		//addConcepts();

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
		
		String sql = "SELECT T1.IL_RT_IdRelType, T1.IL_LG_IdLanguage_From, T1.IL_LG_IdLanguage_To, T2.TE_Lemma, T3.TE_Lemma " +
			"FROM " + interlinguisticTBL + " T1, (SELECT TE_IdTerm, TE_Lemma FROM " + termsTBL + 
			" ) AS T2, (SELECT TE_IdTerm, TE_Lemma FROM " + termsTBL + " ) AS T3 " +
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

			Lemma lemmaFrom = new Lemma(protoFrom);
			lemmaFrom.protoForm = protoFrom;
			lemmaFrom.variants.add(protoFrom);
			Lemma lemmaTo = new Lemma(protoTo);
			lemmaTo.protoForm = protoTo;
			lemmaTo.variants.add(protoTo);
			
			OntClass ocFrom = getConceptClass(lemmaFrom);
			OntClass ocTo = getConceptClass(lemmaTo);
			
			if(ocFrom == null && ocTo == null) {
				OntClass oc = createConceptClass(lemmaTo, langTo);
				addType(oc, lemmaFrom, langFrom);
				continue;
			}
			
			if(ocFrom != null && ocTo == null) {
				OntClass oc = createConceptClass(lemmaFrom, langFrom);
				addType(oc, lemmaTo, langTo);
				continue;
			}

			if(ocFrom == null && ocTo != null) {
				OntClass oc = createConceptClass(lemmaTo, langTo);
				addType(oc, lemmaFrom, langFrom);
				continue;
			}

			if(ocFrom != null && ocTo != null) {
				//FIXME Non è molto bello aggiungerli ad entrambi i concept...
				addType(ocFrom, lemmaTo, langTo);
				addType(ocTo, lemmaFrom, langFrom);
				continue;
			}
		}
		closeConnection(c);
		
		saveModels();		
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

//	public void addAlignment() throws IOException {
//		
//		addConcepts();
//		
//		Connection c = openConnection();		
//		
//		String sql = "SELECT T1.IL_RT_IdRelType, T1.IL_TE_IdTerm_From, T1.IL_LG_IdLanguage_To, T2.TE_Lemma " +
//			"FROM " + interlinguisticTBL + " T1, (SELECT TE_IdTerm, TE_Lemma FROM " + termsTBL + 
//			" ) AS T2 WHERE T1.IL_TE_IdTerm_To = T2.TE_IdTerm " +
//			"AND T1.IL_LG_IdLanguage_From = '" + EditorConf.LANGUAGE + "' " +
//			//"AND T1.TD_OT_IdRelType = 'equivalent' " +
//			"";	
//
//		Vector<String[]> results = eseguiQuery(c, sql);
//
//		int counter = 0;
//		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
//			String[] row = i.next();
//
//			String relType = row[0].trim();
//			String idFrom = row[1].trim();
//			String langTo = row[2].trim();
//			String protoConcept = row[3].trim();
//						
//			if(!relType.equalsIgnoreCase("equivalent")) { //Ignorare le altre?
//				continue;
//			}
//
//			if(counter < 10) {
//				System.out.println("idf: " + idFrom + " proto: " + protoConcept);
//			}
//			counter++;
//
//			if(!langTo.equalsIgnoreCase("EN")) {
//				System.err.println(">> LangTo is not english! Skipping...!?");
//				continue;
//			}
//
//			Concetto c1 = Leveler.appSynsets.get(idFrom);
//			if(c1 == null ) {
//				System.out.println(">>WARNING<< NULL - " + c1);
//				continue;
//			}
//
//			//Link the concept class to the synset
//			Lemma lemma = new Lemma(protoConcept);
//			lemma.protoForm = protoConcept;
//			lemma.variants.add(protoConcept);
//			c1.conceptLemma = lemma; //Sostituisce il precendente Concept
//		}
//		closeConnection(c);
//		
//		addDefinition();
//	}
	
	public void addDefinition() throws IOException {
		
		Connection c = openConnection();		

		String sql = "SELECT T1.TD_TE_IdTerm, T2.DN_NationalCode, T3.CO_Text FROM " + termdocumentTBL + 
			" T1, (SELECT DN_NationalCode, DN_IdDocumentNational FROM " + nationalTBL + 
			") AS T2, (SELECT CO_NationalCode, CO_Text FROM " + corpusTBL + 
			" ) AS T3 WHERE T1.TD_DN_IdDocumentNational = T2.DN_IdDocumentNational " +
			"AND T2.DN_NationalCode = T3.CO_NationalCode " +
			"AND T1.TD_LG_IdLanguage = '" + EditorConf.LANGUAGE + "' " +
			"AND T1.TD_OT_IdRelType = 'definition'" +
			"";
			
		Vector<String[]> results = eseguiQuery(c, sql);

		int counter = 0;
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();

			//String lang = row[0].trim();
			String id = row[0].trim();
			String part = row[1].trim();
			//String code = row[2].trim();
			String text = row[2].trim();

			if(counter < 10) {
				System.out.println("id: " + id + " part: " + part);
			}
			counter++;

			Concetto conc = Leveler.appSynsets.get(id);
			if(conc == null) {
				System.out.println(">>WARNING<< NULL - " + id + " " + conc);
				continue;
			}

			//Add definition
			conc.setDefinizione(text);
			System.out.println("CONCETTO: " + conc + " DEF: " + text);
		}
		closeConnection(c);
	}

	private void addConcepts() throws IOException {
		//Aggiunge un concetto per ogni termine
		
		Connection c = openConnection();

		String sql = "select * from " + termsTBL + 
			" where TE_LG_IdLanguage = '" + EditorConf.LANGUAGE + "'";
		
		Vector<String[]> results = eseguiQuery(c, sql);

		int counter = 0;
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();

			String id = row[1].trim();
			String protoConcept = row[3].trim();
						
			if(counter < 10) {
				System.out.println("idf: " + id + " proto: " + protoConcept);
			}
			counter++;

			Concetto c1 = Leveler.appSynsets.get(id);
			if(c1 == null ) {
				System.out.println(">>WARNING<< NULL - " + c1);
				continue;
			}

			//Link the concept class to the synset
			Lemma lemma = new Lemma(protoConcept);
			lemma.protoForm = protoConcept;
			lemma.variants.add(protoConcept);
			c1.conceptLemma = lemma;
		}
		closeConnection(c);
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
		
		try {
			String strConn = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + mdbFileName;
			Driver d = (Driver)Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
			c = DriverManager.getConnection(strConn);
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
	
	private OntClass createConceptClass(Lemma proto, String lang) {
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
		addType(synsetClass, proto, lang);
		
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
