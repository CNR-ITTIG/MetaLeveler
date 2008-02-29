package it.cnr.ittig.bacci.classifier;

import it.cnr.ittig.bacci.classifier.resource.BasicResource;
import it.cnr.ittig.bacci.classifier.resource.ConceptClass;
import it.cnr.ittig.bacci.classifier.resource.OntologicalClass;
import it.cnr.ittig.bacci.util.Conf;
import it.cnr.ittig.bacci.util.KbModelFactory;

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
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataManager {
	
	//Sorted Data
	private Collection<BasicResource> resources;	
	private Collection<OntologicalClass> classes;
	
	//private Collection<ConceptClass> concepts;
	
	//Maps
	private Map<String,BasicResource> uriToResource;
	private Map<String,OntologicalClass> uriToClass;
	private Map<String,ConceptClass> uriToConcept;
	
	//OntModels
	private OntModel fullModel;
	private OntModel conceptModel;
	private OntModel typeModel;
	
	private OntProperty containsProperty;
	private OntProperty wordProperty;
	private OntProperty lexicalProperty;
	private OntProperty protoProperty;
	
	//Artificial concept class counter
	private static int artificialCounter = 0;
	private static String artificialPrefix = "artconc-";

	public DataManager() {
		
		resources = new TreeSet<BasicResource>();		
		classes = new TreeSet<OntologicalClass>();
		
		//concepts = new HashSet<ConceptClass>();
		
		uriToResource = new HashMap<String, BasicResource>(1024, 0.70f);
		uriToClass = new HashMap<String, OntologicalClass>(128, 0.70f);
		uriToConcept = new HashMap<String, ConceptClass>(1024, 0.70f);
		
		initDocuments();
		
		initData();
		System.out.println("Data initialized! r:" + resources.size() +
				" cc:" /* + concepts.size()*/ + " oc:" + classes.size());
		
		conceptModel = KbModelFactory.getModel();
		KbModelFactory.addImport(conceptModel,
				"http://localhost/runtime.owl", 
				Conf.DOMAIN_ONTO);
		typeModel = KbModelFactory.getModel();
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
		
		String fileName = Conf.DATA_DIRECTORY + File.separatorChar + Conf.CONCEPTS;
		serialize(conceptModel, fileName);
		
		fileName = Conf.DATA_DIRECTORY + File.separatorChar + Conf.TYPES;
		serialize(typeModel, fileName);
	}
	
	private void fill() {
		
		//Create main "Concept" class
		conceptModel.createClass(Conf.conceptClassName);
		OntClass conceptClass = conceptModel.getOntClass(Conf.conceptClassName);
		
		//Create a class for each ConceptClass object
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
				OntClass domainClass = conceptModel.getOntClass(oc.getURI());
				if(domainClass == null) {
					System.err.println("Domain class is null! dc:" + domainClass);
					continue;
				}
				conceptModel.add(ccClass, RDFS.subClassOf, domainClass);
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
	
		String workDir = Conf.DATA_DIRECTORY + File.separatorChar;
		
		KbModelFactory.addDocument(Conf.CONCEPTS, workDir + Conf.CONCEPTS);
		KbModelFactory.addDocument(Conf.TYPES, workDir + Conf.TYPES);
		KbModelFactory.addDocument(Conf.IND, workDir + Conf.IND);
		KbModelFactory.addDocument(Conf.INDW, workDir + Conf.INDW);
	}
	
	private void initData() {
		
		//Con reasoner
		OntModel lexModel = KbModelFactory.getModel(
				"dalos.lexicon", "micro");
		containsProperty = lexModel.getOntProperty(
				Conf.METALEVEL_ONTO_NS + "containsWordSense");
		wordProperty = lexModel.getOntProperty(
				Conf.METALEVEL_ONTO_NS + "word");
		lexicalProperty = lexModel.getOntProperty(
				Conf.METALEVEL_ONTO_NS + "lexicalForm");
		protoProperty = lexModel.getOntProperty(
				Conf.METALEVEL_ONTO_NS + "protoForm");
		
		OntClass synsetClass = lexModel.getOntClass(Conf.synsetClassName);
		if(synsetClass == null) {
			System.err.println("Synset class is null!");
			return;
		}
		
		//List synset individuals
		for(ExtendedIterator i = synsetClass.listInstances(false); i.hasNext(); ) {
			OntResource res = (OntResource) i.next();
			if(res.isAnon()) {
				continue;
			}
			String resNs = res.getNameSpace();
			String resName = res.getLocalName();
			
			BasicResource br = getBasicResource(resNs, resName);
			addVariants(res, br);
			//Add now - sorting depends on variants
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
			if(!ns.equalsIgnoreCase(Conf.DOMAIN_ONTO_NS)) {
				continue;
			}
			getOntologicalClass(ns, name);			
		}
		
		OntClass conceptClass = fullModel.getOntClass(Conf.conceptClassName);
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
				if(!supNs.equalsIgnoreCase(Conf.DOMAIN_ONTO_NS)) {
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
//			concepts.add(cc);
		}

		return cc;
	}

	private BasicResource getBasicResource(String ns, String name) {
		
		String uri = ns + name;
		BasicResource br = uriToResource.get(uri);
		if(br == null) {
			br = new BasicResource();
			br.setURI(uri);
			br.setLexicalForm(name);
			uriToResource.put(uri, br);
		}

		return br;
	}
	
	private void addVariants(OntResource ores, BasicResource br) {
		
		for(ExtendedIterator k = ores.listPropertyValues(containsProperty); 
				k.hasNext();) {
			OntResource ws = (OntResource) k.next();
			OntResource w = (OntResource) ws.getPropertyValue(wordProperty);
			RDFNode protoNode = w.getPropertyValue(protoProperty);
			if(protoNode != null) {
				br.setLexicalForm(((Literal) protoNode).getString());
			} else {
				System.err.println(">> synset without proto form! ores:" + ores);
			}
			for(ExtendedIterator l = w.listPropertyValues(lexicalProperty);
					l.hasNext(); ) {
				RDFNode lexNode = (RDFNode) l.next();
				String lexForm = ((Literal) lexNode).getString();
				br.addVariant(lexForm);
			}
		}
	}

	ConceptClass addArtificialConceptClass(BasicResource br) {
		
		String name = getNextArtificialName();
		String uri = Conf.DALOS_CONCEPTS_NS + name;
		OntClass artClass = fullModel.getOntClass(uri);
		if(artClass != null) {
			System.err.println("artClass already exist! uri: " + uri);
			return null;
		}
		
		ConceptClass cc = getConceptClass(Conf.DALOS_CONCEPTS_NS, name);
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
		
		String prefix = Conf.DALOS_CONCEPTS_NS + artificialPrefix;
		if(cc.getURI().indexOf(prefix) == 0 &&
				cc.getClasses().size() < 1 ) {
			return true;
		}
		return false;
	}
}
