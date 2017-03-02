/**
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

package edu.lternet.pasta.datamanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.dml.DataManager;
import edu.lternet.pasta.dml.database.ConnectionNotAvailableException;
import edu.lternet.pasta.dml.database.DatabaseConnectionPoolInterface;
import edu.lternet.pasta.dml.download.DataStorageInterface;
import edu.lternet.pasta.dml.download.EcogridEndPointInterface;
import edu.lternet.pasta.dml.parser.DataPackage;
import edu.lternet.pasta.dml.parser.Entity;
import edu.lternet.pasta.dml.sample.EcogridEndPoint;

import edu.ucsb.nceas.utilities.Options;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;

/**
 * 
 * EMLDataManager is  EML-centric in that data is managed by first 
 * parsing the EML metadata to discover the data entities described 
 * therein. 
 * 
 * @author dcosta
 * @created 18-Nov-2010 4:30:02 PM
 */
public class EMLDataManager implements DatabaseConnectionPoolInterface {

  /*
   * Class fields
   */
  
  private static String metadataDir = null;
  private static final String METADATA_DIR_DEFAULT = "/home/pasta/local/metadata";
  private static String entityDir = null;
  private static final String ENTITY_DIR_DEFAULT = "/home/pasta/local/data";
  private static String evaluateReportDir = null;
  private static final String EVALUATE_REPORT_DIR_DEFAULT = "/home/pasta/local/report";

  
  /*
   * These fields will be assigned values when the properties file is loaded. 
   */
  private static String dbDriver = null;
  private static String dbURL = null;
  private static String dbUser = null;
  private static String dbPassword = null;
  private static String databaseAdapterName = null;


  /*
   * Instance fields
   */
  
  private Logger logger = Logger.getLogger(EMLDataManager.class);
  
  // An instance of the DataManager class. This object provides the 
  // calling application access to all the public methods exposed by the 
  // Data Manager Library API.
  private DataManager dataManager;

  // A DataStorageInterface object that this class is associated with.
  // The calling application must use an object of this type to interact
  // with the Data Manager's download manager (see testDownloadData()).
  private EMLDataLoader emlDataLoader = null;
  
  private DataStorageInterface[] dataStorageList = new DataStorageInterface[1];

  // The calling application needs to be associated with an
  // EcogridEndPointInterface object for loading data to the database.
  // (See testLoadDataToDB()).
  private EcogridEndPointInterface eepi = null;
  
  
  
  /*
   * Constructors
   */
  
  /**
   * Constructs an EMLDataManager object and initializes several
   * of its instance fields.
   */
  public EMLDataManager()
          throws Exception {
    loadOptions();
    dataManager = DataManager.getInstance(this, databaseAdapterName);
    emlDataLoader = new EMLDataLoader();
    dataStorageList[0] = emlDataLoader;
    eepi = new EcogridEndPoint();
  }

  
  /*
   * Class methods
   */
  
  /** 
   * Gets the value of the entity directory. Returns
   * the configured property value if set, else
   * returns the default value.
   * 
   * @return  the path of the entity directory
   */
  public static String getEntityDir() {
    if (entityDir != null) {
      return entityDir;
    }
    else {
      return ENTITY_DIR_DEFAULT;
    }
  }
  
  
  /** 
   * Gets the value of the metadata directory. Returns
   * the configured property value if set, else
   * returns the default value.
   * 
   * @return  the path of the metadata directory
   */
  public static String getMetadataDir() {
    if (metadataDir != null) {
      return metadataDir;
    }
    else {
      return METADATA_DIR_DEFAULT;
    }
  }
  
  
  /** 
   * Gets the value of the report directory. Returns
   * the configured property value if set, else
   * returns the default value.
   * 
   * @return  the path of the metadata directory
   */
  public static String getEvaluateReportDir() {
    if (evaluateReportDir != null) {
      return evaluateReportDir;
    }
    else {
      return EVALUATE_REPORT_DIR_DEFAULT;
    }
  }
  
  
  /*
   * Instance methods
   */
  
  /* Implemented methods to satisfy the DatabaseConnectionPoolInterface 
     interface: getConnection(), getDBAdapterName(), and returnConnection() */
  
