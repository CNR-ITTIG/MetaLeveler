package it.cnr.ittig.bacci.database;

import java.sql.DriverManager;
import java.util.Vector;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

/**
 * Collegamento e interrogazione del database.
 *  
 * @author lorenzo
 * TODO
 * Implementare gestione altri dbms (oltre mySQL)
 */
public class Database {
	private String nomeDB, username, address, errore, strpass, tipoDB;
	char[] password;
	private boolean connesso;
	private Connection db;
	
	public Database(String[] data) {
		username = data[0];
		strpass = data[1];
		nomeDB = data[2];
		address = data[3];
		tipoDB = data[4];
	}
	
	/*
	 * Apre la connessione con il Database
	 */
	public boolean connetti() {
		connesso = false;
		try {
			Class.forName("com.mysql.jdbc.Driver");

			//if (!nomeDB.equals("")) {
				String strConn = "jdbc:mysql://" + address + "/" + nomeDB +
						"?user=" + username + "&password=" + strpass;
				db = (Connection) DriverManager.getConnection(strConn);
			//}
			connesso = true;
		} catch (Exception e) { 
			errore = e.getMessage();
		}
		return connesso;
	}

	/*
	 * Esegue una query di tipo SELECT.
	 * Ritorna un Vector di String[].
	 */
	public Vector<String[]> eseguiQuery(String query) {
		Vector<String[]> v = null;
		String[] record;
		int colonne = 0;
		try {
			Statement stmt = (Statement) db.createStatement();
			ResultSet rs = (ResultSet) stmt.executeQuery(query);
			v = new Vector<String[]>();
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			colonne = rsmd.getColumnCount();

			while(rs.next()) {
				record = new String[colonne];
				for (int i = 0; i < colonne; i++) {
					record[i] = rs.getString(i+1);
				}
				//Always avoid clone() method ??
				//v.add( record.clone() );
				v.add(record);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			errore = e.getMessage(); 
		}

		return v;
	}

	/*
	 * Esegue una query di tipo UPDATE.
	 * Ritorna false in caso di errore/eccezione.
	 */
	public boolean eseguiAggiornamento(String query) {
		boolean risultato = false;
		try {
			Statement stmt = (Statement) db.createStatement();
			stmt.executeUpdate(query);
			risultato = true;
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			errore = e.getMessage();
			risultato = false;
		}
		return risultato;
	}
	
	/*
	 * Chiude la connessione con il Database
	 */
	public void disconnetti() {
		try {
			db.close();
			connesso = false;
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}

	public boolean isConnesso() { 
		return connesso; 
	}
	
	public String getErrore() { 
		return errore; 
	}

	public String[] getInfo() {

		return new String[]{username, nomeDB, address, tipoDB};
	}

}
