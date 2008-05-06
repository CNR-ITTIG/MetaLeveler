package it.cnr.ittig.bacci.converter.objects;

import java.util.*;

public class Synset implements Comparable { //dovrebbe estendere WebResource...
	
	private String definizione;	
	
	private String id;			//id all'interno del database
	private String tmpId;		//id temporaneo (utile in fase di importazione)
	
	private String URI;
	
	private String datains;
	private String datamod;
	private String timemod;
	
	private boolean candidate;
	
	public Vector<Lemma> lemmi;
	public Vector<RelationSynset> correlazioni;	//l'ordine conta per la visualizzazione! (Vector, non HashSet)
		
	//Aggiunte per MetaLeveler:
	private Vector<Source> riferimenti;
	public String language;
	public Vector<String> ontoclassi;
	
	public Lemma conceptLemma;
	
	public Synset() {
		this("");
	}

	public Synset(String def) {

		definizione = def;
		
		datains = "";
		datamod = "";
		timemod = "";
		
		id = "";
		tmpId = "";
		
		candidate = false;
		
		lemmi = new Vector<Lemma>(); 
		correlazioni = new Vector<RelationSynset>();
		ontoclassi = new Vector<String>();
		riferimenti = new Vector<Source>();
		
		conceptLemma = null;
	}
	
	//get & set:
	public void setID(String newId) { id = newId; }
	public String getID() {	return id; }
	
	public void setTmpID(String newId) { tmpId = newId; }
	public String getTmpID() { return tmpId; }
	
	public void setDefinizione(String def) { definizione = def; }
	public String getDefinizione() { return definizione; }
	
	public void setDataIns(String data) { datains = data; }
	public String getDataIns() { return datains; }
	
	public void setDataMod(String data) { datamod = data; }
	public String getDataMod() { return datamod; }
	
	public void setTimeMod(String data) { timemod = data; }
	public String getTimeMod() { return timemod; }
	
	public void setLang(String data) { language = data; }
	public String getLang() { return language; }
	
	public Collection<Source> getRiferimenti() {
		return Collections.unmodifiableCollection(riferimenti);
	}
	
	public boolean addSource(Source rif) {
		
		return riferimenti.add(rif);
	}
	
	/*
	 * Add new element at the end of Vector.
	 */
	public boolean add(Lemma l) {
		if(l == null) {
			System.err.println("Synset.add(Lemma) - null value!");
			return false;
		}
		if(lemmi.contains(l)) {
			return false;
		}
		lemmi.add(l);
		l.setOrdine(String.valueOf(lemmi.size()));
		return true;
	}

	/*
	 * Add new element in the specified position of Vector.
	 */
	public boolean add(Lemma l, int index) {
		
		if(l == null) {
			System.err.println("Synset.add(Lemma, index) - null value!");
			return false;
		}
		
		if(lemmi.contains(l)) {
			return false;
		}
		
		//numero della variante (index <=> ordine) 
		l.setOrdine(String.valueOf(index));
		
		int count = 0;
		while(count < lemmi.size()) {
			String thisOrdine = ((Lemma) lemmi.get(count)).getOrdine();
			int actual = Integer.valueOf(thisOrdine);
			if(index > actual) {
				count++;
			} else
				break;
		}
		
		lemmi.add(count, l);

		return true;
	}

	public boolean remove(Lemma l) {
		if(l == null) {
			System.err.println("Synset.remove(Lemma) - null value!");
			return false;
		}
		if(!lemmi.contains(l)) {
			return false;
		}
		lemmi.remove(l);
		return true;
	}
	
	public boolean add(RelationSynset c) {
		if(c == null) {
			System.err.println("Synset.add(RelationSynset) - null value!");
			return false;
		}
		if(correlazioni.contains(c)) {
			return false;
		}
		boolean ins = false;
		//Inserisci la RelationSynset rispettando l'ordine lessicografico
		//(dipende da toString() di RelationSynset, quindi rispetta anche il
		//nome della relazione e il tipo).
		for(int i = 0; i < correlazioni.size(); i++) {
			RelationSynset item = correlazioni.get(i);
			if(item.toString().compareToIgnoreCase(c.toString()) < 0) continue;
			if(item.toString().compareToIgnoreCase(c.toString()) > 0) {
				correlazioni.add(i, c);
				ins = true;
				break;
			}
		}
		if(!ins) {
			//Inserisci alla fine del vettore
			correlazioni.add(c);
		}
		return true;
	}

