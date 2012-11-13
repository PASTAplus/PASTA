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
 * @author servilla
 * @since Nov 13, 2012
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

	private DigitalObjectIdentifier identifier = null;
	private ResourceType resourceType = null;

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
		this.identifier = doi;
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
	 * Get the DataCite digital object identifier for the PASTA resource.
	 * 
	 * @return DataCite digital object identifier for the PASTA resource
	 */
	public DigitalObjectIdentifier getDigitalObjectIdentifier() {
		return this.identifier;
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
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
