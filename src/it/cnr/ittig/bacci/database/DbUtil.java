package it.cnr.ittig.bacci.database;

import it.cnr.ittig.bacci.classifier.resource.BasicResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;


/**
 * Metodi per aggiornare e interrogare un database a struttura 'jwn'
 * utilizzando oggetti JWN.
 * 
 * Something like a DAO pattern:
 * application objects <--> DAO <--> relational database
 */
public class DbUtil {
	/*
	 * NOTA: 
	 * quando si sfrutta la funzione LAST_INSERT_ID(), ovvero quando
	 * si usa il metodo getLastID(), la sessione deve essere aperta
	 * in precedenza, quindi in quei casi si utilizzano i metodi
	 * OpenSession() e CloseSession() del DatabaseManager, in modo
	 * da essere sicuri di restare nella stessa sessione col db. 
	 */
	
	static private DatabaseManager db = null;

	public static void setDatabaseManager(DatabaseManager dbm) {
		
		db = dbm;
	}
	
	public static Collection getSynsets() {
		
		Vector<String[]> res = db.genericQuery("classifier.lemmi", null);
		
		Collection data = new HashSet<BasicResource>();
		if(res == null) {
			return data;
		}

		//idfonte, idconcetto1, idconcetto2, fonte
//		for(int i = 0; i < res.size(); i++) {
//			String[] row = res.get(i);
//			Concetto c1 = data.get(row[1]);
//			Fonte item = new Fonte(row[3]);
//			item.setID(row[0]);
//			if(row[2] != null) {
//				Concetto c2 = data.get(row[2]);
//				item.setRelatedSynset(c2);
//			}
//			c1.add(item);		
//		}

		return data;
	}
	

}
