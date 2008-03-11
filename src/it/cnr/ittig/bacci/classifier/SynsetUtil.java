package it.cnr.ittig.bacci.classifier;

import it.cnr.ittig.bacci.classifier.resource.BasicResource;
import it.cnr.ittig.bacci.classifier.resource.Synset;
import it.cnr.ittig.bacci.util.Conf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class SynsetUtil {
	
	private static OntModel om = null;
	
	private static OntProperty containsProperty = null;
	private static OntProperty wordProperty = null;
	private static OntProperty lexicalProperty = null;
	private static OntProperty protoProperty = null;

	public static void setModel(OntModel model) {
		
		om = model;
		initProperties();
	}
	
	private static void initProperties() {
		
		if(om == null) {
			return;
		}
		containsProperty = om.getOntProperty(
				Conf.METALEVEL_ONTO_NS + "containsWordSense");
		if(containsProperty == null) {
			System.err.println("Contains Prop is null!!");
		}
		wordProperty = om.getOntProperty(
				Conf.METALEVEL_ONTO_NS + "word");
		if(wordProperty == null) {
			System.err.println("Word Prop is null!!");
		}
		lexicalProperty = om.getOntProperty(
				Conf.METALEVEL_ONTO_NS + "lexicalForm");
		if(lexicalProperty == null) {
			System.err.println("Lexical Prop is null!!");
		}
		protoProperty = om.getOntProperty(
				Conf.METALEVEL_ONTO_NS + "protoForm");		
		if(protoProperty == null) {
			System.err.println("Proto Prop is null!!");
		}
	}
	
	public static boolean addVariants(OntResource ores, BasicResource br) {

		Synset syn = (Synset) br;
		for(ExtendedIterator k = ores.listPropertyValues(containsProperty); 
				k.hasNext();) {
			OntResource ws = (OntResource) k.next();
			OntResource w = (OntResource) ws.getPropertyValue(wordProperty);
			RDFNode protoNode = w.getPropertyValue(protoProperty);
			if(protoNode != null) {
				syn.setLexicalForm(((Literal) protoNode).getString());
			} else {
				System.err.println(">> synset without proto form! ores:" + ores);
			}
			int lcount = 0;
			for(ExtendedIterator l = w.listPropertyValues(lexicalProperty);
					l.hasNext(); ) {
				RDFNode lexNode = (RDFNode) l.next();
				String lexForm = ((Literal) lexNode).getString();
				syn.addVariant(lexForm);
				lcount++;
			}
			if(lcount == 0) {
				System.err.println("Synset with 0 lexical form!!");
				return false;
			}
		}
		return true;
	}

	public static boolean isCandidateResource(BasicResource br) {
		
		Synset syn = (Synset) br;
		return syn.isCandidate();
	}
}
