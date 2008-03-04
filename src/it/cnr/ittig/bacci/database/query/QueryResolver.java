package it.cnr.ittig.bacci.database.query;

/**
 * Le interrogazioni al database avvengono utilizzando
 * oggetti di tipo QueryResolver.
 * 
 * @author lorenzo bacci
 *
 */
public interface QueryResolver {

	/*
	 * Deve ritornare una query SQL (String) in base al codice "type".
	 * Lo string[] "data" contiene gli eventuali argomenti per la query.
	 */
	public String getQueryString(String type, String[] data);
	
}
