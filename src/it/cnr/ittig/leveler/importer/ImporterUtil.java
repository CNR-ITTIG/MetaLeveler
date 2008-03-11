package it.cnr.ittig.leveler.importer;

import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn.Correlazione;
import it.cnr.ittig.jwneditor.jwn.Relazione;

public abstract class ImporterUtil {
	
	protected boolean addSingleRelation(
			Concetto c1, Concetto c2, String relName) {

		Relazione ipo = new Relazione(EditorConf.iponimia);
		Relazione iper = new Relazione(EditorConf.iperonimia);
		Relazione fuzzy = new Relazione(EditorConf.related);
		Relazione thisRel = null;
		Relazione invRel = null;

		if(relName.equalsIgnoreCase("has_hyponym")
				|| relName.equalsIgnoreCase("hyperonym")
				) {
			thisRel = ipo;
			invRel = iper;
		} else if(relName.equalsIgnoreCase("has_hyperonym")
				|| relName.equalsIgnoreCase("hyperonym")
				) {
			thisRel = iper;
			invRel = ipo;
		} else if(relName.equalsIgnoreCase("fuzzynym")
				|| relName.equalsIgnoreCase("related")
				) {
			thisRel = fuzzy;
			invRel = fuzzy;
		} else {			
			return false;
		}
		
		Correlazione cor = new Correlazione(c2, thisRel);
		c1.add(cor);
		cor = new Correlazione(c1, invRel);
		c2.add(cor);
		return true;
	}


}
