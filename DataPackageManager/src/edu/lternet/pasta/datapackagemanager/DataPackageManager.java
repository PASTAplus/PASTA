/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011 the University of New Mexico.
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

package edu.lternet.pasta.datapackagemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.lternet.pasta.dml.DataManager;
import edu.lternet.pasta.dml.database.ConnectionNotAvailableException;
import edu.lternet.pasta.dml.database.DatabaseConnectionPoolInterface;
import edu.lternet.pasta.dml.download.DownloadHandler;
import edu.lternet.pasta.dml.parser.DataPackage;
import edu.lternet.pasta.dml.quality.QualityReport;

import edu.lternet.pasta.common.DataPackageUpload;
import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.ResourceDeletedException;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.eml.DataPackage.DataDescendant;
import edu.lternet.pasta.common.eml.DataPackage.DataSource;
import edu.lternet.pasta.common.eml.EMLParser;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.authorization.AccessMatrix;
import edu.lternet.pasta.common.security.authorization.Rule;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.datamanager.EMLDataManager;
import edu.lternet.pasta.datamanager.StorageManager;
import edu.lternet.pasta.datapackagemanager.checksum.DigestUtilsWrapper;
import edu.lternet.pasta.datapackagemanager.dc.DublinCore;
import edu.lternet.pasta.datapackagemanager.ore.ResourceMap;
import edu.lternet.pasta.doi.DOIException;
import edu.lternet.pasta.doi.DOIScanner;
import edu.lternet.pasta.doi.Resource;
import edu.lternet.pasta.metadatamanager.MetadataCatalog;
import edu.lternet.pasta.metadatamanager.SolrMetadataCatalog;
import edu.ucsb.nceas.utilities.IOUtil;
import edu.ucsb.nceas.utilities.Options;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * @author dcosta
 * @version 1.0
 * @created 16-Aug-2011 1:40:03 PM
 * 
 *          The DataPackageManager class is the top-level controller for all the
 *          basic services offered by the Data Package Manager web service.
 */
public class DataPackageManager implements DatabaseConnectionPoolInterface {

	private static final String LEVEL_ONE_FILE_NAME = "Level-1-EML.xml";
	private static final String LEVEL_ZERO_FILE_NAME = "Level-0-EML.xml";
	private static final String DOI_SYSTEM_VALUE = "https://doi.org";

	public enum DataPackageManagerAction {
		CREATE, READ, UPDATE, DELETE, SEARCH
	}

	public enum ResourceType {
		archive, data, dataPackage, metadata, report
	}
	
	
	/*
	 * Class methods
	 */
	
	  /**
	   * Examine a data source URL to determine whether it originates from PASTA.
	   * This is helpful in determining provenance.
	   * 
	   * @param dataSourceURL
	   * @return true if the data source is hosted by PASTA, else false.
	   */
	public static boolean isPastaDataSource(String dataSourceURL) {
		if (dataSourceURL == null) {
			return false;
		}
		final String patternString = "http[s]?\\://pasta[-]?[ds]?\\.lternet\\.edu/.*";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(dataSourceURL);
		return (matcher.matches());
	}	  
	  
	  /*
	   * Composes a data package metadata resource identifier based on the PASTA URI
	   * head value and a specific packageId value.
	   */
	  public static String packageIdToMetadataResourceId(String packageId) {
	    String resourceId = null;
	    final String SLASH = "/";
	    
	    if (pastaUriHead != null) {
	      EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
	      
	      try {
	        EmlPackageId emlPackageId = emlPackageIdFormat.parse(packageId);
	        String scope = emlPackageId.getScope();
	        Integer identifier = emlPackageId.getIdentifier();
	        Integer revision = emlPackageId.getRevision();
	      
	        if (scope != null && identifier != null && revision != null) {        
	          resourceId = pastaUriHead + "metadata" + SLASH + "eml" + SLASH + 
	                       scope + SLASH + identifier + SLASH + revision;
	        } 
	      }
	      catch (IllegalArgumentException e) {
	        
	      }
	    }
	    
	    return resourceId;
	  }

	  
	  /**
	   * Composes a data package metadata path based on the parent metadata
	   * path value (as configured in the properties file as property
	   * "datapackagemanager.metadataDir") and a specific packageId value
	   * corresponding to a sub-directory directly under the parent directory.
	   * 
	   * For example:
	   *   packageIdToMetadataPath("edi.1.1") -> "/home/pasta/local/data/edi.1.1"
	   *   
	   * Note that in most PASTA servers for the DataPackageManager service, the
	   * metadata now resides in the same directory with the data entities and
	   * is configured as such.
	   * 
	   * @param  packageId  the package identifier, e.g. "edi.1.1"
	   * @return  the absolute path to the metadata directory for the specified
	   *          data package
	   */
	  public static String packageIdToMetadataPath(String packageId) {
	    String metadataPath = null;
	    
	    if (packageId != null) {
	    	String metadataRootPath = getResourceDir();
	        metadataPath = String.format("%s/%s", metadataRootPath, packageId);
	    }
	    
	    return metadataPath;
	  }

	  
		/**
		 * Converts a pastaURL string to a packageId string, or null if the pastaURL
		 * does not match the recognized PASTA url pattern.
		 * 
		 * @param pastaURL  the pastaURL string, 
		 *                  e.g. https://pasta-d.lternet.edu/package/eml/knb-lter-hbr/58/5
		 * @return the packageId string, 
		 *                  e.g. knb-lter-hbr.58.5
		 */
		public static String pastaURLtoPackageId(String pastaURL) {
			String packageId = null;

			if (pastaURL != null) {
				final String patternString = "^.*/eml/(\\S+)/(\\d+)/(\\d+)$";
				Pattern pattern = Pattern.compile(patternString);
				Matcher matcher = pattern.matcher(pastaURL);
				if (matcher.matches()) {
					String scope = matcher.group(1);
					String identifier = matcher.group(2);
					String revision = matcher.group(3);
					packageId = String.format("%s.%s.%s", scope, identifier,
							revision);
				}
			}

			return packageId;
		}	  

		
	/*
	 * Class fields
	 */

	private static String resourceDir = null;
	private static String entityDir = null;
	private static final String RESOURCE_DIR_DEFAULT = "/home/pasta/local/metadata";

	private static final String SLASH = "/";
	private static final String URI_MIDDLE_ARCHIVE = "archive/eml/";
	private static final String URI_MIDDLE_DATA = "data/eml/";
	private static final String URI_MIDDLE_DATA_PACKAGE = "eml/";
	private static final String URI_MIDDLE_METADATA = "metadata/eml/";
	private static final String URI_MIDDLE_REPORT = "report/eml/";

	/*
	 * These fields will be assigned values when the properties file is loaded.
	 */
	private static String dbDriver = null;
	private static String dbURL = null;
	private static String dbUser = null;
	private static String dbPassword = null;
	private static String databaseAdapterName = null;
	private static String pastaUriHead = null;
	private static String pastaUser = null;
	private static String solrUrl = null;
	private static String xslDir = null;

	
	private static Logger logger = Logger.getLogger(DataPackageManager.class);

