package it.cnr.ittig.bacci.syntagger;

import it.cnr.ittig.bacci.semtester.SyntaxChecker;
import it.cnr.ittig.bacci.semtester.Validator;
import it.cnr.ittig.bacci.syntagger.fragment.Fragment;
import it.cnr.ittig.bacci.syntagger.fragment.KnownFragment;
import it.cnr.ittig.bacci.syntagger.fragment.Text;
import it.cnr.ittig.bacci.util.Conf;
import it.cnr.ittig.bacci.util.KbModelFactory;
import it.cnr.ittig.bacci.util.Util;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class SynTagger {

	private Text text;
	
	private Collection<String> terms;
	
	private OntModel model;
	private OntClass documentClass;
	private OntClass fragmentClass;
	private OntClass leggeClass;
	private OntClass leggeRegionaleClass;
	private OntClass leggeTipoDocClass;
	private Individual documentIndividual;
	private OntProperty hasFragmentProperty;
	
	public SynTagger() {
		
		 text = new Text(
			"Questo testo non è marcato; deve essere marcato come una legge.");
		 
		 terms = new HashSet<String>();
		 terms.add("testo");
		 terms.add("marcato");
		 terms.add("legge");		 
	}
	
	public void run() {

		 model = KbModelFactory.getModel("out.test.ontology", "pellet");
		 if(model == null) {
			 System.err.println("Unable to load test ontology!");
			 return;
		 }
		 System.out.println("Test Ontology loaded.");

		hasFragmentProperty = model.getOntProperty(Conf.TEST_NS + "hasFragment");
		documentClass = model.getOntClass(Conf.TEST_NS + "Document");
		fragmentClass = model.getOntClass(Conf.TEST_NS + "Fragment");
		leggeClass = model.getOntClass(Conf.TEST_NS + "Legge");
		leggeRegionaleClass = model.getOntClass(Conf.TEST_NS + "LeggeRegionale");
		leggeTipoDocClass = model.getOntClass(Conf.TEST_NS + "LeggeTipoDoc");
		
		//Set document individual
		documentIndividual = documentClass.createIndividual(Conf.TEST_NS + "document_1");
		
		model.add(documentIndividual, RDF.type, leggeClass);
		
		
		printReport();
		
		for(Iterator<String> i = terms.iterator(); i.hasNext(); ) {
			String term = i.next();
			checkTerm(term);
		}
		
		setGenericFragments();
		
		text.printFragments();
		
		Validator validator = new Validator();
		validator.process(model);
		
//		SyntaxChecker checker = new SyntaxChecker();
//		checker.process(model);
		
//		model.rebind();
//		
//		printReport();
		
		File output = new File("output.owl");
		Util.serialize(model, output.getAbsolutePath());
	}
	
	private void checkTerm(String term) {
		
		System.out.println("Checking term \"" + term + "\"...");
		
		Pattern pattern = Pattern.compile(term);
		Matcher matcher = pattern.matcher(text.getBuffer());

		while(matcher.find()) {
			System.out.println(">>match>> text:" + matcher.group() +
					" start:" + matcher.start() + " end:" + matcher.end());
			
			KnownFragment frag = new KnownFragment(matcher.group());
			frag.setStart(matcher.start());
			frag.setEnd(matcher.end() - 1);			
			text.addFragment(frag);
			
			Individual fragInd = null;
			if(term.equalsIgnoreCase("legge")) {
//				fragInd = leggeTipoDocClass.createIndividual(
//						Conf.TEST_NS + "fragment_" + matcher.start());
				fragInd = fragmentClass.createIndividual(
						Conf.TEST_NS + "fragment_" + matcher.start());
				//model.add(fragInd, RDF.type, leggeTipoDocClass);
			} else {
				fragInd = fragmentClass.createIndividual(
						Conf.TEST_NS + "fragment_" + matcher.start());				
			}
			model.add(documentIndividual, hasFragmentProperty, fragInd);
			
		}
	}
	
	private void setGenericFragments() {
		/*
		 * Fill actual fragment with generic text fragments.
		 */
		
		String buffer = text.getBuffer();
		
		if(text.getFragments().size() == 0) {
			System.out.println("No fragments in buffer at the moment. Adding everything as generic...");
			if(buffer.length() < 1 ) {
				System.err.println("Text buffer is empty!");
				return;
			}
			Fragment frag = new Fragment(buffer);
			frag.setStart(0);
			frag.setEnd(buffer.length() - 1);
			return;
		}
		
		int index = 0;
		while ( index < buffer.length()) {
			
			Fragment nextFrag = text.getNextFragment(index);
			if(nextFrag == null) {
				//Add remaing text as generic
				if(index < buffer.length()) {
					Fragment frag = new Fragment(buffer.substring(index));
					frag.setStart(index);
					frag.setEnd(buffer.length() - 1);
					text.addFragment(frag);
				}
				break;
			}
			
			int nextStart = nextFrag.getStart();
			int nextEnd = nextFrag.getEnd();
			
			if(index < nextStart) {
				//Create generic fragment *before* 
				System.out.println("BeFoRe - index:" + index + " nS:" + nextStart + " nE:" + nextEnd);
				Fragment frag = new Fragment(buffer.substring(index, nextStart));
				frag.setStart(index);
				frag.setEnd(nextStart - 1);
				text.addFragment(frag);
			}
			
			index = nextEnd + 1;
		}
	}
	
	private void printReport() {
		
		System.out.println("Analyzing individual " + documentIndividual.getLocalName() + " types...");
		for(ExtendedIterator ei = documentIndividual.listRDFTypes(false); ei.hasNext(); ) {
			Resource res = (Resource) ei.next();			
			if( ! res.isAnon() ) {
				System.out.println(">>> " + res.getLocalName());
			}
		}
	}
}
