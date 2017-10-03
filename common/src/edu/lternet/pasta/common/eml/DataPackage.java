/*
 *
 * $Date: 2012-04-02 11:10:19 -0700 (Mon, 02 Apr 2012) $
 * $Author: dcosta $
 * $Revision: $
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

package edu.lternet.pasta.common.eml;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import edu.lternet.pasta.common.eml.Entity.EntityType;


/**
 * DataPackage holds metadata values about a data package that were parsed 
 * from an EML document.
 *  
 * @author dcosta
 *
 */
public class DataPackage {
  
  /*
   * Class fields
   */

  
  /*
   * Instance fields
   */
  
  String packageId = null;
  ArrayList<ResponsibleParty> creatorList = null;
  ArrayList<Entity> entityList = null;
  ArrayList<String> keywords = null;
  ArrayList<TemporalCoverage> temporalCoverageList;
  ArrayList<String> timescales = null;
  ArrayList<String> titles = null;
  ArrayList<String> projectTitles = null;
  ArrayList<String> relatedProjectTitles = null;
  String site = null;
  String abstractText = null;
  String intellectualRightsText = null;
  String methodsText = null;
  String fundingText = null;
  String geographicDescriptionText = null;
  String projectAbstractText = null;
  String taxonomicCoverageText = null;
  boolean hasIntellectualRights = false;

  // Date fields
  String pubDate = null;
  String singleDateTime = null;
  String beginDate = null;
  String endDate = null;
  
  // Spatial coordinate fields
  String westBoundingCoordinate = null;
  String southBoundingCoordinate = null;
  String eastBoundingCoordinate = null;
  String northBoundingCoordinate = null;
  
  ArrayList<BoundingCoordinates> coordinatesList = new ArrayList<BoundingCoordinates>();
  ArrayList<DataSource> dataSources = new ArrayList<DataSource>();
  
  /*
   * Constructors
   */
  
  
  /*
   * Initialize the array lists when constructing this DataPackage object.
   */
  
  public DataPackage() {
    this.creatorList = new ArrayList<ResponsibleParty>();
    this.entityList = new ArrayList<Entity>();
    this.keywords = new ArrayList<String>();
    this.projectTitles = new ArrayList<String>();
    this.relatedProjectTitles = new ArrayList<String>();
    this.temporalCoverageList = new ArrayList<TemporalCoverage>();
    this.timescales = new ArrayList<String>();
    this.titles = new ArrayList<String>();
  }
  
  
  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  
  /**
   * Add a new creator to the creator list for this data package.
   * Filters out duplicates by checking to see whether the responsible
   * party is already in the list.
   */
  public void addCreator(ResponsibleParty responsibleParty) {
	  if ((responsibleParty != null) && (!hasCreator(responsibleParty))) {
		  creatorList.add(responsibleParty);
	  }
  }
  
  
  /**
   * Add a new data source for this data package.
   */
  public void addDataSource(String packageId, String title, String url) {
	  DataSource dataSource = new DataSource(packageId, title, url);
	  dataSources.add(dataSource);
  }
  
  
  /**
   * Add a new keyword for this data package.
   */
  public void addKeyword(String keyword) {
	keywords.add(keyword);
  }
  
  
  /**
   * Finds the matching object name for a given entity in this data package
   * based on the entity's name. This is a convenience method.
   * 
   * @param entityName    The entity name, e.g. "Data Entity One"
   * @return the matching object name, or null if no match was found
   */
	public String findObjectName(String entityName) {
		String objectName = null;

		for (Entity entity : getEntityList()) {
			String name = entity.getName();
			if ((name != null) && 
				(entityName != null) && 
				(name.trim().equals(entityName.trim()))
			) {
				objectName = entity.getObjectName();
			}
		}

		return objectName;
	}

  
  /* Getters and Setter */
  
	public String getAbstractText() {
		return abstractText;
	}


  public Set<String> getBeginDates() {
	  TreeSet<String> beginDates = new TreeSet<String>();
	  
	  for (TemporalCoverage temporalCoverage : temporalCoverageList) {
		  if (temporalCoverage != null) {
			  String beginDate = temporalCoverage.getBeginDate();
			  if (beginDate != null) {
				  beginDates.add(beginDate);
			  }
		  }
	  }
	  
	  return beginDates;
  }
		  
		  
  public Set<String> getEndDates() {
	  TreeSet<String> endDates = new TreeSet<String>();
	  
	  for (TemporalCoverage temporalCoverage : temporalCoverageList) {
		  if (temporalCoverage != null) {
			  String endDate = temporalCoverage.getEndDate();
			  if (endDate != null) {
				  endDates.add(endDate);
			  }
		  }
	  }
	  
	  return endDates;
  }
		  
		  
  public ArrayList<BoundingCoordinates> getCoordinatesList() {
    return coordinatesList;
  }

  
  public ArrayList<ResponsibleParty> getCreatorList() {
	    return creatorList;
  }

	  
  public ArrayList<DataSource> getDataSources() {
	return dataSources;
  }

	  
  public ArrayList<Entity> getEntityList() {
    return entityList;
  }

  
	public String getFundingText() {
		return fundingText;
	}


