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

/**
 * @author servilla
 * @since Nov 5, 2012
 * 
 * Management for EML responsible party names.
 * 
 */
public class Creator {

	public enum CreatorType {
		
		PERSON("individualName"),
		ORGANIZATION("organizationName"), 
		POSITION("positionName");
		
		private CreatorType(String altName) { this.altName = altName; }
		public String getAltName() { return this.altName; }
		
		private String altName;

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

	public Creator(String creatorType) {
		
		if (creatorType.equals(Creator.PERSON)) {
			this.creatorType = (CreatorType) Enum.valueOf(CreatorType.class,
					"PERSON");
		} else if (creatorType.equals(Creator.ORGANIZATION)) {
			this.creatorType = (CreatorType) Enum.valueOf(CreatorType.class,
			    "ORGANIZATION");
		} else { // creatorType = POSITION
			this.creatorType = (CreatorType) Enum.valueOf(CreatorType.class,
			    "POSITION");
		}

	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

	public void setSurName(String surName) throws IllegalStateException {

		if (this.creatorType == CreatorType.PERSON) {
			this.surName = surName;
		} else {
			String gripe = "Operation not supported for this \"creator type\": ."
			    + this.creatorType.toString();
			throw new IllegalStateException(gripe);
		}

	}

	public void setGivenName(String givenName) throws IllegalStateException {

		if (this.creatorType == CreatorType.PERSON) {
			this.givenName = givenName;
		} else {
			String gripe = "Operation not supported for this \"creator type\": ."
			    + this.creatorType.toString();
			throw new IllegalStateException(gripe);
		}

	}

	public void setOrganizationName(String organizationName) throws IllegalStateException {

		if (this.creatorType == CreatorType.ORGANIZATION) {
			this.organizationName = organizationName;
		} else {
			String gripe = "Operation not supported for this \"creator type\": ."
			    + this.creatorType.toString();
			throw new IllegalStateException(gripe);
		}

	}

	public void setPositionName(String positionName) throws IllegalStateException {

		if (this.creatorType == CreatorType.POSITION) {
			this.positionName = positionName;
		} else {
			String gripe = "Operation not supported for this \"creator type\": ."
			    + this.creatorType.toString();
			throw new IllegalStateException(gripe);
		}

	}

	public String getCreatorType() {
		return this.creatorType.toString();
	}

	public String getSurName() {
		return this.surName;
	}

	public String getGivenName() {
		return this.givenName;
	}

	public String getOrganizationName() {
		return this.organizationName;
	}

	public String getPositionName() {
		return this.positionName;
	}

	public String getCreatorName() {

		if (this.creatorType == CreatorType.PERSON) {
			this.creatorName = this.surName + ", " + this.givenName;
		} else if (this.creatorType == CreatorType.ORGANIZATION) {
			this.creatorName = this.organizationName;
		} else { // CreatorType.POSITION
			this.creatorName = this.positionName;
		}

		return this.creatorName;

	}

}
