package it.cnr.ittig.bacci.semtester;

import java.util.Iterator;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.tidy.Checker;
import com.hp.hpl.jena.ontology.tidy.SyntaxProblem;

public class SyntaxChecker {

	public void process(OntModel model) {
		
		boolean expectingLite = false;

		Checker ck = new Checker(expectingLite);
		
		ck.add(model);
		
		String subLang = ck.getSubLanguage();
		
		if(!(subLang.equals("Lite"))) {
			System.err.println("CheckerService - subLanguage:" + subLang);
			for(Iterator i = ck.getProblems(); i.hasNext();) {
				SyntaxProblem sp = (SyntaxProblem) i.next();
				System.err.println(sp.longDescription());
			}
		}
	}
}
