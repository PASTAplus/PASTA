/*
 * 
 * $Date$ $Author$ $Revision$
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

package edu.lternet.pasta.gatekeeper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.common.security.auth.SymmetricEncrypter;

/**
 * The ConfigurationListener class initializes the LTER restful web application.
 * The initialization code executes when the web application context starts up.
 * This class is a subclass of ServletContextListener.
 */
public class ConfigurationListener implements ServletContextListener
{

    private static final Logger logger =
            Logger.getLogger(ConfigurationListener.class);

    /* Name of Gatekeeper's properties file. */
    public static final String PROPERTIES = "gatekeeper.properties";

    public static final String TOKEN_NAME = "token.name";
    public static final String TOKEN_TTL = "token.ttl";
    public static final String AUTH_GROUP = "token.authgroup";
    public static final String PUBLIC_USER = "token.publicuser";
    public static final String LDAP_KEY_STORE = "ldap.keystore";
    public static final String PRIVATE_KEY = "token.privatekey";
    public static final String WEB_SERVICE_VERSION = "web.service.version";
    public static final String LTER_KEYSTORE = "lter.keystore";
    public static final String LTER_CERTIFICATE = "lter.certificate";
    public static final String LTER_KEYSTORE_TYPE = "lter.keystore.type";
    public static final String LTER_KEYSTORE_ALIAS = "lter.keystore.alias";
    public static final String LTER_STORE_PASSWD = "lter.store.passwd";
    public static final String LTER_KEY_PASSWD = "lter.key.passwd";
    public static final String SIGNATURE_DIR = "signature.dir";


    private static String tokenName = null;
    private static long tokenTtl = new Long(-1);
    private static String authGroup = null;
    private static String publicUser = null;
    private static File ldapKeyStore = null;
    private static SecretKey privateKey = null;
    private static String webServiceVersion = null;
    private static File lterKeyStore = null;
    private static File lterCertificate = null;
    private static String lterKeyStoreType = null;
    private static String lterKeyStoreAlias = null;
    private static String lterStorePasswd = null;
    private static String lterKeyPasswd = null;
    private static String signatureDir = null;


    private static File configDir;
    private static File propertiesFile;

    /**
     * Getter for the configDir.
     * 
     * @return the directory as a File.
     */
    public static File getConfigDir() {

        if (configDir == null) {
            File f = new File(System.getProperty("user.dir"), "WebRoot");
            f = new File(f, "WEB-INF");
            return new File(f, "conf");
        }
        return configDir;
    }

    /**
     * Getter for the tokenName class field.
     * 
     * @return the tokenName class field.
     */
    public static String getTokenName() {
        return tokenName;
    }

    /**
     * Getter for the tokenTtl class field.
     * 
     * @return the tokenTtl class field.
     */
    public static long getTokenTtl() {
        return tokenTtl;
    }

    /**
     * Getter for the webServiceVersion class field.
     * 
     * @return the webServiceVersion class field.
     */
    public static String getWebServiceVersion() {
        return webServiceVersion;
    }

    /**
     * Getter for the authGroup class field.
     * 
     * @return the authGroup class field.
     */
    public static String getAuthGroup() {
        return authGroup;
    }

    /**
     * Getter for the publicUser class field.
     * 
     * @return the publicUser class field.
     */
    public static String getPublicUser() {
        return publicUser;
    }

    /**
     * Getter for the ldapKeyStore class field.
     * 
     * @return the ldapKeyStore class field.
     */
    public static File getLdapKeyStore() {
        return ldapKeyStore;
    }

    /**
     * Getter for the privateKey class field.
     * 
     * @return the privateKey class field.
     */
    public static SecretKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Getter for the lterKeyStore class field.
     *
     * @return the lterKeyStore class field.
     */
    public static File getLterKeyStore() {
      return lterKeyStore;
    }

    /**
     * Getter for the lterCertificate class field.
     *
     * @return the lterCertificate class field.
     */
    public static File getLterCertificate() {
      return lterCertificate;
    }

    /**
     * Getter for the lterKeyStoreType class field.
     *
     * @return the lterKeyStoreType class field.
     */
    public static String getLterKeyStoreType() {
      return lterKeyStoreType;
    }

    /**
     * Getter for the lterKeyStoreAlias class field.
     *
     * @return the lterKeyStoreAlias class field.
     */
    public static String getLterKeyStoreAlias() {
      return lterKeyStoreAlias;
    }

    /**
     * Getter for the lterStorePasswd class field.
     *
     * @return the lterStorePasswd class field.
     */
    public static String getLterStorePasswd() {
      return lterStorePasswd;
    }

    /**
     * Getter for the lterKeyPasswd class field.
     *
     * @return the lterKeyPasswd class field.
     */
    public static String getLterKeyPasswd() {
      return lterKeyPasswd;
    }

    /**
     * Getter for the signatureDir class field.
     *
     * @return the signatureDir class field.
     */
    public static String getSignatureDir() {
      return signatureDir;
    }

