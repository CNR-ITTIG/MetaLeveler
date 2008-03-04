package it.cnr.ittig.bacci.database.query;

public class ClassifierQuery implements QueryResolver {
	
	private static String table1 = "";
	private static String table2 = "";
	private static String table3 = "";
	private static String table4 = "";

	public String getQueryString(String type, String[] data) {
		
		if(type.equals("classifier.relations.count")) {
			
			return "select count(*) from tblRelations";
		}

		if(type.equals("classifier.lemmi.count")) {
			
			return "select count(*) from tblLemmi";
		}

		if(type.equals("classifier.varianti.count")) {
			
			return "select count(*) from tblVarianti";
		}

		if(type.equals("classifier.related.count")) {
			
			return "select count(*) from tblRelatedTerms";
		}

		if(type.equals("classifier.lemmi")) {
			
			//
			if(data != null && data[0] != null) {
				System.err.println("getQueryString(), classifier.lemmi - WRONG DATA LENGTH ! (" +
									data.length + ")");
				return "";
			}

			return "select IdLemma, Lemma from tblLemmi";
		}

		if(type.equals("classifier.relations")) {
			
			//
			if(data != null && data[0] != null) {
				System.err.println("getQueryString(), classifier.relations - WRONG DATA LENGTH ! (" +
									data.length + ")");
				return "";
			}

			return "select IdRelation, IdLemmaX, Relation, IdLemmaY from tblRelations";
		}

		if(type.equals("classifier.varianti")) {
			
			//
			if(data != null && data[0] != null) {
				System.err.println("getQueryString(), classifier.varianti - WRONG DATA LENGTH ! (" +
									data.length + ")");
				return "";
			}

			return "select IdVariante, IdLemma, Variante from tblVarianti";
		}

		if(type.equals("classifier.related")) {
			
			//
			if(data != null && data[0] != null) {
				System.err.println("getQueryString(), classifier.related - WRONG DATA LENGTH ! (" +
									data.length + ")");
				return "";
			}

			return "select IdRelatedTerm, IdLemmaVariante, RelatedTerm, Paragraph, Tipo from tblRelatedTerms";
		}

		return "";
	}
}
