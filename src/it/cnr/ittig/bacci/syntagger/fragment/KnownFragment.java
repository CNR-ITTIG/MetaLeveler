package it.cnr.ittig.bacci.syntagger.fragment;

import it.cnr.ittig.bacci.converter.objects.Synset;

public class KnownFragment extends Fragment {
	
	private Synset syn;
	
	public KnownFragment(String text) {
		
		super(text);
		
		syn = null;
	}

	public Synset getSyn() {
		return syn;
	}

	public void setSyn(Synset syn) {
		this.syn = syn;
	}

	public String toString() {
		
		return "%" + this.getText() + "% linkedTo syn: " + syn;
	}

}
