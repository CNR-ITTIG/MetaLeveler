package it.cnr.ittig.bacci.lexicon.decorator;

import java.util.Collection;
import java.util.HashSet;

public class BaseLexicon extends Lexicon {
	
	public BaseLexicon() {
		
		synsets = new HashSet();
	}

	public void getInfo() {		
		System.out.print("< Lexicon >");
	}

	@Override
	public AlignedLexicon getAlignedLexicon(String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLang() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MappedLexicon getMappedLexicon(String code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection getOntoClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOntologyBaseNs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOntologyUrl() {
		// TODO Auto-generated method stub
		return null;
	}

}
