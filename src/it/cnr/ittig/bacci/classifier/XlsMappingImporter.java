package it.cnr.ittig.bacci.classifier;

import it.cnr.ittig.bacci.classifier.resource.BasicResource;
import it.cnr.ittig.bacci.classifier.resource.ConceptClass;
import it.cnr.ittig.bacci.classifier.resource.OntologicalClass;
import it.cnr.ittig.bacci.util.Conf;
import it.cnr.ittig.jwneditor.editor.EditorConf;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.hp.hpl.jena.ontology.OntClass;

public class XlsMappingImporter {
	
	private static String classification = Conf.DATA_DIRECTORY + File.separatorChar 
		+ Conf.CLASSIFICATION;
	private static String mappings = Conf.DATA_DIRECTORY + "/" + File.separatorChar 
		+ Conf.MAPPING;
	
	private static String definitions = Conf.DATA_DIRECTORY + "/" + "claw-def.xls";
	
	private static WritableWorkbook wb = null;
	private static WritableSheet sheet = null;
	private static int row = 0;
	private static Vector<OntClass> classes = null;
	
	private static Sheet mapSheet = null;
	
	private static DataManager dm = null;
	
	private static Set<String> nonMatchingClass = new HashSet<String>();
	private static Set<String> nonMatchingLemma = new HashSet<String>();
	
	public static void classify(DataManager datam) {
		//Adds ontological classes		
		
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
			if(oc1.length() > 0 && !oc1.equalsIgnoreCase("no")) {
				BasicResource br = getSynsetByLemma(lemma);
				if(br != null) {
//					System.out.println("classify() - synset identified: " + br);
				} else {
//						System.err.println(
//								">> WARNING! classify() - c is null, " +
//								"matching lemma not found or invalid kwid! kwid:" 
//								+ kwid + ", lemma:" + lemma);
					continue;
				}
				if(oc1.equalsIgnoreCase("no")) {
					//Rimuovere il synset dal lessico ?
//					Leveler.appSynsets.keySet().remove(kwid);
//					Leveler.appSynsets.values().remove(c);
					continue;
				}

				analyzeClassField(br, oc1);
				analyzeClassField(br, oc2);
				analyzeClassField(br, oc3);
				analyzeClassField(br, oc4);
			}			
		}		
	}
	
	private static void analyzeClassField(BasicResource br, String name) {
		
		if(name.length() < 1) {
			return;
		}
		OntologicalClass oc = checkOntologicalClass(resolve(name));
		if(oc != null) {
			ConceptClass c = br.getConcept();
			if(c == null) {
				c = dm.addArtificialConceptClass(br);
			}
			c.addClass(oc);					
		}
	}
	
	private static BasicResource getSynsetByLemma(String lemma) {
		
		Collection<BasicResource> synsets = dm.getResources();
		for(Iterator<BasicResource> iter = synsets.iterator(); iter.hasNext(); ) {
			BasicResource item = iter.next();
			Collection<String> variants = item.getVariants();
			for(Iterator<String> v = variants.iterator(); v.hasNext(); ) {
				String variant = v.next();
				if(matchLemma(lemma, variant)) {
					//System.out.println("LEMMA OK a: " + lemma + " b:" + variant);
					return item;
				}					
			}
		}
		
		if(!nonMatchingLemma.contains(lemma)) {
			nonMatchingLemma.add(lemma);
			System.err.println(">>> LEMMA match not found! name: " + lemma);
		}
		return null;
	}
	
	private static boolean matchLemma(String lemma1, String lemma2) {
		
		String a = lemma1.trim();
		String b = lemma2.trim();
		if(a.equalsIgnoreCase(b)) {
			return true;
		}
		a = smoothString(a);
		b = smoothString(b);
		if(a.equalsIgnoreCase(b)) {
			System.out.println("matchLemma(): (1) " + a + " <-> " + b);
			return true;
		}
		return false;
	}
	
	private static String smoothString(String str) {
		
		String empty = "";
		str = str.replaceAll(" ", empty);
		str = str.replaceAll("-", empty);
		str = str.replaceAll("_", empty);
		str = str.replaceAll("'", empty);
		str = str.replaceAll("`", empty);
		str = str.replaceAll("°", empty);
		return str;
	}
	
	private static boolean matchClass(String name, OntologicalClass oc) {
		
		//System.out.println("Matching name " + name + " with class " + oc + "...");
		return matchLemma(name, oc.toString());
	}
	
	private static OntologicalClass checkOntologicalClass(String name) {
		//Confronta name con le lexical form delle ontological class presenti,
		
		Collection<OntologicalClass> ocs = dm.getClasses();
		for(Iterator<OntologicalClass> i = ocs.iterator(); i.hasNext(); ) {
			OntologicalClass oc = i.next();
			if(matchClass(name, oc)) {
//				System.out.println("::MATCH FOUND! name: " + name 
//						+ " --> oc: " + oc);
				return oc;
			}
		}
		if(!nonMatchingClass.contains(name)) {
			nonMatchingClass.add(name);
			System.err.println(">>> CLASS match not found! name: " + name);
		}
		return null;
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
//				String rspace = "http://turing.ittig.cnr.it/jwn/ontologies/consumer-law.owl#";
//				return rspace + rclasse;
				return rclasse;
			}
		}
		
		return "";
	}
}