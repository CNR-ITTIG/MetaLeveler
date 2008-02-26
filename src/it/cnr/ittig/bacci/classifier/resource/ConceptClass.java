package it.cnr.ittig.bacci.classifier.resource;

import java.util.Collection;
import java.util.TreeSet;

public class ConceptClass extends WebResource {

	private Collection<OntologicalClass> classes;
	
	public ConceptClass() {
		super();

		classes = new TreeSet<OntologicalClass>();
	}

	public Collection<OntologicalClass> getClasses() {
		return classes;
	}
	
	public boolean addClass(OntologicalClass oc) {
		
		return classes.add(oc);
	}
	
	public boolean removeClass(OntologicalClass oc) {
		
		return classes.remove(oc);
	}
}
