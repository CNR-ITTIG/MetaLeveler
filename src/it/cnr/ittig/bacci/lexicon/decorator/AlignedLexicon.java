package it.cnr.ittig.bacci.lexicon.decorator;

public class AlignedLexicon extends LexiconDecorator {

	public AlignedLexicon(Lexicon component) {

		super(component);
		
		//init...
	}
	
	public void getInfo() {
		super.getInfo();
		System.out.print("< AlignedLexicon >");
	}

}
