/**
 *    '$RCSfile: DownloadHandler.java,v $'
 *
 *     '$Author: leinfelder $'
 *       '$Date: 2008-08-08 21:40:51 $'
 *   '$Revision: 1.28 $'
 *
 *  For Details: http://kepler.ecoinformatics.org
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package edu.lternet.pasta.dml.download;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import edu.lternet.pasta.dml.database.DatabaseLoader;
import edu.lternet.pasta.dml.parser.Entity;
import edu.lternet.pasta.dml.quality.QualityCheck;
import edu.lternet.pasta.dml.quality.QualityReport;
import edu.lternet.pasta.dml.quality.QualityCheck.Status;
import org.ecoinformatics.ecogrid.authenticatedqueryservice.AuthenticatedQueryServiceGetToStreamClient;
import org.ecoinformatics.ecogrid.queryservice.QueryServiceGetToStreamClient;


/**
 * This class will read a input stream from remote entity for given URL and
 * write data into given local storage systems. This is the main class of download
 * component. The class implements Runnable interface, so the download process
 * will be run in another thread.
 * 
 * @author tao
 */
public class DownloadHandler implements Runnable 
{
  /*
   * Class fields
   */
  
  private static String anonymousFtpPasswd = "anonymous@domain.org";
  public static Log log = LogFactory.getLog(DownloadHandler.class);
  // Used in quality reporting
  protected final static String ONLINE_URLS_EXCEPTION_MESSAGE = "Error reading from the data source.";

  
  /*
   * Constants
   */   
    private static final String ANONYMOUS       = "anonymous";
    private static final String SRBUSERNAME     = "testuser.sdsc";
    private static final String SRBPASSWD       = "TESTUSER";
    private static final int    SLEEPTIME       = 100;
    private static final int    MAXLOOPNUMBER   = 2000000;
    
    protected static Hashtable<String, DownloadHandler> handlerList = 
      new Hashtable<String, DownloadHandler>();
    private static String SRBENDPOINT     = "http://srbbrick8.sdsc.edu:8080/SRBImpl/services/SRBQueryService";
    private static String SRBMACHINE      = "srb-mcat.sdsc.edu";
 
    
	/*
	 * Instance fields
	 */
  
    //protected DownloadHandler handler = null;
	//private String identifier = null;
	private String url        = null;
	private DataStorageInterface[] dataStorageClassList = null;
	private String[] errorMessages = null;
	protected boolean completed = false;
	protected boolean success = false;
	protected boolean busy = false;
	private Exception exception = null;
	
	protected String ecogridEndPoint = "http://ecogrid.ecoinformatics.org/knb/services/QueryService";
	protected Entity entity = null;
	protected String sessionId      = null;
	
    
    /*
     * Constructors
     */
	
    /**
     * This version of the constructor stores the entity object for which the download is 
     * being performed. This is to support quality reporting, where information about the
     * associated entity is needed as part of the quality information being reported on.
     * 
     * @param entity  the Entity object for which this DownloadHandler is downloading data
     * @param url     the url (or identifier) of entity need be downloaded
     */
    protected DownloadHandler(Entity entity, String url, EcogridEndPointInterface endPoint)
    {
      this(url, endPoint);
      this.entity = entity;
    }
  
  
    /**
     * Constructor of this class
     * @param url  the url (or identifier) of entity need be downloaded
     */
    protected DownloadHandler(String url, EcogridEndPointInterface endPoint)
    {
        this.url = url;
        if (endPoint != null)
        {
            ecogridEndPoint = endPoint.getMetacatEcogridEndPoint();
            SRBENDPOINT = endPoint.getSRBEcogridEndPoint();
            SRBMACHINE = endPoint.getSRBMachineName();
            
            //do we have authenticated version?
            if (endPoint instanceof AuthenticatedEcogridEndPointInterface) {
            	sessionId = ((AuthenticatedEcogridEndPointInterface)endPoint).getSessionId();
            	//can we actually use it?
            	if (sessionId != null) {
            		ecogridEndPoint = ((AuthenticatedEcogridEndPointInterface)endPoint).getMetacatAuthenticatedEcogridEndPoint();
            	}
            }
        }     
        //loadOptions();
        //this.identifier = identifier;
        //this.dataStorageClassList = dataStorageClassList;
    }
    
    
    /*
     * Class methods
     */  
	
