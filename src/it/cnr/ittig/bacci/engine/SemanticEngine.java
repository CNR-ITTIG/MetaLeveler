package it.cnr.ittig.bacci.engine;

import it.cnr.ittig.bacci.classifier.resource.OntologicalClass;
import it.cnr.ittig.bacci.util.Conf;
import it.cnr.ittig.bacci.util.KbModelFactory;
import it.cnr.ittig.bacci.util.Util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class SemanticEngine {
	
	private Map<String,OntologicalClass> uriToOntoClass;
	
	public SemanticEngine() {
		
		uriToOntoClass = new HashMap<String, OntologicalClass>();
		
		process();
		OntModel output = fillModel();
		File outFile = new File("semanticProperties.owl");
		Util.serialize(output, outFile.getAbsolutePath());
	}

	private void process() {
		
		System.out.println("@@ SemanticEngine @@ Loading models...");
		OntModel onto = KbModelFactory.getModel("dalos.ontology", "micro");
		System.out.println("()() Initializing semantic properties ()()");
	
		for(Iterator i = onto.listClasses(); i.hasNext(); ) {
			OntClass soc = (OntClass) i.next();
			if(soc.isAnon()) {
				continue;
			}
			String suri = soc.getNameSpace() + soc.getLocalName();
			if(!soc.isAnon() && !soc.getNameSpace().equalsIgnoreCase(Conf.DALOS_ONTO_NS)) {
				continue;
			}
			OntologicalClass stoc = uriToOntoClass.get(suri);
			if(stoc == null) {
				stoc = new OntologicalClass();
				stoc.setURI(suri);
//				if(soc.isAnon()) {
//					stoc.setLexicalForm("(anonymous class)");
//				} else {
					stoc.setLexicalForm(soc.getLocalName());					
//				}
				uriToOntoClass.put(suri, stoc);
			}
			
			System.out.println("> stoc: " + stoc);
			
			for(Iterator k = soc.listDeclaredProperties(); k.hasNext(); ) {
				OntProperty op = (OntProperty) k.next();
				String relUri = op.getNameSpace() + op.getLocalName();
				System.out.println(">>> relName: " + relUri);
				for(Iterator z = op.listRange(); z.hasNext(); ) {
					OntClass ooc = (OntClass) z.next();
					System.out.println(">>>> ooc uri: " + ooc.getNameSpace() + ooc.getLocalName());
					if(!ooc.isAnon()) {
						OntologicalClass otoc = uriToOntoClass.get(ooc.getNameSpace() + ooc.getLocalName());
						if(otoc == null) {
							//Not a domain class?
							continue;
						}
						System.out.println(">>>>> otoc: " + otoc);
						stoc.addSemanticProperty(relUri, otoc);
					} else {
						//Anonymous range.. collection? restriction?
						if(ooc.isRestriction()) {
							System.out.println(">>> RESTRICTION found for stoc:" + stoc + " rel:" + relUri);
						} else if(ooc.isUnionClass()) {
							System.out.println(">>> UNION FOUND!");
							UnionClass uc = ooc.asUnionClass();
							for(Iterator y = uc.listOperands(); y.hasNext(); ) {
								OntClass operand = (OntClass) y.next();
								//Union: add every operand class
								OntologicalClass otoc = uriToOntoClass.get(operand.getNameSpace() + operand.getLocalName());
								if(otoc == null) {
									//Not a domain class?
									continue;
								}
								System.out.println(">>>>> otoc from union: " + otoc);
								stoc.addSemanticProperty(relUri, otoc);						
							}
						} else if(ooc.isIntersectionClass()) {
							System.out.println(">>> INTERSECTION FOUND!");
						} else if(ooc.isComplementClass()) {
							System.out.println(">>> COMPLEMENT FOUND!");
						} else if(ooc.isEnumeratedClass()) {
							System.out.println(">>> ENUMERATED FOUND!");
						} else {
							System.err.println("Unknown Range for stoc:" + stoc + " rel:" + relUri);
						}
					}
				}
			}
		}
	}
	
	private OntModel fillModel() {
		
		OntModel output = KbModelFactory.getModel();
		
		for(Iterator<String> i = uriToOntoClass.keySet().iterator(); i.hasNext(); ) {
			String uri = i.next();
			OntologicalClass oc = uriToOntoClass.get(uri);
			Resource sres = output.createResource(uri); 
			Map<String,Collection<OntologicalClass>> semanticProperties = oc.getSemanticProperties();
			for(Iterator<String> k = semanticProperties.keySet().iterator(); k.hasNext(); ) {
				String relUri = k.next();
				Property pres = output.createProperty(relUri);
				Collection<OntologicalClass> destinations = semanticProperties.get(relUri);
				for(Iterator<OntologicalClass> z = destinations.iterator(); z.hasNext(); ) {
					OntologicalClass dest = z.next();
					Resource dres = output.createResource(dest.getURI());
					output.add(sres, pres, dres);
				}
			}
		}
		
		return output;
	}
}