	static {
		try {
		  loadOptions();
		}
		catch (Exception e) {
			logger.fatal("Error loading options: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/*
	 * Instance fields
	 */

	// An instance of the DataManager class. This object provides the
	// calling application access to all the public methods exposed by the
	// Data Manager Library API.
	private DataManager dataManager;
	private DOIScanner doiScanner;
	private String scope;
	private Integer identifier;
	private Integer revision;
	private String packageId;
	private String user;
	private AuthToken authToken;

	
	/*
	 * Constructors
	 */

	public DataPackageManager() throws Exception {
		dataManager = DataManager.getInstance(this, databaseAdapterName);
		doiScanner = new DOIScanner();
	}

	
	/*
	 * Class methods
	 */

	/**
	 * Composes a resource identifier for a given resource type.
	 */
	public static String composeResourceId(ResourceType resourceType,
	    String scope, Integer identifier, Integer revision, String entityId) {
		String resourceId = null;
		String uriDocidPart = scope + SLASH + identifier + SLASH + revision;

		if (resourceType == null) {
			throw new IllegalArgumentException("null resourceType");
		}

		switch (resourceType) {
		case archive:
			resourceId = pastaUriHead + URI_MIDDLE_ARCHIVE + uriDocidPart;
			break;
		case data:
			resourceId = pastaUriHead + URI_MIDDLE_DATA + uriDocidPart + SLASH
			    + entityId;
			break;
		case dataPackage:
			resourceId = pastaUriHead + URI_MIDDLE_DATA_PACKAGE + uriDocidPart;
			break;
		case metadata:
			resourceId = pastaUriHead + URI_MIDDLE_METADATA + uriDocidPart;
			break;
		case report:
			resourceId = pastaUriHead + URI_MIDDLE_REPORT + uriDocidPart;
			break;
		}

		return resourceId;
	}

	
	/**
	 * Gets the value of the resource directory. Returns the configured property
	 * value if set, else returns the default value.
	 * 
	 * @return the path of the resource directory
	 */
	public static String getResourceDir() {
		if (resourceDir != null) {
			return resourceDir;
		} else {
			return RESOURCE_DIR_DEFAULT;
		}
	}

	
	  /*
	   * Boolean to determine whether a given packageId scope is found in
	   * the set of allowable scopes in PASTA's scope registry.
	   */
		public static boolean isValidScope(String scope) {
			boolean isValid = false;

			if (scope == null)
				return false;
			
			String scopeRegistry = DataPackage.getScopeRegistry();
			if (scopeRegistry == null)
				return false;

			StringTokenizer stringTokenizer = new StringTokenizer(scopeRegistry, ",");
			final int tokenCount = stringTokenizer.countTokens();

			for (int i = 0; i < tokenCount; i++) {
				String token = stringTokenizer.nextToken();
				if (scope.equals(token)) {
					isValid = true;
					break;
				}
			}

			return isValid;
		}
	  
	  
	/**
	 * Utility method to make a DataPackageRegistry object using database
	 * connection settings.
	 * 
	 * @return a DataPackageRegistry object
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static DataPackageRegistry makeDataPackageRegistry()
	    throws ClassNotFoundException, SQLException {
		return new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
	}

	
	/*
	 * Instance methods
	 */

	/*
	 * Implemented methods to satisfy the DatabaseConnectionPoolInterface
	 * interface: getConnection(), getDBAdapterName(), and returnConnection()
	 */

	/**
	 * Gets a database connection from the pool. Implementation of this method is
	 * required by the DatabaseConnectionPoolInterface. Note that in this example,
	 * there is no actual pool of connections. A full-fledged application should
	 * manage a pool of connections that can be re-used.
	 * 
	 * @return checked out connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException,
	    ConnectionNotAvailableException {
		Connection connection = null;

		try {
			Class.forName(dbDriver);
		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
			throw (new SQLException(e.getMessage()));
		}

		try {
			connection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
			throw (e);
		}

		return connection;
	}

	
	/**
	 * Gets the data entity format and returns it as a string.
	 * 
	 * @param scope
	 *          The scope of the metadata document
	 * @param identifier
	 *          The identifier of the metadata document
	 * @param revision
	 *          The revision of the metadata document
	 * @param entityId
	 *          The entityId of the entity
	 * @return The data entity media type, e.g. "text/csv"
	 */
	public MediaType getDataEntityFormat(String scope, Integer identifier,
	    String revision, String entityId) throws Exception {
		MediaType mediaType = null;

		Integer revisionInt = new Integer(revision);
		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
			    dbURL, dbUser, dbPassword);
		String resourceId = composeResourceId(ResourceType.data, scope, identifier, revisionInt, entityId);
		String dataFormat = dataPackageRegistry.getDataFormat(resourceId);
		

		try {
			mediaType = MediaType.valueOf(dataFormat);
		} 
		catch (IllegalArgumentException e) {
			// Set to OCTET_STREAM if non-standard media type.
			mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
		}

		return mediaType;
	}

	
	/**
	 * Returns a list of data package resource identifiers for the specified data
	 * package. This is a pass-through to the DataPackageRegistry method of the
	 * same name.
	 * 
	 * @param scope
	 *          the scope, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier integer value, e.g. 2
	 * @param revision
	 *          the revision value, e.g. 1
	 */
	public ArrayList<String> getDataPackageResources(String scope,
	    Integer identifier, Integer revision) throws ClassNotFoundException,
	    SQLException, IllegalArgumentException {
		ArrayList<String> resources = new ArrayList<String>();

		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
		    dbURL, dbUser, dbPassword);
		if (dataPackageRegistry != null) {
			resources = dataPackageRegistry.getDataPackageResources(scope,
			    identifier, revision);
		}

		return resources;
	}

	
	/**
	 * Get database adpater name. Implementation of this method is required by the
	 * DatabaseConnectionPoolInterface.
	 * 
	 * @return database adapter name, for example, "PostgresAdapter"
	 */
	public String getDBAdapterName() {
		return databaseAdapterName;
	}

	
	/**
	 * Returns checked out database connection to the pool. Implementation of this
	 * method is required by the DatabaseConnectionPoolInterface. Note that in
	 * this example, there is no actual pool of connections to return the
	 * connection to. A full-fledged application should manage a pool of
	 * connections that can be re-used.
	 * 
	 * @param conn
	 *          , Connection that is being returned
	 * @return boolean indicator if the connection was returned successfully
	 */
	public boolean returnConnection(Connection conn) {
		boolean success = false;

		try {
			conn.close();
			success = true;
		} catch (Exception e) {
			// If the return fails, don't throw an Exception,
			// just return a fail status
			success = false;
		}

		return success;
	}

	/* End DatabaseConnectionPoolInterface methods */

	
	/**
	 * Calculates the SHA-1 checksum of a PASTA resource in the data package registry.
	 * 
	 * @param resourceId         The PASTA resource identifier string
	 * @param file               The file object whose checksum is to be calculated
	 * @return  the calcuated SHA-1 checksum, a 40-character string
	 */
	public String calculateChecksum(String resourceId, File file) throws Exception {
		String checksum = null;
		checksum = DigestUtilsWrapper.getSHA1Checksum(file);
		
		return checksum;
	}

	
	/**
	 * Create a data package in PASTA and return a resource map of the created
	 * resources.
	 * 
	 * @param emlFile
	 *          The EML document to be created in the metadata catalog
	 * @param user
	 *          The user value
	 * @param authToken
	 *          The authorization token
	 * @param transaction
	 *          The transaction identifier
	 * @return The resource map generated as a result of creating the data
	 *         package.
	 */
	public String createDataPackage(File emlFile, String user,
	    AuthToken authToken, String transaction) throws ClientProtocolException,
	    FileNotFoundException, IOException, Exception {
		boolean isEvaluate = false;
		String resourceMap = null;

		// Construct an EMLDataPackage object
		DataPackage dataPackage = parseEml(emlFile, isEvaluate); // Parse EML

		if (dataPackage != null) {
			EMLDataPackage levelZeroDataPackage = new EMLDataPackage(dataPackage);
			// Get the packageId from the EMLDataPackage object
			String packageId = levelZeroDataPackage.getPackageId();

			/* Is this a Level 1 data package?
			if (levelZeroDataPackage.isLevelOne()) {
				String message = "The data package you are attempting to insert, '"
				    + packageId
				    + "', is a Level-1 data package. Only Level-0 data packages may be "
				    + "inserted into PASTA.";
				throw new UserErrorException(message);
			}
			*/

			// Is this discovery-level EML?
			if (!levelZeroDataPackage.hasEntity()) {
				String message = "The data package you are attempting to insert, '"
				    + packageId
				    + "', does not describe a data entity. You may be attempting to "
				    + "insert a discovery-level data package into PASTA. Please note "
				    + "that only Level-0 data packages containing at least one data "
				    + "entity may be inserted into PASTA.";
				throw new UserErrorException(message);
			}

			String scope = levelZeroDataPackage.getScope();
			Integer identifier = levelZeroDataPackage.getIdentifier();
			Integer revision = levelZeroDataPackage.getRevision();
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);

			/*
			 * If we have the data package but it was previously deleted (i.e.
			 * de-activated)
			 */
			boolean isDeactivatedDataPackage = dataPackageRegistry
			    .isDeactivatedDataPackage(scope, identifier);
			if (isDeactivatedDataPackage) {
				String message = "Attempting to insert a data package that was previously deleted from PASTA: "
				    + levelZeroDataPackage.getDocid();
				throw new ResourceDeletedException(message);
			}

			// Do we already have this data package in PASTA?
			boolean hasDataPackage = dataPackageRegistry.hasDataPackage(scope,
			    identifier);

			/*
			 * If we do have this data package in PASTA, throw an exception
			 */
			if (hasDataPackage) {
				String message = "Attempting to insert a data package that already exists in PASTA: "
				    + levelZeroDataPackage.getDocid();
				throw new ResourceExistsException(message);
			}

			// Is this data package actively being worked on in PASTA?
			WorkingOn workingOn = dataPackageRegistry.makeWorkingOn();
			boolean isActive = workingOn.isActive(scope, identifier, revision);

			/*
			 * If this data package is already active in PASTA, throw an exception
			 */
			if (isActive) {
				String message = "Attempting to insert a data package that is currently being processed in PASTA: "
				    + levelZeroDataPackage.getDocid();
				throw new UserErrorException(message);
			}

			boolean isUpdate = false;
			boolean useChecksum = false;
			resourceMap = createDataPackageAux(emlFile, levelZeroDataPackage,
			    dataPackageRegistry, packageId, scope, identifier, revision, user,
			    authToken, isUpdate, isEvaluate, transaction, useChecksum);
		}
		
		// Return the resource map
		return resourceMap;
	}

	
	/**
	 * Derive a Level-1 EML file from a Level-0 EML file.
	 * 
	 * @param levelZeroEMLFile    the Level-0 EML file
	 * @param entityURIHashMap       an association where the keys
	 *         are entity names (<entityName>) and the values are
	 *         the PASTA data URLs that should replace the site
	 *         data URLs in the Level-1 EML document.
	 * @return a Level-1 EML XML string
	 * @throws IOException
	 * @throws TransformerException
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
 public String toLevelOne(File levelZeroEMLFile,
                        HashMap<String, String> entityURIHashMap) 
         throws IOException,
                TransformerException, SAXException, ParserConfigurationException {
    String levelOneEMLString = null;
    Document levelZeroEMLDocument = XmlUtility.xmlFileToDocument(levelZeroEMLFile);
    Node documentElement = levelZeroEMLDocument.getDocumentElement();
    LevelOneEMLFactory levelOneEMLFactory = new LevelOneEMLFactory();
    Document levelOneEMLDocument = levelOneEMLFactory.make(
        levelZeroEMLDocument, entityURIHashMap);
    documentElement = levelOneEMLDocument.getDocumentElement();
    levelOneEMLString = XMLUtilities.getDOMTreeAsString(documentElement);
    
    // Convert to dereferenced EML
    // levelOneEMLString = DataPackage.dereferenceEML(levelOneEMLString);

    return levelOneEMLString;
  }


	/**
	 * Derive an enhanced Level-1 EML file from an existing Level-1 EML file.
	 * Additional information is added to the document, such as the DOI
	 * alternate identifier. This enhancement to the Level-1 EML occurs at a 
	 * later stage in the processing than when the original Level-1 EML is 
	 * generated.
	 * 
	 * @param levelOneEMLFile    the Level-1 EML file
	 * @return an enhanced Level-1 EML XML string with information added
	 * @throws IOException
	 * @throws TransformerException
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public String toLevelOneEnhanced(File levelOneEMLFile, String alternateID, String attributeValue)
			throws IOException, TransformerException, SAXException, ParserConfigurationException {
		String enhancedEMLString = null;
		Document levelOneEMLDocument = XmlUtility.xmlFileToDocument(levelOneEMLFile);
		Node documentElement = levelOneEMLDocument.getDocumentElement();
		LevelOneEMLFactory levelOneEMLFactory = new LevelOneEMLFactory();
		Document enhancedEMLDocument = 
				levelOneEMLFactory.enhance(levelOneEMLDocument, alternateID, attributeValue);
		documentElement = enhancedEMLDocument.getDocumentElement();
		enhancedEMLString = XMLUtilities.getDOMTreeAsString(documentElement);

		return enhancedEMLString;
	}


    /**
	 * Stores a local copy of EML metadata on the file system.
	 * 
	 * @param  emlPackageId the package id object of this metadata file 
	 * @param  xmlContent   the XML string content
	 * @param  isLevelZero  true if this is Level-0 metadata,
	 *                       false if this is Level-1 metadata
	 * @return the EML file that was created
	 */
	 private File storeMetadata(EmlPackageId emlPackageId, String xmlContent, boolean isLevelZero) 
	         throws IOException {
	   File emlFile = null;
	   
	   if (emlPackageId != null) {
	     FileSystemResource metadataResource = 
	         new FileSystemResource(emlPackageId);
	     boolean isReportResource = false;
	     String dirPath = metadataResource.getDirPath(isReportResource);   
	     File dirFile = new File(dirPath);  
	     if (dirFile != null && !dirFile.exists()) { dirFile.mkdirs(); }
	     String emlFilename = (isLevelZero ? LEVEL_ZERO_FILE_NAME : LEVEL_ONE_FILE_NAME);
	     emlFile = new File(dirPath, emlFilename);
	     FileWriter fileWriter = null;
	     
	     StringBuffer stringBuffer = new StringBuffer(xmlContent);
	     try {
	       fileWriter = new FileWriter(emlFile);
	       IOUtil.writeToWriter(stringBuffer, fileWriter, true);
	     }
	     catch (IOException e) {
	       logger.error("IOException storing metadata file:\n" + 
	                    e.getMessage());
	       e.printStackTrace();
	       throw(e);
	     }
	     finally {
	       if (fileWriter != null) { fileWriter.close(); }
	     }
	   }
	   
	   return emlFile;
	 }

	 
	/*
	 * Implements common logic that is shared by the createDatePackage(),
	 * evaluateDataPackage(), and updateDataPackage() methods.
	 */
	private String createDataPackageAux(File emlFile, EMLDataPackage levelZeroDataPackage,
				DataPackageRegistry dataPackageRegistry, String packageId, String scope,
				Integer identifier, Integer revision, String user, AuthToken authToken,
				boolean isUpdate, boolean isEvaluate, String transaction, boolean useChecksum)
			throws	ClassNotFoundException, SQLException, IOException,
					ClientProtocolException, TransformerException, Exception {
		Authorizer authorizer = new Authorizer(dataPackageRegistry);
		DataManagerClient dataManagerClient = new DataManagerClient();
		String formatType = null;   // used for metadata resources
		boolean isDataPackageValid;
		boolean isDataValid = false;
		HashMap<String, String> entityURIHashMap = new HashMap<String, String>();
		ArrayList<String> entityURIList = new ArrayList<String>();
		boolean mayOverwrite = isEvaluate;
		String resourceMap = null;
		String uriDocidPart = scope + SLASH + identifier + SLASH + revision;
		String metadataURI = pastaUriHead + URI_MIDDLE_METADATA + uriDocidPart;
		String reportURI = pastaUriHead + URI_MIDDLE_REPORT + uriDocidPart;
		String qualityReportXML = null;
		String resourceLocation = null;
		String datasetAccessXML = levelZeroDataPackage.getAccessXML();
		AccessMatrix datasetAccessMatrix = new AccessMatrix(datasetAccessXML);
		EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
		EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope, identifier.toString(), revision.toString());
		WorkingOn workingOn = dataPackageRegistry.makeWorkingOn();
		File levelOneEMLFile = null;
		String levelZeroXML = null;
		
