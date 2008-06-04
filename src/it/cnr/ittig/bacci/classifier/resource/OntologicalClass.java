package it.cnr.ittig.bacci.classifier.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class OntologicalClass extends WebResource {
	
	Collection<BasicResource> resources;
	
	Map<String,Collection<OntologicalClass>> semanticProperties;

	public OntologicalClass() {
		super();

		resources = new TreeSet<BasicResource>();
		semanticProperties = new HashMap<String, Collection<OntologicalClass>>();
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

	public void addSemanticProperty(String rel, OntologicalClass toc) {
		
		Collection<OntologicalClass> values = semanticProperties.get(rel);
		if(values == null) {
			values = new TreeSet<OntologicalClass>();
			semanticProperties.put(rel, values);
		}
		values.add(toc);
	}
	
	public Map<String,Collection<OntologicalClass>> getSemanticProperties() {
		return semanticProperties;
	}

}
