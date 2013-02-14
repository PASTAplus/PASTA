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

import javax.persistence.Persistence;

import edu.lternet.pasta.common.PastaConfigListener;


public class ConfigurationListener extends PastaConfigListener
{

    /**
     * The name of the Event Manager's properties file.
     */
    public static final String AUDIT_MANAGER_PROPERTIES =
        "auditmanager.properties";
    
    private static Properties properties;

    /**
     * Used in the properties file to specify the Audit Manager's persistence
     * unit.
     */
    public static final String PERSISTENCE_UNIT = "persistence.unit";
    private static String persistenceUnit;
    private static String junitPersistenceUnit;

    /**
     * Used in the properties file to specify the Audit Manager's persistence
     * unit for JUnit testing.
     */
    public static final String JUNIT_PERSISTENCE_UNIT =
        "junit.persistence.unit";
    private static String webServiceVersion;
    private static File apiDocument;
    private static File tutorialDocument;
    private static File welcomePage;
    private static File demoDirectory;

    @Override
    public void setContextSpecificProperties() {
        super.setPastaServiceAcr();

        properties = loadPropertiesFile(AUDIT_MANAGER_PROPERTIES);

        setPersistenceUnit(properties);
        setJUnitPersistenceUnit(properties);
        comparePersistenceUnits();

        webServiceVersion = getProperty(properties, WEB_SERVICE_VERSION);
        apiDocument = getFile(properties, API_DOCUMENT);
        tutorialDocument = getFile(properties, TUTORIAL_DOCUMENT);
        welcomePage = getFile(properties, WELCOME_PAGE);
        demoDirectory = getFile(properties, "demo.directory");
    }

    /**
     * Returns the primary persistence unit. This value must match a {@code
     * persistence-unit} element in the {@code persistence.xml} file, but it
     * cannot match the value for {@link #JUNIT_PERSISTENCE_UNIT}.
     *
     * @return the primary persistence unit used by the Event Manager.
     *
     * @see #PERSISTENCE_UNIT
     */
    public static String getPersistenceUnit() {
        return persistenceUnit;
    }

    private static void setPersistenceUnit(Properties p) {
        persistenceUnit = getProperty(p, PERSISTENCE_UNIT);
        checkPersistenceUnit(persistenceUnit);
    }
    
    
    /**
     * Returns the properties class variable.
     * 
     * @return  properties class variable
     */
    public static Properties getProperties() {
      return properties;
    }

    
    private static void checkPersistenceUnit(String pUnit) {

        try {
            Persistence.createEntityManagerFactory(pUnit).close();
        } catch (RuntimeException e) {
            String s = "The persistence unit '" + pUnit +
                       "' in " + AUDIT_MANAGER_PROPERTIES +
                       " could not be loaded. Make sure it matches " +
                       "a persistence-unit in persistence.xml.";
            throw new IllegalStateException(s, e);
        }

    }

    /**
     * Returns the persistence unit used for JUnit testing. This value must
     * match a {@code persistence-unit} element in the {@code persistence.xml}
     * file, but it cannot match the value for {@link #PERSISTENCE_UNIT}.
     *
     * @return the persistence unit used for JUnit testing.
     */
    public static String getJUnitPersistenceUnit() {
        return junitPersistenceUnit;
    }

    private static void setJUnitPersistenceUnit(Properties p) {
        junitPersistenceUnit = getProperty(p, JUNIT_PERSISTENCE_UNIT);
        checkPersistenceUnit(junitPersistenceUnit);
    }

    private static void comparePersistenceUnits() {

        if (persistenceUnit.equals(junitPersistenceUnit)) {
            String s = "The properties '" + JUNIT_PERSISTENCE_UNIT +
                       "' and '" + PERSISTENCE_UNIT +
                       "' were assigned the same value '" + persistenceUnit +
                       "'. This is too dangerous to allow.";
            throw new IllegalArgumentException(s);
        }
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
     * Returns the welcome page.
     * @return the welcome page.
     */
    public static File getWelcomePage() {
        return welcomePage;
    }

    /**
     * Returns the directory containing the web page demo.
     * @return the directory containing the web page demo.
     */
    public static File getDemoDirectory() {
        return demoDirectory;
    }
}
