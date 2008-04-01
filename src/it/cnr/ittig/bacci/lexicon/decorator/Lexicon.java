package it.cnr.ittig.bacci.lexicon.decorator;

import it.cnr.ittig.bacci.lexicon.AlignedLexicon;

import java.util.Collection;
import java.util.Map;

public abstract class Lexicon {

	//BASE LEXICON
	private String name;
	private String ns;	
	private String fileName;
	private String version;
	
	private Collection synsets;
	
	public Collection getSynsets() {
		return synsets;
	}
	
	//CLASSIFIED LEXICON
	private String ontologyBaseNs = null;
	private String ontologyUrl = null;
	private Collection classes = null;
	
	public Collection getOntoClasses() {		
		return classes;
	}

	public String getOntologyBaseNs() {
		return ontologyBaseNs;
	}

	public String getOntologyUrl() {
		return ontologyUrl;
	}
	
	//ALIGNED LEXICON
	private String lang = null;	
	private Map<String, AlignedLexicon> langToLexicon = null;

	public AlignedLexicon getAlignedLexicon(String lang) {
		return langToLexicon.get(lang);
	}

	public String getLang() {
		return lang;
	}
	
	//MAPPED LEXICON
	private String code = null;	
	private Map<String, MappedLexicon> codeToLexicon = null;

	public MappedLexicon getMappedLexicon(String code) {		
		return codeToLexicon.get(code);
	}

	public String getCode() {
		return code;
	}
	
	//COMMON METHODS
	abstract public void getInfo();
}
