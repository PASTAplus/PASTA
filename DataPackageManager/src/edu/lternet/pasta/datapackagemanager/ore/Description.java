package edu.lternet.pasta.datapackagemanager.ore;

import java.util.ArrayList;
import java.util.List;

public class Description {
	
	private static final String indent = "  ";
	private String about;
	private List<DescriptionElement> elements;
	
	
	public Description(String about) {
		this.about = about;
		this.elements = new ArrayList<DescriptionElement>();
	}
	
	
	public void addElement(DescriptionElement element) {
		elements.add(element);
	}
	
	
	public String toXML() {
		String xml = "";
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("%s<rdf:Description rdf:about=\"%s\">\n", indent, about));
		
		for (DescriptionElement element : elements) {
			sb.append(element.toXML());
		}
		
		sb.append(String.format("%s</rdf:Description>\n", indent));
		xml = sb.toString();
		return xml;
	}

}
