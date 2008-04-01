package it.cnr.ittig.bacci.lexicon;

public interface Mappable {
	
	public String getCode();
	
	public MappedLexicon getMappedLexicon(String code);
	
}