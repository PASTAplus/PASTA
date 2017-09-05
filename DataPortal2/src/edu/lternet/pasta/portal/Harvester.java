/*
 *
 * $Date: 2012-04-02 11:10:19 -0700 (Mon, 02 Apr 2012) $
 * $Author: dcosta $
 * $Revision: $
 *
 * Copyright 2011,2012 the University of New Mexico.
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

package edu.lternet.pasta.portal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.LoginClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaIdleTimeException;
import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.EmlUtility;
import edu.lternet.pasta.common.ResourceNotFoundException;

/**
 * 
 * @author dcosta
 *
 */
public class Harvester implements Runnable {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.Harvester.class);
  private static final String PACKAGEID_PARSING_ERROR =
    "Unable to parse packageId from document. The document may not be well-formed XML.";
  
  
  /*
   * Instance variables
   */
  
  private int dummyPackageIdCounter = 1;
  private String harvesterPath = null;
  private String harvestReportId = null;
  private String harvestDirPath = null;
  private boolean evaluate;
  // List of EML documents URLs to batch process
  private ArrayList<String> documentURLs = null;
  private String uid = null;
  private boolean useChecksum = false;

  
  /*
   * Constructors
   */
  
  /**
   * Constructs a Harvester object with the specified values.
   * 
   * @param harvesterPath  the directory under which harvest results are stored
   * @param harvestReportId  the harvest report identifier
   * @param uid  the user identifier, e.g. "ucarroll"
   * @param isEvaluate  true if evaluate, false if upload
   */
  public Harvester(String harvesterPath, String harvestReportId, String uid, 
		           boolean isEvaluate, boolean useChecksum) {
    this.harvesterPath = harvesterPath;
    this.harvestReportId = harvestReportId;	
	this.harvestDirPath = String.format("%s/%s", harvesterPath, harvestReportId);
    this.uid = uid;
    this.evaluate = isEvaluate;
    this.useChecksum = useChecksum;
  }
  

  /*
   * Class methods
   */
  
  /**
   * Create a directory if it does not already exist.  This will attempt
   * to create any parent directories if necessary.
   * 
   * @param dirPath the full pathname of the directory to create
   * @returns boolean representing success or failure of directory creation
   */
  public static void createDirectory(String dirPath) throws IOException {
    File file = new File(dirPath);
    if (file.exists() && file.isDirectory()) {
      return;
    }
    if (!file.mkdirs()) {
      throw new IOException("Could not create directory: " + dirPath);
    }
  }


  /*
   * A main program that exercises much of the HarvesterServlet's functionality.\
   * No command arguments are needed.
   */
  public static void main(String[] args) {
    String testURL = "http://trachyte.lternet.edu:8080/testData/NoneSuchBugCountDuplicateEntity.xml";
    //String testMetacatHarvestURL = "http://trachyte.lternet.edu:8080/testData/trachyteHarvestList.xml";
    String uid = null;
    String password = null;
    
    ConfigurationListener.configure();
    PropertiesConfiguration options = ConfigurationListener.getOptions();
    
    if (options == null) {
      logger.error("Failed to load the DataPortal properties file: 'dataportal.properties'");
    }
    else {
      String harvesterPath = options.getString("harvester.path");
      uid = options.getString("eventservice.uid");
      if (uid == null) {
        logger.error("No value found for property: 'eventservice.uid'");
      }
      else {
        uid = "ucarroll";
      }
      boolean isEvaluate = true;
      boolean useChecksum = true;
      String harvestReportId = uid + "-evaluate";
      Harvester harvester = new Harvester(harvesterPath, harvestReportId, uid, isEvaluate, useChecksum);
      password = options.getString("eventservice.password");
      if (password == null) {
        logger.error("No value found for property: 'eventservice.password'");
      }
    
      /*
       * Authenticate the test user
       */
      try {
        LoginClient loginClient = new LoginClient(uid, password);
      } 
      catch (PastaAuthenticationException e) {
        logger.error("User '" + uid + "' failed to authenticate.");
      }

      harvester.documentURLs = new ArrayList<String>();
      harvester.documentURLs.add(testURL);
      try {
        harvester.processDocumentURLs();
      }
      catch (Exception e) {
        e.printStackTrace();
        logger.error(e.getMessage());
      }
    }
  }
  
  
  /*
   * Instance methods
   */
  
  
  /*
   * Check whether too many harvests are already executing.
   */
  private boolean checkForTooManyHarvests() {
    boolean tooMany = false;
    
    return tooMany;
  }
  
  
  /*
   * Reads the contents of the document URL into a string.
   */
  private String emlStringFromURL(String documentURL) throws IOException {
    String emlString = null;

    try {
    	URL url = new URL(documentURL);
    	// Use Java 7 try-with-resources. It closes the stream automatically.
    	try (InputStream inputStream = url.openStream()) {
    		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
    		emlString = getAsString(inputStreamReader, true);
    	}
    }
    catch (IOException e) {
    	logger.error(e.getMessage());
    	throw(e);
    }
     		
    return emlString;
  }

  
  /**
   *  Reads character data from the <code>Reader</code> provided, using a 
   *  buffered read. Returns data as a <code>StringBufer</code>
   *
   *  @param  reader              <code>Reader</code> object to be read
   *
   *  @param  closeWhenFinished   <code>boolean</code> value to indicate 
   *                              whether Reader should be closed when reading
   *                              finished
   *
   *  @return                     <code>StringBuffer</code> containing  
   *                              characters read from the <code>Reader</code>
   *
   *  @throws IOException if there are problems accessing or using the Reader.
   */
  public StringBuffer getAsStringBuffer(Reader reader, boolean closeWhenFinished) 
		  throws IOException {
    if (reader == null)
      return null;

    StringBuffer sb = new StringBuffer();
    
    try {
      char[] buff = new char[4096];
      int numCharsRead;

      while ((numCharsRead = reader.read(buff, 0, buff.length)) != -1) {
        sb.append(buff, 0, numCharsRead);
      }
    }
    catch (IOException ioe) {
      throw ioe;
    }
    finally {
      if (closeWhenFinished) {
        try {
          if (reader != null)
            reader.close();
        }
        catch (IOException ce) {
          ce.printStackTrace();
        }
      }
    }
    
    return sb;
  }

  
  /**
   *  Reads character data from the <code>Reader</code> provided, using a 
   *  buffered read. Returns data as a <code>String</code>
   *
   *  @param  reader              <code>Reader</code> object to be read
   *
   *  @param  closeWhenFinished   <code>boolean</code> value to indicate 
   *                              whether Reader should be closed when reading
   *                              finished
   *
   *  @return                     <code>String</code> containing  
   *                              characters read from the <code>Reader</code>
   *
   *  @throws IOException if there are problems accessing or using the Reader.
   */
  public String getAsString(Reader reader, boolean closeWhenFinished)
      throws IOException {
    StringBuffer sb = getAsStringBuffer(reader, closeWhenFinished);
    return sb.toString();
  }
  
  
  /**
   * Accesses the harvesterPath value.
   * 
   * @return  harvesterPath
   */
  public String getHarvesterPath() {
	  return harvesterPath;
  }

  
  /**
   * Accesses the harvestReportId value.
   * 
   * @return  harvestReportId
   */
  public String getHarvestReportId() {
	  return harvestReportId;
  }
  
  
  /**
   * Access the evaluate boolean value.
   * 
   * @return  evaluate: true if this is an evaluate operation, 
   *          false if this is an upload operation
   */
  public boolean isEvaluate() {
	  return evaluate;
  }

  
  /*
   * Harvests or evaluates a single EML document.
   */
	private void processEMLFile(String harvestDirPath, String uid, File emlFile, 
			                    boolean isEvaluate) {
		String filename = "serviceMessage.txt";
		String packageId = "";
		EmlPackageId emlPackageId = null;

		try {
			DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
			String serviceMessage = null;

			/*
			 * Parse the packageId from the EML document.
			 * 
			 * If we fail to determine the packageId value by parsing the EML
			 * document, then assign a dummy value. Increment the integer part
			 * of the dummy value to ensure uniqueness during this harvest.
			 */
			try {
				emlPackageId = EmlUtility.emlPackageIdFromEML(emlFile);
				EmlPackageIdFormat epif = new EmlPackageIdFormat();
				packageId = epif.format(emlPackageId);
			}
			catch (Exception e) {
				packageId = "Unknown-Package-ID-" + dummyPackageIdCounter++;
			}

			String packageIdPath = harvestDirPath + "/" + packageId;
			String verb = isEvaluate ? "Evaluating" : "Uploading";
			logger.info(String.format("%s data package: %s", verb, packageId));

			Harvester.createDirectory(packageIdPath);

			/* 
			 * Process upload (insert or update) operation 
			 */
			if (!isEvaluate) {
				if (emlPackageId != null) {
					String scope = emlPackageId.getScope();
					Integer identifier = emlPackageId.getIdentifier();
					boolean isUpdate = isUpdate(dpmClient, scope, identifier);

					String resourceMap = null;
					try {
						if (isUpdate) {
							resourceMap = dpmClient.updateDataPackage(scope, identifier, emlFile);
						}
						else {
							resourceMap = dpmClient.createDataPackage(emlFile);
						}

						if (resourceMap != null) {
							String resourceMapPath = packageIdPath + "/resourceMap.txt";
							File resourceMapFile = new File(resourceMapPath);
							FileUtils.writeStringToFile(resourceMapFile, resourceMap);

							/*
							 * Store a local copy of the quality report so that
							 * quality check statistics can be displayed in the
							 * harvest report.
							 */
							String revision = emlPackageId.getRevision().toString();
							String qualityReportStr = dpmClient.readDataPackageReport(scope, identifier, revision);
							String qualityReportPath = packageIdPath + "/qualityReport.xml";
							File qualityReportFile = new File(qualityReportPath);
							boolean append = false;
							FileUtils.writeStringToFile(qualityReportFile, qualityReportStr, append);
						}
					}
					catch (PastaIdleTimeException e) {
						writeServiceMessage(packageIdPath, filename, e.getMessage());
					}
					catch (Exception e) {
						String eMessage = e.getMessage();

						if (isQualityReport(eMessage)) {
							filename = "qualityReport.xml";
							serviceMessage = eMessage;
						}
						else {
							serviceMessage = String.format("Error uploading packageId '%s': %s", packageId, eMessage);
							logger.error(serviceMessage);
						}

						writeServiceMessage(packageIdPath, filename, serviceMessage);
					}
				}
				else {
					serviceMessage = PACKAGEID_PARSING_ERROR;
					logger.error(serviceMessage);
					writeServiceMessage(packageIdPath, filename, serviceMessage);
				}
			}
			/* 
			 * Process evaluate operation 
			 */
			else {
				if (emlPackageId != null) {
					String qualityReportXML = null;

					try {
						qualityReportXML = dpmClient.evaluateDataPackage(emlFile, this.useChecksum);
						String qualityReportPath = packageIdPath + "/qualityReport.xml";
						File qualityReportFile = new File(qualityReportPath);
						FileUtils.writeStringToFile(qualityReportFile, qualityReportXML);
					}
					catch (PastaIdleTimeException e) {
						writeServiceMessage(packageIdPath, filename, e.getMessage());
					}
					catch (Exception e) {
						logger.error(serviceMessage);
						writeServiceMessage(packageIdPath, filename, e.getMessage());
					}
				}
				else {
					serviceMessage = PACKAGEID_PARSING_ERROR;
					logger.error(serviceMessage);
					writeServiceMessage(packageIdPath, filename, serviceMessage);
				}
			}
		}
		catch (PastaAuthenticationException e) {
			logger.error(e.getMessage());
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
  
  
    private void writeServiceMessage(String packageIdPath, String filename, String serviceMessage) {
		String serviceMessagePath = packageIdPath + "/" + filename;

		try {
			File serviceMessageFile = new File(serviceMessagePath);
			boolean append = true;
			FileUtils.writeStringToFile(serviceMessageFile, serviceMessage, append);
		}
		catch (IOException e) {
			logger.error(String.format("Error writing service message to file %s: %s", serviceMessagePath, e.getMessage()));
		}
    }
  
  
  /*
   * Boolean to determine whether the entity body returned by the
   * DataPackageManager service is a quality report XML.
   */
  private boolean isQualityReport(String serviceMessage) {
    boolean isQualityReport = false;
    
    if (serviceMessage != null) {
      if (serviceMessage.contains("<qr:qualityReport")) {
        if (serviceMessage.trim().endsWith("</qr:qualityReport>")) {
          isQualityReport = true;
        }
      }
    }
    
    return isQualityReport;
  }

  
  /*
   * Boolean to determine whether a data package should be
   * updated versus created.
   * 
   * @return  true if the data package should be updated, false if
   *          it should be created.
   */
  private boolean isUpdate(DataPackageManagerClient dpmClient, 
                           String scope, Integer identifier) 
          throws Exception {
    boolean isUpdate = false;
    
    try {
      String revisionsStr = dpmClient.listDataPackageRevisions(scope, identifier, null);
      if (revisionsStr != null) {
        String[] revisionsArray = revisionsStr.split("\n");
        for (int i = 0; i < revisionsArray.length; i++) {
          String revision = revisionsArray[i];
          if (revision != null && !revision.equals("")) {
            isUpdate = true;
            break;
          }
        }
     }
    }
    catch (ResourceNotFoundException e) {
      // A 404 status means that no revisions were found.
      // No action needed.
    }
    
    return isUpdate;
  }
  
  
  /**
   * Runs the processDocumentURLs() code in a separate thread for batch
   * processing.  
   */
  public void run() {
    try {
      processDocumentURLs();
      logger.debug("Launched batch processing thread to process " + 
                  documentURLs.size() + " EML documents.");
    }
    catch (Exception e) {
      logger.error("Exception while batch processing document URLs: " + e.getMessage());
    }
  }

  
 /*
  * Inserts or evaluates a list of EML documents. The document
  * URLs must first be stored in the this.documentURLs
  * instance variable.
  */
  private void processDocumentURLs() throws Exception {
    boolean tooManyHarvests = checkForTooManyHarvests();

    if (!tooManyHarvests) {
      // Directory for storing harvester files for this harvest
      Harvester.createDirectory(harvestDirPath);
      
      // Sub-directory for temporary EML files
      String harvestEMLPath = harvestDirPath + "/eml";
      Harvester.createDirectory(harvestEMLPath);
      
      for (String documentURL : documentURLs) {
        if (documentURL != null && !documentURL.equals("")) {
          String urlErrorMessage = null;
          String emlString = null;
          
          try {
            emlString = emlStringFromURL(documentURL);
          }
          catch (IOException e) {
            urlErrorMessage = "IOException : " + e.getMessage();
            logger.error(urlErrorMessage); 
          }
          
          /*
           * Save the EML to file then process it for evaluation or upload
           */
          if (emlString != null) {
            File emlFile = saveEmlToFile(harvestEMLPath, emlString, evaluate);
            processEMLFile(harvestDirPath, uid, emlFile, evaluate);
            // Sleep to allow DAS database connection recovery
            Thread.sleep(30000);
          }
          else if (urlErrorMessage != null) {
            writeUrlIoMessage(harvestDirPath, documentURL, urlErrorMessage);
          }
        }    
      }
    }
  }
  
  
  /**
   * Inserts or evaluates a single EML document, passed in as an XML string.
   * 
   * @param emlString  the EML XML document string
   */
  public void processSingleDocument(String emlString) 
          throws Exception {
    // Directory for storing harvester files for this harvest
    Harvester.createDirectory(harvestDirPath);

    // Sub-directory for temporary EML files
    String harvestEMLPath = harvestDirPath + "/eml";
    Harvester.createDirectory(harvestEMLPath);

    /*
     * Save the EML to file then process it for evaluation or upload
     */
    if (emlString != null) {
      File emlFile = saveEmlToFile(harvestEMLPath, emlString, evaluate);
      processEMLFile(harvestDirPath, uid, emlFile, evaluate);
    }
  }
   
   
  /**
   * Inserts or evaluates a single EML document, passed in as an XML file.
   * 
   * @param emlFile  the EML XML document file
   */
  public void processSingleDocument(File emlFile) 
          throws Exception {
    // Directory for storing harvester files for this harvest
    Harvester.createDirectory(harvestDirPath);

    // Sub-directory for temporary EML files
    String harvestEMLPath = harvestDirPath + "/eml";
    Harvester.createDirectory(harvestEMLPath);

    /*
     * Process the EML file for evaluation or upload
     */
    if (emlFile != null) {
      processEMLFile(harvestDirPath, uid, emlFile, evaluate);
    }
  }
   
   
  /*
   * Write the text of a URL IO error message to file for subsequent use in
   * harvest reports.
   */
  private void writeUrlIoMessage(String path, String url, String message) {
    boolean append = true;
    String urlMessagesPath = path + "/urlMessages.txt";
    
    File urlMessagesFile = new File(urlMessagesPath);
    
    try {
      if (urlMessagesFile != null) {
        String messageText = "URL: " + url + " ; " + message + "\n";
        FileUtils.writeStringToFile(urlMessagesFile, messageText, append);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      logger.error(e.getMessage());
    }
  }
  
  
  /*
   * Saves an EML document to the file system for subsequent
   * processing.
   */
  private File saveEmlToFile(String harvestEMLPath, String xml, boolean isEvaluate) {
    File tempFile = null;
    Date now = new Date();
    Long mili = now.getTime();
    String tempFileName = mili.toString() + ".xml";
    logger.debug("NOW: " + mili.toString());
    StringBuffer xmlBuffer = new StringBuffer(xml);
    
    String tempFilePath = harvestEMLPath + "/" + tempFileName;
    tempFile = new File(tempFilePath);
    
    try {
      FileWriter fileWriter = new FileWriter(tempFile);
      writeToWriter(xmlBuffer, fileWriter, true);
    }
    catch (IOException e) {
      logger.error("IOException:\n" + e.getMessage());
      e.printStackTrace();
    }
    
    return tempFile;
  }
  
  
  /**
   * Sets the documentURLs instance variable to the specified urlList.
   * 
   * @param urlList    A list of documentURL string. Each URL should reference
   *                   an EML document to be processed.
   */
  public void setDocumentURLs(ArrayList<String> urlList) {
    this.documentURLs = urlList;
  }
  
  
  /**
   *  Reads character data from the <code>StringBuffer</code> provided, and 
   *  writes it to the <code>Writer</code> provided, using a buffered write. 
   *
   *  @param  buffer              <code>StringBuffer</code> whose contents are 
   *                              to be written to the <code>Writer</code>
   *
   *  @param  writer              <code>java.io.Writer</code> where contents 
   *                              of StringBuffer are to be written
   *
   *  @param  closeWhenFinished   <code>boolean</code> value to indicate 
   *                              whether Reader should be closed when reading
   *                              finished
   *
   *  @return                     <code>StringBuffer</code> containing  
   *                              characters read from the <code>Reader</code>
   *
   *  @throws IOException if there are problems accessing or using the Writer.
   */
  public void writeToWriter(StringBuffer buffer, 
                            Writer writer,
                            boolean closeWhenFinished) 
          throws IOException {
    if (writer == null) {
      throw new IOException("writeToWriter(): Writer is null");
    }

    char[] bufferChars = new char[buffer.length()];
    buffer.getChars(0, buffer.length(), bufferChars, 0);

    try {
      writer.write(bufferChars);
      writer.flush();
    }
    catch (IOException ioe) {
      throw ioe;
    }
    finally {
      if (closeWhenFinished) {
        try {
          if (writer != null)
            writer.close();
        }
        catch (IOException ce) {
          ce.printStackTrace();
        }
      }
    }
  }

}
