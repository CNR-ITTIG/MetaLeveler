package it.cnr.ittig.jwneditor.jwn;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class Concept {

	private Collection<Concept> cohypoConcepts;
	private Collection<Concept> eqsynConcepts;
	private Collection<Concept> fuzzyConcepts;
	private Collection<Concept> hyperConcepts;
	
	private String URI;
	
	public Concept(String uri) {
		
		URI = uri;
		
		cohypoConcepts = new HashSet<Concept>();
		eqsynConcepts = new HashSet<Concept>();
		fuzzyConcepts = new HashSet<Concept>();
		hyperConcepts = new HashSet<Concept>();


	}

	public String getURI() {
		
		return URI;
	}
	
	public Collection<Concept> getCohypoConcepts() {
		return Collections.unmodifiableCollection(cohypoConcepts);
	}

	public Collection<Concept> getEqsynConcepts() {
		return Collections.unmodifiableCollection(eqsynConcepts);
	}

	public Collection<Concept> getFuzzyConcepts() {
		return Collections.unmodifiableCollection(fuzzyConcepts);
	}

	public Collection<Concept> getHyperConcepts() {
		return Collections.unmodifiableCollection(hyperConcepts);
	}
	
	public boolean addCohypoConcept(Concept l) {
		return cohypoConcepts.add(l);
	}
	
	public boolean addEqsynConcept(Concept l) {
		return eqsynConcepts.add(l);
	}

	public boolean addFuzzyConcept(Concept l) {
		return fuzzyConcepts.add(l);
	}

	public boolean addHyperConcept(Concept l) {
		return hyperConcepts.add(l);
	}


}
