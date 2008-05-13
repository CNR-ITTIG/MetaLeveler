package it.cnr.ittig.bacci.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFWriter;

public class Util {
	
	public static void serialize(OntModel om, String fileName) {
		
		serialize(om, fileName, "");
	}
	
	public static void serialize(OntModel om, String fileName, String base) {
		
		RDFWriter writer = om.getWriter("RDF/XML"); //faster than RDF/XML-ABBREV		
		File outputFile = new File(fileName);
		String relativeOutputFileName = "file://" + outputFile.getAbsolutePath();
		
		if(base.trim().length() > 1) {
			//Set base property
			writer.setProperty("xmlbase", base);
		}

		System.out.println("Serializing ontology model to " + outputFile + "...");
		try {
			OutputStream out = new FileOutputStream(outputFile);
			//Write down the BASE model only (don't follow imports...)
			writer.write(om.getBaseModel(), out, relativeOutputFileName);
			out.close();
		} catch(Exception e) {
			System.err.println("Exception serializing model:" + e.getMessage());
			e.printStackTrace();
		}
	}	

	public static void initDocuments(String dataDir) {
	
		String workDir = dataDir + File.separatorChar;
		
		File file = new File(workDir + Conf.CONCEPTS);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.CONCEPTS, workDir + Conf.CONCEPTS);
		}
		file = new File(workDir + Conf.TYPES);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.TYPES, workDir + Conf.TYPES);
		}
		file = new File(workDir + Conf.IND);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.IND, workDir + Conf.IND);
		}
		file = new File(workDir + Conf.INDW);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.INDW, workDir + Conf.INDW);
		}
		file = new File(workDir + Conf.SOURCES);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.SOURCES, workDir + Conf.SOURCES);
		}
		file = new File(workDir + Conf.LINKS);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.LINKS, workDir + Conf.LINKS);
		}
		file = new File(workDir + Conf.INTERCONCEPTS);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.INTERCONCEPTS, workDir + Conf.INTERCONCEPTS);
		}
		file = new File(workDir + Conf.LEXICALIZATION);
		if(file.exists()) {
			KbModelFactory.addDocument(Conf.LEXICALIZATION, workDir + Conf.LEXICALIZATION);
		}

		
		//Single lexicons
		String origWorkDir = workDir;
		
		workDir = origWorkDir + "EN" + File.separatorChar;
		file = new File(workDir + Conf.LEXICALIZATION);
		if(file.exists()) {
			KbModelFactory.addDocument("EN" + Conf.LEXICALIZATION, workDir + Conf.LEXICALIZATION);
		}
		file = new File(workDir + Conf.IND);
		if(file.exists()) {
			KbModelFactory.addDocument("EN" + Conf.IND, workDir + Conf.IND);
		}
		
		workDir = origWorkDir + "ES" + File.separatorChar;
		file = new File(workDir + Conf.LEXICALIZATION);
		if(file.exists()) {
			KbModelFactory.addDocument("ES" + Conf.LEXICALIZATION, workDir + Conf.LEXICALIZATION);
		}
		file = new File(workDir + Conf.IND);
		if(file.exists()) {
			KbModelFactory.addDocument("ES" + Conf.IND, workDir + Conf.IND);
		}
		
		workDir = origWorkDir + "IT" + File.separatorChar;
		file = new File(workDir + Conf.LEXICALIZATION);
		if(file.exists()) {
			KbModelFactory.addDocument("IT" + Conf.LEXICALIZATION, workDir + Conf.LEXICALIZATION);
		}
		file = new File(workDir + Conf.IND);
		if(file.exists()) {
			KbModelFactory.addDocument("IT" + Conf.IND, workDir + Conf.IND);
		}
		
		workDir = origWorkDir + "NL" + File.separatorChar;
		file = new File(workDir + Conf.LEXICALIZATION);
		if(file.exists()) {
			KbModelFactory.addDocument("NL" + Conf.LEXICALIZATION, workDir + Conf.LEXICALIZATION);
		}
		file = new File(workDir + Conf.IND);
		if(file.exists()) {
			KbModelFactory.addDocument("NL" + Conf.IND, workDir + Conf.IND);
		}

	}
	
	public static File getApplicationDataDir(String appName) {
		
		File dataDir = null;		
		String homeDir = System.getProperty("user.home");		
		String osName = System.getProperty("os.name").toLowerCase();		
		
		if(osName.indexOf("win") > -1) {
			//Windows environment
			File appData = new File(homeDir + File.separatorChar +
					"Application Data");
			if(appData.exists()) {
				dataDir = new File(homeDir + File.separatorChar +
					"Application Data" + File.separatorChar + appName);
			} else {
				//Put everything under c:\?
				File cDir = new File("c:" + File.separatorChar);
				if(!cDir.exists()) {
					System.err.println("EnvUtil - Unable to read C:\\ !");
					return null;
				}
				dataDir = new File("c:" + File.separatorChar + appName);
			}
		} else {
			//For Linux and Mac use /home/user/.app ?
			dataDir = new File(homeDir + File.separatorChar + 
					"." + appName);
		}

		return dataDir;
	}
}
