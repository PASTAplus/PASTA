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

package edu.lternet.pasta.common.eml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlUtility;

/**
 * @author servilla
 * @since Nov 6, 2012
 * 
 *        Provides a plain old Java object (POJO) interface to content from an
 *        EML 2.1.0 XML document.
 */
public class EmlObject {

	/*
	 * Class variables
	 */

	/*
	 * Instance variables
	 */

	private Logger logger = Logger.getLogger(EmlObject.class);

	private DataPackage dataPackage = null;
	private Integer personCount = 0;
	private Integer orgCount = 0;

	/*
	 * Constructors
	 */

	/**
	 * Create the EML 2.1.0 POJO from an EML file.
	 * 
	 * @param emlString
	 *            The EML XML document
	 */
	public EmlObject(String emlString) {
		EMLParser emlParser = new EMLParser();
		this.dataPackage = emlParser.parseDocument(emlString);
	}

	
  /**
   * Create the EML 2.1.0 POJO from an EML file.
   * 
   * @param emlFile
   */
  public EmlObject(File emlFile) {
    this(EmlUtility.getEmlDoc(emlFile));
  }

  
	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

  /**
   * Returns ArrayList of Creators.
   * 
   * @return Creator array list
   */
  public ArrayList<ResponsibleParty> getCreators() {

    ArrayList<ResponsibleParty> creators = this.dataPackage.getCreatorList();
    
    for (ResponsibleParty creator : creators) {
      if (creator.isPerson()) {
        this.personCount++;
      }
      else if (creator.isOrganization()) {
        this.orgCount++;
      }
    }
    
    return creators;
  }

  
  	/**
  	 * Access the dataPackage instance variable.
  	 * 
  	 * @return  the dataPackage object
  	 */
	public DataPackage getDataPackage() {
		return this.dataPackage;
	}


	/**
	 * Returns the number of creators of type PERSON after the call to 
	 * getCreators().
	 * 
	 * @return Person count
	 */
	public Integer getPersonCount()	{
		return this.personCount;
	}
	
	/**
	 * Returns the number of creators of type ORGANIZATION after the call to 
	 * getCreators().
	 * 
	 * @return Organization count
	 */
	public Integer getOrgCount() {
		return this.orgCount;
	}

	/**
	 * Returns ArrayList of titles.
	 * 
	 * @return Title array list
	 */
	public ArrayList<Title> getTitles() {
		ArrayList<Title> titles = new ArrayList<Title>();
		List<String> titleList = this.dataPackage.getTitles();

		for (String titleName : titleList) {
			Title title = new Title();
		
			try {
				title.setTitleType(Title.MAIN);
			} 
			catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

			title.setTitle(titleName.trim());
			titles.add(title);
		}

		return titles;
	}
	
	/**
	 * Returns the publication date from the EML document.
	 * 
	 * @return Publication date
	 */
	public String getPubDate() {
		
		String pubDate = this.dataPackage.getPubDate();
		
		return pubDate;
		
	}
	
	/**
	 * Returns the abstract text string from the EML document.
	 * 
	 * @return abstract text
	 */
	public String getAbstractText() {
		String abstractText = this.dataPackage.getAbstractText();
		
		return abstractText;
	}
	
	
	/**
	 * Boolean to determine whether the data package has the intellectualRights 
	 * element present.
	 * 
	 * @return true if the intellectualRights element is present, else false
	 */
	public boolean hasIntellectualRights() {
		return this.dataPackage.hasIntellectualRights();
	}
	
	
	/*
	 * Access methods for the bounding coordinates
	 */
	
	public String jsonSerializeCoordinates() {
		return this.dataPackage.jsonSerializeCoordinates();
	}

	public String stringSerializeCoordinates() {
		return this.dataPackage.stringSerializeCoordinates();
	}

}
