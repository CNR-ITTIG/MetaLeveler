package it.cnr.ittig.bacci.converter;

import it.cnr.ittig.bacci.converter.objects.Concept;
import it.cnr.ittig.bacci.converter.objects.OntoClass;
import it.cnr.ittig.bacci.converter.objects.Synset;
import it.cnr.ittig.bacci.util.Conf;
import it.cnr.ittig.bacci.util.KbModelFactory;
import it.cnr.ittig.bacci.util.Util;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Converter {
	
	private Map<String,OntoClass> uriToOntoClass = null;
	
	private Set<Concept> links = null;
	
	private String dataDir = null;
	
	public Converter() {
		
		uriToOntoClass = new HashMap<String, OntoClass>();
		links = new HashSet<Concept>();
		
		initEnv();
	}
	
	private void initEnv() {
		
		File dataDirFile = new File(".");
		dataDir = dataDirFile.getAbsolutePath();
		Util.initDocuments(dataDir);
	}
	
	public void run() {
		
		//crea il modello leggendo i vecchi concept		
		//crea gli oggetti in memoria
		initConcepts();		
		
		OntModel linksModel = KbModelFactory.getModel();

		//serializza i concept secondo il nuovo formato
		serializeConcepts(linksModel);
		serializeLexicalizations(linksModel);
		
	}
	
	private void initConcepts() {
		
		OntModel oldConceptModel = KbModelFactory.getModel(
			"dalos.ontoconcepts");

		OntClass conceptClass = oldConceptModel.getOntClass(
					Conf.conceptClassName);
		if(conceptClass == null) {
			System.err.println("ERROR! initConcepts() - conceptClass is null");
			return;
		}
		
		for(Iterator i = conceptClass.listSubClasses(); i.hasNext(); ) {
			OntResource ores = (OntResource) i.next();
			String puri = ores.getNameSpace() + ores.getLocalName();
			Concept conc = new Concept();
			conc.setURI(puri);
			links.add(conc);
			
			for(StmtIterator k = oldConceptModel.listStatements(
					(Resource) ores, RDFS.subClassOf, (RDFNode) null);
				k.hasNext();) {
				Statement stm = k.nextStatement();
				Resource obj = (Resource) stm.getObject();
				String objNS = obj.getNameSpace();
				String objName = obj.getLocalName();
				
				//if(!objNS.equalsIgnoreCase(Conf.DOMAIN_ONTO_NS)) {
				//continue;
				//}
				
				String uri = objNS + objName;
				OntoClass oc = (OntoClass) uriToOntoClass.get(uri); 
				if(oc == null) {
				oc = new OntoClass();
				oc.setURI(uri);
				}
				conc.addLink(oc);
				//oc.addConcept(conc);
				
				//System.out.println("Adding pivot class " + poc + 
				//" to tree class " + toc + " turi:" + turi);
				
				uriToOntoClass.put(uri, oc);			
			}
		}
	}
	
	private void serializeConcepts(OntModel linksModel) {
		
		OntModel metaConcModel = KbModelFactory.getModel(
			"dalos.metaconc");
		OntModel ontoModel = KbModelFactory.getModel(
			"dalos.ontology");
		
		OntClass conceptClass = metaConcModel.getOntClass(
				Conf.CONCEPT_CLASS);
		if(conceptClass == null) {
			System.err.println("ERROR! Converter - conceptClass is null");
			return;
		}
		
		//Create rdf:type relations
		for(Iterator<Concept> i = links.iterator(); i.hasNext(); ) {
			Concept conc = i.next();
			OntResource concRes = linksModel.createOntResource(conc.getURI());
			linksModel.add(concRes, RDF.type, conceptClass);
			
			for(Iterator<OntoClass> o = conc.getLinks().iterator(); o.hasNext();) {
				OntoClass oc = o.next();
				OntClass classRes = ontoModel.getOntClass(oc.getURI());
				linksModel.add(concRes, RDF.type, classRes);
			}
		}
		
		//Create inter-linguistic relations
		
		//Serialize
		String fileName = dataDir + File.separatorChar + Conf.LINKS;
		Util.serialize(linksModel, fileName);
	}	

	private void serializeLexicalizations(OntModel linksModel) {
		
		OntModel lexModel = KbModelFactory.getModel();
		OntModel metaConcModel = KbModelFactory.getModel(
			"dalos.metaconc");
		OntModel indModel = KbModelFactory.getModel(
			"dalos.ind");
		OntModel typesModel = KbModelFactory.getModel(
			"dalos.types");
		OntProperty lexProp = lexModel.getOntProperty(
			Conf.CONCEPTSCHEMA_NS + Conf.LEXICALIZATION_PROP);
	
		OntClass conceptClass = metaConcModel.getOntClass(
				Conf.CONCEPT_CLASS);
		if(conceptClass == null) {
			System.err.println("ERROR! ConverterLex - conceptClass is null");
			return;
		}
		
		//Create
		StmtIterator si = typesModel.listStatements(null, RDF.type, (RDFNode) null); 		
		for(; si.hasNext(); ) {
			Statement stm = si.nextStatement();
			Resource sub = stm.getResource();
			RDFNode obj = stm.getObject();
			if(sub.isAnon() || obj.isAnon()) {
				continue;
			}
			String sUri = sub.getNameSpace() + sub.getLocalName();
			Resource objRes = (Resource) obj;
			String cUri = objRes.getNameSpace() + objRes.getLocalName();
			OntResource synRes = indModel.getOntResource(sUri);
			OntResource concRes = linksModel.getOntResource(cUri);
			lexModel.add(concRes, lexProp, synRes);
		}		
	}
}
