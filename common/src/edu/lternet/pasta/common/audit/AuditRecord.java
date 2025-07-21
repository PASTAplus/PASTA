package edu.lternet.pasta.common.audit;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import edu.lternet.pasta.common.ISO8601Utility;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.edi.EdiToken;


/**
 * Class to represent a single entry in the audit log from the
 * client's perspective.
 * 
 * 
 * @author dcosta
 *
 */
public class AuditRecord {
  
  /* 
   * Class variables 
   */

  /* 
   * Instance variables 
   */
  
  private int oid;
  private String entryTime;
  private String category;
  private String service;
  private String serviceMethod;
  private int responseStatus;
  private String resourceId;
  private String user = "";
  private String userAgent;
  private String groups;
  private String authSystem;
  private String entryText;


    /*
   * Constructors 
   */
  
  /**
   * The default constructor is used when we are querying the
   * audit record from a database query as opposed to parsing it
   * from XML.
   */
  public AuditRecord() {
    
  }

  
  public AuditRecord(
      Date date,
      String service,
      String entryText,
      AuthToken authToken,
      String ediTokenStr,
      int httpStatusCode,
      String serviceMethod,
      String resourceId,
      String robot,
      String userAgent
  )
  {
    super();
    this.entryTime = ISO8601Utility.formatDateTime(date);
    this.category = categoryFromStatusCode(httpStatusCode);
    this.service = service;
    this.serviceMethod = serviceMethod;
    if (authToken != null) {
      this.user = authToken.getUserId();
      if (this.user != null && this.user.equals("public") && (robot != null)) {
    	  this.user = "robot";
      }
      Set<String> groupsSet = authToken.getGroups();
      this.groups = groupsSetToGroupsString(groupsSet);
      AuthSystemDef authSystemDef = authToken.getAuthSystem();
      this.authSystem = authSystemDef.getCanonicalName();
    }
    if (ediTokenStr != null) {
        EdiToken ediToken = new EdiToken(ediTokenStr);
        String subj = ediToken.getSubject();
        this.user += " (" + subj + ")";
    }
    this.responseStatus = httpStatusCode;
    this.resourceId = resourceId;
    this.entryText = entryText;
    this.userAgent = userAgent;
  }
  
  
  /**
   * Constructs an audit record object by parsing the XML string.
   * 
   * @param auditRecordXML      The audit entry XML string
   */
  public AuditRecord(String auditRecordXML) {
    this.entryTime = parseElement(auditRecordXML, "entryTime");
    this.category = parseElement(auditRecordXML, "category");
    this.service = parseElement(auditRecordXML, "service");
    this.serviceMethod = parseElement(auditRecordXML, "serviceMethod");
    this.user = parseElement(auditRecordXML, "user");
    this.userAgent = parseElement(auditRecordXML, "userAgent");
    this.groups = parseElement(auditRecordXML, "groups");
    this.authSystem = parseElement(auditRecordXML, "authSystem");
    String responseStatusStr = parseElement(auditRecordXML, "responseStatus");
    this.responseStatus = new Integer(responseStatusStr);
    this.resourceId = parseElement(auditRecordXML, "resourceId");
    this.entryText = parseElement(auditRecordXML, "entryText");
  }
  
  
  /* 
   * Class methods 
   */
  
  
  /* 
   * Instance methods 
   */
  
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
   * @return the timestamp string of this audit record.
   */
  public String getEntryTime() {
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
    return responseStatus;
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
   * Returns the user of this audit record.
   * @return the user of this audit record.
   */
  public String getUser() {
    return user;
  }

  
  /**
   * Returns the userAgent value of this audit record.
   * @return the userAgent value of this audit record.
   */
  public String getUserAgent() {
    return userAgent;
  }

  
  /* Setter methods */

  public void setAuthSystem(String s) {
    String authSystem = "";
    if (s != null && !s.equalsIgnoreCase("null")) {
      authSystem = s;
    }
    this.authSystem = authSystem;
  }

  
  public void setCategory(String category) {
    this.category = category;
  }

  
  public void setEntryText(String s) {
    String text = "";
    if (s != null && !s.equalsIgnoreCase("null")) {
      text = XmlUtility.xmlEncode(s);
    }
    this.entryText = text;
  }

  
  public void setEntryTime(Date date) {
    if (date != null) {
      this.entryTime = ISO8601Utility.formatDateTime(date);
    }
  }

  
  public void setGroups(String s) {
    String groups = "";
    if (s != null && !s.equalsIgnoreCase("null")) {
      groups = s;
    }
    this.groups = groups;
  }

  
  public void setOid(Integer i) {
    this.oid = i;
  }


  public void setResourceId(String s) {
    String resourceId = "";
    if (s != null && !s.equalsIgnoreCase("null")) {
      resourceId = s;
    }
    this.resourceId = resourceId;
  }

  
  public void setResponseStatus(Integer i) {
    if (i != null) responseStatus = i.intValue();
  }

  
  public void setService(String s) {
    this.service = s;
  }

  
  public void setServiceMethod(String s) {
    this.serviceMethod = s;
  }

  
  public void setUser(String s) {
    String user = "";
    if (s != null && !s.equalsIgnoreCase("null")) {
      user = s;
    }
    this.user = user;
  }

  
  public void setUserAgent(String s) {
	  String userAgent = "";
	  if (s != null && !s.equalsIgnoreCase("null")) {
	   userAgent = s;
	  }
	  this.userAgent = userAgent;
  }

	  
  private String categoryFromStatusCode(int statusCode) {
    String category = "error";
    
    if (statusCode < 400) {
      category = "info";
    }
    else if (statusCode < 500) {
      category = "warn";
    }
    
    return category;
  }
  
  
  private String getTimestamp() {
    String timestamp = "";
    
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(new Date());
    XMLGregorianCalendar date;
    try {
        date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        timestamp = date.toString();
    }
    catch (DatatypeConfigurationException e) {
        throw new IllegalStateException("Invalid date attempted to be set.");
    }

    
    return timestamp;
  }

  
  private String parseElement(String auditEntry, String elementName) {
    String elementText = "";
    
    String startTag = String.format("<%s>", elementName);
    String endTag = String.format("</%s>", elementName);
    int start = auditEntry.indexOf(startTag) + startTag.length();
    int end = auditEntry.indexOf(endTag);
    elementText = auditEntry.substring(start, end);
    
    return elementText;
  }
  
  
  /*
   * Returns the contents of this audit entry as an XML string.
   */
  public String toXML() {
    String xmlString = null;
    StringBuffer stringBuffer = new StringBuffer("");
    
    stringBuffer.append("<auditRecord>\n");
    stringBuffer.append(String.format("  <oid>%d</oid>\n", oid));
    stringBuffer.append(String.format("  <entryTime>%s</entryTime>\n", entryTime));
    stringBuffer.append(String.format("  <category>%s</category>\n", category));
    stringBuffer.append(String.format("  <service>%s</service>\n", service));
    stringBuffer.append(String.format("  <serviceMethod>%s</serviceMethod>\n", serviceMethod));
    stringBuffer.append(String.format("  <responseStatus>%d</responseStatus>\n", responseStatus));
    stringBuffer.append(String.format("  <resourceId>%s</resourceId>\n", resourceId));
    stringBuffer.append(String.format("  <user>%s</user>\n", user));
    stringBuffer.append(String.format("  <userAgent>%s</userAgent>\n", userAgent));
    stringBuffer.append(String.format("  <groups>%s</groups>\n", groups));
    stringBuffer.append(String.format("  <authSystem>%s</authSystem>\n", authSystem));
    stringBuffer.append(String.format("  <entryText>%s</entryText>\n", entryText));
    stringBuffer.append("</auditRecord>\n");
    
    xmlString = stringBuffer.toString();
    return xmlString;
  }
  
  
  private String groupsSetToGroupsString(Set<String> groupsSet) {
    String groups = "";
    StringBuffer stringBuffer = new StringBuffer("");
    
    for (String group : groupsSet) {
      stringBuffer.append(String.format("%s,", group));
    }

    groups = stringBuffer.toString();
    if (groups.endsWith(",")) { groups = groups.substring(0, groups.length() - 1); }
    return groups;
  }
  
}
