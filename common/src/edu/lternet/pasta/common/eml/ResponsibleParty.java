package edu.lternet.pasta.common.eml;

import java.util.ArrayList;

import edu.lternet.pasta.common.XmlUtility;

public class ResponsibleParty {
  
  /*
   * Class fields
   */

  
  /*
   * Instance fields
   */
  
  private String elementType;        // "contact", "creator", "metadataProvider"
  
  private String salutation = "";
  private ArrayList<String> givenNames = null;
  private String surName = null;
  private String organizationName = null;
  private String positionName = null;
  
  private ArrayList<String> deliveryPoints;
  private String city = "";
  private String administrativeArea = "";
  private String postalCode = "";
  private String country = "";
  
  private String phone = "";
  private String phoneType = "";
  private String electronicMailAddress = "";
  private String onlineUrl = "";

  
  /*
   * Constructors
   */

  public ResponsibleParty (String elementType) {
    deliveryPoints = new ArrayList<String>();
    givenNames = new ArrayList<String>();
    this.elementType = elementType;
  }

  
  /*
   * Class methods
   */

  
  /*
   * Instance methods
   */
  
  /**
   * Adds a deliveryPoint to the list of delivery points.
   * 
   * @param   deliveryPoint  the deliveryPoint string to add
   */
  public void addDeliveryPoint(String deliveryPoint) {
    this.deliveryPoints.add(deliveryPoint);
  }
  
  
  /**
   * Adds a given name to the list of given names.
   * 
   * @param   givenName  the givenName string to add
   */
  public void addGivenName(String givenName) {
    this.givenNames.add(XmlUtility.xmlEncode(givenName));
  }
  
  
  /**
   * Composes an address string from the information in the address element.
   * 
   * @return           the composed name string
   */
  public String composeAddress(String indent) {
    StringBuffer addressBuffer = new StringBuffer("");
    
    for (String deliveryPoint : deliveryPoints) {
      addressBuffer.append(deliveryPoint);
      addressBuffer.append("\n" + indent);
    }
    
    addressBuffer.append(city);
    
    if ((administrativeArea != null) && !administrativeArea.equals("")) {
      if ((city != null) && !city.equals("")) {
        addressBuffer.append(", ");
      }
      addressBuffer.append(administrativeArea);
    }
    if ((postalCode != null) && !postalCode.equals("")) {
      addressBuffer.append("\n" + indent);
      addressBuffer.append(postalCode);
    }
    if ((country != null) && !country.equals("")) {
      addressBuffer.append("\n" + indent);
      addressBuffer.append(country);
    }
    
    return addressBuffer.toString().trim();
  }
  
  
  /**
   * Composes a name from the salutation, givenName, and surName values.
   * 
   * @return    name     the composed name string
   */
  public String composeName() {
    StringBuffer nameBuffer = new StringBuffer("");
    
    nameBuffer.append(salutation);
    nameBuffer.append(" ");
    
    for (String givenName : givenNames) {
      nameBuffer.append(givenName);
      nameBuffer.append(" ");
    }
    
    nameBuffer.append(surName);
    
    return nameBuffer.toString().trim();
  }
  
  
  /*
   * Getters and setters
   */

  
  public String getElementType() {
    return elementType;
  }
  
  
  public String getGivenName() {
    StringBuffer givenNameBuffer = new StringBuffer("");
    
    for (String givenName : givenNames) {
      givenNameBuffer.append(String.format(" %s", givenName));
    }
    
    return givenNameBuffer.toString().trim();
  }


