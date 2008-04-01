package it.cnr.ittig.bacci.lexicon;

public interface Alignable {

	public String getLang();
	
	public AlignedLexicon getAlignedLexicon(String lang);
}
