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
 * @author Duane Costa
 * @since Nov 9, 2012
 * 
 *        Bundles resource attribute information.
 * 
 */
public class Resource {

	/*
	 * Class variables
	 */
    
    private final String INDENT = "    ";

	/*
	 * Instance variables
	 */

	private String dataFormat = null;
    private String dateCreated = null;
    private String dateDeactivated = null;
    private String doi = null;
    private String entityId = null;
    private String entityName = null;
    private String fileName = null;
    private String formatType = null;
    private Integer identifier = null;
    private String md5Checksum = null;
    private String mimeType = null;
    private String packageId = null;
    private String principalOwner = null;
	private String resourceId = null;
	private String resourceLocation = null;
    private Long resourceSize = null;
	private String resourceType = null;
    private Integer revision = null;
	private String scope = null;
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
	
	
	/*
	 * Accessor methods 
	 */

    public String getDataFormat() { return this.dataFormat; }
    public String getDateCreated() { return this.dateCreated; }
    public String getDateDeactivated() { return this.dateDeactivated; }
    public String getDoi() { return this.doi; }
    public String getEntityId() { return this.entityId; }
    public String getEntityName() { return this.entityName; }
    public String getFileName() { return this.fileName; }
    public String getFormatType() { return this.formatType; }
    public Integer getIdentifier() { return this.identifier; }
    public String getMd5Checksum() { return this.md5Checksum; }
    public String getMimeType() { return this.mimeType; }
    public String getPackageId() { return this.packageId; }
    public String getPrincipalOwner() { return this.principalOwner; }
    public String getResourceId() { return this.resourceId; }
    public String getResourceLocation() { return this.resourceLocation; }
    public Long getResourceSize() { return this.resourceSize; }
    public String getResourceType() { return this.resourceType; }
    public Integer getRevision() { return this.revision; }
    public String getScope() { return this.scope; }
    public String getSha1Checksum() { return this.sha1Checksum; }

    public void setDataFormat(String dataFormat) { this.dataFormat = dataFormat; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
    public void setDateDeactivated(String dateDeactivated) { this.dateDeactivated = dateDeactivated; }
    public void setDoi(String doi) { this.doi = doi; }
    public void setEntityId(String entityId) { this.entityId = entityId; }  
    public void setEntityName(String entityName) { this.entityName = entityName; }  
    public void setFileName(String fileName) { this.fileName = fileName; }  
    public void setFormatType(String formatType) { this.formatType = formatType; }  
    public void setIdentifier(Integer identifier) { this.identifier = identifier; }
    public void setMd5Checksum(String checksum) { this.md5Checksum = checksum; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public void setPackageId(String packageId) { this.packageId = packageId; }
    public void setPrincipalOwner(String principalOwner) { this.principalOwner = principalOwner; }
	public void setResourceId(String resourceId) { this.resourceId = resourceId; }
	public void setResourceLocation(String resourceLocation) { this.resourceLocation = resourceLocation; }
    public void setResourceSize(Long size) { this.resourceSize = size;}
	public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public void setRevision(Integer revision) { this.revision = revision; }
	public void setScope(String scope) { this.scope = scope; }
	public void setSha1Checksum(String checksum) { this.sha1Checksum = checksum; }
	
	
	private void appendStringIfNonEmpty(StringBuffer sb, String elementName, String value) {
	    if (value != null && !value.isEmpty()) {
	        sb.append(String.format("%s<%s>%s</%s>\n", INDENT, elementName, value, elementName));
	    }
	}
	
	
    private void appendIntegerIfNonEmpty(StringBuffer sb, String elementName, Integer value) {
        if (value != null) {
            sb.append(String.format("%s<%s>%d</%s>\n", INDENT, elementName, value, elementName));
        }
    }
    
    private void appendLongIfNonEmpty(StringBuffer sb, String elementName, Long value) {
        if (value != null) {
            sb.append(String.format("%s<%s>%d</%s>\n", INDENT, elementName, value, elementName));
        }
    }
    

	public String toXML() {
	    StringBuffer xmlBuffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    xmlBuffer.append("<resourceMetadata>\n");    
	    appendStringIfNonEmpty(xmlBuffer, "dataFormat", this.dataFormat);
        appendStringIfNonEmpty(xmlBuffer, "dateCreated", this.dateCreated);
        appendStringIfNonEmpty(xmlBuffer, "dateDeactivated", this.dateDeactivated);
        appendStringIfNonEmpty(xmlBuffer, "doi", this.doi);
        appendStringIfNonEmpty(xmlBuffer, "entityId", this.entityId);
        appendStringIfNonEmpty(xmlBuffer, "entityName", this.entityName);
        appendStringIfNonEmpty(xmlBuffer, "fileName", this.fileName);
        appendStringIfNonEmpty(xmlBuffer, "formatType", this.formatType);
        appendIntegerIfNonEmpty(xmlBuffer, "identifier", this.identifier);
        appendStringIfNonEmpty(xmlBuffer, "md5Checksum", this.md5Checksum);
        appendStringIfNonEmpty(xmlBuffer, "mimeType", this.mimeType);
        appendStringIfNonEmpty(xmlBuffer, "packageId", this.packageId);
        appendStringIfNonEmpty(xmlBuffer, "principalOwner", this.principalOwner);
        appendStringIfNonEmpty(xmlBuffer, "resourceId", this.resourceId);
        appendStringIfNonEmpty(xmlBuffer, "resourceLocation", this.resourceLocation);
        appendLongIfNonEmpty(xmlBuffer, "resourceSize", this.resourceSize);
        appendStringIfNonEmpty(xmlBuffer, "resourceType", this.resourceType);
        appendIntegerIfNonEmpty(xmlBuffer, "revision", this.revision);
        appendStringIfNonEmpty(xmlBuffer, "scope", this.scope);
        appendStringIfNonEmpty(xmlBuffer, "sha1Checksum", this.sha1Checksum);
        xmlBuffer.append("</resourceMetadata>\n");
        
	    String xml = xmlBuffer.toString();
	    return xml;
	}

}