	public String getIntellectualRightsText() {
		return intellectualRightsText;
	}


  public ArrayList<String> getKeywords() {
	return keywords;
  }

	  
	public String getGeographicDescriptionText() {
		return geographicDescriptionText;
	}


	public String getMethodsText() {
		return methodsText;
	}


	public String getProjectAbstractText() {
		return projectAbstractText;
	}


  public String getSite() {
	return site;
  }

	  
  public String getPackageId() {
    return packageId;
  }
  
  
	public ArrayList<String> getProjectTitles() {
		return projectTitles;
	}
	  
	  
	public ArrayList<String> getRelatedProjectTitles() {
		return relatedProjectTitles;
	}
	  
	  
  public String getPubDate() {
    return pubDate;
  }
  
  
	public TreeSet<String> getSingleDateTimes() {
		TreeSet<String> singleDateTimes = new TreeSet<String>();
		for (TemporalCoverage temporalCoverage : temporalCoverageList) {
			if (temporalCoverage != null) {
				Set<String> temporalCoverageDateTimes = temporalCoverage.getSingleDateTimes();
				singleDateTimes.addAll(temporalCoverageDateTimes);
			}
		}

		return singleDateTimes;
	}
	  
	  
  public String getTaxonomicCoverageText() {
	return taxonomicCoverageText;
  }


  public ArrayList<String> getTitles() {
    return titles;
  }
  
  
  private boolean hasCreator(ResponsibleParty responsibleParty) {
	  boolean hasCreator = false;
	  String responsiblePartyCreatorName = null;
	  
	  if (responsibleParty != null) {
		  responsiblePartyCreatorName = responsibleParty.getCreatorName();
	  
		  if (responsiblePartyCreatorName != null) {
			  for (ResponsibleParty rp : this.creatorList) {
				  String rpCreatorName = rp.getCreatorName();
				  if (responsiblePartyCreatorName.equalsIgnoreCase(rpCreatorName)) {
					  hasCreator = true;
					  break;
				  }
			  }
		  }
	  }
	  
	  return hasCreator;
  }
  
  
  	/**
  	 * Boolean method to determine whether this data package has at
  	 * least one data table entity within it. 
  	 * 
  	 * @return  true if at least one data table entity is found, else false
  	 */
	public boolean hasDataTableEntity() {
		boolean hasDataTable = false;

		for (Entity entity : getEntityList()) {
			if (entity.getEntityType() == EntityType.dataTable) {
				hasDataTable = true;
			}
		}

		return hasDataTable;
	}
	
	
	public boolean hasIntellectualRights() {
		return hasIntellectualRights;
	}
	
	
	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

  
	  public void setBeginDate(String beginDate) {
		    this.beginDate = beginDate;
	  }
	  
	  
	  public void setEndDate(String endDate) {
		  this.endDate = endDate;
	  }
		
	  
		public void setFundingText(String fundingText) {
			this.fundingText = fundingText;
		}

	  
		public void setGeographicDescriptionText(String geographicDescriptionText) {
			this.geographicDescriptionText = geographicDescriptionText;
		}

		
		public void setIntellectualRights(boolean hasIntellectualRights) {
			this.hasIntellectualRights = hasIntellectualRights;
		}
		
