package it.cnr.ittig.bacci.converter;

import it.cnr.ittig.bacci.converter.objects.Concept;
import it.cnr.ittig.bacci.converter.objects.Relation;
import it.cnr.ittig.bacci.converter.objects.Synset;
import it.cnr.ittig.bacci.util.Conf;
import it.cnr.ittig.bacci.util.KbModelFactory;
import it.cnr.ittig.bacci.util.Util;
import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.jwn.Lemma;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class InterConceptAdder {

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
	
	private String dataDir = null;
	
	private Map<String,Concept> uriToConcept = null;
	private Map<Synset,Concept> synsetToConcept = null;
	private Map<String,Synset> idToSynset = null;
	
	public InterConceptAdder() {
		
		uriToConcept = new HashMap<String,Concept>();
		synsetToConcept = new HashMap<Synset,Concept>();
		idToSynset = new HashMap<String,Synset>();
		
		
		initEnv();
	}
	
	private void initEnv() {
		
		File dataDirFile = new File(".");
		dataDir = dataDirFile.getAbsolutePath();
		Util.initDocuments(dataDir);
	}
	
	public void addInterAlignment() {
		
		//carica in memoria i 4 lessici
		OntModel enLexicon = KbModelFactory.getModel("dalos.lexicon.EN", "micro");
		OntModel esLexicon = KbModelFactory.getModel("dalos.lexicon.ES", "micro");
		OntModel itLexicon = KbModelFactory.getModel("dalos.lexicon.IT", "micro");
		OntModel nlLexicon = KbModelFactory.getModel("dalos.lexicon.NL", "micro");
		
		OntModel concModel = KbModelFactory.getModel("dalos.metaconc");
		
		//crea oggetti in memoria
		initConcepts(concModel);		
		addLexicalizations(concModel, enLexicon);
		addLexicalizations(concModel, esLexicon);
		addLexicalizations(concModel, itLexicon);
		addLexicalizations(concModel, nlLexicon);
		
		//LEGGI DATABASE
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
			
			Synset synFrom = idToSynset.get(idFrom);
			if(synFrom == null) {
				System.err.println("synFrom not found: " + idFrom);
				return;
			}
			Synset synTo = idToSynset.get(idTo);
			if(synTo == null) {
				System.err.println("synTo not found: " + idTo);
				return;
			}
			
			//SE I CONCETTI MANCANO?? VANNO CREATI...
			Concept concFrom = synsetToConcept.get(synFrom);
			if(concFrom == null) {
				System.err.println("Missing concFrom for: " + idFrom);
				return;
			}
			Concept concTo = synsetToConcept.get(synTo);
			if(concTo == null) {
				System.err.println("Missing concTo for: " + idTo);
				return;
			}
			
			if(!addInterRelation(concFrom, concTo, relType)) {
				System.err.println("ERROR.");
				return;
			}

		}
		closeConnection(c);
		
		//Serialization...
		OntModel interModel = KbModelFactory.getModel();
		
		OntProperty narrowProp = concModel.getOntProperty(Conf.CONCEPTSCHEMA_NS + "narrowMatch");
		OntProperty broaderProp = concModel.getOntProperty(Conf.CONCEPTSCHEMA_NS + "broaderMatch");
		OntProperty eqsynProp = concModel.getOntProperty(Conf.CONCEPTSCHEMA_NS + "eqsynMatch");
		OntProperty fuzzyProp = concModel.getOntProperty(Conf.CONCEPTSCHEMA_NS + "fuzzyMatch");
		OntProperty cohypoProp = concModel.getOntProperty(Conf.CONCEPTSCHEMA_NS + "cohypoMatch");
		
		Collection<Concept> concepts = uriToConcept.values();
		for(Iterator<Concept> i = concepts.iterator(); i.hasNext(); ) {
			Concept c1 = i.next();
			OntResource res1 = concModel.getOntResource(c1.getURI());
			for(Iterator<Concept> k = c1.getHyperConcepts().iterator(); k.hasNext();) {
				Concept c2 = k.next();
				OntResource res2 = concModel.getOntResource(c2.getURI());
				interModel.add(res1, narrowProp, res2);
				interModel.add(res2, broaderProp, res1);
			}
			for(Iterator<Concept> k = c1.getCohypoConcepts().iterator(); k.hasNext();) {
				Concept c2 = k.next();
				OntResource res2 = concModel.getOntResource(c2.getURI());
				interModel.add(res1, cohypoProp, res2);
				interModel.add(res2, cohypoProp, res1);
			}
			for(Iterator<Concept> k = c1.getEqsynConcepts().iterator(); k.hasNext();) {
				Concept c2 = k.next();
				OntResource res2 = concModel.getOntResource(c2.getURI());
				interModel.add(res1, eqsynProp, res2);
				interModel.add(res2, eqsynProp, res1);
			}
			for(Iterator<Concept> k = c1.getFuzzyConcepts().iterator(); k.hasNext();) {
				Concept c2 = k.next();
				OntResource res2 = concModel.getOntResource(c2.getURI());
				interModel.add(res1, fuzzyProp, res2);
			}
		}
		
		String fileName = dataDir + File.separatorChar + Conf.INTERCONCEPTS;
		Util.serialize(interModel, fileName);
		
	}
	
	private boolean addInterRelation(Concept c1, Concept c2, String relName) {
		
		if(relName.equalsIgnoreCase("hypernymy")) {
			c1.addHyperConcept(c2);
		} else if(relName.equalsIgnoreCase("co_hyponym")) {
			c1.addCohypoConcept(c2);
		} else if(relName.equalsIgnoreCase("fuzzynym")) {
			c1.addFuzzyConcept(c2);
		} else if(relName.equalsIgnoreCase("eq-synonym")) {
			c1.addEqsynConcept(c2);
		} else {
			System.err.println("addInterRelation() - " +
					"Relation not found: " + relName + " !");
			return false;
		}
		return true;
	}
	
	private void initConcepts(OntModel concModel) {
		
		OntClass conceptClass = concModel.getOntClass(Conf.CONCEPT_CLASS);
		if(conceptClass == null) {
			System.err.println("ERROR! initConcepts() - conceptClass is null");
			return;
		}
		
		for(Iterator i = conceptClass.listInstances(true); i.hasNext(); ) {
			OntResource ores = (OntResource) i.next();
			String puri = ores.getNameSpace() + ores.getLocalName();
			
			Concept conc = uriToConcept.get(puri);
			if(conc == null) {
				conc = new Concept();
				conc.setURI(puri);
				uriToConcept.put(puri, conc);
			}
		}
	}
	
	private void addLexicalizations(OntModel concModel, OntModel lexicon) {
		
		OntProperty lexProp = lexicon.getOntProperty(Conf.LEXICALIZATION_PROP);
		if(lexProp == null) {
			System.err.println("ERROR! INTER - lexProp is null");
			return;
		}
		OntProperty idProp = lexicon.getOntProperty(Conf.METALEVEL_ONTO_NS + "synsetId");
		if(idProp == null) {
			System.err.println("ERROR! INTER - idProp is null");
			return;
		}

		StmtIterator si = lexicon.listStatements(null, lexProp, (RDFNode) null);
		for(; si.hasNext(); ) {
			com.hp.hpl.jena.rdf.model.Statement stm = si.nextStatement();
			OntResource subj = (OntResource) stm.getSubject();
			OntResource obj = (OntResource) stm.getObject();
			String curi = subj.getNameSpace() + subj.getLocalName();
			String suri = obj.getNameSpace() + obj.getLocalName();
			Concept conc = uriToConcept.get(curi);
			//Prendi l'id del synset
			RDFNode idNode = obj.getPropertyValue(idProp);
			if(idNode == null) {
				System.err.println("NO ID for " + suri);
				return;
			}
			String rawId = ((Literal) idNode).getString();
			rawId = rawId.substring(1); //toglie l'1 iniziale
			Integer intId = Integer.valueOf(rawId); //toglie gli zeri iniziali
			String id = String.valueOf(intId); //id finale String
			//Prendi il synset
			Synset syn = idToSynset.get(id);
			if(syn == null) {
				syn = new Synset();
				syn.setID(id);
				syn.setURI(suri);
				idToSynset.put(id, syn);
			}
			
			//Aggiungi la lexicalization
			synsetToConcept.put(syn, conc);
		}
	}


	private Connection openConnection() {
		
		Connection c = null;
		Properties prop = new Properties();
		//prop.setProperty("DB2e_ENCODING", "CP-1252");
		prop.setProperty("DB2e_ENCODING", "Windows-1252");
		
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
}
