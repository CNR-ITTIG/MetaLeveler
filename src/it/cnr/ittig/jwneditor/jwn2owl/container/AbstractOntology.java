package it.cnr.ittig.jwneditor.jwn2owl.container;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

public abstract class AbstractOntology implements OntologyContainer {
	
	protected ModelMaker maker = null;

	protected String modelName;

	/*
	 * 
	 */
	abstract protected void loadSchema();

	/*
	 * 
	 */
	abstract protected ModelMaker getModelMaker();
	
	/*
	 * 
	 */
	protected OntModelSpec getModelSpec(ModelMaker maker, boolean useReasoner) {
		
		OntModelSpec spec;
		
		if(useReasoner) {
			//use Pellet Reasoner
	        spec = new OntModelSpec(PelletReasonerFactory.THE_SPEC);
		} else {
			spec = new OntModelSpec( OntModelSpec.OWL_MEM);
		}
		
		spec.setImportModelMaker(maker);
		
		return spec;
	}
	
	public String getModelName() {
		
		return modelName;
	}
		
	/*
	 * 
	 */
	public OntModel getOntModel(boolean withReasoner) {
		
		Model base = maker.openModel(modelName, true);
	
		return ModelFactory.createOntologyModel(getModelSpec(maker, 
				withReasoner), base);
	}	

}	

