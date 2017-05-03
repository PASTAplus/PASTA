/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2017 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */
package edu.lternet.pasta.datapackagemanager.ore;

/**
 * 
 * @author Duane
 * 
 * A utility class for creating text elements with no attribute
 *
 */
public class TextElement extends DescriptionElement {

	private String text;
	
	
	/**
	 * Constructs the text element from an element name and the text content
	 * 
	 * @param name  the element name
	 * @param text  the textual content within the element
	 */
	public TextElement(String name, String text) {
		super(name);
		this.text = text;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see edu.lternet.pasta.datapackagemanager.ore.DescriptionElement#toXML()
	 */
	String toXML() {
		String xml = null;
		StringBuilder sb = new StringBuilder("");
		
		sb.append(String.format("%s<%s>%s</%s>\n", indent, elementName, text, elementName));
		
		xml = sb.toString();
		return xml;
	}
}
