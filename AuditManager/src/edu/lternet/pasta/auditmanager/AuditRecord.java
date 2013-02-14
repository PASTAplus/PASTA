/*
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

package edu.lternet.pasta.auditmanager;


import java.util.Date;

import org.apache.log4j.Logger;
import org.owasp.esapi.codecs.XMLEntityCodec;


/**
 * This class is used to represent an individual audit record from 
 * the audit manager's 'eventlog' table.
 */
public class AuditRecord {
  
  /* Class variables */
  
  private static Logger logger = Logger.getLogger(AuditRecord.class);
  

  /* Instance variables */
  
  private String authSystem;
  private String category;
  private String entryText;
  private Date entryTime;
  private String groups;
  private int oid;
  private String resourceId;
  private String service;
  private String serviceMethod;
  private Integer statusCode;
  private String userId;

  
  /* Constructors */
  
  
  /* Class methods */
  
  
  /* Instance methods */
  
  /**
   * Returns the auth system of this audit record.
   * @return the auth system of this audit record.
   */
  public String getAuthSystem() {
    return authSystem;
  }


  /**
   * Returns the category of this audit record.
   * @return the category of this audit record.
   */
  public String getCategory() {
    return category;
  }


  /**
   * Returns the entry text of this audit record.
   * @return the entry text of this audit record.
   */
  public String getEntryText() {
    return entryText;
  }


  /**
   * Returns the entry time of this audit record.
   * @return the Date of this audit record.
   */
  public Date getEntryTime() {
    return entryTime;
  }


  /**
   * Returns the groups of this audit record.
   * @return the groups of this audit record.
   */
  public String getGroups() {
    return groups;
  }


  /**
   * Returns the OID of this audit record.
   * @return the OID of this audit record.
   */
  public Integer getOid() {
    return oid;
  }


  /**
   * Returns the resource ID of this audit record.
   * @return the resource ID of this audit record.
   */
  public String getResourceId() {
    return resourceId;
  }


  /**
   * Returns the response status of this audit record.
   * @return the response status of this audit record.
   */
  public Integer getResponseStatus() {
    return statusCode;
  }


  /**
   * Returns the originating service of this audit record.
   * @return the originating service of this audit record.
   */
  public String getService() {
    return service;
  }


/**
  * Returns the service method of this audit record.
  * @return the service method of this audit record.
  */
  public String getServiceMethod() {
    return serviceMethod;
  }


  /**
   * Returns the status code of this audit record.
   * @return the status code of this audit record.
   */
  public Integer getStatusCode() {
    return statusCode;
  }


  /**
   * Returns the user of this audit record.
   * @return the user of this audit record.
   */
  public String getUser() {
    return userId;
  }

  
  /* Setter methods */

  public void setAuthSystem(String authSystem) {
    this.authSystem = authSystem;
  }

  
  public void setCategory(String category) {
    this.category = category;
  }

  
  public void setEntryText(String s) {
    this.entryText = s;
  }

  
  public void setEntryTime(Date date) {
    this.entryTime = date;
  }

  
  public void setGroups(String s) {
    this.groups = s;
  }

  
  public void setOid(Integer i) {
    this.oid = i;
  }


  public void setResourceId(String s) {
    this.resourceId = s;
  }

  
  public void setService(String s) {
    this.service = s;
  }

  
  public void setServiceMethod(String s) {
    this.serviceMethod = s;
  }

  
  public void setStatusCode(Integer i) {
    if (i != null) statusCode = i.intValue();
  }

  
  public void setUserId(String s) {
    this.userId = s;
  }

  
  /**
   * Returns a string representation of this subscription.
   * @return a string representation of this subscription.
   */
  public String toString() {
    return String.format(
      "oid=%d,entryTime=%s,service=%s,category=%s,serviceMethod=%s,entryText=%s,resourceId=%s,statusCode=%d,user=%s,groups=%s,authSystem=%s", 
      oid, entryTime, service, category, serviceMethod, entryText, resourceId, statusCode, userId, groups, authSystem
    );
  }
  
  
  public String toXML() {
    String xmlString = null;
    StringBuffer sb = new StringBuffer("");
    sb.append("  <auditRecord>\n");
    sb.append("    <oid>" + getOid().toString() + "</oid>\n");
    sb.append("    <entryTime>" + ISO8601Utilities.formatDateTime(getEntryTime()) + "</entryTime>\n");
    sb.append("    <category>" + getCategory() + "</category>\n");
    sb.append("    <service>" + getService() + "</service>\n");
    sb.append("    <serviceMethod>" + getServiceMethod() + "</serviceMethod>\n");
    sb.append("    <responseStatus>" + getResponseStatus() + "</responseStatus>\n");
    sb.append("    <resourceId>" + getResourceId() + "</resourceId>\n");
    sb.append("    <user>" + getUser() + "</user>\n");
    sb.append("    <groups>" + getGroups() + "</groups>\n");
    sb.append("    <authSystem>" + getAuthSystem() + "</authSystem>\n");
    sb.append("    <entryText>" + xmlEncode(getEntryText()) + "</entryText>\n");
    sb.append("  </auditRecord>\n");
    
    xmlString = sb.toString();
    return xmlString;
  }


  private String xmlEncode(String rawXml) {
    String encodedXml = null;

    if (rawXml == null) {
      encodedXml = "";
    }
    else {
      // Encodings for XML
      XMLEntityCodec xmlEntityCodec = new XMLEntityCodec();
      char[] immune = new char[0];
      StringBuffer xml = new StringBuffer();

      for (int a = 0; a < rawXml.length(); a++) {
        xml.append(xmlEntityCodec.encodeCharacter(immune, rawXml.charAt(a)));
      }
      
      encodedXml = xml.toString();
    }

    return encodedXml;
  }

}
