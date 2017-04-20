package edu.lternet.pasta.datapackagemanager.ore;

public class TextElement extends DescriptionElement {

	private String text;
	
	public TextElement(String name, String text) {
		super(name);
		this.text = text;
	}
	
	public String toXML() {
		String xml = null;
		StringBuilder sb = new StringBuilder("");
		
		sb.append(String.format("%s<%s>%s</%s>\n", indent, elementName, text, elementName));
		
		xml = sb.toString();
		return xml;
	}
}
