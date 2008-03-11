package it.cnr.ittig.leveler.importer;

import it.cnr.ittig.bacci.database.DatabaseManager;
import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn.Lemma;
import it.cnr.ittig.leveler.Leveler;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

public class IttigDatabaseImporter extends ImporterUtil 
	implements MetaImporter {
	
	private DatabaseManager dbm;
	
	public IttigDatabaseImporter() {

		dbm = EditorConf.DBM;
	}

	public void createSynsets() throws IOException {
		
		Vector<String[]> results = new Vector<String[]>();
		results = dbm.runQuery("classifier.glossario");
		
		for(Iterator<String[]> i = results.iterator(); 
				i.hasNext();) {
			String[] record = i.next();
			String id = record[0];
			String proto = record[1]; //lexical?
			String tot = record[2];
			String lexical = record[3]; //proto?
			String stop = record[4];
			String onto = record[5];
			String ittig = record[6];
			
			Concetto conc = new Concetto();
			conc.setID(id);
			Lemma lemma = new Lemma(proto);
			lemma.setLemmaLang(EditorConf.LANGUAGE);
			conc.add(lemma);
			Leveler.appSynsets.put(id, conc);
			lemma.variants.add(lexical);
			
			if(onto.equalsIgnoreCase("s")) {
				conc.setCandidate(true);
			}
		}
	}
	

	public void addAlignment() throws IOException {
	}

	public void addIpo() throws IOException {
	}

	public void addRelated() throws IOException {
		//Parse every relation...

		Vector<String[]> results = new Vector<String[]>();
		results = dbm.runQuery("classifier.relazioni");
		
		for(Iterator<String[]> i = results.iterator(); 
				i.hasNext();) {
			String[] record = i.next();
			String idRel = record[0];
			String idFrom = record[1];
			String relName = record[2];
			String idTo = record[3];
			
			Concetto c1 = Leveler.appSynsets.get(idFrom);
			Concetto c2 = Leveler.appSynsets.get(idTo);
			if(c1 == null || c2 == null) {
				System.out.println(">>WARNING<< NULL - " 
						+ c1 + " id:" + idFrom + " - "
						+ c2 + " id:" + idTo);
				continue;
			}
			//Add new relation
			if(!addSingleRelation(c1, c2, relName)) {
				System.err.println("Relation not found: " + relName + " !");
				break;
			}
		}		
	}

	public void addRif() throws IOException {
	}

}
