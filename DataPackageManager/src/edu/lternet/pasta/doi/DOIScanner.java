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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.ISO8601Utility;
import edu.lternet.pasta.common.security.authorization.AccessMatrix;
import edu.lternet.pasta.common.security.authorization.Rule;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.common.security.token.BasicAuthToken;

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @since Nov 9, 2012
 * 
 *        Scans the Data Package resource registry and performs DOI registration
 *        for those resources lacking DOIs.
 * 
 */
public class DOIScanner {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
	    .getLogger(edu.lternet.pasta.doi.DOIScanner.class);

	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static final String LEVEL1NAME = "Level-1-EML.xml";
	private static final String PUBLIC = "public";

	/*
	 * Instance variables
	 */

	private String dbDriver = null;
	private String dbURL = null;
	private String dbUser = null;
	private String dbPassword = null;
	private String metadataDir = null;
	private String doiTest = null;
	private String ezidBaseUrl = null;
	private String ezidStageUrl = null;

	/*
	 * Constructors
	 */

	/**
	 * Creates a new DOI scanning instance to scan the Data Package Manager for
	 * resources without DOIs.
	 * 
	 * @throws SQLException
	 */
	public DOIScanner() throws ConfigurationException {

		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		this.loadOptions(options);

	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

	/**
	 * Loads Data Manager options from a configuration file.
	 * 
	 * @param options
	 *          Configuration options object.
	 */
	private void loadOptions(Options options) throws ConfigurationException {

		if (options != null) {

			// Load database connection options
			this.dbDriver = options.getOption("dbDriver");
			this.dbURL = options.getOption("dbURL");
			this.dbUser = options.getOption("dbUser");
			this.dbPassword = options.getOption("dbPassword");

			// Load DOI options
			this.doiTest = options.getOption("datapackagemanager.doiTest");
			this.ezidBaseUrl = options.getOption("datapackagemanager.ezidBaseUrl");
			this.ezidStageUrl = options.getOption("datapackagemanager.ezidStageUrl");

			// Load PASTA service options
			this.metadataDir = options.getOption("datapackagemanager.metadataDir");

		} else {
			throw new ConfigurationException("Configuration options failed to load.");
		}

	}

	/**
	 * Scans the Data Package Manager resource registry for resources that are (1)
	 * not deactivated (not deleted), (2) publicly accessible and (3) do not have
	 * a DOI. Resources that meet these criteria have a DataCite DOI registered to
	 * them on their behalf.
	 */
	public void doScan() throws DOIException {

		ArrayList<Resource> resourceList = null;

		EzidRegistrar ezidRegistrar = null;
		
		try {
			ezidRegistrar = new EzidRegistrar();
		} catch (ConfigurationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new DOIException(e.getMessage());
		}

		try {
			resourceList = this.getDoiResourceList();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new DOIException(e.getMessage());
		}

		File emlFile = null;
		EmlObject emlObject = null;
		String resourceUrl = null;
		String publicationYear = null;
		ArrayList<Creator> creators = null;
		ArrayList<Title> titles = null;
		DigitalObjectIdentifier identifier = null;
		ResourceType resourceType = null;
		AlternateIdentifier alternateIdentifier = null;
		Date time = null;
		String doi = null;

		// For all resources without a registered DOI
		for (Resource resource : resourceList) {

			// Build EML document object
			emlFile = new File(this.getEmlFilePath(resource.getPackageId()));
			emlObject = new EmlObject(emlFile);

			// Set local metadata attributes
			resourceUrl = resource.getResourceId();
			publicationYear = this.getResourceCreateYear(resource.getDateCreated());
			creators = emlObject.getCreators();
			titles = emlObject.getTitles();

			// If DOI testing, add salt to resource identifier to create unique DOI
			// so subsequent tests will not result in EZID create errors.
			if (this.doiTest.equals("true")) {
				time = new Date();
				Long salt = time.getTime();
				//identifier = new DigitalObjectIdentifier(resource.getResourceId()
				//    + salt.toString());
				identifier = new DigitalObjectIdentifier(resource.getResourceId());
			} else {
				identifier = new DigitalObjectIdentifier(resource.getResourceId());
			}

			resourceType = new ResourceType(ResourceType.DATASET);
			resourceType.setResourceType(resource.getResourceType());
			alternateIdentifier = new AlternateIdentifier(AlternateIdentifier.URL);
			alternateIdentifier.setAlternateIdentifier(resource.getResourceId());

			// Create and populate the DataCite metadata object
			DataCiteMetadata dataCiteMetadata = new DataCiteMetadata();

			dataCiteMetadata.setLocationUrl(resourceUrl);
			dataCiteMetadata.setPublicationYear(publicationYear);
			dataCiteMetadata.setCreators(creators);
			dataCiteMetadata.setTitles(titles);
			dataCiteMetadata.setDigitalObjectIdentifier(identifier);
			dataCiteMetadata.setResourceType(resourceType);
			dataCiteMetadata.setAlternateIdentifier(alternateIdentifier);

			// Set and register DOI with DatCite metadata
			ezidRegistrar.setDataCiteMetadata(dataCiteMetadata);
			
			try {
	      ezidRegistrar.registerDataCiteMetadata();
      } catch (EzidException e) {
	      logger.error(e.getMessage());
	      e.printStackTrace();
	  
	      /*
	       * In the event that a DOI registration succeeded with EZID,
	       * but failed to be recorded in the resource registry, the following
	       * exception allows the resource registry to be updated with the
	       * DOI string.
	       */

	      if (e.getMessage().equals("identifier already exists")) {
	      	logger.warn("Proceeding with resource registry update...");
	      } else {
					throw new DOIException(e.getMessage());	      	
	      }
	      
      }

			// Update Data Package Manager resource registry with DOI
			doi = dataCiteMetadata.getDigitalObjectIdentifier().getDoi();
      this.updateRegistryDoi(resourceUrl, doi);

		}

	}

	/**
	 * Returns a connection to the database.
	 * 
	 * @return conn The database Connection object
	 */
	protected Connection getConnection() throws ClassNotFoundException {
		Connection conn = null;
		SQLWarning warn;

		// Load the jdbc driver
		try {
			Class.forName(dbDriver);
		} catch (ClassNotFoundException e) {
			logger.error("Can't load driver " + e.getMessage());
			throw (e);
		}

		// Make the database connection
		try {
			conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);

			// If a SQLWarning object is available, print its warning(s).
			// There may be multiple warnings chained.
			warn = conn.getWarnings();

			if (warn != null) {
				while (warn != null) {
					logger.warn("SQLState: " + warn.getSQLState());
					logger.warn("Message:  " + warn.getMessage());
					logger.warn("Vendor: " + warn.getErrorCode());
					warn = warn.getNextWarning();
				}
			}
		} catch (SQLException e) {
			logger.error("Database access failed " + e);
		}

		return conn;

	}

