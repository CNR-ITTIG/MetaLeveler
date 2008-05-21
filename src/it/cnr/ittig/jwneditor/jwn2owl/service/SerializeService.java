package it.cnr.ittig.jwneditor.jwn2owl.service;

import it.cnr.ittig.jwneditor.jwn2owl.container.OntologyContainer;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;

public class SerializeService {

	public void process2(OntologyContainer container, String outputFile, String ns) {
		
		
		
		//XXX
		//Util.serialize(container.getOntModel(false), outputFile, ns, "ISO-8859-1");
//		Util.serialize(container.getOntModel(false), outputFile, ns, "UTF-8");
//		if(true) return;
		
		
		
		
		
		
		System.out.println("Serializing ontology model to " + outputFile + "...");

		//Serialize "pure" RDF:
		//Model m = ModelFactory.createDefaultModel();
		
		//Serialize using OWL constructs:
		OntModel om = container.getOntModel(false);
		
		//Set prefix? Se non viene messo, usa il def.namespace, migliore visualizzazione?
		//om.setNsPrefix("jwn", ((AbstractOntology) container).getModelName() + "#");
		
		RDFWriter writer = om.getWriter("RDF/XML"); //faster than RDF/XML-ABBREV
		
		//RDFWriter configuration
		
		//More info about properties and error handler (RDFWriter/RDFReader) at:
		//http://jena.sourceforge.net/IO/iohowto.html
		//http://jena.sourceforge.net/javadoc/com/hp/hpl/jena/xmloutput/RDFXMLWriterI.html
		
		//Get relative file name and use it as base..
		//File oFile = new File(outputFile);
		//String relativeOutputFileName = "file:" + oFile.getName();		
		//oFile = null;
		
		//set base property
		//String relativeOutputFileName = EditorConf.onto_work;		
		//writer.setProperty("xmlbase", relativeOutputFileName);
		String relativeOutputFileName = "file://" + outputFile;
		if(ns == null ||ns.equals("")) {
			writer.setProperty("xmlbase", relativeOutputFileName);
		} else {
			writer.setProperty("xmlbase", ns);
		}
		
		writer.setProperty("showXmlDeclaration", true);
		
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

	public void process(OntologyContainer container, String outputFile, String ns) {
		
		
		System.out.println("Serializing ontology model to " + outputFile + "...");

		//Serialize "pure" RDF:
		//Model m = ModelFactory.createDefaultModel();
		
		//Serialize using OWL constructs:
		OntModel om = container.getOntModel(false);
		
		//Set prefix? Se non viene messo, usa il def.namespace, migliore visualizzazione?
		//om.setNsPrefix("jwn", ((AbstractOntology) container).getModelName() + "#");
		
//		RDFWriter writer = om.getWriter("RDF/XML"); //faster than RDF/XML-ABBREV
//		
//		String relativeOutputFileName = "file://" + outputFile;
//		if(ns == null ||ns.equals("")) {
//			writer.setProperty("xmlbase", relativeOutputFileName);
//		} else {
//			writer.setProperty("xmlbase", ns);
//		}
//		
//		writer.setProperty("showXmlDeclaration", true);
		
		try {
			OutputStream out = new FileOutputStream(outputFile);
			//Write down the BASE model only (don't follow imports...)
			OutputStreamWriter outw = new OutputStreamWriter(out, "UTF-8");
			//writer.write(om.getBaseModel(), outw, relativeOutputFileName);
			Model outModel = om.getBaseModel();
			outModel.write(outw, "RDF/XML");
			out.close();
		} catch(Exception e) {
			System.err.println("Exception serializing model:" + e.getMessage());
			e.printStackTrace();
		}
	}	
}
