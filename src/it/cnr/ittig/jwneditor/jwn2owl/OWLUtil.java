package it.cnr.ittig.jwneditor.jwn2owl;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.vocabulary.OWL;

import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn.Lemma;

public class OWLUtil {
	
	public static void addImport(OntModel om, String source, String dest) {

		Ontology ont = om.createOntology(source); 
		om.add(ont, OWL.imports, om.createResource(dest));
	}

	public static String getConceptClassName(Lemma l) {
		
		return getConceptClassNameURI(l.getTempConceptName(), l.getPartOfSpeech(), l.getSense());
		//return getConceptClassNameURI(l.getLexicalForm(), l.getPartOfSpeech(), l.getSense());
	}

	public static String getSynsetClassName(Lemma l) {
		
		return getSynsetClassNameURI(l.getLexicalForm(), l.getPartOfSpeech(), l.getSense());
	}

	public static String getSynsetName(Lemma l) {
		
		return getSynsetURI(l.getLexicalForm(), l.getPartOfSpeech(), l.getSense());
	}

	public static String getWordSenseName(Lemma l) {
		
		return getWordSenseURI(l.getLexicalForm(), l.getPartOfSpeech(), l.getSense());
	}

	public static String getWordName(Lemma l) {
		
		return getWordURI(l.getLexicalForm());
	}
	
	/*
	 * Returns a 12-characters string.
	 */
	public static String getSynsetID(Concetto c) {
		
		String id = c.getID();
		int len = id.length();
		if(len > 11) {
			System.err.println("len > 11 ! (" + len + ") - c:" + c);
		}
		//fill with zero(s)...
		for(int i = 0; i < 12 - len - 1; i++) {
			id = "0" + id;
		}
		return getSynsetIDStartingNumber(c.getPartOfSpeech()) + id;
	}
	
	/*
	 * From jwn2owl...
	 */
	
	public static String getSynsetClass(String pos) {
		
		if(pos.equalsIgnoreCase("N")) return "NounSynset";
		if(pos.equalsIgnoreCase("V")) return "VerbSynset";
		if(pos.equalsIgnoreCase("AG")) return "AdjectiveSynset";
		if(pos.equalsIgnoreCase("AV")) return "AdverbSynset";
		if(pos.equalsIgnoreCase("NP")) return "NounSynset";
		//if(pos.equalsIgnoreCase("S")) return "AdjectiveSatelliteSynset";		
		System.err.println("getSynsetClassName() -- unknown pos:" + pos);
		return "";
	}
	
	public static String getWordSenseClass(String pos) {
		
		if(pos.equalsIgnoreCase("N")) return "NounWordSense";
		if(pos.equalsIgnoreCase("V")) return "VerbWordSense";
		if(pos.equalsIgnoreCase("AG")) return "AdjectiveWordSense";
		if(pos.equalsIgnoreCase("AV")) return "AdverbWordSense";
		if(pos.equalsIgnoreCase("NP")) return "NounWordSense";
		//if(pos.equalsIgnoreCase("S")) return "";		
		System.err.println("getWordSenseClassName() -- unknown pos:" + pos);
		return "";
	}
	
	public static String getWordClass() {
		
		return "Word";
	}
	
	private static String getPosName(String pos) {
		
		if(pos.equalsIgnoreCase("N")) return "noun";
		if(pos.equalsIgnoreCase("V")) return "verb";
		if(pos.equalsIgnoreCase("AG")) return "adjective";
		if(pos.equalsIgnoreCase("AV")) return "adverb";
		if(pos.equalsIgnoreCase("NP")) return "noun";
		//if(pos.equalsIgnoreCase("S")) return "";		
		System.err.println("getPosName() -- unknown pos:" + pos);
		return "";
	}

	/*
	 * Returns the first number of ID
	 * (a "noun" synset id is something like "10000987").
	 */
	private static String getSynsetIDStartingNumber(String pos) {
		
		if(pos.equalsIgnoreCase("N")) return "1";
		if(pos.equalsIgnoreCase("V")) return "2";
		if(pos.equalsIgnoreCase("AG")) return "3";
		if(pos.equalsIgnoreCase("AV")) return "4";
		if(pos.equalsIgnoreCase("NP")) return "1";
		//if(pos.equalsIgnoreCase("S")) return "";		
		System.err.println("getSynsetID() -- unknown pos:" + pos);
		return "";
	}
	
	private static String getConceptClassNameURI(String name, String pos, String sense) {
		return "concept-" + getOWLName(name) + "-" + getPosName(pos) + "-" + sense;
	}
	
	private static String getSynsetClassNameURI(String name, String pos, String sense) {
		return getOWLName(name) + "-" + getPosName(pos) + "-" + sense;
	}
	
	private static String getSynsetURI(String name, String pos, String sense) {
		return "synset-" + getOWLName(name) + "-" + getPosName(pos) + "-" + sense;
	}	
	
	private static String getWordSenseURI(String name, String pos, String sense) {
		return "wordsense-" + getOWLName(name) + "-" + getPosName(pos) + "-" + sense;
	}	
	
	private static String getWordURI(String name) {
		return "word-" + getOWLName(name);
	}
	
	public static String getOWLName(String name) {
		
		//An OWL name cannot begin with a number! Check this?
		
		name = name.replace(' ','_');
		name = name.replace('!','-');
		name = name.replace('\'','-');
		name = name.replace(':','-');
		name = name.replace(',','_');
		
		name = name.toLowerCase();  // chars to lower case?

		//Why this shouldnt work?!
//		name = name.replaceAll(">","&gt;");
//		name = name.replaceAll("<","&lt;");
//		name = name.replaceAll("\"","&quot;");
		
		//Temp fix:
		name = name.replace('>','-');
		name = name.replace('<','-');
		name = name.replace('\"','-');
		
		//Not allowed character:
		name = name.replace('(','-');
		name = name.replace('[','-');
		name = name.replace('{','-');
		name = name.replace(')','-');
		name = name.replace(']','-');
		name = name.replace('}','-');
		name = name.replace('=','_');
		
		name = getOWLNameAccent(name);
		
		return name;		
	}
	
	public static String getOWLNameAccent(String name) {
		
		name = name.replace('à','a');
		name = name.replace('è','e');
		name = name.replace('é','e');
		name = name.replace('ì','i');
		name = name.replace('ò','o');
		name = name.replace('ù','u');

		return name;
	}

}
