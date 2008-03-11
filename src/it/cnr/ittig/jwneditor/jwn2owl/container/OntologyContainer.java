package it.cnr.ittig.jwneditor.jwn2owl.container;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelMaker;

public interface OntologyContainer {

	public String getNameSpace();
	
	public OntModel getOntModel(boolean withReasoner);
	
	public void resetModel();
}
