package it.cnr.ittig.bacci.divide;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryEngine {

	private Dataset dataset;
	
	private OntModel model;
	
	public Model run(String query) {
		
		dataset = DatasetFactory.create(model);
		
		Query q = QueryFactory.create(query);
		Model resultModel = QueryExecutionFactory.
								create(q, dataset).execConstruct();
		
		return resultModel;
		
	}
	
	/*
	 * QUERIES;
	 * 

//LEXICAL & GENERICS

PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
CONSTRUCT {
  <synset-xyz> ?p ?o .
  ?s ?p1 <synset-xyz> .
  ?o ?p2 ?o2 .
  ?o2 ?p3 ?o4 .
}
WHERE { 
  <synset-xyz> ?p ?o .
  ?s ?p1 <synset-xyz> .
  ?o ?p2 ?o2 .
  ?o2 ?p3 ?o4 .
}

//SOURCES:

PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
CONSTRUCT {
  <synset-xyz> ?p ?o .
  ?s ?p1 <synset-xyz> .
}
WHERE { 
  <synset-xyz> ?p ?o .
  ?s ?p1 <synset-xyz> .
}



	 */
}
