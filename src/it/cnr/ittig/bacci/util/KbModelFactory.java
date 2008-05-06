package it.cnr.ittig.bacci.util;

import it.cnr.ittig.bacci.classifier.gui.Gui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.vocabulary.OWL;

public class KbModelFactory {
	
	private static OntDocumentManager odm = 
		OntDocumentManager.getInstance();

	private static Map<String,String> localDocuments = 
		new HashMap<String,String>();	

	public static void addDocument(String fileCode, String fileName) {
		
		String key = fileCode;
		System.out.println("@@ addDocument(): " + key + " -> " + fileName);
		localDocuments.put(key, fileName);
	}
	
	public static void addAltDoc(String origFileName, 
			String localFileName) {
		
		odm.addAltEntry(origFileName, localFileName);
	}

	public static OntModel getModel() {
		/*
		 * Returns an empty in-memory OntModel.
		 */
		
		return getModel("", "", "", null);
	}
	
	public static OntModel getModel(String type) {
		
		return getModel(type, "", "", null);
	}
	
	public static OntModel getModel(String type, String reasoner) {
		
		return getModel(type, reasoner, "", "");
	}
	
	public static OntModel getModel(String type, String reasoner, String lang) {
		
		return getModel(type, reasoner, lang, "");
	}
	
	public static OntModel getModel(String type, String reasoner, 
			String lang, String URI) {
		/*
		 * Ritorna un OntModel in base a varie configurazioni.
		 * type: sceglie i moduli ontologici da caricare
		 * reasoner: sceglie il reasoner da utilizzare nel modello
		 */
		
//		String DATA_DIR = (String) Gui.appProperties.getProperty("resDir");
//		String ONTO_NS = (String) Gui.appProperties.getProperty("ontoNs");
		
		String ONTO = Conf.DALOS_ONTO;
		if(Gui.appProperties != null) {
			ONTO = (String) Gui.appProperties.getProperty("ontoText");			
		}

		ModelMaker maker = ModelFactory.createMemModelMaker();

		OntModelSpec spec = null;
		if(reasoner.length() > 0) {
			Reasoner r = null;
			if(reasoner.equalsIgnoreCase("rdf")) {
				r = ReasonerRegistry.getRDFSReasoner();
			}
			if(reasoner.equalsIgnoreCase("micro")) {
				r = ReasonerRegistry.getOWLMicroReasoner();
			}
			if(reasoner.equalsIgnoreCase("mini")) {
				r = ReasonerRegistry.getOWLMiniReasoner();
			}
			if(reasoner.equalsIgnoreCase("owl")) {
				r = ReasonerRegistry.getOWLReasoner();
			}
			if(reasoner.equalsIgnoreCase("pellet")) {
				//può servire pellet ? oppure un external reasoner?
			}
			if(r == null) {
				System.err.println("getModel() - Reasoner type not found: " + type);
				return null;
			}
			spec =  OntModelSpec.OWL_MEM ;
			spec.setReasoner(r);
		} else {
			spec = new OntModelSpec( OntModelSpec.OWL_MEM );
		}
		spec.setImportModelMaker(maker);
		OntModel om = ModelFactory.createOntologyModel(spec, null);
		
		if(type.equalsIgnoreCase("dalos.full")) {
			readLocalDocument(om, lang, Conf.CONCEPTS);
			readLocalDocument(om, lang, Conf.TYPES);
			readSchema(om, ONTO);
		}
		if(type.equalsIgnoreCase("dalos.lexicon")) {
			readSchema(om, Conf.METALEVEL_ONTO);
			readLocalDocument(om, lang, Conf.IND);
			readLocalDocument(om, lang, Conf.INDW);
			readLocalDocument(om, lang, Conf.TYPES);
		}		
		if(type.equalsIgnoreCase("dalos.lexicon.light")) {
			readSchema(om, Conf.METALEVEL_ONTO);
			readLocalDocument(om, lang, Conf.IND);
			readLocalDocument(om, lang, Conf.TYPES);
		}		
		if(type.equalsIgnoreCase("dalos.ontology")) {
			readSchema(om, ONTO);
		}		
		if(type.equalsIgnoreCase("dalos.concepts")) {
			readLocalDocument(om, lang, Conf.CONCEPTS);
		}
		if(type.equalsIgnoreCase("dalos.ontoconcepts")) {
			readSchema(om, ONTO);
			readSchema(om, Conf.CONCEPT_SCHEMA);
			readLocalDocument(om, lang, Conf.CONCEPTS);
		}
		if(type.equalsIgnoreCase("dalos.metaconc")) {
			readSchema(om, Conf.METALEVEL_ONTO);
			readSchema(om, Conf.CONCEPT_SCHEMA);
		}
		if(type.equalsIgnoreCase("dalos.types")) {
			readLocalDocument(om, lang, Conf.TYPES);
		}
		if(type.equalsIgnoreCase("dalos.ind")) {
			readSchema(om, Conf.METALEVEL_ONTO);
			readLocalDocument(om, lang, Conf.IND);
		}
		//SINGLE LEXICONS
		if(type.equalsIgnoreCase("dalos.lexicon.EN")) {
			readSchema(om, Conf.METALEVEL_ONTO);
			readSchema(om, Conf.CONCEPT_SCHEMA);
			readLocalDocument(om, lang, "EN" + Conf.IND);
			readLocalDocument(om, lang, "EN" + Conf.LEXICALIZATION);
		}		
		if(type.equalsIgnoreCase("dalos.lexicon.ES")) {
			readSchema(om, Conf.METALEVEL_ONTO);
			readSchema(om, Conf.CONCEPT_SCHEMA);
			readLocalDocument(om, lang, "ES" + Conf.IND);
			readLocalDocument(om, lang, "ES" + Conf.LEXICALIZATION);
		}		
		if(type.equalsIgnoreCase("dalos.lexicon.IT")) {
			readSchema(om, Conf.METALEVEL_ONTO);
			readSchema(om, Conf.CONCEPT_SCHEMA);
			readLocalDocument(om, lang, "IT" + Conf.IND);
			readLocalDocument(om, lang, "IT" + Conf.LEXICALIZATION);
		}		
		if(type.equalsIgnoreCase("dalos.lexicon.NL")) {
			readSchema(om, Conf.METALEVEL_ONTO);
			readSchema(om, Conf.CONCEPT_SCHEMA);
			readLocalDocument(om, lang, "NL" + Conf.IND);
			readLocalDocument(om, lang, "NL" + Conf.LEXICALIZATION);
		}		
		odm.setProcessImports(true);
		odm.loadImports(om);
		
		return om;
	}

