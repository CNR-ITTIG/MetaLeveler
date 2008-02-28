package it.cnr.ittig.bacci.classifier;

import it.cnr.ittig.bacci.classifier.resource.BasicResource;
import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn.Lemma;
import it.cnr.ittig.leveler.Leveler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.hp.hpl.jena.ontology.OntClass;

public class XlsMappingImporter {
	
	private static String classification = EditorConf.DATA_DIR + File.separatorChar 
		+ EditorConf.CLASSIFICATION;
	private static String mappings = EditorConf.DATA_DIR + "/" + File.separatorChar 
		+ EditorConf.MAPPING;
	
	private static String definitions = EditorConf.DATA_DIR + "/" + "claw-def.xls";
	
	private static WritableWorkbook wb = null;
	private static WritableSheet sheet = null;
	private static int row = 0;
	private static Vector<OntClass> classes = null;
	
	private static Sheet mapSheet = null;
	
	private static DataManager dm = null;
	
	public static void classify(DataManager datam) {
		//Aggiunge le classi ontologiche agli oggetti synset
		
		dm = datam;
		
		Workbook wb = null;
		Sheet sheet = null;
		
		try {
			wb = Workbook.getWorkbook(new File(classification));
			Workbook tmpwb = Workbook.getWorkbook(new File(mappings));
			mapSheet = tmpwb.getSheet(0);
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sheet = wb.getSheet(0);
		int rows = sheet.getRows();
		for(row = 1; row < rows; row++) {			
			String kwid = sheet.getCell(0, row).getContents().trim();
			String lemma = sheet.getCell(1, row).getContents().trim();
			String oc1 = sheet.getCell(2, row).getContents().trim();
			String oc2 = sheet.getCell(3, row).getContents().trim();
			String oc3 = sheet.getCell(4, row).getContents().trim();
			String oc4 = sheet.getCell(5, row).getContents().trim();
			if(oc1.length() > 0) {
				Concetto c = getSynsetByLemma(lemma);
				if(c != null) {
					System.out.println("classify() - synset identified: " + c);
				} else {
					if(oc1.trim().length() > 0 && !oc1.trim().equalsIgnoreCase("no")) {
						System.err.println(
								">> WARNING! classify() - c is null, " +
								"matching lemma not found or invalid kwid! kwid:" 
								+ kwid + ", lemma:" + lemma);
					}
					continue;
				}
				if(oc1.equalsIgnoreCase("no")) {
					//Rimuovere il synset dal lessico ?
//					Leveler.appSynsets.keySet().remove(kwid);
//					Leveler.appSynsets.values().remove(c);
					continue;
				}
				
				c.ontoclassi.add(resolve(oc1));
				if(oc2.trim().length() > 0) {
					c.ontoclassi.add(resolve(oc2));
					if(oc3.trim().length() > 0) {
						c.ontoclassi.add(resolve(oc3));
						if(oc4.trim().length() > 0) {
							c.ontoclassi.add(resolve(oc4));
						}
					}
				}
			}			
		}		
	}
	
	private static Concetto getSynsetByLemma(String lemma) {
		
		Concetto c = null;
		Collection<BasicResource> synsets = dm.getResources();
		for(Iterator<Concetto> iter = synsets.iterator(); iter.hasNext(); ) {
			Concetto item = iter.next();
			for(int i = 0; i < item.lemmi.size(); i++) {
				Lemma thisLemma = item.lemmi.get(i);
				for(int v = 0; v < thisLemma.variants.size(); v++) {
					String variant = thisLemma.variants.get(v);
					if(matchLemma(lemma, variant)) {
						c = item;
						//System.out.println("LEMMA OK a: " + lemma + " b:" + variant);
						break;
					}					
				}
			}
		}		
		return c;
	}
	
	private static boolean matchLemma(String lemma1, String lemma2) {
		
		String a = lemma1.trim();
		String b = lemma2.trim();
		//System.out.println("a: " + lemma1 + " b:" + lemma2);
		if(a.equalsIgnoreCase(b)) {
			return true;
		}
		String empty = "";
		a = a.replaceAll(" ", empty);
		b = b.replaceAll(" ", empty);
		if(a.equalsIgnoreCase(b)) {
			System.out.println("matchLemma(): (1) " + a + " <-> " + b);
			return true;
		}
		return false;
	}
	
	private static String resolve(String oc) {
		//Restituisce la classe completa di namespace
		
		try {
			int id = Integer.valueOf(oc);
		} catch (NumberFormatException e) {			

//			for(int i = 1; i < mapSheet.getRows(); i++) {
//				String rontoclasse = mapSheet.getCell(1, i).getContents().trim();
//				if(rontoclasse.equalsIgnoreCase(oc)) {
//					String rclasse = mapSheet.getCell(2, i).getContents().trim();
//					String rspace = mapSheet.getCell(3, i).getContents().trim();
//					return rspace + rclasse;
//				}
//			}
			System.err.println("Invalid onto class: " + oc);
			return null;
		}
		
		for(int i = 1; i < mapSheet.getRows(); i++) {
			String rid = mapSheet.getCell(0, i).getContents().trim();
			if(rid.equalsIgnoreCase(oc)) {
				String rclasse = mapSheet.getCell(2, i).getContents().trim();
				//String rspace = mapSheet.getCell(3, i).getContents().trim();
				String rspace = "http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl#";
				return rspace + rclasse;
			}
		}
		
		return "";
	}
}