package it.cnr.ittig.jwneditor.jwn2owl.container;

import it.cnr.ittig.jwneditor.editor.EditorConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

public class InMemoryOntology extends AbstractOntology {

	String namespace = EditorConf.onto_work + "#";
	
	private OntModel om;
	
	public InMemoryOntology(String mName){
		
		modelName = mName;
		
		om = null;
		
		//Answer a model maker
		maker = getModelMaker();
		
		Model base = maker.createModel(modelName, false);
		om = ModelFactory.createOntologyModel(
				getModelSpec(maker, false), base);
	}
	
	public String getNameSpace() {
		
		return namespace;
	}
	
	protected void loadSchema() {		
	}
	
	public void resetModel() {
	}
	
	public ModelMaker getModelMaker() {
		
		if(maker == null) {
			maker = ModelFactory.createMemModelMaker();
		}
		
		return maker;
	}
	
	public OntModel getOntModel(boolean withReasoner) {
	
		return om;
	}
	
	public void setOntModel(OntModel om) {
	
		this.om = om;
	}
}
