package it.cnr.ittig.jwneditor.jwn2owl.container;

import com.hp.hpl.jena.rdf.model.ModelMaker;

public class InMemoryOntology extends AbstractOntology {

	public InMemoryOntology(String mName){
		
		modelName = mName;
	}
	
	public String getNameSpace() {
		
		return modelName + "#";
	}
	
	protected void loadSchema() {		
	}
	
	public void resetModel() {
	}
	
	protected ModelMaker getModelMaker() {
		
		return null;
	}
}
