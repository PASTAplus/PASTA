/*
 *
 * $Date: 2012-06-22 12:23:25 -0700 (Fri, 22 June 2012) $
 * $Author: dcosta $
 * $Revision: 2145 $
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

package edu.lternet.pasta.portal.search;


/**
 * The LTERSite class holds information about the LTER sites, including
 * information needed for administrative services such as DAS administration
 * and Metacat Harvester administration.
 */
public class LTERSite {
  
  /*
   * Class fields
   */
  
  public static final String[] sites = {
    "AND",
    "ARC",
    "BES",
    "BNZ",
    "CCE",
    "CDR",
    "CAP",
    "CWT",
    "FCE",
    "GCE",
    "HFR",
    "HBR",
    "JRN",
    "KBS",
    "KNZ",
    "NWK",
    "LUQ",
    "MCM",
    "MCR",
    "NWT",
    "NIN",
    "NTL",
    "PAL",
    "PIE",
    "SBC",
    "SEV",
    "SGS",
    "VCR",
};


public static final String[] siteNames = {
    "Andrews LTER",
    "Arctic LTER",
    "Baltimore Ecosystem Study",
    "Bonanza Creek LTER",
    "California Current Ecosystem",
    "Cedar Creek Ecosystem Science Reserve",
    "Central Arizona - Phoenix Urban LTER",
    "Coweeta LTER",
    "Florida Coastal Everglades LTER",
    "Georgia Coastal Ecosystems LTER",
    "Harvard Forest LTER",
    "Hubbard Brook LTER",
    "Jornada Basin LTER",
    "Kellogg Biological Station LTER",
    "Konza Prairie LTER",
    "LTER Network Office",
    "Luquillo LTER",
    "McMurdo Dry Valleys LTER",
    "Moorea Coral Reef LTER",
    "Niwot Ridge LTER",
    "North Inlet LTER",
    "North Temperate Lakes LTER",
    "Palmer Antarctica LTER",
    "Plum Island Ecosystems LTER",
    "Santa Barbara Coastal LTER",
    "Sevilleta LTER",
    "Shortgrass Steppe",
    "Virginia Coast Reserve LTER",
};

  
  /*
   * Instance fields
   */

  private String site;      // The three-letter uppercase acronym for this site
  private String siteName;  // The long name for this site
  
  
  /* 
   * Constructors
   */
   
  /* 
   * @param site     The three-letter acronym for this LTER site.
   */
  public LTERSite(final String site) {
    if (site != null) {
      this.site = site.toUpperCase();

      for (int i = 0; i < sites.length; i++) {
        String shortName = sites[i];
        String longName = siteNames[i];
        if (site.equalsIgnoreCase(shortName)) {
          this.siteName = longName;
        }
      }
    }
  }

  
  /*
   * Class methods
   */
  
  
  /**
   * Composes an HTML string, a list of options elements, one per site.
   * For use inside an HTML <select> element.
   * 
   * @param  indent      the indent string, a string of spaces, used to format
   *                     the list with the appropriate indent level
   * @param  includeNIN  boolean, if true, the NIN site is included in the list
   * @param  selectedSite a site string, e.g. "SGS", could be null. If a valid
   *                     site, the corresponding <option> will include the
   *                     'selected="selected"' attribute.
   * @return a string of HTML holding the options list
   */
  public static String composeHTMLOptions(final String indent,
                                          boolean includeNIN, 
                                          String selectedSite) {
    StringBuffer htmlStringBuffer = new StringBuffer("");
    
    for (int i = 0; i < sites.length; i++) {
      String site = sites[i];
      String siteName = siteNames[i];
      if (includeNIN || (!site.equals("NIN"))) {
        htmlStringBuffer.append(indent);
        htmlStringBuffer.append("<option value=\"");
        htmlStringBuffer.append(site);
        htmlStringBuffer.append("\"");
        if ((selectedSite != null) && (site.equalsIgnoreCase(selectedSite))) {
          htmlStringBuffer.append(" selected=\"selected\"");
        }
        htmlStringBuffer.append(">");
        htmlStringBuffer.append(siteName);
        htmlStringBuffer.append("</option>\n");
      }
    }
    
    return htmlStringBuffer.toString();
  }
  
  
  /**
   * Derives the site acronym from the docid value.
   * 
   * @param   docid       the docid value, e.g. "knb-lter-gce.1.1"
   * @return  site        the site acronym, e.g. "GCE". If no site string can be
   *                      parsed, returns an empty string, "".
   */
  public static String getSiteFromDocid(String docid) {
    String site = "";
    
    if (docid != null) {
      if (docid.startsWith("sev.")) {
        site = "SEV";
      }
      else if (docid.startsWith("ces_")) {
        site = "CAP";
      }
      else if (docid.startsWith("knb-lter-")) {
        String siteStr = docid.substring(9, 12);
        if (siteStr != null) { site = siteStr.toUpperCase(); }
      }
    }
    
    return site;
  }
  

