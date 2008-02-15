package it.cnr.ittig.bacci.divide;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryEngine {

	public static Model run(Model model, String query) {
		
		Dataset dataset = DatasetFactory.create(model);
		
		//System.out.println("Exec Sparql: " + query);
		
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

//NUOVA:
CONSTRUCT {
  <synset-xyz> ?p ?o .
  ?o1 ?p2 ?o2 .
  ?o4 ?p5 ?o5 .
}
WHERE { 
  {
  <synset-xyz> ?p ?o .
  } UNION {
  <synset-xyz> ?p1 ?o1 .
  ?o1 rdf:type owns:WordSense .
  ?o1 ?p2 ?o2 .
  } UNION {
  <synset-xyz> ?p3 ?o3 .
  ?o3 ?p4 ?o4 .
  ?o4 rdf:type owns:Word .
  ?o4 ?p5 ?o5 .
  }
}


//SOURCES:

PREFIX src

CONSTRUCT {
  <synset-xyz> ?p ?o .
  ?o1 ?p2 ?o2 .
  ?o4 ?p5 ?o5 .
}
WHERE {
  {
  <synset-xyz> ?p ?o .
  ?o rdf:type src:Source
  } UNION {
  <synset-xyz> ?p1 ?o1 .
  ?o1 rdf:type owns:WordSense .
  ?o1 ?p2 ?o2 .
  } UNION {
  <synset-xyz> ?p3 ?o3 .
  ?o3 ?p4 ?o4 .
  ?o4 rdf:type owns:Word .
  ?o4 ?p5 ?o5 .
  }
}

//////////////////////
PREFIX src
CONSTRUCT {
  synset src:source ?s .
  ?s src:involvesPartition ?p .
  ?s src:content ?cont .
  ?p src:partitionCode ?pcode .
  ?p src:belongsTo ?doc .
  ?doc src:documentCode ?dcode .
  ?doc src:link ?link .
} WHERE {
  synset src:source ?s .
  ?s src:involvesPartition ?p .
  ?s src:content ?cont .
  ?p src:partitionCode ?pcode .
  ?p src:belongsTo ?doc .
  ?doc src:documentCode ?dcode .
  ?doc src:link ?link .
}
	 */
}
