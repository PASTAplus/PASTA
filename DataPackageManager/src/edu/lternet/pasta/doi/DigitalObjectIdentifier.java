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

import org.apache.log4j.Logger;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author servilla
 * @since Nov 8, 2012
 * 
 *        Manage the attributes of a DOI based on the given PASTA resource
 *        identifier.
 * 
 */
public class DigitalObjectIdentifier {

	/*
	 * Class variables
	 */
	
	private static final String prefix = "10.6073";
	private static final String context = "pasta";
	private static final String type = "DOI";

	/*
	 * Instance variables
	 */

	private Logger logger = Logger.getLogger(DigitalObjectIdentifier.class);
	
	private String pastaId = null;
	private String md5Id = null;

	/*
	 * Constructors
	 */
	
	public DigitalObjectIdentifier(String pastaId) {
		
		this.pastaId = pastaId;
		this.md5Id = DigestUtils.md5Hex(pastaId);
		
	}
	
	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */
	
	/**
	 * Get the DOI prefix.
	 * 
	 * @return DOI prefix
	 */
	public String getPrefix() {
		return DigitalObjectIdentifier.prefix;
	}
	
	/**
	 * Get DOI context.
	 * 
	 * @return DOI context
	 */
	public String getContext() {
		return DigitalObjectIdentifier.context;
	}
	
	/**
	 * Get DOI type.
	 * 
	 * @return DOI type.
	 */
	public String getType() {
		return DigitalObjectIdentifier.type;
	}
	
	/**
	 * Get DOI PASTA identifier.
	 * 
	 * @return DOI PASTA identifier
	 */
	public String getPastaId() {
		return this.pastaId;
	}
	
	/**
	 * Get DOI MD5 identifier (opaque identifier).
	 * 
	 * @return DOI MD5 identifier
	 */
	public String getMd5Id() {
		return this.md5Id;
	}
	
	/**
	 * Get the DOI canonical form.
	 * 
	 * @return DOI canonical form
	 */
	public String getDoi() {
		String doi = "doi:" + DigitalObjectIdentifier.prefix + "/"
		    + DigitalObjectIdentifier.context + "/" + this.md5Id;
		return doi;
	}

	
	/**
	 * Get the DOI identifier.
	 * 
	 * @return DOI identifier
	 */
	public String getIdentifier() {
		String identifier = DigitalObjectIdentifier.prefix + "/"
		    + DigitalObjectIdentifier.context + "/" + this.md5Id;
		return identifier;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String pastaId = "https://pasta.lternet.edu/package/metadata/eml/knb-lter-nin/1/1";
		
		DigitalObjectIdentifier doi = new DigitalObjectIdentifier(pastaId);
		
		System.out.println(doi.getDoi());

	}

}
