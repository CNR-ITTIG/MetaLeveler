package it.cnr.ittig.bacci.converter.objects;

import it.cnr.ittig.bacci.classifier.resource.WebResource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

//This class defines WORDSENSE objects...

public class Lemma extends WebResource {

	private String partOfSpeech;
	private String sense;
	
	private static String DEFAULT_POS = "N";
	private static String DEFAULT_SENSE = "1";

	//Word Collection
	private Collection<Word> words;
	
	//Synset that contains this object
	private Synset inSynset;

	public Lemma() {
				
		words = new HashSet<Word>();
		
		partOfSpeech = DEFAULT_POS;
		sense = DEFAULT_SENSE;

		inSynset = null;
	}
	
	public String toString() {
		if(partOfSpeech != null) {
			return lexicalForm + "  [" + partOfSpeech + ", " + sense + "]";
		} else {
			return lexicalForm + "  [" + sense + "]";
		}
	}

	public Synset getInSynset() {
		return inSynset;
	}

	public void setInSynset(Synset inSynset) {
		this.inSynset = inSynset;
	}

	public String getPartOfSpeech() {
		return partOfSpeech;
	}

	public void setPartOfSpeech(String partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}

	public String getSense() {
		return sense;
	}

	public void setSense(String sense) {
		this.sense = sense;
	}

	public Collection<Word> getWords() {
		return Collections.unmodifiableCollection(words);
	}

	public boolean addWord(Word word) {
		return words.add(word);
	}

	public boolean removeWord(Word word) {
		return words.remove(word);
	}

//	private String checkPipe(String l) {
//		
//		String form = l;
//		String[] forms = l.split("[|]");
//		//System.out.println("checkPipe() l:" + l + " fsize:" + forms.length);
//		if(forms.length > 1) {
//			for(int i = 0; i < forms.length; i++) {
//				String item = forms[i];
//				variants.add(item);
//				if(i == 0) {
//					form = item;
//				}				
//			}			
//		} else {
//			variants.add(form);
//		}
//		
//		return form;
//	}

}
