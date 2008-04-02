package it.cnr.ittig.bacci.lexicon.decorator;

import java.util.Map;

public class MappedLexicon extends LexiconDecorator {

	public MappedLexicon(Lexicon component) {

		super(component);
		
		//init...
	}
	
	private String code = null;	
	private Map<String, MappedLexicon> codeToLexicon = null;

	public MappedLexicon getMappedLexicon(String code) {		
		return codeToLexicon.get(code);
	}

	public String getCode() {
		return code;
	}
	

	public void getInfo() {
		super.getInfo();
		System.out.print("< MappedLexicon >");
	}
}
