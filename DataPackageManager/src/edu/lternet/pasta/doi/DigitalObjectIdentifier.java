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

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.ucsb.nceas.utilities.Options;
import org.apache.log4j.Logger;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author servilla
 * @since Nov 8, 2012
 * 
 *        Manage the attributes of a DOI based on the given PASTA resource
 *        identifier.
 * 
 */
public class DigitalObjectIdentifier {

	/*
	 * Class variables
	 */

  private static Logger logger = Logger.getLogger(DigitalObjectIdentifier.class);

	private static final String type = "DOI";
  private static final String dirPath = "WebRoot/WEB-INF/conf";

	/*
	 * Instance variables
	 */

  private String prefix;
  private String context;
	private String md5Id = null;

	/*
	 * Constructors
	 */

    /**
     * Return a new Digital Object Identifier object for the given PASTA
     * identifier and MD5 string. This version of the constructor is called
     * when we already know the MD5 string and so don't need to regenerate it.
     * This is typically the case when we are updating metadata for an
     * existing DOI instead of minting a new DOI.
     *
     * @param md5Id
     *            The generated MD5 string
     * @throws ConfigurationException
     */
    public DigitalObjectIdentifier(String md5Id) throws ConfigurationException {
        Options options = ConfigurationListener.getOptions();

        if (options == null) {
            ConfigurationListener configurationListener = new ConfigurationListener();
            configurationListener.initialize(dirPath);
            options = ConfigurationListener.getOptions();
        }

        loadOptions(options);

        this.md5Id = md5Id;
    }    
    
    
	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */
	
	/**
	 * Get the DOI prefix.
	 * 
	 * @return DOI prefix
	 */
	public String getPrefix() {
		return prefix;
	}
	
	/**
	 * Get DOI context.
	 * 
	 * @return DOI context
	 */
	public String getContext() {
		return context;
	}
	
	/**
	 * Get DOI type.
	 * 
	 * @return DOI type.
	 */
	public String getType() {
		return DigitalObjectIdentifier.type;
	}
	
	/**
	 * Get DOI MD5 identifier (opaque identifier).
	 * 
	 * @return DOI MD5 identifier
	 */
	public String getMd5Id() {
		return this.md5Id;
	}
	
	/**
	 * Get the DOI canonical form.
	 * 
	 * @return DOI canonical form
	 */
	public String getDoi() {
		String doi = "doi:" + prefix + "/" + context + "/" + this.md5Id;
		return doi;
	}

	
	/**
	 * Get the DOI identifier.
	 * 
	 * @return DOI identifier
	 */
	public String getIdentifier() {
		String identifier = prefix + "/"  + context + "/" + this.md5Id;
		return identifier;
	}


  /*
   * Load local properties from identity.properties
   */
  protected void loadOptions(Options options) throws ConfigurationException {


    if (options == null) {
      String gripe = "Failed to load the Data Package Manager properties file: 'datapackagemanager.properties'";
      throw new ConfigurationException(gripe);
    } else {
      try {
        prefix = options.getOption("datapackagemanager.doiPrefix");
        context = options.getOption("datapackagemanager.doiContext");
      }
      catch (Exception e) {
        logger.error(e.getMessage());
        e.printStackTrace();
        throw new ConfigurationException(e.getMessage());
      }
    }

  }


  /**
	 * @param args
	 */
    public static void main(String[] args) {
        String resourceId = "https://pasta.lternet.edu/package/metadata/eml/knb-lter-nin/1/1";
        String md5Id = DigestUtils.md5Hex(resourceId);
        DigitalObjectIdentifier doi = null;

        try {
            doi = new DigitalObjectIdentifier(md5Id);
        } 
        catch (ConfigurationException e) {
            logger.error("DigitalObjectIdentifier: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println(doi.getDoi());
    }

	/**
	 * Test if the "doi" string is in a raw DOI format such that it begins
	 * with a DOI shoulder (e.g., 10.6073/pasta/a9c450094539256346bb53d791bbb588).
	 *
	 * @param doi
	 * @return Boolean
	 */

	public static Boolean isRawDoi(String doi) {
		String shoulder = doi.split("/")[0];
		try {
			Float.parseFloat(shoulder);
		}
		catch (NullPointerException | NumberFormatException ex) {
			return false;
		}
		return true;
	}

}