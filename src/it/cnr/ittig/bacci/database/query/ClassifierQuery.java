package it.cnr.ittig.bacci.database.query;

public class ClassifierQuery implements QueryResolver {
	
	private static String t1 = "tblGlossario";
	private static String t2 = "tblRelations";
	private static String t3 = "tblDocumenti";
	private static String t4 = "tblFrequenze";
	

	public String getQueryString(String type, String[] data) {
		
		if(type.equals("classifier.glossario.count")) {
			
			return "select count(*) from " + t1;
		}

		if(type.equals("classifier.glossario")) {
			
			return "select * from " + t1;
		}

		if(type.equals("classifier.relazioni")) {
			
			return "select * from " + t2;
		}

		if(type.equals("classifier.documenti")) {
			
			return "select * from " + t3;
		}

		if(type.equals("classifier.frequenze")) {
			
			return "select * from " + t4;
		}


		return "";
	}
}
