package it.cnr.ittig.bacci.lexicon.decorator;

import java.util.Collection;

public class RunLex {

	public RunLex() {
		
		System.out.println("Lexicon Decorator...");
		
		Lexicon bl = new BaseLexicon();
		Lexicon al = new AlignedLexicon(bl);
		Lexicon cl = new ClassifiedLexicon(al);
		Lexicon ml = new MappedLexicon(cl);
		
		System.out.println("getInfo()...\n");
		ml.getInfo();
		
		System.out.println("\nbl.getOntoClasses()...\n");
		Collection classes = bl.getOntoClasses();
		System.out.println(classes);
		
		System.out.println("\ncl.getOntoClasses()...\n");
		classes = al.getOntoClasses();
		System.out.println(classes);
		
		System.out.println("\nal.getOntoClasses()...\n");
		classes = cl.getOntoClasses();
		System.out.println(classes);
		
		System.out.println("\nml.getOntoClasses()...\n");
		classes = ml.getOntoClasses();
		System.out.println(classes);
	}
}