  /**
   * Gets a database connection from the pool. Implementation of this method is 
   * required by the DatabaseConnectionPoolInterface. Note that in this
   * example, there is no actual pool of connections. A full-fledged
   * application should manage a pool of connections that can be re-used.
   * 
   * @return checked out connection
   * @throws SQLException
   */
  public Connection getConnection()
          throws SQLException, ConnectionNotAvailableException {
    Connection connection = null;

    try {
      Class.forName(dbDriver);
    } 
    catch (java.lang.ClassNotFoundException e) {
      System.err.print("ClassNotFoundException: ");
      System.err.println(e.getMessage());
      throw (new SQLException(e.getMessage()));
    }

    try {
      connection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
    } 
    catch (SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw (e);
    }

    return connection;
  }


  /**
   * Get database adpater name. Implementation of this method is required by
   * the DatabaseConnectionPoolInterface.
   * 
   * @return database adapter name, for example, "PostgresAdapter"
   */
  public String getDBAdapterName() {
    return databaseAdapterName;
  }


  /**
   * Returns checked out database connection to the pool.
   * Implementation of this method is required by the 
   * DatabaseConnectionPoolInterface.
   * Note that in this example, there is no actual pool of connections
   * to return the connection to. A full-fledged
   * application should manage a pool of connections that can be re-used.
   * 
   * @param  conn, Connection that is being returned
   * @return boolean indicator if the connection was returned successfully
   */
  public boolean returnConnection(Connection conn) {
    boolean success = false;

    try {
      if (conn != null) conn.close();
      success = true;
    } 
    catch (Exception e) {
      // If the return fails, don't throw an Exception,
      // just return a fail status
      success = false;
    }

    return success;
  }
  

