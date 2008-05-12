package it.cnr.ittig.bacci.converter;

import it.cnr.ittig.bacci.converter.objects.Concept;
import it.cnr.ittig.bacci.converter.objects.OntoClass;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class InterConceptAdder {

	private OntModel enLexicon = null;
	private OntModel esLexicon = null;
	private OntModel itLexicon = null;
	private OntModel nlLexicon = null;
	
	private OntModel enLexModel = null;
	private OntModel esLexModel = null;
	private OntModel itLexModel = null;
	private OntModel nlLexModel = null;
	
	//private OntModel metaConcModel = null;
	private OntModel concModel = null;

	//Artificial concept class counter
	private static int artificialCounter = 0;
	private static String artificialPrefix = "artconc-";

	private String mdbFileName = EditorConf.MDB_FILE_NAME;

	private String termsTBL = "TD_Terms";
	private String corpusTBL = "TD_Corpus";
	//private String internationalTBL = "TD_DocumentsInternational";
	private String nationalTBL = "TD_DocumentsNational";
	private String interlinguisticTBL = "TD_InterlinguisticRelations";
	private String intralinguisticTBL = "TD_IntralinguisticRelations";
	private String termdocumentTBL = "TD_TermDocumentRelations";
	
	private String dataDir = null;
	
	private Map<String,Concept> uriToConcept = null;
	private Map<Synset,Concept> synsetToConcept = null;
	private Map<String,Synset> idToSynset = null;
	
	private Map<String,OntoClass> uriToOntoClass = null;
	
	public InterConceptAdder() {
		
		uriToConcept = new HashMap<String,Concept>();
		synsetToConcept = new HashMap<Synset,Concept>();
		idToSynset = new HashMap<String,Synset>();
		
		uriToOntoClass = new HashMap<String, OntoClass>();
		
		initEnv();
	}
	
	private void initEnv() {
		
		File dataDirFile = new File(".");
		dataDir = dataDirFile.getAbsolutePath();
		Util.initDocuments(dataDir);
		
		File mdbFile = new File(mdbFileName);
		mdbFileName = mdbFile.getAbsolutePath();
	}
	
	public void addInterAlignment() {
		
		//carica in memoria i 4 lessici
		enLexicon = KbModelFactory.getModel("dalos.lexicon.EN", "micro");
		esLexicon = KbModelFactory.getModel("dalos.lexicon.ES", "micro");
		itLexicon = KbModelFactory.getModel("dalos.lexicon.IT", "micro");
		nlLexicon = KbModelFactory.getModel("dalos.lexicon.NL", "micro");
		
		enLexModel = KbModelFactory.getModel("dalos.lexicalization.EN");
		esLexModel = KbModelFactory.getModel("dalos.lexicalization.ES");
		itLexModel = KbModelFactory.getModel("dalos.lexicalization.IT");
		nlLexModel = KbModelFactory.getModel("dalos.lexicalization.NL");
		
		//metaConcModel = KbModelFactory.getModel("dalos.metaconc");
		concModel = KbModelFactory.getModel("dalos.concepts.all");
		
		//crea oggetti in memoria
		initConcepts();		
		
		addSynsets(enLexicon);
		addSynsets(esLexicon);
		addSynsets(itLexicon);
		addSynsets(nlLexicon);
		
		addLexicalizations(enLexicon);
		addLexicalizations(esLexicon);
		addLexicalizations(itLexicon);
		addLexicalizations(nlLexicon);

		//LEGGI DATABASE
		Connection c = openConnection();		
		
		String sql = "SELECT T1.IL_RT_IdRelType, T1.IL_LG_IdLanguage_From, T1.IL_LG_IdLanguage_To, T1.IL_TE_IdTerm_From, T1.IL_TE_IdTerm_To " +
			"FROM " + interlinguisticTBL + " T1 " +
			"WHERE T1.IL_RT_IdRelType <> 'equivalent' " +
			"";	

		Vector<String[]> results = eseguiQuery(c, sql);

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

			Synset synFrom = idToSynset.get(idFrom);
			if(synFrom == null) {
				System.err.println("synFrom not found: " + idFrom);
				continue;
			}
			Synset synTo = idToSynset.get(idTo);
			if(synTo == null) {
				System.err.println("synTo not found: " + idTo);
				continue;
			}
			
			//SE I CONCETTI MANCANO?? VANNO CREATI...
			Concept concFrom = synsetToConcept.get(synFrom);
			if(concFrom == null) {
				System.out.println("Missing concFrom for: " + idFrom + 
					"! Creating it...");
				concFrom = getArtificialConcept();
				if(concFrom == null) {
					return;
				}
				synsetToConcept.put(synFrom, concFrom);
				//Aggiungi la lessicalizzazione
				addNewLexicalization(concFrom, synFrom, langFrom);
			}
			Concept concTo = synsetToConcept.get(synTo);
			if(concTo == null) {
				System.out.println("Missing concTo for: " + idTo +
					"! Creating it...");
				concTo = getArtificialConcept();
				if(concTo == null) {
					return;
				}
				synsetToConcept.put(synTo, concTo);
				//Aggiungi la lessicalizzazione
				addNewLexicalization(concTo, synTo, langTo);
			}
			
			if(!addInterRelation(concFrom, concTo, relType)) {
				System.err.println("ERROR.");
				return;
			}

		}
		closeConnection(c);
		
		//Serialization...
		System.out.println("CONCEPTS TO SERIALIZE: " 
				+ uriToConcept.values().size());

		OntModel interModel = KbModelFactory.getModel();
		OntModel newConcModel = KbModelFactory.getModel();
		
		OntProperty narrowProp = concModel.getOntProperty(Conf.CONCEPTSCHEMA_NS + "narrowMatch");
		OntProperty broaderProp = concModel.getOntProperty(Conf.CONCEPTSCHEMA_NS + "broaderMatch");
		OntProperty eqsynProp = concModel.getOntProperty(Conf.CONCEPTSCHEMA_NS + "eqsynMatch");
		OntProperty fuzzyProp = concModel.getOntProperty(Conf.CONCEPTSCHEMA_NS + "fuzzyMatch");
		OntProperty cohypoProp = concModel.getOntProperty(Conf.CONCEPTSCHEMA_NS + "cohypoMatch");
		
		OntClass conceptClass = concModel.getOntClass(Conf.CONCEPT_CLASS);
		Collection<Concept> concepts = uriToConcept.values();
		
		//Create "Concept" objects definitions and add ontological links
		int counter = 0;
		for(Iterator<Concept> i = concepts.iterator(); i.hasNext(); ) {
			Concept conc = i.next();
			Resource concRes = newConcModel.createOntResource(conc.getURI());
			counter++;
			newConcModel.add(concRes, RDF.type, conceptClass);
			//Ontological Classes
			for(Iterator<OntoClass> o = conc.getLinks().iterator(); o.hasNext();) {
				OntoClass oc = o.next();
				OntClass classRes = concModel.getOntClass(oc.getURI());
				if(classRes == null) {
					System.err.println("Ontological class is null: " + oc.getURI());
					continue;
				}
				newConcModel.add(concRes, RDF.type, classRes);
			}
		}
		System.out.println("ADDED CONCEPTS: " + counter);
		
		//Add interlinguistic relations
		for(Iterator<Concept> i = concepts.iterator(); i.hasNext(); ) {
			Concept c1 = i.next();
			OntResource res1 = newConcModel.getOntResource(c1.getURI());
			if(res1 == null) {
				System.err.println("Unable to retrieve " + c1.getURI());
			}
			for(Iterator<Concept> k = c1.getHyperConcepts().iterator(); k.hasNext();) {
				Concept c2 = k.next();
				OntResource res2 = newConcModel.getOntResource(c2.getURI());
				if(res2 == null) {
					System.err.println("Unable to retrieve " + c2.getURI());
				}
				interModel.add(res1, narrowProp, res2);
				interModel.add(res2, broaderProp, res1);
			}
			for(Iterator<Concept> k = c1.getCohypoConcepts().iterator(); k.hasNext();) {
				Concept c2 = k.next();
				OntResource res2 = newConcModel.getOntResource(c2.getURI());
				interModel.add(res1, cohypoProp, res2);
				interModel.add(res2, cohypoProp, res1);
			}
			for(Iterator<Concept> k = c1.getEqsynConcepts().iterator(); k.hasNext();) {
				Concept c2 = k.next();
				OntResource res2 = newConcModel.getOntResource(c2.getURI());
				interModel.add(res1, eqsynProp, res2);
				interModel.add(res2, eqsynProp, res1);
			}
			for(Iterator<Concept> k = c1.getFuzzyConcepts().iterator(); k.hasNext();) {
				Concept c2 = k.next();
				OntResource res2 = newConcModel.getOntResource(c2.getURI());
				interModel.add(res1, fuzzyProp, res2);
			}
		}
		
		
		String fileName = dataDir + File.separatorChar + Conf.LINKS;
		Util.serialize(newConcModel, fileName);
		
		fileName = dataDir + File.separatorChar + Conf.INTERCONCEPTS;
		Util.serialize(interModel, fileName);
		
		fileName = dataDir + File.separatorChar + "EN" + File.separatorChar + Conf.LEXICALIZATION;
		Util.serialize(enLexModel, fileName);
		fileName = dataDir + File.separatorChar + "ES" + File.separatorChar + Conf.LEXICALIZATION;
		Util.serialize(esLexModel, fileName);
		fileName = dataDir + File.separatorChar + "IT" + File.separatorChar + Conf.LEXICALIZATION;
		Util.serialize(itLexModel, fileName);
		fileName = dataDir + File.separatorChar + "NL" + File.separatorChar + Conf.LEXICALIZATION;
		Util.serialize(nlLexModel, fileName);
		
	}
	
	private void addNewLexicalization(Concept conc, Synset syn, String lang) {
		
		OntProperty lexProp = concModel.getOntProperty(Conf.LEXICALIZATION_PROP);

		OntModel lexModel = null;
		OntModel lexiconModel = null;
		if(lang.equalsIgnoreCase("en")) {
			lexModel = enLexModel;
			lexiconModel = enLexicon;
		} else if(lang.equalsIgnoreCase("es")) {
			lexModel = esLexModel;
			lexiconModel = esLexicon;
		} else if(lang.equalsIgnoreCase("it")) {
			lexModel = itLexModel;
			lexiconModel = itLexicon;
		} else if(lang.equalsIgnoreCase("nl")) {
			lexModel = nlLexModel;
			lexiconModel = nlLexicon;
		} else {
			System.err.println("addNewLexicalization() - lang not found: " + lang);
			return;
		}
		
		Resource concRes = concModel.getResource(conc.getURI());
		Resource synRes = lexiconModel.getResource(syn.getURI());
		lexModel.add(concRes, lexProp, synRes);
	}
	
	private boolean addInterRelation(Concept c1, Concept c2, String relName) {
		
		if(relName.equalsIgnoreCase("hypernymy")) {
			c1.addHyperConcept(c2);
		} else if(relName.equalsIgnoreCase("co-hyponymy")) {
			c1.addCohypoConcept(c2);
		} else if(relName.equalsIgnoreCase("fuzzynym")) {
			c1.addFuzzyConcept(c2);
		} else if(relName.equalsIgnoreCase("eq_synonym")) {
			c1.addEqsynConcept(c2);
		} else {
			System.err.println("addInterRelation() - " +
					"Relation not found: " + relName + " !");
			return false;
		}
		return true;
	}
	
	private void initConcepts() {
		
		OntClass conceptClass = concModel.getOntClass(Conf.CONCEPT_CLASS);
		if(conceptClass == null) {
			System.err.println("ERROR! initConcepts() - conceptClass is null");
			return;
		}
		
		for(Iterator i = conceptClass.listInstances(true); i.hasNext(); ) {
			OntResource ores = (OntResource) i.next();
			String puri = ores.getNameSpace() + ores.getLocalName();
			checkArtificialName(ores.getLocalName());
			Concept conc = uriToConcept.get(puri);
			if(conc == null) {
				conc = new Concept();
				conc.setURI(puri);
				uriToConcept.put(puri, conc);
			}
			for(Iterator k = ores.listRDFTypes(true); k.hasNext();) {
				Resource ocRes = (Resource) k.next();
				String uri = ocRes.getNameSpace() + ocRes.getLocalName();
				OntoClass oc = (OntoClass) uriToOntoClass.get(uri); 
				if(oc == null) {
					oc = new OntoClass();
					oc.setURI(uri);
					uriToOntoClass.put(uri, oc);
				}
				conc.addLink(oc);
			}
		}
		
		System.out.println("CONCEPTS INITIALIZED: " 
				+ uriToConcept.values().size());
	}
	
	private void addLexicalizations(OntModel lexicon) {
		
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

		int counter = 0;
		StmtIterator si = lexicon.listStatements(null, lexProp, (RDFNode) null);
		for(; si.hasNext(); ) {
			com.hp.hpl.jena.rdf.model.Statement stm = si.nextStatement();
			Resource subj = (Resource) stm.getSubject();
			Resource obj = (Resource) stm.getObject();
			String curi = subj.getNameSpace() + subj.getLocalName();
			String suri = obj.getNameSpace() + obj.getLocalName();
			Concept conc = uriToConcept.get(curi);
			//Prendi l'id del synset
			OntResource objRes = lexicon.getOntResource(obj);
			RDFNode idNode = objRes.getPropertyValue(idProp);
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
			synsetToConcept.put(syn, conc);
			counter++;
		}
		System.out.println("addLexicalizations() - lex added: " + counter);
	}

	private void addSynsets(OntModel lexicon) {
		
		OntClass synsetClass = lexicon.getOntClass(Conf.SYNSET_CLASS);
		if(synsetClass == null) {
			System.err.println("ERROR! addSynsets() - synsetClass is null!");
			return;
		}
		OntProperty idProp = lexicon.getOntProperty(Conf.METALEVEL_ONTO_NS + "synsetId");
		if(idProp == null) {
			System.err.println("ERROR! addSynsets() - idProp is null");
			return;
		}

		int counter = 0;
		for(Iterator i = synsetClass.listInstances(false); i.hasNext(); ) {
			Resource res = (Resource) i.next();
			String suri = res.getNameSpace() + res.getLocalName();
			//Prendi l'id del synset
			OntResource synRes = lexicon.getOntResource(res);
			RDFNode idNode = synRes.getPropertyValue(idProp);
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
				counter++;
			}

		}
		System.out.println("addSynsets() - syn added: " + counter);
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
	
	private String getNextArtificialName() {
		
		artificialCounter++;
		String code = "0000000000";
		String num = String.valueOf(artificialCounter);
		code += num;
		code = code.substring(num.length());
		return artificialPrefix + code;
	}
	
	private void checkArtificialName(String name) {
		
		if(name.indexOf(artificialPrefix) == 0) {
			String code = name.substring(artificialPrefix.length());
			try {
				int num = Integer.valueOf(code);
				if(num > artificialCounter) {
					artificialCounter = num;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.err.println("CODE: " + code);
			}
		}
	}
	
	private Concept getArtificialConcept() {
		
		String name = getNextArtificialName();
		String uri = Conf.DALOS_NS + Conf.CONCEPTS + "#" + name;
		OntClass artClass = concModel.getOntClass(uri);
		if(artClass != null) {
			System.err.println("artConcept already exist! uri: " + uri);
			return null;
		}
		Concept conc = uriToConcept.get(uri);
		if(conc != null) {
			System.err.println("Concept obj already exist! uri: " + uri);
			return null;		
		}
		conc = new Concept();
		conc.setURI(uri);
		uriToConcept.put(uri, conc);
		OntClass conceptClass = concModel.getOntClass(Conf.CONCEPT_CLASS);
		OntResource concRes = concModel.createOntResource(conc.getURI());
		concModel.add(concRes, RDF.type, conceptClass);
		return conc;
	}
	
}
