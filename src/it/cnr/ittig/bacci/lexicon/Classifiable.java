package it.cnr.ittig.bacci.lexicon;

import java.util.Collection;

public interface Classifiable {

	public String getOntologyBaseNs();
	
	public String getOntologyUrl();
	
	public Collection getOntoClasses();
}
