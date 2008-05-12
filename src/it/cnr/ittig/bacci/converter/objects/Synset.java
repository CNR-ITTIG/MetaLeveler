package it.cnr.ittig.bacci.converter.objects;

import it.cnr.ittig.bacci.classifier.resource.WebResource;

import java.util.*;

public class Synset extends WebResource {
	
	private String gloss;	
	
	private String id;			//id all'interno del database	
	private String synsetId;	//synsetId property sulla metalevel ontology
	
	private Collection<Lemma> lemmas;
	private Collection<Source> sources;
	private Collection<Concept> concepts;	
	
	//USARE DEGLI HASHMAP INVECE DI RELATIONSYNSET ?????????
	private Collection<RelationSynset> relationToSynsets;
		
	private String language;
	
	private Lemma conceptLemma;
	
	public Synset() {
		
		id = "";
			
		lemmas = new TreeSet<Lemma>(); 
		//correlazioni = new HashSet<RelationSynset>();
		concepts = new HashSet<Concept>();
		sources = new HashSet<Source>();		
	}

	public void setID(String newId) { id = newId; }
	public String getID() {	return id; }
	
	public void setGloss(String gloss) { this.gloss = gloss; }
	public String getGloss() { return gloss; }
	
	public void setLang(String data) { language = data; }
	public String getLang() { return language; }
	
	public boolean addSource(Source source) {		
		return sources.add(source);
	}
	
	public boolean removeSource(Source source) {		
		return sources.remove(source);
	}
	
	public boolean add(Lemma l) {
		return lemmas.add(l);
	}

	public boolean remove(Lemma l) {
		return lemmas.remove(l);
	}

	public String getSynsetId() {
		return synsetId;
	}

	public void setSynsetId(String synsetId) {
		this.synsetId = synsetId;
	}

	public Collection<Lemma> getLemmas() {
		return Collections.unmodifiableCollection(lemmas);
	}

	public Collection<Source> getSources() {
		return Collections.unmodifiableCollection(sources);
	}
}
