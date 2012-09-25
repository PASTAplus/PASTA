/*
 * 
 * $Date: 2012-02-28 11:02:24 -0700 (Tue, 28 Feb 2012) $ $Author: jmoss $ $Revision: 1728 $
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

package edu.lternet.pasta.metadatamanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.EmlUtility;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.ucsb.nceas.metacat.client.DocumentNotFoundException;
import edu.ucsb.nceas.metacat.client.InsufficientKarmaException;
import edu.ucsb.nceas.metacat.client.Metacat;
import edu.ucsb.nceas.metacat.client.MetacatAuthException;
import edu.ucsb.nceas.metacat.client.MetacatException;
import edu.ucsb.nceas.metacat.client.MetacatFactory;
import edu.ucsb.nceas.metacat.client.MetacatInaccessibleException;


/**
 * The MetacatMetadataCatalog class implements the MetadataCatalog interface to
 * provide access to the Metacat repository.
 */
public class MetacatMetadataCatalog implements MetadataCatalog {

  /*
   * Class variables
   */

    private static Logger logger = Logger
            .getLogger(MetacatMetadataCatalog.class);


    /*
     * Instance variables
     */
    
    private String pastaUser = null;
    private Metacat metacat = null;
    private String metacatUrl = null;

    private enum Action {
        CREATE, READ, UPDATE, DELETE, READACL, QUERY
    }
    
    
    /*
     * Constructors
     */
    
    /**
     * Constructs a MetacatMetadataCatalog object with the specified
     * metacatUrl and pastaUser settings.
     */
    public MetacatMetadataCatalog(String metacatUrl, String pastaUser) {
      this.metacatUrl = metacatUrl;
      this.pastaUser = pastaUser;
    }
    

    /*
     * Class methods
     */
    
    
    
    /*
     * Instance methods
     */
    
