package it.cnr.ittig.bacci.database;

import it.cnr.ittig.bacci.database.query.QueryResolver;
import it.cnr.ittig.jwneditor.editor.util.UtilEditor;

import java.util.Iterator;
import java.util.Vector;

/**
 * Classe di interfaccia tra applicazione e database.
 * 
 * @author lorenzo bacci
 */
public class DatabaseManager {

	private Database classDatabase;
	
	Vector<QueryResolver> registeredQuery = null;
	
	public DatabaseManager() {
		
		classDatabase = null;		
		registeredQuery = new Vector<QueryResolver>();
	}
	
	public boolean initDatabase(String[] data) {
		// data[]: nomeUtente, password, nomeDataBase, indirizzoDataBase, tipoDataBase

		if(data == null) {
			System.err.append("setDatabase - data is null!!??");
			return false;
		}
		
		classDatabase = new Database(data);

		if(classDatabase.connetti()) {
			classDatabase.disconnetti();
			return true;
		} else {
			System.err.println("Database last error msg: " +
					classDatabase.getErrore());
			return false;
		}
	}
	
	public boolean runUpdate(String queryType) {
		
		return runUpdate(queryType, null);
	}
	
	public Vector<String[]> runQuery(String queryType) {
		
		return runQuery(queryType, null);
	}
	
	/**
	 * Esegue un update in una singola sessione:
	 * connessione, update, disconnessione.
	 * @param queryType codice query
	 * @param data parametri aggiuntivi per la query
	 * @return 
	 * False se la connessione al db fallisce, true altrimenti.
	 */
	public boolean runUpdate(String queryType, String[] data) {
		
		boolean res = false;
		String query = getQuery(queryType, data);
		if(query.equals("")) return false;			

		if(classDatabase == null) {
			System.err.println("Database connection failed!");
		}
		
		if(classDatabase.connetti()) {
			UtilEditor.debug("runUpdate() - type:" + queryType + " QUERY:" + query);
			res = classDatabase.eseguiAggiornamento(query);
			classDatabase.disconnetti();
		} else {
			//Connessione al database fallita
			System.err.println("Database connection failed!");
		}
		
		return res;
	}
		
	/**
	 * Esegue una query in una singola sessione:
	 * connessione, query, disconnessione.
	 * @param queryType codice query
	 * @param data parametri aggiuntivi per la query
	 * @return 
	 * Un vettore di String[] che rappresenta i risultati
	 * in forma di tabella oppure null se la connessione fallisce. 
	 */
	 public Vector<String[]> runQuery(String queryType, String[] data) {
		
		Vector<String[]> res = null;
		String query = getQuery(queryType, data);
		if(query.equals("")) return null;			
		
		if(classDatabase == null) {
			System.err.println("Database connection failed!");
		}
		
		if(classDatabase.connetti()) {
			UtilEditor.debug("runQuery() - type:" + queryType + " QUERY:" + query);

			try{
    			res = classDatabase.eseguiQuery(query);
    		} catch(Exception ex) {
    			System.err.println(ex.getMessage());
    			ex.printStackTrace();
    		} finally {
    			classDatabase.disconnetti();
    		}

		} else {
			//Connessione al database fallita
			System.err.println("Database connection failed!");
			return null;
		}
		if(res==null) {
			System.err.println("Results vector is null!? TYPE:" + queryType);
			return null;
		}
		
		return res;
	}
	
	/**
	 * Controlla se esiste gi� una connessione al db aperta.
	 * Esegue l'update nella connessione esistente oppure
	 * apre una nuova connessione, esegue l'update e poi
	 * si disconnette.
	 * @param queryType codice query
	 * @param data parametri aggiuntivi per la query
	 * @return 
	 * False se la connessione al db fallisce, true altrimenti.
	 */
	 public boolean genericUpdate(String queryType, String[] data) {
		if(classDatabase.isConnesso()) {
			return connectedUpdate(queryType, data);
		} else {
			return runUpdate(queryType, data);
		}
	}
	
	/**
	 * Controlla se esiste gi� una connessione al db aperta.
	 * Esegue la query nella connessione esistente oppure
	 * apre una nuova connessione, esegue la query e poi
	 * si disconnette.
	 * @param queryType codice query
	 * @param data parametri aggiuntivi per la query
	 * @return 
	 * Un vettore di String[] che rappresenta i risultati
	 * in forma di tabella oppure null se la connessione fallisce. 
	 */
	 public Vector<String[]> genericQuery(String queryType, String[] data) {
		if(classDatabase.isConnesso()) {
			return connectedQuery(queryType, data);
		} else {
			return runQuery(queryType, data);
		}
	}
	
