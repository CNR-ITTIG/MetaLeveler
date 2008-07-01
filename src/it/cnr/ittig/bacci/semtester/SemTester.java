package it.cnr.ittig.bacci.semtester;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SemTester {

	private static OntDocumentManager odm = 
		OntDocumentManager.getInstance();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		OntModel model = loadModel();

		Validator validator = new Validator();
		SyntaxChecker checker = new SyntaxChecker();

		if(model != null) {
			validator.process(model);
			checker.process(model);
		}

	}
	
	private static OntModel loadModel() {
		
		OntModel model = ModelFactory.createOntologyModel( 
				PelletReasonerFactory.THE_SPEC );   
		
		String modelUrl = "http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl";
		//String modelUrl = "file:///E:/Ontologies/dalos/consumer-law.owl";
		model.read(modelUrl);
		
		loadModules(model);
				
		odm.setProcessImports(true);
		odm.loadImports(model);

		return model;
	}
	
	private static void loadModules(OntModel model) {
		
		////////MODELS//////
		
		//Dolce + CLO
		//model.read("http");
		
		//Altri Moduli
		model.read("file:///E:/Ontologies/dalos/owns.owl");
		model.read("file:///E:/Ontologies/dalos/language-properties-full.owl");
		model.read("file:///E:/Ontologies/dalos/metaconcepts.owl");
		model.read("file:///E:/Ontologies/dalos/metasources.owl");
		
		////////INDIVIDUALS//////
		
		//Concepts
		model.read("file:///E:/Ontologies/dalos/common/links.owl");
		model.read("file:///E:/Ontologies/dalos/common/interconcepts.owl");
		
		//IT Lexicon
		model.read("file:///E:/Ontologies/dalos/IT/individuals.owl");
		model.read("file:///E:/Ontologies/dalos/IT/individuals-word.owl");
		model.read("file:///E:/Ontologies/dalos/IT/lexicalizations.owl");
		//model.read("file:///E:/Ontologies/dalos/IT/sources.owl");

	}

}
