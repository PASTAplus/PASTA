/*
 *
 * Copyright 2011, 2012, 2013 the University of New Mexico.
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

package edu.lternet.pasta.doi;

import java.util.ArrayList;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.eml.ResponsibleParty;
import edu.lternet.pasta.common.eml.Title;

/**
 * @author servilla
 * @since Nov 13, 2012
 * 
 *        Manages the DataCite metadata object.
 * 
 */
public class DataCiteMetadata extends CitationMetadata {

	/*
	 * Class variables
	 */

	private static final String PUBLISHER = "Long Term Ecological Research Network";

	/*
	 * Instance variables
	 */

	private Logger logger = Logger.getLogger(DataCiteMetadata.class);

	private DigitalObjectIdentifier digitalObjectIdentifier = null;
	private ResourceType resourceType = null;
	private AlternateIdentifier alternateIdentifier = null;

	/*
	 * Constructors
	 */

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

	/**
	 * Set the DataCite digital object identifier for the PASTA resource.
	 * 
	 * @param doi
	 */
	public void setDigitalObjectIdentifier(DigitalObjectIdentifier doi) {
		this.digitalObjectIdentifier = doi;
	}

	/**
	 * Set the DataCite resource type for the PASTA resource.
	 * 
	 * @param resourceType
	 */
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	/**
	 * Set the DataCite alternate identifier for the PASTA resource.
	 * 
	 * @param alternateIdentifier
	 */
	public void setAlternateIdentifier(AlternateIdentifier alternateIdentifier) {
		this.alternateIdentifier = alternateIdentifier;
	}

	/**
	 * Get the DataCite digital object identifier for the PASTA resource.
	 * 
	 * @return DataCite digital object identifier for the PASTA resource
	 */
	public DigitalObjectIdentifier getDigitalObjectIdentifier() {
		return this.digitalObjectIdentifier;
	}

	/**
	 * Get the DataCite resource type for the PASTA resource.
	 * 
	 * @return DataCite resource type for the PASTA resource
	 */
	public ResourceType getResourceType() {
		return this.resourceType;
	}

	/**
	 * Get the DataCite alternate identifier for the PASTA resource.
	 * 
	 * @return DataCite alternate identifier for the PASTA resource
	 */
	public AlternateIdentifier getAlternateIdentifier() {
		return this.alternateIdentifier;
	}

	/**
	 * Generate and return the DataCite metadata package as XML.
	 * 
	 * @return DataCite metadata package as XML
	 */
	public String toDataCiteXml() {

		StringBuffer xml = new StringBuffer("");

		// Pre-amble and opening tag
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xml.append("<resource xmlns=\"http://datacite.org/schema/kernel-2.2\"\n");
		xml.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		xml.append("    xsi:schemaLocation=\"http://datacite.org/schema/kernel-2.2");
		xml.append(" http://schema.datacite.org/meta/kernel-2.2/metadata.xsd\">\n");

		// The DOI identifier, woo-hoo
		if (this.digitalObjectIdentifier != null) {
			xml.append("    <identifier identifierType=\""
			    + this.digitalObjectIdentifier.getType() + "\">");
			xml.append(this.digitalObjectIdentifier.getIdentifier() + "</identifier>\n");
		}

		// Creators section
		xml.append("    <creators>\n");

		ArrayList<ResponsibleParty> creators = super.creators;

		if (creators != null) {
			for (ResponsibleParty creator : creators) {
			  String creatorName = creator.getCreatorName();
				xml.append("        <creator>\n");
				xml.append(String.format("            <creatorName>%s</creatorName>\n", creatorName));
				xml.append("        </creator>\n");
			}
		}

		xml.append("    </creators>\n");

		// Titles section
		xml.append("    <titles>\n");

		ArrayList<Title> titles = super.titles;

		if (titles != null) {
			for (Title title : titles) {
				xml.append("        <title>" + title.getTitle() + "</title>\n");
			}
		}

		xml.append("    </titles>\n");

		// Publisher section
		xml.append("    <publisher>" + DataCiteMetadata.PUBLISHER
		    + "</publisher>\n");

		// Publication year section
		xml.append("    <publicationYear>" + this.publicationYear
		    + "</publicationYear>\n");

		// Resource type section
		if (this.resourceType != null) {
			xml.append("    <resourceType resourceTypeGeneral=\""
			    + this.resourceType.getResourceTypeGeneral() + "\">"
			    + this.resourceType.getResourceType() + "</resourceType>\n");
		}

		// Alternate identifier section
		if (this.alternateIdentifier != null) {

			String alternateIdentifier = null;
			alternateIdentifier = this.alternateIdentifier.getAlternateIdentifier();

			xml.append("    <alternateIdentifiers>\n");
			xml.append("        <alternateIdentifier alternateIdentifierType=\"");
			xml.append(this.alternateIdentifier.getAlternateIdentifierType() + "\">");
			xml.append(alternateIdentifier);
			xml.append("</alternateIdentifier>\n");
			xml.append("    </alternateIdentifiers>\n");
		}

		xml.append("</resource>\n");

		//return DataCiteMetadata.escape(xml.toString());
		return xml.toString();

	}
	
	/**
	 * EZID percent encoding of reserved characters: '%', '\n', '\r', and ':'.
	 * 
	 * @param s String to encode
	 * @return Encoded string
	 */
	public static String escape(String s) {
	  return s.replace("%", "%25").replace("\n", "%0A").
	      replace("\r", "%0D").replace(":", "%3A");
	  }
	
	/**
	 * EZID percent decoding of reserved characters: '%', '\n', '\r', and ':'.
	 * 
	 * @param s String to decode
	 * @return Decoded string
	 */
	public static String unescape(String s) {
		
	  StringBuffer b = new StringBuffer();
	  int i;
	  while ((i = s.indexOf("%")) >= 0) {
	    b.append(s.substring(0, i));
	    b.append((char) Integer.parseInt(s.substring(i+1, i+3), 16));
	    s = s.substring(i+3);
	  }
	  b.append(s);
	  return b.toString();
	  
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
