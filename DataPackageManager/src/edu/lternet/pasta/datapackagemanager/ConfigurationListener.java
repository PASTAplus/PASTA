/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011 the University of New Mexico.
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

package edu.lternet.pasta.datapackagemanager;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.ucsb.nceas.utilities.Options;


/**
 * The ConfigurationListener class initializes the DataPackageManager
 * restful web application. The initialization code executes when
 * the web application context starts up. This class implements the
 * ServletContextListener interface.
 * 
 * @author dcosta
 * @created 19-Nov-2010 2:10:00 PM
 *
 */
public class ConfigurationListener implements ServletContextListener {

  /*
   * Class fields
   */
  
  private static final Logger logger = 
    Logger.getLogger(edu.lternet.pasta.datapackagemanager.ConfigurationListener.class);
    
  private static Options options = null;
  private static String eventSubscriptionDocument = null;
  private static String serviceDocument = null;
  private static String versionNumber = null;
  private static String versionHeader = null;
  
  
  /*
   * Instance fields
   */

  
  /*
   * Class methods
   */
  
  /**
   * Accessor method for the options class variable
   * 
   * @return  the options class variable
   */
  public static Options getOptions() {
    return options;
  }
  
  
  /*
   * Instance methods
   */
  
  /**
   * This method can be used to execute code when the web application
   * shuts down.
   * 
  * @param  servletContextEvent     The ServletContextEvent object
   */
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
  }

  
  /**
   * Run initialization code when at web application start-up.
   * 
   * 
   * @param  servletContextEvent     The ServletContextEvent object
   * @throws ResourceNotFoundException
   *                 if the properties file can not be found
   * @throws IllegalStateException
   *                 if an {@link IOException} is thrown while reading the 
   *                 properties file
   */
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext servletContext = servletContextEvent.getServletContext();

    String CONFIG_DIR = servletContext.getInitParameter("CONFIG_DIR");
    String dirPath = servletContext.getRealPath(CONFIG_DIR);
    initialize(dirPath);
  }
  
  
  /** Getting for the event subscription xsd document, a Document
   * object created from the eml-subscription.xsd schema file.
   * 
   * @return   the serviceDocument XML string
   */
  public static String getEventSubscriptionDocument() {
    return eventSubscriptionDocument;
  }
  

  /** Getting for the service document, a Document
   * object created from the service.xml file
   * 
   * @return   the serviceDocument XML string
   */
  public static String getServiceDocument() {
    return serviceDocument;
  }
  

  /**
   * Getter for the versionNumber class field.
   * @return the versionNumber class field
   */
  public static String getVersionNumber()
  {
    return versionNumber;
  }

  
  /**
   * Getter for the versionHeader class field.
   * @return the versionHeader class field
   */
  public static String getVersionHeader()
  {
    return versionHeader;
  }


  /**
   * Initialize the logger and load the configuration from the properties file
   * when starting up the web application.
   * 
   * @param dirPath  the directory path where configuration files are found
   * @throws ResourceNotFoundException
   *                 if the file can not be found
   * @throws IllegalStateException
   *                 if an {@link IOException} is thrown while reading the file
   */
  public void initialize(String dirPath) {
    // Initialize the properties file for log4j
    String log4jPropertiesPath = dirPath + "/log4j.properties";
    PropertyConfigurator.configureAndWatch(log4jPropertiesPath);
    
    // Initialize the properties file for Data Manager service
    String propertiesPath = dirPath + "/datapackagemanager.properties";
    String serviceFilePath = dirPath + "/service.xml";
    String eventSubscriptionFilePath = dirPath + "/eml-subscription.xsd";
    
    try {
      File propertiesFile = new File(propertiesPath);
      propertiesFile = FileUtility.assertCanRead(propertiesFile);
      options = Options.initialize(propertiesFile);
      setVersion(options);
      
      File serviceFile = new File(serviceFilePath);
      serviceFile = FileUtility.assertCanRead(serviceFile);
      serviceDocument = FileUtility.fileToString(serviceFile);
      if (serviceDocument == null || serviceDocument.equals("")) {
        throw new IllegalStateException("Error loading service.xml file.");
      }
      
      File eventSubscriptionFile = new File(eventSubscriptionFilePath);
      eventSubscriptionFile = FileUtility.assertCanRead(eventSubscriptionFile);
      eventSubscriptionDocument = FileUtility.fileToString(eventSubscriptionFile);
      if (eventSubscriptionDocument == null || eventSubscriptionDocument.equals("")) {
        throw new IllegalStateException("Error loading eml-subscription.xsd file.");
      }
      
      
      
    } 
    catch (IOException e) {
      String errorMessage = "IOException loading properties file at '" + propertiesPath + "': " + e.getMessage();
      throw new IllegalStateException(errorMessage, e);
    }
  
    logger.info(versionHeader + ": " + versionNumber + " has initialized.");
  } 

  
  private void setVersion(Options options) {
    versionNumber = options.getOption("datapackagemanager.version.number");
    versionHeader = options.getOption("datapackagemanager.version.header");
    if (versionNumber == null || versionHeader == null)
      throw new IllegalArgumentException("datapackagemanager.version.number or " +
                                         "datapackagemanager.version.header " +
                                         "not specified");
  }

}
