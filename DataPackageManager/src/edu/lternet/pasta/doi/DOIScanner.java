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

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.ISO8601Utility;

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

	private static final String dirPath = "WebRoot/WEB-INF/conf";
	
	private static final String LEVEL1NAME = "Level-1-EML.xml";

	/*
	 * Instance variables
	 */

	private Logger logger = Logger.getLogger(DOIScanner.class);

	private String dbDriver = null;
	private String dbURL = null;
	private String dbUser = null;
	private String dbPassword = null;
	private String databaseAdapterName = null;
	private String metadataDir = null;
	private String pastaUriHead = null;

	/*
	 * Constructors
	 */

	public DOIScanner() throws SQLException {

		Options options;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		this.loadOptions(options);

		ArrayList<Resource> resourceList = null;

		try {

			Connection conn = this.getConnection();

			try {
				resourceList = this.getDoiLessResourceList(conn);
			} finally {
				conn.close();
			}

		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		File emlFile = null;
		EmlObject emlObject = null;
		String resourceUrl = null;
		String publicationYear = null;
		ArrayList<Creator> creators = null;
		ArrayList<Title> titles = null;
		DigitalObjectIdentifier identifier = null;
		ResourceType resourceType = null;
		
		// For all resources without a registered DOI
		for (Resource resource : resourceList) {

			// Build EML document object
			emlFile = new File(this.getEmlFilePath(resource.getPackageId()));
			emlObject = new EmlObject(emlFile);
			
			// Set local metadata attributes
			resourceUrl = resource.getResourceId();
			publicationYear = ISO8601Utility.formatYear();
			creators = emlObject.getCreators();
			titles = emlObject.getTitles();
			identifier = new DigitalObjectIdentifier(resource.getResourceId());
			resourceType = new ResourceType(ResourceType.DATASET);
			resourceType.setResourceType(resource.getResourceType());
			
			DataCiteMetadata dataCiteMetadata = new DataCiteMetadata();
			
			dataCiteMetadata.setLocationUrl(resourceUrl);
			dataCiteMetadata.setPublicationYear(publicationYear);
			dataCiteMetadata.setCreators(creators);
			dataCiteMetadata.setTitles(titles);
			dataCiteMetadata.setDigitalObjectIdentifier(identifier);
			dataCiteMetadata.setResourceType(resourceType);
			
			System.out.print(dataCiteMetadata.getLocationUrl() + " ");
			System.out.print(dataCiteMetadata.getPublicationYear() + " ");
			System.out.println(dataCiteMetadata.getDigitalObjectIdentifier().getDoi());
			
			// register DOI
			// set DOI to resource registry
			// if yes - ignore

		}

	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

	/**
	 * Loads Data Manager options from a configuration file.
	 */
	private void loadOptions(Options options) {

		if (options != null) {

			// Load database connection options
			dbDriver = options.getOption("dbDriver");
			dbURL = options.getOption("dbURL");
			dbUser = options.getOption("dbUser");
			dbPassword = options.getOption("dbPassword");
			databaseAdapterName = options.getOption("dbAdapter");

			// Load PASTA service options
			metadataDir = options.getOption("datapackagemanager.metadataDir");
			pastaUriHead = options.getOption("datapackagemanager.pastaUriHead");

		} else {
			System.out.println("Configuration options failed to load.");
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

	protected ArrayList<Resource> getDoiLessResourceList(Connection conn)
	    throws SQLException {

		ArrayList<Resource> resourceList = new ArrayList<Resource>();
		String resourceId = null;
		String resourceType = null;

		String queryString = "SELECT resource_id, resource_type, package_id FROM "
		    + " datapackagemanager.resource_registry WHERE" + " md5_id IS NULL;";

		Statement stat = conn.createStatement();
		ResultSet result = stat.executeQuery(queryString);

		while (result.next()) {

			Resource resource = new Resource();

			resource.setResourceId(result.getString("resource_id"));
			resource.setResourceType(result.getString("resource_type"));
			resource.setPackageId(result.getString("package_id"));

			resourceList.add(resource);

		}

		return resourceList;

	}
	
	/**
	 * Returns file system path to the Level-1 EML document for the given
	 * package identifier.
	 * 
	 * @param packageId  Level-1 EML package identifier.
	 * @return File system path to Level-1 EML document
	 */
	private String getEmlFilePath(String packageId) {
		
		return this.metadataDir + "/" + packageId + "/" + LEVEL1NAME;
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			DOIScanner doiScanner = new DOIScanner();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		System.out.println("I am here!");

	}

}