    /**
     * Gets a downloadHandler with specified url from the hash.
     * Return null if no handler found for this source.
     * 
     * @param source  the source URL to which the returned download handler is
     *                associated. The source URL is the key, the download
     *                handler object is the associated value.
     * @return  the DownloadHandler value associated with the source, or null
     *          if DownloadHandler object is associated with this source.
     */
    protected static synchronized DownloadHandler getHandlerFromHash(
                                                                  String source)
    {
        DownloadHandler handler = null;
        
        if (source != null)
        {
          handler = handlerList.get(source);
          // assign download handler to one in List      
        }
        
        return handler;
    }
    
    
	/**
	 * Gets an instance of the DownloadHandler Object for this URL.
     * 
	 * @param url The url (or identifier) of entity to be downloaded
	 * @param endPoint the object which provides ecogrid endpoint information
	 * @return  DownloadHandler object associated with this URL
	 * 
	 */
	public static DownloadHandler getInstance(String url, 
                                              EcogridEndPointInterface endPoint)
	{
		DownloadHandler handler = getHandlerFromHash(url);
        
		if (handler == null)
		{
      log.debug("Constructing DownloadHandler for URL: " + url);
			handler = new DownloadHandler(url, endPoint);
		}
        
		return handler;
	}
	
    
  /**
   * Gets an instance of the DownloadHandler Object for this URL. This version
   * of the method passes the associated entity object as a parameter, and
   * also calls the constructor that accepts the entity object. This is
   * to support quality reporting. When creating a qualityCheck object relating
   * to downloading, the DownloadHandler will need some information from the
   * entity such as the packageId and entity name.
   * 
   * @param entity   The entity object for which the download is being performed
   * @param url The url (or identifier) of entity to be downloaded
   * @param endPoint the object which provides ecogrid endpoint information
   * @return  DownloadHandler object associated with this URL
   * 
   */
  public static DownloadHandler getInstance(Entity entity, String url, 
                                              EcogridEndPointInterface endPoint)
  {
    DownloadHandler handler = getHandlerFromHash(url);
        
    if (handler == null)
    {
      log.debug("Constructing DownloadHandler for URL: " + url);
      handler = new DownloadHandler(entity, url, endPoint);
    }
        
    return handler;
  }
  
    
    /**
     * Sets the DownloadHandler object into the hash table. This will be called
     * at the start of download process. So we can keep track which handler is 
     * doing the download job now. Since it will access a static variable 
     * handlerList in different thread, it should be static and synchronized
     * 
     * @param  downloadHandler  the DownloadHandler object to be stored in the
     *                          hash
     *
     */
    private static synchronized void putDownloadHandlerIntoHash(
                                                DownloadHandler downloadHandler)
    {
        if (downloadHandler != null)
        {
          String source = downloadHandler.getUrl();
          if (source != null)
          {
            //System.out.println("add the source "+source);
            handlerList.put(source, downloadHandler);
          }
        }
    }
    
    
    /**
     * Removes the downloadHandler obj from the hash table. This method will be
     * called at the end of download process. Since it will access a static 
     * variable handlerList in different thread, it should be static and 
     * synchronized;
     * 
     * @param  downloadHandler  the DownloadHandler object to be removed
     *                          from the hash
     */
    private static synchronized void removeDownloadHandlerFromHash(
                                                DownloadHandler downloadHandler)
    {
        if (downloadHandler != null)
        {
          String source = downloadHandler.getUrl();
          
          if (source != null)
          {
            //System.out.println("remove the source "+source);
            handlerList.remove(source);
          }
        }
    }
    
    
    /**
     * Setter method for the anonymousFtpPasswd class variable.
     * 
     * @param passwd  The anonymous FTP password setting. FTP servers
     *                usually just check that the value parses as a
     *                syntactically valid email address such as
     *                'anonymous@domain.org' (the default setting).
     */
    public static void setAnonymousFtpPasswd(String passwd) {
    	anonymousFtpPasswd = passwd;
    }
    
    
    /*
     * Instance methods
     */
		
