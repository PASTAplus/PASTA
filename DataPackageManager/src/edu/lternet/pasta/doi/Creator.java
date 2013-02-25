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

import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.common.XmlUtility;

/**
 * @author servilla
 * @since Nov 5, 2012
 * 
 *        Management for EML responsible party names.
 * 
 */
public class Creator {

	public enum CreatorType {
		individualName, organizationName, positionName;
	};

	/*
	 * Class variables
	 */

	public static final String PERSON = "individualName";
	public static final String ORGANIZATION = "organizationName";
	public static final String POSITION = "positionName";

	/*
	 * Instance variables
	 */

	private Logger logger = Logger.getLogger(Creator.class);

	private CreatorType creatorType = null;

	private String surName = null;
	private String givenName = null;
	private String organizationName = null;
	private String positionName = null;
	private String creatorName = null; // One of "surName, givenName", "orgName",
	                                   // or "positionName".

	/*
	 * Constructors
	 */

	/**
	 * Sets creator type at initialization.
	 * 
	 * @param creatorType
	 */
	public Creator(String creatorType) {

		if (creatorType.equals(PERSON) || creatorType.equals(ORGANIZATION)
		    || creatorType.equals(POSITION)) {
			this.creatorType = (CreatorType) Enum.valueOf(CreatorType.class,
			    creatorType);
		}

	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

	/**
	 * Sets creator surname.
	 * 
	 * @param surName
	 * @throws Exception
	 */
	public void setSurName(String surName) throws Exception {

		if (this.creatorType.toString().equals(Creator.PERSON)) {
			this.surName = XmlUtility.xmlEncode(surName);
		} else {
			String gripe = "Operation not supported for this \"creator type\": "
			    + this.creatorType.toString();
			throw new Exception(gripe);
		}

	}

	/**
	 * Sets creator given name.
	 * 
	 * @param givenName
	 * @throws Exception
	 */
	public void setGivenName(String givenName) throws Exception {

		if (this.creatorType.toString().equals(Creator.PERSON)) {
			this.givenName = XmlUtility.xmlEncode(givenName);
		} else {
			String gripe = "Operation not supported for this \"creator type\": "
			    + this.creatorType.toString();
			throw new Exception(gripe);
		}

	}

	/**
	 * Sets creator organization name.
	 * 
	 * @param organizationName
	 * @throws Exception
	 */
	public void setOrganizationName(String organizationName) throws Exception {

		if (this.creatorType.toString().equals(Creator.ORGANIZATION)) {
			this.organizationName = XmlUtility.xmlEncode(organizationName);
		} else {
			String gripe = "Operation not supported for this \"creator type\": "
			    + this.creatorType.toString();
			throw new Exception(gripe);
		}

	}

	/**
	 * Sets creator position name.
	 * 
	 * @param positionName
	 * @throws Exception
	 */
	public void setPositionName(String positionName) throws Exception {

		if (this.creatorType.toString().equals(Creator.POSITION)) {
			this.positionName = XmlUtility.xmlEncode(positionName);
		} else {
			String gripe = "Operation not supported for this \"creator type\": "
			    + this.creatorType.toString();
			throw new Exception(gripe);
		}

	}

	/**
	 * Gets creator type.
	 * 
	 * @return Creator type
	 */
	public String getCreatorType() {
		return this.creatorType.toString();
	}

	/**
	 * Gets creator surname.
	 * 
	 * @return Creator surname
	 */
	public String getSurName() {
		return this.surName;
	}

	/**
	 * Gets creator given name.
	 * 
	 * @return Creator given name
	 */
	public String getGivenName() {
		return this.givenName;
	}

	/**
	 * Gets creator organization name.
	 * 
	 * @return Organization name.
	 */
	public String getOrganizationName() {
		return this.organizationName;
	}

	/**
	 * Gets creator position name.
	 * 
	 * @return Position name.
	 */
	public String getPositionName() {
		return this.positionName;
	}

	/**
	 * Gets creator name.
	 * 
	 * @return Creator name.
	 */
	public String getCreatorName() {

		if (this.creatorType.toString().equals(Creator.PERSON)) {
			this.creatorName = this.surName + ", " + this.givenName;
		} else if (this.creatorType.toString().equals(Creator.ORGANIZATION)) {
			this.creatorName = this.organizationName;
		} else { // CreatorType.POSITION
			this.creatorName = this.positionName;
		}

		return this.creatorName;

	}

}