	public boolean remove(RelationSynset c) {
		if(c == null) {
			System.err.println("Synset.remove(RelationSynset) - null value!");
			return false;
		}
		if(!correlazioni.contains(c)) {
			return false;
		}
		correlazioni.remove(c);
		return true;
	}
	
	public String getLexicalForm() {
		
		if(lemmi.size() == 0) {
			return "";
		}
		return lemmi.get(0).getLexicalForm();	
	}
	
	public String getPartOfSpeech() {
		
		if(lemmi.size() == 0) {
			return "";
		}
		return lemmi.get(0).getPartOfSpeech();	
	}
	
	public String toString() {
		
		String str = "(empty synset)";
 
		if(lemmi.size() > 0) {
			str = "";
			for(int i = 0; i < lemmi.size(); i++) {
				if(lemmi.get(i) == null) {
					System.err.println("ERRORE!! lemma nullo! i:" + i + 
										" SynsetID:" + this.getID());
					str += "( - NULL!! - )";
				} else {
					str += lemmi.get(i).toString();
				}
				if(i < lemmi.size() - 1) {
					str += " , ";
				}
			}

			if(definizione != null && definizione.trim().length() > 0) {
				str += " - (" + definizione + ")";
			}
		}
		
		return str;
	}
	
	public boolean equals(Object obj) {

		if(obj == null) {
			return false;
		}
		if(obj instanceof Lemma) {
			//Verifica l'uguaglianza solo per il lemma primario
			Lemma l1 = this.getPrimario();
			Lemma l2 = (Lemma) obj;
			if(l1 == null) {
				return false;
			}
			return l1.equals(l2);
		}
		/*
		if(obj instanceof Synset) {
			//Si deve controllare tutta la struttura, ma per motivi
			//pratici (non serve mai controllare tutta la struttura!?)
			//anche qui verifica l'uguaglianza solo per il lemma primario
			Lemma l1 = this.getPrimario();
			Lemma l2 = ((Synset) obj).getPrimario();
			if(l1 == null) {
				//vero se anche l2 è null?
				return false;
			}
			return l1.equals(l2);
		}
		*/
		/*
		 * L'uguaglianza stretta di due concetti si basa sui dettagli di
		 * un Synset: id, definizione, ?ontoclasse?,... ed è indipendente
		 * dai lemmi o dalle relazioni contenute nel Synset.
		 * 
		 * update: SI PUO' LASCIARE SOLO IL CONTROLLO SULL'ID... !?
		 */
		if(obj instanceof Synset) {
			Synset c2 = (Synset) obj;
			String id2 = c2.getID();
			//String def2 = c2.getDefinizione();
			//String oc2 = c2.getOntoClass();
			if(this.getID().equals(id2) 
					// && this.getDefinizione().equals(def2) 
					// && this.getOntoClass().equals(oc2)
					) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Lascia tutto nella mani di equals() ! 
	 */
	public int hashCode() {
//		if(id == null || id.trim().length() < 1) {
//			System.err.println("Synset with no valid id !! : " + this.toString());
//			return 0;
//		} else {
//			return Integer.valueOf(id);
//		}
		return 1;
	}
	
	public int compareTo(Object obj) throws ClassCastException {
		
		if(!(obj instanceof Synset)) {
			throw new ClassCastException("Object is not a valid synset!");
		}
		String objForm = ((Synset) obj).getLexicalForm();
		return this.getLexicalForm().compareTo(objForm);
	}
	
	public boolean setPrimario(Lemma l) {
		
		if(l == null) {
			return false;
		}
		if(lemmi.contains(l)) {
			lemmi.remove(l);	
		}
		lemmi.add(0, l);
		return true;
	}
	
	public Lemma getPrimario() {
		if(lemmi.size() == 0) {
			return null;
		}
		return lemmi.get(0);
	}
	
	public int getLemmiCount() {
		if(lemmi == null) {
			return 0;
		}
		return lemmi.size();
	}
	
	public int getCorrelazioniCount() {
		if(correlazioni == null) {
			return 0;
		}
		return correlazioni.size();
	}
	
	public boolean isCandidate() {
		return candidate;
	}

	public void setCandidate(boolean candidate) {
		this.candidate = candidate;
	}

	public String getURI() {
		return URI;
	}

	public void setURI(String uri) {
		URI = uri;
	}
	
}
