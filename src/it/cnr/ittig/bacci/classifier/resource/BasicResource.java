package it.cnr.ittig.bacci.classifier.resource;

import java.util.Collection;
import java.util.HashSet;


public class BasicResource extends WebResource {

	private Collection<ConceptClass> concepts;

	public BasicResource() {
		super();

		concepts = new HashSet<ConceptClass>();
	}

	public Collection<ConceptClass> getConcepts() {
		
		return concepts;
	}

	public boolean addConcept(ConceptClass cc) {
		
		return concepts.add(cc);
	}
	
	public boolean removeConcept(ConceptClass cc) {
		
		return concepts.remove(cc);
	}
}
