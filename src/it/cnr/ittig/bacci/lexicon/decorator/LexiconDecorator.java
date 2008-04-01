package it.cnr.ittig.bacci.lexicon.decorator;

public abstract class LexiconDecorator extends Lexicon {
	
	Lexicon component = null;
	
	public LexiconDecorator(Lexicon component) {
		
		this.component = component;
	}

	public void getInfo() {
		component.getInfo(); //...does the trick!
	}
}
