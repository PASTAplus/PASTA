/**
 *    '$RCSfile: TarDataHandler.java,v $'
 *
 *     '$Author: costa $'
 *       '$Date: 2006-11-15 22:49:35 $'
 *   '$Revision: 1.9 $'
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.lternet.pasta.dml.parser.Entity;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

/**
 * This is a sub-class of ArchiveddDataHandler class. It will handle 
 * download tar data entity. After downloading, the tar entity will be 
 * unarchived and written to DataStorageInterface transparently.
 * 
 * @author tao
 *
 */
public class TarDataHandler extends ArchivedDataHandler
{
  /*
   * Constructors
   */
  
  /**
   * Constructor
   * @param entity The entity object whose data is being downloaded. Used for
   *               quality reporting. Can be set to null in cases where we 
   *               don't need a back-pointer to the entity.
   * @param url  The url (or identifier) of the tar entity
   * @param endPoint the object which provides ecogrid endpoint information
  */
  protected TarDataHandler(Entity entity, String url, EcogridEndPointInterface endPoint)
  {
    super(entity, url, endPoint);
  }

    /*
     * Constructor
     * @param url  The url (or identifier) of the tar entity
     * @param endPoint the object which provides ecogrid endpoint information
     */
  protected TarDataHandler(String url, EcogridEndPointInterface endPoint)
  {
    super(url, endPoint);
  }
  
  /*
   * Class methods
   */  
  
  /**
   * Gets the TarDataHandler object
   * 
   * @param   entity The entity object whose data is being downloaded, possibly null
   * @param   url The url (or identifier) of entity need be downloaded
   * @param   endPoint the object which provides ecogrid endpoint information
   * @return  TarDataHandler object associated with this url
   */
  public static TarDataHandler getTarHandlerInstance(Entity entity,
                                                     String url, 
                                                     EcogridEndPointInterface endPoint)
  {
    TarDataHandler  tarHandler = (TarDataHandler)getHandlerFromHash(url);
        
    if (tarHandler == null)
    {
      tarHandler = new TarDataHandler(entity, url, endPoint);
    }
        
    return tarHandler;
  }
 
    
    /*
     * Overwrite the the method in DownloadHandler in order to unarchive it.
     * It only writes first file (if it have mutiple entities) into 
     * DataStorageSystem.
     */
    protected boolean writeRemoteInputStreamIntoDataStorage(InputStream in) 
            throws IOException
    {
 	   boolean success = false;
 	   TarInputStream tarInputStream = null;
       
 	   if (in == null)
 	   {
 		   return success;
 	   }
 	   
 	   try
 	   {
 		   tarInputStream = new TarInputStream(in);
 		   TarEntry entry = tarInputStream.getNextEntry();
 		   int index = 0;
           
 		   while ((entry != null) && (index < 1))
 		   {
 			  if (entry.isDirectory())
 			  {
 				  entry = tarInputStream.getNextEntry();
 				  continue;
 			  }
              
 			  // this method will close the tarInputStream, and tarInputStream is not null!!!
 		      success = super.writeRemoteInputStreamIntoDataStorage(tarInputStream);
 		      index++;
 		   }        
 	   }
     catch (IOException e)
     {
       String errorMsg = String.format("%s %s: %s", 
                                       ONLINE_URLS_EXCEPTION_MESSAGE,
                                       "Error downloading tar file", 
                                       e.getMessage()
                                      );       
       throw new IOException(errorMsg);
     }
       
 	   return success;
    }
    
       
    /*
     *  Get data from Ecogrid server base on given Ecogrid endpoint and 
     *  identifier. This method includes the uncompress process.
     *  It overwrites the one in DownloadHandler.java
     */
    protected boolean getContentFromEcoGridSource(String endPoint, 
                                                  String ecogridIdentifier)
    {
 	   boolean success = false;
        File zipTmp = writeEcoGridArchivedDataIntoTmp(endPoint, 
                                                      ecogridIdentifier, 
                                                      ".tar");
        try
        {
 	       if (zipTmp != null)
 	       {
 	    	  InputStream stream = new FileInputStream(zipTmp);
 	    	  success = this.writeRemoteInputStreamIntoDataStorage(stream);
 	       }
        }
        catch(Exception e)
        {
     	   System.out.println("Error is " + e.getMessage());
        }
        
        return success;
    }

}
