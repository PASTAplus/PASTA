/*
 *
 * $Date$
 * $Author$
 * $Revision$
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

package edu.lternet.pasta.common;


/**
 * 
 * @author Duane Costa
 * 
 * The DataPackageUpload class holds the content of an individual data
 * package upload record. It has been expanded to also represent data
 * package delete records.
 *
 */
public class DataPackageUpload implements Comparable<DataPackageUpload> {
	
	/*
	 *  Class fields
	 */
	
	
	/*
	 *  Instance fields	
	 */
	
	protected Integer identifier;
	protected Integer revision;
	protected String packageId;
	protected String scope;
	protected String serviceMethod;
	protected String uploadDate;
	
	
	/*
	 *  Constructors
	 */
	
	/**
	 * This alternate constructor is used by the UpdateStats class.
	 * Development of the UpdateStats class was motivated by a need to avoid 
	 * querying the Audit Manager due to severe performance problems.
	 * 
	 * @param dpmClient      the DataPackageManager client
	 * @param uploadDate     the upload date
	 * @param serviceMethod  one of "createDataPackage" or 
	 *                              "updateDataPackage" or
	 *                              "deleteDataPackage"
	 * @param scope          the data package scope value
	 * @param identifier     the data package identifier value
	 * @param revision       the data package revision value
	 */
	public DataPackageUpload(String uploadDate, String serviceMethod, 
			            String scope, Integer identifier, Integer revision) {
		this.uploadDate = uploadDate;
		this.serviceMethod = serviceMethod;
		this.scope = scope;
		this.identifier = identifier;
		this.revision = revision;
		this.packageId = String.format("%s.%d.%d", this.scope, this.identifier, this.revision);
	}
	
	
	/*
	 * Class methods
	 */
	
	
	/*
	 * Instance methods
	 */
	
	public String getUploadDate() {
		return uploadDate;
	}


	public String getServiceMethod() {
		return serviceMethod;
	}


	public String getPackageId() {
		return packageId;
	}
	
	
	public String getScope() {
		return scope;
	}
	
	
	public Integer getIdentifier() {
		return identifier;
	}
	
	
	public Integer getRevision() {
		return revision;
	}
	
	
	/*
	 * Returns the contents of this DataPackageUpload object as an XML string.
	 */
	public String toXML() {
		String xmlString = null;
		StringBuffer stringBuffer = new StringBuffer("");
		String dataPackageElementName = "dataPackage";
		
		stringBuffer.append(String.format("  <%s>\n", dataPackageElementName));
		stringBuffer.append(String.format("    <packageId>%s</packageId>\n", packageId));
		stringBuffer.append(String.format("    <scope>%s</scope>\n", scope));
		stringBuffer.append(String.format("    <identifier>%d</identifier>\n", identifier));
		stringBuffer.append(String.format("    <revision>%d</revision>\n", revision));
		stringBuffer.append(String.format("    <serviceMethod>%s</serviceMethod>\n", serviceMethod));
		stringBuffer.append(String.format("    <date>%s</date>\n", uploadDate));
		stringBuffer.append(String.format("  </%s>\n", dataPackageElementName));

		xmlString = stringBuffer.toString();
		return xmlString;
	}


	/**
	 * Allows sorting of DataPackageUpload objects.
	 */
	@Override
	public int compareTo(DataPackageUpload dpu) {
		/*
		 * If date strings are the same, order by revision ascending
		 */
		if (this.uploadDate.equals(dpu.uploadDate)) {
			return this.revision.compareTo(dpu.revision);
		}
		/*
		 * but most of the time we'll be comparing the date strings
		 */
		else {
			return this.uploadDate.compareTo(dpu.uploadDate);
		}
	}
		  
}
