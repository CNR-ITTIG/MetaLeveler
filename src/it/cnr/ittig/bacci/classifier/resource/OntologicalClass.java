package it.cnr.ittig.bacci.classifier.resource;

import java.util.Collection;
import java.util.TreeSet;

public class OntologicalClass extends WebResource {
	
	Collection<BasicResource> resources;

	public OntologicalClass() {
		super();

		resources = new TreeSet<BasicResource>();
	}

	public Collection<BasicResource> getResources() {
		return resources;
	}
	
	public boolean addResource(BasicResource br) {
		
		return resources.add(br);
	}
	
	public boolean removeResource(BasicResource br) {
		
		return resources.remove(br);
	}

}
