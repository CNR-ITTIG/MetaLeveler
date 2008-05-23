package it.cnr.ittig.bacci.divide;

import it.cnr.ittig.jwneditor.editor.EditorConf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Divide et impera !
 * 
 * OWL Knowledge Base Segmentation
 * 
 * @author Lorenzo Bacci
 *
 */
public class Divider {
	
	//Private Fields
	private OntModel model;
	
	private Map<String, String> uriToSegment;
	
	private RDFNode typeFilter;

	private OntModel segment;
	
	private int segmentCode;
	
	private String segmentName;

	private final String filler = "0000000000";
	
	private ModelMaker maker;
	
	private OntModelSpec spec;
	
	private File segmentDir;
	
	private String query;

	//Public Fields
	public File baseDir;	

	public String prefix;
	
	public String typeOfSegment; 
	
	public String typeOfSizing;
	
	public int maxSegmentSize;	
	
	//Constructors
	public Divider(File owlFile) {
		
		//init model...
		initModel(owlFile);
		initData();
	}

	public Divider(OntModel mod) {
		
		model = mod;
		initData();
	}
	
	private void initModel(File owlFile) {
		
		ModelMaker maker = ModelFactory.createMemModelMaker();
		OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
		spec.setImportModelMaker(maker);
		model = ModelFactory.createOntologyModel(spec, null);
		model.read("file:////" + owlFile.getAbsolutePath());
	}
	
	private void initData() {

		//TODO Set init parameters depending on the given model
		uriToSegment = new HashMap<String, String>();

		maker = ModelFactory.createMemModelMaker();
		spec = new OntModelSpec( OntModelSpec.OWL_MEM );
		spec.setImportModelMaker(maker);

		prefix = "";
		
		typeOfSegment = "";
		
		typeOfSizing = "single"; //Triples num? Memory occupation?
		
		typeFilter = null;
		
		baseDir = new File(".");
		
		prefix = "segment";
		
		query = "";
		
		maxSegmentSize = 64;
	}
	
	public void setQuery(String query) {
		
		this.query = query;
	}
	
	public void process() {
		
		segment = null;
		segmentName = "-";
		segmentCode = 0;
		
		if(!prepareDir()) {
			System.err.println("prepareDir() failed.");
			return;
		}
		
		//For each resource, get actual model and process it
		long t1 = System.currentTimeMillis();
		int count = 0;

		//ExtendedIterator iter = model.listIndividuals();
		System.out.println("size: " + model.size());

//		ResIterator iter = model.listSubjects();
		OntClass synClass = model.getOntClass(
				"http://turing.ittig.cnr.it/jwn/ontologies/owns.owl#Synset");
		if(synClass == null) {
			System.err.println("Divider - synclass is null!");
			return;
		}
		ExtendedIterator iter = synClass.listInstances(false);
		
		while(iter.hasNext()) {
			
			//if(count > 1) break;
			
			OntResource res = (OntResource) iter.next();
			//Resource res = (Resource) iter.next();
			//Resource res = iter.nextResource();
			//System.out.println("Processing resource " + res.getLocalName());
			
//			String resName = res.getLocalName();
//			if(!resName.startsWith("synset-")) {
//				continue;
//			}

			if(typeFilter != null) {
				// ?
			}

			count++;
			if( ( count % 100 ) == 0) {
				long t2 = System.currentTimeMillis();
				long t3 = (t2 - t1) / 1000;
				System.out.println(count + " in " + t3 + " s");
			}
			//System.out.println(">> " + res.getLocalName() + " (" + count + ")" );

			checkSegment();
			fillSegment(res);			
		}
		
		//Add remaining triples in the last segment...
		if(typeOfSizing.equalsIgnoreCase("triple")) {
			createSegment();
		}
		
		try {
			//Write down all the mappings
			serializeMap();	
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			System.out.println("Divider Done.");
		}
	}
	
	private void checkSegment() {
		
		if(segment == null ||
				typeOfSizing.equalsIgnoreCase("single")) {			
			
			createSegment();
		}

		if(typeOfSizing.equalsIgnoreCase("triple")) {
			
			//Count triples in actualSegment, then
			//return the actual or a new segment.
			//Save segment if necessary.
			if(segment.size() > maxSegmentSize) {
				createSegment();
			} 
		}
	}
	
	private void createSegment() {
		
		if(segment != null) {
			saveSegment();
		}
		
		//Crea un modello vuoto in memoria
		segment = ModelFactory.createOntologyModel(spec, null);
		
		//Set next segment name
		setNextSegmentName();
	}
	
	private boolean saveSegment() {
		
		RDFWriter writer = segment.getWriter("RDF/XML");
		
		String outputFileName = segmentDir.getAbsolutePath() 
				+ File.separatorChar +  segmentName;
		try {
			OutputStream out = new FileOutputStream(outputFileName);
			//Write down the BASE model only (don't follow imports...)
			writer.write(segment.getBaseModel(), out, null); //"file://" + outputFileName);
			out.close();
		} catch(Exception e) {
			System.err.println("Exception serializing model:" + e.getMessage());
			e.printStackTrace();
		}

		return true;
	}
	
	private void addMapping(Resource res) {
		
		//aggiungi il mapping tra res e segment nell'hashmap
		String resName = res.getNameSpace() + res.getLocalName();		
		uriToSegment.put(resName, segmentName);
	}
	