  /**
     * This method can be used to execute code when the web application shuts
     * down.
     * 
     * @param servletContextEvent
     *            The ServletContextEvent object.
     */
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    /**
     * Run initialization code when at web application start-up.
     * 
     * @param servletContextEvent
     *            The ServletContextEvent object.
     */
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        ServletContext context = servletContextEvent.getServletContext();
        String CONFIG_DIR = context.getInitParameter("CONFIG_DIR");
        String realPath = context.getRealPath(CONFIG_DIR);
        try {
            setConfigDir(realPath);
            initialize();
        }
        catch (RuntimeException e) {
            logger.fatal("Initialization of Gatekeeper failed.", e);
            throw e;
        }
    }

    /**
     * Initialize all Properties files and prepare retrievable values.
     * 
     * @param dir
     *            The directory containing properties files.
     */
    public void initialize(String dir) {

        // necessary for JUnit tests
        configDir = new File(dir);
        initialize();
    }

    /**
     * Initialize all Properties files and prepare retrievable values with
     * default configuration directory.
     */
    public void initialize() {

        setLog4jProperties();
        setPropertiesFile();
        Properties prop = loadPropertiesFile();
        setTokenName(prop);
        setTokenTtl(prop);
        setVersionString(prop);
        setAuthGroup(prop);
        setPublicUser(prop);
        setLdapKeyStore(prop);
        setPrivateKey(prop);
        setLterKeyStore(prop);
        setLterCertificate(prop);
        setLterKeyStoreType(prop);
        setLterKeyStoreAlias(prop);
        setLterStorePasswd(prop);
        setLterKeyPasswd(prop);
        setSignatureDir(prop);
    }

    private Properties loadPropertiesFile() {

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return properties;
    }

    private void setTokenName(Properties p) {

        tokenName = p.getProperty(TOKEN_NAME);
        if (tokenName == null)
            throw new IllegalArgumentException(TOKEN_NAME + " not specified");
    }

    private void setTokenTtl(Properties p) {

        tokenTtl = new Long(p.getProperty(TOKEN_TTL));
        if (tokenTtl == -1)
            throw new IllegalArgumentException(TOKEN_TTL + " not specified");
    }

    private void setVersionString(Properties p) {

        webServiceVersion = p.getProperty(WEB_SERVICE_VERSION);
        if (webServiceVersion == null)
            throw new IllegalArgumentException(WEB_SERVICE_VERSION
                                               + " not specified");
    }

    private void setAuthGroup(Properties p) {

        authGroup = p.getProperty(AUTH_GROUP);
        if (authGroup == null)
            throw new IllegalArgumentException(AUTH_GROUP + " not specified");
    }

    private void setPublicUser(Properties p) {

        publicUser = p.getProperty(PUBLIC_USER);
        if (publicUser == null)
            throw new IllegalArgumentException(publicUser + " not specified");
    }

    private void setPropertiesFile() {

        propertiesFile = FileUtility.assertCanRead(new File(getConfigDir(),
                                                   PROPERTIES));
    }

    private void setLdapKeyStore(Properties p) {

        String fileName = p.getProperty(LDAP_KEY_STORE);
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException(LDAP_KEY_STORE
                    + " not specified properly");
        }

        ldapKeyStore = FileUtility.assertCanRead(new File(configDir, fileName));
    }

    private void setPrivateKey(Properties p) {

        String fileName = p.getProperty(PRIVATE_KEY);

        File f = null;
        try {
            f = FileUtility.assertCanRead(new File(configDir, fileName));
            privateKey = SymmetricEncrypter.readKey(f);
        }
        catch (UserErrorException e) {
            privateKey = SymmetricEncrypter.makeKey();
            SymmetricEncrypter.writeKey(privateKey, f);
        }

    }

    private void setLterKeyStore(Properties p) {

      String fileName = p.getProperty(LTER_KEYSTORE);
      if (fileName == null || fileName.isEmpty()) {
        throw new IllegalArgumentException(LTER_KEYSTORE
                                               + " not specified properly");
      }

      lterKeyStore = FileUtility.assertCanRead(new File(configDir, fileName));
    }

    private void setLterCertificate(Properties p) {

      String fileName = p.getProperty(LTER_CERTIFICATE);
      if (fileName == null || fileName.isEmpty()) {
        throw new IllegalArgumentException(LTER_CERTIFICATE
                                               + " not specified properly");
      }

      lterCertificate = FileUtility.assertCanRead(new File(configDir, fileName));
    }

    private void setLterKeyStoreType(Properties p) {

      lterKeyStoreType = p.getProperty(LTER_KEYSTORE_TYPE);
      if (lterKeyStoreType == null)
        throw new IllegalArgumentException(lterKeyStoreType + " not specified");
    }

    private void setLterKeyStoreAlias(Properties p) {

      lterKeyStoreAlias = p.getProperty(LTER_KEYSTORE_ALIAS);
      if (lterKeyStoreAlias == null)
        throw new IllegalArgumentException(lterKeyStoreAlias + " not specified");
    }

    private void setLterStorePasswd(java.util.Properties p) {

      lterStorePasswd = p.getProperty(LTER_KEY_PASSWD);
      if (lterStorePasswd == null)
        throw new IllegalArgumentException(lterStorePasswd + " not specified");
    }

    private void setLterKeyPasswd(java.util.Properties p) {
  
      lterKeyPasswd = p.getProperty(LTER_STORE_PASSWD);
      if (lterKeyPasswd == null)
        throw new IllegalArgumentException(lterKeyPasswd + " not specified");
    }

    private void setSignatureDir(java.util.Properties p) {
  
      signatureDir = p.getProperty(SIGNATURE_DIR);
      if (signatureDir == null)
        throw new IllegalArgumentException(signatureDir + " not specified");
    }
 
    private void setLog4jProperties() {

        File properties = new File(getConfigDir(), "log4j.properties");
        PropertyConfigurator.configureAndWatch(properties.getAbsolutePath());
    }

    private void setConfigDir(String realPath) {
        configDir = FileUtility.assertCanRead(realPath);
    }

}
