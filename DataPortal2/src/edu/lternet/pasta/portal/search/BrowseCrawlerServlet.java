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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.portal.ConfigurationListener;


/**
 * BrowseCrawlerServlet class allows the BrowseCrawler to run as a background
 * process.
 */
public class BrowseCrawlerServlet extends HttpServlet implements Runnable {
  
  /*
   * Class fields
   */;
  private static final Logger logger = Logger.getLogger(BrowseCrawlerServlet.class);
  static final long serialVersionUID = 0;  // Needed for Eclipse warning.

  
  /*
   * Instance fields
   */
  private boolean keepRunning = true;       // stops thread if false
  private Thread browseCrawlerThread;       // background thread
  private int crawlPeriod;                  // minimum hours between crawls
  ServletContext servletContext = null;
  private int windowBeginsAt = 0;           // first hour of crawlwindow (0-23)
  private int windowEndsAt = 23;            // last hour of crawl window (0-23) 


  /*
   * Class methods
   */

  
  /*
   * Instance methods
   */

  /**
   * Determine whether this is a good time to run the crawler. Currently the
   * only criterion is whether the current time is within the crawl window
   * hours. For example, if windowBeginsAt is 3, and windowEndsAt is 4, then
   * the crawler will only run between 3 a.m. and 4 a.m.  (Note that the logic
   * here is limited, because it only works when windowBeginsAt is less than
   * windowEndsAt.)
   */
  private boolean checkCrawlCriteria() {
    boolean crawlCriteria = true;
    
    crawlCriteria = crawlCriteria && isWithinCrawlWindow();
    
    return crawlCriteria;
  }
  

  /**
   * Stops the BrowseCrawlerServlet thread when the servlet shuts down.
   */
  public void destroy() {
    keepRunning = false;
  }

  
  /**
   * Initializes the servlet by starting a separate thread in which to
   * run the BrowseCrawler main program.
   * 
   * @param config   the ServletConfig object, holding servlet configuration
   *                 info
   * @throws         ServletException
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    PropertiesConfiguration options = ConfigurationListener.getOptions();
    String browseDirPath = options.getString("browse.dir");
    BrowseSearch.setBrowseCacheDir(browseDirPath);
    Integer crawlPeriodInt;
    this.servletContext = getServletContext();

    crawlPeriodInt = new Integer("24");
    this.crawlPeriod = crawlPeriodInt.intValue();

    browseCrawlerThread = new Thread(this);
    browseCrawlerThread.setPriority(Thread.MIN_PRIORITY); // be a good citizen
    browseCrawlerThread.start();
  }
  
 
  /**
   * Initiates a new crawl by constructing a new BrowseCrawler object and
   * calling its crawl() method.
   */
  private void initiateNewCrawl() {
    BrowseCrawler browseCrawler = new BrowseCrawler();
    BrowseGroup browseCache = browseCrawler.crawlKeywordTerms();    

    if (browseCache != null) {
    	/* Lock the servlet context object to guarantee that only one thread at a
    	 * time can be getting or setting the context attribute. 
    	 */
    	synchronized(servletContext) {
    		servletContext.setAttribute("browseKeywordHTML", browseCache.toHTML());
    	}
    }
  }
  

  /**
   * Boolean to determine whether the current time is within the crawl window.
   * In other words, is this a good time to crawl? For example, if 
   * windowBeginsAt is 3, and windowEndsAt is 4, then the crawler
   * will only run between 3 a.m. and 4 a.m.  (Note that the logic
   * here is limited, because it only works when windowBeginsAt is less than
   * windowEndsAt.)
   * 
   * @return  true if the current time is within the crawl window, else false
   */
  private boolean isWithinCrawlWindow() {
    Calendar now = Calendar.getInstance();
    int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
    boolean isWithin = false;
    
    if ((hourOfDay >= windowBeginsAt) && (hourOfDay <= windowEndsAt)) {
      isWithin = true;
    }
    
    logger.debug("windowBeginsAt:" + windowBeginsAt);
    logger.debug("windowEndsAt:  " + windowEndsAt);
    
    return isWithin;
  }
  

  /**
   * Runs the BrowseCrawler main program in a separate thread. 
   */
  public void run() {
    long delta;                     // endTime - startTime
    long endTime;                   // time that a crawl completes
    final long oneHour = (60 * 60 * 1000);      // milliseconds in one hour
    long periodSleepTime;           // time to sleep between crawls
    SimpleDateFormat simpleDateFormat = 
                       new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
    long startTime;                 // time that a crawl starts
    final long tenMinutes = (10 * 60 * 1000);   // milliseconds in ten minutes
    
    while (keepRunning) {
      Date now = new Date();
      logger.debug(
          "Checking crawl critieria at time: " +
          simpleDateFormat.format(now)
                        );
      if (checkCrawlCriteria()) {
        logger.info("Initiating new crawl.");
        startTime = System.currentTimeMillis();
        initiateNewCrawl();
        endTime = System.currentTimeMillis();
        delta = endTime - startTime;

        try {
          periodSleepTime = (crawlPeriod * oneHour) - delta;
          logger.info(
              "Crawl completed. Sleeping for " + 
              periodSleepTime +  
              " milliseconds."
                            );
          Thread.sleep(periodSleepTime);
        }
        catch (InterruptedException e) {
          logger.warn("InterruptedException: " + e.getMessage());
        }
      }
      else {
        logger.debug("Crawl criteria failed. Sleeping for ten minutes.");
        try {
          Thread.sleep(tenMinutes);
        }
        catch (InterruptedException e) {
          logger.error("InterruptedException: " + e.getMessage());
        }
      }
    }
  }
 
}
