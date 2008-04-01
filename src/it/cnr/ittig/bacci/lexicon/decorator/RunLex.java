package it.cnr.ittig.bacci.lexicon.decorator;

public class RunLex {

	public RunLex() {
		
		System.out.println("Lexicon Decorator...");
		
		Lexicon bl = new BaseLexicon();
		Lexicon cl = new ClassifiedLexicon(bl);
		Lexicon al = new AlignedLexicon(cl);
		Lexicon ml = new MappedLexicon(al);
		
		System.out.println("getInfo()...\n");
		ml.getInfo();
	}
}
