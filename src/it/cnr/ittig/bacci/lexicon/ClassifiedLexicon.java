package it.cnr.ittig.bacci.lexicon;

import java.util.Collection;

public class ClassifiedLexicon extends Lexicon 
	implements Classifiable {

	private Collection classes = null;
	
	public Collection getOntoClasses() {
		
		return classes;
	}

	public String getOntologyBaseNs() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOntologyUrl() {
		// TODO Auto-generated method stub
		return null;
	}

}
