package it.cnr.ittig.bacci.converter;

import it.cnr.ittig.bacci.converter.objects.Concept;
import it.cnr.ittig.bacci.converter.objects.Lemma;
import it.cnr.ittig.bacci.converter.objects.OntoClass;
import it.cnr.ittig.bacci.converter.objects.Source;
import it.cnr.ittig.bacci.converter.objects.Synset;
import it.cnr.ittig.bacci.converter.objects.Word;
import it.cnr.ittig.bacci.util.Conf;
import it.cnr.ittig.bacci.util.KbModelFactory;
import it.cnr.ittig.bacci.util.Util;

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
import java.util.Vector;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class LexiconRefiner {

	/*
	 * Aggiunge le varianti per l'italiano collegandosi
	 * ad un file .mdb; IND, INDW
	 */
	
	private String mdbFileName = "dalos_dir+dec_con_np_ita_final.mdb";
	
	private String dataDir = null;
	
	private OntModel model;
	
	private OntModel indwModel;

	private Map<String,Synset> idToSynset = null;	
	private Map<String,Synset> uriToSynset = null;
	
	private OntProperty idProp = null;
	
	private OntProperty wsProp = null;
	private OntProperty wProp = null;
	
	private OntProperty lexFormProp = null;
	private OntProperty protoFormProp = null;
	
	public LexiconRefiner() {
		
		uriToSynset = new HashMap<String,Synset>();
		idToSynset = new HashMap<String,Synset>();
		
		initEnv();
	}
	
	private void initEnv() {
		
		File dataDirFile = new File(".");
		dataDir = dataDirFile.getAbsolutePath();
		Util.initDocuments(dataDir);
		
		File mdbFile = new File(mdbFileName);
		mdbFileName = mdbFile.getAbsolutePath();
	}
	
	public void refine() {
		
		model = KbModelFactory.getModel("dalos.lexicon", "micro");
		
		indwModel = KbModelFactory.getModel("single.indw");
		
		idProp = model.getOntProperty(Conf.SYNID_PROP);		
		wsProp = model.getOntProperty(Conf.WORDSENSE_PROP);
		wProp = model.getOntProperty(Conf.WORD_PROP);		
		lexFormProp = model.getOntProperty(Conf.LEXFORM_PROP);
		protoFormProp = model.getOntProperty(Conf.PROTOFORM_PROP);	

//		initConcepts();
//		addLexicalizations(model);
		initSynsets();
		
		readDb();
	}
	
//	private void initConcepts() {
//		
//		OntClass conceptClass = model.getOntClass(Conf.CONCEPT_CLASS);
//		if(conceptClass == null) {
//			System.err.println("ERROR! initConcepts() - conceptClass is null");
//			return;
//		}
//		
//		for(Iterator i = conceptClass.listInstances(true); i.hasNext(); ) {
//			OntResource ores = (OntResource) i.next();
//			String puri = ores.getNameSpace() + ores.getLocalName();
//			Concept conc = uriToConcept.get(puri);
//			if(conc == null) {
//				conc = new Concept();
//				conc.setURI(puri);
//				uriToConcept.put(puri, conc);
//			}
//			for(Iterator k = ores.listRDFTypes(true); k.hasNext();) {
//				Resource ocRes = (Resource) k.next();
//				String uri = ocRes.getNameSpace() + ocRes.getLocalName();
//				OntoClass oc = (OntoClass) uriToOntoClass.get(uri); 
//				if(oc == null) {
//					oc = new OntoClass();
//					oc.setURI(uri);
//					uriToOntoClass.put(uri, oc);
//				}
//				conc.addLink(oc);
//			}
//		}
//		
//		System.out.println("CONCEPTS INITIALIZED: " 
//				+ uriToConcept.values().size());
//	}
//	
//	private void addLexicalizations(OntModel lexicon) {
//		
//		OntProperty lexProp = lexicon.getOntProperty(Conf.LEXICALIZATION_PROP);
//		if(lexProp == null) {
//			System.err.println("ERROR! INTER - lexProp is null");
//			return;
//		}
//		OntProperty idProp = lexicon.getOntProperty(Conf.METALEVEL_ONTO_NS + "synsetId");
//		if(idProp == null) {
//			System.err.println("ERROR! INTER - idProp is null");
//			return;
//		}
//
//		int counter = 0;
//		StmtIterator si = lexicon.listStatements(null, lexProp, (RDFNode) null);
//		for(; si.hasNext(); ) {
//			com.hp.hpl.jena.rdf.model.Statement stm = si.nextStatement();
//			Resource subj = (Resource) stm.getSubject();
//			Resource obj = (Resource) stm.getObject();
//			String curi = subj.getNameSpace() + subj.getLocalName();
//			String suri = obj.getNameSpace() + obj.getLocalName();
//			Concept conc = uriToConcept.get(curi);
//			//Prendi l'id del synset
//			OntResource objRes = lexicon.getOntResource(obj);
//			RDFNode idNode = objRes.getPropertyValue(idProp);
//			if(idNode == null) {
//				System.err.println("NO ID for " + suri);
//				return;
//			}
//			String rawId = ((Literal) idNode).getString();
//			rawId = rawId.substring(1); //toglie l'1 iniziale
//			Integer intId = Integer.valueOf(rawId); //toglie gli zeri iniziali
//			String id = String.valueOf(intId); //id finale String
//			//Prendi il synset
//			Synset syn = idToSynset.get(id);
//			if(syn == null) {
//				syn = new Synset();
//				syn.setID(id);
//				syn.setURI(suri);
//				idToSynset.put(id, syn);
//			}			
//			synsetToConcept.put(syn, conc);
//			counter++;
//		}
//		System.out.println("addLexicalizations() - lex added: " + counter);
//	}

	private void initSynsets() {
		
		OntClass synsetClass = model.getOntClass(Conf.SYNSET_CLASS);
		
		for(Iterator i = synsetClass.listInstances(false); i.hasNext();) {
			OntResource synRes = (OntResource) i.next();			
			String uri = synRes.getNameSpace() + synRes.getLocalName();
			Synset syn = uriToSynset.get(uri);
			if(syn == null) {
				syn = new Synset();
				syn.setURI(uri);
				uriToSynset.put(uri, syn);
			}
			
			String id = getSynId(synRes);
			if(idToSynset.get(id) == null) {
				idToSynset.put(id, syn);
			}
			
			//fillSynset(synRes, syn);
		}
	}

	private String getSynId(OntResource synRes) {
		
		if( !synRes.hasProperty(idProp) ) {
			System.err.println("NO ID for " + 
					synRes.getNameSpace() + synRes.getLocalName());
			return "";
		}
		
		RDFNode idNode = synRes.getPropertyValue(idProp);
		//System.out.println("Getting id for " + synRes.getNameSpace() + synRes.getLocalName());
		String rawId = ((Literal) idNode).getString();
		rawId = rawId.substring(1); //toglie l'1 iniziale
		Integer intId = Integer.valueOf(rawId); //toglie gli zeri iniziali
		return String.valueOf(intId); //id finale String
	}
	
//	private void fillSynset(OntResource synRes, Synset syn) {
//		
//		//Sources
//		for(Iterator i = synRes.listPropertyValues(sourceProp); i.hasNext(); ) {
//			Resource res = (Resource) i.next();
//			String suri = res.getNameSpace() + res.getLocalName();
//			
//			Source source = uriToSource.get(suri);
//			if(source == null) {
//				source = new Source();
//				source.setURI(suri);
//				uriToSource.put(suri, source);
//			}
//			
//			syn.addSource(source);
//		}
//
//		//Part of Speech
//		String pos = "";
//		if(synRes.hasRDFType(nounClass, true)) {
//			pos = "N";
//		} else if(synRes.hasRDFType(verbClass, true)) {
//			pos = "V";
//		} else if(synRes.hasRDFType(adverbClass, true)) {
//			pos = "AV";
//		} else if(synRes.hasRDFType(adjectiveClass, true)) {
//			pos = "AG";
//		} else {
//			System.err.println("ERROR! No Synset Class for resource: "
//					+ syn.getURI());
//			return;
//		}			
//		
//		//Id
//		if(synRes.hasProperty(idProp)) {
//			Literal lit = (Literal) synRes.getPropertyValue(idProp);
//			String id = lit.getString();
//			syn.setSynsetId(id);			
//		}
//		
//		//Gloss
//		if(synRes.hasProperty(glossProp)) {
//			Literal lit = (Literal) synRes.getPropertyValue(glossProp);
//			String gloss = lit.getString();
//			syn.setGloss(gloss);			
//		}
//		
//		//Wordsenses & Variants
//		for(Iterator i = synRes.listPropertyValues(wsProp); i.hasNext(); ) {
//			OntResource wsRes = (OntResource) i.next();
//			String uri = wsRes.getNameSpace() + wsRes.getLocalName();
//			Lemma lemma = new Lemma();
//			lemma.setURI(uri);
//			lemma.setPartOfSpeech(pos);
//			syn.add(lemma);
//			lemma.setInSynset(syn);
//			
//			String sense = uri.substring(uri.lastIndexOf('-') + 1);
//			try {
//				Integer senseInt = Integer.valueOf(sense);
//			} catch (NumberFormatException e) {
//				// TODO Auto-generated catch block
//				System.err.println("ERROR getting sense! uri: " 
//						+ uri + " - sense: " + sense);
//				e.printStackTrace();
//				return;
//			}			
//			lemma.setSense(sense);
//			
//			for(Iterator k = wsRes.listPropertyValues(wProp); k.hasNext(); ) {
//				OntResource wRes = (OntResource) k.next();
//				String wuri = wRes.getNameSpace() + wsRes.getLocalName();
//				Word word = new Word();
//				word.setURI(wuri);
//				
//				//Proto Form
//				if(wRes.hasProperty(protoFormProp)) {
//					Literal lit = (Literal) wRes.getPropertyValue(protoFormProp);
//					String proto = lit.getString();
//					word.setProtoForm(proto);
//				}
//				
//				//Variants
//				for(Iterator z = wRes.listPropertyValues(lexFormProp); z.hasNext(); ) {
//					Literal lit = (Literal) z.next();
//					String variant = lit.getString();
//					word.addLexicalForm(variant);
//				}
//			}
//		}
//	}
	
	private void readDb() {
		
		System.out.println("synsets size: " + idToSynset.values().size());
		
		//LEGGI DATABASE
		Connection c = openConnection();		
		
		String sql = "SELECT T1.kwid, T1.forma_variante " +
			"FROM wbt_app_glossario_varianti T1 " +
			"";	

		Collection<Synset> dbSynsets = new HashSet<Synset>();
		
		Vector<String[]> results = eseguiQuery(c, sql);

		int nullCounter = 0;
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();

			String kwid = row[0].trim();
			String variant = row[1].trim();
			
			Synset syn = idToSynset.get(kwid);
			if(syn == null) {
				System.err.println("Null syn for kwid: " + kwid);
				nullCounter++;
				continue;
			}
			
			dbSynsets.add(syn);
		}
		System.err.println("NULL COUNTER:" + nullCounter);
		
		closeConnection(c);
		
		System.out.println("Checking dbSynsets...");
		for(Iterator<Synset> i = idToSynset.values().iterator(); i.hasNext(); ) {
			Synset syn = i.next();
			if( !dbSynsets.contains(syn)) {
				System.out.println("oooo MISSING oooo syn: " + syn.getURI());
			}
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
