/*
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
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

package edu.lternet.pasta.eventmanager;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.PastaConfigListener;

/**
 * Initializes the RESTful Event Manager web service. This class loads the Event
 * Manager's properties file(s) and provides accessor methods for those
 * properties. Note that the location of properties files will change depending
 * on the deployment environment - tomcat vs. local file system (used during
 * JUnit testing).
 */
public class ConfigurationListener extends PastaConfigListener {
	
	/*
	 * Class variables
	 */

    /**
     * The name of the Event Manager's properties file.
     */
    public static final String EVENT_MANAGER_PROPERTIES =
        "eventmanager.properties";

    /**
     * Used in the properties file to specify the XML schema for creating
     * subscriptions.
     */
    public static final String EML_SUBSCRIPTION_SCHEMA =
        "eml.subscription.schema";

    private static final Logger logger =
            Logger.getLogger(ConfigurationListener.class);

    private static File apiDocument;
    private static String auditHost;
    private static String dbDriver;           // database driver
    private static String dbURL;              // database URL
    private static String dbUser;             // database user name
    private static String dbPassword;         // database user password
    private static File demoDirectory;
    private static File emlSubscriptionSchema;
    private static String serviceDocument = null;
    private static File tutorialDocument;
    private static String webServiceVersion;
    private static File welcomePage;
    
    
    /*
     * Class methods
     */

    /**
     * Returns the API document of the Event Manager.
     *
     * @return the API document of the Event Manager.
     *
     * @see #API_DOCUMENT
     */
    public static File getApiDocument() {
        return apiDocument;
    }


    /**
     * Returns the audit host property value, e.g. "audit.lternet.edu".
     * 
     * @return  the audit host property value
     */
    public static String getAuditHost() {
      return auditHost;
    }

    
    /**
     * Returns the 'dbDriver' database driver value
     * @return dbDriver
     */
    public static String getDbDriver() {
        return dbDriver;
    }
    
    
    /**
     * Returns the 'dbURL' database URL value
     * @return dbURL
     */
    public static String getDbURL() {
        return dbURL;
    }
    
    
    /**
     * Returns the 'dbUser' database user value
     * @return dbUser
     */
    public static String getDbUser() {
        return dbUser;
    }
    
    
    /**
     * Returns the 'dbPassword' database password value
     * @return dbPassword
     */
    public static String getDbPassword() {
        return dbPassword;
    }
    
    
    /**
     * Returns the directory containing the web page demo.
     * @return the directory containing the web page demo.
     */
    public static File getDemoDirectory() {
        return demoDirectory;
    }
    
    
    /**
     * Returns the file containing the XML schema for creating EML modification
     * event subscriptions via the web service 'create' call.
     *
     * @return the file containing the XML schema for creating EML modification
     *         event subscriptions via the web service 'create' call.
     *
     * @see #EML_SUBSCRIPTION_SCHEMA
     */
    public static File getEmlSubscriptionSchemaFile() {
        return emlSubscriptionSchema;
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
     * Returns the tutorial document of the Event Manager.
     *
     * @return the tutorial document of the Event Manager.
     *
     * @see #TUTORIAL_DOCUMENT
     */
    public static File getTutorialDocument() {
        return tutorialDocument;
    }


    /**
     * Returns the web service version that is contained in response to clients.
     *
     * @return the web service version that is contained in response to clients.
     *
     * @see #WEB_SERVICE_VERSION
     */
    public static String getWebServiceVersion() {
        return webServiceVersion;
    }


    /**
     * Returns the welcome page.
     * @return the welcome page.
     */
    public static File getWelcomePage() {
        return welcomePage;
    }

    
    /*
     * Instance methods 
     */

    @Override
    public void setContextSpecificProperties() {

        super.setPastaServiceAcr();
        File serviceFile = getPastaServiceAcr();
        
        try {
          serviceDocument = FileUtils.readFileToString(serviceFile);
        }
        catch (IOException e) {
            logger.fatal("Initialization of the web service failed.", e);
        }

        Properties properties = loadPropertiesFile(EVENT_MANAGER_PROPERTIES);

        apiDocument = getFile(properties, API_DOCUMENT);
        auditHost = getProperty(properties, "eventmanager.auditmanager.host");
        dbDriver = getProperty(properties, "dbDriver");
        dbURL = getProperty(properties, "dbURL");
        dbUser = getProperty(properties, "dbUser");
        dbPassword = getProperty(properties, "dbPassword");
        demoDirectory = getFile(properties, "demo.directory");
        emlSubscriptionSchema = getFile(properties, EML_SUBSCRIPTION_SCHEMA);
        tutorialDocument = getFile(properties, TUTORIAL_DOCUMENT);
        webServiceVersion = getProperty(properties, WEB_SERVICE_VERSION);
        welcomePage = getFile(properties, WELCOME_PAGE);
    }
     
}
