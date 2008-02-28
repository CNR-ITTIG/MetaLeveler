package it.cnr.ittig.bacci.classifier.resource;

public abstract class WebResource implements Comparable {
	
	private String URI;
	
	private String lexicalForm;

	public WebResource() {

		URI = "";
		lexicalForm = "";
	}

	public String getLexicalForm() {
		return lexicalForm;
	}

	public void setLexicalForm(String lexicalForm) {
		this.lexicalForm = lexicalForm;
	}

	public String getURI() {
		return URI;
	}

	public void setURI(String uri) {
		URI = uri;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((URI == null) ? 0 : URI.hashCode());
		result = PRIME * result + ((lexicalForm == null) ? 0 : lexicalForm.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final WebResource other = (WebResource) obj;
		if (URI == null) {
			if (other.URI != null)
				return false;
		} else if (!URI.equals(other.URI))
			return false;
		if (lexicalForm == null) {
			if (other.lexicalForm != null)
				return false;
		} else if (!lexicalForm.equals(other.lexicalForm))
			return false;
		return true;
	}
	
	public String toString() {

		if(lexicalForm.trim().length() < 1) {
			return "(empty) " + URI;
		}
		return lexicalForm;
	}
	
	public int compareTo(Object obj) throws ClassCastException {
		
		if(!(obj instanceof WebResource)) {
			throw new ClassCastException(
					"Object is not a valid web resource!");
		}
		
		String objForm = ((WebResource) obj).toString();
		return this.toString().compareToIgnoreCase(objForm);
	}
}
