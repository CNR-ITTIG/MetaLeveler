package it.cnr.ittig.bacci.converter.objects;

import it.cnr.ittig.bacci.classifier.resource.WebResource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class Concept extends WebResource {

	private Collection<Concept> cohypoConcepts;
	private Collection<Concept> eqsynConcepts;
	private Collection<Concept> fuzzyConcepts;
	private Collection<Concept> hyperConcepts;
	
//	private String URI;
	
	//Links to ontological classes
	private Collection<OntoClass> links;
	
	//Links to synset in different languages
	private Collection<Synset> terms;
	
//	public Concept(String uri) {
//		
//		URI = uri;
		
	public Concept() {
		
		super();
		
		cohypoConcepts = new HashSet<Concept>();
		eqsynConcepts = new HashSet<Concept>();
		fuzzyConcepts = new HashSet<Concept>();
		hyperConcepts = new HashSet<Concept>();

		links = new HashSet<OntoClass>();
		terms = new HashSet<Synset>();

	}

//	public String getURI() {
//		
//		return URI;
//	}
	
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

	public Collection<OntoClass> getLinks() {
		return Collections.unmodifiableCollection(links);
	}
	
	public Collection<Synset> getTerms() {
		return Collections.unmodifiableCollection(terms);
	}
	
	public boolean addLink(OntoClass oc) {
		
		return links.add(oc);
	}
	
	public boolean removeLink(OntoClass oc) {
		
		return links.remove(oc);
	}
	
	public boolean addTerm(Synset syn) {
		
		return terms.add(syn);
	}
	
	public boolean removeTerm(Synset syn) {
		
		return terms.remove(syn);
	}

}
