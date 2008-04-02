package it.cnr.ittig.bacci.lexicon.decorator;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

public class ClassifiedLexicon extends LexiconDecorator {
	
	public ClassifiedLexicon(Lexicon component) {

		super(component);
		
		classes = new Vector();
	}
	
	private String ontologyBaseNs = null;
	private String ontologyUrl = null;
	private Collection classes = null;
	
	public Collection getOntoClasses() {		
		return Collections.unmodifiableCollection(classes);
	}

	public String getOntologyBaseNs() {
		return ontologyBaseNs;
	}

	public String getOntologyUrl() {
		return ontologyUrl;
	}
	
	public void getInfo() {
		super.getInfo();
		System.out.print("< ClassifiedLexicon >");
	}
}
