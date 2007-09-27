package it.cnr.ittig.jwneditor.jwn;

public class Correlazione {

	private Concetto destination;
	private Relazione relation;
	
	private String id;
	
	public Correlazione(Concetto d, Relazione r) {
		
		destination = d;
		relation = r;
		
		id = null;
	}
	
	public void setID(String newId) { id = newId; }
	public String getID() {	return id; }

	public Concetto getDestination() {
		return destination;
	}
	
	public void setDestination(Concetto c) {
		destination = c;
	}
	
	public Relazione getRelation() {
		return relation;
	}
	
	public String toString() {
		return relation.getLexicalForm() + " --> " + destination;
	}
	
	public boolean equals(Object obj) {
		
		if(obj == null) {
			return false;
		}
		if(obj instanceof Correlazione) {
			Correlazione cor = (Correlazione) obj;
			Relazione rel = cor.getRelation();
			Concetto dest = cor.getDestination();
			if(relation.equals(rel) && destination.equals(dest)) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		String id;
		if(destination == null || destination.getID() == null) {
			id = "0";
		} else {
			id = destination.getID();
		}
		return Integer.valueOf(id);
	}
}
