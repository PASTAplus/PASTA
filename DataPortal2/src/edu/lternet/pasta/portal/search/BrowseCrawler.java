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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import org.apache.log4j.Logger;

import edu.lternet.pasta.portal.ConfigurationListener;


/**
 * The BrowseCrawler class searches browse terms via the Data Package Manager
 * and stores the search results in a browse cache on disk.
 */
public class BrowseCrawler {

  /*
   * Class fields
   */
  private static final Logger logger = Logger.getLogger(BrowseCrawler.class);


  /*
   * Instance fields
   */
  
  
  /*
   * Constructor
   */
  
  public BrowseCrawler() {
  }

  
  /*
   * Class methods
   */

  /**
   * BrowseCrawler main method. Used only for testing the class from the
   * command line.
   * 
   * @param args        the command line arguments, String[]
   */
  public static void main(String[] args) {
    ConfigurationListener.configure();
    BrowseCrawler browseCrawler = new BrowseCrawler();
    browseCrawler.crawlKeywordTerms();
  }
  

  /*
   * Instance methods
   */
  
	/**
	 * Crawls keyword terms in the LTER Controlled Vocabulary, querying each
	 * browse term.
	 */
	public BrowseGroup crawlKeywordTerms() {
		logger.info("Starting crawl of keywords in LTER Controlled Vocabulary.");

		BrowseGroup browseCache = BrowseGroup.generateKeywordCache();

		if (browseCache != null) {
			ArrayList<BrowseTerm> browseTerms = new ArrayList<BrowseTerm>();
			browseCache.getBrowseTerms(browseTerms);
			logger.info(String.format("Found %d keyword terms", browseTerms.size()));
			for (BrowseTerm browseTerm : browseTerms) {
				logger.info("Crawling term: " + browseTerm.getValue());
				browseTerm.crawl();
			}
			File browseCacheFile = new File(BrowseSearch.browseKeywordPath);
			writeBrowseCache(browseCacheFile, browseCache);
			logger.info(String.format("Finished keyword crawl: %d terms", browseTerms.size()));
		}
		else {
			logger.error("An error occurred while attempting to generate the keyword cache.");
		}

		return browseCache;
	}

  
  /**
   * Writes the browse cache from memory to disk.
   */
  private void writeBrowseCache(File browseCacheFile, BrowseGroup browseCache) {
    StringBuffer stringBuffer = new StringBuffer("");   
    stringBuffer.append(browseCache.toXML());
    
    try {
      FileUtils.writeStringToFile(browseCacheFile, stringBuffer.toString());
    }
    catch (IOException e) {
      logger.error("IOException:\n" + e.getMessage());
      e.printStackTrace();
    }
  }

}
