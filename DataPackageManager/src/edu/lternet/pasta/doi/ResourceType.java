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
 *        Manages DataCite resource type metadata.
 * 
 */
public class ResourceType {

	enum ResourceTypeGeneral {
		Collection, Dataset, Event, Film, Image, InteractiveResource, Model,
		PhysicalObject, Service, Software, Sound, Text
	}

	/*
	 * Class variables
	 */
	
	public static final String COLLECTION = "Collection";
	public static final String DATASET	= "Dataset";
	public static final String EVENT = "Event";
	public static final String FILM = "Film";
	public static final String IMAGE = "Image";
	public static final String INTERACTIVERESOURCE = "InteractiveResource";
	public static final String MODEL = "Model";
	public static final String PHYSICALOBJECT = "PhysicalObject";
	public static final String SERVICE = "Service";
	public static final String SOFTWARE = "Software";
	public static final String SOUND = "Sound";
	public static final String TEXT = "Text";

	/*
	 * Instance variables
	 */

	private String resourceType = null;
	private ResourceTypeGeneral resourceTypeGeneral = null;

	/*
	 * Constructors
	 */

	/**
	 * Create new ResourceType object with required resource type general
	 * attribute.
	 * 
	 * @param resourceTypeGeneral
	 */
	public ResourceType(String resourceTypeGeneral) {

		this.resourceTypeGeneral = (ResourceTypeGeneral) Enum.valueOf(
		    ResourceTypeGeneral.class, resourceTypeGeneral);

	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

	/**
	 * Set the specific DataCite resource type.
	 * 
	 * @param resourceType
	 */
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	/**
	 * Get the specific DataCite resource type.
	 * 
	 * @return Specific DataCite resource type.
	 */
	public String getResourceType() {
		return this.resourceType;
	}

	/**
	 * Get the general class of the DataCite resource type.
	 * 
	 * @return General class of the DataCite resource type
	 */
	public String getResourceTypeGeneral() {
		return this.resourceTypeGeneral.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
