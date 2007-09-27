package it.cnr.ittig.jwneditor.jwn2owl.service;

import java.util.Iterator;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.reasoner.ValidityReport;

import it.cnr.ittig.jwneditor.jwn2owl.container.OntologyContainer;

public class ValidateService {

	public void process(OntologyContainer container) {
		
		OntModel model = container.getOntModel(true);

	    // print validation report
	    ValidityReport report = model.validate();
	    printIterator( report.getReports(), "Validation Results" );
	}
	
    private static void printIterator(Iterator i, String header) {
        System.out.println(header);
        for(int c = 0; c < header.length(); c++)
            System.out.print("=");
        System.out.println();
        
        if(i.hasNext()) {
	        while (i.hasNext()) 
	            System.out.println( i.next() );
        }       
        else
            System.out.println("<EMPTY>");
        
        System.out.println();
    }

}