	/**
	 * Esegue l'update sfruttando una connessione con il db 
	 * precedentemente creata.
	 * Nota: meglio usare sempre genericUpdate().
	 * @param queryType codice query
	 * @param data parametri aggiuntivi per la query
	 * @return 
	 * False se la connessione al db fallisce oppure se la connessione
	 * non era gi� stata creata, true altrimenti.
	 */
	 public boolean connectedUpdate(String queryType, String[] data) {
		
		boolean res = false;
		String query = getQuery(queryType, data);
		if(query.equals("")) return false;

		if(classDatabase == null) {
			System.err.println("Database connection failed!");
		}
		
		if(classDatabase.isConnesso()) {
			UtilEditor.debug("connectedUpdate() - type:" + queryType + " QUERY:" + query);
			res = classDatabase.eseguiAggiornamento(query);
		} else {
			//Connessione al database fallita
			System.err.println("connectedUpdate() - no active session!");
		}
		
		return res;
	}
	
	
	/**
	 * Esegue la query sfruttando una connessione con il db 
	 * precedentemente creata.
	 * Nota: meglio usare sempre genericQuery().
	 * @param queryType codice query
	 * @param data parametri aggiuntivi per la query
	 * @return 
	 * Un vettore di String[] che rappresenta i risultati
	 * in forma di tabella oppure null se la connessione fallisce. 
	 */
	 public Vector<String[]> connectedQuery(String queryType, String[] data) {
		
		Vector<String[]> res = null;
		String query = getQuery(queryType, data);
		if(query.equals("")) return null;
		
		if(classDatabase == null) {
			System.err.println("Database connection failed!");
		}
		
		if(classDatabase.isConnesso()) {
			UtilEditor.debug("connectedQuery() - type:" + queryType + " QUERY:" + query);
			res = classDatabase.eseguiQuery(query);
		} else {
			//Connessione al database fallita
			System.err.println("connectedQuery() - no active session!");
			return null;
		}
		
		if(res==null) {
			System.err.println("Results vector is null! TYPE:" + queryType);
			return null;
		}
		
		return res;
	}
	
	/**
	 * Crea una nuova connessione attiva con il database. 
	 * @return 
	 * False se la connessione fallisce, true altrimenti.
	 */
	 public boolean OpenSession() {
		 if(classDatabase.isConnesso()) {
			 return true;
		 }
		 if(classDatabase.connetti()) {
			 return true;
		 } else {
			 //Connessione al database fallita
			 System.err.println("Database connection failed!");
			 return false;
		 }
	 }
	
	/**
	 * Controlla se era stata creata in precedenza una 
	 * connessione attiva con il database. In caso 
	 * affermativo, chiudila. 
	 * @return 
	 * False se la connessione fallisce oppure
	 * nessuna connessione creata in precedenza,
	 * true altrimenti.
	 */
	 public boolean CloseSession() {
		if(classDatabase.isConnesso() == false) {
			return false;
		}
		classDatabase.disconnetti();
		return true;
	}
	
	/**
	 * Richiede informazioni e parametri di collegamento
	 * del database.
	 * @return
	 * Array di String con nome utente, nome database, 
	 * indirizzo e tipo di database.
	 */
	public String[] getInfo() {
		
		if(classDatabase == null) {
			return null;
		} else {
			return classDatabase.getInfo();
		}
	}
	
	public boolean checkConnection() {
		
		if(classDatabase == null) {
			return false;
		}
		if(classDatabase.connetti()) {
			classDatabase.disconnetti();
			return true;
		}
		return false;
	}
	
	private String getQuery(String type, String[] data) {

		String query = "";
		for(Iterator<QueryResolver> i = registeredQuery.iterator(); i.hasNext();) {
			QueryResolver item = i.next();
			query = item.getQueryString(type, data);
			if(!query.equals("")) break;
		}
		
		if(query.equals("")) {
			System.err.println("getQuery() - Type not found or data error - TYPE:" + type);
		}
		
		return query;
	}
	
	public boolean addQueryResolver(QueryResolver obj) {
		
		if(registeredQuery != null && !registeredQuery.contains(obj)) {
			registeredQuery.add(obj);
			return true;
		}
		return false;
	}
}
