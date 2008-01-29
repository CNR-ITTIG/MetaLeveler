package it.cnr.ittig.bacci.divide;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class QueryEngine {

	private Dataset dataset;
	
	private OntModel model;
	
	private void run(String query) {
		
		dataset = DatasetFactory.create(model);
		
		Query q = QueryFactory.create(query);
		ResultSet results = QueryExecutionFactory.
								create(q, dataset).execSelect();
		
		ResultSetFormatter.out(System.out, results, q);
		
	}
}
