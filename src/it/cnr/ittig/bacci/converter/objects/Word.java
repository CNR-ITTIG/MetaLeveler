package it.cnr.ittig.bacci.converter.objects;

import it.cnr.ittig.bacci.classifier.resource.WebResource;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

public class Word extends WebResource {
	
	private Collection<String> lexicalForms;
	
	private String protoForm;
	
	public Word() {
		
		lexicalForms = new TreeSet<String>();
		protoForm = "";
	}

	public Collection<String> getLexicalForms() {
		return Collections.unmodifiableCollection(lexicalForms);
	}

	public boolean addLexicalForm(String str) {
		return lexicalForms.add(str);
	}

	public boolean removeLexicalForm(String str) {
		return lexicalForms.remove(str);
	}

	public String getProtoForm() {
		return protoForm;
	}

	public void setProtoForm(String protoForm) {
		this.protoForm = protoForm;
	}

}
