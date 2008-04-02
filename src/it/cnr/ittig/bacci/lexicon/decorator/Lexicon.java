package it.cnr.ittig.bacci.lexicon.decorator;

import java.util.Collection;
import java.util.Collections;

/**
 * Lexicons management implemented with 'Decorator' pattern.
 * 
 * @author Lorenzo Bacci
 */
public abstract class Lexicon {

	//BASE LEXICON
	protected String name = null;
	protected String ns = null;	
	protected String fileName = null;
	protected String version = null;
	
	protected Collection synsets = null;
	
	public Collection getSynsets() {
		return Collections.unmodifiableCollection(synsets);
	}
	
	//CLASSIFIED LEXICON
	abstract public Collection getOntoClasses();
	abstract public String getOntologyBaseNs();
	abstract public String getOntologyUrl();

	//ALIGNED LEXICON
	abstract public AlignedLexicon getAlignedLexicon(String lang);
	abstract public String getLang();

	//MAPPED LEXICON
	abstract public MappedLexicon getMappedLexicon(String code);
	abstract public String getCode();

	//COMMON METHODS
	abstract public void getInfo();

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNs() {
		return ns;
	}

	public void setNs(String ns) {
		this.ns = ns;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
