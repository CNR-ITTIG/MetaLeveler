package it.cnr.ittig.bacci.lexicon.decorator;

import java.util.Map;

public class AlignedLexicon extends LexiconDecorator {

	public AlignedLexicon(Lexicon component) {

		super(component);
		
		//init...
	}
	
	private String lang = null;	
	private Map<String, AlignedLexicon> langToLexicon = null;

	public AlignedLexicon getAlignedLexicon(String lang) {
		return langToLexicon.get(lang);
	}

	public String getLang() {
		return lang;
	}
	
	public void getInfo() {
		super.getInfo();
		System.out.print("< AlignedLexicon >");
	}

}
