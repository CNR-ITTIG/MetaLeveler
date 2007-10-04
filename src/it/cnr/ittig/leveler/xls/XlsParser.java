package it.cnr.ittig.leveler.xls;

import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn.Lemma;
import it.cnr.ittig.leveler.Leveler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class XlsParser {
	
	private String classification = EditorConf.DATA_DIR + "/" + "classification.xls";
	private String mappings = EditorConf.DATA_DIR + "/" + "mappings.xls";
	private String definitions = EditorConf.DATA_DIR + "/" + "claw-def.xls";
	
	private WritableWorkbook wb = null;
	private WritableSheet sheet = null;
	private int row = 0;
	private Vector<OntClass> classes = null;
	
	private Sheet mapSheet = null;
	
	public void classify() {
		//Aggiunge le classi ontologiche agli oggetti synset
		
		Workbook wb = null;
		Sheet sheet = null;
		
		try {
			wb = Workbook.getWorkbook(new File(classification));
			Workbook tmpwb = Workbook.getWorkbook(new File(mappings));
			mapSheet = tmpwb.getSheet(0);
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sheet = wb.getSheet(0);
		int rows = sheet.getRows();
		for(row = 1; row < rows; row++) {
			String kwid = sheet.getCell(0, row).getContents().trim();
			String oc1 = sheet.getCell(2, row).getContents().trim();
			String oc2 = sheet.getCell(3, row).getContents().trim();
			String oc3 = sheet.getCell(4, row).getContents().trim();
			String oc4 = sheet.getCell(5, row).getContents().trim();
			if(oc1.length() > 0) {
				Concetto c = Leveler.appSynsets.get(kwid);
				if(c == null) {
					System.out.println("c is null!! kwid:" + kwid);
					continue;
				}
				c.ontoclassi.add(resolve(oc1));
				if(oc2.length() > 0) {
					c.ontoclassi.add(resolve(oc2));
					if(oc3.length() > 0) {
						c.ontoclassi.add(resolve(oc3));
						if(oc4.length() > 0) {
							c.ontoclassi.add(resolve(oc4));
						}
					}
				}
			}			
		}		
	}
	
	private String resolve(String oc) {
		//Restituisce la classe completa di namespace
		
		int id = -1;
		
		try {
			id = Integer.valueOf(oc);
		} catch (NumberFormatException e) {			

			for(int i = 1; i < mapSheet.getRows(); i++) {
				String rontoclasse = mapSheet.getCell(1, i).getContents().trim();
				if(rontoclasse.equalsIgnoreCase(oc)) {
					String rclasse = mapSheet.getCell(2, i).getContents().trim();
					String rspace = mapSheet.getCell(3, i).getContents().trim();
					return rspace + rclasse;
				}
			}			
		}
		
		for(int i = 1; i < mapSheet.getRows(); i++) {
			String rid = mapSheet.getCell(0, i).getContents().trim();
			if(rid.equalsIgnoreCase(oc)) {
				String rclasse = mapSheet.getCell(2, i).getContents().trim();
				String rspace = mapSheet.getCell(3, i).getContents().trim();
				return rspace + rclasse;
			}
		}
		
		return "";
	}
	
	public void addDefinitions() {
		//Aggiunge eventuali definizioni
		
		Workbook wb = null;
		Sheet sheet = null;
		
		try {
			wb = Workbook.getWorkbook(new File(definitions));
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sheet = wb.getSheet(0);
		int rows = sheet.getRows();
		for(row = 1; row < rows; row++) {
			String num = sheet.getCell(0, row).getContents().trim();
			String lemmaEN = sheet.getCell(3, row).getContents().trim();
			String lemmaIT = sheet.getCell(3, row).getContents().trim();
			String defEN = sheet.getCell(2, row).getContents().trim();
			String defIT = sheet.getCell(4, row).getContents().trim();
			if(lemmaIT.length() < 1) {
				continue;
			}
			Concetto c = getConcettoByLemma(lemmaIT);
			if(c == null) {
				System.out.println(">>WARNING<< Definition without matching synset! " +
						"lemma: " + lemmaIT);				
				continue;
			}
			//System.out.println("++ Adding DEFINITION : " + lemmaIT + " - " + defIT);
			c.setDefinizione(defIT + " (" + num + ")");
		}
	}
	
	private Concetto getConcettoByLemma(String lemma) {
		
		Collection<Concetto> syns = Leveler.appSynsets.values();
		for(Iterator<Concetto> i = syns.iterator(); i.hasNext(); ) {
			Concetto c = i.next(); 
			for(int k = 0; k < c.lemmi.size(); k++) {
				Lemma l = c.lemmi.get(k);
				if(lemma.equalsIgnoreCase(l.getLexicalForm())) {
					return c;
				}
			}
		}
		return null;
	}
	
	//Da usare per creare il file excel la prima volta
	public void fill() {
		
		try {
			wb = Workbook.createWorkbook(new File(classification));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Label label;
		try {
			sheet = wb.createSheet("classification", 0);
			label = new Label(0, 0, "KWID");
			sheet.addCell(label);
			label = new Label(1, 0, "TERMINE");
			sheet.addCell(label);
			label = new Label(2, 0, "ONTOCLASSE");
			sheet.addCell(label);
			label = new Label(3, 0, "ONTOCLASSE");
			sheet.addCell(label);
			label = new Label(4, 0, "ONTOCLASSE");
			sheet.addCell(label);
			label = new Label(5, 0, "ONTOCLASSE");
			sheet.addCell(label);
		} catch (RowsExceededException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		row = 1;
		Collection<String> keys = Leveler.appSynsets.keySet();		
		Collection<Concetto> synsets = Leveler.appSynsets.values();
		
		//Trova la chiave pi� alta...
		int max = 0;
		for(Iterator<String> i = keys.iterator(); i.hasNext();) {
			String str = i.next();
			int value = Integer.valueOf(str);
			if(value > max) {
				max = value;
			}
		}		
		max++;
		
		//Riempi il foglio excel con i termini ordinati per KWID
		for(int i = 0; i < max; i++) {
			String id = String.valueOf(i);
			Concetto c = Leveler.appSynsets.get(id);
			if(c == null) {
				continue;
			}
			try {
				label = new Label(0, row, c.getID());
				sheet.addCell(label);
				label = new Label(1, row, c.lemmi.get(0).getLexicalForm());
				sheet.addCell(label);
				row++;
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			wb.write();
			wb.close();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createMappingClass() {
		
		try {
			wb = Workbook.createWorkbook(new File(mappings));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Label label;
		try {
			sheet = wb.createSheet("mappings", 0);
			label = new Label(0, 0, "ID");
			sheet.addCell(label);
			label = new Label(1, 0, "ONTOCLASSE");
			sheet.addCell(label);
			label = new Label(2, 0, "CLASSE");
			sheet.addCell(label);
			label = new Label(3, 0, "URL");
			sheet.addCell(label);
		} catch (RowsExceededException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		row = 1;
		
		ModelMaker maker = ModelFactory.createMemModelMaker();
		Reasoner r = ReasonerRegistry.getOWLMicroReasoner();
		//OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
		OntModelSpec spec =  OntModelSpec.OWL_MEM ;
        spec.setReasoner(r);
		spec.setImportModelMaker(maker);
		OntModel om = ModelFactory.createOntologyModel(spec, null);		
		//om.read(EditorConf.clawModel);
		om.read("file://" + EditorConf.DATA_DIR + "/consumer-law-merge25-09.owl");
		//String ns = EditorConf.clawModel + "#";

		classes = new Vector<OntClass>();
		
//		OntClass oc = om.getOntClass("http://www.w3.org/2002/07/owl#Nothing");		
//		expand(oc);
		
		for(Iterator<OntClass> i = om.listClasses(); i.hasNext();) {
			OntClass oc = i.next();
			if(oc.isAnon() || 
					!oc.getNameSpace().equalsIgnoreCase(EditorConf.clawModel + "#")) {
				continue;				
			}
			addSortedClass(oc);
		}
		
		addMapping();
		
		try {
			wb.write();
			wb.close();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void addSortedClass(OntClass oc) {
		
		boolean ins = false;
		for(int i = 0; i < classes.size(); i++) {
			OntClass item = classes.get(i);
			if(item.getLocalName().compareToIgnoreCase(oc.getLocalName()) < 0) continue;
			if(item.getLocalName().compareToIgnoreCase(oc.getLocalName()) > 0) {
				classes.add(i, oc);
				ins = true;
				break;
			}
		}
		if(!ins) {
			//Inserisci alla fine del vettore
			classes.add(oc);
		}
	}

	
//	private void expand(OntClass oc) {
//	
//		System.out.println("Expanding " + oc.getNameSpace() + oc.getLocalName());
//		for(Iterator i = oc.listSuperClasses(true); i.hasNext();) {
//			OntClass c = (OntClass) i.next();
//			if(c.isAnon()) {
//				continue;
//			}
////			if(c.getLocalName().equalsIgnoreCase("thing") ||
////					c.getLocalName().equalsIgnoreCase("resource")) {
////				continue;
////			}
//			if(!c.getNameSpace().equalsIgnoreCase(EditorConf.clawModel + "#")) {
//				continue;
//			}
//			if(!classes.contains(c)) {
//				classes.add(c);
//			}
//			expand(c);
//		}
//	}
	
	//private void addMapping(OntClass oc) {
	private void addMapping() {
		
		for(int i = 0; i < classes.size(); i++) {
			OntClass oc = classes.get(i);
			String name = oc.getLocalName();
			String space = oc.getNameSpace();
			try {
				Label label = new Label(0, row, String.valueOf(i + 1));
				sheet.addCell(label);
				label = new Label(1, row, name);
				sheet.addCell(label);
				label = new Label(2, row, name);
				sheet.addCell(label);
				label = new Label(3, row, space);
				sheet.addCell(label);
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			row++;
		}
	}
}
