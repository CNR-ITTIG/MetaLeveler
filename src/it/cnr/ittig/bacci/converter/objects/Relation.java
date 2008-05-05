package it.cnr.ittig.bacci.converter.objects;

public class Relation {
	
	private String lexicalForm;
	private String type;
	
	private String id;
	
	//Eventuale Relation reciproca (es.: iponimo <-> iperonimo).
	private String IDreciproca;
	private Relation reciproca;
	
	private static String DEFAULT_TYPE = "WNET";
	
	public Relation() {
		this(null);
	}
	
	public Relation(String name) {
		
		this(name, DEFAULT_TYPE);
	}
	
	public Relation(String name, String t) {
		
		lexicalForm = name;
		type = t;
		
		id = null;
		IDreciproca = null;
		reciproca = null;
	}
	
	public void setLexicalForm(String lex) { lexicalForm = lex; }
	public String getLexicalForm() { return lexicalForm; }

	public void setID(String newId) { id = newId; }
	public String getID() {	return id; }

	public void setType(String t) { type = t; }
	public String getType() { return type; }

	public void setInversaID(String id) { IDreciproca = id; }
	public String getInversaID() { return IDreciproca; }

	public void setInversa(Relation r) { reciproca = r; }
	public Relation getInversa() { return reciproca; }

	public boolean equals(Object obj) {
		
		if(obj == null) {
			return false;
		}
		if(obj instanceof Relation) {
			Relation r = (Relation) obj;
			if(this.getLexicalForm() == null || r.getLexicalForm() == null) {
				return false;
			}
			if(this.getLexicalForm().equalsIgnoreCase(r.getLexicalForm()) &&
					this.getType().equalsIgnoreCase(r.getType()) ) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Lascia tutto nella mani di equals() ! 
	 */
	public int hashCode() {
		return 1;
	}
	
	public String toString() {
		String ret = "";
		if(this.getLexicalForm() == null) {
			ret = "(undefined)";
		} else {
			//return this.getLexicalForm() + " [" + this.getType() + "]";
			ret = this.getLexicalForm();
			if(reciproca != null) {
				ret += " <--> " + reciproca.getLexicalForm();
			}
		}
		
		return ret;
	}

}
