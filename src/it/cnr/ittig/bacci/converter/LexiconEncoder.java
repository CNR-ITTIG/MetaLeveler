package it.cnr.ittig.bacci.converter;

import it.cnr.ittig.jwneditor.editor.EditorConf;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

public class LexiconEncoder {

	private String mdbFileName = EditorConf.MDB_FILE_NAME;
	
	private void readDb() {
		
		//LEGGI DATABASE
		Connection c = openConnection();		
		
		String sql = "SELECT T1.kwid, T1.forma_variante " +
			"FROM wbt_app_glossario_varianti T1 " +
			"";	

		//Collection<Synset> dbSynsets = new HashSet<Synset>();
		
		Vector<String[]> results = eseguiQuery(c, sql);

		int nullCounter = 0;
		for(Iterator<String[]> i = results.iterator(); i.hasNext(); ) {
			String[] row = i.next();

			String kwid = row[0].trim();
			String variant = row[1].trim();
			
		}		
		System.err.println("NULL COUNTER:" + nullCounter);
		
		closeConnection(c);
		
	}
	
	private Connection openConnection() {
		
		Connection c = null;
		Properties prop = new Properties();
		//prop.setProperty("DB2e_ENCODING", "CP-1252");
		prop.setProperty("DB2e_ENCODING", "Windows-1252");
		
		try {
			String strConn = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + mdbFileName;
			Driver d = (Driver)Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
			c = DriverManager.getConnection(strConn, prop);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return c;
	}
	
	private void closeConnection(Connection c) {
		
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private Vector<String[]> eseguiQuery(Connection c, String query) {
		System.out.println("Exec query: " + query);
		Vector<String[]> v = null;
		String[] record;
		int colonne = 0;
		try {
			Statement stmt = (Statement) c.createStatement();
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
		}
		return v;
	}

}
