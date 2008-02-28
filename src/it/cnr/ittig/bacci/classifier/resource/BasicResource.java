package it.cnr.ittig.bacci.classifier.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;


public class BasicResource extends WebResource {

	private ConceptClass concept;
	
	//TODO Support multiple senses?!
	private Collection<String> variants;

	public BasicResource() {
		super();

		concept = null;
		variants = new Vector<String>();
	}

	public ConceptClass getConcept() {
		
		return concept;
	}

	public void setConcept(ConceptClass cc) {
		
		concept = cc;
	}
	
	public Collection<String> getVariants() {
		
		return Collections.unmodifiableCollection(variants);
	}	
	
	public boolean addVariant(String variant) {
		
		if(variants.size() == 0) {
			setLexicalForm(variant);
		}
		return variants.add(variant);
	}
	
	public boolean removeVariant(String variant) {
		
		return variants.remove(variant);
	}
	
//	private String getPrimario() {
//		
//		for(Iterator<String> i = variants.iterator();
//				i.hasNext(); ) {
//			return i.next();
//		}
//		
//		return null;
//	}
//	
//	
//	public String toString() {
//		
//		if(variants.size() > 0) {
//			return getPrimario();
//		}
//		
//		return super.toString();
//	}
//	
}
