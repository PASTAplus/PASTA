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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.eml.EmlObject;
import edu.lternet.pasta.common.eml.ResponsibleParty;
import edu.lternet.pasta.common.eml.Title;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;
import edu.lternet.pasta.datapackagemanager.JournalCitation;
import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @author dcosta
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

	/*
	 * Instance variables
	 */

	private String dbDriver = null;
	private String dbURL = null;
	private String dbUser = null;
	private String dbPassword = null;
	private Registrar registrar = null;
	private String metadataDir = null;
	private String doiUrlHeadEDI = null;
	private String doiUrlHeadLTER = null;
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

		Options options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		loadOptions(options);
		
		if (this.doiTest.equalsIgnoreCase(TRUE)) {
			this.setDoiTest(true);
		} else {
			this.setDoiTest(false);
		}
		
		try {
			String doiProvider = options.getOption("datapackagemanager.doiProvider");

			if (doiProvider == null) {
				throw new ConfigurationException("No property value specified for datapackagemanager.doiProvider");
			}
			else if (doiProvider.equalsIgnoreCase("ezid")) {
				registrar = new EzidRegistrar();
			}
			else if (doiProvider.equalsIgnoreCase("datacite")) {
				registrar = new DataCiteRegistrar();
			}
			else {
				throw new ConfigurationException("Unsupported property value specified for datapackagemanager.doiProvider: " + doiProvider);
			}
		} 
		catch (ConfigurationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw(e);
		}

		dataPackageRegistry = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);

	}

	/*
	 * Class methods
	 */
	
	public static void main(String[] args) {
		try {
			DOIScanner doiScanner = new DOIScanner();
			doiScanner.doScanToRegister();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Instance methods
	 */
	
	
	public DataPackageRegistry getDataPackageRegistry() {
	    return this.dataPackageRegistry;
	}

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

			this.doiUrlHeadEDI = options.getOption("datapackagemanager.doiUrlHead.edi");
			this.doiUrlHeadLTER = options.getOption("datapackagemanager.doiUrlHead.lter");
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
	public void doScanToRegister() throws Exception {

		ArrayList<Resource> resourceList = null;

		try {
			resourceList = dataPackageRegistry.listDoilessResources();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new DOIException(e.getMessage());
		}

		// For all resources without a registered DOI
		for (Resource resource : resourceList) {
			processOneResource(resource);
		}

	}
	
	
	/**
	 * Boolean to determine whether a data package is an LTER data package based on its scope value.
	 * 
	 * @param scope   the data package scope value
	 * @return  true if an LTER data package, else false
	 * @throws DOIException
	 */
	public boolean isLTERScope(String scope) 
			throws DOIException {
		boolean isLTERScope = false;
		
		if (scope != null) {
			if (scope.equals("ecotrends") ||
				scope.startsWith("knb-lter-") ||
				scope.startsWith("lter-landsat")
			   ) {
				isLTERScope = true;
			}
		}
		else {
			throw new DOIException("The resource has a null scope value.");
		}
		
		return isLTERScope;
	}
	
	
	private String scopeFromPackageId(String packageId)
			throws DOIException {
		String scope = null;
		
		if (packageId != null && packageId.contains(".")) {
			scope = packageId.substring(0, packageId.indexOf('.'));
		}
		else {
			throw new DOIException("The resource has a null or invalid packageId value.");
		}
		
		return scope;
	}
	
	
	
	private String md5IdFromDoi(String doi) {
	    String md5Id = null;
	    
	    if (doi != null) {
	        md5Id = doi.substring(doi.lastIndexOf('/') + 1);
	    }
	    
	    return md5Id;
	}

	
	/**
	 * Processes a single resource for DOI registration.
	 * 
	 * @param resource    the Resource to be registered
	 * @return doi        the DOI value that was registered
	 * 
	 * @throws DOIException
	 */
	public String processOneResource(Resource resource) throws Exception {

		File emlFile = null;
		EmlObject emlObject = null;
		String resourceId = null;
		String doiUrl = null;
		String publicationYear = null;
		ArrayList<ResponsibleParty> creators = null;
		ArrayList<Title> titles = null;
		DigitalObjectIdentifier digitalObjectIdentifier = null;
		ResourceType resourceType = null;
		AlternateIdentifier alternateIdentifier = null;
		Date time = null;
		String doi = resource.getDoi();
        String packageId = resource.getPackageId();

			// Build EML document object
			emlFile = new File(this.getEmlFilePath(packageId));
			
			if (emlFile.exists()) {
				emlObject = new EmlObject(emlFile);

				// Set local metadata attributes
				resourceId = resource.getResourceId();
				String doiUrlHead = this.doiUrlHeadEDI;
				String scope = scopeFromPackageId(packageId);
				if (isLTERScope(scope)) {
					doiUrlHead = this.doiUrlHeadLTER;
				}
				doiUrl = doiUrlHead + packageId;
				logger.info("DOI landing page URL will be set to: " + doiUrl);
				publicationYear = this.getResourceCreateYear(resource.getDateCreated());
				creators = emlObject.getCreators();
				titles = emlObject.getTitles();

				String md5Id = null;
				if (doi != null) {
				    md5Id = md5IdFromDoi(doi);
				} 
				else {
				    // If DOI testing, add salt to resource identifier to create unique DOI
                    // so subsequent tests will not result in DOI create errors.
				    if (this.isDoiTest) {
				        time = new Date();
				        Long salt = time.getTime();
				        md5Id = DigestUtils.md5Hex(resourceId + salt.toString());
				    } 
				    else {
				        md5Id = DigestUtils.md5Hex(resourceId);
				    }
				}
				
                digitalObjectIdentifier = new DigitalObjectIdentifier(md5Id);

				resourceType = new ResourceType(ResourceType.DATASET);
				resourceType.setResourceType(resource.getResourceType());
				alternateIdentifier = new AlternateIdentifier(AlternateIdentifier.URL);
				alternateIdentifier.setAlternateIdentifier(resourceId);

				// Create and populate the DataCite metadata object
				DataCiteMetadata dataCiteMetadata = new DataCiteMetadata();

				dataCiteMetadata.setLocationUrl(doiUrl);
				dataCiteMetadata.setPublicationYear(publicationYear);
				dataCiteMetadata.setCreators(creators);
				dataCiteMetadata.setTitles(titles);
				dataCiteMetadata.setDigitalObjectIdentifier(digitalObjectIdentifier);
				dataCiteMetadata.setResourceType(resourceType);
				dataCiteMetadata.setAlternateIdentifier(alternateIdentifier);
				
				/*
				 * Find all the journal citations for this data package and tell the
				 * DataCite metadata object to include them as relatedIdentifiers.
				 */
				ArrayList<JournalCitation> journalCitations = dataPackageRegistry.listDataPackageCitations(packageId);
				dataCiteMetadata.addJournalCitations(journalCitations);

				try {
					doi = dataCiteMetadata.getDigitalObjectIdentifier().getDoi();
					registrar.registerDataCiteMetadata(dataCiteMetadata);
				} 
				catch (RegistrarException e) {
					/*
					 * In the event that a DOI registration succeeded, but
					 * failed to be recorded in the resource registry, the following
					 * exception allows the resource registry to be updated with the DOI
					 * string.
					 */

					if (e.getMessage().equals("identifier already exists")) {
						String msg = String.format(
                            "%s already exists in DOI registry but it needs to be updated in the resource registry for %s.", 
                            doi, resource.getPackageId()
                        );
						logger.warn(msg + "  Proceeding with resource registry update...");
					} 
					else {
						logger.error(e.getMessage());
						e.printStackTrace();
						doi = null;
					}
				}

				if (doi != null) {
					// Update Data Package Manager resource registry with DOI
					try {
						dataPackageRegistry.addResourceDoi(resourceId, doi);
					} 
					catch (SQLException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
						throw new DOIException(e.getMessage());
					}
				}
			} 
			else {
				String gripe = "doScanToRegister: Level-1-EML.xml file does not exist for "
				    + resource.getPackageId();
				logger.error(gripe);
			}
			
		return doi;
	}

	
	/**
	 * Scans the Data Package Manager resource registry for resources that have
	 * both (1) a DOI and (2) a deactivated date - indicating that the resource
	 * has been obsoleted. Resources that meet these criteria are made
	 * "unavailable" through EZID.
	 * 
	 * Note: This method, and calls to this method, have been commented-out 
	 *       because this logic is no longer considered valid as of Ticket #912:
	 *       
	 *       https://trac.lternet.edu/trac/NIS/ticket/912
	 * 
	 * @throws DOIException
	 *
	public void doScanToObsolete() throws DOIException {

		ArrayList<String> doiList = null;

		Registrar registrar = null;

		try {
			registrar = new DataCiteRegistrar();
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
				registrar.obsoleteDoi(doi);
			} catch (EzidException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			
			dataPackageRegistry.deleteResourceDoi(doi);

		}

	}
	*/
	
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
