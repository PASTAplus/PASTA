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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;

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
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	/*
	 * Instance variables
	 */

	private String dbDriver = null;
	private String dbURL = null;
	private String dbUser = null;
	private String dbPassword = null;
	private String metadataDir = null;
	private String doiUrlHead = null;
	private String doiTest = null;
	private Boolean isDoiTest = null;
	
	private DataPackageRegistry dataPackageRegistry = null;
	
	/*
	 * Constructors
	 */

	/**
	 * Creates a new DOI scanning instance to scan the Data Package Manager for
	 * resources without DOIs.
	 * @throws ClassNotFoundException 
	 * 
	 * @throws SQLException
	 */
	public DOIScanner() throws ConfigurationException, ClassNotFoundException, SQLException {

		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		this.loadOptions(options);
		
		if (this.doiTest.equalsIgnoreCase(TRUE)) {
			this.setDoiTest(true);
		} else {
			this.setDoiTest(false);
		}
		
		dataPackageRegistry = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);

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

			this.doiUrlHead = options.getOption("datapackagemanager.doiUrlHead");
			this.doiTest = options.getOption("datapackagemanager.doiTest");

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
	 * 
	 * @throws DOIException
	 */
	public void doScanToRegister() throws DOIException {

		ArrayList<Resource> resourceList = null;

		EzidRegistrar ezidRegistrar = null;

		try {
			ezidRegistrar = new EzidRegistrar();
			if (this.isDoiTest) {
				ezidRegistrar.setDoiTest(true);
			} else {
				ezidRegistrar.setDoiTest(false);
			}
		} catch (ConfigurationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new DOIException(e.getMessage());
		}

		try {
			resourceList = dataPackageRegistry.listDoilessResources();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new DOIException(e.getMessage());
		}

		File emlFile = null;
		EmlObject emlObject = null;
		String resourceUrl = null;
		String doiUrl = null;
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
			doiUrl = this.doiUrlHead + resource.getPackageId();
			publicationYear = this.getResourceCreateYear(resource.getDateCreated());
			creators = emlObject.getCreators();
			titles = emlObject.getTitles();

			// If DOI testing, add salt to resource identifier to create unique DOI
			// so subsequent tests will not result in EZID create errors.
			if (this.isDoiTest) {
				time = new Date();
				Long salt = time.getTime();
				identifier = new DigitalObjectIdentifier(resource.getResourceId()
				+ salt.toString());
			} else {
				identifier = new DigitalObjectIdentifier(resource.getResourceId());
			}

			resourceType = new ResourceType(ResourceType.DATASET);
			resourceType.setResourceType(resource.getResourceType());
			alternateIdentifier = new AlternateIdentifier(AlternateIdentifier.URL);
			alternateIdentifier.setAlternateIdentifier(resource.getResourceId());

			// Create and populate the DataCite metadata object
			DataCiteMetadata dataCiteMetadata = new DataCiteMetadata();

			dataCiteMetadata.setLocationUrl(doiUrl);
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
				doi = dataCiteMetadata.getDigitalObjectIdentifier().getDoi();
				
			} catch (EzidException e) {
				
				/*
				 * In the event that a DOI registration succeeded with EZID, but failed
				 * to be recorded in the resource registry, the following exception
				 * allows the resource registry to be updated with the DOI string.
				 */

				if (e.getMessage().equals("identifier already exists")) {
					logger.warn("Proceeding with resource registry update...");
				} else {
					logger.error(e.getMessage());
					e.printStackTrace();
					doi = null;
				}

			}

			if (doi != null) {
				// Update Data Package Manager resource registry with DOI
				try {
					dataPackageRegistry.addResourceDoi(resourceUrl, doi);
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
					throw new DOIException(e.getMessage());
				}
			}

		}

	}

	/**
	 * Scans the Data Package Manager resource registry for resources that have
	 * both (1) a DOI and (2) a deactivated date - indicating that the resource
	 * has been obsoleted. Resources that meet these criteria are made
	 * "unavailable" through EZID.
	 * 
	 * @throws DOIException
	 */
	public void doScanToObsolete() throws DOIException {

		ArrayList<String> doiList = null;

		EzidRegistrar ezidRegistrar = null;

		try {
			ezidRegistrar = new EzidRegistrar();
			if (this.isDoiTest) {
				ezidRegistrar.setDoiTest(true);
			}
		} catch (ConfigurationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new DOIException(e.getMessage());
		}

		try {
			doiList = dataPackageRegistry.listObsoleteDois();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new DOIException(e.getMessage());
		}

		// Obsolete all EZID and resource registry DOIs
		for (String doi : doiList) {
			
			logger.info("DOI to obsolete: " + doi);
			
			try {
				ezidRegistrar.obsoleteDoi(doi);
			} catch (EzidException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			
			dataPackageRegistry.deleteResourceDoi(doi);

		}

	}
	
	/**
	 * Explicitly set whether DOI testing is enabled.
	 * 
	 * @param isDoiTest Boolean test flag
	 */
	public void setDoiTest(Boolean isDoiTest) {
		if (isDoiTest) {
			this.isDoiTest = true;			
		} else {
			this.isDoiTest = false;
		}
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

}
