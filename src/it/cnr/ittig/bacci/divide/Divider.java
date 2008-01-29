package it.cnr.ittig.bacci.divide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

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
	
	private OntModel templateModel;
	
	private OntModel segment;
	
	private int segmentCode;
	
	private String segmentName;

	private final String filler = "0000000000";
	
	private ModelMaker maker;
	
	private OntModelSpec spec;
	
	private File segmentDir;
	
	//private int tripleInSegment;
	
	private final int MAX_TRIPLE_SEGMENT = 64;
	
	private String query;

	//Public Fields
	public File baseDir;	

	public String prefix;
	
	public String typeOfSegment; 
	
	public String typeOfSizing;
	
	//Constructors
	public Divider(File owlFile, OntModel template) {
		
		//init model...
		
		templateModel = template;
		
		initModel(owlFile);
		initData();
	}

	public Divider(OntModel mod, OntModel template) {
		
		model = mod;
		templateModel = template;
		segment = null;
		
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
		
		typeOfSegment = "light";
		
		typeOfSizing = "single"; //Triples num? Memory occupation?
		
		//tripleInSegment = 0;
		
		typeFilter = null;
		
		baseDir = new File(".");
		
		prefix = "segment";
		
		query = "";
	}
	
	public void setQuery(String query) {
		
		this.query = query;
	}
	
//	private void initWeight() {
//		
//		if(typeOfSegment.equalsIgnoreCase("light")) weight = 0;
//		if(typeOfSegment.equalsIgnoreCase("normal")) weight = 1;
//		if(typeOfSegment.equalsIgnoreCase("heavy")) weight = 2;
//		if(typeOfSegment.equalsIgnoreCase("heavier")) weight = 3;
//	}

	public void process() {
		
		//initWeight();
		
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
		ResIterator iter = model.listSubjects();
		
		while(iter.hasNext()) {
			count++;
			if( ( count % 100 ) == 0) {
				long t2 = System.currentTimeMillis();
				long t3 = (t2 - t1) / 1000;
				System.out.println(count + " in " + t3 + " s)");
			}

			//OntResource res = (OntResource) iter.next();
			Resource res = iter.nextResource();
			//System.out.println("Processing resource " + res.getLocalName());

			if(typeFilter != null) {
				// ?
			}
			checkSegment();
			fillSegment(res);
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
			if(segment.size() > MAX_TRIPLE_SEGMENT) {
				createSegment();
			} 
		}
	}
	
	private void createSegment() {
		
		if(segment != null) {
			saveSegment();
		}
		
		//tripleInSegment = 0;
		
		//Crea un modello vuoto in memoria e fagli leggere
		//il modello template.
		segment = ModelFactory.createOntologyModel(spec, null);
		segment.add(templateModel, true); //true adds also reified statements
		
		//Set next segment name
		setNextSegmentName();
	}
	
	private boolean saveSegment() {
		
		RDFWriter writer = segment.getWriter("RDF/XML"); //faster than RDF/XML-ABBREV
		
		//set base property
		//writer.setProperty("xmlbase", relativeOutputFileName);
//		String relativeOutputFileName = "file://" + outputFile;
//		if(ns == null ||ns.equals("")) {
//			writer.setProperty("xmlbase", relativeOutputFileName);
//		} else {
//			writer.setProperty("xmlbase", ns);
//		}
		
		String outputFileName = segmentDir.getAbsolutePath() 
				+ File.separatorChar +  segmentName;
		try {
			OutputStream out = new FileOutputStream(outputFileName);
			//Write down the BASE model only (don't follow imports...)
			writer.write(segment.getBaseModel(), out, 
					"file://" + outputFileName);
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
		
		if(typeOfSegment.equalsIgnoreCase("light")) {
			addLightData(res);
			
		}	
		if(typeOfSegment.equalsIgnoreCase("normal")) {
			//addNormalData(res, segment);
			
		}	
		if(typeOfSegment.equalsIgnoreCase("heavy")) {
			
			QueryEngine engine = new QueryEngine();
			
			String query = "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"CONSTRUCT { " +
					" <synset-xyz> ?p ?o .  ?s ?p1 <synset-xyz> .  ?o ?p2 ?o2 .  ?o2 ?p3 ?o4 . " +
					"} WHERE { " +
					" <synset-xyz> ?p ?o .  ?s ?p1 <synset-xyz> .  ?o ?p2 ?o2 .  ?o2 ?p3 ?o4 . }";
			
			Model resultModel = engine.run(query);
			
			addDataFromModel(resultModel);
			
		}	
		if(typeOfSegment.equalsIgnoreCase("heavier")) {
			
		}
		
		if(typeOfSegment.equalsIgnoreCase("query")) {
			
			//Customize query string...
		}		
			
	}
	
	private void addLightData(Resource res) {
		
		RDFNode obj = null;
		
		StmtIterator iter = 
			model.listStatements(res, null, obj);

		while(iter.hasNext()) {
			
			//tripleInSegment++;
			segment.add(iter.nextStatement());
		}
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
