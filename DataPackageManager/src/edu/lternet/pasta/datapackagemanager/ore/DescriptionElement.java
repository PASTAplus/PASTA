package edu.lternet.pasta.datapackagemanager.ore;

public abstract class DescriptionElement {
	
	protected static final String indent = "    ";
	
	protected String elementName;
	
	public DescriptionElement(String name) {
		this.elementName = name;
	}
	
	abstract public String toXML();
}
