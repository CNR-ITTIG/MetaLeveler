package it.cnr.ittig.jwneditor.jwn;

public class Riferimento {

	private String text;
	private String code;
	private String fileName;
	
	public Riferimento() {

		this.text = null;
		this.code = null;
		this.fileName = null;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
