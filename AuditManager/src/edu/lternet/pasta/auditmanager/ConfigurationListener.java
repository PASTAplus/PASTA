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

package edu.lternet.pasta.auditmanager;

import java.io.File;
import java.util.Properties;

import edu.lternet.pasta.common.PastaConfigListener;


public class ConfigurationListener extends PastaConfigListener {

    // The name of the Event Manager's properties file.
    public static final String AUDIT_MANAGER_PROPERTIES = "auditmanager.properties";
    
    private static Properties properties;
    private static String webServiceVersion;
    private static File apiDocument;
    private static File tutorialDocument;
    private static File welcomePage;
    private static File demoDirectory;

    
    /**
     * Returns the API document of the Audit Manager.
     *
     * @return the API document of the Audit Manager.
     *
     * @see #API_DOCUMENT
     */
    public static File getApiDocument() {
        return apiDocument;
    }

    
    /**
     * Returns the directory containing the web page demo.
     * @return the directory containing the web page demo.
     */
    public static File getDemoDirectory() {
        return demoDirectory;
    }
    
    
    /**
     * Returns the tutorial document of the Audit Manager.
     *
     * @return the tutorial document of the Audit Manager.
     *
     * @see #TUTORIAL_DOCUMENT
     */
    public static File getTutorialDocument() {
        return tutorialDocument;
    }


    /**
     * Returns the properties class variable.
     * 
     * @return  properties class variable
     */
    public static Properties getProperties() {
      return properties;
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

    
    @Override
    public void setContextSpecificProperties() {
        super.setPastaServiceAcr();
        
        properties = loadPropertiesFile(AUDIT_MANAGER_PROPERTIES);
        
        apiDocument = getFile(properties, API_DOCUMENT);
        demoDirectory = getFile(properties, "demo.directory");
        tutorialDocument = getFile(properties, TUTORIAL_DOCUMENT);
        webServiceVersion = getProperty(properties, WEB_SERVICE_VERSION);
        welcomePage = getFile(properties, WELCOME_PAGE);
    }

}