  public String getGivenInitials() {
	    StringBuffer sb = new StringBuffer("");
	    
	    for (String givenName : givenNames) {
	    	String firstInitial = firstInitial(givenName);
	    	sb.append(String.format(" %s", firstInitial));
	    }
	    
	    return sb.toString().trim();
	  }

  
  /*
   * Shorten a name to just its first initial followed by a period.
   */
  private String firstInitial(String name) {
	  String firstInitial = name;
	  
	  if (name != null && name.length() > 0) {
		  firstInitial = String.format("%c.", name.charAt(0));
	  }
	  
	  return firstInitial;
  }

  
  /**
   * Get individual name.
   * 
   * @param  useFullGivenName  if true, use the full given name, otherwise
   *                           use the first initials of the given name
   * @param  lastNameFirst     if true, position last name ahead of given name
   * 
   * @return individualName Individual name.
   */
  public String getIndividualName(boolean useFullGivenName, boolean lastNameFirst) {    
	String individualName = "";
    String surName = this.surName;
    String givenName = useFullGivenName ? getGivenName() : getGivenInitials();
    
    if ((givenName != null) && (!givenName.equals(""))) {
    	if (useFullGivenName) {
    		if (lastNameFirst) {
    			individualName = String.format("%s, %s", surName, givenName);
    		}
    		else {
    			individualName = String.format("%s %s", givenName, surName);
    		}
    	}
    	else {
    		if (lastNameFirst) {
    			individualName = String.format("%s %s", surName, givenName);
    		}
    		else {
    			individualName = String.format("%s %s", givenName, surName);
    		}
    	}
    }
    else {
    	individualName = surName;
    }
    
    return individualName;
  }
  
  
  public String getSalutation() {
    return salutation;
  }


  public String getSurName() {
    return surName;
  }


  public String getOrganizationName() {
    return organizationName;
  }


  public String getPositionName() {
    return positionName;
  }


  public ArrayList<String> getDeliveryPoints() {
    return deliveryPoints;
  }


  public String getCity() {
    return city;
  }


  public String getAdministrativeArea() {
    return administrativeArea;
  }


  public String getPostalCode() {
    return postalCode;
  }


  public String getCountry() {
    return country;
  }


  /**
   * Gets creator name.
   * 
   * @return Creator name.
   */
  public String getCreatorName() {
    String creatorName = null;

    if (isPerson()) {
      creatorName = this.surName + ", " + getGivenName();
    } 
    else if (isOrganization()) {
      creatorName = this.organizationName;
    } 
    else {
      creatorName = this.positionName;
    }

    return creatorName;
  }

  
  public String getPhone() {
    return phone;
  }


  public String getPhoneType() {
    return phoneType;
  }


  public String getElectronicMailAddress() {
    return electronicMailAddress;
  }


  public String getOnlineUrl() {
    return onlineUrl;
  }


  /**
   * Determine whether this responsible party has an organization, even
   * if it is not exclusively an organization element (for example, a creator element
   * with both an <code><individualName></code> and an <code><organizationName></code>
   * element contained within it). Note how this differs from the
   * <code>isOrganization()</code> method.
   * 
   * @return   true if the responsible party has an organization, else false
   */
  public boolean hasOrganization() {
	    return (organizationName != null && !organizationName.equals(""));
  }


  public boolean isOrganization() {
    return (!isPerson() && organizationName != null && !organizationName.equals(""));
  }


  public boolean isPerson() {
    return (surName != null && !surName.equals(""));
  }


  public void setElementType(String elementType) {
    this.elementType = elementType;
  }


  public void setSalutation(String salutation) {
    this.salutation = salutation;
  }


  public void setSurName(String surName) {
    this.surName = XmlUtility.xmlEncode(surName);
  }


  public void setOrganizationName(String organizationName) {
    this.organizationName = XmlUtility.xmlEncode(organizationName);
  }


  public void setPositionName(String positionName) {
    this.positionName = XmlUtility.xmlEncode(positionName);
  }


  public void setDeliveryPoints(ArrayList<String> deliveryPoints) {
    this.deliveryPoints = deliveryPoints;
  }


  public void setCity(String city) {
    this.city = city;
  }


  public void setAdministrativeArea(String administrativeArea) {
    this.administrativeArea = administrativeArea;
  }


  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }


  public void setCountry(String country) {
    this.country = country;
  }


  public void setPhone(String phone) {
    this.phone = phone;
  }


  public void setPhoneType(String phoneType) {
    this.phoneType = phoneType;
  }


  public void setElectronicMailAddress(String electronicMailAddress) {
    this.electronicMailAddress = electronicMailAddress;
  }


  public void setOnlineUrl(String onlineUrl) {
    this.onlineUrl = onlineUrl;
  }
  
}
