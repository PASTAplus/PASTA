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


/**
 * @author Duane Costa
 * @since Feb 5, 2018
 * 
 *        Manages a related identifier object.
 * 
 */
public class RelatedIdentifier {

	enum RelatedIdentifierType {
		ARK, arXiv, bibcode, DOI, EAN13, EISSN, Handle, IGSN, ISBN, ISSN, ISTC, LISSN, LSID, PMID, PURL,
		UPC, URL, URN
	}

	/*
	 * Class variables
	 */

	public static final String IS_CITED_BY = "IsCitedBy";

	
	/*
	 * Instance variables
	 */

	private String relatedIdentifier = null;
	private RelatedIdentifierType relatedIdentifierType = null;
	private String relationType = null;

	/*
	 * Constructors
	 */

	/**
	 * Creates a new related identifier with the appropriate related
	 * identifier type. 
	 * 
	 * The only relationType we're currently interested in documenting
	 * is the "isCitedBy" relation type. In other words, the data package is cited
	 * by the journal article (via the journal article DOI or URL). 
	 * 
	 * @param relatedIdentifierType
	 */
	public RelatedIdentifier(String relatedIdentifier, RelatedIdentifierType relatedIdentifierType) {
	    this.relatedIdentifier = relatedIdentifier;
		this.relatedIdentifierType = relatedIdentifierType;
		this.relationType = IS_CITED_BY;
	}

	
	/*
	 * Class methods
	 */

	
	/*
	 * Instance methods
	 */

	/**
	 * Set related identifier.
	 * 
	 * @param relatedIdentifier
	 */
	public void setRelatedIdentifier(String relatedIdentifier) {
		this.relatedIdentifier = relatedIdentifier;
	}

	
	/**
	 * Get related identifier.
	 * 
	 * @return Related identifier
	 */
	public String getRelatedIdentifier() {
		return this.relatedIdentifier;
	}
	
	
	/**
	 * Get related identifier type.
	 * 
	 * @return Related identifier type
	 */
	public String getRelatedIdentifierType() {
		return this.relatedIdentifierType.name();
	}
	
	
	public String getRelationType() {
	    return this.relationType;
	}

}