	/**
	 * Implements the Create Data Entities use case.
	 * 
	 * @param dataPackage  the DataPackage object
	 * @param evaluateMode boolean to determine whether the create
	 *                 operation should be run in evaluate mode.
	 * @param transaction  the transaction identifier
	 * @return         a map of entityIds mapped to their associated entityURLs
	 */
	public Map<String, String> createDataEntities(DataPackage dataPackage,
	                                            boolean evaluateMode,
	                                            String transaction) 
	        throws IOException, 
	               MalformedURLException, 
	               ResourceExistsException, 
	               IllegalStateException, 
	               Exception {
		Map<String, String> entityIdNamePairs = new LinkedHashMap<String, String>();
		emlDataLoader.setEvaluateMode(evaluateMode);
		
		if (dataPackage != null) {
      String packageId = dataPackage.getPackageId();
      
      if (packageId == null || packageId.equals("")) {
        String message = "No packageId found in document. This may not be a valid EML document.";
        throw new Exception(message);
      }

      Entity[] entityArray = dataPackage.getEntityList();
      EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
      EmlPackageId emlPackageId = emlPackageIdFormat.parse(packageId);

      try {
		    if (entityArray != null) {
		      for (Entity entity : entityArray) {
		        
		        /*
		         * Do not attempt to process an entity if the entity
		         * does not have a distribution online and has either 
		         * distribution offline or inline.
		         */
		        if ((entity != null) && 
		            !entity.hasDistributionOnline() &&
		            (entity.hasDistributionOffline() || entity.hasDistributionInline())
		           ) {
		          logger.warn("Data loading was not attempted for entity '" +
		                      entity.getName() + "' " +
		                      "because its distribution is 'inline' or 'offline'.");
		        }
						else {
							EMLEntity emlEntity = new EMLEntity(entity);
							String entityId = emlEntity.getEntityId();
							String entityName = emlEntity.getEntityName();
							// String entityURL = deriveDataURL(emlPackageId,
							// entityId);
							entityIdNamePairs.put(entityId, entityName);

							String url = emlEntity.getUrl();
							emlDataLoader.putUrlMapEntries(url, emlPackageId, entityId);

							// Download the entity
							downloadEntity(emlPackageId, emlEntity, evaluateMode);

							/*
							 * Load entity into a database unless it's an image
							 * entity (spatialRaster or spatialVector) or an
							 * otherEntity or it uses an externally defined
							 * format
							 */
							if (!entity.getIsImageEntity() && !entity.isExternallyDefinedFormat()
									&& !entity.isOtherEntity()) {
								loadEntity(emlPackageId, emlEntity);
							}
						}
					  }
		      
          /*
           * Delete the entities from the Data Manager Library's Data Registry.
           * We don't need the relational database tables to persist, only the
           * file system entities.
           */
          if (dataManager != null) {
            try {
              dataManager.dropTables(packageId);
            }
            catch (Exception e) {
              logger.error(
                  "Error dropping data entities from the Data Manager Library: " + 
                  e.getMessage());
              e.printStackTrace();
              throw(e);
            }
          }
		    }
		  }
      catch (SQLException e) {
        logger.error("Error connecting to Data Cache Registry: " + 
                     e.getMessage());
        e.printStackTrace();
        throw(e);
      }
      finally {
        EMLDataPackage emlDataPackage = new EMLDataPackage(dataPackage);
        EMLQualityReport emlQualityReport = new EMLQualityReport(emlPackageId, emlDataPackage);
        emlQualityReport.storeQualityReport(evaluateMode, transaction);
      }
    }
		else {
		  logger.error("parseEml() returned null data package");
		  throw new IllegalStateException("parseEml() returned null data package");
		}

		return entityIdNamePairs;
	}

	
	/**
	 * Downloads and stores a data entity.
	 * 
	 * @param emlPackageId     an EmlPackageId object
	 * @param entityId         the entity id
	 * @param entity           an Entity object
	 * @param evaluateMode     boolean to determine whether the downloadEntity
	 *                         operation should be run in evaluate mode.
	 * @throws IllegalStateException if the Data Manager Library indicates
	 *         that the entity failed to download successfully
	 */
	public void downloadEntity(EmlPackageId emlPackageId,
	                              EMLEntity emlEntity,
	                              boolean evaluateMode)
	        throws IllegalStateException {
	  boolean success = false;
	  boolean preserveFormat = true;
	  Entity entity = emlEntity.getEntity();
	  String entityName = emlEntity.getEntityName();
	  String entityId = emlEntity.getEntityId();
	  String packageId = emlPackageId.toString();
	  
	  if (dataManager != null) {
	    success = dataManager.downloadData(entity, eepi, dataStorageList, preserveFormat);
        if (!success) {
      	  String errorMsg = 
              String.format("An entity failed to download successfully: packageId: %s; entity name: %s; entity id: %s",
      			            packageId, entityName, entityId);
      	  throw new IllegalStateException(errorMsg);
        }
	    else {
			EMLFileSystemEntity efse = 
					new EMLFileSystemEntity(entityDir, emlPackageId, entityId);
			efse.setEvaluateMode(evaluateMode);
			String entityFileURL = efse.getEntityFileURL(evaluateMode);
			emlEntity.setFileUrl(entityFileURL);
				if (true) {
					// Double-check to ensure that the entity file exists on the
					// system in the expected location
					File entityFile = efse.getEntityFile();
					if ((entityFile == null) || (!entityFile.exists())) {
						String errorMsg = 
							String.format("An entity file is missing from the data repository: " +
								          "entityDir: %s; packageId: %s; entity id: %s",
										   entityDir, packageId, entityId);
						throw new IllegalStateException(errorMsg);
					}
					else {
						try (FileInputStream fis = new FileInputStream(entityFile)){
							String md5 = DigestUtils.md5Hex(fis);
							entity.setMd5HashValue(md5);
						}
						catch (IOException e) {
							logger.warn("Unable to determine MD5 from entityfile: " +
						                e.getMessage());
						}
						try (FileInputStream fis = new FileInputStream(entityFile)){
							String sha1 = DigestUtils.shaHex(fis);
							entity.setSha1HashValue(sha1);
						}
						catch (IOException e) {
							logger.warn("Unable to determine SHA-1from entityfile: " +
					                e.getMessage());
						}
					}
				}
			}
	  }
	  else {
      	  String errorMsg = "dataManager is null.";
          throw new IllegalStateException(errorMsg);
	  }
	}

	
  /*
   * The next two methods are deprecated.
   */
  
