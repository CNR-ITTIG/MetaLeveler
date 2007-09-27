package it.cnr.ittig.jwneditor.jwn;

public class Fonte {

	private String fonte;
	private Concetto relatedSynset;
	
	private String id;
	
	public Fonte(String f) {
		
		this(f, null);
	}
	
	public Fonte(String f, Concetto c) {
		
		fonte = f;
		relatedSynset = c;
		
		id = null;
	}
	
	public void setID(String newId) { id = newId; }
	public String getID() {	return id; }

	public String getLexicalForm() {
		return fonte;
	}
	
	public Concetto getRelatedSynset() {
		return relatedSynset;
	}
	
	public void setRelatedSynset(Concetto c) {
		relatedSynset = c;
	}
	
	public String toString() {
		if(relatedSynset != null) {
			return fonte + " => " + relatedSynset.toString();
		}
		return fonte;
	}

	public boolean equals(Object obj) {
		
		if(obj == null) {
			return false;
		}
		if(obj instanceof Fonte) {
			Fonte f = (Fonte) obj;
			Concetto rel = f.getRelatedSynset();
			if(relatedSynset == null) {
				if(rel == null && fonte.equals(f.getLexicalForm())) {
					return true;
				} else {
					return false;
				}
			}
			if(fonte.equals(f.getLexicalForm()) && relatedSynset.equals(rel)) {
				return true;
			}
		}
		return false;
	}

	/*
	public int hashCode() {
		String id;
		if(relatedSynset == null || relatedSynset.getID() == null) {
			id = "0";
		} else {
			id = relatedSynset.getID();
		}
		return Integer.valueOf(id);
	}
	*/
}