	/**
	 * Returns an array list of resources that are both publicly accessible and
	 * lacking DOIs.
	 * 
	 * @return Array list of resources
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	protected ArrayList<Resource> getDoiResourceList() throws SQLException {

		ArrayList<Resource> resourceList = new ArrayList<Resource>();

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "SELECT resource_id, resource_type, package_id, date_created"
		    + " FROM datapackagemanager.resource_registry WHERE"
		    + " doi IS NULL and date_deactivated IS NULL;";

		Statement stat = null;

		try {

			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);
			String resourceId = null;

			while (result.next()) {

				Resource resource = new Resource();

				// Test here for resource public accessibility before adding to list

				resourceId = result.getString("resource_id");

				if (isPublicAccessible(resourceId)) {

					resource.setResourceId(resourceId);
					resource.setResourceType(result.getString("resource_type"));
					resource.setPackageId(result.getString("package_id"));
					resource.setDateCreate(result.getString("date_created"));

					resourceList.add(resource);

				}

			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			conn.close();
		}

		return resourceList;

	}

	/**
	 * Update the DOI field of the Data Package Manager resource registry with
	 * DOIs for the resource identified by the resource identifier.
	 *  
	 * @param resourceId The resource identifier of the resource to be updated
	 * @param doi The DOI of the resource
	 * @throws Exception
	 */
	protected void updateRegistryDoi(String resourceId, String doi)
	    throws DOIException {

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "UPDATE datapackagemanager.resource_registry"
		    + " SET doi='" + doi + "' WHERE resource_id='" + resourceId + "';";

		Statement stat = null;
		Integer rowCount = null;

		try {
			stat = conn.createStatement();
			rowCount = stat.executeUpdate(queryString);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		if (rowCount != 1) {
			String gripe = "updateRegistryDoi: failed to update DOI in resource registry.";
			throw new DOIException(gripe);
		}

	}

	/**
	 * Determines whether the given resource is publicly accessible.
	 * 
	 * @param resourceId
	 * @return Is publicly accessible
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	protected Boolean isPublicAccessible(String resourceId) throws SQLException {

		Boolean publicAccessible = false;

		ArrayList<Rule> ruleList = new ArrayList<Rule>();

		Connection conn = null;

		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "SELECT resource_id, principal, access_type, "
		    + "access_order, permission FROM datapackagemanager.access_matrix WHERE"
		    + " resource_id='" + resourceId + "';";

		Statement stat = null;

		try {

			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);

			while (result.next()) {

				Rule rule = new Rule();

				rule.setPrincipal(result.getString("principal"));
				rule.setAccessType(result.getString("access_type"));
				rule.setOrder(result.getString("access_order"));
				rule.setPermission((Rule.Permission) Enum.valueOf(
				    Rule.Permission.class, result.getString("permission")));

				ruleList.add(rule);

			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			conn.close();
		}

		String tokenString = BasicAuthToken.makeTokenString(DOIScanner.PUBLIC,
		    DOIScanner.PUBLIC);
		AuthToken authToken = AuthTokenFactory
		    .makeAuthTokenWithPassword(tokenString);

		AccessMatrix accessMatrix = new AccessMatrix(ruleList);
		Rule.Permission permission = (Rule.Permission) Enum.valueOf(
		    Rule.Permission.class, Rule.READ);
		publicAccessible = accessMatrix.isAuthorized(authToken, null, permission);

		return publicAccessible;

	}

	/**
	 * Returns file system path to the Level-1 EML document for the given package
	 * identifier.
	 * 
	 * @param packageId
	 *          Level-1 EML package identifier.
	 * @return File system path to Level-1 EML document
	 */
	private String getEmlFilePath(String packageId) {

		return this.metadataDir + "/" + packageId + "/" + LEVEL1NAME;

	}

	private String getResourceCreateYear(String createDate) {
		String year = null;

		String[] dateParts = createDate.split("-");
		year = dateParts[0];

		return year;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DOIScanner doiScanner = null;
	
		try {
	    doiScanner = new DOIScanner();
			doiScanner.doScan();
    } catch (ConfigurationException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
    } catch (DOIException e) {
    	logger.error(e.getMessage());
	    e.printStackTrace();
    }

	}

}