	/**
	 * This method will download data for the given url in a new thread.
     * It implements from Runnable Interface.
	 */
    public void run()
    {
    	DownloadHandler handler = getHandlerFromHash(url);
        
    	if (handler != null)
    	{
    		/*
             * A handler which points to the same URL is busy in downloading 
             * process, so do nothing, just wait for the handler to finish the 
             * download.
             */
    	    int index = 0;
            
    		while (handler.isBusy() && index < MAXLOOPNUMBER)
    	    {
    	    	try
    	    	{
    	    		Thread.sleep(SLEEPTIME);
    	    	}
    	    	catch(Exception e)
    	    	{
    	    		break;
    	    	}
    	    	index++;
    	    }
    	    
    	    success = handler.isSuccess();
    	    busy =false;
    	    completed = true;
    		return;
    	}
    	else
    	{
    		// if no handler which points same url, put the handler into hash table for tracking
    		putDownloadHandlerIntoHash(this);
    	}
        
    	busy = true;
    	completed = false;
        
    	try
    	{
    	  success = getContentFromSource(url);
    	}
    	catch(Exception e)
    	{
    	   log.error("Error in DownloadHandler run method " + e.getMessage());
    	}
        
    	//System.out.println("after get source"+url);
    	// waiting DataStorageInterface to finished serialize( some DataStorageInterface will
    	// span another thread
    	waitingStorageInterfaceSerialize();  	
        
    	if (dataStorageClassList != null)
    	{
    		int length = dataStorageClassList.length;
    		for (int i=0; i<length; i++)
    		{
    			DataStorageInterface storage = dataStorageClassList[i];
    			if (storage != null)
    			{
    			   success = success && storage.isSuccess(url);
    			   if (storage instanceof DatabaseLoader)
    			   {
    				   exception = storage.getException();
    			   }
    			}
    		}
    		
    	}
        
    	// downloading is done, remove the handler from hash.
    	removeDownloadHandlerFromHash(this);
    	//this.notifyAll();
    	busy = false; 
    	completed = true;
    }
    
    
    /*
     * Waits until DataStorageClass finishes serializing.
     * Sometimes the outputstream which DownloadHandler gets from
     * DataStorageClass is not in same thread as the main one there.
     * A good example of this is the DatabaseLoader class.
     */
    private void waitingStorageInterfaceSerialize()
    {
    	if (dataStorageClassList != null)
    	{
    		int length = dataStorageClassList.length;
    		boolean completedInDataStorageClassList = true;
            
    		for (int i=0; i<length; i++)
    		{
    			DataStorageInterface storage = dataStorageClassList[i];
    			if (storage != null)
    			{
    			   if (storage.doesDataExist(url))
    			   {
    				   
    			   }
    			   else
    			   {
    			      completedInDataStorageClassList = 
                                             completedInDataStorageClassList && 
                                             storage.isCompleted(url);
    			   }
    			}
    		}
    		
    		while (!completedInDataStorageClassList)
    		{
    			completedInDataStorageClassList = true;
                
        		for (int i=0; i<length; i++)
        		{
        			DataStorageInterface storage = dataStorageClassList[i];
                    
        			if (storage != null)
        			{
        			   if (storage.doesDataExist(url))
          			   {
          				   
          			   }
          			   else
          			   {
          			      completedInDataStorageClassList = 
                                             completedInDataStorageClassList && 
                                             storage.isCompleted(url);
          			   }
        			}
        		}
    		}
    	}
    }
    
    
    /**
     * Downloads data into the given list of DataStorageInterface objects. 
     * This method will create, start and wait for another thread to download 
     * data.
     * 
     * @param  dataStorages  The list of destinations for the downloaded data
     * @return true if successful, else false
     */
    public boolean download(DataStorageInterface[] dataStorages) 
            throws DataSourceNotFoundException, Exception
    {
    	this.setDataStorageClassList(dataStorages);
    	Thread loadData = new Thread(this);
    	loadData.start();
    	int index = 0;
        
    	while (!this.isCompleted() && index < MAXLOOPNUMBER)
    	{
    		if (exception != null)
            {
            	throw exception;
            }
    		Thread.sleep(SLEEPTIME);
    		index++;
    	}
        
        success = this.isSuccess();
        
        if (exception != null)
        {
        	throw exception;
        }
        
    	return success;
    }
    
    
    /**
     * Returns the thread status - busy or not
     *  
     * @return boolean variable busy - if this thread is busy on downloading
     */
    public boolean isBusy()
    {
    	return busy;
    }
    
    
    /**
     * Returns the status of downloading, completed or not
     * 
     * @return value of completetion
     */
    public boolean isCompleted()
    {
    	return completed;
    }
    
    
    /**
     * Returns the status of downloading, success or not
     * 
     * @return The value of success
     */
    public boolean isSuccess()
    {
    	return success;
    }
    
    
    /**
     * Retruns the objects of DataStorageInterface associated with this class.
     * 
     * @return The array of DataStorageInterface object
     */
    public DataStorageInterface[] getDataStorageClassList() 
    {
    	return dataStorageClassList;
    }
    
    
    /**
     * Sets the objects of DataStorageInterface associated with this class.
     * 
     * @param dataStorageClassList  The array of DataStorageInterface which 
     *                              will be associcated to this class
     */
    public void setDataStorageClassList(
                                    DataStorageInterface[] dataStorageClassList)
    {
    	this.dataStorageClassList = dataStorageClassList;
    }
    
    
    /**
     * Gets content from given source and writes it to DataStorageInterface 
     * to store them. This method will be called by run()
     * 
     * @param resourceName  the URL to the source data to be retrieved
     */
    protected boolean getContentFromSource(String resourceName)
    {
    	boolean successFlag = false;
    	QualityCheck onlineURLsQualityCheck = null;
    	boolean onlineURLsException = false;  // used to determine status of onlineURLs quality check
        
      if (resourceName != null) { resourceName = resourceName.trim(); }
      
        if (resourceName != null && 
            (resourceName.startsWith("http://") ||
             resourceName.startsWith("https://") ||
             resourceName.startsWith("file://") ||
             resourceName.startsWith("ftp://") 
            )
           ) {
             // get the data from a URL
            int responseCode = 0;
            String responseMessage = null;
            
             try {
                 URL url = new URL(resourceName);
                 boolean isFTP = false;

                 if (entity != null) {
                   String contentType = null;
                   
                   // Find the right MIME type and set it as content type
                   if (resourceName.startsWith("http")) {
                	 HttpURLConnection httpURLConnection = (HttpURLConnection)  url.openConnection();
                     httpURLConnection.setRequestMethod("HEAD");
                     httpURLConnection.connect();
                     contentType = httpURLConnection.getContentType();
                     responseCode = httpURLConnection.getResponseCode();
                     responseMessage = httpURLConnection.getResponseMessage();
                   }
                   else if (resourceName.startsWith("file")) {
                     URLConnection urlConnection= url.openConnection();
                     urlConnection.connect();
                     contentType = urlConnection.getContentType();
                   }
                   else { // FTP
                	 isFTP = true;
                     contentType = "application/octet-stream";
                   }
                 
                   entity.setUrlContentType(contentType);
                 }
                 
                 if (!isFTP) { // HTTP(S) or FILE
                   InputStream filestream = url.openStream();

                   try {
                     successFlag = 
                        this.writeRemoteInputStreamIntoDataStorage(filestream);
                   }
                   catch (IOException e) {
                     exception = e;
                     String errorMessage = e.getMessage();
                     if (errorMessage.startsWith(ONLINE_URLS_EXCEPTION_MESSAGE)) {
                       onlineURLsException = true;
                     }
                   }
                   finally {
                	 filestream.close();
                   }
                 } else { // FTP
                   String[] urlParts = resourceName.split("/");
                   String address = urlParts[2];
                   String dir = "/";
                   for (int i = 3; i < urlParts.length - 1; i++) {
                     dir += urlParts[i] + "/";
                   }
                   String fileName = urlParts[urlParts.length - 1];
                   FTPClient ftpClient = new FTPClient();
                   ftpClient.connect(address);
                   ftpClient.login(ANONYMOUS, anonymousFtpPasswd);
                   ftpClient.changeWorkingDirectory(dir);
                   ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                   ftpClient.enterLocalPassiveMode();  // necessary to avoid firewall blocking
                   InputStream filestream = ftpClient.retrieveFileStream(fileName);
                   try {
                     successFlag =
                         this.writeRemoteInputStreamIntoDataStorage(filestream);
                   }
                   catch (IOException e) {
                     exception = e;
                     String errorMessage = e.getMessage();
                     if (errorMessage.startsWith(ONLINE_URLS_EXCEPTION_MESSAGE)) {
                       onlineURLsException = true;
                     }
                   }
                   finally {
                     try {
                       filestream.close();
                     } catch (IOException e) {
                       exception = new DataSourceNotFoundException(
                         String.format("Error closing local file '%s': %s",
                         resourceName, e.getMessage()));
                       onlineURLsException = true;
                     }
                   }

                   // logout and disconnect if FTP session
                   if (resourceName.startsWith("ftp") && ftpClient != null) {
                     try {
                       ftpClient.enterLocalActiveMode();
                       ftpClient.logout();
                       ftpClient.disconnect();
                     } catch (IOException e) {
                       exception = new DataSourceNotFoundException(
                         String.format("Error disconnecting from FTP with resource '%s': %s",
                                       resourceName, e.getMessage()));
                       onlineURLsException = true;
                     }
                   }
                 }
             }
             catch (MalformedURLException e) {
            	 String eClassName = e.getClass().getName();
            	 String eMessage = String.format("%s: %s", eClassName, e.getMessage());
                 onlineURLsException = true;
            	 exception = new DataSourceNotFoundException(String.format(
            			 "The URL '%s' is a malformed URL: %s", resourceName, eMessage));
             }
             catch (IOException e) {
            	 String eClassName = e.getClass().getName();
            	 String eMessage = String.format("%s: %s", eClassName, e.getMessage());
            	 if (responseCode > 0) {
            		 eMessage = String.format("Response Code: %d %s; %s", responseCode, responseMessage, eMessage);
            	 }
                 onlineURLsException = true;
            	 exception = new DataSourceNotFoundException(String.format(
            			 "The URL '%s' is not reachable: %s", resourceName, eMessage));
             }

             // Initialize the "Online URLs are live" quality check
             String qualityCheckIdentifier = "onlineURLs";
             QualityCheck qualityCheckTemplate = 
               QualityReport.getQualityCheckTemplate(qualityCheckIdentifier);
             onlineURLsQualityCheck = 
               new QualityCheck(qualityCheckIdentifier, qualityCheckTemplate);
               
             if (QualityCheck.shouldRunQualityCheck(entity, onlineURLsQualityCheck)) {
               String resourceNameEscaped = QualityCheck.embedInCDATA(resourceName);
               
               if (!onlineURLsException) {
                 onlineURLsQualityCheck.setStatus(Status.valid);
                 onlineURLsQualityCheck.setFound("true");
                 onlineURLsQualityCheck.setExplanation(
                   "Succeeded in accessing URL: " + resourceNameEscaped);
               }
               else {
                 onlineURLsQualityCheck.setFailedStatus();
                 onlineURLsQualityCheck.setFound("false");
                 String explanation = "Failed to access URL: " + resourceNameEscaped;
                 explanation = explanation + "; " + 
                               QualityCheck.embedInCDATA(exception.getMessage());
                 onlineURLsQualityCheck.setExplanation(explanation);
               }
               
               entity.addQualityCheck(onlineURLsQualityCheck);
             }

             return successFlag;
         }
         else if (resourceName != null && 
                  resourceName.startsWith("ecogrid://")) {
             // get the docid from url
             int start = resourceName.indexOf("/", 11) + 1;
             //log.debug("start: " + start);
             int end = resourceName.indexOf("/", start);
             
             if (end == -1) {
                 end = resourceName.length();
             }
             
             //log.debug("end: " + end);
             String ecogridIdentifier = resourceName.substring(start, end);
             // pass this docid and get data item
             //System.out.println("the endpoint is "+ECOGRIDENDPOINT);
             //System.out.println("The identifier is "+ecogridIdentifier);
             //return false;
             return getContentFromEcoGridSource(ecogridEndPoint, 
                                                ecogridIdentifier);
         }
         else if (resourceName != null && resourceName.startsWith("srb://")) {
             // get srb docid from the url
             String srbIdentifier = transformSRBurlToDocid(resourceName);
             // reset endpoint for srb (This is hack we need to figure ou
             // elegent way to do this
             //mEndPoint = Config.getValue("//ecogridService/srb/endPoint");
             // pass this docid and get data item
             //log.debug("before get srb data");
             return getContentFromEcoGridSource(SRBENDPOINT, srbIdentifier);
         }
         else {
        	 successFlag = false;
             return successFlag;
         }
    }
    
    
    /**
     *  Get data from ecogrid server base on given end point and identifier.
     *  This method will handle the distribution url for ecogrid or srb protocol
     *  This method will be called by getContentFromSource().
     *  
     *  @param  endPoint    the end point of ecogrid service
     *  @param  identifier  the entity identifier in ecogrid service
     */
    protected boolean getContentFromEcoGridSource(String endPoint, 
                                                  String identifier)
    {        
        // create a ecogrid client object and get the full record from the
        // client
    	//System.out.println("=================the endpoint is "+endPoint);
    	//System.out.println("=================the identifier is "+identifier);
    	boolean successFlag = false;
        
    	if (endPoint != null && identifier != null)
    	{
	        //log.debug("Get " + identifier + " from " + endPoint);
	        
	        try
	        {
	            //fatory
	            //log.debug("This is instance pattern");
	            	            
	            //log.debug("Get from EcoGrid: " + identifier);
	            NeededOutputStream [] outputStreamList = getOutputStreamList();
                
	            if (outputStreamList != null)
	            {
	            	boolean oneLoopSuccess = true;
                    
	            	for (int i=0; i<outputStreamList.length; i++)
	            	{	            		
	            		NeededOutputStream stream = outputStreamList[i];
                        
	            		if (stream != null && stream.getNeeded())
	            		{
		                    BufferedOutputStream bos = 
                             new BufferedOutputStream(stream.getOutputStream());
		                    
		                    //use the appropriate client
		                    URL endPointURL = new URL(endPoint);
		                    if (sessionId != null) {
		                    	AuthenticatedQueryServiceGetToStreamClient authenticatedEcogridClient = 
		        	            	new AuthenticatedQueryServiceGetToStreamClient(endPointURL);
		                    	authenticatedEcogridClient.get(identifier, sessionId, bos);
		                    }
		                    else {
		                    	QueryServiceGetToStreamClient ecogridClient = 
                                    new QueryServiceGetToStreamClient(endPointURL);
		                    	ecogridClient.get(identifier, bos);
		                    }
		                    bos.flush();
		                    bos.close();
                            
		                    if (oneLoopSuccess)
		                    {
		                    	successFlag = true;
		                    }
	            		}
	            		else if (stream != null)
	            		{
	            			if (oneLoopSuccess)
		                    {
		                    	successFlag = true;
		                    }
	            		}
	            		else
	            		{
	            			oneLoopSuccess = false;
	            			successFlag = false;
	            		}
	            	}
	            }
	            else
	            {
	            	successFlag = false;
	            }
	            
                return successFlag;
	            
	        }
	        catch(Exception ee)
	        {
	            log.error("DownloadHandler - error getting content from Ecogrid ", ee);
	            ee.printStackTrace();
	            successFlag = false;
	            return successFlag;
	        }
    	}
    	else
    	{
    		//System.out.println("in else path of get data from other source");
    		// this is not ecogrid source, we need download by other protocol
    		//return getContentFromSource(identifier);
    		return false;    		
    	}
        
    }
    
    
    /*
     * This method will transfer a srb url to srb docid in ecogrid.
     * srb id should look like: 
     *   srb://seek:/home/beam.seek/IPCC_climate/Present/ccld6190.dat
     * and correspond docid looks like:
     *   srb://testuser:TESTUSER@orion.sdsc.edu/home/beam.seek/IPCC_climate/Present/ccld6190.dat
     */
    private String transformSRBurlToDocid(String srbURL)
    {
        String docid = null;
        
        if (srbURL == null)
        {
            return docid;
        }
        
        String regex = "seek:";
        srbURL = srbURL.trim();
        //log.debug("The srb url is "+srbURL);
        // get user name , passwd and machine namefrom configure file
        String user = SRBUSERNAME;//Config.getValue("//ecogridService/srb/user");
        String passwd = SRBPASSWD;//Config.getValue("//ecogridService/srb/passwd");
        String machineName = SRBMACHINE;//Config.getValue("//ecogridService/srb/machineName");
        String replacement = user+":"+passwd+"@"+machineName;
        docid = srbURL.replaceFirst(regex, replacement);
        //log.debug("The srb id is " + docid);
        
        return docid;
    }
    
    
    /*
     * Method to get an array of outputstream for datastorage class.
     * If dataStorageClassList is null, null will return.
     * If some dataStorage object couldn't get an outputstream or
     * identifier is already in the datastorage object, null will associate
     * with this object.
     * 
     */
    private NeededOutputStream[] getOutputStreamList()
    {
    	NeededOutputStream[] list = null;
        
    	if (dataStorageClassList != null)
    	{ 
    		 list = new NeededOutputStream[dataStorageClassList.length];
             
	  		 for (int i = 0; i<dataStorageClassList.length; i++)
	  		 {
	  			 DataStorageInterface dataStorge = dataStorageClassList[i];
                 
	  			 if (dataStorge != null && !dataStorge.doesDataExist(url))
	  			 {
	  		     log.debug("DownloadHandler.startSerialize()");
	  				 OutputStream osw = dataStorge.startSerialize(url);
	  				 NeededOutputStream stream = new NeededOutputStream(osw, 
                                                                        true);
	                 list[i] = stream;
	  			 }
	  			 else if(dataStorge != null)
	  			 {
	  				 OutputStream osw = null;
	  				 NeededOutputStream stream = new NeededOutputStream(osw, 
                                                                        false);
	                 list[i] = stream;
	  			 }
	  			 else
	  			 {
	  				 list[i] = null;
	  			 }
	  		 }
  	  }
     
      return list;
    }
    
    
    /*
     * Method to close a OutputStream array. Calls the finishSerialize() method
     * for each of the DataStorageInterface objects in the
     * dataStorageClassList.
     */
    private void finishSerialize(String error) throws IOException
    {
    	if (dataStorageClassList != null)
    	{
    		int length = dataStorageClassList.length;
    		//boolean completedInDataStorageClassList = true;
            
    		for (int i=0; i<length; i++)
    		{
    			DataStorageInterface storage = dataStorageClassList[i];
                
    			//if (storage != null && storage.isCompleted(url))
    			if (storage != null)
    			{
    			  storage.finishSerialize(url, error);
    			}
    		}
    	}
    	log.debug("DownloadHandler.finishSerialize()");
    }
    
    
    /**
     * Gets Error messages for downloading process.
     * 
     * @return The array of errory messages
     */
    public String[] getErrorMessages()
    {
    	return errorMessages;
    }
    
    
    /*
     * This class add a new flag in OutputStream class - need to write the
     * remote input stream to this output stream or not.
     * If data storage insterface already download this identifier,
     * this OuptStream will be marked as NonNeeded. So download would NOT
     * happen again
     * 
     * @author tao
     *
     */
    private class NeededOutputStream
    {
        /*
         * Instance fields
         */
      
