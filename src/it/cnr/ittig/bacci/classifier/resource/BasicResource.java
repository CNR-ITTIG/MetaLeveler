package it.cnr.ittig.bacci.classifier.resource;



public class BasicResource extends WebResource {

	private ConceptClass concept;

	public BasicResource() {
		super();

		concept = null;
	}

	public ConceptClass getConcept() {
		
		return concept;
	}

	public void setConcept(ConceptClass cc) {
		
		concept = cc;
	}	
}
