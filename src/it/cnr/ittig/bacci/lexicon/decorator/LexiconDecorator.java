package it.cnr.ittig.bacci.lexicon.decorator;

public class LexiconDecorator extends Lexicon {
	
	Lexicon component = null;
	
	public LexiconDecorator(Lexicon component) {
		
		this.component = component;
	}

}
