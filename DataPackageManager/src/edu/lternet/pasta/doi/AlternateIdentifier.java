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

import edu.lternet.pasta.doi.ResourceType.ResourceTypeGeneral;

/**
 * @author servilla
 * @since Nov 14, 2012
 * 
 *        Manages an alternate identifier object.
 * 
 */
public class AlternateIdentifier {

	enum AlternateIdentifierType {
		ARK, DOI, EAN13, EISSN, Handle, ISBN, ISSN, ISTC, LISSN, LSID, PURL,
		UPC, URL, URN
	}

	/*
	 * Class variables
	 */

	public static final String ARK = "ARK";
	public static final String DOI = "DOI";
	public static final String EAN13 = "EAN13";
	public static final String EISSN = "EISSN";
	public static final String HANDLE = "Handle";
	public static final String ISBN = "ISBN";
	public static final String ISSN = "ISSN";
	public static final String ISTC = "ISTC";
	public static final String LISSN = "LISSN";
	public static final String LSID = "LSID";
	public static final String PURL = "PURL";
	public static final String UPC = "UPC";
	public static final String URL = "URL";
	public static final String URN = "URN";

	/*
	 * Instance variables
	 */

	private String alternateIdentifier = null;
	private AlternateIdentifierType alternateIdentifierType = null;

	/*
	 * Constructors
	 */

	/**
	 * Creates a new alternate identifier with the appropriate alternate
	 * identifier type.
	 * 
	 * @param alternateIdentifierType
	 */
	public AlternateIdentifier(String alternateIdentifierType) {

		this.alternateIdentifierType = (AlternateIdentifierType) Enum.valueOf(
		    AlternateIdentifierType.class, alternateIdentifierType);

	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

	/**
	 * Set alternate identifier.
	 * 
	 * @param alternateIdentifier
	 */
	public void setAlternateIdentifier(String alternateIdentifier) {
		this.alternateIdentifier = alternateIdentifier;
	}

	/**
	 * Get alternate identifier.
	 * 
	 * @return Alternate identifier
	 */
	public String getAlternateIdentifier() {
		return this.alternateIdentifier;
	}
	
	/**
	 * Get alternate identifier type.
	 * 
	 * @return Alternate identifier type
	 */
	public String getAlternateIdentifierType() {
		return this.alternateIdentifierType.toString();
	}

}