		public void setIntellectualRightsText(String text) {
			this.intellectualRightsText = text;
		}

	  
		public void setMethodsText(String methodsText) {
			this.methodsText = methodsText;
		}

	  
  public void setPackageId(String packageId) {
    this.packageId = packageId;
    
    // If we can derive the LTER site from packageId, then do so
    if (packageId != null && 
    	packageId.startsWith("knb-lter-") &&
    	packageId.charAt(12) == '.') {
    	this.site = packageId.substring(9,12).toLowerCase();
    }
  }
  
  
	public void setProjectAbstractText(String projectAbstractText) {
		this.projectAbstractText = projectAbstractText;
	}

  
  public void setPubDate(String pubDate) {
    this.pubDate = pubDate;
  }
  
  
  public void setSingleDateTime(String singleDateTime) {
	    this.singleDateTime = singleDateTime;
  }
	  
	  
	public void setTaxonomicCoverageText(String taxonomicCoverageText) {
		this.taxonomicCoverageText = taxonomicCoverageText;
	}
	
	
	public void addBoundingCoordinates(String north, String south, String east, String west) {
		BoundingCoordinates boundingCoordinates = new BoundingCoordinates(north, south, east, west);
		coordinatesList.add(boundingCoordinates);
	}
	
	
	public void addTemporalCoverage(TemporalCoverage temporalCoverage) {
		if (temporalCoverage != null) {
			temporalCoverageList.add(temporalCoverage);
		}
	}
	
	
	public Set<String> getAlternativeTimeScales() {
		Set<String> alternativeTimeScales = new TreeSet<String>();

		for (TemporalCoverage temporalCoverage : temporalCoverageList) {
			if (temporalCoverage != null) {
				alternativeTimeScales.addAll(temporalCoverage.getAlternativeTimeScales());
			}
		}

		return alternativeTimeScales;
	}

	
	public String stringSerializeCoordinates() {
		String coordinates = null;
		StringBuilder sb = new StringBuilder("");
		
		for (int i = 0; i < coordinatesList.size(); i++) {
			BoundingCoordinates boundingCoordinates = coordinatesList.get(i);
			sb.append(boundingCoordinates.stringSerialize());
			if ((i + 1) < coordinatesList.size()) { sb.append(":"); }
		}
		
		coordinates = sb.toString();
		return coordinates;
	}
	
	
	public String jsonSerializeCoordinates() {
		String coordinates = null;
		StringBuilder sb = new StringBuilder("[\n");
		
		for (int i = 0; i < coordinatesList.size(); i++) {
			BoundingCoordinates boundingCoordinates = coordinatesList.get(i);
			sb.append(String.format("  %s", boundingCoordinates.jsonSerialize()));
			if ((i + 1) < coordinatesList.size()) { sb.append(","); }
			sb.append("\n");
		}
		
		sb.append("]\n");
		
		coordinates = sb.toString();
		return coordinates;
	}
	
	
	public class BoundingCoordinates {
		private String north, south, east, west;
		
		public String getNorth() {
			return north;
		}
		
		
		public String getSouth() {
			return south;
		}
		
		
		public String getEast() {
			return east;
		}
		
		
		public String getWest() {
			return west;
		}
		
		BoundingCoordinates(String north, String south, String east, String west) {
			this.north = north;
			this.south = south;
			this.east = east;
			this.west = west;
		}
		
		public String jsonSerialize() {
			return String.format("{\"north\":\"%s\", \"south\":\"%s\", \"east\":\"%s\", \"west\":\"%s\"}", 
					               north, south, east, west);
		}

		public String stringSerialize() {
			return String.format("%s,%s,%s,%s", north, south, east, west);
		}

		public String solrSerialize() {
			return String.format("%s %s %s %s", west, south, east, north);
		}
	}
	
	
	public class DataSource {
		private String sourceId, sourceTitle, sourceURL;
		
		public String getSourceId() {
			return sourceTitle;
		}
		
		
		public String getSourceTitle() {
			return sourceTitle;
		}
		
		
		public String getSourceURL() {
			return sourceURL;
		}
		
		
		public DataSource(String sourceId, String sourceTitle, String sourceURL) {
			this.sourceId = (sourceId == null) ? "" : sourceId;
			this.sourceTitle = (sourceTitle == null) ? "" : sourceTitle;
			this.sourceURL = (sourceURL == null) ? "" : sourceURL;
		}

		public String toXML() {
			String xml = null;
			StringBuilder stringBuilder = new StringBuilder("");
			
			stringBuilder.append("    <dataSource>\n");			
			stringBuilder.append(String.format("        <packageId>%s</packageId>\n", sourceId));
			stringBuilder.append(String.format("        <title>%s</title>\n", sourceTitle));
			stringBuilder.append(String.format("        <url>%s</url>\n", sourceURL));		
			stringBuilder.append("    </dataSource>\n");

			xml = stringBuilder.toString();
			return xml;
		}
	}
	
	
	public class DataDescendant {
		private String derivedId, derivedTitle, derivedURL;
		
		public String getDerivedId() {
			return derivedId;
		}
		
		
		public String getDerivedTitle() {
			return derivedTitle;
		}
		
		
		public String getDerivedURL() {
			return derivedURL;
		}
		
		
		public DataDescendant(String derivedId, String derivedTitle, String derivedURL) {
			this.derivedId = (derivedId == null) ? "" : derivedId;
			this.derivedTitle = (derivedTitle == null) ? "" : derivedTitle;
			this.derivedURL = (derivedURL == null) ? "" : derivedURL;
		}
		
		
		public String toXML() {
			String xml = null;
			StringBuilder stringBuilder = new StringBuilder("");
			
			stringBuilder.append("    <dataDescendant>\n");			
			stringBuilder.append(String.format("        <packageId>%s</packageId>\n", derivedId));
			stringBuilder.append(String.format("        <title>%s</title>\n", derivedTitle));
			stringBuilder.append(String.format("        <url>%s</url>\n", derivedURL));		
			stringBuilder.append("    </dataDescendant>\n");

			xml = stringBuilder.toString();
			return xml;
		}
	}
	
}