    	private OutputStream stream = null;
    	private boolean needed      = true;
    	
        
    	/**
    	 * Constructor
    	 * @param stream  OutputStream in this object
    	 * @param needed  Need of writting inputstream to this output stream
    	 */
    	public NeededOutputStream(OutputStream stream, boolean needed)
    	{
    		this.stream = stream;
    		this.needed = needed;
    	}
        
        /*
         * Instance methods
         */
    	
    	/**
    	 * Gets the OuptStream associated with this object
    	 * @return The OutputStream  object
    	 */
    	public OutputStream getOutputStream()
    	{
    		return stream;
    	}
    	
    	/**
    	 * Gets the value if need write the inpustream to this output stream
    	 * @return The boolean value of need to write
    	 */
    	public boolean getNeeded()
    	{
    		return needed;
    	}
    }
    
    
    /**
     * Gets the URL that this DownloadHandler object is associated with. 
     */
    public String getUrl()
    {
    	return url;
    }
    
    
    /*
     * This method will read from remote inputsream and write it
     * to StorageSystem. It only handles http or ftp protocals. It does not
     * handle ecogrid protocol.
     */
	protected boolean writeRemoteInputStreamIntoDataStorage(InputStream inputStream)  
	        throws IOException {
		boolean successFlag = false;

		try {
			NeededOutputStream[] outputStreamList = getOutputStreamList();

			if (outputStreamList != null) {
				// start reading, to write to the ouputstreams
				byte[] b = new byte[1024];
				int bytesRead = inputStream.read(b, 0, 1024);
				
				if (bytesRead < 1) {
				  throw new IOException(
				      String.format("%s %s", ONLINE_URLS_EXCEPTION_MESSAGE, "0 bytes were read."));
				}
				
				int kilobytes = 1;
        //System.err.printf("  Kilobytes read: .\n");
				
				/*
				 * Store the first kilobyte of data in the entity for
				 * subsequent use in quality reporting
				 */
				if (entity != null && entity.getFirstKilobyte() == null) {
				  String firstKilobyte = new String(b);
				  entity.setFirstKilobyte(firstKilobyte);
				}
				
				NeededOutputStream stream = null;
				OutputStream os = null;
				while (bytesRead > -1) {
					// write to each outputstreams
					for (int i = 0; i < outputStreamList.length; i++) {
						stream = outputStreamList[i];
						if (stream != null && stream.getNeeded()) {
							os = stream.getOutputStream();
              os.write(b, 0, bytesRead);
						}
					}
					// get the next bytes
					bytesRead = inputStream.read(b, 0, 1024);
					kilobytes++;
					/*if (kilobytes < 1000) {
            System.err.printf(".");
            if (kilobytes % 100 == 0) {
              System.err.printf("\n");
            }
					}					
					else if (kilobytes % 1000 == 0) {
	          System.err.printf("\n  Kilobytes read: %d", kilobytes);
					}*/
				}
        //System.err.printf("\n");

				// done writing to the streams
				for (int i = 0; i < outputStreamList.length; i++) {
					stream = outputStreamList[i];
					if (stream != null && stream.getNeeded()) {
						os.flush();
						os.close();
					}
				}
				successFlag = true;
				//String error = null;
				//finishSerialize(error);
				log.info(String.format("  Total Kilobytes Read: %d\n", kilobytes));
				log.debug("DownloadHandler.finishSerialize()");
			} else {
				successFlag = false;
			}

			return successFlag;

		}
		catch (IOException e) {
		  successFlag = false;
		  e.printStackTrace();
		  throw(e);
		}
		catch (Exception ee) {
			successFlag = false;
			return successFlag;
		}		

	}
    
}
