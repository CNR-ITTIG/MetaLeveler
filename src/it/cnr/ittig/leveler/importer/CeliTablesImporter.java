package it.cnr.ittig.leveler.importer;

import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn.Correlazione;
import it.cnr.ittig.jwneditor.jwn.Lemma;
import it.cnr.ittig.jwneditor.jwn.Relazione;
import it.cnr.ittig.leveler.Leveler;

import java.io.File;
import java.io.IOException;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class CeliTablesImporter implements MetaImporter {
	
	private String baseDir = EditorConf.DATA_DIR + File.separatorChar 
			+ EditorConf.DB_TABLES_DIR + File.separatorChar;
	
	private String termsFN = baseDir + "TD_Terms.xls";
	private String corpusFN = baseDir + "TD_Corpus.xls";
	private String internationalFN = baseDir + "TD_DocumentsInternational.xls";
	private String nationalFN = baseDir + "TD_DocumentsNational.xls";
	private String interlinguisticFN = baseDir + "TD_InterlinguisticRelations.xls";
	private String intralinguisticFN = baseDir + "TD_IntralinguisticRelations.xls";
	private String termdocumentFN = baseDir + "TD_TermDocumentRelations.xls";
	private String languagesFN = baseDir + "TD_Languages.xls";
	
	public void createSynsets() throws IOException {
		
		Workbook wb = null;
		Sheet sheet = null;
		
		try {
			wb = Workbook.getWorkbook(new File(termsFN));
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sheet = wb.getSheet(0);
		int rows = sheet.getRows();
		for(int row = 1; row < rows; row++) {
			String lang = sheet.getCell(0, row).getContents().trim();
			String id = sheet.getCell(1, row).getContents().trim();
			String lexical = sheet.getCell(2, row).getContents().trim();
			String proto = sheet.getCell(3, row).getContents().trim();
			String freq = sheet.getCell(4, row).getContents().trim();
			
			if(!lang.equalsIgnoreCase(EditorConf.LANGUAGE)) {
				continue;
			}

			Concetto conc = new Concetto();
			conc.setID(id);
			Lemma lemma = new Lemma(proto);
			lemma.protoForm = proto;
			conc.add(lemma);
			Leveler.appSynsets.put(id, conc);
			
			//aggiungi le varianti dalla tabella AL LEMMA !! 
			lemma.variants.add(proto);
			lemma.variants.add(lexical);
		}
		
		wb.close();
	}
	
	public void addIpo() throws IOException {
		//Adds also fuzzynym relations.
		
		Workbook wb = null;
		Sheet sheet = null;
		
		try {
			wb = Workbook.getWorkbook(new File(intralinguisticFN));
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sheet = wb.getSheet(0);
		int rows = sheet.getRows();
		for(int row = 1; row < rows; row++) {
			String lang = sheet.getCell(0, row).getContents().trim();
			String relName = sheet.getCell(2, row).getContents().trim();
			String idFrom = sheet.getCell(3, row).getContents().trim();
			String idTo = sheet.getCell(4, row).getContents().trim();
			
			if(!lang.equalsIgnoreCase(EditorConf.LANGUAGE)) {
				continue;
			}

			Concetto c1 = Leveler.appSynsets.get(idFrom);
			Concetto c2 = Leveler.appSynsets.get(idTo);
			if(c1 == null || c2 == null) {
				System.out.println(">>WARNING<< NULL - " + c1 + " " + c2);
				continue;
			}
			//Add new relation
			if(!addSingleRelation(c1, c2, relName)) {
				System.err.println("Relation not found: " + relName + " !");
				break;
			}
		}
		wb.close();
	}
	
	private boolean addSingleRelation(Concetto c1, Concetto c2, String relName) {

		Relazione ipo = new Relazione(EditorConf.iponimia);
		Relazione iper = new Relazione(EditorConf.iperonimia);
		Relazione fuzzy = new Relazione(EditorConf.related);
		Relazione thisRel = null;
		Relazione invRel = null;

		if(relName.equalsIgnoreCase("has_hyponym")) {
			thisRel = ipo;
			invRel = iper;
		} else if(relName.equalsIgnoreCase("has_hyperonym")) {
			thisRel = iper;
			invRel = ipo;
		} else if(relName.equalsIgnoreCase("fuzzynym")) {
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
	
	public void addRelated() throws IOException {		
	}
	
	public void addRif() throws IOException {
		
		Workbook wb = null;
		Sheet sheet = null;
		Workbook docwb = null;
		Sheet docsheet = null;
		
		try {
			wb = Workbook.getWorkbook(new File(termdocumentFN));
			docwb = Workbook.getWorkbook(new File(nationalFN));
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sheet = wb.getSheet(0);
		docsheet = docwb.getSheet(0);
		int rows = sheet.getRows();
		for(int row = 1; row < rows; row++) {
			String lang = sheet.getCell(0, row).getContents().trim();
			String id = sheet.getCell(2, row).getContents().trim();
			String idPart = sheet.getCell(3, row).getContents().trim();
			String relName = sheet.getCell(4, row).getContents().trim();
		
			if(!lang.equalsIgnoreCase(EditorConf.LANGUAGE)) {
				continue;
			}

			//System.out.print(".");
			if(!relName.equalsIgnoreCase("mention")) {
				System.err.println("Unknown relation type: " + relName + " !");
				continue;
			}

			Concetto conc = Leveler.appSynsets.get(id);
			if(conc == null) {
				System.out.println(">>WARNING<< NULL - " + id + " " + conc);
				continue;
			}
			
			//Look for document partition
			int docrows = docsheet.getRows();
			int count = 0;
			for(int docrow = 1; docrow < docrows; docrow++) {
				String itemlang = docsheet.getCell(0, docrow).getContents().trim();
				String itemid = docsheet.getCell(1, docrow).getContents().trim();
				String part = docsheet.getCell(3, docrow).getContents().trim();
				
				//Non possono esserci riferimenti 
				//verso partizioni in lingue differenti!?
				if(!itemlang.equalsIgnoreCase(EditorConf.LANGUAGE)) {
					continue;
				}				
				if(itemid.equalsIgnoreCase(idPart)) {
					//Add a new reference
					conc.riferimenti.add(part);			
					count++;
				}
			}
			if(count == 0) {
				System.err.println("References not found: " + idPart + " !");
				break;
			}
		}
		wb.close();
		docwb.close();
	}

	public void addAlignment() throws IOException {
		
		Workbook wb = null;
		Sheet sheet = null;
		Workbook termwb = null;
		Sheet termsheet = null;
		
		try {
			wb = Workbook.getWorkbook(new File(interlinguisticFN));
			termwb = Workbook.getWorkbook(new File(termsFN));
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sheet = wb.getSheet(0);
		termsheet = termwb.getSheet(0);
		int rows = sheet.getRows();
		for(int row = 1; row < rows; row++) {
			//String relName = sheet.getCell(1, row).getContents().trim();
			String langFrom = sheet.getCell(2, row).getContents().trim();
			String idFrom = sheet.getCell(3, row).getContents().trim();
			//String idDocFrom = sheet.getCell(4, row).getContents().trim();
			String langTo = sheet.getCell(5, row).getContents().trim();
			String idTo = sheet.getCell(6, row).getContents().trim();
			//String idDocTo = sheet.getCell(7, row).getContents().trim();
			
			if(!langFrom.equalsIgnoreCase(EditorConf.LANGUAGE)) {
				continue;
			}
			//System.out.print(".");
			if(!langTo.equalsIgnoreCase("EN")) {
				System.err.println(">> LangTo is not english! Skipping...!?");
				continue;
			}

			Concetto c1 = Leveler.appSynsets.get(idFrom);
			if(c1 == null ) {
				System.out.println(">>WARNING<< NULL - " + c1);
				continue;
			}
			
			String protoConcept = "";			
			int trows = termsheet.getRows();
			for(int trow = 1; trow < trows; trow++) {
				//String termlang = termsheet.getCell(0, trow).getContents().trim();
				String termid = termsheet.getCell(1, trow).getContents().trim();
				String termproto = termsheet.getCell(3, trow).getContents().trim();
				if(termid.equalsIgnoreCase(idTo)) {
					protoConcept = termproto;
					break;
				}
			}
			if(protoConcept.equalsIgnoreCase("")) {
				System.err.println("Proto concept not found: " + 
						idFrom + " -> " + idTo + " !");
				break;
			}

			//Link the concept class to the synset
			Lemma lemma = new Lemma(protoConcept);
			lemma.protoForm = protoConcept;
			lemma.variants.add(protoConcept);
			c1.conceptLemma = lemma;
		}
		wb.close();
		termwb.close();
	}
}
