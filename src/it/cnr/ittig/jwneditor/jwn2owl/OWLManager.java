package it.cnr.ittig.jwneditor.jwn2owl;

import it.cnr.ittig.jwneditor.jwn2owl.container.InMemoryOntology;
import it.cnr.ittig.jwneditor.jwn2owl.container.OntologyContainer;
import it.cnr.ittig.jwneditor.jwn2owl.container.PersistentOntology;
import it.cnr.ittig.jwneditor.jwn2owl.service.AddService;
import it.cnr.ittig.jwneditor.jwn2owl.service.SerializeService;
import it.cnr.ittig.jwneditor.jwn2owl.service.ValidateService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;

/*
 * Classe di interfaccia con il resto dell'applicazione.
 * Costruisce e mantiene l'oggetto container.
 * Riceve comunicazioni dall'applicazione, valida le 
 * richieste e le passa alle classi specifiche, che
 * lavorano sull'ontologia richiedendo l'OntModel
 * al container.
 */
public class OWLManager {

	Map<String,OntologyContainer> containers;
	AddService adder;
	ValidateService validator;
	SerializeService writer;
//	CheckerService checker;
	
	public OWLManager() {
		
		containers = new HashMap<String,OntologyContainer>();
		
		startServices();
	}
	
	private void startServices() {
		
		adder = new AddService(this);
		validator = new ValidateService();
		writer = new SerializeService();
//		checker = new CheckerService();
	}
	
	public Map<String,OntologyContainer> getContainers() {
		
		return containers;
	}
	
	public boolean setModel(String modelName, OntModel om) {
		
		OntologyContainer cont = containers.get(modelName);
		if(cont == null) {
			return false;
		}
		
		if(!(cont instanceof InMemoryOntology)) {
			return false;
		}
		
		((InMemoryOntology) cont).setOntModel(om);
		return true;
	}
	
	/*
	 * 
	 */
	public boolean addModel(String modelName) {
		
		containers.put(modelName, new InMemoryOntology(modelName));
		return true;
	}
	
	/*
	 * If persistent is true, create a Persistent
	 * container with default connection parameters.
	 * TODO
	 * This method should not be used! Always pass
	 * connection parameters to OWLManager.
	 */
	public boolean addModel(String modelName, boolean persistent) {
		
		if(containers.containsKey(modelName)) {
			System.err.println("Container key exist! name: " + modelName);
			return false;
		}
		
		if(persistent) {
			containers.put(modelName, new PersistentOntology(modelName));
		} else {
			containers.put(modelName, new InMemoryOntology(modelName));
		}
		
		return true;
	}
	
	/*
	 * Create a Persistent container with 
	 * specified connection parameters.
	 * (useless? there is only one config. for jena db... (it's in conf file) ).
	 */
	public boolean addModel(String modelName, String URL, String User, String Pw, 
								String Type, String driver) {
		
		containers.put(modelName, new PersistentOntology(modelName, URL, 
													User, Pw, Type, driver));
		return true;
	}
	
	public void resetModel(String modelName) {
		
		//check model
		OntologyContainer cont = containers.get(modelName);
		if(cont == null) {
			System.err.println("Model not found: " + modelName);
			return;
		}
		
		cont.resetModel();
	}
	
	public void addIndividuals(String modelName, Collection data) {
		
		//INPUT VALIDATION
		//In this application, data must be a collection of objects "Concetto"
		
		//check model
		OntologyContainer cont = containers.get(modelName);
		if(cont == null) {
			System.err.println("Model not found: " + modelName);
			return;
		}
		
		//Process input
		adder.process(cont, data);
	}
	
	public void validateModel(String modelName) {

		//check model
		OntologyContainer cont = containers.get(modelName);
		if(cont == null) {
			System.err.println("Model not found: " + modelName);
			return;
		}

		//Validate model
		validator.process(cont);
	}	
	
	public void writeModel(String modelName, String file, String ns) {
		
		//INPUT VALIDATION
		//file esiste? nome valido?...

		//check model
		OntologyContainer cont = containers.get(modelName);
		if(cont == null) {
			System.err.println("Model not found: " + modelName);
			return;
		}

		//Serialize the OntModel
		writer.process(cont, file, ns);
	}
	
//	public void checkModel(String modelName) {		
//
//		//check model
//		OntologyContainer cont = containers.get(modelName);
//		if(cont == null) {
//			System.err.println("Model not found: " + modelName);
//			return;
//		}
//
//		//Validate model
//		checker.process(cont);
//	}
}