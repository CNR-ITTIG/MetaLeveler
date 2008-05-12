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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class LexiconCleaner {

	/*
	 * elimina i termini italiani che non hanno collegamenti
	 * verso l'ontologia; IND INDW LEXICALIZATIONS
	 */

	private String dataDir = null;
	
	private OntModel model;
	
	private OntModel indModel;
	private OntModel indwModel;
	private OntModel lexModel;
	private OntModel sourceModel;
	
	private Map<String,Concept> uriToConcept = null;
	private Map<Synset,Concept> synsetToConcept = null;
	private Map<String,Synset> idToSynset = null;
	
	private Map<String,Synset> uriToSynset = null;
	private Map<String,Source> uriToSource = null;

	private Map<String,OntoClass> uriToOntoClass = null;
	
	private OntProperty sourceProp = null;
	private OntProperty lexProp = null;
	
	private OntProperty idProp = null;
	private OntProperty glossProp = null;
	
	private OntProperty wsProp = null;
	private OntProperty wProp = null;
	
	private OntProperty lexFormProp = null;
	private OntProperty protoFormProp = null;
	
	private OntClass nounClass = null;
	private OntClass verbClass = null;
	private OntClass adverbClass = null;
	private OntClass adjectiveClass = null;


	public LexiconCleaner() {
		
		uriToConcept = new HashMap<String,Concept>();
		uriToSynset = new HashMap<String,Synset>();
		uriToSource = new HashMap<String,Source>();
		synsetToConcept = new HashMap<Synset,Concept>();
		idToSynset = new HashMap<String,Synset>();
		
		uriToOntoClass = new HashMap<String, OntoClass>();
		
		initEnv();
	}
	
	private void initEnv() {
		
		File dataDirFile = new File(".");
		dataDir = dataDirFile.getAbsolutePath();
		Util.initDocuments(dataDir);		
	}

	public void clean() {

		model = KbModelFactory.getModel("dalos.cleaner", "micro");
		
		indModel = KbModelFactory.getModel("single.ind");
		indwModel = KbModelFactory.getModel("single.indw");
		lexModel = KbModelFactory.getModel("single.lex");
		sourceModel = KbModelFactory.getModel("single.sources");
		
		lexProp = lexModel.getOntProperty(Conf.LEXICALIZATION_PROP);
		sourceProp = lexModel.getOntProperty(Conf.SOURCE_PROP);
		
		idProp = lexModel.getOntProperty(Conf.SYNID_PROP);
		glossProp = lexModel.getOntProperty(Conf.GLOSS_PROP);
		
		wsProp = lexModel.getOntProperty(Conf.WORDSENSE_PROP);
		wProp = lexModel.getOntProperty(Conf.WORD_PROP);
		
		lexFormProp = lexModel.getOntProperty(Conf.LEXFORM_PROP);
		protoFormProp = lexModel.getOntProperty(Conf.PROTOFORM_PROP);	

		nounClass = lexModel.getOntClass(Conf.NOUN_CLASS);
		verbClass = lexModel.getOntClass(Conf.VERB_CLASS);
		adverbClass = lexModel.getOntClass(Conf.ADVERB_CLASS);
		adjectiveClass = lexModel.getOntClass(Conf.ADVERB_CLASS);

		initConcepts();
		addLexicalizations(model);
		initSynsets();
		
		for(Iterator<Synset> i = idToSynset.values().iterator(); i.hasNext(); ) {
			Synset syn = i.next();
			Concept conc = synsetToConcept.get(syn);
			if(conc == null) {
				System.out.println("-- No concept -- Deleting syn: " 
						+ syn.getURI());
				removeSynset(syn);				
			} else {
				if(conc.getLinks().size() == 0) {
					System.out.println("-- No links -- Deleting syn: " 
							+ syn.getURI());
					removeSynset(syn);									
				}
			}
		}
		
		//Serialize
		String fileName = dataDir + File.separatorChar + Conf.IND;
		Util.serialize(indModel, fileName);

		fileName = dataDir + File.separatorChar + Conf.INDW;
		Util.serialize(indwModel, fileName);
		
		fileName = dataDir + File.separatorChar + Conf.LEXICALIZATION;
		Util.serialize(lexModel, fileName);
		
		fileName = dataDir + File.separatorChar + Conf.SOURCES;
		Util.serialize(sourceModel, fileName);
	}
	
	private void removeSynset(Synset syn) {
		
		Collection<OntModel> involvedModels = new HashSet<OntModel>();
		Collection<String> involvedResources = new HashSet<String>();
		
		involvedModels.add(indModel);
		involvedModels.add(indwModel);
		involvedModels.add(lexModel);
		involvedModels.add(sourceModel);
		
		//synset
		involvedResources.add(syn.getURI());
		
		//wordsense
		for(Iterator<Lemma> i = syn.getLemmas().iterator(); i.hasNext(); ) {
			Lemma lemma = i.next();
			involvedResources.add(lemma.getURI());			
			//word
			for(Iterator<Word> k = lemma.getWords().iterator(); k.hasNext(); ) {
				Word word = k.next();				
				involvedResources.add(word.getURI());
			}
		}
				
		//source
		for(Iterator<Source> i = syn.getSources().iterator(); i.hasNext(); ) {
			Source source = i.next();
			involvedResources.add(source.getURI());			
		}
		
		removeInvolved(involvedModels, involvedResources);
	}
	
	private void removeInvolved(Collection<OntModel> models, 
			Collection<String> resourcesUri) {
		
		for(Iterator<OntModel> m = models.iterator(); m.hasNext(); ) {
			OntModel mod = m.next();
			for(Iterator<String> u = resourcesUri.iterator(); u.hasNext(); ) {
				String uri = u.next();
				removeStmt(mod, uri);
			}
		}
	}
	
	private void removeStmt(OntModel mod, String uri) {
		
		Resource res = mod.getResource(uri);
		if(res == null) {
			//That resource does not appear in this model
			return;
		}
		
		mod.removeAll(res, null, null);
		mod.removeAll(null, null, res);
	}
	
	private void initConcepts() {
		
		OntClass conceptClass = model.getOntClass(Conf.CONCEPT_CLASS);
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
			
			fillSynset(synRes, syn);
		}
	}

	private String getSynId(OntResource synRes) {
		
		RDFNode idNode = synRes.getPropertyValue(idProp);
		if(idNode == null) {
			System.err.println("NO ID for " + 
					synRes.getNameSpace() + synRes.getLocalName());
			return "";
		}
		String rawId = ((Literal) idNode).getString();
		rawId = rawId.substring(1); //toglie l'1 iniziale
		Integer intId = Integer.valueOf(rawId); //toglie gli zeri iniziali
		return String.valueOf(intId); //id finale String
	}
	
	private void fillSynset(OntResource synRes, Synset syn) {
		
		//Sources
		for(Iterator i = synRes.listPropertyValues(sourceProp); i.hasNext(); ) {
			Resource res = (Resource) i.next();
			String suri = res.getNameSpace() + res.getLocalName();
			
			Source source = uriToSource.get(suri);
			if(source == null) {
				source = new Source();
				source.setURI(suri);
				uriToSource.put(suri, source);
			}
			
			syn.addSource(source);
		}

		//Part of Speech
		String pos = "";
		if(synRes.hasRDFType(nounClass, true)) {
			pos = "N";
		} else if(synRes.hasRDFType(verbClass, true)) {
			pos = "V";
		} else if(synRes.hasRDFType(adverbClass, true)) {
			pos = "AV";
		} else if(synRes.hasRDFType(adjectiveClass, true)) {
			pos = "AG";
		} else {
			System.err.println("ERROR! No Synset Class for resource: "
					+ syn.getURI());
			return;
		}			
		
		//Id
		if(synRes.hasProperty(idProp)) {
			Literal lit = (Literal) synRes.getProperty(idProp);
			String id = lit.getString();
			syn.setSynsetId(id);			
		}
		
		//Gloss
		if(synRes.hasProperty(glossProp)) {
			Literal lit = (Literal) synRes.getProperty(glossProp);
			String gloss = lit.getString();
			syn.setGloss(gloss);			
		}
		
		//Wordsenses & Variants
		for(Iterator i = synRes.listPropertyValues(wsProp); i.hasNext(); ) {
			OntResource wsRes = (OntResource) i.next();
			String uri = wsRes.getNameSpace() + wsRes.getLocalName();
			Lemma lemma = new Lemma();
			lemma.setURI(uri);
			lemma.setPartOfSpeech(pos);
			syn.add(lemma);
			lemma.setInSynset(syn);
			
			String sense = uri.substring(uri.lastIndexOf('-') + 1);
			try {
				Integer senseInt = Integer.valueOf(sense);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				System.err.println("ERROR getting sense! uri: " 
						+ uri + " - sense: " + sense);
				e.printStackTrace();
				return;
			}			
			lemma.setSense(sense);
			
			for(Iterator k = wsRes.listPropertyValues(wProp); k.hasNext(); ) {
				OntResource wRes = (OntResource) k.next();
				String wuri = wRes.getNameSpace() + wsRes.getLocalName();
				Word word = new Word();
				word.setURI(wuri);
				
				//Proto Form
				if(wRes.hasProperty(protoFormProp)) {
					Literal lit = (Literal) synRes.getProperty(protoFormProp);
					String proto = lit.getString();
					word.setProtoForm(proto);
				}
				
				//Variants
				for(Iterator z = wRes.listPropertyValues(lexFormProp); z.hasNext(); ) {
					Literal lit = (Literal) z.next();
					String variant = lit.getString();
					word.addLexicalForm(variant);
				}
			}
		}
	}

}
