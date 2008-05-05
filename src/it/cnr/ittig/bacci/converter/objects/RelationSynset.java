package it.cnr.ittig.bacci.converter.objects;

public class RelationSynset {

	private Synset destination;
	private Relation relation;
	
	private String id;
	
	public RelationSynset(Synset d, Relation r) {
		
		destination = d;
		relation = r;
		
		id = null;
	}
	
	public void setID(String newId) { id = newId; }
	public String getID() {	return id; }

	public Synset getDestination() {
		return destination;
	}
	
	public void setDestination(Synset c) {
		destination = c;
	}
	
	public Relation getRelation() {
		return relation;
	}
	
	public String toString() {
		return relation.getLexicalForm() + " --> " + destination;
	}
	
	public boolean equals(Object obj) {
		
		if(obj == null) {
			return false;
		}
		if(obj instanceof RelationSynset) {
			RelationSynset cor = (RelationSynset) obj;
			Relation rel = cor.getRelation();
			Synset dest = cor.getDestination();
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
