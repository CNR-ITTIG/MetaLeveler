package it.cnr.ittig.bacci.semtester;

import java.util.Iterator;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.reasoner.ValidityReport;

public class Validator {

	
	public void process(OntModel model) {
		
	    // print validation report
	    ValidityReport report = model.validate();
	    if(report == null) {
	    	System.err.println("Validator: report is null!");
	    }
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