  /**
   * Derives the site acronym from the packageId attribute value.
   * 
   * @param packageId     the packageId attribute value, e.g. "knb-lter-gce.1.1"
   * @return  site        the site acronym, e.g. "GCE"
   */
  public static String getSiteFromPackageId(String packageId) {
    String site = null;
    
    if (packageId != null) {
      if (packageId.startsWith("sev.")) {
        site = "SEV";
      }
      else if (packageId.startsWith("knb-lter-")) {
        String siteStr = packageId.substring(9, 12);
        if (siteStr != null) { site = siteStr.toUpperCase(); }
      }
    }
    
    return site;
  }
  

  /**
   * Given a docid, parse out the three-letter site acronym and convert to upper
   * case.
   * 
   * @param docid  A document id, e.g. "knb-lter-lno.1.1".
   * @return site  The three-letter site acronym, in upper case. If no LTER site
   *               is matched, returns "".
   */
  public static String parseSite(String docid) {
    String site = "";
    
    if (docid.startsWith("knb-lter-")) {
      site = docid.substring(9, 12).toUpperCase();
    } 
    else if (docid.startsWith("sev.")) {
      site = "SEV";
    }
    else {
      site = "";
    }
    
    return site;
  }


  /*
   * Instance methods
   */
  
  /**
   * For a given site, return the docid prefix search string for that
   * site's EML documents.
   * 
   * @return prefix      The first few letters that uniquely identify a site's
   *                     docid. Typically "knb-lter-xyz".
   */
  public String getDocidPrefix() {
    final String prefix;

    if (site == null) {
      prefix = "";
    }
    else {
      prefix = "knb-lter-" + site.toLowerCase();
    }
    
    return prefix;
  }
  
  
  /**
   * For a given site, return the packageId attribute search string for that
   * site's EML documents.
   * 
   * @return packageId   The first few letters that uniquely identify a site's
   *                     packageId. Typically "knb-lter-xyz", though there are
   *                     some exceptions.
   */
  public String getPackageId() {
    final String packageId;

    if (site == null) {
      packageId = "";
    }
    else {
      packageId = "knb-lter-" + site.toLowerCase();
    }
    
    return packageId;
  }
  
  
  /**
   * Gets the site value
   * 
   * @return                   the site acronym, e.g. "GCE"
   */
  public String getSite() {
    return this.site;
  }
  
  
  /**
   * Gets the siteName value
   * 
   * @return     the siteName string, e.g. "Georgia Coastal Ecosystems"
   */
  public String getSiteName() {
    return this.siteName;
  }
  
  
  /**
   * For a given site, return system attribute search string for that
   * site's EML documents.
   * 
   * @return system  A string representing the system attribute used in a LTER
   *                 site's EML documents.
   */
  public String getSystem() {
    final String system;
    
    if (site == null) {
      system = "";
    }
    else {
      system = "knb";
    }
    
    return system;
  }
  

  /**
   * Boolean to determine whether a given string is a valid LTER site.
   */
  public boolean isValidSite() {
    boolean isValid = false;
    
    if (site != null) {   
      for (int i = 0; i < sites.length; i++) {
        if (site.equals(sites[i])) { 
          isValid = true;
          break;
        }
      }
    }
    
    return isValid;
  }

}
