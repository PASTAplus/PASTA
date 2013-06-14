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
 * @since Nov 9, 2012
 * 
 *        Bundles resource attribute information.
 * 
 */
public class Resource {

	/*
	 * Class variables
	 */

	/*
	 * Instance variables
	 */

	private String resourceId = null;
	private String resourceLocation = null;
	private String resourceType = null;
	private String packageId = null;
	private String dateCreated = null;
	private String scope = null;
	private Integer identifier = null;
	private Integer revision = null;
	private String entityId = null;
	private String sha1Checksum = null;

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
	 * Set resource identifier.
	 * 
	 * @param resourceId
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	/**
	 * Set resource location.
	 * 
	 * @param resourceLocation
	 */
	public void setResourceLocation(String resourceLocation) {
		this.resourceLocation = resourceLocation;
	}
	
	/**
	 * Set resource type.
	 * 
	 * @param resourceType
	 */
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	/**
	 * Set package identifier.
	 * 
	 * @param packageId
	 */
	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}
	
	/**
	 * Set date created.
	 * 
	 * @param dateCreated
	 */
	public void setDateCreate(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * Get resource identifier.
	 * 
	 * @return Resource identifier
	 */
	public String getResourceId() {
		return this.resourceId;
	}
	
	/**
	 * Get resource type.
	 * 
	 * @return Resource type
	 */
	public String getResourceLocation() {
		return this.resourceLocation;
	}
	
	/**
	 * Get resource type.
	 * 
	 * @return Resource type
	 */
	public String getResourceType() {
		return this.resourceType;
	}
	
	/**
	 * Get package identifier.
	 * 
	 * @return Package identifier
	 */
	public String getPackageId() {
		return this.packageId;
	}
	
	/**
	 * Get date created.
	 * 
	 * @return Date created
	 */
	public String getDateCreated() {
		return this.dateCreated;
	}
	
	
	/*
	 * Additional accessor methods for scope, identifier, revision, and entityId
	 */
	
	public String getScope() {
		return scope;
	}

	
	public void setScope(String scope) {
		this.scope = scope;
	}

	
	public Integer getIdentifier() {
		return identifier;
	}

	
	public void setIdentifier(Integer identifier) {
		this.identifier = identifier;
	}

	
	public Integer getRevision() {
		return revision;
	}

	
	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	
	public String getEntityId() {
		return entityId;
	}

	
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
		
	public String getSha1Checksum() {
		return sha1Checksum;
	}

	
	public void setSha1Checksum(String checksum) {
		this.sha1Checksum = checksum;
	}
		
}
