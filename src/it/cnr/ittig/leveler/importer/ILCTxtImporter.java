package it.cnr.ittig.leveler.importer;

import it.cnr.ittig.jwneditor.editor.EditorConf;
import it.cnr.ittig.jwneditor.jwn.Concetto;
import it.cnr.ittig.jwneditor.jwn.Correlazione;
import it.cnr.ittig.jwneditor.jwn.Lemma;
import it.cnr.ittig.jwneditor.jwn.Relazione;
import it.cnr.ittig.leveler.Leveler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ILCTxtImporter implements MetaImporter {
	
	private String glossario = EditorConf.DATA_DIR + "/" + "glossario.txt";
	private String glossarioNT = EditorConf.DATA_DIR + "/" + "glossario_nt.txt";
	private String glossarioRT = EditorConf.DATA_DIR + "/" + "glossario_rt.txt";
	private String glossarioRIF = EditorConf.DATA_DIR + "/" + "glossario_documenti.txt";
	
	public void createSynsets() throws IOException {
		
		BufferedReader inputStream = null;
		
		try {
			inputStream = new BufferedReader(new FileReader(glossario));
			
			String l;
			while((l = inputStream.readLine()) != null) {
				String[] data = l.split("\t");
				if(data.length != 4) {
					System.out.println(">>WARNING<< SIZE:" + data.length +
							" DATA:" + data);
				}
				if(data[0].equalsIgnoreCase("kwid")) {
					continue;
				}
				try {
					Integer.parseInt(data[0]);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					System.out.println(">>WARNING<< ID:" + data[0] +
							" DATA:" + data);
					continue;
				}
				String proto = data[3].toLowerCase().trim();
				String str = data[1].toLowerCase().trim();
				Concetto conc = new Concetto();
				conc.setID(data[0]);
				Lemma lemma = new Lemma(proto);
				lemma.protoForm = proto;
				conc.add(lemma);
				Leveler.appSynsets.put(data[0], conc);
				
				//aggiungi le varianti dalla tabella AL LEMMA !! 
				lemma.variants.add(proto);
				lemma.variants.add(str);
								
			}
		} finally {
			if(inputStream != null) {
				inputStream.close();
			}
		}
	}
	
	public void addIpo() throws IOException {
		
		Relazione ipo = new Relazione(EditorConf.iponimia);
		Relazione iper = new Relazione(EditorConf.iperonimia);
		
		BufferedReader inputStream = null;
		
		try {
			inputStream = new BufferedReader(new FileReader(glossarioNT));
			
			String l;
			while((l = inputStream.readLine()) != null) {
				String[] data = l.split("\t");
				if(data.length != 4) {
					System.out.println(">>WARNING<< SIZE:" + data.length +
							" DATA:" + data);
				}
				if(data[0].equalsIgnoreCase("nt_id")) {
					continue;
				}
				try {
					Integer.parseInt(data[1]);
					Integer.parseInt(data[2]);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					System.out.println(">>WARNING<< ID - DATA:" + data);
					continue;
				}
				Concetto c1 = Leveler.appSynsets.get(data[1]);
				Concetto c2 = Leveler.appSynsets.get(data[2]);
				if(c1 == null || c2 == null) {
					System.out.println(">>WARNING<< NULL - " + c1 + " " + c2);
					continue;
				}
				Correlazione cor = new Correlazione(c2, ipo);
				c1.add(cor);
				cor = new Correlazione(c1, iper);
				c2.add(cor);
			}
		} finally {
			if(inputStream != null) {
				inputStream.close();
			}
		}
	}
			
	public void addRelated() throws IOException {
		
		Relazione rel = new Relazione(EditorConf.related);
		
		BufferedReader inputStream = null;
		
		try {
			inputStream = new BufferedReader(new FileReader(glossarioRT));
			
			String l;
			while((l = inputStream.readLine()) != null) {
				String[] data = l.split("\t");
				if(data.length != 4) {
					System.out.println(">>WARNING<< SIZE:" + data.length +
							" DATA:" + data);
				}
				if(data[0].equalsIgnoreCase("rt_id")) {
					continue;
				}
				try {
					Integer.parseInt(data[1]);
					Integer.parseInt(data[2]);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					System.out.println(">>WARNING<< ID - DATA:" + data);
					continue;
				}
				Concetto c1 = Leveler.appSynsets.get(data[1]);
				Concetto c2 = Leveler.appSynsets.get(data[2]);
				if(c1 == null || c2 == null) {
					System.out.println(">>WARNING<< NULL - " + c1 + " " + c2);
					continue;
				}
				Correlazione cor = new Correlazione(c2, rel);
				c1.add(cor);
				cor = new Correlazione(c1, rel);
				c2.add(cor);
			}
		} finally {
			if(inputStream != null) {
				inputStream.close();
			}
		}
	}
	
	public void addRif() throws IOException {
		
		BufferedReader inputStream = null;
		
		try {
			inputStream = new BufferedReader(new FileReader(glossarioRIF));
			
			String l;
			while((l = inputStream.readLine()) != null) {
				String[] data = l.split("\t");
				if(data.length != 4) {
					System.out.println(">>WARNING<< SIZE:" + data.length +
							" DATA:" + data);
				}
				if(data[0].equalsIgnoreCase("kwid")) {
					continue;
				}
				try {
					Integer.parseInt(data[0]);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					System.out.println(">>WARNING<< ID - DATA:" + data);
					continue;
				}
				Concetto conc = Leveler.appSynsets.get(data[0]);
				if(conc == null) {
					System.out.println(">>WARNING<< NULL - " + data[0] + " " + conc);
					continue;
				}
				conc.riferimenti.add(data[1]);
			}
		} finally {
			if(inputStream != null) {
				inputStream.close();
			}
		}
	}
	
	public void addAlignment() throws IOException {		
	}
}
