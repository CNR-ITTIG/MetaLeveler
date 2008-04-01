package it.cnr.ittig.bacci.lexicon.decorator;

public class ClassifiedLexicon extends LexiconDecorator {
	
	public ClassifiedLexicon(Lexicon component) {

		super(component);
		
		//init classes...
	}
	
	public void getInfo() {
		super.getInfo();
		System.out.print("< ClassifiedLexicon >");
	}
}