  /**
   * 
   * @param emlPackageId
   * @param entityId
   *
  public ArrayList<String> listDataEntities(String scope, String identifier){
    ArrayList<String> dummyArrayList = new ArrayList<String>();
    
    dummyArrayList.add("entity-1");
    dummyArrayList.add("entity-2");
    dummyArrayList.add("entity-3");
    
    return dummyArrayList;
  }

  
  /**
   * 
   * @param emlPackageId
   * @param entityId
   *
  public ArrayList<String> listDataEntities(String scope){
    ArrayList<String> dummyArrayList = new ArrayList<String>();
    
    dummyArrayList.add("entity-1");
    dummyArrayList.add("entity-2");
    dummyArrayList.add("entity-3");
    
    return dummyArrayList;
  }*/

  
  /**
   * Loads a data entity into the database using the Data Manager Library 
   * method DataManager.loadDataToDB().
   * 
   * @param emlPackageId     the package id object
   * @param emlEntity        the entity object
   * @return success, true when successfully loaded into the database, else false
   * @throws MalformedURLException  when the URL to the data entity is malformed
   * @throws IOException     when an IO error occurs
   * @throws Exception
   */
	public boolean loadEntity(EmlPackageId emlPackageId, EMLEntity emlEntity)
			throws MalformedURLException, IOException, Exception {
		boolean success = false;
		String fileURL = emlEntity.getFileUrl();
		String entityURL = null;

		Entity entity = emlEntity.getEntity();

		if (dataManager != null) {
			try {
				if (fileURL != null) {  // if PASTA has a copy of the entity on its file system
					entityURL = entity.getURL(); // first store the remote entity URL value
					entity.setURL(fileURL);  // set the file URL for data table upload
				}
				success = dataManager.loadDataToDB(entity, eepi);
				if (fileURL != null) {
					entity.setURL(entityURL);  // reset back to the remote entity URL value
				}
			}
			catch (MalformedURLException e) {
				logger.error("MalformedURLException while loading entity.");
				e.printStackTrace();
				throw (e);
			}
			catch (IOException e) {
				logger.error("IOException while loading entity.");
				e.printStackTrace();
				throw (e);
			}
			catch (Exception e) {
				logger.error("Exception while loading entity.");
				e.printStackTrace();
				throw (e);
			}
		}

		logger.debug("Finished loadEntity(), success = " + success);

		return success;
	}

  
  /**
   * Loads Data Manager options from a configuration file.
   */
  private void loadOptions()
          throws Exception {
    try {
      // Load database connection options
      Options options = ConfigurationListener.getOptions();
      dbDriver = options.getOption("dbDriver");
      dbURL = options.getOption("dbURL");
      dbUser = options.getOption("dbUser");
      dbPassword = options.getOption("dbPassword");
      databaseAdapterName = options.getOption("dbAdapter");
      
      // Directory paths for entities and entity reports
      metadataDir = options.getOption("datapackagemanager.metadataDir");      
      entityDir = options.getOption("datapackagemanager.entityDir");    
      evaluateReportDir = options.getOption("datapackagemanager.reportDir");     
    } 
    catch (Exception e) {
      logger.error("Error in loading options: " + e.getMessage());
      e.printStackTrace();
      throw(e);
    }    
  }

  
	/**
	 * Reads a data entity and returns it as a file.
	 * 
	 * @param resourceLocation  The location of this resource.
   * @param scope       The scope of the metadata document.
   * @param identifier  The identifier of the metadata document.
   * @param revision    The revision of the metadata document.
   * @param entityId    The entityId of the entity to be read.
   * @return   a file object containing the locally stored entity data
	 */
	public File readDataEntity(String resourceLocation,
	                           String scope, String identifier, 
	                           String revision, String entityId) {
	  File entityFile = null;
	  EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope, identifier, revision);

    EMLFileSystemEntity efse = new EMLFileSystemEntity(resourceLocation, emlPackageId, entityId);
	  entityFile = efse.getEntityFile();
	  
	  return entityFile;
	}

}
