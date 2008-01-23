package it.cnr.ittig.leveler.importer;

import java.io.IOException;

public interface metaImporter {

	/**
	 * Create synset (Concetto object) and add it
	 * to the synset collection. Create alse its
	 * lexical forms.
	 * 
	 * @throws IOException
	 */
	public void createSynsets() throws IOException;
	
	/**
	 * Add hypo/hyper relations between synsets.
	 * 
	 * @throws IOException
	 */
	public void addIpo() throws IOException;
	
	/**
	 * Add fuzzynym relations between synsets.
	 * 
	 * @throws IOException
	 */
	public void addRelated() throws IOException;
	
	/**
	 * Add references to document corpus.
	 * 
	 * @throws IOException
	 */
	public void addRif() throws IOException;
	
	/**
	 * Add interlinguistic relations between terms.
	 * 
	 * @throws IOException
	 */
	public void addAlignment() throws IOException;
	
}
