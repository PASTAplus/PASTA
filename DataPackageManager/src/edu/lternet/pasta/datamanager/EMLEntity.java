/**
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
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

package edu.lternet.pasta.datamanager;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import edu.lternet.pasta.dml.parser.Entity;

import edu.lternet.pasta.common.EmlPackageId;


/**
 * Parent class of EMLDatabaseEntity and EMLFileSystemEntity
 * subclasses. Declares instance fields and implements some of the 
 * basic access methods to them.
 * 
 * @author dcosta
 * @created 18-Nov-2010 4:30:03 PM
 */
public class EMLEntity {

  /*
   * Class fields
   */
  

  /*
   * Instance fields
   */
  
  private Logger logger = Logger.getLogger(EMLEntity.class);
  
  private EmlPackageId emlPackageId;
  private Entity entity = null;
  protected String entityId = "";
  private String entityName;
  private String dataFormat = "";
  protected String packageId;
  private String url = "";
  
  
  /*
   * Constructors
   */
  
  
  /**
   * Explicit default constructor is defined for the subclasses of EMLEntity.
   */
  EMLEntity() {
    
  }
    

  /**
   * Constructs an EMLEntity object with a specified
   * 'edu.lternet.pasta.dml.parser.Entity' object. EMLEntity acts as a
   * wrapper that encapsulates its Data Manager Library entity object.
   * 
   * @param entity  An entity object as defined by the Data Manager Library.
   */
  public EMLEntity(Entity entity) 
          throws MalformedURLException, UnsupportedEncodingException {
    this.entity = entity;
    this.dataFormat = entity.getDataFormat();
    this.entityName = entity.getName();
    
    if (entityName != null) { 
        entityId = edu.lternet.pasta.common.eml.Entity.entityIdFromEntityName(entityName);
    }

    this.packageId = entity.getPackageId();
    this.url = entity.getURL();
    
    try {
      URL aURL = new URL(url);
      if (aURL != null) {
        logger.debug("Constructed URL object: " + aURL.toString());
      }
    }
		catch (MalformedURLException e) {
			String message = null;
			if (url == null || url.equals("")) {
				message = 
			        String.format("Error when attempting to process entity \"%s\". " +
                                  "All data entities in PASTA must specify an online URL. " +
								  "No online URL value was found at this XPath: " +
                                  "\"dataset/[entity-type]/physical/distribution/online/url\". " +
                                  "(PASTA will use only the first occurrence of this XPath.)",
						          entityName);
			}
			else {
				message = 
				    String.format("Error when attempting to process entity \"%s\" with entity URL \"%s\": %s",
								  entityName, url, e.getMessage());
			}
			logger.error(message);
			throw new MalformedURLException(message);
		}
  }
  

  /*
   * Class methods
   */
  

  /*
   * Instance methods
   */
  
  /* Access methods */
  
  /**
   * Gets the package id object for this entity.
   * 
   * @return  the package id object
   */
  public EmlPackageId getEmlPackageId() {
    return emlPackageId;
  }

  
  /**
   * Gets the Data Manager Library entity object that this object encapsulates.
   * 
   * @return  an Entity object
   */
  public Entity getEntity() {
    return entity;
  }

  
  /**
   * Get the entity identifier for this entity.
   * 
   * @return   the entityId string
   */
  public String getEntityId() {
    return entityId;
  }

  
  /**
   * Gets the entity name string for this entity.
   * 
   * @return  the entity name string
   */
  public String getEntityName() {
    return entityName;
  }

  
  /**
   * Gets the dataFormat value for this entity.
   * 
   * @return  the dataFormat string value
   */
  public String getDataFormat() {
    return dataFormat;
  }

  
  /**
   * Get the packageId string for this entity.
   * 
   * @return  the packageId string
   */
  public String getPackageId() {
    return packageId;
  }

  
  /**
   * Gets the download URL for this entity
   * 
   * @return   the URL string
   */
  public String getUrl() {
    return url;
  }
 
}