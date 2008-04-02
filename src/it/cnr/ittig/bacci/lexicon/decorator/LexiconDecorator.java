package it.cnr.ittig.bacci.lexicon.decorator;

import java.util.Collection;

public class LexiconDecorator extends Lexicon {
	
	Lexicon component = null;
	
	public LexiconDecorator(Lexicon component) {
		
		this.component = component;
	}

	public void getInfo() {
		component.getInfo(); //...does the trick!
	}

	public AlignedLexicon getAlignedLexicon(String lang) {
		return component.getAlignedLexicon(lang);
	}

	public String getCode() {
		return component.getCode();
	}

	public String getLang() {
		return component.getLang();
	}

	public MappedLexicon getMappedLexicon(String code) {
		return component.getMappedLexicon(code);
	}

	public Collection getOntoClasses() {
		return component.getOntoClasses();
	}

	public String getOntologyBaseNs() {
		return component.getOntologyBaseNs();
	}

	public String getOntologyUrl() {
		return component.getOntologyUrl();
	}
	
}