		try {
		if (!isEvaluate) {
			workingOn.addDataPackage(scope, identifier, revision);
		}
		
		ReservationManager reservationManager = 
				new ReservationManager(dbDriver, dbURL, dbUser, dbPassword);
		String reservationUserId = reservationManager.getReservationUserId(scope, identifier);
		if (reservationUserId != null && !reservationUserId.equals(user)) {
			String msg = String.format(
					"%s.%d is currently reserved by user %s. You should be logged in as user %s to evaluate or upload this data package. Alternatively, you could change the packageId of this data package to a value that does not conflict with the reserved identifier %s.%d",
					scope, identifier, reservationUserId, reservationUserId, scope, identifier);
			throw new Exception(msg);
		}

		/*
		 * Is the metadata for this data package valid?
		 */
		boolean isMetadataValid = levelZeroDataPackage.isMetadataValid();
		isDataPackageValid = isMetadataValid;

			if (isDataPackageValid || isEvaluate) {

				/*
				 * Create (or evaluate) the data entities in the Data Manager
				 */
				Map<String, String> entityIdNamePairs = null;
				DataPackage dataPackage = levelZeroDataPackage.getDataPackage();
				if (isEvaluate) {
					entityIdNamePairs = dataManagerClient.evaluateDataEntities(
							dataPackageRegistry, dataPackage, transaction, useChecksum);
				} else {
					entityIdNamePairs = dataManagerClient.createDataEntities(dataPackageRegistry, dataPackage,
					    transaction, useChecksum);
				}

				if (entityIdNamePairs != null) {
          for (String entityId : entityIdNamePairs.keySet()) {
            EMLEntity emlEntity = new EMLEntity(levelZeroDataPackage);
            emlEntity.setEntityId(entityId);
            String entityName = entityIdNamePairs.get(entityId);
            emlEntity.setEntityName(entityName);
            String entityURI = pastaUriHead + URI_MIDDLE_DATA + uriDocidPart + SLASH + entityId;
            emlEntity.setEntityURI(entityURI);
            entityURIList.add(entityURI);
            entityURIHashMap.put(entityName, entityURI);
            // Add this emlEntity to the list of entities in the data package
            levelZeroDataPackage.addEMLEntity(emlEntity);
          }
          
          isDataValid = levelZeroDataPackage.isDataValid();
				}
			}

		isDataPackageValid = isDataPackageValid && isDataValid;

		/*
		 * If the data package is valid, and if this is not evaluate mode, insert or
		 * update the metadata to the Metadata Catalog service.
		 */
		if (isDataPackageValid && !isEvaluate) {
			MetadataCatalog solrCatalog = new SolrMetadataCatalog(solrUrl);
			String levelOneXML = toLevelOne(emlFile, entityURIHashMap);

		    try {
		        Document levelZeroEMLDocument = XmlUtility.xmlFileToDocument(emlFile);
		        Node documentElement = levelZeroEMLDocument.getDocumentElement();
		        levelZeroXML = XMLUtilities.getDOMTreeAsString(documentElement);

		        boolean isLevelZero = true;
		        storeMetadata(emlPackageId, levelZeroXML, isLevelZero);
		        

		        isLevelZero = false;
		        levelOneEMLFile = storeMetadata(emlPackageId, levelOneXML, isLevelZero);

		        DataPackage dataPackage = parseEml(levelOneEMLFile, isEvaluate);
		        
		        /*
		         * Level One EML may potentially have a different access block than
		         * the Level Zero EML, so the access matrix needs to be regenerated.
		         */
				if (dataPackage != null) {
					EMLDataPackage levelOneDataPackage = new EMLDataPackage(dataPackage);
					datasetAccessXML = levelOneDataPackage.getAccessXML();
					datasetAccessMatrix = new AccessMatrix(datasetAccessXML);
				}
		        
		        /*
		         * Now that the Level-1 EML is stored on disk, we can use it to
		         * generate Dublin Core and store that as well.
		         */
		        String metadataChildDir = packageIdToMetadataPath(packageId);
		        DublinCore dublinCore = new DublinCore();
		        dublinCore.transformMetadata(xslDir, metadataChildDir);
		    }
		    catch (IOException e) {
		        throw (e);
		    }
		
			String solrResult = null;

			/*
			 * If the Metadata Catalog client throws an exception during an insert or
			 * update operation, roll-back any previous data entity inserts that were
			 * performed by deleting the data package (for this specific revision
			 * only) from the Data Manager.
			 */
				if (isUpdate) {
					solrResult = solrCatalog.updateEmlDocument(emlPackageId,
							levelOneXML);
				}
				else {
					solrResult = solrCatalog.createEmlDocument(emlPackageId,
							levelOneXML);
				}

			/*
			 * Check whether there was a problem inserting or updating to the 
			 * Solr metadata Catalog. (A non-null solrResult string means that an error
			 * message was generated.) The metadata (and hence the data package) 
			 * can no longer be considered valid.
			 */
			if (solrResult != null) {
				isMetadataValid = false;
				isDataPackageValid = false;
			}
			
			/*
			 * Insert any provenance records that may exist for this data package
			 */
			ProvenanceIndex provenanceIndex = new ProvenanceIndex(dataPackageRegistry);
			try {
				provenanceIndex.insertProvenanceRecords(packageId, levelOneXML);
			} 
			catch (Exception e) {
				provenanceIndex.rollbackProvenanceRecords(packageId);
				throw (e);
			}
		}

		/*
		 * If the data package is valid and this is not evaluate mode, add data
		 * package resources to the registry.
		 */
		if (isDataPackageValid && !isEvaluate) {
			for (EMLEntity emlEntity : levelZeroDataPackage.getEMLEntityList()) {
				String entityDir = EMLDataManager.getEntityDir();
				String entityId = emlEntity.getEntityId();
				String entityName = emlEntity.getEntityName();
				String entityURI = emlEntity.getEntityURI();
				String objectName = findObjectName(levelZeroXML, entityName);

				dataPackageRegistry.addDataPackageResource(entityURI,
				    ResourceType.data, entityDir, packageId, scope, identifier,
				    revision, entityId, entityName, objectName, user, formatType, mayOverwrite);
				
				// Store the checksums of the data entity resource
				File file = getDataEntityFile(scope, identifier,
						revision.toString(), entityId, authToken, user);
				storeChecksums(entityURI, file);

				// Store the size of the data entity resource
				storeResourceSize(entityURI, file);

				// Store the data format of the data entity resource, as derived by the EML parser
				String dataFormat = emlEntity.getDataFormat();
				if (dataFormat != null) storeDataFormat(entityURI, dataFormat);

				/*
				 * Get the <access> XML block for this data entity and store the
				 * entity's access control rules in the access_matrix table for this
				 * entityURI resource identifier
				 */
				String entityAccessXML = emlEntity.getAccessXML();
				AccessMatrix entityAccessMatrix = new AccessMatrix(entityAccessXML);
				authorizer.storeAccessMatrix(entityURI, entityAccessMatrix,
				    mayOverwrite);
			}

			/*
			 * Add the metadata resource to the registry
			 */
			
			String metadataFormatType = levelZeroDataPackage.getFormatType();
			
			dataPackageRegistry.addDataPackageResource(metadataURI,
			    ResourceType.metadata, resourceLocation, packageId, scope,
			    identifier, revision, null, null, null, user, metadataFormatType, mayOverwrite);
			
			/*
			 * Store the access control rules for the metadata resource
			 */
			authorizer.storeAccessMatrix(metadataURI, datasetAccessMatrix,
			    mayOverwrite);
			
		}

		/*
		 * If the data package is valid and this is not evaluate mode, add the
		 * quality report resource to the registry.
		 */
		if (isDataPackageValid && !isEvaluate) {

			dataPackageRegistry.addDataPackageResource(reportURI,
			    ResourceType.report, resourceLocation, packageId, scope, identifier,
			    revision, null, null, null, user, formatType, mayOverwrite);

			// Store the checksum of the report resource
			File file = readDataPackageReport(scope, identifier,
					revision.toString(), emlPackageId, authToken, user);
			storeChecksums(reportURI, file);

			/*
			 * Store the access control rules for the quality report resource
			 */
			authorizer
			    .storeAccessMatrix(reportURI, datasetAccessMatrix, mayOverwrite);
		}

		qualityReportXML = levelZeroDataPackage.getDataPackage().getQualityReport()
		    .toXML();

		/*
		 * Generate the resource map for the newly created data package
		 */
		if (isEvaluate) {
			/*
			 * The return value of an Evaluate operation is always the quality report.
			 */
			resourceMap = qualityReportXML;
		} 
		else {
			if (isDataPackageValid) {

				/*
				 * Add the data package resource map to the Data Package Registry
				 */
				String dataPackageURI = composeResourceId(ResourceType.dataPackage,
				    scope, identifier, revision, null);
				
				dataPackageRegistry.addDataPackageResource(dataPackageURI,
				    ResourceType.dataPackage, resourceLocation, packageId, scope,
				    identifier, revision, null, null, null, user, formatType, mayOverwrite);

				resourceMap = generateDataPackageResourceGraph(dataPackageURI,
				    metadataURI, entityURIList, reportURI);

				/*
				 * Store the access control rules for the data package resource
				 */
				authorizer.storeAccessMatrix(dataPackageURI, datasetAccessMatrix,
				    mayOverwrite);
				
				/*
				 * Add a DataPackageUpload object to the DataPackageUploadManager to update
				 * its list of recent inserts and recent updates, but only if this is a
				 * publicly accessible data package.
				 */
				boolean isPublic = dataPackageRegistry.isPublicAccessible(dataPackageURI);
				if (isPublic) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = new Date();
					String today = sdf.format(date);
					DataPackageUpload dpu = null;
					if (isUpdate) {
						dpu = new DataPackageUpload(today, "updateDataPackage", scope, identifier, revision, user, null);
						DataPackageUploadManager.addRecentUpdate(dpu);
					}
					else {
						dpu = new DataPackageUpload(today, "createDataPackage", scope, identifier, revision, user, null);
						DataPackageUploadManager.addRecentInsert(dpu);
					}
				}

				/*
				 * Register a DOI for the data package
				 */
				if (doiScanner != null) {
					// DOIs should be created only for publicly accessible resources
					boolean publicOnly = true;
					ArrayList<Resource> resourceList = 
							dataPackageRegistry.listDataPackageResources(packageId, publicOnly);
					if (resourceList != null) {
						for (Resource resource : resourceList) {
							if (resource.getResourceType().equals("dataPackage") &&
								resource.getDoi() == null
							   ) {
								try {
									String doi = doiScanner.processOneResource(resource);
									if (doi != null) {
										SolrMetadataCatalog solrCatalog = new SolrMetadataCatalog(solrUrl);
										solrCatalog.indexDoi(emlPackageId, doi);
										
										/*
										 * Insert the DOI into the EML as an alternate identifier
										 */
										if (levelOneEMLFile != null) {
											String enhancedXML =  toLevelOneEnhanced(levelOneEMLFile, doi, DOI_SYSTEM_VALUE);
									        boolean isLevelZero = false;
									        storeMetadata(emlPackageId, enhancedXML, isLevelZero);

									        /*
									         * Now that the Level-1 EML is stored on disk, we can use it to
									         * generate Dublin Core and store that as well.
									         */
									        String metadataChildDir = packageIdToMetadataPath(packageId);
									        DublinCore dublinCore = new DublinCore();
									        dublinCore.transformMetadata(xslDir, metadataChildDir);
										}
									}
								}
								catch (DOIException e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								}
							}
						}
					}
				}
				
				// Store the checksum of the metadata resource
				File file = getMetadataFile(scope, identifier, revision.toString(),
						user, authToken);
				storeChecksums(metadataURI, file);
				
				/*
				 * Optimize data storage for the data package
				 */
				try {
					StorageManager storageManager = new StorageManager(dataPackageRegistry, emlPackageId);
					storageManager.optimizeStorage();
				}
				catch (Exception e) {
					logger.error(
							String.format("Exception optimizing data storage for data package %s: %s",
									      packageId, e.getMessage()));
				}

				/*
				 * Set the date uploaded in the reservation table to indicate
				 * that this identifier is no longer actively reserved.
				 */
				reservationManager.setDateUploaded(scope, identifier);

				/*
				 * Notify the Event Manager about the new resource
				 */
				notifyEventManager(packageId, scope, identifier, revision, user,
				    authToken);
			} 
			else {
				throw new UserErrorException(qualityReportXML);
			}
		}
		}
		finally {
			/*
			 * Since the data package is no longer being worked on, set
			 * the end date in the working_on table. This should occur
			 * regardless of whether the upload succeeded or failed.
			 */
			if (!isEvaluate) {
				workingOn.updateEndDate(scope, identifier, revision);
			}
		}

		return resourceMap;
	}
	
	
	/**
	 * Creates a new reservation for the specified user and scope. The
	 * next reservable identifier is determined, the reservation is placed,
	 * and the identifier is returned as the method's value.
	 * 
	 * @param userId    The user who is making the reservation
	 * @param scope     The scope of the reserved identifier, e.g. "edi"
	 * @return identifier  The integer value of the reserved identifier
	 * @throws Exception
	 */
	public Integer createReservation(String userId, String scope) 
			throws Exception {
		boolean isValidScope = isValidScope(scope);
		if (!isValidScope) {
			String msg = 
					String.format("Attempting to create a data package identifier reservation for an unknown scope: %s",
					              scope);
			throw new UserErrorException(msg);
		}
		
		Integer identifier = getNextReservableIdentifier(scope);
		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);
		
		dataPackageRegistry.addDataPackageReservation(scope, identifier, userId);
		
		return identifier;
	}
	
	
	/**
	 * Deletes a reservation for the specified user and document identifier.
	 * 
	 * @param userId    The user who is making the reservation
	 * @param scope     The scope of the data package to be deleted, e.g. "edi"
	 * @return identifier  The integer value of the reserved identifier that is to be deleted
	 * @throws Exception
	 */
	public Integer deleteReservation(String userId, String scope, Integer identifier) 
			throws Exception {
		boolean isValidScope = isValidScope(scope);
		if (!isValidScope) {
			String msg = 
					String.format("Attempting to delete a data package identifier reservation for an unknown scope: %s",
					              scope);
			throw new UserErrorException(msg);
		}
		
		ReservationManager reservationManager = new ReservationManager(dbDriver, dbURL, dbUser, dbPassword);
		reservationManager.deleteDataPackageReservation(scope, identifier, userId);
		
		return identifier;
	}
	
	
	/*
	 * Determine the next reservable identifier value for the specified scope.
	 * Three lists must be checked:
	 *    1. List of identifiers known in the resource registry belong
	 *       to the specified scope, including identifiers for data packages 
	 *       that have been deleted;
	 *    2. List of "working on" identifiers for the specified scope, 
	 *       i.e. inserts or updates that are currently in progress;
	 *    3. List of reserved identifiers for the specified scope.
	 */
	private Integer getNextReservableIdentifier(String scope) 
		throws Exception{
		Integer nextReservable = null;
		final Boolean isSpokenFor = new Boolean(true);
		DataPackageRegistry dataPackageRegistry = 
				new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
		
		HashMap<Integer, Boolean> spokenFor = new HashMap<Integer, Boolean>();

		/*
		 * First get a list of all known identifiers for this scope that exist
		 * in the resource registry. Include identifiers for data packages that
		 * were deleted since these identifier values can't be reused.
		 */
		boolean includeDeleted = true;
		ArrayList<String> identifierList = 
				dataPackageRegistry.listDataPackageIdentifiers(scope, includeDeleted);

		for (String identifier : identifierList) {
			Integer spokenForIdentifier = Integer.parseInt(identifier);
			spokenFor.put(spokenForIdentifier, isSpokenFor);
		}
		
		/*
		 * Next get a list of all known identifiers for this scope that are
		 * actively being worked on (i.e. an upload operation for this
		 * identifier is actively in progress).
		 */
		ArrayList<Integer> identifiers = dataPackageRegistry.listWorkingOnIdentifiers(scope);
		
		for (Integer identifier : identifiers) {
			spokenFor.put(identifier, isSpokenFor);
		}
		
		
		/*
		 * Finally, get a list of all active reservations for this scope.
		 */
		String reservationString = dataPackageRegistry.listReservationIdentifiers(scope);
		String[] reservationEntries = reservationString.split("\n");
		if (reservationEntries != null && reservationEntries.length > 0) {
			for (String reservedIdentifier : reservationEntries) {
				if (!reservedIdentifier.isEmpty()) {
					Integer spokenForIdentifier = Integer.parseInt(reservedIdentifier);
					spokenFor.put(spokenForIdentifier, isSpokenFor);
				}
			}
		}
		
		/*
		 * Now find the first available identifier value that isn't claimed by
		 * one of the three previous lists.
		 */
		Integer nextCandidate = new Integer(1);
		while (nextReservable == null) {
			Boolean value = spokenFor.get(nextCandidate);
			if (value != null && value.equals(isSpokenFor)) {
				nextCandidate = nextCandidate + 1;
			}
			else {
				nextReservable = nextCandidate;
			}
		}
		
		return nextReservable;
	}
	
	/*
	 * Notifies the event manager of a change to a data package by using an
	 * EventManagerClient object.
	 */
	private void notifyEventManager(String packageId, String scope,
	    Integer identifier, Integer revision, String user, AuthToken authToken) {
		this.packageId = packageId;
		this.scope = scope;
		this.identifier = identifier;
		this.revision = revision;
		this.user = user;
		this.authToken = authToken;

		EventManagerClient eventManagerClient = new EventManagerClient();

		try {
			eventManagerClient.notifyEventManager(this.scope, this.identifier,
			    this.revision);
		} catch (Exception e) {
			logger
			    .warn("Exception while notifying Event Manager of changed to data package: "
			        + this.packageId + ". " + e.getMessage());
		}

	}

	
	/**
	 * Delete a data package in PASTA based on its scope and identifier values
	 * 
	 * @param scope
	 *          The scope value of the data package to be deleted
	 * @param identifier
	 *          The identifier value of the data package to be deleted
	 * @param user
	 *          The user value
	 * @param authToken
	 *          The authentication token object
	 */
	public boolean deleteDataPackage(String scope, Integer identifier,
	    String user, AuthToken authToken) throws ClassNotFoundException,
	    SQLException, ClientProtocolException, IOException, Exception {
		boolean hasDataPackage = false;
		boolean deleted = false;
		Integer revision = getNewestRevision(scope, identifier);

		try {
			/*
			 * Do we have this data package in PASTA?
			 */
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);
			hasDataPackage = dataPackageRegistry.hasDataPackage(scope, identifier);

			/*
			 * Check whether user is authorized to delete the data package
			 */
			String entityId = null;
			String resourceId = composeResourceId(ResourceType.dataPackage, scope,
			    identifier, revision, entityId);
			Authorizer authorizer = new Authorizer(dataPackageRegistry);
			boolean isAuthorized = authorizer.isAuthorized(authToken, resourceId,
			    Rule.Permission.write);
			if (!isAuthorized) {
				String message = "User " + user
				    + " does not have permission to delete this data package: "
				    + resourceId;
				throw new UnauthorizedException(message);
			}

			/*
			 * If we do have this data package in PASTA, first check to see whether it
			 * was previously deleted
			 */
			if (hasDataPackage) {
				boolean isDeactivatedDataPackage = dataPackageRegistry
				    .isDeactivatedDataPackage(scope, identifier);
				if (isDeactivatedDataPackage) {
					String docid = EMLDataPackage.composeDocid(scope, identifier);
					String message = "Attempting to delete a data package that was previously deleted from PASTA: "
					    + docid;
					throw new ResourceDeletedException(message);
				}

				/*
				 * Delete the metadata from the Metadata Catalog
				 */
				MetadataCatalog solrCatalog = new SolrMetadataCatalog(solrUrl);
				EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
				EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope,
				    identifier.toString(), revision.toString());
				solrCatalog.deleteEmlDocument(emlPackageId);

				/*
				 * Delete the data package from the resource registry
				 */
				
				deleted = dataPackageRegistry.deleteDataPackage(scope, identifier);
			}
		} catch (ClassNotFoundException e) {
			logger.error("Error connecting to Data Package Registry: "
			    + e.getMessage());
			e.printStackTrace();
			throw (e);
		} catch (SQLException e) {
			logger.error("Error connecting to Data Package Registry: "
			    + e.getMessage());
			e.printStackTrace();
			throw (e);
		}

		return deleted;
	}

	
	/**
	 * Evaluate a data package, returning the XML string representation of the
	 * quality report.
	 * 
	 * @param emlFile
	 *          file containing the EML document to be evalauted
	 * @param user
	 *          the user name
	 * @param authToken
	 *          the authentication token object
	 * @param transaction
	 *          the transaction identifier
	 * @return the quality report XML string
	 */
	public String evaluateDataPackage(File emlFile, String user,
	    AuthToken authToken, String transaction, boolean useChecksum) throws ClientProtocolException,
	    FileNotFoundException, IOException, Exception {
		DataPackage dataPackage = null;
		final boolean isEvaluate = true;
		String xmlString = null;

		// Construct an EMLDataPackage object
		try {
			dataPackage = parseEml(emlFile, isEvaluate); // Parse EML
		} catch (UserErrorException e) {
			return e.getMessage();
		}

		if (dataPackage != null) {
			EMLDataPackage levelZeroDataPackage = new EMLDataPackage(dataPackage);

			// Get the packageId from the EMLDataPackage object
			String packageId = levelZeroDataPackage.getPackageId();

			/* Is this a Level 1 data package?
			if (levelZeroDataPackage.isLevelOne()) {
				String message = "The data package you are attempting to evaluate, '"
				    + packageId
				    + "', is a Level-1 data package. Only Level-0 data packages may be "
				    + "inserted into PASTA.";
				throw new UserErrorException(message);
			}
			*/

			// Is this discovery-level EML?
			if (!levelZeroDataPackage.hasEntity()) {
				String message = "The data package you are attempting to evaluate, '"
				    + packageId
				    + "', does not describe a data entity. You may be attempting to "
				    + "insert a discovery-level data package into PASTA. Please note "
				    + "that only Level-0 data packages containing at least one data "
				    + "entity may be inserted into PASTA.";
				throw new UserErrorException(message);
			}

			// Do we already have this data package in PASTA?
			String scope = levelZeroDataPackage.getScope();
			Integer identifier = levelZeroDataPackage.getIdentifier();
			Integer revision = levelZeroDataPackage.getRevision();
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);

			/*
			 * Create the quality report
			 */
			boolean isUpdate = false;
			xmlString = createDataPackageAux(emlFile, levelZeroDataPackage,
			    dataPackageRegistry, packageId, scope, identifier, revision, user,
			    authToken, isUpdate, isEvaluate, transaction, useChecksum);

			// Clean up resources in evaluate mode
			levelZeroDataPackage.deleteDataPackageResources(isEvaluate);
		}

		return xmlString;
	}

	
	/**
	 * 
	 */
	private String generateDataPackageResourceGraph(String dataPackageURI,
	    String metadataURI, ArrayList<String> entityList, String reportURI) {
		StringBuffer stringBuffer = new StringBuffer("");
		stringBuffer.append(dataPackageURI + "\n");
		stringBuffer.append(metadataURI + "\n");
		for (String entityURI : entityList) {
			stringBuffer.append(entityURI + "\n");
		}
		stringBuffer.append(reportURI);

		return stringBuffer.toString();
	}

	
	/**
	 * Gets the newest revision of a data package based on its scope and
	 * identifier.
	 * 
	 * @param scope
	 *          the scope of the entity
	 * @param identifier
	 *          the identifier of the entity
	 * @return newest an Integer representing the newest revision
	 * @throws SQLException
	 *           if an error occurs when connection to the data cache
	 */
	public Integer getNewestRevision(String scope, Integer identifier)
	    throws ClassNotFoundException, SQLException, ResourceNotFoundException {
		Integer newest = null;

		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);
			newest = dataPackageRegistry.getNewestRevision(scope, identifier);
			
			if (newest == null) {
			  String message = String.format(
					  "No resources found for scope='%s', identifier='%s'\n\n",
					  scope, identifier.toString());
			  throw new ResourceNotFoundException(message);
			}
					
		} catch (SQLException e) {
			logger
			    .error("Error connecting to Data Cache Registry: " + e.getMessage());
			throw (e);
		}

		return newest;
	}

	
	/**
	 * Gets the oldest revision of a data package based on its scope and
	 * identifier.
	 * 
	 * @param scope
	 *          the scope of the entity
	 * @param identifier
	 *          the identifier of the entity
	 * @return oldest an Integer representing the oldest revision
	 * @throws SQLException
	 *           if an error occurs when connection to the data cache
	 */
	public Integer getOldestRevision(String scope, Integer identifier)
	    throws ClassNotFoundException, SQLException, ResourceNotFoundException {
		Integer oldest = null;

		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);
			oldest = dataPackageRegistry.getOldestRevision(scope, identifier);
			
			if (oldest == null) {
			  String message = String.format(
					  "No resources found for scope='%s', identifier='%s'\n\n",
					  scope, identifier.toString());
			  throw new ResourceNotFoundException(message);
			}
					
		} catch (SQLException e) {
			logger
			    .error("Error connecting to Data Cache Registry: " + e.getMessage());
			throw (e);
		}

		return oldest;
	}
	
	
	/**
	 * Determines whether the user identified by the authentication token is
	 * authorized to access the given resource identifier with the given
	 * permission.
	 * 
	 * @param authToken
	 *          The user's authentication token.
	 * @param resourceId
	 *          The resource identifier of the requested resource.
	 * @param permission
	 *          The requested permission for accessing the resource (e.g., READ).
	 * @return The boolean result of the request.
	 * @throws IllegalArgumentException, UnauthorizedException
	 */
	public Boolean isAuthorized(AuthToken authToken, String resourceId,
	    Rule.Permission permission) throws IllegalArgumentException,
	    UnauthorizedException {

		Boolean isAuthorized = null;
		DataPackageRegistry dpr = null;
		String userId = authToken.getUserId();

		if (resourceId == null || resourceId.isEmpty()) {
			String gripe = "ResourceId parameter is null or is empty!";
			throw new IllegalArgumentException(gripe);
		}
		
    try {
	    dpr = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
    } catch (ClassNotFoundException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
    } catch (SQLException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
    }
    
		Authorizer authorizer = new Authorizer(dpr);
		try {
	    isAuthorized = authorizer.isAuthorized(authToken, resourceId, permission);
	    
			if (!isAuthorized) {
				String gripe = "User \"" + userId + "\" is not authorized to "
				    + permission + " this " + resourceId + " resource!";
				throw new UnauthorizedException(gripe);
			}
	    
    } catch (ClassNotFoundException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
    } catch (SQLException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
    }
		
		return isAuthorized;
		
	}
	
	
	/**
	 * List the docid values of all data packages that are active
	 * (not deleted) in the resource registry.
	 * 
	 * @return A newline-separated list of document id strings corresponding 
	 *         to the list of active (undeleted) data packages, where a 
	 *         document id is the packageId minus the revision value
	 */
	public String listActiveDataPackages() throws ClassNotFoundException,
	    SQLException {
		DataPackageRegistry dataPackageRegistry = 
				new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
		String packageListString = null;
		StringBuffer stringBuffer = new StringBuffer("");
		ArrayList<String> packageList = 
				dataPackageRegistry.listActiveDataPackages();

		// Throw a ResourceNotFoundException if the list is empty
		if (packageList == null || packageList.size() == 0) {
			String message = "No resources found\n\n";
			throw new ResourceNotFoundException(message);
		}

		for (String dataPackage : packageList) {
			stringBuffer.append(dataPackage + "\n");
		}

		packageListString = stringBuffer.toString();
		return packageListString;
	}

	
	/**
	 * Returns an XML-formatted list of all reservations currently in PASTA.
	 * 
	 * @return An XML-formatted string.
	 */
	public String listActiveReservations() 
			throws Exception {
		ReservationManager reservationManager = 
				new ReservationManager(dbDriver, dbURL, dbUser, dbPassword);
		String reservationXML = reservationManager.listActiveReservations();
		return reservationXML;
	}

	
	/**
	 * Lists all numeric identifiers that are actively reserved by end users
	 * for the current scope.
	 * 
	 * @return A string of newline-separated numeric identifier values.
	 */
	public String listReservationIdentifiers(String scope) 
			throws Exception {
		DataPackageRegistry dataPackageRegistry = 
				new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
		String reservationsString = dataPackageRegistry.listReservationIdentifiers(scope);
		return reservationsString;
	}

	
	/**
	 * List the package ID values (including revisions) of all data packages that 
	 * are active (not deleted) in the resource registry.
	 * 
	 * @return A newline-separated list of package ID strings corresponding 
	 *         to the list of active (undeleted) data packages.
	 */
	public String listAllDataPackages() 
			throws ClassNotFoundException, SQLException {
		boolean includeInactive = false;
		DataPackageRegistry dataPackageRegistry = 
				new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
		String packageListString = null;
		StringBuffer stringBuffer = new StringBuffer("");
		ArrayList<String> packageList = 
				dataPackageRegistry.listAllDataPackageRevisions(includeInactive);

		// Throw a ResourceNotFoundException if the list is empty
		if (packageList == null || packageList.size() == 0) {
			String message = "No resources found\n\n";
			throw new ResourceNotFoundException(message);
		}

		for (String dataPackage : packageList) {
			stringBuffer.append(dataPackage + "\n");
		}

		packageListString = stringBuffer.toString();
		return packageListString;
	}

	
	/**
	 * List the data entity resources for the specified data package that are
	 * readable by the specified user.
	 * 
	 * @param scope
	 *          the scope value
	 * @param identifier
	 *          the identifier value
	 * @param revision
	 *          the revision value
	 * @param user
	 *          the user name
	 * @return a newline-separated list of data entity resource identifiers
	 */
	public String listDataEntities(String scope, Integer identifier,
	    Integer revision, String user) throws ClassNotFoundException,
	    SQLException, IllegalArgumentException, ResourceNotFoundException {
		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
		    dbURL, dbUser, dbPassword);
		String entityListString = null;
		StringBuffer stringBuffer = new StringBuffer("");
		ArrayList<String> entityList = dataPackageRegistry.listDataEntities(scope,
		    identifier, revision);

		/* Throw a ResourceNotFoundException if the list is empty
		if (entityList == null || entityList.size() == 0) {
			String message = "No entity resources found for scope = '" + scope
			    + "'; identifier = '" + identifier + "'; revision = '" + revision
			    + "'\n\n";
			throw new ResourceNotFoundException(message);
		}*/

		for (String entity : entityList) {
			stringBuffer.append(entity + "\n");
		}

		entityListString = stringBuffer.toString();
		return entityListString;
	}

	
	/**
	 * List the data descendant URLs for the specified data package, that is,
	 * the list of data package identifiers representing the data 
	 * packages that are derived from the specified source data package.
	 * This is determined by searching the "derivedFrom" field of all data
	 * packages in 
	 * 
	 * @param scope
	 *          the scope value of the source data package
	 * @param identifier
	 *          the identifier value of the source data package
	 * @param revision
	 *          the revision value of the source data package
	 * @param authToken
	 *          the authorization token object
	 * @return a newline-separated list of data package resource identifiers
	 */
	public String listDataDescendants(String scope, Integer identifier, Integer revision, AuthToken authToken) 
			throws Exception {
		String dataDescendantsString = null;
		StringBuilder stringBuilder = new StringBuilder("");
		EmlPackageId epi = new EmlPackageId(scope, identifier, revision);
		EmlPackageIdFormat epif = new EmlPackageIdFormat();
		String sourceId = epif.format(epi);

		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
			    dbURL, dbUser, dbPassword);
		
		ArrayList<edu.lternet.pasta.common.eml.DataPackage.DataDescendant> dataDescendants = 
				dataPackageRegistry.listDataDescendants(sourceId);
		
	    stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    stringBuilder.append("<dataDescendants>\n");

		for (DataDescendant dataDescendant : dataDescendants) {
			stringBuilder.append(dataDescendant.toXML());
		}

	    stringBuilder.append("</dataDescendants>\n");

	    dataDescendantsString = stringBuilder.toString();
		return dataDescendantsString;
	}


	/**
	 * List the data sources URLs for the specified data package, that is,
	 * the list of data package identifiers representing the data 
	 * sources from which this data package is derived
	 * 
	 * @param scope
	 *          the scope value of the derived data package
	 * @param identifier
	 *          the identifier value of the derived data package
	 * @param revision
	 *          the revision value of the derived data package
	 * @param user
	 *          the user name
	 * @return a newline-separated list of data package resource identifiers
	 */
	public String listDataSources(String scope, Integer identifier, Integer revision, AuthToken authToken) 
			throws Exception {
		String dataSourcesString = null;
		StringBuilder stringBuilder = new StringBuilder("");
		EmlPackageId epi = new EmlPackageId(scope, identifier, revision);
		EmlPackageIdFormat epif = new EmlPackageIdFormat();
		String derivedId = epif.format(epi);

		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
			    dbURL, dbUser, dbPassword);
		
		ArrayList<edu.lternet.pasta.common.eml.DataPackage.DataSource> dataSources = 
				dataPackageRegistry.listDataSources(derivedId);
		
	    stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    stringBuilder.append("<dataSources>\n");

		for (DataSource dataSource : dataSources) {
			stringBuilder.append(dataSource.toXML());
		}

	    stringBuilder.append("</dataSources>\n");

	    dataSourcesString = stringBuilder.toString();
		return dataSourcesString;
	}


	/**
	 * Generates an XML string listing data package changes, supporting the
	 * listDataPackageChanges web service method.
	 * 
	 * @param  fromDate  the date-time determining how far back in time
	 *                   changes should be included. If null, include all
	 *                   changes (inserts, updates, deletes) that are
	 *                   recorded in the resource registry.
	 * @param  toDate    the date-time determining how recent in time
	 *                   changes should be included. If null, include all
	 *                   changes (inserts, updates, deletes) from the fromDate
	 *                   to the present date-time.
	 * @param  scope     Filter results by whether they match a particular 
	 *                   data package scope value, e.g. "edi".
	 * @return the XML string listing the data package changes. See the
	 *         listDataPackageChanges web service method for an example
	 *         document
	 * @throws Exception
	 */
	public String listDataPackageChanges(String fromDate, String toDate, String scope) 
		throws Exception {
		final int limit = 0;
		String xml = null;
		StringBuilder sb = new StringBuilder("");
		ArrayList<DataPackageUpload> recentChanges = new ArrayList<DataPackageUpload>();
		
		
		DataPackageRegistry dpr = new DataPackageRegistry(dbDriver,
			    dbURL, dbUser, dbPassword);
		boolean excludeDeleted = false;
		boolean excludeDuplicateUpdates = false;
		ArrayList<DataPackageUpload> inserts = dpr.getChanges("createDataPackage", fromDate, toDate, scope, limit, excludeDeleted, excludeDuplicateUpdates);
		ArrayList<DataPackageUpload> updates = dpr.getChanges("updateDataPackage", fromDate, toDate, scope, limit, excludeDeleted, excludeDuplicateUpdates);
		ArrayList<DataPackageUpload> deletes = dpr.getChanges("deleteDataPackage", fromDate, toDate, scope, limit, excludeDeleted, excludeDuplicateUpdates);
		
		recentChanges.addAll(inserts);
		recentChanges.addAll(updates);
		recentChanges.addAll(deletes);
		Collections.sort(recentChanges);
		
		sb.append("<dataPackageChanges>\n");
		for (DataPackageUpload dpu : recentChanges) {
			sb.append(dpu.toXML());
		}
		sb.append("</dataPackageChanges>\n");
		
		xml = sb.toString();
		
		return xml;
	}


	/**
	 * List the identifier values for data packages with the specified scope.
	 * 
	 * @param scope
	 *          the scope value
	 * @param includeDeleted
	 *          if true, included identifiers for deleted data packages,
	 *          else exclude them
	 * @return a newline-separated list of identifier values
	 */
	public String listDataPackageIdentifiers(String scope, boolean includeDeleted)
	    throws ClassNotFoundException, SQLException, IllegalArgumentException,
	    ResourceNotFoundException {
		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
		    dbURL, dbUser, dbPassword);
		String identifierListString = null;
		StringBuffer stringBuffer = new StringBuffer("");
		ArrayList<String> identifierList = 
				dataPackageRegistry.listDataPackageIdentifiers(scope, includeDeleted);

		// Throw a ResourceNotFoundException if the list is empty
		if (identifierList == null || identifierList.size() == 0) {
			String message = "No resources found for scope='" + scope + "'\n\n";
			throw new ResourceNotFoundException(message);
		}

		for (String identifier : identifierList) {
			stringBuffer.append(identifier + "\n");
		}

		identifierListString = stringBuffer.toString();
		return identifierListString;
	}

	
	/**
	 * List the revision values for data packages with the specified scope and
	 * identifier that are readable by the specified user.
	 * 
	 * @param scope
	 *          the scope value
	 * @param identifier
	 *          the identifier value
	 * @param user
	 *          the user name
	 * @return a newline-separated list of revision values
	 */
	public String listDataPackageRevisions(String scope, Integer identifier)
	    throws ClassNotFoundException, SQLException, IllegalArgumentException,
	    ResourceNotFoundException {
		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
		    dbURL, dbUser, dbPassword);
		String revisionListString = null;
		StringBuffer stringBuffer = new StringBuffer("");
		ArrayList<String> revisionList = dataPackageRegistry
		    .listDataPackageRevisions(scope, identifier);

		// Throw a ResourceNotFoundException if the list is empty
		if (revisionList == null || revisionList.size() == 0) {
			String message = "No resources found for scope='" + scope
			    + "', identifier='" + identifier + "'\n\n";
			throw new ResourceNotFoundException(message);
		}

		for (String revision : revisionList) {
			stringBuffer.append(revision + "\n");
		}

		revisionListString = stringBuffer.toString();
		return revisionListString;
	}
	
	
	/**
	 * List the scope values for all data packages that are readable by the
	 * specified user.
	 * 
	 * @param user
	 *          the user name
	 * @return a newline-separated list of scope values
	 */
	public String listDataPackageScopes() throws ClassNotFoundException,
	    SQLException {
		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
		    dbURL, dbUser, dbPassword);
		String scopeListString = null;
		StringBuffer stringBuffer = new StringBuffer("");
		ArrayList<String> scopeList = dataPackageRegistry.listDataPackageScopes();

		// Throw a ResourceNotFoundException if the list is empty
		if (scopeList == null || scopeList.size() == 0) {
			String message = "No resources found\n\n";
			throw new ResourceNotFoundException(message);
		}

		for (String scope : scopeList) {
			stringBuffer.append(scope + "\n");
		}

		scopeListString = stringBuffer.toString();
		return scopeListString;
	}

	
	/**
	 * Lists all data packages that have been deleted from the resource
	 * registry.
	 * 
	 * @return A newline-separated list of document id strings corresponding 
	 *         to the list of deleted data packages, where a document id is 
	 *         the packageId minus the revision value
	 */
	public String listDeletedDataPackages() throws ClassNotFoundException,
	    SQLException {
		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
		    dbURL, dbUser, dbPassword);
		String packageListString = null;
		StringBuffer stringBuffer = new StringBuffer("");
		ArrayList<String> packageList = dataPackageRegistry.listDeletedDataPackages();

		for (String dataPackage : packageList) {
			stringBuffer.append(dataPackage + "\n");
		}

		packageListString = stringBuffer.toString();
		return packageListString;
	}

	
	/**
	 * List the data packages in the resource registry where the principal_owner matches 
	 * the specified distinguished name value.
	 * 
	 * @param distinguishedName
	 *          the user distinguished name, e.g. "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org"
	 * @return a newline-separated list of packageId values (including the revision value)
	 */
	public String listUserDataPackages(String distinguishedName)
	    throws ClassNotFoundException, SQLException, IllegalArgumentException,
	    ResourceNotFoundException {
		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
		    dbURL, dbUser, dbPassword);
		String dataPackageListStr = null;
		StringBuffer stringBuffer = new StringBuffer("");
		ArrayList<String> dataPackageList = 
				dataPackageRegistry.listUserDataPackages(distinguishedName);

		for (String identifier : dataPackageList) {
			stringBuffer.append(identifier + "\n");
		}

		dataPackageListStr = stringBuffer.toString();
		return dataPackageListStr;
	}

	
	/**
	 * Lists all data packages that are actively being worked on.
	 * 
	 * @return An XML string, as composed by the DataPackageRegistry class.
	 */
	public String listWorkingOn() 
			throws Exception {
		DataPackageRegistry dataPackageRegistry = 
				new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
		String packageListString = dataPackageRegistry.listWorkingOn();
		return packageListString;
	}

	
	/**
	 * Loads Data Manager options from a configuration file.
	 */
	private static void loadOptions() throws Exception {
		try {
			// Load database connection options
			Options options = ConfigurationListener.getOptions();
			if (options == null) { throw new Exception("ConfigurationListener.getOptions() returned null."); }
			dbDriver = options.getOption("dbDriver");
			dbURL = options.getOption("dbURL");
			dbUser = options.getOption("dbUser");
			dbPassword = options.getOption("dbPassword");
			databaseAdapterName = options.getOption("dbAdapter");
			
			// Load scope registry
			String scopeRegistry = options.getOption("scopeRegistry");
			DataPackage.setScopeRegistry(scopeRegistry);

			// Load PASTA service options
			resourceDir = options.getOption("datapackagemanager.metadataDir");
			solrUrl = options
				 .getOption("datapackagemanager.metadatacatalog.solrUrl");
			pastaUriHead = options.getOption("datapackagemanager.pastaUriHead");
			pastaUser = options
			    .getOption("datapackagemanager.metadatacatalog.pastaUser");
			entityDir = options.getOption("datapackagemanager.entityDir");
			xslDir = options.getOption("datapackagemanager.xslDir");

			
			// Data Manager Library (DML) options
			String anonymousFtpPasswd = options.getOption("anonymousFtpPasswd");
			if (anonymousFtpPasswd != null) {
				DownloadHandler.setAnonymousFtpPasswd(anonymousFtpPasswd);
			}
			String qualityReportingStr = options.getOption("qualityReporting");
			String qualityReportTemplate = options.getOption("qualityReportTemplate");
			String emlDereferencerXSLT = options.getOption("emlDereferencerXSLT");
			String preferredFormatStringsURL = options.getOption("dml.preferredFormatStringsURL");
			if (qualityReportingStr != null) {
				if (qualityReportingStr.equalsIgnoreCase("true")) {
					QualityReport.setQualityReporting(true, qualityReportTemplate);
					QualityReport.setEmlDereferencerXSLTPath(emlDereferencerXSLT);
					QualityReport.setPreferredFormatStrings(preferredFormatStringsURL);
				} else if (qualityReportingStr.equalsIgnoreCase("false")) {
					QualityReport.setQualityReporting(false, null);
				}
			}
		} catch (Exception e) {
			logger.error("Error loading options: " + e.getMessage());
			e.printStackTrace();
			throw (e);
		}
	}

	
	/**
	 * Parse an EML document using the Data Manager Library method
	 * DataManager.parseMetadata().
	 * 
	 * @param emlDocument
	 *          the EML document to be parsed
	 * @param isEvaluate
	 *          true if this is an evaluate operation
	 * @return DataPackage the DataPackage object produced by parsing the EML
	 */
	public DataPackage parseEml(File emlFile, boolean isEvaluate)
	    throws FileNotFoundException, Exception {
		DataPackage dataPackage = null;

		if (emlFile != null) {
			FileInputStream fileInputStream = new FileInputStream(emlFile);
			if (fileInputStream != null) {
				if (dataManager != null) {
					dataPackage = dataManager.parseMetadata(fileInputStream);

					if (dataPackage != null) {
						if (!isEvaluate && dataPackage.hasDatasetQualityError()) {
							String qualityReportXML = dataPackage.getQualityReport().toXML();
							throw new UserErrorException(qualityReportXML);
						}
					}
				}
			}
		}

		return dataPackage;
	}

	
	/**
	 * Returns the entity name for the given entity resource identifier if it
	 * exists; otherwise, throw a ResourceNotFoundException. Authorization for
	 * reading the entity name is based on access rules for the data package
	 * resource, not on the access rules for the data entity resource.
	 * 
	 * @param dataPackageResourceId
	 *          the data package resource identifier, used for authorization
	 *          purposes
	 * @param entityResourceId
	 *          the entity resource identifier, used as the key to the entityName
	 *          value
	 * @param authToken
	 *          the authentication token object
	 * @return the data entity name string for this resource, if it exists
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws UnauthorizedException
	 * @throws ResourceNotFoundException
	 * @throws Exception
	 */
	public String readDataEntityName(String dataPackageResourceId,
	    String entityResourceId, AuthToken authToken)
	    throws ClassNotFoundException, SQLException, UnauthorizedException,
	    ResourceNotFoundException, Exception {

		String entityName = null;
		String user = authToken.getUserId();

		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);

			/*
			 * Check whether user is authorized to read the data entity. This is based
			 * on the access rules specified for the data package, not on the access
			 * rules specified for reading the data entity.
			 */
			Authorizer authorizer = new Authorizer(dataPackageRegistry);
			boolean isAuthorized = authorizer.isAuthorized(authToken,
			    dataPackageResourceId, Rule.Permission.read);
			if (!isAuthorized) {
				String gripe = "User "
				    + user
				    + " does not have permission to read the entity name for this resource: "
				    + entityResourceId;
				throw new UnauthorizedException(gripe);
			}

			entityName = dataPackageRegistry.getDataEntityName(entityResourceId);

			if (entityName == null) {
				String gripe = "An entityName value does not exist for this resource: "
				    + entityResourceId;
				throw new ResourceNotFoundException(gripe);
			}

		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return entityName;

	}

	
	/**
	 * For the specified data package, returns a newline-separated 
	 * list of strings, where each string contains an entity id followed by a
	 * comma followed by the name of that entity.
	 * 
	 * @param scope
	 *          the scope value
	 * @param identifier
	 *          the identifier value
	 * @param revision
	 *          the revision value
	 * @param authToken
	 *          the authorization token
	 * @return a newline-separated list of strings, where each string contains an entity id followed by a
	 *         comma followed by the entity name.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws UnauthorizedException
	 * @throws ResourceNotFoundException
	 * @throws Exception
	 */
	public String readDataEntityNames(String scope, Integer identifier, Integer revision, AuthToken authToken)
	    throws ClassNotFoundException, SQLException, UnauthorizedException,
	    	ResourceNotFoundException, Exception {
		String entityNames = null;
		String packageId = String.format("%s.%d.%d", scope, identifier, revision);
		StringBuilder stringBuilder = new StringBuilder("");
		
		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);

			boolean hasDataPackage = dataPackageRegistry.hasDataPackage(scope, identifier, revision.toString());
			if (!hasDataPackage) {
				String msg = String.format("Unknown data package: %s", packageId);
				throw new ResourceNotFoundException(msg);
			}
			
			Authorizer authorizer = new Authorizer(dataPackageRegistry);

			entityNames = dataPackageRegistry.getEntityNames(scope, identifier, revision);

			if (entityNames != null) {
				String[] lines = entityNames.split("\n");
				for (String line : lines) {
					if ((line != null) && (line.length() > 0)) {
						String[] keyValuePair = line.split(",");
						if (keyValuePair.length >= 2) {
							int firstCommaPosition = line.indexOf(',');
							String entityId = line.substring(0, firstCommaPosition);
							String entityName = line.substring(firstCommaPosition + 1);
							String resourceId = DataPackageManager.composeResourceId(ResourceType.data, scope, identifier, revision, entityId);
							boolean isAuthorized = authorizer.isAuthorized(authToken, resourceId, Rule.Permission.read);
							if (isAuthorized) {
								stringBuilder.append(String.format("%s,%s\n", entityId, entityName));
							}
						}
					}
				}
			}
		} 
		catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		} 
		catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		entityNames = stringBuilder.toString();
		return entityNames;
	}

	
	/**
	 * Reads a data entity and returns it as a byte array. The specified user must
	 * be authorized to read the data entity resource.
	 * 
	 * @param scope
	 *          The scope of the data package.
	 * @param identifier
	 *          The identifier of the data package.
	 * @param revision
	 *          The revision of the data package.
	 * @param entityId
	 *          The entityId of the data package.
	 * @param user
	 *          The user name
	 * @return a File object containing the locally stored entity data
	 */
	public File getDataEntityFile(String scope, Integer identifier,
	    String revision, String entityId, AuthToken authToken, String user)
	    throws ClassNotFoundException, SQLException, ClientProtocolException,
	    IOException, Exception {

		File file = null;
		boolean hasDataPackage = false;
		Integer revisionInt = new Integer(revision);
		String resourceId = composeResourceId(ResourceType.data, scope, identifier,
		    revisionInt, entityId);

		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);
			hasDataPackage = dataPackageRegistry.hasDataPackage(scope, identifier,
			    revision);

			if (!hasDataPackage) {
				String message = "Attempting to read a data entity that does not exist in PASTA: "
				    + resourceId;
				throw new ResourceNotFoundException(message);
			}

			/*
			 * If we have the data package but it was previously deleted (i.e.
			 * de-activated)
			 * 
			 * Note: This logic is no longer valid as of Ticket #912:
			 *       https://trac.lternet.edu/trac/NIS/ticket/912
			 *
			boolean isDeactivatedDataPackage = dataPackageRegistry
			    .isDeactivatedDataPackage(scope, identifier);
			if (isDeactivatedDataPackage) {
				String message = "Attempting to read a data entity that was previously deleted from PASTA: "
				    + resourceId;
				throw new ResourceDeletedException(message);
			}*/

			/*
			 * Now that we know that the data package is in the registry, check
			 * whether the user is authorized to read the data entity.
			 */
			Authorizer authorizer = new Authorizer(dataPackageRegistry);
			boolean isAuthorized = authorizer.isAuthorized(authToken, resourceId,
			    Rule.Permission.read);
			if (!isAuthorized) {
				String message = "User " + user
				    + " does not have permission to read this data entity: "
				    + resourceId;
				throw new UnauthorizedException(message);
			}

			DataManagerClient dataManagerClient = new DataManagerClient();
			String resourceLocation = dataPackageRegistry
			    .getResourceLocation(resourceId);
			file = dataManagerClient.getDataEntityFile(resourceLocation, scope,
			    identifier, revision, entityId);
		} catch (ClassNotFoundException e) {
			logger.error("Error connecting to Data Package Registry: "
			    + e.getMessage());
			e.printStackTrace();
			throw (e);
		} catch (SQLException e) {
			logger.error("Error connecting to Data Package Registry: "
			    + e.getMessage());
			e.printStackTrace();
			throw (e);
		}

		return file;

	}
	
	
	/**
	 * Reads a data package and returns it as a string representing a resource
	 * map. The specified user must be authorized to read the data package
	 * resource.
	 * 
	 * @param scope
	 *          The scope of the data package.
	 * @param identifier
	 *          The identifier of the data package.
	 * @param revisionStr
	 *          The revision of the data package.
	 * @param user
	 *          The user name
	 * @param oreFormat
	 *          If true, return the resource map in ORE formatted
	 *          as application/rdf+xml
	 */
	public String readDataPackage(String scope, Integer identifier,
	                              String revisionStr, AuthToken authToken, 
	                              String user, boolean oreFormat)
	        throws Exception {		
		boolean hasDataPackage = false;
		Integer revision = null;
		DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
		    dbURL, dbUser, dbPassword);
		String resourceMapStr = null;
		StringBuffer stringBuffer = new StringBuffer("");

		/*
		 * Handle symbolic revisions such as "newest" and "oldest".
		 */
		if (revisionStr != null) {
			if (revisionStr.equals("newest")) {
				revision = getNewestRevision(scope, identifier);
				if (revision != null) {
					hasDataPackage = true;
				}
			} else if (revisionStr.equals("oldest")) {
				revision = getOldestRevision(scope, identifier);
				if (revision != null) {
					hasDataPackage = true;
				}
			} else {
				hasDataPackage = dataPackageRegistry.hasDataPackage(scope, identifier,
				    revisionStr);
				if (hasDataPackage) {
					revision = new Integer(revisionStr);
				}
			}
		}
		
		/*
		 * Now that we know that the data package is in the registry, check whether
		 * the user is authorized to read it. If authorized, then get its resource
		 * map.
		 */
		if (hasDataPackage) {
			String entityId = null;
			String dataPackageId = composeResourceId(ResourceType.dataPackage, scope,
			    identifier, revision, entityId);
			
			/*
			 * Check whether user is authorized to read the data package
			 */
			Authorizer authorizer = new Authorizer(dataPackageRegistry);
			boolean isAuthorized = authorizer.isAuthorized(authToken, dataPackageId,
			    Rule.Permission.read);
			if (!isAuthorized) {
				String message = "User " + user
				    + " does not have permission to read this data package: "
				    + dataPackageId;
				throw new UnauthorizedException(message);
			}

			/*
			 * Get the resource map
			 */
			if (oreFormat) {
				// Return the resource map in ORE format (application/rdf+xml)
				ResourceMap resourceMap = new ResourceMap(this, pastaUriHead, scope, identifier, revision);
				resourceMapStr = resourceMap.toXML();
			} 
			else {
				// Return the resource map in simple plain text format (text/plain)
				ArrayList<String> resources = dataPackageRegistry.getDataPackageResources(scope, identifier, revision);

				for (String resourceId : resources) {
					stringBuffer.append(resourceId + "\n");
				}

				resourceMapStr = stringBuffer.toString();
			}
		} 
		else {
			if (!hasDataPackage) {
				String packageId = EMLDataPackage.composePackageId(scope, identifier,
				    revisionStr);
				String message = "Attempting to read a data package that does not exist in PASTA: "
				    + packageId;
				throw new ResourceNotFoundException(message);
			}
		}

		return resourceMapStr;
	}

	
	/**
	 * Reads the data package error message from the system.
	 * 
	 * @param transaction
	 *          The transaction identifier
	 * @return The error message
	 * @throws ResourceNotFoundException
	 */
	public String readDataPackageError(String transaction)
	    throws ResourceNotFoundException {

		String error = null;
		DataPackageError dpError = null;

		try {
			dpError = new DataPackageError();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		try {
			error = dpError.readError(transaction);
		} catch (FileNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}

		return error;

	}

	
	/**
	 * Read a data package quality report, returning the XML file. The specified
	 * user must be authorized to read the report.
	 * 
	 * @param scope
	 *          the scope value
	 * @param identifier
	 *          the identifier value
	 * @param revision
	 *          the revision value
	 * @param emlPackageId
	 *          an EmlPackageId object
	 * @param user
	 *          the user name
	 * @return a file object containing the data package quality report XML
	 */
	public File readDataPackageReport(String scope, Integer identifier,
	    String revision, EmlPackageId emlPackageId, AuthToken authToken,
	    String user) throws ClassNotFoundException, SQLException {
		boolean evaluate = false;
		File xmlFile = null;
		String transaction = null;

		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);
			Integer revisionInt = new Integer(revision);
			boolean hasDataPackage = dataPackageRegistry.hasDataPackage(scope,
			    identifier, revision);

			if (hasDataPackage) {
				String entityId = null;
				String reportId = composeResourceId(ResourceType.report, scope,
				    identifier, revisionInt, entityId);
				/*
				 * If we have the data package but it was previously deleted (i.e.
				 * de-activated)
				 * 
				 * Note: This logic is no longer valid as of Ticket #912:
				 *       https://trac.lternet.edu/trac/NIS/ticket/912
				 *
				 *
				boolean isDeactivatedDataPackage = dataPackageRegistry
				    .isDeactivatedDataPackage(scope, identifier);
				if (isDeactivatedDataPackage) {
					String message = "Attempting to read a data package report that was "
					    + "previously deleted from PASTA: " + reportId;
					throw new ResourceDeletedException(message);
				}
				*/

				/*
				 * Check whether user is authorized to read the data package report
				 */
				Authorizer authorizer = new Authorizer(dataPackageRegistry);
				boolean isAuthorized = authorizer.isAuthorized(authToken, reportId,
				    Rule.Permission.read);
				if (!isAuthorized) {
					String message = "User " + user
					    + " does not have permission to read this data package report: "
					    + reportId;
					throw new UnauthorizedException(message);
				}

				DataPackageReport dataPackageReport = new DataPackageReport(
				    emlPackageId);

				if (dataPackageReport != null) {
					xmlFile = dataPackageReport.getReport(evaluate, transaction);
				}
			}
		} finally {
		}

		return xmlFile;
	}

	
	/**
	 * Read an evaluate quality report, returning the XML file. The transaction id
	 * for the evaluate operation that generated the report must be specified.
	 * 
	 * @param transaction
	 *          the transaction identifier, e.g. "1364424858431"
	 * @return a file object containing the data package quality report XML
	 */
	public File readEvaluateReport(String transaction) {
		
		DataPackageReport dpr = new DataPackageReport(null);
		return dpr.getEvaluateReportFile(transaction);
		
	}

	
	/**
	 * Reads metadata from the Metadata Catalog and returns it as a String. The
	 * specified user must be authorized to read the metadata resource.
	 * 
	 * @param scope
	 *          The scope of the metadata document.
	 * @param identifier
	 *          The identifier of the metadata document.
	 * @param revision
	 *          The revision of the metadata document.
	 * @param user
	 *          The user name value
	 * @param authToken
	 *          The AuthToken object
	 * @return The metadata document, an XML string.
	 */
	public String readMetadata(String scope, Integer identifier, String revision,
	    String user, AuthToken authToken) throws ClassNotFoundException,
	    SQLException, ClientProtocolException, IOException, Exception {
		String entityId = null;
		String metadataXML = null;
		boolean hasDataPackage = false;
		Integer revisionInt = new Integer(revision);
		String metadataId = composeResourceId(ResourceType.metadata, scope,
		    identifier, revisionInt, entityId);
		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);
			hasDataPackage = dataPackageRegistry.hasDataPackage(scope, identifier,
			    revision);

			if (hasDataPackage) {
				/*
				 * Check whether user is authorized to read the data package metadata
				 */
				Authorizer authorizer = new Authorizer(dataPackageRegistry);
				boolean isAuthorized = authorizer.isAuthorized(authToken, metadataId,
				    Rule.Permission.read);
				if (!isAuthorized) {
					String message = "User " + user
					    + " does not have permission to read this metadata document: "
					    + metadataId;
					throw new UnauthorizedException(message);
				}

				EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
				EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope,
				    identifier.toString(), revision);
				DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(
				    emlPackageId);

				if (dataPackageMetadata != null) {
					boolean evaluateMode = false;
					File levelOneEMLFile = dataPackageMetadata.getMetadata(evaluateMode);
					try {
						metadataXML = FileUtils.readFileToString(levelOneEMLFile);
					}
					catch (IOException e) {
						logger.error("Error reading Level-1 metadata file: "
							    + e.getMessage());
							e.printStackTrace();
							throw (e);
					}
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("Error connecting to Data Package Registry: "
			    + e.getMessage());
			e.printStackTrace();
			throw (e);
		} catch (SQLException e) {
			logger.error("Error connecting to Data Package Registry: "
			    + e.getMessage());
			e.printStackTrace();
			throw (e);
		}

		return metadataXML;
	}

	
	/**
	 * Reads Dublin Core metadata from the Metadata Catalog and returns it as a String. The
	 * specified user must be authorized to read the metadata resource.
	 * 
	 * @param scope
	 *          The scope of the metadata document.
	 * @param identifier
	 *          The identifier of the metadata document.
	 * @param revision
	 *          The revision of the metadata document.
	 * @param user
	 *          The user name value
	 * @param authToken
	 *          The AuthToken object
	 * @return The Dublin Core metadata document, an XML string.
	 */
	public String readMetadataDublinCore(String scope, Integer identifier, String revision,
		    String user, AuthToken authToken) throws ClassNotFoundException,
		    SQLException, ClientProtocolException, IOException, Exception {
			String entityId = null;
			String metadataXML = null;
			boolean hasDataPackage = false;
			Integer revisionInt = new Integer(revision);
			String metadataId = composeResourceId(ResourceType.metadata, scope,
			    identifier, revisionInt, entityId);
			try {
				DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
				    dbDriver, dbURL, dbUser, dbPassword);
				hasDataPackage = dataPackageRegistry.hasDataPackage(scope, identifier,
				    revision);

				if (hasDataPackage) {
					/*
					 * Check whether user is authorized to read the data package metadata
					 */
					Authorizer authorizer = new Authorizer(dataPackageRegistry);
					boolean isAuthorized = authorizer.isAuthorized(authToken, metadataId,
					    Rule.Permission.read);
					if (!isAuthorized) {
						String message = "User " + user
						    + " does not have permission to read this metadata document: "
						    + metadataId;
						throw new UnauthorizedException(message);
					}

					EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
					EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope,
					    identifier.toString(), revision);
					DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(
					    emlPackageId);
					/*
					 * This tells the dataPackageMetadata object that we want to 
					 * retrieve Dublin Core instead of EML.
					 */
					dataPackageMetadata.setDublinCore(true);

					if (dataPackageMetadata != null) {
						boolean evaluateMode = false;
						File levelOneEMLFile = dataPackageMetadata.getMetadata(evaluateMode);
						try {
							metadataXML = FileUtils.readFileToString(levelOneEMLFile);
						}
						catch (IOException e) {
							logger.error("Error reading Level-1 metadata file: "
								    + e.getMessage());
								e.printStackTrace();
								throw (e);
						}
					}
				}
			} catch (ClassNotFoundException e) {
				logger.error("Error connecting to Data Package Registry: "
				    + e.getMessage());
				e.printStackTrace();
				throw (e);
			} catch (SQLException e) {
				logger.error("Error connecting to Data Package Registry: "
				    + e.getMessage());
				e.printStackTrace();
				throw (e);
			}

			return metadataXML;
		}

		
	/**
	 * Reads metadata from the Metadata Catalog and returns it as a String. The
	 * specified user must be authorized to read the metadata resource.
	 * 
	 * @param scope
	 *          The scope of the metadata document.
	 * @param identifier
	 *          The identifier of the metadata document.
	 * @param revision
	 *          The revision of the metadata document.
	 * @param user
	 *          The user name value
	 * @param authToken
	 *          The AuthToken object
	 * @return The metadata document, an XML string.
	 */
	public File getMetadataFile(String scope, Integer identifier,
	    String revision, String user, AuthToken authToken)
	    throws ClassNotFoundException, SQLException, ClientProtocolException,
	    IOException, Exception {
		String entityId = null;
		File levelOneEMLFile = null;
		boolean hasDataPackage = false;
		Integer revisionInt = new Integer(revision);
		String metadataId = composeResourceId(ResourceType.metadata, scope,
		    identifier, revisionInt, entityId);
		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);
			hasDataPackage = dataPackageRegistry.hasDataPackage(scope, identifier,
			    revision);

			if (hasDataPackage) {
				/*
				 * Check whether user is authorized to read the data package metadata
				 */
				Authorizer authorizer = new Authorizer(dataPackageRegistry);
				boolean isAuthorized = authorizer.isAuthorized(authToken, metadataId,
				    Rule.Permission.read);
				if (!isAuthorized) {
					String message = "User " + user
					    + " does not have permission to read this metadata document: "
					    + metadataId;
					throw new UnauthorizedException(message);
				}

				EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
				EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope,
				    identifier.toString(), revision);
				DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(
				    emlPackageId);

				if (dataPackageMetadata != null) {
					boolean evaluateMode = false;
					levelOneEMLFile = dataPackageMetadata.getMetadata(evaluateMode);
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("Error connecting to Data Package Registry: "
			    + e.getMessage());
			e.printStackTrace();
			throw (e);
		} catch (SQLException e) {
			logger.error("Error connecting to Data Package Registry: "
			    + e.getMessage());
			e.printStackTrace();
			throw (e);
		}

		return levelOneEMLFile;
	}

	
	/**
	 * Returns the access control list (ACL) for the given resource identifier if
	 * it exists; otherwise, throw a ResourceNotFoundException.
	 * 
	 * @param resourceId      The resource identifier
	 * @return  an XML string representing the access control list. The string includes
	 *          an entry for the owner/submitter although that entry does not appear
	 *          in the access_matrix table (the owner/submitter is stored only in the 
	 *          resource_registry table).
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws UnauthorizedException
	 * @throws ResourceNotFoundException
	 * @throws Exception
	 */
	public String readResourceAcl(String resourceId)
	    throws ClassNotFoundException, SQLException, UnauthorizedException,
	    ResourceNotFoundException, Exception {

		String acl = null;

		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);

			boolean hasResource = dataPackageRegistry.hasResource(resourceId);
			if (!hasResource) {
				String gripe = "Resource not found: " + resourceId;
				throw new ResourceNotFoundException(gripe);
			}

			acl = dataPackageRegistry.getResourceAcl(resourceId);
			if (acl == null) {
				String gripe = "An access control list (ACL) does not exist for this resource: " + resourceId;
				throw new ResourceNotFoundException(gripe);
			}

		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return acl;

	}

	
    /**
     * Returns the resource metadata for the given resource identifier if
     * it exists; otherwise, throw a ResourceNotFoundException.
     * 
     * @param resourceType    The type of resource whose resource metadata is to be retrieved
     * @param resourceId      The resource identifier
     * @return  an XML string representing the resource metadata for the specified resourceId.
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws UnauthorizedException
     * @throws ResourceNotFoundException
     * @throws Exception
     */
    public String readResourceMetadata(ResourceType resourceType, String resourceId)
        throws ClassNotFoundException, SQLException, UnauthorizedException,
        ResourceNotFoundException, Exception {

        String resourceMetadataXML = null;

        try {
            DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
                dbDriver, dbURL, dbUser, dbPassword);

            boolean hasResource = dataPackageRegistry.hasResource(resourceId);
            if (!hasResource) {
                String gripe = "Resource not found: " + resourceId;
                throw new ResourceNotFoundException(gripe);
            }

            resourceMetadataXML = dataPackageRegistry.getResourceMetadata(resourceType, resourceId);
            if (resourceMetadataXML == null) {
                String gripe = "Resource metadata does not exist for this resource: " + resourceId;
                throw new ResourceNotFoundException(gripe);
            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw (e);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return resourceMetadataXML;
    }

    
	/**
	 * For the specified data package, returns a newline-separated 
	 * list of strings, where each string contains an entity id followed by a
	 * comma followed by the size of that entity (in bytes).
	 * Throws a ResourceNotFoundException if the specified data package does
	 * not exist.
	 * 
	 * @param scope
	 *          the scope value
	 * @param identifier
	 *          the identifier value
	 * @param revision
	 *          the revision value
	 * @param authToken
	 *          the authorization token
	 * @return a newline-separated list of strings, where each string contains an entity id followed by a
	 *         comma followed by an integer representing the size (in bytes) of that entity.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws UnauthorizedException
	 * @throws ResourceNotFoundException
	 * @throws Exception
	 */
	public String readEntitySizes(String scope, Integer identifier, Integer revision, AuthToken authToken)
	    throws ClassNotFoundException, SQLException, UnauthorizedException,
	    	ResourceNotFoundException, Exception {
		String entitySizes = null;
		String packageId = String.format("%s.%d.%d", scope, identifier, revision);
		StringBuilder stringBuilder = new StringBuilder("");
		String user = authToken.getUserId();
		
		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);
			
			boolean hasDataPackage = dataPackageRegistry.hasDataPackage(scope, identifier, revision.toString());
			if (!hasDataPackage) {
				String msg = String.format("Unknown data package: %s", packageId);
				throw new ResourceNotFoundException(msg);
			}
			
			Authorizer authorizer = new Authorizer(dataPackageRegistry);

			entitySizes = dataPackageRegistry.getEntitySizes(scope, identifier, revision);

			if (entitySizes != null) {
				String[] lines = entitySizes.split("\n");
				for (String line : lines) {
					if ((line != null) && (line.length() > 0)) {
						String[] keyValuePair = line.split(",");
						if (keyValuePair.length == 2) {
							String entityId = keyValuePair[0];
							String entitySize = keyValuePair[1];
							String resourceId = DataPackageManager.composeResourceId(ResourceType.data, scope, identifier, revision, entityId);
							boolean isAuthorized = authorizer.isAuthorized(authToken, resourceId, Rule.Permission.read);
							if (isAuthorized) {
								stringBuilder.append(String.format("%s,%s\n", entityId, entitySize));
							}
						}
					}
				}
			}
		} 
		catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		} 
		catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		}

		entitySizes = stringBuilder.toString();
		return entitySizes;
	}

	
	/**
	 * Returns the SHA-1 checksum for the given resource identifier if
	 * it exists; otherwise, throw a ResourceNotFoundException.
	 * 
	 * @param resourceId   the resource identifier
	 * @param authToken    the authorization token
	 * @return the SHA-1 checksum string
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws UnauthorizedException
	 * @throws ResourceNotFoundException
	 * @throws Exception
	 */
	public String readResourceChecksum(String resourceId, AuthToken authToken)
	    throws ClassNotFoundException, SQLException, UnauthorizedException,
	    ResourceNotFoundException, Exception {

		String checksum = null;
		String user = authToken.getUserId();

		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);

			/*
			 * Check whether user is authorized to read the data package report
			 */
			Authorizer authorizer = new Authorizer(dataPackageRegistry);
			boolean isAuthorized = authorizer.isAuthorized(authToken, resourceId,
			    Rule.Permission.read);
			if (!isAuthorized) {
				String gripe = "User " + user
				    + " does not have permission to read the checksum for this resource: "
				    + resourceId;
				throw new UnauthorizedException(gripe);
			}

			checksum = dataPackageRegistry.getResourceShaChecksum(resourceId);

			if (checksum == null) {
				String gripe = "A checksum does not exist for this resource: " + resourceId;
				throw new ResourceNotFoundException(gripe);
			}

		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return checksum;

	}

	
	/**
	 * Returns the size value (in bytes) for the given resource identifier if
	 * it exists; otherwise, throw a ResourceNotFoundException.
	 * 
	 * @param resourceId   the resource identifier
	 * @param authToken    the authorization token
	 * @return the size value (in bytes)
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws UnauthorizedException
	 * @throws ResourceNotFoundException
	 * @throws Exception
	 */
	public Long readResourceSize(String resourceId, AuthToken authToken)
	    throws ClassNotFoundException, SQLException, UnauthorizedException,
	    ResourceNotFoundException, Exception {

		Long resourceSize = null;
		String user = authToken.getUserId();

		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);

			/*
			 * Check whether user is authorized to read the data package report
			 */
			Authorizer authorizer = new Authorizer(dataPackageRegistry);
			boolean isAuthorized = authorizer.isAuthorized(authToken, resourceId,
			    Rule.Permission.read);
			if (!isAuthorized) {
				String gripe = "User " + user
				    + " does not have permission to read the size value for this resource: "
				    + resourceId;
				throw new UnauthorizedException(gripe);
			}

			resourceSize = dataPackageRegistry.getResourceSize(resourceId);

			if (resourceSize == null) {
				String gripe = "A size value does not exist for this resource: " + resourceId;
				throw new ResourceNotFoundException(gripe);
			}

		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return resourceSize;

	}

	
	/**
	 * Returns the Digital Object Identifier for the given resource identifier if
	 * it exists; otherwise, throw a ResourceNotFoundException.
	 * 
	 * @param resourceId   the resource identifier
	 * @param authToken    the authorization token
	 * @return the Digital Object Identifier (DOI) value
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws UnauthorizedException
	 * @throws ResourceNotFoundException
	 * @throws Exception
	 */
	public String readResourceDoi(String resourceId, AuthToken authToken)
	    throws ClassNotFoundException, SQLException, UnauthorizedException,
	    ResourceNotFoundException, Exception {

		String doi = null;
		String user = authToken.getUserId();

		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);

			/*
			 * Check whether user is authorized to read the data package report
			 */
			Authorizer authorizer = new Authorizer(dataPackageRegistry);
			boolean isAuthorized = authorizer.isAuthorized(authToken, resourceId,
			    Rule.Permission.read);
			if (!isAuthorized) {
				String gripe = "User " + user
				    + " does not have permission to read the DOI for this resource: "
				    + resourceId;
				throw new UnauthorizedException(gripe);
			}

			doi = dataPackageRegistry.getDoi(resourceId);

			if (doi == null) {
				String gripe = "A DOI does not exist for this resource: " + resourceId;
				throw new ResourceNotFoundException(gripe);
			}

		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return doi;

	}

	
	/**
	 * Returns the format type stored for the given resource identifier if
	 * the resource exists; otherwise, throw a ResourceNotFoundException.
	 * 
	 * @param resourceId   the resource identifier
	 * @param authToken    the authorization token
	 * @return the format type
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws UnauthorizedException
	 * @throws ResourceNotFoundException
	 * @throws Exception
	 */
	public String readResourceFormatType(String resourceId, AuthToken authToken)
	    throws ClassNotFoundException, SQLException, UnauthorizedException,
	    ResourceNotFoundException, Exception {

		String formatType = null;
		String user = authToken.getUserId();

		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);

			/*
			 * Check whether user is authorized to read the resource
			 */
			Authorizer authorizer = new Authorizer(dataPackageRegistry);
			boolean isAuthorized = authorizer.isAuthorized(authToken, resourceId,
			    Rule.Permission.read);
			if (!isAuthorized) {
				String gripe = String.format(
						"User %s does not have permission to read this resource: %s", 
						user, resourceId);
				throw new UnauthorizedException(gripe);
			}

			formatType = dataPackageRegistry.getFormatType(resourceId);

			if (formatType == null) {
				String gripe = String.format(
						"A formatType does not exist for this resource: %s",
						resourceId);
				throw new ResourceNotFoundException(gripe);
			}

		} 
		catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		} 
		catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		}

		return formatType;
	}

	
	/**
	 * Run a Metadata Catalog query operation. The returned String is a
	 * PASTA-formatted resultset XML string.
	 * 
	 * @param uriInfo  
	 *          A JAX-RS UriInfo object from which the query parameters 
	 *          can be accessed.
	 * @param user
	 *          The user name value
	 * @param authToken
	 *          The authentication token object
	 * @return The resultset XML string
	 */
	public String searchDataPackages(UriInfo uriInfo, String user,
	    AuthToken authToken) 
	    		throws ClientProtocolException, IOException, Exception {
		MetadataCatalog solrCatalog = new SolrMetadataCatalog(solrUrl);
		String solrXML = solrCatalog.query(uriInfo);
		
		return solrXML;
	}
	
	
	/**
	 * Calculates and stores the MD5 and SHA-1 checksums of a PASTA resource in the 
	 * data package registry.
	 * 
	 * @param resourceId         The PASTA resource identifier string
	 * @param file               The file object whose checksum is to be calculated
	 */
	public void storeChecksums(String resourceId, File file) {
		try {
			storeMD5Checksum(resourceId, file);
			storeSHA1Checksum(resourceId, file);
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	
	/**
	 * Calculates and stores the MD5 checksum of a PASTA resource in the 
	 * data package registry.
	 * 
	 * @param resourceId         The PASTA resource identifier string
	 * @param file               The file object whose MD5 checksum is to be calculated
	 */
	public void storeMD5Checksum(String resourceId, File file) {
		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
				    dbDriver, dbURL, dbUser, dbPassword);
			String md5Checksum = DigestUtilsWrapper.getMD5Checksum(file);
			dataPackageRegistry.updateMD5Checksum(resourceId, md5Checksum);
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	
	/**
	 * Calculates and stores the SHA-1 checksum of a PASTA resource in the 
	 * data package registry.
	 * 
	 * @param resourceId         The PASTA resource identifier string
	 * @param file               The file object whose SHA-1 checksum is to be calculated
	 */
	public void storeSHA1Checksum(String resourceId, File file) {
		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
				    dbDriver, dbURL, dbUser, dbPassword);
			String sha1Checksum = DigestUtilsWrapper.getSHA1Checksum(file);
			dataPackageRegistry.updateSHA1Checksum(resourceId, sha1Checksum);
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	
	/**
	 * Stores the data format of a PASTA data entity resource in the data package registry.
	 * 
	 * @param resourceId   The PASTA resource identifier string
	 * @param dataFormat   The data format value to be stored, e.g. "text/csv"
	 */
	public void storeDataFormat(String resourceId, String dataFormat) {
		try {
			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
				    dbDriver, dbURL, dbUser, dbPassword);
			dataPackageRegistry.updateDataFormat(resourceId, dataFormat);
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	
	/**
	 * Stores the size in bytes of a PASTA resource in the data package registry.
	 * 
	 * @param resourceId       the PASTA resource identifier string
	 * @param file             the file resource whose resource_size is to be stored
	 */
	public void storeResourceSize(String resourceId, File file) {
		try {
			if (file != null && file.exists()) {
				DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
					    dbDriver, dbURL, dbUser, dbPassword);
				long fileLength = file.length();
				dataPackageRegistry.updateResourceSize(resourceId, fileLength);
			}
			else {
				throw new Exception("Unable to determine size of resource: " + resourceId);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	
    /**
     * Stores the filename of a PASTA resource in the data package registry.
     * 
     * @param resourceId  the PASTA resource identifier string
     * @param filename    the filename value to be stored
     */
    public void storeResourceFilename(String resourceId, String filename) {
        try {
            if (filename != null) {
                DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
                        dbDriver, dbURL, dbUser, dbPassword);
                dataPackageRegistry.updateResourceFilename(resourceId, filename);
            }
            else {
                throw new Exception("filename null for resource: " + resourceId);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    
	/**
	 * Update a data package in PASTA and return a resource map of the created
	 * resources.
	 * 
	 * @param emlFile
	 *          The EML document to be created in the metadata catalog
	 * @param scope
	 *          The scope value
	 * @param identifier
	 *          The identifier value
	 * @param user
	 *          The user value
	 * @param authToken
	 *          The authorization token
	 * @param transaction
	 *          The transaction identifier
	 * @return The resource map generated as a result of updating the data
	 *         package.
	 */
	public String updateDataPackage(File emlFile, String scope,
	    Integer identifier, String user, AuthToken authToken, String transaction,
	    boolean useChecksum)
	    throws ClientProtocolException, FileNotFoundException, IOException,
	    UserErrorException, Exception {
		boolean isEvaluate = false;
		String resourceMap = null;

		// Construct an EMLDataPackage object
		DataPackage dataPackage = parseEml(emlFile, isEvaluate); // Parse EML

		if (dataPackage != null) {
			EMLDataPackage levelZeroDataPackage = new EMLDataPackage(dataPackage);

			// Get the packageId from the EMLDataPackage object
			String packageId = levelZeroDataPackage.getPackageId();

			String emlScope = levelZeroDataPackage.getScope();
			if (!scope.equals(emlScope)) {
				String message = "The scope value specified in the URL ('"
				    + scope
				    + "') does not match the scope value specified in the EML packageId attribute ('"
				    + emlScope + "').";
				throw new UserErrorException(message);
			}

			Integer emlIdentifier = levelZeroDataPackage.getIdentifier();
			if (!identifier.equals(emlIdentifier)) {
				String message = "The identifier value specified in the URL ('"
				    + identifier
				    + "') does not match the identifier value specified in the EML packageId attribute ('"
				    + emlIdentifier + "').";
				throw new UserErrorException(message);
			}

			/* Is this a Level 1 data package?
			if (levelZeroDataPackage.isLevelOne()) {
				String message = "The data package you are attempting to update, '"
				    + packageId
				    + "', is a Level-1 data package. Only Level-0 data packages may be "
				    + "inserted into PASTA.";
				throw new UserErrorException(message);
			}
			*/

			// Is this discovery-level EML?
			if (!levelZeroDataPackage.hasEntity()) {
				String message = "The data package you are attempting to insert, '"
				    + packageId
				    + "', does not describe a data entity. You may be attempting to "
				    + "insert a discovery-level data package into PASTA. Please note "
				    + "that only Level-0 data packages containing at least one data "
				    + "entity may be inserted into PASTA.";
				throw new UserErrorException(message);
			}

			DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(
			    dbDriver, dbURL, dbUser, dbPassword);

			/*
			 * If we have the data package but it was previously deleted (i.e.
			 * de-activated)
			 */
			boolean isDeactivatedDataPackage = dataPackageRegistry
			    .isDeactivatedDataPackage(scope, identifier);
			if (isDeactivatedDataPackage) {
				String message = "Attempting to update a data package that was previously deleted from PASTA: "
				    + levelZeroDataPackage.getDocid();
				throw new ResourceDeletedException(message);
			}

			// Do we already have this data package in PASTA?
			boolean hasDataPackage = dataPackageRegistry.hasDataPackage(scope,
			    identifier);

			/*
			 * If we do not have this data package in PASTA, throw an exception
			 */
			if (!hasDataPackage) {
				String message = "Attempting to update a data package that does not exist in PASTA: "
				    + levelZeroDataPackage.getDocid();
				throw new ResourceNotFoundException(message);
			}

			/*
			 * Get the newest revision for this data package; is the revision higher
			 * than the newest revision in PASTA?
			 */
			Integer revision = levelZeroDataPackage.getRevision();
			Integer newestRevision = dataPackageRegistry.getNewestRevision(scope,
			    identifier);
			if (revision <= newestRevision) {
				String message = "Attempting to update a data package to revision '"
				    + revision + "' but an equal or higher revision ('"
				    + newestRevision + "') " + "already exists in PASTA: " + packageId;
				throw new ResourceExistsException(message);
			}

			/*
			 * Check whether user is authorized to update the data package by looking
			 * at the access control rules for the newest revision
			 */
			String entityId = null;

			String resourceId = composeResourceId(ResourceType.dataPackage, scope,
			    identifier, newestRevision, entityId);
			Authorizer authorizer = new Authorizer(dataPackageRegistry);
			boolean isAuthorized = authorizer.isAuthorized(authToken, resourceId,
			    Rule.Permission.write);
			if (!isAuthorized) {
				String message = "User " + user
				    + " does not have permission to update this data package: "
				    + resourceId;
				throw new UnauthorizedException(message);
			}

			// Is this data package actively being worked on in PASTA?
			WorkingOn workingOn = dataPackageRegistry.makeWorkingOn();
			boolean isActive = workingOn.isActive(scope, identifier, revision);

			/*
			 * If this data package is already active in PASTA, throw an exception
			 */
			if (isActive) {
				String message = "Attempting to update a data package that is currently being processed in PASTA: "
				    + levelZeroDataPackage.getDocid();
				throw new UserErrorException(message);
			}

			boolean isUpdate = true;
			resourceMap = createDataPackageAux(emlFile, levelZeroDataPackage,
			    dataPackageRegistry, packageId, scope, identifier, revision, user,
			    authToken, isUpdate, isEvaluate, transaction, useChecksum);
		}

		// Return the resource map
		return resourceMap;
	}

	
	/**
	 * Writes the data package error message to the system.
	 * 
	 * @param transaction
	 *          The transaction identifier
	 * @param error
	 *          The exception object of the error
	 */
	public void writeDataPackageError(String transaction, Exception error) {
		DataPackageError dpError = null;
		error.printStackTrace();

		try {
			dpError = new DataPackageError();
			dpError.writeError(transaction, error);
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	
	/**
	 * Deletes the data package error message from the system.
	 * 
	 * @param transaction
	 *          The transaction identifier
	 * @throws FileNotFoundException
	 */
	public void deleteDataPackageError(String transaction)
	    throws ResourceNotFoundException {

		DataPackageError dpError = null;

		try {
			dpError = new DataPackageError();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		try {
			dpError.deleteError(transaction);
		} catch (FileNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}

	}
	
	
	/**
	 * Generate an "archive" of the data package by parsing and retrieving
	 * components of the data package resource map
	 * 
	 * @param scope
	 *          The scope value of the data package
	 * @param identifier
	 *          The identifier value of the data package
	 * @param revision
	 *          The revision value of the data package
	 * @param map
	 *          The resource map of the data package
	 * @param authToken
	 *          The authentication token of the user requesting the archive
	 * @param transaction
	 *          The transaction id of the request
	 * @return The file path to the data package archive
	 * @throws Exception 
	 * @throws IOException
	 */
	public String createDataPackageArchive(String scope, Integer identifier,
	    Integer revision, String userId, AuthToken authToken, String transaction)
	    throws Exception {

		String archiveName = null;
		DataPackageArchive dataPackageArchive = null;

		try {
			dataPackageArchive = new DataPackageArchive();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		if (dataPackageArchive != null) {
			try {
				archiveName = dataPackageArchive.createDataPackageArchive(scope, identifier, revision, userId,
				    authToken, transaction, xslDir);
			} catch (Exception e) {
				throw e;
			}
		}
		
		return archiveName;

	}	
	
	
	/**
	 * Returns the File object of the data package archive identified by the
	 * transaction identifier.
	 * 
	 * @param transaction
	 *          The transaction identifier of the data package archive.
	 * @return The archive File object
	 * @throws FileNotFoundException
	 */
	public File getDataPackageArchiveFile(String transaction)
	    throws ResourceNotFoundException {

		File file = null;

		DataPackageArchive archive = null;

		try {
			archive = new DataPackageArchive();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		try {
		file = archive.getDataPackageArchiveFile(transaction);
		} catch (FileNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}

		return file;

	}
	
	
	/**
	 * Deletes the data package archive identified by the transaction identifier.
	 * 
	 * @param transaction
	 *          The transaction identifier of the data package archive.
	 * @throws FileNotFoundException
	 */
	public void deleteDataPackageArchive(String transaction)
	    throws FileNotFoundException {

		DataPackageArchive archive = null;

		try {
			archive = new DataPackageArchive();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		archive.deleteDataPackageArchive(transaction);
	}
	
	
  /*
   * Matches the specified 'entityName' value with the entity names
   * found in the EML document string, and returns the corresponding 
   * objectName value for the matching entity, if an objectName was
   * specified for the matching entity.
   * 
   * Returns null if:
   *   (1) The EML document fails to parse, or
   *   (2) No entities match the specified entityName value, or
   *   (3) The matching entity does not specify an objectName in
   *       the EML document.
   */
  protected String findObjectName(String xml, String entityName) {
    String objectName = null;
    EMLParser emlParser = new EMLParser();
    
    if (xml != null && entityName != null) {
      try {
        InputStream inputStream = IOUtils.toInputStream(xml, "UTF-8");
        edu.lternet.pasta.common.eml.DataPackage dataPackage = emlParser.parseDocument(inputStream);
        
        if (dataPackage != null) {
          objectName = dataPackage.findObjectName(entityName);
        }
      }
      catch (Exception e) {
        logger.error("Error parsing EML metacdata: " + e.getMessage());
      }
    }
    
    return objectName;
  }

  
  /**
   * For a given packageId and entityName, find the corresponding objectName value as documented in the EML.
   * 
   * @param emlPackageId    the EML packageId object
   * @param entityName      the entity name value
   * @return objectName     the corresponding objectName value for that data entity, if it exists
   */
    public String getEntityObjectName(EmlPackageId emlPackageId, String entityName) {
        String xml = null;
        String objectName = null;
        EMLParser emlParser = new EMLParser();

        if (emlPackageId != null && entityName != null) {
            try {
                xml = getMetadataXML(emlPackageId);
                InputStream inputStream = IOUtils.toInputStream(xml, "UTF-8");
                edu.lternet.pasta.common.eml.DataPackage dataPackage = emlParser.parseDocument(inputStream);

                if (dataPackage != null) {
                    objectName = dataPackage.findObjectName(entityName);
                }
            } 
            catch (Exception e) {
                logger.error(String.format("Error parsing EML metacdata for %s: %s",
                                           emlPackageId.toString(), e.getMessage()));
            }
        }

        return objectName;
    }  
  
    
    /*
     * Read and return the metadata XML string for the specified packageId.
     */
    private String getMetadataXML(EmlPackageId emlPackageId) throws IOException {
        String metadataXML = null;
        DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(emlPackageId);

        if (dataPackageMetadata != null) {
            boolean evaluateMode = false;
            File levelOneEMLFile = dataPackageMetadata.getMetadata(evaluateMode);
            try {
                metadataXML = FileUtils.readFileToString(levelOneEMLFile);
            } 
            catch (IOException e) {
                logger.error("Error reading Level-1 metadata file: " + e.getMessage());
                e.printStackTrace();
                throw (e);
            }
        }

        return metadataXML;
    }
    
    
    /*
     * Journal citation methods
     */

    public JournalCitation createJournalCitation(String userId, String xml) 
            throws ClassNotFoundException, SQLException, ResourceNotFoundException, UserErrorException {
        LocalDateTime now = LocalDateTime.now();
        JournalCitation journalCitation = new JournalCitation(xml);
        
        if (journalCitation != null) {
            journalCitation.setPrincipalOwner(userId);
            journalCitation.setDateCreated(now);       
            DataPackageRegistry dpr = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
            String packageId = journalCitation.getPackageId();
            EmlPackageIdFormat epif = new EmlPackageIdFormat();
            EmlPackageId epi = epif.parse(packageId);

            if (epi != null) {
                String scope = epi.getScope();
                Integer identifier = epi.getIdentifier();
                Integer revision = epi.getRevision();
                boolean hasDataPackage = dpr.hasDataPackage(scope, identifier, revision.toString());
                if (hasDataPackage) {
                    dpr.addJournalCitation(journalCitation);
                    updateDataCiteMetadata(dpr, packageId);
                }
                else {
                    throw new ResourceNotFoundException(
                            String.format(
                                "No data package for packageId '%s' was found in the data repository",
                                packageId));
                }
            }
            else {
                throw new UserErrorException(
                    String.format(
                        "Error parsing packageId value '%s'. Please specify a valid scope.identifier.revision.",
                        packageId));
            }
        }
        
        return journalCitation;
    }
    
    
    private void updateDataCiteMetadata(DataPackageRegistry dataPackageRegistry,
                                        String packageId) 
            throws SQLException {
        boolean publicOnly = true;
        ArrayList<Resource> resourceList = 
                dataPackageRegistry.listDataPackageResources(packageId, publicOnly);
        if (resourceList != null) {
            for (Resource resource : resourceList) {
                if (resource.getResourceType().equals("dataPackage") &&
                    resource.getDoi() != null
                   ) {
                    try {
                        String doi = doiScanner.processOneResource(resource);
                    }
                    catch (Exception e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    
    public Integer deleteJournalCitation(Integer id, String userId) 
            throws ClassNotFoundException, SQLException {
        Integer deletedId = null;
        
        if (id != null && userId != null) {
            DataPackageRegistry dpr = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
            ArrayList<JournalCitation> journalCitations = dpr.getCitationWithId(id);
            if (journalCitations != null && journalCitations.size() > 0) {
                JournalCitation journalCitation = journalCitations.get(0);
                String packageId = journalCitation.getPackageId();
                deletedId = dpr.deleteJournalCitation(id, userId);
                if (deletedId != null) {
                    updateDataCiteMetadata(dpr, packageId);
                }
            }
        }
        
        return deletedId;
    }
    
    
   public String getCitationWithId(Integer journalCitationId, AuthToken authToken) 
            throws Exception {
        boolean includeDeclaration = true;
        String journalCitationsXML = null;
        StringBuilder stringBuilder = new StringBuilder("");

        DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
                dbURL, dbUser, dbPassword);
        
        ArrayList<edu.lternet.pasta.datapackagemanager.JournalCitation> journalCitations = 
                dataPackageRegistry.getCitationWithId(journalCitationId);
        
        if (journalCitations.size() == 0) {
            String msg = String.format("No journal citations found for id value %d", journalCitationId);
            throw new ResourceNotFoundException(msg);
        }
        
        for (JournalCitation journalCitation : journalCitations) {
            stringBuilder.append(journalCitation.toXML(includeDeclaration));
        }

        journalCitationsXML = stringBuilder.toString();
        return journalCitationsXML;
    }


   public String listDataPackageCitations(String scope, Integer identifier, Integer revision, AuthToken authToken) 
           throws Exception {
       boolean includeDeclaration = false;
       String journalCitationsXML = null;
       StringBuilder stringBuilder = new StringBuilder("");
       EmlPackageId epi = new EmlPackageId(scope, identifier, revision);
       EmlPackageIdFormat epif = new EmlPackageIdFormat();
       String packageId = epif.format(epi);

       DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
               dbURL, dbUser, dbPassword);
       
       ArrayList<edu.lternet.pasta.datapackagemanager.JournalCitation> journalCitations = 
               dataPackageRegistry.listDataPackageCitations(packageId);
       
       stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
       stringBuilder.append("<journalCitations>\n");

       for (JournalCitation journalCitation : journalCitations) {
           stringBuilder.append(journalCitation.toXML(includeDeclaration));
       }

       stringBuilder.append("</journalCitations>\n");

       journalCitationsXML = stringBuilder.toString();
       return journalCitationsXML;
   }


   public String listPrincipalOwnerCitations(String principalOwner, AuthToken authToken) 
           throws Exception {
       boolean includeDeclaration = false;
       String journalCitationsXML = null;
       StringBuilder stringBuilder = new StringBuilder("");

       DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
               dbURL, dbUser, dbPassword);
       
       ArrayList<edu.lternet.pasta.datapackagemanager.JournalCitation> journalCitations = 
               dataPackageRegistry.listPrincipalOwnerCitations(principalOwner);
       
       stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
       stringBuilder.append("<journalCitations>\n");

       for (JournalCitation journalCitation : journalCitations) {
           stringBuilder.append(journalCitation.toXML(includeDeclaration));
       }

       stringBuilder.append("</journalCitations>\n");

       journalCitationsXML = stringBuilder.toString();
       return journalCitationsXML;
   }


}
