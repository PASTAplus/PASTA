package edu.lternet.pasta.datapackagemanager.ore;

public class EmptyElement extends DescriptionElement {

	private String attributeName;
	private String attributeValue;
	
	public EmptyElement(String name, String attName, String attValue) {
		super(name);
		this.attributeName = attName;
		this.attributeValue = attValue;
	}
	
	public String toXML() {
		String xml = null;
		StringBuilder sb = new StringBuilder("");
		
		sb.append(String.format("%s<%s %s=\"%s\"/>\n", 
				                indent, elementName, attributeName, attributeValue));
		
		xml = sb.toString();
		return xml;
	}
}
