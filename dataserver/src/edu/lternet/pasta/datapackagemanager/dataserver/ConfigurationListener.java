/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011-2015 the University of New Mexico.
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

package edu.lternet.pasta.datapackagemanager.dataserver;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.lternet.pasta.common.ResourceNotFoundException;


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
    Logger.getLogger(ConfigurationListener.class);
    
  private static PropertiesConfiguration config = null;

  
  
  /*
   * Instance fields
   */

  
  /*
   * Class methods
   */
  
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

		// Create an absolute path for accessing configuration properties
		String cwd = servletContext.getRealPath(".");

		// Initialize log4j
		String log4jPropertiesPath = cwd + "/WEB-INF/conf/log4j.properties";
		PropertyConfigurator.configureAndWatch(log4jPropertiesPath);

		// Initialize commons configuration
		String appConfigPath = cwd + "/WEB-INF/conf/datapackagemanager.properties";
		try {
			config = new PropertiesConfiguration(appConfigPath);
			config.setProperty("system.cwd", cwd);
			config.save();
		} catch (ConfigurationException e) {
			logger.error(e);
			e.printStackTrace();
		}

	}

	
	/**
	 * Configure logger and properties applications for non-servlet based
	 * execution (e.g., main(String[] args)).
	 */
	public static void configure() {

		String cwd = System.getProperty("user.dir");

		// Initialize log4j
		String log4jPropertiesPath = cwd + "/WebRoot/WEB-INF/conf/log4j.properties";
		PropertyConfigurator.configureAndWatch(log4jPropertiesPath);

		// Initialize commons configuration
		String appConfigPath = cwd + "/WebRoot/WEB-INF/conf/dataserver.properties";
		try {
			config = new PropertiesConfiguration(appConfigPath);
			config.setProperty("system.cwd", cwd);
			config.save();
		} catch (ConfigurationException e) {
			logger.error(e);
			e.printStackTrace();
		}

	}

	
	/**
	 * Return the Configuration object to obtain property options.
	 * 
	 * @return The Configuration object.
	 */
	public static PropertiesConfiguration getOptions() {
		return config;
	}

}
