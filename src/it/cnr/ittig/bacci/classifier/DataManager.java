package it.cnr.ittig.bacci.classifier;

import it.cnr.ittig.bacci.classifier.gui.Gui;
import it.cnr.ittig.bacci.classifier.resource.BasicResource;
import it.cnr.ittig.bacci.classifier.resource.ConceptClass;
import it.cnr.ittig.bacci.classifier.resource.OntologicalClass;
import it.cnr.ittig.bacci.classifier.resource.Synset;
import it.cnr.ittig.bacci.database.DatabaseManager;
import it.cnr.ittig.bacci.util.Conf;
import it.cnr.ittig.bacci.util.KbModelFactory;
import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.leveler.Leveler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataManager {
	
	//Sorted Data
	private Collection<BasicResource> resources;	
	private Collection<OntologicalClass> classes;
	
	private String DATA_DIR = "";
	private String ONTO = "";
	private String ONTO_NS = "";
	private String RES_NS = "";
	private String CONCEPT_CLASS_NAME = "";
	private String CONCEPT_NS = "";
	
	//Maps
	private Map<String,BasicResource> uriToResource;
	private Map<String,OntologicalClass> uriToClass;
	private Map<String,ConceptClass> uriToConcept;
	
	//OntModels
	private OntModel fullModel;
	private OntModel conceptModel;
	private OntModel typeModel;
	private OntModel ontologyModel;
	
	//Artificial concept class counter
	private static int artificialCounter = 0;
	private static String artificialPrefix = "artconc-";

	public DataManager() {
		
		resources = new TreeSet<BasicResource>();		
		classes = new TreeSet<OntologicalClass>();
		
		uriToResource = new HashMap<String, BasicResource>(1024, 0.70f);
		uriToClass = new HashMap<String, OntologicalClass>(128, 0.70f);
		uriToConcept = new HashMap<String, ConceptClass>(1024, 0.70f);
	}
	
	public boolean init() {
				
		initEnv();
		initDocuments();		
		initData();
		
		System.out.println("Data initialized! r:" + resources.size() +
				" cc:" /* + concepts.size()*/ + " oc:" + classes.size());
		
		conceptModel = KbModelFactory.getModel();
		ontologyModel = KbModelFactory.getModel();
		
		KbModelFactory.readSchema(ontologyModel, ONTO, true);			

		typeModel = KbModelFactory.getModel();
		
		return true;
	}
	
	public Collection<OntologicalClass> getClasses() {
		return classes;
	}

	public Collection<BasicResource> getResources() {
		return resources;
	}
	
	public Collection<OntologicalClass> getClasses(BasicResource br) {
		
		ConceptClass cc = br.getConcept();
		Set<OntologicalClass> data = new TreeSet<OntologicalClass>();
		if(cc != null) {
			data.addAll(cc.getClasses());
		}		
		return data;
	}
	
	public Collection<BasicResource> getResources(OntologicalClass oc) {
		return oc.getResources();
	}
	
	public Collection<BasicResource> getLinkedResources() {
	
		Set<BasicResource> data = new TreeSet<BasicResource>();		
		for(Iterator<BasicResource> i = resources.iterator(); i.hasNext(); ) {
			BasicResource br = i.next();
			ConceptClass cc = br.getConcept();
			if(cc != null && cc.getClasses().size() > 0) {
				data.add(br);
			}
		}
		return data;
	}
	
	public Collection<BasicResource> getUnlinkedResources() {
		
		Set<BasicResource> data = new TreeSet<BasicResource>();		
		for(Iterator<BasicResource> i = resources.iterator(); i.hasNext(); ) {
			BasicResource br = i.next();
			ConceptClass cc = br.getConcept();
			if(cc == null || cc.getClasses().size() == 0) {
				data.add(br);
			}
		}
		return data;
	}
	
	public Collection<BasicResource> getCandidateResources() {
		
		Set<BasicResource> data = new TreeSet<BasicResource>();		
		if(Conf.WORDNET_DATA) {
			for(Iterator<BasicResource> i = resources.iterator(); 
					i.hasNext(); ) {
				BasicResource br = i.next();
				if(SynsetUtil.isCandidateResource(br)) {
					data.add(br);
				}
			}
		}
		return data;
	}
	
	public boolean addClass(BasicResource br, OntologicalClass oc) {
		
		System.out.println("++++ br:" + br + " oc:" + oc);
		ConceptClass cc = br.getConcept();
		if(cc == null) {
			//Add artificial concept
			cc = addArtificialConceptClass(br);
		}		cc.addClass(oc);
		
		if(!oc.addResource(br)) {
			return false;
		}

		return true;
	}

	public boolean removeClass(BasicResource br, OntologicalClass oc) {
		
		System.out.println("---- br:" + br + " oc:" + oc);
		ConceptClass cc = br.getConcept();
		if(cc != null) {
			cc.removeClass(oc);
		} else {
			System.err.println("REMOVE ERROR! Concept class not found! br:" 
					+ br);
			return false;
		}

		if(!oc.removeResource(br)) {
			return false;
		}
		
		return true;
	}
	
	public void save() {

		fill();
		
		String fileName = DATA_DIR + File.separatorChar + Conf.CONCEPTS;
		serialize(conceptModel, fileName);
		
		fileName = DATA_DIR + File.separatorChar + Conf.TYPES;
		serialize(typeModel, fileName);
	}
	
	private void fill() {
		
		//Create main "Concept" class
		conceptModel.createClass(CONCEPT_CLASS_NAME);
		OntClass conceptClass = conceptModel.getOntClass(CONCEPT_CLASS_NAME);
		
		//Create a class for each ConceptClass object
		
		//Link resources to concept class
		for(Iterator<BasicResource> i = resources.iterator(); i.hasNext(); ) {
			BasicResource br = i.next();
			OntResource brRes = typeModel.createOntResource(br.getURI());
			ConceptClass cc = br.getConcept();
			if(cc == null) {
				continue;
			}
			if(isEmptyArtificial(cc)) {
				continue;
			}
			OntClass ccClass = conceptModel.createClass(cc.getURI());

			typeModel.add(brRes, RDF.type, ccClass);			
			conceptModel.add(ccClass, RDFS.subClassOf, conceptClass);
			for(Iterator<OntologicalClass> k = cc.getClasses().iterator();
					k.hasNext(); ) {
				OntologicalClass oc = k.next();
				OntClass domainClass = ontologyModel.getOntClass(oc.getURI()); //TODO DA CONCEPTMODEL DEVE PRENDERE LA DOMAIN CLASS ????
				if(domainClass == null) {
					System.err.println("Domain class is null! dc:" + domainClass);
					continue;
				}
				conceptModel.add(ccClass, RDFS.subClassOf, domainClass);
			}
		}		
		//Add empty (but not artificial!) concept classes
		Collection<ConceptClass> concepts = uriToConcept.values();
		for(Iterator<ConceptClass> i = concepts.iterator(); i.hasNext(); ) {
			ConceptClass cc = i.next();
			if(isEmptyArtificial(cc)) {
				continue;
			}
			OntClass ccClass = conceptModel.getOntClass(cc.getURI());
			if(ccClass == null) {
				ccClass = conceptModel.createClass(cc.getURI());
				conceptModel.add(ccClass, RDFS.subClassOf, conceptClass);
				for(Iterator<OntologicalClass> k = cc.getClasses().iterator();
						k.hasNext(); ) {
					OntologicalClass oc = k.next();
					OntClass domainClass = conceptModel.getOntClass(oc.getURI());
					if(domainClass == null) {
						System.err.println("Domain class is null! dc:" + domainClass);
						continue;
					}
					conceptModel.add(ccClass, RDFS.subClassOf, domainClass);
				}
			}
		}		
	}
	
	private void serialize(OntModel om, String fileName) {
		
		RDFWriter writer = om.getWriter("RDF/XML"); //faster than RDF/XML-ABBREV		
		File outputFile = new File(fileName);
		String relativeOutputFileName = "file://" + outputFile.getAbsolutePath();

		System.out.println("Serializing ontology model to " + outputFile + "...");
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

	private void initDocuments() {
	
		String workDir = DATA_DIR + File.separatorChar;
		
		File file = new File(workDir + Conf.CONCEPTS);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.CONCEPTS, workDir + Conf.CONCEPTS);
		}
		file = new File(workDir + Conf.TYPES);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.TYPES, workDir + Conf.TYPES);
		}
		file = new File(workDir + Conf.IND);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.IND, workDir + Conf.IND);
		}
		file = new File(workDir + Conf.INDW);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.INDW, workDir + Conf.INDW);
		}
	}
	
	private void initData() {
		
		//Con reasoner
		OntModel lexModel = KbModelFactory.getModel(
				"dalos.lexicon", "micro");
		if(Conf.WORDNET_DATA) {
			SynsetUtil.setModel(lexModel);
		}

		OntClass synsetClass = lexModel.getOntClass(Conf.synsetClassName);
		if(synsetClass == null) {
			System.err.println("Synset class is null!");
			return;
		}
		
		//List individuals
		for(ExtendedIterator i = synsetClass.listInstances(false); i.hasNext(); ) {
			OntResource res = (OntResource) i.next();
			if(res.isAnon()) {
				continue;
			}
			String resNs = res.getNameSpace();
			String resName = res.getLocalName();
			
			BasicResource br = getBasicResource(resNs, resName);
			if(Conf.WORDNET_DATA) {
				SynsetUtil.addVariants(res, br);				
			}
			resources.add(br);
		}
		System.out.println("Added resources: " + resources.size());

		lexModel = null;
		
		//Senza reasoner
		fullModel = KbModelFactory.getModel("dalos.full", "");
		
		//List Ontology classes
		for(ExtendedIterator ci = fullModel.listClasses(); ci.hasNext();) {
			OntClass item = (OntClass) ci.next();
			if(item.isAnon()) {
				continue;
			}
			String ns = item.getNameSpace();
			String name = item.getLocalName();
			if(!ns.equalsIgnoreCase(ONTO_NS)) {
				continue;
			}
			getOntologicalClass(ns, name);			
		}
		
		OntClass conceptClass = fullModel.getOntClass(CONCEPT_CLASS_NAME);
		if(conceptClass == null) {
			System.err.println("Concept class is null!");
			return;
		}
		
		//List and create concept classes
		for(ExtendedIterator i = conceptClass.listSubClasses(true);
				i.hasNext();) {
			OntClass item = (OntClass) i.next();
			if(item.isAnon()) {
				continue;
			}
			String ccNs = item.getNameSpace();
			String ccName = item.getLocalName();
			checkArtificialName(ccName);
			getConceptClass(ccNs, ccName);
		}
		
		//Set concept classes in basic resources
		for(ExtendedIterator i = conceptClass.listSubClasses(true);
				i.hasNext();) {
			OntClass item = (OntClass) i.next();
			if(item.isAnon()) {
				continue;
			}
			String uri = item.getNameSpace() + item.getLocalName();
			ConceptClass cc = uriToConcept.get(uri);
			if(cc == null) {
				System.err.println("## ERROR ## null cc for: " + uri);
				continue;
			}
			//For each concept class, get concept instances
			for(ExtendedIterator k = item.listInstances(true);
					k.hasNext(); ) {
				OntResource res = (OntResource) k.next();
				if(res.isAnon()) {
					continue;
				}
				String resNs = res.getNameSpace();
				String resName = res.getLocalName();
				
				BasicResource br = getBasicResource(resNs, resName);
				ConceptClass previousCc = br.getConcept();
				if(previousCc != null && previousCc != cc) {
					System.err.println("Two concept class error! " +
							"res:" + res + " cc:"  + cc +
							" prevCc:" + previousCc + "!");
					//mergeConcept(br, res);
					continue;
				}
				br.setConcept(cc);
			}
			
			//For each concept class, get ontological super classes
			for(ExtendedIterator z = item.listSuperClasses(true);
					z.hasNext(); ) {
				OntClass sup = (OntClass) z.next();
				if(sup.isAnon()) {
					continue;
				}
				String supNs = sup.getNameSpace();
				String supName = sup.getLocalName();
				if(!supNs.equalsIgnoreCase(ONTO_NS)) {
					continue;
				}
				OntologicalClass oc = getOntologicalClass(supNs, supName);
				cc.addClass(oc);
			}
		}
		
		//Set basic resources in ontological class
		for(Iterator<BasicResource> i = resources.iterator(); 
			i.hasNext();) {
			BasicResource br = i.next();
			ConceptClass cc = br.getConcept();
			if(cc == null) {
				continue;
			}
			for(Iterator<OntologicalClass> k = cc.getClasses().iterator();
					k.hasNext(); ) {
				OntologicalClass oc = k.next();
				oc.addResource(br);
			}
		}
		
		//Look for external mapping?
		if(Conf.EXTERNAL_MAPPING) {
			System.out.println("Processing external mapping...");
			XlsMappingImporter.classify(this);
		}
	}
	
	private OntologicalClass getOntologicalClass(String ns, String name) {
		
		String uri = ns + name;
		OntologicalClass oc = uriToClass.get(uri);
		if(oc == null) {
			oc = new OntologicalClass();
			oc.setURI(uri);
			oc.setLexicalForm(name);
			uriToClass.put(uri, oc);
			classes.add(oc);
		}

		return oc;
	}

	private ConceptClass getConceptClass(String ns, String name) {
		
		String uri = ns + name;
		ConceptClass cc = uriToConcept.get(uri);
		if(cc == null) {
			cc = new ConceptClass();
			cc.setURI(uri);
			cc.setLexicalForm(name);
			uriToConcept.put(uri, cc);
		}

		return cc;
	}

	private BasicResource getBasicResource(String ns, String name) {
		
		String uri = ns + name;
		BasicResource br = uriToResource.get(uri);
		if(br == null) {
			if(Conf.WORDNET_DATA) {
				br = new Synset();				
			} else {
				br = new BasicResource();
			}
			br.setURI(uri);
			br.setLexicalForm(name);
			uriToResource.put(uri, br);
		}

		return br;
	}
	
	ConceptClass addArtificialConceptClass(BasicResource br) {
		
		String name = getNextArtificialName();
		String uri = CONCEPT_NS + name;
		OntClass artClass = fullModel.getOntClass(uri);
		if(artClass != null) {
			System.err.println("artClass already exist! uri: " + uri);
			return null;
		}
		
		ConceptClass cc = getConceptClass(CONCEPT_NS, name);
		br.setConcept(cc);
		return cc;
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
	
	private boolean isEmptyArtificial(ConceptClass cc) {
		
		String prefix = CONCEPT_NS + artificialPrefix;
		if(cc.getURI().indexOf(prefix) == 0 &&
				cc.getClasses().size() < 1 ) {
			return true;
		}
		return false;
	}
	
	private void initEnv() {
		
		DATA_DIR = (String) Gui.appProperties.getProperty("resDir");
		ONTO = (String) Gui.appProperties.getProperty("ontoText");
		ONTO_NS = (String) Gui.appProperties.getProperty("ontoNs");
		RES_NS = (String) Gui.appProperties.getProperty("resNs");
		CONCEPT_NS = RES_NS + Conf.CONCEPTS + "#";
		CONCEPT_CLASS_NAME = CONCEPT_NS + "Concept";		
	}
	
	public void processDb(DatabaseManager dbm) {
		//Crea gli oggetti importando i dati del database;
		//Salvali in RDF: si devono salvare soltanto i file 
		//individuals e individuals-word !
		
		initEnv();
		
		EditorConf.onto_concepts = RES_NS + Conf.CONCEPTS;
		EditorConf.onto_ind = RES_NS + "IT/" + Conf.IND;
		EditorConf.onto_indw = RES_NS + "IT/" + Conf.INDW;
		EditorConf.onto_types = RES_NS + "IT/" + Conf.TYPES;
		EditorConf.onto_sources = RES_NS + "IT/" + Conf.SOURCES;
		EditorConf.domainOntoModel = ONTO;
		EditorConf.domainOntoModelNs = ONTO_NS;
		
		EditorConf.DIVIDE = false;
		EditorConf.ADD_ALIGNMENT = false;
		EditorConf.LINK_TO_ONTOLOGY = false;
		EditorConf.USE_JENA_DB = false;
		EditorConf.ONLY_LEXICON = true;
		EditorConf.DATA_DIR = DATA_DIR;
		EditorConf.LANGUAGE = "IT";
		EditorConf.TYPE_INPUT = "ittig";
		EditorConf.DBM = dbm;
		
		Leveler.main(null);
	}
}
