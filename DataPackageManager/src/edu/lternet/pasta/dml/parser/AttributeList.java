/**
 *    '$RCSfile: AttributeList.java,v $'
 *
 *     '$Author: leinfelder $'
 *       '$Date: 2008-08-11 18:27:05 $'
 *   '$Revision: 1.5 $'
 *
 *  For Details: http://kepler.ecoinformatics.org
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package edu.lternet.pasta.dml.parser;

import java.util.Vector;


/**
 * @author tao
 *
 * This class reprents of list of attributes in the entity object
 */
public class AttributeList
{
  /*
   * Instance Fields
   */
   private Vector<Attribute> attributes   = new Vector<Attribute>();
   private String id           = null;
   private boolean isReference = false;
   private String referenceId  = null;
   private Entity parentTable  = null;
   
   
  /*
   * Constructors
   */
   
  /**
   * Constructs an AttributeList object
   */
  public AttributeList() {
    attributes = new Vector<Attribute>();
  }
   
  
  /*
   * Instance methods
   */
   
  /**
   * Gets the attribute field
   * 
   * @return   an array of Attribute objects, or null if there are no
   *           attributes in the list
   */
  public Attribute[] getAttributes() {
    if (attributes == null || attributes.size() == 0) {
      return null;
    } 
    else {
      int size = attributes.size();
      Attribute[] list = new Attribute[size];
      
      for (int i = 0; i < size; i++) {
        list[i] = (Attribute) attributes.elementAt(i);
      }
      
      return list;
    }
  }
  
  
  /**
   * Pretty prints the attribute list.
   * 
   * @return   a pretty-print string for the attribute list
   */
  public String prettyPrintAttributes() {
      String prettyPrintStr = "";
      Attribute[] attributeList = getAttributes();
      StringBuffer sb = new StringBuffer("");
      
      if (attributeList != null) {
          int i = 1;
          for (Attribute attribute : attributeList) {
              String name = attribute.getName();
              sb.append(name);
              if (i < attributeList.length) { sb.append(","); }
              i++;
          }
          prettyPrintStr = sb.toString();
      }
      
      return prettyPrintStr;
  }

  
  
  /**
   * @param attributes   The attributes to set.
   */
  /*
   * public void setAttributes(Vector attributes) { 
   *   this.attributes = attributes; 
   * }
   */
  
  
  /**
   * Gets the database field names for the attributes in this AttributeList.
   * 
   * @return   an array of Strings objects, or null if there are no
   *           attributes in the list. 
   */
  public String[] getDBFieldNames() {
    if (attributes == null || attributes.size() == 0) {
      return null;
    } 
    else {
      int size = attributes.size();
      String[] list = new String[size];
      
      for (int i = 0; i < size; i++) {
        Attribute attribute = (Attribute) attributes.elementAt(i);
        list[i] = attribute.getDBFieldName();
      }
      
      return list;
    }
  }
  
  
  /**
   * Gets the id.
   * 
   * @return  a string representing the id
   */
  public String getId() {
    return id;
  }
    
    
  /**
   * Gets the names for the attributes in this AttributeList.
   * 
   * @return   an array of Strings objects, or null if there are no
   *           attributes in the list. 
   */
  public String[] getNames() {
    if (attributes == null || attributes.size() == 0) {
      return null;
    } 
    else {
      int size = attributes.size();
      String[] list = new String[size];
      
      for (int i = 0; i < size; i++) {
        Attribute attribute = (Attribute) attributes.elementAt(i);
        list[i] = attribute.getName();
      }
      
      return list;
    }
  }
  
  
  /**
   * Sets the id
   * 
   * @param id  the id to set.
   */
  public void setId(String id) {
    this.id = id;
  }
    
    
  /**
   * Gets the isReference field
   * 
   * @return a boolean, the value of the isReference field
   */
  public boolean isReference() {
    return isReference;
  }
    
    
  /**
   * Sets the isReference field.
   * 
   * @param isReference  The isReference value to set, a boolean
   */
  public void setReference(boolean isReference) {
    this.isReference = isReference;
  }
    
    
  
  /**
   * Gets the referenceId field.
   * 
   * @return  a string representing the referenceId
   */
  public String getReferenceId() {
    return referenceId;
  }
    

  /**
   * Sets the referenceId field.
   * 
   * @param referenceId   a string representing the referenceId value to set
   */
  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }
    
    
  /**
   * Sets parentTable entity for this AttributeList.
   * 
   * @param  entity    the parent Entity for this attribute list.
   */
  public void setParent(Entity entity) {
    parentTable = entity;
  }
    
    
  /**
   * Gets the parent entity for this AtttributeList.
   * 
   * @return  an Entity, the parent entity for this attribute list
   */
  public Entity getParent() {
    return parentTable;
  }
    
    
  /**
   * Adds an Attribute to this attribute list.
   * 
   * @param  a  the Attribute to be added to the 'attributes' field
   */
  public void add(Attribute a) {
    attributes.addElement(a);
  }
  
  /**
   * Look up the Attribute(s) by given name
   * @param name of the Attribute(s) to return
   * @return array of Attribute(s) that match the name parameter
   */
  public Attribute[] getAttributes(String name) {
	  if (attributes == null || attributes.size() == 0) {
	      return null;
	  } 
	  else {
		  
	      Vector<Attribute> list = new Vector<Attribute>();
		  for (int i = 0; i < attributes.size(); i++) {
			  Attribute a = (Attribute) attributes.elementAt(i);
			  if (a.getName().equals(name)) {
				  list.add(a);
			  }
		  }
		  return (Attribute[]) list.toArray(new Attribute[0]);
	  }
  }
  
  /**
   * Look up only the _first_ Attribute matching the name parameter
   * @param name Attribute name to match (first) on
   * @return first Attribute matching the name parameter
   */
  public Attribute getAttribute(String name) {
	  Attribute[] list = getAttributes(name);
	  if (list != null && list.length > 0) {
		  return list[0];
	  }
	  return null;
  }
  
}
