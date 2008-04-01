package it.cnr.ittig.bacci.lexicon.decorator;

public class MappedLexicon extends LexiconDecorator {

	public MappedLexicon(Lexicon component) {

		super(component);
		
		//init...
	}
	
	public void getInfo() {
		super.getInfo();
		System.out.print("< MappedLexicon >");
	}
}
