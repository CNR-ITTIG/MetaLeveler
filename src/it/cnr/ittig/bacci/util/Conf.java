package it.cnr.ittig.bacci.util;

public class Conf {

	public static String DATA_DIRECTORY = "input";
	
	//public static boolean EXTERNAL_MAPPING = true;
	public static boolean EXTERNAL_MAPPING = false;
	public final static String CLASSIFICATION = "classification-15-07.xls"; 
	public final static String MAPPING = "mappings-15-07-mod.xls"; 

	
	public static String DOMAIN_ONTO = 
		"http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl";
	public static String METALEVEL_ONTO =
		"http://turing.ittig.cnr.it/jwn/ontologies/owns.owl";
	public static String METALEVEL_PROP =
		"http://turing.ittig.cnr.it/jwn/ontologies/owns-full.owl";
	public static String SOURCE_SCHEMA = 
		"http://turing.ittig.cnr.it/jwn/ontologies/metasources.owl";
	public static String METALEVEL_FULL =
		"http://turing.ittig.cnr.it/jwn/ontologies/language-properties-full.owl";
	
	public static String DOMAIN_ONTO_NS = "http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl#";
	public static String METALEVEL_ONTO_NS = Conf.METALEVEL_ONTO + "#";	
	public static String METALEVEL_PROP_NS = Conf.METALEVEL_PROP + "#";
	public static String SOURCESCHEMA_NS = Conf.SOURCE_SCHEMA + "#";

	public static String OWL_ROOT_CLASS = "http://www.w3.org/2002/07/owl#Thing";
	
	public static String CONCEPTS = "concepts.owl";
	public static String IND = "individuals.owl";
	public static String INDW = "individuals-word.owl";
	public static String TYPES = "types.owl";
	public static String SOURCES = "sources.owl";
	
	public static String LOCAL_DOMAIN_ONTO = "consumer-law.owl";
	public static String LOCAL_DOMAIN_MERGE_ONTO = "consumer-law-merge.owl";
	public static String LOCAL_METALEVEL_ONTO = "owns.owl";
	public static String LOCAL_METALEVEL_PROP = "owns-full.owl";
	public static String LOCAL_SOURCE_SCHEMA = "metasources.owl";
	public static String LOCAL_METALEVEL_FULL = "language-properties-full.owl";
	
	public static String DALOS_NS = "http://localhost/dalos/";

	public static String DALOS_CONCEPTS_NS = DALOS_NS + CONCEPTS + "#";

	public static String conceptClassName = DALOS_CONCEPTS_NS + "Concept";	
	
	public static String synsetClassName = METALEVEL_ONTO_NS + "Synset";
}
