package it.cnr.ittig.bacci.converter;

public class Run {

	public static void main(String[] args) {
		
		//Converter conv = new Converter();
		//conv.convert();
		
		//conv.addInterlinguistic();
		
//		LexiconCleaner lc = new LexiconCleaner();
//		lc.clean();
		
//		LexiconRefiner lr = new LexiconRefiner();
//		lr.refine();
		
		LexiconRefinerForeign lr = new LexiconRefinerForeign();
		lr.refine();
		
	}
}
