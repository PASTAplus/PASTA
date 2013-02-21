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

package edu.lternet.pasta.portal.eml;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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

	public static final String INDIVIDUAL_NAME = "individualName";
	public static final String ORGANIZATION_NAME = "organizationName";
	public static final String POSITION_NAME = "positionName";

	/*
	 * Instance variables
	 */

	private Logger logger = Logger.getLogger(Creator.class);

	private CreatorType creatorType = null;

	private String surName = null;
	private String givenName = null;
	private List<String> givenNames = null;
	private String organizationName = null;
  private List<String> organizationNames = null;
	private String positionName = null;
	private String creatorName = null; // One of "surName, givenName", "orgName",
	                                   // or "positionName".
	private boolean isOrganization = false;
	private boolean isPerson = false;

	/*
	 * Constructors
	 */
	
	public Creator() {
	  this.givenNames = new ArrayList<String>();
	  this.organizationNames = new ArrayList<String>();
	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */
	
	public void addGivenName(String givenName) {
	  givenNames.add(givenName);
	}
	
	
  public void addOrganization(String organization) {
    organizationNames.add(organization);
  }
  
  
	public boolean isOrganization() {
	  return isOrganization;
	}

  public boolean isPerson() {
    return isPerson;
  }

	/**
	 * Sets creator surname.
	 * 
	 * @param surName
	 * @throws Exception
	 */
	public void setSurName(String surName) {
			this.surName = surName;
	}

	/**
	 * Sets creator given name.
	 * 
	 * @param givenName
	 * @throws Exception
	 */
	public void setGivenName(String givenName) {
			this.givenName = givenName;
	}

	/**
	 * Sets creator organization name.
	 * 
	 * @param organizationName
	 * @throws Exception
	 */
	public void setOrganizationName(String organizationName) {
			this.organizationName = organizationName;
	}

	/**
	 * Sets creator position name.
	 * 
	 * @param positionName
	 * @throws Exception
	 */
	public void setPositionName(String positionName) {
			this.positionName = positionName;
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
    StringBuffer givenNameBuffer = new StringBuffer("");

    for (String givenName : givenNames) {
      givenNameBuffer.append(givenName + " ");
    }
    
    return givenNameBuffer.toString().trim();
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
	 * Get individual name.
	 * 
	 * @return individualName Individual name.
	 */
	public String getIndividualName() {
		
		String individualName = this.surName;
		
		if (this.givenName != null) {
			individualName += ", " + this.givenName;
		}
		
		return individualName;
		
	}
	
	public void setIsOrganization(boolean isOrganization) {
	  this.isOrganization = isOrganization;
	}

  public void setIsPerson(boolean isPerson) {
    this.isPerson = isPerson;
  }

}
