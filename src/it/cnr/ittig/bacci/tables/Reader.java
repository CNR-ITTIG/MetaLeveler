package it.cnr.ittig.bacci.tables;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reader {
	
	//FALSE: genera html con tag standard - TRUE: genera xml con tag preceduti da "h:"
	static boolean USEH = false;
	static String H = "h:";

	protected String plainFileName;
	protected String xmlFileName;

	protected File plainFile;
	protected File xmlFile;
	
	public boolean run(String plain, String xml) {
		
		plainFileName = plain;
		xmlFileName = xml;
		
		if(!USEH) {
			xmlFileName += ".html";
		}
		
		plainFile = new File(plainFileName);
		if(!plainFile.exists()) {
			return false;
		}
		xmlFile = new File(xmlFileName);
		
		boolean res = false;
		
		//Estrai il testo e analizza
	    try {

			FileInputStream fstream = new FileInputStream(plainFile);
		    DataInputStream in = new DataInputStream(fstream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    
		    res = process(br);

		    //Close the input stream
		    in.close();
	    } catch (Exception e){//Catch exception if any
	    	System.err.println("Error: " + e.getMessage());
	    }
		
		return res;
	}
	
	private boolean process(BufferedReader br) throws Exception {

		String output = "";
		
		if(!USEH) {
			H = "";
			output += "<html><body>";
		}
		
		output += "<" + H + "table border=2>";
		
		String cifraS = "\\sL.?\\s+([\\d\\.\\,])*$";
		Pattern cifraPattern = Pattern.compile(cifraS);
		
		String pmS = "\\spm\\.?$";
		Pattern pmPattern = Pattern.compile(pmS);
		
	    String strLine;
	    
	    boolean inCella = false;	    
	    boolean inLine = false;

	    while ((strLine = br.readLine()) != null)   {
	    	
	    	//Aggiungi sempre uno spazio a inizio riga
	    	strLine = " " + strLine;
	    	
	    	if(inCella) {
	    		//Il testo viene aggiunto nell'ultima cella finchè non si trova una cifra o un pm
	    		Matcher cifraMatcher = cifraPattern.matcher(strLine);
	    		Matcher pmMatcher = pmPattern.matcher(strLine);
	    		String matching = "";
	    		
	    		if(cifraMatcher.find()) {
	    			matching = cifraMatcher.group();
	    		}
	    		if(pmMatcher.find()) {
	    			matching = pmMatcher.group();
	    		}

	    		if(!matching.equals("")) {
	    			//Trovata cifra o pm
	    			inCella = false;
	    			int index = strLine.length() - matching.length();
	    			//Aggiungi il testo precedente il match nell'ultima cella aperta
	    			output += strLine.substring(0, index);
	    			//Chiudi cella e riga
	    			output += "</" + H + "td><" + H + "td>" + matching + "</" + H + "td></" + H + "tr>";

	    		} else {
	    			//Cifra o pm non ancora trovati, aggiungi tutto il testo nell'ultima cella e non chiudere la cella
	    			output += strLine;
	    		}
	    		continue;
	    	}
	    	
	    	//Se linea vuota, fai il flush della inLine (se c'è), non creare TR vuota
	    	if(strLine.trim().length() < 1) {
	    		if(inLine) {
	    			output += "</" + H + "td><" + H + "td>&nbsp;</" + H + "td><" + H + "td>&nbsp;</" + H + "td></" + H + "tr>";
		    		inLine = false;
	    		}
	    		continue;
	    	}
	    			    	
	    	//Se è un cap metti nella prima cella e poi vai "inCella"
	    	if(strLine.trim().toLowerCase().startsWith("cap.")) {
	    		output += "<" + H + "tr><" + H + "td>";
	    		output += strLine + "</" + H + "td><" + H + "td>";
	    		inCella = true;
	    		inLine = false;
	    		continue;
	    	}
	    	
	    	//Se non è vuota, appendi alla precedente oppure crea un nuovo TR in base a inLine
	    	if(!inLine) {
	    		output += "<" + H + "tr><" + H + "td>" + strLine;
	    		inLine = true;
	    		continue;
	    	}
	    	
	    	//Appendi alla inLine
    		output += strLine;

	    }
		
	    //flush inCella
	    if(inCella) {
	    	output += "</" + H + "td><" + H + "td>&nbsp;</" + H + "td></" + H + "tr>";
	    }
	    
	    //flush inLine
		if(inLine) {
			output += "</" + H + "td><" + H + "td>&nbsp;</" + H + "td><" + H + "td>&nbsp;</" + H + "td></" + H + "tr>";
		}
	    
		//Finalizza
	    output += "</" + H + "table>";
	    
	    if(!USEH) {
	    	output += "</body></html>";
	    }
		
	    //Print output?
	    //System.out.println("OUTPUT:\n\n" + output);
	    
	    //Write output file?
	    write(output);
	    
		return true;
	}
	
	private void write(String output) throws Exception {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(xmlFile));
		bw.write(output);
		bw.flush();
		bw.close();
	}
	
}
