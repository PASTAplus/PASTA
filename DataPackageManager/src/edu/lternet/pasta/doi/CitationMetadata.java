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

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.eml.ResponsibleParty;
import edu.lternet.pasta.common.eml.Title;

/**
 * @author servilla
 * @since Nov 5, 2012
 *
 * Container for generic citation metadata.
 */
public class CitationMetadata {
	
	/*
	 * Class variables
	 */

	/*
	 * Instance variables
	 */
	
	private Logger logger = Logger.getLogger(CitationMetadata.class);
	
	protected String locationUrl = null;
	protected String publicationYear = null;
	protected ArrayList<ResponsibleParty> creators = null;
	protected ArrayList<Title> titles = null;
	
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
	 * Set the PASTA resource location URL.
	 * 
	 * @param locationUrl
	 */
	public void setLocationUrl(String locationUrl) {
		this.locationUrl = locationUrl;
	}
	
	/**
	 * Set the PASTA resource publication year.
	 * 
	 * @param publicationYear
	 */
	public void setPublicationYear(String publicationYear) {
		this.publicationYear = publicationYear;
	}
	
	/**
	 * Set the PASTA resource list of creators.
	 * 
	 * @param creators
	 */
	public void setCreators(ArrayList<ResponsibleParty> creators) {
		this.creators = creators;
	}
	
	/**
	 * Set the PASTA resource list of titles.
	 * 
	 * @param titles
	 */
	public void setTitles(ArrayList<Title> titles) {
		this.titles = titles;
	}
	
	/**
	 * Get the PASTA resource location URL.
	 * 
	 * @return Location URL.
	 */
	public String getLocationUrl() {
		return this.locationUrl;
	}
	
	/**
	 * Get the PASTA resource publication year.
	 * 
	 * @return Publication year.
	 */
	public String getPublicationYear() {
		return this.publicationYear;
	}
	
	/**
	 * Get the PASTA resource list of creators.
	 * 
	 * @return List of creators.
	 */
	public ArrayList<ResponsibleParty> getCreators() {
		return this.creators;
	}
	
	/**
	 * Get the PASTA resource list of titles.
	 * 
	 * @return List of titles.
	 */
	public ArrayList<Title> getTitles() {
		return this.titles;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