	private void serializeMap() 
		throws IOException, FileNotFoundException {
		
		System.out.println("Serializing map...");
		
		String mapName = baseDir.getAbsolutePath() + File.separatorChar + 
			prefix + "-map.dat";
		
		FileOutputStream fos = new FileOutputStream(mapName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(uriToSegment);				
		
		oos.close();
		fos.close();
	}
	
	private void setNextSegmentName() {
		
		String code = Integer.toString(segmentCode);	
		String name = filler + code;
		segmentCode++;		
		segmentName = prefix + "-" + 
				name.substring(code.length()) + ".owl";
	}
	
	private void fillSegment(Resource res) {
		
		addMapping(res);
		
		//String resURI = res.getNameSpace() + res.getLocalName();
		//String resNS = res.getNameSpace();
		
		//Per le lexical properties non ci sarebbe bisogno di individual-words...
		String lang = EditorConf.LANGUAGE;

		String resName = "rns:" + res.getLocalName();
		
		if(typeOfSegment.equalsIgnoreCase("lexical")) {

			query =  
				"PREFIX rns: <http://localhost/dalos/" + lang + "/individuals.owl#> " +
				"PREFIX owns: <http://turing.ittig.cnr.it/jwn/ontologies/owns.owl#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"CONSTRUCT { " + resName + " ?p ?o .  ?o1 ?p2 ?o2 .  ?o4 ?p5 ?o5 . " +
				" } WHERE { { " + resName + " ?p ?o . } UNION { " + resName + 
				" ?p1 ?o1 .  ?o1 rdf:type owns:WordSense .  ?o1 ?p2 ?o2 .  " +
				" } UNION {  " + resName + " ?p3 ?o3 .  ?o3 ?p4 ?o4 ." +
				"  ?o4 rdf:type owns:Word .  ?o4 ?p5 ?o5 .  } } ";			
			
		} else if(typeOfSegment.equalsIgnoreCase("source")) {
			//Optional non funziona in questa query?

//			query = 
//				"PREFIX rns: <http://localhost/dalos/" + lang + "/individuals.owl#> " +
//				"PREFIX src: <http://turing.ittig.cnr.it/jwn/ontologies/metasources.owl#> " +
//				"CONSTRUCT { " +
//				resName + " src:source ?s . " +
//				"?s src:involvesPartition ?p . " +
//				"?s src:content ?cont . " +
//				"?p src:partitionCode ?pcode . " +
//				"?p src:belongsTo ?doc . " +
//				"?doc src:documentCode ?dcode . " +
//				"?doc src:link ?link . " +
//				"} WHERE { " +
//				resName + " src:source ?s . " +
//				"?s src:involvesPartition ?p . " +
//				"?p src:partitionCode ?pcode . " +
//				"?p src:belongsTo ?doc . " +
//				"?doc src:documentCode ?dcode . " +
//				"OPTIONAL {" +
//				"?s src:content ?cont . " +
//				"?doc src:link ?link . } " +
//				"}";
			
			query = 
				"PREFIX rns: <http://localhost/dalos/" + lang + "/individuals.owl#> " +
				"PREFIX src: <http://turing.ittig.cnr.it/jwn/ontologies/metasources.owl#> " +
				"CONSTRUCT { " +
				resName + " src:source ?s . " +
				"?s src:involvesPartition ?p . " +
				"?s src:content ?cont . " +
				"?p src:partitionCode ?pcode . " +
				"?p src:belongsTo ?doc . " +
				"?doc src:documentCode ?dcode . " +
				"?doc src:link ?dlink . " +
				"} WHERE { { " +
				resName + " src:source ?s . " +
				"?s src:involvesPartition ?p . " +
				"?p src:partitionCode ?pcode . " +
				"?p src:belongsTo ?doc . " +
				"?doc src:documentCode ?dcode . " +
				" } UNION { " +
				resName + " src:source ?s . " +
				"?s src:involvesPartition ?p . " +
				"?s src:content ?cont . " +
				"?p src:partitionCode ?pcode . " +
				"?p src:belongsTo ?doc . " +
				"?doc src:documentCode ?dcode . " +
				" } UNION { " +
				resName + " src:source ?s . " +
				"?s src:involvesPartition ?p . " +
				"?p src:partitionCode ?pcode . " +
				"?p src:belongsTo ?doc . " +
				"?doc src:documentCode ?dcode . " +
				"?doc src:link ?dlink . } " +				
				"}";

		} else if(typeOfSegment.equalsIgnoreCase("semantic")) {
		} else {
			System.err.println("Divider - type of segment not supported: "
					+ typeOfSegment);
			return;
		}
		
		Model resultModel = QueryEngine.run(model, query);	
		//System.out.println("Result model size: " + resultModel.size());
		addDataFromModel(resultModel);		
	}

	private void addDataFromModel(Model dataModel) {
		
		segment.add(dataModel);
	}
	
	private boolean prepareDir() {
		
		//Prepare base dir
		if(!baseDir.exists()) {
			if(!baseDir.mkdir()) {
				System.err.println("mkdir base not allowed!");
				return false;			
			}
		} else {
			if(!baseDir.isDirectory()) {
				System.err.println("base is not a directory !?");
				return false;
			}
		}
		
		//Prepare segment dir
		String segmentDirName = baseDir.getAbsolutePath() + 
				File.separatorChar + prefix;
		
		segmentDir = new File(segmentDirName);
		System.out.println("Preparing " + segmentDir.getAbsolutePath() + "...");
		if(segmentDir.exists()) {
			if(!segmentDir.delete()) {
				System.err.println("Segment dir is not empty!");
				return false;
			}
		}
		
		if(!segmentDir.mkdir()) {
			System.err.println("mkdir segment not allowed!");
			return false;			
		}
		
		return true;
	}
}
