/*
 *
 * $Date: 2012-04-02 11:10:19 -0700 (Mon, 02 Apr 2012) $
 * $Author: dcosta $
 * $Revision: $
 *
 * Copyright 2011,2012 the University of New Mexico.
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

package edu.lternet.pasta.common.eml;

import java.util.ArrayList;

import edu.lternet.pasta.common.eml.Entity.EntityType;


/**
 * DataPackage holds metadata values about a data package that were parsed 
 * from an EML document.
 *  
 * @author dcosta
 *
 */
public class DataPackage {
  
  /*
   * Class fields
   */

  
  /*
   * Instance fields
   */
  
  String packageId = null;
  String pubDate = null;
  ArrayList<ResponsibleParty> creatorList = null;
  ArrayList<Entity> entityList = null;
  ArrayList<String> titles = null;

  
  /*
   * Constructors
   */
  
  
  /*
   * Initialize the array lists when constructing this DataPackage object.
   */
  
  DataPackage() {
    this.creatorList = new ArrayList<ResponsibleParty>();
    this.entityList = new ArrayList<Entity>();
    this.titles = new ArrayList<String>();
  }
  
  
  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  
  /**
   * Add a new creator to the creator list for this data package.
   * Filters out duplicates by checking to see whether the responsible
   * party is already in the list.
   */
  public void addCreator(ResponsibleParty responsibleParty) {
	  if ((responsibleParty != null) && (!hasCreator(responsibleParty))) {
		  creatorList.add(responsibleParty);
	  }
  }
  
  
  /**
   * Finds the matching object name for a given entity in this data package
   * based on the entity's name. This is a convenience method.
   * 
   * @param entityName    The entity name, e.g. "Data Entity One"
   * @return the matching object name, or null if no match was found
   */
  public String findObjectName(String entityName) {
    String objectName = null;
    
    for (Entity entity : getEntityList()) {
      String name = entity.getName();
      if ((name != null) && (name.equals(entityName))) {
        objectName = entity.getObjectName();
      }
    }
     
    return objectName;
  }
  
  
  /* Getters and Setter */
  
  public ArrayList<ResponsibleParty> getCreatorList() {
    return creatorList;
  }

  
  public ArrayList<Entity> getEntityList() {
    return entityList;
  }

  
  public String getPackageId() {
    return packageId;
  }
  
  
  public String getPubDate() {
    return pubDate;
  }
  
  
  public ArrayList<String> getTitles() {
    return titles;
  }
  
  
  private boolean hasCreator(ResponsibleParty responsibleParty) {
	  boolean hasCreator = false;
	  String responsiblePartyCreatorName = null;
	  
	  if (responsibleParty != null) {
		  responsiblePartyCreatorName = responsibleParty.getCreatorName();
	  
		  if (responsiblePartyCreatorName != null) {
			  for (ResponsibleParty rp : this.creatorList) {
				  String rpCreatorName = rp.getCreatorName();
				  if (responsiblePartyCreatorName.equalsIgnoreCase(rpCreatorName)) {
					  hasCreator = true;
					  break;
				  }
			  }
		  }
	  }
	  
	  return hasCreator;
  }
  
  
  	/**
  	 * Boolean method to determine whether this data package has at
  	 * least one data table entity within it. 
  	 * 
  	 * @return  true if at least one data table entity is found, else false
  	 */
	public boolean hasDataTableEntity() {
		boolean hasDataTable = false;

		for (Entity entity : getEntityList()) {
			if (entity.getEntityType() == EntityType.dataTable) {
				hasDataTable = true;
			}
		}

		return hasDataTable;
	}

  
  public void setPackageId(String packageId) {
    this.packageId = packageId;
  }
  
  
  public void setPubDate(String pubDate) {
    this.pubDate = pubDate;
  }
  
}