    /**
     * Creates an EML Document entry in the Metacat Repository.
     * 
     * @param emlDocument
     *            the EML Document.
     * @return a String from Metacat representing whether the creation was
     *         successful.
     * @throws IllegalStateException
     *             failure in input/output including inability to connect to
     *             Metacat.
     * @throws UnauthorizedException
     *             failure to authenticate.
     * @throws UserErrorException
     *             any further Metacat based exceptions.
     */
    public String createEmlDocument(String emlDocument)
            throws IllegalStateException, UnauthorizedException,
            UserErrorException {

        Document doc = XmlUtility.xmlStringToDoc(emlDocument);
        EmlPackageId epid = EmlUtility.getEmlPackageId(doc);
        return metacatAction(Action.CREATE, epid, emlDocument);
    }

    
    /**
     * Reads an EML Document from the Metacat Repository.
     * 
     * @param epid
     *            an EmlPackageId representing the EML Document.
     * @return a String from Metacat representing the requested EML Document.
     * @throws IllegalStateException
     *             failure in input/output including inability to connect to
     *             Metacat.
     * @throws ResourceNotFoundException
     *             the request EmlPackageId was not found.
     * @throws UnauthorizedException
     *             failure to authenticate.
     * @throws UserErrorException
     *             any further Metacat based exceptions.
     */
    public String readEmlDocument(EmlPackageId epid)
            throws IllegalStateException, ResourceNotFoundException,
            UnauthorizedException, UserErrorException {

        return metacatAction(Action.READ, epid, null);
    }

    
    /**
     * Updates an EML Document in the Metacat Repository.
     * 
     * @param epid
     *            an EmlPackageId representing the EML Document.
     * @param emlDocument
     *            an EML Document.
     * @return a String from Metacat representing the whether the update was
     *         successful.
     * @throws IllegalStateException
     *             failure in input/output including inability to connect to
     *             Metacat.
     * @throws UnauthorizedException
     *             failure to authenticate.
     * @throws UserErrorException
     *             any further Metacat based exceptions.
     */
    public String updateEmlDocument(EmlPackageId epid, String emlDocument)
            throws IllegalStateException, UnauthorizedException,
            UserErrorException {

        return metacatAction(Action.UPDATE, epid, emlDocument);
    }

    
    /**
     * Deletes an EML Document in the Metacat Repository.
     * 
     * @param epid
     *            an EmlPackageId representing the EML Document.
     * @return a String from Metacat representing whether the delete was
     *         successful.
     * @throws IllegalStateException
     *             failure in input/output including inability to connect to
     *             Metacat.
     * @throws UnauthorizedException
     *             failure to authenticate.
     * @throws UserErrorException
     *             any further Metacat based exceptions.
     */
    public String deleteEmlDocument(EmlPackageId epid)
            throws IllegalStateException, UnauthorizedException,
            UserErrorException {

        return metacatAction(Action.DELETE, epid, null);
    }

    
    /**
     * Query the Metacat Repository.
     * 
     * @param xmlQuery
     *            the query in appropriate metacat string format.
     *            
     * @return a String from Metacat representing the query.
     * 
     * @throws IllegalStateException
     *             failure in input/output including inability to connect to
     *             Metacat.
     * @throws UnauthorizedException
     *             failure to authenticate.
     * @throws UserErrorException
     *             any further Metacat based exceptions.
     */
    public String query(String xmlQuery)
            throws IllegalStateException, UnauthorizedException,
            UserErrorException {

        return metacatAction(Action.QUERY, null, xmlQuery);
    }

    
    /**
     * Gets the Access Control List of an EML Document in the Metacat
     * Repository.
     * 
     * @param epid
     *            an EmlPackageId representing the EML Document.
     * @return a String from Metacat representing the ACL.
     * @throws IllegalStateException
     *             failure in input/output including inability to connect to
     *             Metacat.
     * @throws UnauthorizedException
     *             failure to authenticate.
     * @throws UserErrorException
     *             any further Metacat based exceptions.
     */
    public String getAccessControlList(EmlPackageId epid)
            throws IllegalStateException, UnauthorizedException,
            UserErrorException {

        return metacatAction(Action.READACL, epid, null);
    }

    
    /**
     * Makes a connection to the Metacat Repository.
     */
    private void connectMetacatClient()
            throws IllegalStateException, UnauthorizedException {

        String metacatResponse = null;

        try {
            metacat = MetacatFactory.createMetacatConnection(metacatUrl);
            metacatResponse = metacat.login(pastaUser, "");
        }
        catch (MetacatInaccessibleException e) {
            logger.fatal("Metacat Inaccessible: " + metacatResponse);
            throw new IllegalStateException(e);
        }
        catch (MetacatAuthException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    
    /**
     * Disconnects a connection to the Metacat Repository.
     */
    private void disconnectMetacatClient() throws IllegalStateException,
            UserErrorException {

        try {
            metacat.logout();
        }
        catch (MetacatInaccessibleException e) {
            throw new IllegalStateException(e);
        }
        catch (MetacatException e) {
            throw new UserErrorException(e.getMessage());
        }
    }

    
    /*
     * Uses the metacat client to perform one of several different
     * Metacat actions, returning the result string.
     */
    private String metacatAction(Action action, EmlPackageId emlPackageId,
            String document)
            throws IllegalStateException, ResourceNotFoundException,
                   UnauthorizedException, UserErrorException {

        EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
        
        String packageId = (emlPackageId != null) ? 
                           emlPackageIdFormat.format(emlPackageId) : 
                           null;
                           
        StringReader stringReader = (document != null) ? 
                                    stringReader = new StringReader(document) : 
                                    null;
                                    
        String metacatResultStr = null;
        connectMetacatClient();

        try {
            switch (action) {
            case CREATE:
                metacatResultStr = metacat.insert(packageId, stringReader, null);
                break;
            case UPDATE:
                metacatResultStr = metacat.update(packageId, stringReader, null);
                break;
            case READ:
                metacatResultStr = inputStreamToString(metacat.read(packageId));
                break;
            case DELETE:
                metacatResultStr = metacat.delete(packageId);
                break;
            case READACL:
                metacatResultStr = metacat.getAccessControl(packageId);
                break;
            case QUERY:
                Reader queryReader = metacat.query(stringReader);
                metacatResultStr = IOUtils.toString(queryReader);
                break;
            }
        }
        catch (DocumentNotFoundException e) {
            disconnectMetacatClient();
            throw new ResourceNotFoundException(e.getMessage());
        }
        catch (InsufficientKarmaException e) {
            disconnectMetacatClient();
            throw new UnauthorizedException(e.getMessage());
        }
        catch (MetacatException e) {
            disconnectMetacatClient();
            throw new UserErrorException(e.getMessage());
        }
        catch (IOException e) {
            disconnectMetacatClient();
            throw new IllegalStateException(e);
        }
        catch (MetacatInaccessibleException e) {
            throw new IllegalStateException(e);
        }

        disconnectMetacatClient();

        return metacatResultStr;
    }

    
  private String inputStreamToString(InputStream inputStream)
      throws IOException {

    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    String line = null;
    StringBuilder stringBuilder = new StringBuilder();

    try {
      while ((line = bufferedReader.readLine()) != null)
        stringBuilder.append(line).append("\n");
    }
    catch (IOException e) {
      logger.error(e.getMessage());
      throw e;
    }

    return stringBuilder.toString();
  }

}
