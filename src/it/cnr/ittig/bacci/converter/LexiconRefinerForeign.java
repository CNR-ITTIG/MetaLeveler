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
import it.cnr.ittig.jwneditor.editor.EditorConf;

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

public class LexiconRefinerForeign {

	/*
	 * Aggiunge le varianti per l'italiano collegandosi
	 * ad un file .mdb; IND, INDW
	 */
	
	private String mdbFileName = EditorConf.MDB_FILE_NAME;
	
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
	
	public LexiconRefinerForeign() {
		
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

		//serialize
		String fileName = dataDir + File.separatorChar + Conf.INDW;
		Util.serialize(indwModel, fileName);

	}
	
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
	
	private void readDb() {
		
		System.out.println("synsets size: " + idToSynset.values().size());
		
		//LEGGI DATABASE
		Connection c = openConnection();		
		
		String sql = "select TE_LG_IdLanguage, TE_IdTerm, TE_SurfaceForm, TE_Lemma from TD_Terms";	

		//Collection<Synset> dbSynsets = new HashSet<Synset>();
		
		Vector<String[]> results = eseguiQuery(c, sql);

		int nullCounter = 0;
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();

			String lang = row[0].trim();
			String kwid = row[1].trim();
			String proto = row[2].trim();
			String variant = row[3].trim();
						
			if( ! lang.equalsIgnoreCase("NL")) {
				continue;
			}
			
			Synset syn = idToSynset.get(kwid);
			if(syn == null) {
				System.err.println("Null syn for kwid: " + kwid);
				nullCounter++;
				continue;
			}
			
			if(proto.equalsIgnoreCase(variant)) {
				continue;
			}
			
			//Aggiungi variante
			
			//Prendi il syn dal model
			OntResource synRes = model.getOntResource(syn.getURI());
			OntResource modelWsRes = (OntResource) synRes.getPropertyValue(wsProp);
			String wsUri = modelWsRes.getNameSpace() + modelWsRes.getLocalName();
			
			//Prendi il wordsense dall'indwModel
			Resource wsRes = indwModel.getResource(wsUri);
			com.hp.hpl.jena.rdf.model.Statement wStmt = wsRes.getProperty(wProp);			
			Resource wRes = (Resource) wStmt.getObject();
			
			//Aggiungi la variante
			Literal lit = indwModel.createLiteral(variant);
			indwModel.add(wRes, lexFormProp, lit);			
			
			//dbSynsets.add(syn);
		}		
		System.err.println("NULL COUNTER:" + nullCounter);
		
		closeConnection(c);
		
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