	private static void readSchema(OntModel om, String url) {
		
		readSchema(om, url, false);
	}
			
	public static void readSchema(OntModel om, String url, boolean useRemote) {
		
		if(url.trim().length() < 1) {
			System.err.println("readSchema() - URL is empty!");
			return;
		}
		if(url.indexOf("http:/") != 0) {
			//Local file ?
			File file = new File(url);
			om.read("file:///" + file.getAbsolutePath());
			return;
		}

		if(useRemote) {
			URL u = null;
			try {
				u = new URL(url);			
				System.out.println("URL: " + u.toString());
				om.read(u.openStream(), null);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("#### URL unreachable! Trying to load local data...");
				String localFile = odm.doAltURLMapping(url);
				System.out.println("localFile: " + localFile);
				om.read(localFile);
			}
		} else {
			String localFile = odm.doAltURLMapping(url);
			if(localFile.trim().length() < 1) {
				System.err.println("readSchema() - Unable to resolve! URL: " 
						+ url + " localFile: " + localFile);			
				return;
			}
			om.read(localFile);
		}
	}

	private static void readLocalDocument(OntModel om, String lang, String fileCode) {
		
		String key = lang + fileCode;
		String fileName = (localDocuments.get(key));
		if(fileName == null) {
			System.err.println("readLocalDocument() - doc not found! key: " + key);
			return;
		}
		File file = new File(fileName);
		om.read("file:///" + file.getAbsolutePath());
	}

	public static void addImport(OntModel om, String source, String dest) {

		Ontology ont = om.createOntology(source); 
		om.add(ont, OWL.imports, om.createResource(dest));
		odm.loadImports(om);
	}
}
