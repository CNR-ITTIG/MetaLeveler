package it.cnr.ittig.bacci.classifier.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

public class Synset extends BasicResource {
	
	private Collection<String> variants;
	
	public Synset() {
		super();

		variants = new TreeSet<String>(); //sorted!
	}

	public Collection<String> getVariants() {
		
		return Collections.unmodifiableCollection(variants);
	}	
	
	public boolean addVariant(String variant) {
		
		return variants.add(variant);
	}
	
	public boolean removeVariant(String variant) {
		
		return variants.remove(variant);
	}

	public String toString() {

		for(Iterator<String> i = variants.iterator(); i.hasNext();) {
			return i.next();
		}
		
		System.err.println("---- Surface form not found! size:" 
				+ variants.size() + "! using proto form...");
		return super.toString();
	}
}
