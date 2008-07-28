package it.cnr.ittig.bacci.util;

public class Conf {

	public static String APPLICATION_DATA_DIR = "";
	
	//public static boolean EXTERNAL_MAPPING = true;
	public static boolean EXTERNAL_MAPPING = false;	
	public final static String CLASSIFICATION = "classification-070308.xls"; 
	public final static String MAPPING = "mappings-070308.xls"; 
	
	public static boolean WORDNET_DATA = true;
	
//	public static String DOMAIN_ONTO = 
//		"http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl";
//	public static String DOMAIN_ONTO_NS = 
//		"http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl#";

	public static String METALEVEL_ONTO =
		"http://turing.ittig.cnr.it/jwn/ontologies/owns.owl";
	public static String METALEVEL_PROP =
		"http://turing.ittig.cnr.it/jwn/ontologies/owns-full.owl";
	public static String SOURCE_SCHEMA = 
		"http://turing.ittig.cnr.it/jwn/ontologies/metasources.owl";
	public static String CONCEPT_SCHEMA = 
		"http://turing.ittig.cnr.it/jwn/ontologies/metaconcepts.owl";
	public static String METALEVEL_FULL =
		"http://turing.ittig.cnr.it/jwn/ontologies/language-properties-full.owl";
	
	public static String METALEVEL_ONTO_NS = Conf.METALEVEL_ONTO + "#";	
	public static String METALEVEL_PROP_NS = Conf.METALEVEL_PROP + "#";
	public static String SOURCESCHEMA_NS = Conf.SOURCE_SCHEMA + "#";
	public static String CONCEPTSCHEMA_NS = Conf.CONCEPT_SCHEMA + "#";

	public static String CONCEPTS = "concepts.owl";
	public static String IND = "individuals.owl";
	public static String INDW = "individuals-word.owl";
	public static String TYPES = "types.owl";
	public static String SOURCES = "sources.owl";
	
	//NEW
	public static String LINKS = "links.owl";
	public static String INTERCONCEPTS = "interconcepts.owl";
	public static String LEXICALIZATION = "lexicalizations.owl";
	
	public static String LOCAL_DOMAIN_ONTO = "consumer-law.owl";
	public static String LOCAL_DOMAIN_MERGE_ONTO = "consumer-law-merge.owl";
	public static String LOCAL_METALEVEL_ONTO = "owns.owl";
	public static String LOCAL_METALEVEL_PROP = "owns-full.owl";
	public static String LOCAL_SOURCE_SCHEMA = "metasources.owl";
	public static String LOCAL_METALEVEL_FULL = "language-properties-full.owl";
	
	public static String SYNSET_CLASS = METALEVEL_ONTO_NS + "Synset";
	public static String conceptClassName = "Concept";
	
	//New
	public static String CONCEPT_CLASS = CONCEPTSCHEMA_NS + "Concept";
	public static String LEXICALIZATION_PROP = CONCEPTSCHEMA_NS + "hasLexicalization";
	public static String SOURCE_PROP = SOURCESCHEMA_NS + "source";
	public static String SYNID_PROP = METALEVEL_ONTO_NS + "synsetId";
	public static String GLOSS_PROP = METALEVEL_ONTO_NS + "gloss";
	public static String WORDSENSE_PROP = METALEVEL_ONTO_NS + "containsWordSense";
	public static String WORD_PROP = METALEVEL_ONTO_NS + "word";
	public static String LEXFORM_PROP = METALEVEL_ONTO_NS + "lexicalForm";
	public static String PROTOFORM_PROP = METALEVEL_ONTO_NS + "protoForm";
	
	public static String NOUN_CLASS = METALEVEL_ONTO_NS + "NounSynset";
	public static String VERB_CLASS = METALEVEL_ONTO_NS + "VerbSynset";
	public static String ADVERB_CLASS = METALEVEL_ONTO_NS + "AdverbSynset";
	public static String ADJECTIVE_CLASS = METALEVEL_ONTO_NS + "AdjectiveSynset";

	public static String CNIPA_NS = "http://localhost/cnipa/";	
	public static String DALOS_NS = "http://localhost/dalos/";

	public static String RES_NAMESPACE = ""; //dalos? cnipa?
	public static String CNIPA_RES_NS = CNIPA_NS + "IT/";	
	public static String DALOS_RES_NS = DALOS_NS + "IT/";
	public static String CNIPA_ONTO = 
		"http://turing.ittig.cnr.it/jwn/ontologies/cnipa.owl";
	public static String CNIPA_ONTO_NS = CNIPA_ONTO + "#"; 
	public static String DALOS_ONTO = 
		"http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl";
	public static String DALOS_ONTO_NS = DALOS_ONTO + "#"; 
	
	//DATABASE
	//public static String dbUser = "juris";
	//public static String dbPass = "juris.";
	public static String dbUser = "";
	public static String dbPass = "";
	public static String dbName = "cnipa";
	public static String dbAddress = "172.16.0.12";
	public static String dbType = "MySQL";
	
	//TEST ONTOLOGY
	public static String TEST_ONTO = 
		"http://turing.ittig.cnr.it/jwn/ontologies/test.owl";
	public static String TEST_NS = TEST_ONTO + "#";


}
