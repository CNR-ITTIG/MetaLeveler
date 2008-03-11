package it.cnr.ittig.leveler;

import it.cnr.ittig.bacci.divide.Divider;
import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.editor.util.UtilEditor;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn2owl.OWLManager;
import it.cnr.ittig.leveler.importer.CeliOdbcImporter;
import it.cnr.ittig.leveler.importer.CeliTablesImporter;
import it.cnr.ittig.leveler.importer.ILCTxtImporter;
import it.cnr.ittig.leveler.importer.IttigDatabaseImporter;
import it.cnr.ittig.leveler.importer.MetaImporter;
import it.cnr.ittig.leveler.mapper.XlsMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class Leveler {
	
	public static Map<String,Concetto> appSynsets = null;
	
	private static OWLManager manager = null;

	public static void main(String[] args) {
		
		appSynsets = new HashMap<String,Concetto>(2048, 0.70f);
		
		initAppDataDir();
		
		System.out.println("DATA_DIR: " + EditorConf.DATA_DIR);

		if(EditorConf.DIVIDE) {

			segmentData();
			
			return;
		}
		
		MetaImporter parser = null;
		if(EditorConf.TYPE_INPUT.equalsIgnoreCase("txt")) {
			parser = new ILCTxtImporter();
		} else if(EditorConf.TYPE_INPUT.equalsIgnoreCase("xls")) {
			parser = new CeliTablesImporter();
		} else if(EditorConf.TYPE_INPUT.equalsIgnoreCase("mdb")) {
			parser = new CeliOdbcImporter();
		} else if(EditorConf.TYPE_INPUT.equalsIgnoreCase("ittig")) {
			parser = new IttigDatabaseImporter();
		} else {
			System.err.println("Unknown input type.");
			return;
		}
		
		XlsMapper xParser = new XlsMapper();
		
		try {
			
			System.out.println("Creating synsets...");
			parser.createSynsets();
			
			System.out.println("Adding ipo/iper...");
			parser.addIpo();
				
			System.out.println("Adding related...");
			parser.addRelated();
			
			System.out.println("Adding references...");
			parser.addRif();
			
			if(EditorConf.ADD_ALIGNMENT) {
				System.out.println("Adding alignment...");
				parser.addAlignment();
			}

			if(EditorConf.ADD_ALIGNMENT) {
				return;
			}
			
			if(EditorConf.LANGUAGE.equals("IT") && EditorConf.LINK_TO_ONTOLOGY) {
				System.out.println("Adding ontologies classifications...");
				xParser.classify();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Adesso si salvano su DB i modelli popolati con gli oggetti synset
		long t1 = System.currentTimeMillis();
		manager = new OWLManager();
		
		owlProcessing("create");
		owlProcessing("reset");
		owlProcessing("add");
		
		//System.out.println("Serializing...");
		owlProcessing("write");
		
		long t2 = System.currentTimeMillis();
		long t3 = (t2 - t1) / 1000;
		System.out.println("OWL processing done in " + t3 + "s.");
	}

	private static void owlProcessing(String function) {
		
		if(function.equals("create")) {
			manager.addModel(EditorConf.MODEL_URI, EditorConf.USE_JENA_DB);
		}
		
		if(function.equals("reset")) {
			manager.resetModel(EditorConf.MODEL_URI);
		}
		
		if(function.equals("add")) {
			manager.addIndividuals(EditorConf.MODEL_URI, appSynsets.values());
		}

		if(function.equals("write")) {			
			manager.addModel(EditorConf.onto_ind, EditorConf.USE_JENA_DB);
			manager.addModel(EditorConf.onto_indw, EditorConf.USE_JENA_DB);
			manager.addModel(EditorConf.onto_ind_claw, EditorConf.USE_JENA_DB);
			manager.addModel(EditorConf.onto_concepts, EditorConf.USE_JENA_DB);
			manager.addModel(EditorConf.onto_types, EditorConf.USE_JENA_DB);
			manager.addModel(EditorConf.onto_sources, EditorConf.USE_JENA_DB);
			
			manager.writeModel(EditorConf.onto_ind,
					EditorConf.local_onto_ind, EditorConf.onto_ind);
			manager.writeModel(EditorConf.onto_indw,
					EditorConf.local_onto_indw, EditorConf.onto_indw);
			manager.writeModel(EditorConf.onto_ind_claw,
					EditorConf.local_onto_ind_claw, EditorConf.onto_ind_claw);
			manager.writeModel(EditorConf.onto_concepts,
					EditorConf.local_onto_concepts, EditorConf.onto_concepts);
			manager.writeModel(EditorConf.onto_types,
					EditorConf.local_onto_types, EditorConf.onto_types);
			manager.writeModel(EditorConf.onto_sources,
					EditorConf.local_onto_sources, EditorConf.onto_sources);
		}			
	}

	private static void initAppDataDir() {
	
		//Il nome dovrebbe essere diverso secondo il s.o.
		//es. linux: .jwnEditor winXp: Application Data\jwnEditor ?
		File dataDir = null;
		//File homeDir = new File(EditorConf.HOME_DIR);
		
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.indexOf("windows") > -1) {
			//Windows environment
			File appData = new File(EditorConf.HOME_DIR + "/Application Data");
			if(appData.exists()) {
				dataDir = new File(EditorConf.HOME_DIR + "/Application Data/MetaLeveler");
			} else {
				//negli altri casi si prova a mettere tutto sotto c:\
				dataDir = new File("c:/MetaLevelerData");
			}
		} else {
			//Linux e Mac sempre sotto /home/utente/.xyz ?
			dataDir = new File(EditorConf.HOME_DIR + "/.MetaLeveler");
		}
		
		UtilEditor.debug("OS info:");
		UtilEditor.debug(System.getProperty("os.arch"));
		UtilEditor.debug(System.getProperty("os.name"));
		UtilEditor.debug(System.getProperty("os.version"));
		UtilEditor.debug(System.getProperty("sun.desktop"));
		UtilEditor.debug(System.getProperty("user.name"));
		UtilEditor.debug(System.getProperty("user.dir"));
		UtilEditor.debug(System.getProperty("user.home"));
		UtilEditor.debug(System.getProperty("user.language"));
		UtilEditor.debug(System.getProperty("user.country"));
		UtilEditor.info("Setting up application directory: " 
							+ dataDir.getPath() + "...");
		
		if(dataDir.exists()) {
			if(!dataDir.isDirectory()) {
				UtilEditor.error(dataDir.getPath() + " is not a directory!");
			}
		} else {
			//Se non esiste crea la directory
			if(!dataDir.mkdir()) {
				UtilEditor.error("Error creating directory " + 
									dataDir.getPath() + " !");
			}
		}
		
		//Aggiorna il valore della directory in EditorConf
		if(EditorConf.DATA_DIR.equals("")) {
			EditorConf.DATA_DIR = dataDir.getPath();			
		}
		EditorConf.MODEL_URI = EditorConf.onto_work;
		
		EditorConf.local_onto_ind = EditorConf.DATA_DIR 
			+ File.separatorChar + "individuals.owl";
		EditorConf.local_onto_indw = EditorConf.DATA_DIR 
			+ File.separatorChar + "individuals-word.owl";
		EditorConf.local_onto_ind_claw = EditorConf.DATA_DIR 
			+ File.separatorChar + "ind-to-consumer.owl";
		EditorConf.local_onto_work = EditorConf.DATA_DIR 
			+ File.separatorChar + "jurWordNet.owl";
		EditorConf.local_onto_concepts = EditorConf.DATA_DIR 
			+ File.separatorChar + "concepts.owl";
		EditorConf.local_onto_types = EditorConf.DATA_DIR 
			+ File.separatorChar + "types.owl";
		EditorConf.local_onto_sources = EditorConf.DATA_DIR 
			+ File.separatorChar + "sources.owl";		
	}

	private static void segmentData() {
		
		//init data model		
		ModelMaker maker = ModelFactory.createMemModelMaker();
		//OntModelSpec spec =  OntModelSpec.OWL_MEM ;
		OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
		spec.setImportModelMaker(maker);
		Reasoner r = ReasonerRegistry.getOWLMicroReasoner();
		spec.setReasoner(r);
		OntModel model = ModelFactory.createOntologyModel(spec, null);
		
		File file = null;
		
		if(EditorConf.DIVIDE_TYPE.equalsIgnoreCase("lexical")) {
			file = new File(EditorConf.DATA_DIR + 
				File.separatorChar + "individuals.owl");
			model.read("file:////" + file.getAbsolutePath());
			file = new File(EditorConf.DATA_DIR + 
					File.separatorChar + "individuals-word.owl");
			model.read("file:////" + file.getAbsolutePath());
			file = new File(EditorConf.DATA_DIR + 
					File.separatorChar + "types.owl");
			model.read("file:////" + file.getAbsolutePath());

			addImport(model, 
					"http://localhost/runtime.owl", 
					"http://turing.ittig.cnr.it/jwn/ontologies/owns.owl");
			addImport(model, 
					"http://localhost/runtime.owl", 
					"http://turing.ittig.cnr.it/jwn/ontologies/language-properties-full.owl");
			
		} else if(EditorConf.DIVIDE_TYPE.equalsIgnoreCase("source")) {
			file = new File(EditorConf.DATA_DIR + 
					File.separatorChar + "individuals.owl");
			model.read("file:////" + file.getAbsolutePath());
			file = new File(EditorConf.DATA_DIR + 
					File.separatorChar + "types.owl");
			model.read("file:////" + file.getAbsolutePath());
			file = new File(EditorConf.DATA_DIR + 
					File.separatorChar + "sources.owl");
			model.read("file:////" + file.getAbsolutePath());
			addImport(model, 
					"http://localhost/runtime.owl", 
					"http://turing.ittig.cnr.it/jwn/ontologies/owns.owl");
			addImport(model, 
					"http://localhost/runtime.owl", 
					"http://turing.ittig.cnr.it/jwn/ontologies/metasources.owl");
		} else {
			System.err.println("Leveler - divide type not found: "
					+ EditorConf.DIVIDE_TYPE);
			return;
		}
	
		OntDocumentManager odm = OntDocumentManager.getInstance();
		odm.setProcessImports(true);
		odm.loadImports(model);

		//Go on...
		String baseDirName = EditorConf.DATA_DIR + File.separatorChar +
			EditorConf.DIVIDE_DIR + "-" + EditorConf.DIVIDE_TYPE + 
			File.separatorChar;
	
		Divider divider = new Divider(model);
		divider.baseDir = new File(baseDirName); 
		divider.prefix = "seg-" + EditorConf.LANGUAGE;
		divider.typeOfSizing = "triple";
		//divider.typeOfSizing = "single";
		divider.typeOfSegment = EditorConf.DIVIDE_TYPE;
		if(EditorConf.DIVIDE_TYPE.equals("lexical")) {
			divider.maxSegmentSize = 64;			
		} else {
			divider.maxSegmentSize = 128;
		}
		divider.process();
	}
	
	public static void addImport(OntModel om, String source, String dest) {

		Ontology ont = om.createOntology(source); 
		om.add(ont, OWL.imports, om.createResource(dest));
	}
	
	private static void mergeConceptFiles() {
		
		ModelMaker maker = ModelFactory.createMemModelMaker();
		//OntModelSpec spec =  OntModelSpec.OWL_MEM ;
		OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
		spec.setImportModelMaker(maker);
		Reasoner r = ReasonerRegistry.getOWLMicroReasoner();
		spec.setReasoner(r);
		OntModel model = ModelFactory.createOntologyModel(spec, null);
		
		File file = new File(EditorConf.DATA_DIR + 
				File.separatorChar + "conceptsIT.owl");
			model.read("file:////" + file.getAbsolutePath());

		file = new File(EditorConf.DATA_DIR + 
				File.separatorChar + "conceptsEN.owl");
			model.read("file:////" + file.getAbsolutePath());

		RDFWriter writer = model.getWriter("RDF/XML");
		
		String outputFileName = EditorConf.DATA_DIR + 
							File.separatorChar + "concepts.owl";
		try {
			OutputStream out = new FileOutputStream(outputFileName);
			//Write down the BASE model only (don't follow imports...)
			writer.write(model.getBaseModel(), out, 
					"file://" + outputFileName);
			out.close();
		} catch(Exception e) {
			System.err.println("Exception serializing model:" + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void adjustTypes() {
		
		ModelMaker maker = ModelFactory.createMemModelMaker();
		//OntModelSpec spec =  OntModelSpec.OWL_MEM ;
		OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
		spec.setImportModelMaker(maker);
		Reasoner r = ReasonerRegistry.getOWLMicroReasoner();
		spec.setReasoner(r);
		OntModel model = ModelFactory.createOntologyModel(spec, null);
		OntModel indModel = ModelFactory.createOntologyModel(spec, null);
		OntModel modelOk = ModelFactory.createOntologyModel(spec, null);
		
		File file = new File(EditorConf.DATA_DIR + 
				File.separatorChar + "types.owl");
		model.read("file:////" + file.getAbsolutePath());
		modelOk.read("file:////" + file.getAbsolutePath());
		
		file = new File(EditorConf.DATA_DIR + 
				File.separatorChar + "owns.owl");	
		indModel.read("file:////" + file.getAbsolutePath());

		OntClass synClass = indModel.getOntClass(
				"http://turing.ittig.cnr.it/jwn/ontologies/owns.owl#NounSynset");
		
		if(synClass == null) {
			System.err.println("synClass is null!");
			return;
		}

		for(StmtIterator si = model.listStatements(
				(Resource) null, RDF.type, (RDFNode) null); si.hasNext();) {
			Statement stmt = si.nextStatement();
			Resource subj = stmt.getSubject();
			Resource obj = (Resource) stmt.getObject();
			if(obj.isAnon() || subj.isAnon()) {
				continue;
			}
			if(obj.getNameSpace().equalsIgnoreCase(EditorConf.onto_concepts + "#")) {
				modelOk.add(subj, RDF.type, synClass);
			}
		}
		
		RDFWriter writer = model.getWriter("RDF/XML");
		
		String outputFileName = EditorConf.DATA_DIR + 
							File.separatorChar + "types-ok.owl";
		try {
			OutputStream out = new FileOutputStream(outputFileName);
			//Write down the BASE model only (don't follow imports...)
			writer.write(modelOk.getBaseModel(), out, 
					"file://" + outputFileName);
			out.close();
		} catch(Exception e) {
			System.err.println("Exception serializing model:" + e.getMessage());
			e.printStackTrace();
		}
	}
}
