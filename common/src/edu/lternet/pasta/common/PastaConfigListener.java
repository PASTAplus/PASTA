/*
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.lternet.pasta.common.security.access.UnauthorizedException;

/**
 * Used to facilitate the implementation of web service configuration listeners.
 * <p>
 * Objects of this class are instantiated either by Tomcat, when the web service
 * is deployed, or "manually" during JUnit testing. These instances are used
 * only to set static class properties, which can then be accessed using static
 * methods.
 * </p>
 * <p>
 * When a web service is deployed, Tomcat invokes
 * {@link #contextInitialized(ServletContextEvent)}, which handles all property
 * configuration. This includes: identifying the web service's {@code conf}
 * directory, initializing its logger, and invoking the abstract method
 * {@link #setContextSpecificProperties()}, which must be implemented by
 * extenders of this class.
 * </p>
 *
 */
public abstract class PastaConfigListener implements ServletContextListener {

    /**
     * Used in the properties file to specify the web service version to be
     * included in the <em>web service</em> header in responses to clients.
     */
    public static final String WEB_SERVICE_VERSION = "web.service.version";

    /**
     * Used in the properties file to specify the welcome page document of the
     * web service.
     */
    public static final String WELCOME_PAGE = "welcome.page";

    /**
     * Used in the properties file to specify the tutorial document of the web
     * service.
     */
    public static final String TUTORIAL_DOCUMENT = "tutorial.document";

    /**
     * Used in the properties file to specify the API document of the web
     * service.
     */
    public static final String API_DOCUMENT = "api.document";

    /**
     * The name of the log4j properties file.
     */
    public static final String LOG4J_PROPERTIES = "log4j.properties";

    /**
     * The name of the pasta-service-0.1 xml file.
     */
    public static final String PASTA_SERVICE_XML = "service.xml";

    private static final Logger logger =
                                Logger.getLogger(PastaConfigListener.class);

    private static File configDir;
    private static File pastaServiceAcr;
    protected static Properties properties;

    /**
     * Returns the {@code conf} directory used by this PASTA web service. If
     * deployed in Tomcat, the {@code CONFIG_DIR} property in the web.xml file
     * is used to determine this value. Otherwise, the file {@code
     * <user.dir>/WebRoot/WEB-INF/conf} is returned, where {@code user.dir} is a
     * Java {@link System} property.
     *
     * @return the {@code conf} directory used by this PASTA web service.
     */
    public static File getConfigDir() {

        if (configDir == null) {
            // setting to <user.dir>/WebRoot/WEB-INF/conf
            File f = new File(System.getProperty("user.dir"), "WebRoot");
            f = new File(f, "WEB-INF");
            configDir = FileUtility.assertCanRead(new File(f, "conf"));

            WorkingDirectory.setWorkingDirectory(configDir);
        }

        return configDir;
    }

    /**
     * Returns the logger, if one exists.
     * @return the logger, or {@code null} if not deployed.
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Loads the specified properties file in the {@code conf} directory.
     *
     * @param propertiesFile
     *            the name of the properties file.
     *
     * @return the properties contained in the specified file.
     *
     * @throws ResourceNotFoundException
     *             if the file cannot be found.
     *
     * @throws UnauthorizedException
     *             if the file exists, but cannot be read.
     *
     * @throws IllegalStateException
     *             if an {@link IOException} is thrown while reading the file.
     */
    public static void loadPropertiesFile(String propertiesFile) {
        File file = getFile(propertiesFile);
        properties = new Properties();

        try {
            properties.load(new FileInputStream(file));
        } 
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
  
    }

    /**
     * Returns the value of the specified property.
     *
     * @param p
     *            the properties.
     * @param key
     *            the property key.
     *
     * @return the value of the specified property.
     *
     * @throws NullPointerException
     *             if the specified property is missing or is an empty string.
     */
    public static String getProperty(Properties p, String key) {

        String value = p.getProperty(key);

        if (value == null || value.isEmpty()) {
            missingProperty(key);
        }

        return value;
    }

    private static void missingProperty(String propertyKey) {
        String s = "'" + propertyKey + "' not specified in properties file.";
        throw new NullPointerException(s);
    }

    /**
     * Returns the specified file in the {@code conf} directory.
     *
     * @param fileName the name of the file.
     *
     * @return the specified file in the {@code conf} directory.
     *
     * @throws ResourceNotFoundException
     *             if the file cannot be found.
     *
     * @throws UnauthorizedException
     *             if the file exists, but cannot be read.
     */
    public static File getFile(String fileName) {
        File file = new File(getConfigDir(), fileName);
        return FileUtility.assertCanRead(file);
    }

    /**
     * Returns the file specified by the provided key in the provided
     * properties.
     *
     * @param p the properties containing they key-value pair.
     *
     * @param key the key used to specify the file name.
     *
     * @return the specified file.
     *
     * @throws ResourceNotFoundException
     *             if the file cannot be found.
     *
     * @throws UnauthorizedException
     *             if the file exists, but cannot be read.
     */
    public static File getFile(Properties p, String key) {
        String fileName = getProperty(p, key);
        return getFile(fileName);
    }

    /**
     * Receives notification that the ServletContext is about to be shut down.
     * Does nothing.
     *
     * @param servletContextEvent
     *            the ServletContextEvent containing the ServletContext that is
     *            being destroyed
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // do nothing
    }

    /**
     * Receives notification that the web application initialization process is
     * starting. The {@code conf} directory is determined, the Log4J properties
     * file is loaded, and {@link #setContextSpecificProperties()} is called.
     *
     * @param servletContextEvent
     *            the ServletContextEvent containing the ServletContext that is
     *            being initialized
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        try {
            setConfigDir(servletContextEvent);
            setLog4jProperties();
            setPastaServiceAcr();
            setContextSpecificProperties();
        } catch (RuntimeException e) {
            logger.fatal("Initialization of the web service failed.", e);
            throw e;
        }

    }

    private void setConfigDir(ServletContextEvent sce) {

        ServletContext context = sce.getServletContext();

        String CONFIG_DIR = context.getInitParameter("CONFIG_DIR");
        String realPath = context.getRealPath(CONFIG_DIR);

        configDir = FileUtility.assertCanRead(realPath);

        WorkingDirectory.setWorkingDirectory(configDir);
    }

    private void setLog4jProperties() {
        File properties = new File(getConfigDir(), LOG4J_PROPERTIES);
        PropertyConfigurator.configureAndWatch(properties.getAbsolutePath());
    }

    public static void setPastaServiceAcr() {
        pastaServiceAcr = new File(getConfigDir(), PASTA_SERVICE_XML);
    }

    /**
     * Makes available the file containing the service access control rules.
     * @return File containing the service access control rules.
     */
    public static File getPastaServiceAcr() {
        return pastaServiceAcr;
    }

    /**
     * Called during {@linkplain #contextInitialized(ServletContextEvent)}
     * to initialize all configuration properties specific to the web service.
     */
    public abstract void setContextSpecificProperties();

}
