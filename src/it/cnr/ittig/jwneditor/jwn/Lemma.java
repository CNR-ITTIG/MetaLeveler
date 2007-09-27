package it.cnr.ittig.jwneditor.jwn;

public class Lemma {

	private String lexicalForm;
	private String partOfSpeech;
	private String sense;
	
	private String ordine;
	
	private String id;
	
	//Prendere questi valori da un file Conf (EditorConf.java)
	private static String DEFAULT_POS = "N";
	private static String DEFAULT_SENSE = "1";
	
	private Concetto synset;
	
	public Lemma(String l) {
		this(l, DEFAULT_POS);
	}
	
	public Lemma(String l, String pos) {
		this(l, pos, DEFAULT_SENSE);
	}
	
	public Lemma(String l, String pos, String s) {
		
		lexicalForm = l;
		partOfSpeech = pos;		//puo' essere null ! (visualizzazione concetti in elenco concetti)
		sense = s;
		
		ordine = "";
		id = null;
		
		synset = null;
	}
	
	public void setID(String newId) { id = newId; }
	public String getID() {	return id; }

	public String getLexicalForm() { return lexicalForm; }
	public void setLexicalForm(String lex) { lexicalForm = lex; }
	
	public String getPartOfSpeech() { return partOfSpeech; }
	public void setPartOfSpeech(String pos) { partOfSpeech = pos; }
	
	public String getSense() { return sense; }
	public void setSense(String s) { sense = s; }
	
	public String getOrdine() { return ordine; }
	public void setOrdine(String o) { ordine = o; }

	public Concetto getSynset() { return synset; }
	public void setSynset(Concetto c) { synset = c; }

	public String toString() {
		if(partOfSpeech != null) {
			return lexicalForm + "  [" + partOfSpeech + ", " + sense + "]";
		} else {
			return lexicalForm + "  [" + sense + "]";
		}
	}

	public boolean equals(Object obj) {
		
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof Lemma)) {
			return false;
		}
		Lemma l = (Lemma) obj;
		
		if(this.getLexicalForm().equalsIgnoreCase(l.getLexicalForm()) && 
				this.getPartOfSpeech().equalsIgnoreCase(l.getPartOfSpeech()) &&
				this.getSense().equalsIgnoreCase(l.getSense()) ) {
			return true;
		}
		
		return false;
	}
}
