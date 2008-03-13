package it.cnr.ittig.jwneditor.jwn2owl.service;

import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn.Lemma;
import it.cnr.ittig.jwneditor.jwn.Relazione;
import it.cnr.ittig.jwneditor.jwn2owl.OWLManager;
import it.cnr.ittig.jwneditor.jwn2owl.OWLUtil;
import it.cnr.ittig.jwneditor.jwn2owl.container.AbstractOntology;
import it.cnr.ittig.jwneditor.jwn2owl.container.OntologyContainer;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class AddService {

	private OWLManager owlManager = null;
	
	OntModel m; //OntModel m_work;		
	OntModel m_ind;
	OntModel m_indw;
	OntModel m_conc;
	OntModel m_types;
	OntModel m_sources;
	
	ModelMaker maker;
	
	OntProperty containsProperty;
	OntProperty glossProperty;
	OntProperty idProperty;
	OntProperty inSynsetProperty;
	OntProperty wordProperty;
	OntProperty senseProperty;
	OntProperty lexicalProperty;
//	OntProperty tagProperty;
	OntProperty protoProperty;
	OntProperty candidateProperty;
	
	OntProperty sourceProp;
	OntProperty involvesSynset;
	OntProperty involvesPartition;	
	OntProperty belongsTo;
	OntProperty docCode;
	OntProperty partCode;
	
	ObjectProperty hypo;
	ObjectProperty belongs;
	
	OntClass nounClass;
	OntClass verbClass;
	OntClass adjectiveClass;
	OntClass adverbClass;
	OntClass nounWSClass;
	OntClass verbWSClass;
	OntClass adjectiveWSClass;
	OntClass adverbWSClass;
	OntClass wordClass;

	OntClass conceptClass;
	
	OntClass documentClass;
	OntClass partitionClass;
	OntClass sourceClass;
	
	Set<OntClass> validClasses;
	Set<String> invalidClasses;
	
	String NS_SCHEMA; //schema namespace
	String LANG_SCHEMA; //language properties namespace
	String SOURCE_SCHEMA;
	String NS_CURRENT; //this model namespace
	
//	String NS_IND = "file://" + EditorConf.local_onto_ind + "#";
//	String NS_INDW = "file://" + EditorConf.local_onto_indw + "#";
//	String NS_CONC = "file://" + EditorConf.local_onto_concepts + "#";
//	String NS_TYPE = "file://" + EditorConf.local_onto_types + "#";
//	String NS_SOURCE = "file://" + EditorConf.local_onto_sources + "#";
	
	String NS_IND = EditorConf.onto_ind + "#";
	String NS_INDW = EditorConf.onto_indw + "#";
	String NS_CONC = EditorConf.onto_concepts + "#";
	String NS_TYPE = EditorConf.onto_types + "#";
	String NS_SOURCE = EditorConf.onto_sources + "#";
	
	boolean logging = false;
	
	public AddService(OWLManager owlManager) {
		
		this.owlManager = owlManager;
	}
	
	private void initModels() {
		
		OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
		spec.setImportModelMaker(maker);
		
		m_ind = ModelFactory.createOntologyModel(spec,
				maker.createModel(EditorConf.onto_ind, false));
		m_indw = ModelFactory.createOntologyModel(spec,
				maker.createModel(EditorConf.onto_indw, false));
		m_conc = ModelFactory.createOntologyModel(spec,
				maker.createModel(EditorConf.onto_concepts, false));
		//m_conc = initConceptModel(spec, maker);
		m_types = ModelFactory.createOntologyModel(spec,
				maker.createModel(EditorConf.onto_types, false));
		//m_types = initTypeModel(spec, maker);
		m_sources = ModelFactory.createOntologyModel(spec,
				maker.createModel(EditorConf.onto_sources, false));

		if(!EditorConf.USE_JENA_DB) {
			owlManager.addModel(EditorConf.onto_ind, false);
			owlManager.addModel(EditorConf.onto_indw, false);
			owlManager.addModel(EditorConf.onto_types, false);
			owlManager.addModel(EditorConf.onto_concepts, false);
			owlManager.addModel(EditorConf.onto_sources, false);
			
			owlManager.setModel(EditorConf.onto_ind, m_ind);
			owlManager.setModel(EditorConf.onto_indw, m_indw);
			owlManager.setModel(EditorConf.onto_types, m_types);
			owlManager.setModel(EditorConf.onto_concepts, m_conc);
			owlManager.setModel(EditorConf.onto_sources, m_sources);
		}

		OWLUtil.addImport(m, EditorConf.onto_work, EditorConf.onto_ind);
		OWLUtil.addImport(m, EditorConf.onto_work, EditorConf.onto_indw);
		OWLUtil.addImport(m, EditorConf.onto_work, EditorConf.ownSchema);
		OWLUtil.addImport(m, EditorConf.onto_work, EditorConf.langSchema);
		OWLUtil.addImport(m, EditorConf.onto_work, EditorConf.domainOntoModel);
		OWLUtil.addImport(m, EditorConf.onto_work, EditorConf.onto_concepts);
		OWLUtil.addImport(m, EditorConf.onto_work, EditorConf.onto_types);
		OWLUtil.addImport(m, EditorConf.onto_work, EditorConf.sourceSchema);
		
		OntDocumentManager odm = OntDocumentManager.getInstance();
		odm.setProcessImports(true);

		setPrefixes(m);
		setPrefixes(m_ind);
		setPrefixes(m_indw);
		setPrefixes(m_conc);
		setPrefixes(m_types);
		setPrefixes(m_sources);
		
//		odm.addAltEntry(EditorConf.onto_ind, EditorConf.local_onto_ind);
//		odm.addAltEntry(EditorConf.onto_indw, EditorConf.local_onto_indw);
//		odm.addAltEntry(EditorConf.onto_ind_claw, EditorConf.local_onto_ind_claw);
//		odm.addAltEntry(EditorConf.onto_concepts, EditorConf.local_onto_concepts);
//		odm.addAltEntry(EditorConf.onto_types, EditorConf.local_onto_types);
//		odm.addAltEntry(EditorConf.onto_sources, EditorConf.local_onto_sources);
		
		odm.loadImports(m);
	}
	
	/*
	 * Check if a concept owl file exist and use it.
	 */
	private OntModel initConceptModel(OntModelSpec spec, ModelMaker maker) {
		
		OntModel cMod = ModelFactory.createOntologyModel(spec,
				maker.createModel(EditorConf.onto_concepts, false));
		
		File conceptFile = new File(EditorConf.local_onto_concepts);
		if(conceptFile.exists()) {
			//Load the pre-existent concept model
			System.out.println("Loading pre-existent concept model (" + 
					conceptFile + ")...");
			cMod.read("file:///" + EditorConf.local_onto_concepts);
		} 
		return cMod;
	}
	
	/*
	 * Check if a type owl file exist and use it.
	 */
	private OntModel initTypeModel(OntModelSpec spec, ModelMaker maker) {
		//Serve per aggiungere i collegamenti con le classi NounSynset, etc.
		
		OntModel tMod = ModelFactory.createOntologyModel(spec,
				maker.createModel(EditorConf.onto_types, false));
		
		File typeFile = new File(EditorConf.local_onto_types);
		if(typeFile.exists()) {
			//Load the pre-existent concept model
			System.out.println("Loading pre-existent type model (" + 
					typeFile + ")...");
			tMod.read("file:///" + EditorConf.local_onto_types);
		} 
		return tMod;
	}
	
	private void init() {
		
		initModels();
		
		containsProperty = m.getOntProperty(NS_SCHEMA + "containsWordSense");
		glossProperty = m.getOntProperty(NS_SCHEMA + "gloss");
		idProperty = m.getOntProperty(NS_SCHEMA + "synsetId");
		inSynsetProperty = m.getOntProperty(NS_SCHEMA + "inSynset");
		wordProperty = m.getOntProperty(NS_SCHEMA + "word");
		senseProperty = m.getOntProperty(NS_SCHEMA + "sense");
		lexicalProperty = m.getOntProperty(NS_SCHEMA + "lexicalForm");
		protoProperty = m.getOntProperty(NS_SCHEMA + "protoForm");
		
		sourceProp = m.getOntProperty(SOURCE_SCHEMA + "source");
		involvesSynset = m.getOntProperty(SOURCE_SCHEMA + "involvesSynset");
		involvesPartition = m.getOntProperty(SOURCE_SCHEMA + "involvesPartition");	
		belongsTo = m.getOntProperty(SOURCE_SCHEMA + "belongsTo");
		docCode = m.getOntProperty(SOURCE_SCHEMA + "documentCode");
		partCode = m.getOntProperty(SOURCE_SCHEMA + "partitionCode");

		hypo = m.getObjectProperty(LANG_SCHEMA + EditorConf.iponimia);
		belongs = m.getObjectProperty(LANG_SCHEMA + EditorConf.belongs);

		nounClass = m.getOntClass(NS_SCHEMA + OWLUtil.getSynsetClass("N"));
		verbClass = m.getOntClass(NS_SCHEMA + OWLUtil.getSynsetClass("V"));
		adjectiveClass = m.getOntClass(NS_SCHEMA + OWLUtil.getSynsetClass("AG"));
		adverbClass = m.getOntClass(NS_SCHEMA + OWLUtil.getSynsetClass("AV"));
		nounWSClass = m.getOntClass(NS_SCHEMA + OWLUtil.getWordSenseClass("N"));
		verbWSClass = m.getOntClass(NS_SCHEMA + OWLUtil.getWordSenseClass("V"));
		adjectiveWSClass = m.getOntClass(NS_SCHEMA + OWLUtil.getWordSenseClass("AG"));
		adverbWSClass = m.getOntClass(NS_SCHEMA + OWLUtil.getWordSenseClass("AV"));
		wordClass = m.getOntClass(NS_SCHEMA + OWLUtil.getWordClass());
		
		conceptClass = m_conc.createClass(NS_CONC + "Concept");
		
		documentClass = m.getOntClass(SOURCE_SCHEMA + "Document");
		partitionClass = m.getOntClass(SOURCE_SCHEMA + "Partition");
		sourceClass = m.getOntClass(SOURCE_SCHEMA + "Source");
		
		validClasses = new HashSet<OntClass>();		
		invalidClasses = new HashSet<String>();
	}
	
	private void setPrefixes(OntModel mod) {
		
		mod.setNsPrefix("claw", EditorConf.domainOntoModelNs);
		mod.setNsPrefix("ind", NS_IND);
		mod.setNsPrefix("indw", NS_INDW);
		mod.setNsPrefix("owns", NS_SCHEMA);
		mod.setNsPrefix("langf", LANG_SCHEMA);
		mod.setNsPrefix("conc", NS_CONC);
		mod.setNsPrefix("type", NS_TYPE);
		mod.setNsPrefix("sources", SOURCE_SCHEMA);
		mod.setNsPrefix("source", NS_SOURCE);
	}
	
	private OntClass getUpperClass(String name, String ns) {

		if(invalidClasses.contains(ns + name)) {
			return null;
		}
		
		for(Iterator<OntClass> i = validClasses.iterator(); i.hasNext();) {
			OntClass item = i.next();
			if(item.getLocalName().equals(name) &&
					item.getNameSpace().equals(ns)) {
				return item;
			}
		}
		
		//trova, aggiungi e restituisci la nuova upper class
		OntClass oc = m.getOntClass(ns + name);
		if(oc == null) {
			System.err.println("ooo>> CLASS NOT FOUND ! (" +
								ns + name + ")");
			invalidClasses.add(ns + name);
			return null;
		}

		validClasses.add(oc);
		
		return oc;
	}
	
	private void addUpperSubClass(OntClass synClass, OntClass upper) {
		
		m_conc.add(synClass, RDFS.subClassOf, upper);
	}
	
//	private OntClass getUpperDocuments(String name, String ns) {
//		
//		for(int i = 0; i < upperClasses.size(); i++) {
//			OntClass item = upperClasses.get(i);
//			if(item.getLocalName().equals(name) &&
//					item.getNameSpace().equals(ns)) {
//				return item;
//			}
//		}
//		
//		//trova, aggiungi e restituisci la nuova upper class
//		OntClass oc = m.getOntClass(ns + name);
//		if(oc == null) {
//			System.err.println("ooo>> CLASS NOT FOUND ! (" +
//								ns + name + ")");
//			return null;
//		}
//
//		upperClasses.add(oc);
//		return oc;
//	}
	
	private void processIndividual(Concetto item) {

		OntResource synset = null;
		
		logging = false;		
		
		//crea l'individuo di Synset
		synset = createSynsetIndividual(item);
		if(synset == null) {
			System.err.println("synset is null! item:" + item);
			return;
		}
		if(item.toString().startsWith("conto")) {
			System.out.println("item: " + item + " synset: " + 
					synset.getNameSpace() + synset.getLocalName());
		}

		if(logging) {
			System.out.println("synset created: " + synset);
		}

		//aggiungi i "dettagli" del concetto
		addSynsetDetails(item, synset);
		
		//Per ogni lemma, crea l'individuo di 
		//Wordsense e Word, crea i collegamenti
		for(int k = 0; k < item.lemmi.size(); k++) {
			Lemma lemma = item.lemmi.get(k);
			
			OntResource wordsense = createWordSenseIndividual(lemma);
			if(wordsense == null) {
				System.err.println("wordsense is null! lemma:" + lemma);
				continue;
			}

			OntResource word = createWordIndividual(lemma);
			if(word == null) {
				System.err.println("word is null! lemma:" + lemma);
				continue;
			}

			//proto form and lexical forms
			if(lemma.getProtoForm().length() > 0) {
				Literal lit = m_indw.createTypedLiteral(lemma.getProtoForm());
				word.addProperty(protoProperty, lit);
			}

			for(int v = 0; v < lemma.variants.size(); v++) {
				String variant = lemma.variants.get(v);
				//add lexical form for this word
				Literal lit = m_indw.createTypedLiteral(variant);
				word.addProperty(lexicalProperty, lit);
			}

			//add schema relations...
			addSchemaProperties(synset, wordsense, word);					
		}
	}

	private void processIndividualRelations(Concetto item) {
		
		//Retrive source individual
		Individual sourceIndividual = getIndividual(item);

		for(int k = 0; k < item.correlazioni.size(); k++) {
			Relazione rel = item.correlazioni.get(k).getRelation();
			Concetto dest = item.correlazioni.get(k).getDestination();
			
			//Retrieve or create this (Jur)WordNet property
			OntProperty op = getWNProperty(rel);
			
			//Retrive destination individual
			Individual destIndividual = getIndividual(dest);
			
			if(sourceIndividual == null || destIndividual == null || op == null) {
				System.err.println("processIndividualRelations() - failed. -- Removed synset?");
				System.out.println("s:" + sourceIndividual + " d:" + destIndividual +
						" op:" + op);
				System.out.println("Destination: " + dest);
				System.out.println("Source: " + item);
				continue;
			}
			
			//Add property to individual
			sourceIndividual.addProperty(op, destIndividual);			
		}
		
		//Add super/sub class relations
		//addHierarchy(item);		
	}
	
	private void processLinks(Concetto item) {

		OntResource synset = null;
		String sname = OWLUtil.getSynsetName(item.getPrimario());
		synset = m_ind.getOntResource(NS_IND + sname);
		if(synset == null) {
			System.err.println("synset is null! item:" + item);
			return;
		}

		//Links to concepts.owl
		//crea la classe corrispondente
		OntClass synsetClass = createConceptClass(item, synset);
		
		if(synsetClass == null && item.ontoclassi.size() > 0) {
			System.err.println(">>> WARNING: no concept for classified term:" 
					+ item + "! Skipping...");
			return;
		}
		
		for(Iterator<String> ci = item.ontoclassi.iterator(); ci.hasNext();) {
			String ocName = ci.next();
			String[] data = ocName.split("#");
			String namespace = data[0] + "#";
			String ocname = data[1];
			OntClass upper = getUpperClass(ocname, namespace);
			if(upper != null) {
				addUpperSubClass(synsetClass, upper);							
			}			
		}
	}
		
	private void processSources(Concetto item) {

		OntResource synset = null;
		String sname = OWLUtil.getSynsetName(item.getPrimario());
		synset = m_ind.getOntResource(NS_IND + sname);
		if(synset == null) {
			System.err.println("synset is null! item:" + item);
			return;
		}
		
		int maxSources = item.riferimenti.size();

		//Set a BOUND?
		maxSources = 99999;
		if(item.riferimenti.size() < maxSources) {
			maxSources = item.riferimenti.size();
		}

		//Aggiungi sources (non considera le frequenze al momento)		
		for(int k = 0; k < maxSources; k++) {
			String partitionCode = item.riferimenti.get(k);
			String documentCode = partitionCode.substring(0, partitionCode.
					indexOf('-', partitionCode.indexOf('-') + 1));
			String cid = item.getID();
			String sourceName = NS_SOURCE + "source-" + cid + "-" + partitionCode;
			String partitionName = NS_SOURCE + "partition-" + partitionCode;
			String documentName = NS_SOURCE + "document-" + documentCode;
			
			//System.out.println(">>" + item + " rif: " +  partitionName);
			
			OntResource source = m_sources.createOntResource(sourceName);
			OntResource partition = m_sources.getOntResource(partitionName);
			if(partition == null) {
				partition = m_sources.createOntResource(partitionName);
			}
			OntResource document =  m_sources.getOntResource(documentName);
			if(document == null) {
				document = m_sources.createOntResource(documentName);
			}
			synset.addProperty(sourceProp, source);
			source.addProperty(involvesSynset, synset);
			source.addProperty(involvesPartition, partition);
			partition.addProperty(belongsTo, document);
			partition.addProperty(partCode, m_sources.createLiteral(partitionCode));
			document.addProperty(docCode, m_sources.createLiteral(documentCode));
		}
	}

	public void process(OntologyContainer container, Collection concetti) {		
		/*
		 * Si itera tutto l'insieme dei concetti due volte.
		 * La prima volta si creano le classi e gli individui
		 * Synset, WordSense e Word, e si creano le relazioni
		 * dello schema tra questi individui (e si aggiungono
		 * anche gli eventuali collegamenti con altre ontologie).
		 * La seconda volta si inseriscono le relazioni di wordnet
		 * tra gli individui di Synset precedentemente creati. 
		 */
		
		m = container.getOntModel(false);
		NS_SCHEMA = EditorConf.ownSchema + "#";
		LANG_SCHEMA = EditorConf.langSchema + "#";
		NS_CURRENT = container.getNameSpace();
		SOURCE_SCHEMA = EditorConf.sourceSchema + "#";

		maker = ((AbstractOntology) container).getModelMaker();
		
		init();
		
		System.out.println("AddService - Processing data...");
		System.out.println("AddService - Adding synsets individuals...");
		
		//create synset and individuals
		long t1 = System.currentTimeMillis();
		int recordCount = 0;
		for(Iterator i = concetti.iterator(); i.hasNext();) {
			recordCount++;
			if( ( recordCount % 100 ) == 0) {
				long t2 = System.currentTimeMillis();
				long t3 = (t2 - t1) / 1000;
				System.out.println(recordCount + "/" + 
						concetti.size() + " in " + t3 + " s)");
			}
			processIndividual((Concetto) i.next());
		}
		
		System.out.println("AddService - Adding individual relations...");

		//Relations between synsets individuals
		t1 = System.currentTimeMillis();
		recordCount = 0;
		for(Iterator i = concetti.iterator(); i.hasNext();) {
			recordCount++;
			if( ( recordCount % 100 ) == 0) {
				long t2 = System.currentTimeMillis();
				long t3 = (t2 - t1) / 1000;
				System.out.println(recordCount + "/" + 
						concetti.size() + " in " + t3 + " s)");
			}
			processIndividualRelations((Concetto) i.next());
		}
		
		System.out.println("AddService - Adding external links...");
		t1 = System.currentTimeMillis();
		recordCount = 0;
		for(Iterator i = concetti.iterator(); i.hasNext();) {
			recordCount++;
			if( ( recordCount % 100 ) == 0) {
				long t2 = System.currentTimeMillis();
				long t3 = (t2 - t1) / 1000;
				System.out.println(recordCount + "/" + 
						concetti.size() + " in " + t3 + " s)");
			}
			processLinks((Concetto) i.next());
		}
		
		System.out.println("AddService - Adding sources...");
		t1 = System.currentTimeMillis();
		recordCount = 0;
		for(Iterator i = concetti.iterator(); i.hasNext();) {
			recordCount++;
			if( ( recordCount % 100 ) == 0) {
				long t2 = System.currentTimeMillis();
				long t3 = (t2 - t1) / 1000;
				System.out.println(recordCount + "/" + 
						concetti.size() + " in " + t3 + " s)");
			}
			processSources((Concetto) i.next());
		}

		System.out.println("AddService - Done.");
	}
	
	private OntClass createConceptClass(Concetto c, OntResource syn) {
		
		//Verifica se ha belongs_to_class per WordNet
		RDFNode value = syn.getPropertyValue(belongs);
		if(value != null) {
			//Non creare la classe concept relativa.
			if(logging) {
				System.out.println(">>>>>> Skipping " + 
						syn + " BELONGS_TO_CLASS " + value);
			}
			return null;
		}		
		
		//Crea la classe concept in base al valore dell'attributo 'conceptClass'
		if(c.conceptLemma == null) {
			return null;
		}
		//String name = OWLUtil.getConceptClassName(c.getPrimario());
		String name = OWLUtil.getConceptClassName(c.conceptLemma);
		//System.out.println("Concept: " + name);
		OntClass synsetClass = m_conc.getOntClass(NS_CONC + name);
		if(synsetClass == null) {
			//Add a new concept class
			synsetClass = m_conc.createClass(NS_CONC + name);
			synsetClass.addSuperClass(conceptClass);
		}
		//Link this synset to the concept class
		m_types.add(syn, RDF.type, synsetClass);
		
		return synsetClass;
	}
	
	private OntResource createSynsetIndividual(Concetto c) {
		
		String name = OWLUtil.getSynsetName(c.getPrimario());
		OntResource res = m_ind.createOntResource(NS_IND + name);
		OntClass posClass = getPosSynsetClass(c.getPartOfSpeech());
		m_ind.add(res, RDF.type, posClass);
		
		//Candidate term?
		if(c.isCandidate()) {
			OntProperty candProp = getCandidateProperty();
			m_ind.add(res, candProp, "true");
		}
		return res;
	}
	
	private OntProperty getCandidateProperty() {
		
		candidateProperty = m_ind.getOntProperty(
				NS_SCHEMA + "candidate");
		if(candidateProperty == null) {
			candidateProperty = m_ind.createOntProperty(
					NS_SCHEMA + "candidate");
		}

		return candidateProperty;
	}

	private OntResource createWordSenseIndividual(Lemma l) {
		
		String name = OWLUtil.getWordSenseName(l);
		OntClass oc = getPosWSClass(l.getPartOfSpeech());
		OntResource res = m_indw.createOntResource(NS_IND + name);
		m_indw.add(res, RDF.type, oc);
		return res;
	}

	private OntResource createWordIndividual(Lemma l) {
		
		String name = OWLUtil.getWordName(l);
		OntResource res = m_indw.createOntResource(NS_IND + name);
		m_indw.add(res, RDF.type, wordClass);
		return res;
	}
	
	private OntClass getPosSynsetClass(String pos) {
		
		if(pos.equalsIgnoreCase("N")) return nounClass;
		if(pos.equalsIgnoreCase("V")) return verbClass;
		if(pos.equalsIgnoreCase("AG")) return adjectiveClass;
		if(pos.equalsIgnoreCase("AV")) return adverbClass;
		return null;
	}
	
	private OntClass getPosWSClass(String pos) {
		
		if(pos.equalsIgnoreCase("N")) return nounWSClass;
		if(pos.equalsIgnoreCase("V")) return verbWSClass;
		if(pos.equalsIgnoreCase("AG")) return adjectiveWSClass;
		if(pos.equalsIgnoreCase("AV")) return adverbWSClass;
		return null;
	}
	
	/*
	 * Add gloss, synsetID, (tagCount?)
	 */
	private void addSynsetDetails(Concetto item, OntResource synset) {
			
		String def = item.getDefinizione();
		if(def.trim().length() > 0) {
			Literal lit = m_ind.createTypedLiteral(def);
			//synset.addProperty(glossProperty, def);
			synset.addProperty(glossProperty, lit);
		}
		
		String id = OWLUtil.getSynsetID(item);
		if(!synset.hasProperty(idProperty)) {
			Literal lit = m_ind.createTypedLiteral(id);
			//synset.addProperty(idProperty, id);
			synset.addProperty(idProperty, lit);
		} else {
			System.out.println("========!!! id already in " + synset);
			System.out.println("old:" + synset.getPropertyValue(idProperty) + 
					" new:" + id);
		}
	}
	
	/*
	 * Add schema-defined properties between Synset, WordSense and Word.
	 */
	private void addSchemaProperties(OntResource synset, OntResource wordsense,
			OntResource word) {
		
		m_indw.add(synset, containsProperty, wordsense);
		m_indw.add(wordsense, inSynsetProperty, synset);
		m_indw.add(wordsense, wordProperty, word);
		m_indw.add(word, senseProperty, wordsense);
	}
	
	private Individual getIndividual(Concetto c) {

		String name = OWLUtil.getSynsetName(c.getPrimario());
		Individual ind = m_ind.getIndividual(NS_IND + name);
		return ind;
	}
	
	/*
	 * Find or create matching property.
	 */
	private OntProperty getWNProperty(Relazione rel) {
		
		String name = OWLUtil.getOWLName(rel.getLexicalForm());
		OntProperty op = m.getOntProperty(LANG_SCHEMA + name);
		if(op == null) {
			System.err.println("OntProperty not found! name:" + name);
		}
		return op;
	}	
}
