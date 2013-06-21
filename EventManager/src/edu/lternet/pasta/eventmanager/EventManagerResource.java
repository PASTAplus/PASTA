/*
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

package edu.lternet.pasta.eventmanager;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.PastaServiceUtility;
import edu.lternet.pasta.common.PastaWebService;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.security.authorization.AccessMatrix;
import edu.lternet.pasta.common.security.authorization.InvalidPermissionException;
import edu.lternet.pasta.common.security.authorization.Rule;
import edu.lternet.pasta.common.security.token.AuthToken;

/**
 * An abstract class that provides utility methods for the components of the
 * Event Manager web service.
 *
 * @webservicename Event Manager
 * @baseurl https://event.lternet.edu/
 */
@Path("")
public class EventManagerResource extends PastaWebService {

    /**
     * Returns the access control rule for creating EML modification
     * subscriptions.
     *
     * @param method A String representing the serviceMethod Name that
     *               initiated the call sequence.
     * @return the access control rule for creating EML modification
     *         subscriptions.
     */
    protected String getServiceAcr(String method) {
        File acrFileName = ConfigurationListener.getPastaServiceAcr();
        String pastaService = FileUtility.fileToString(acrFileName);
        return PastaServiceUtility.getAccessTypeString(method, pastaService);
    }
   
    
    /**
     * Serves the files related to the demo web page.
     * @param fileName the name of the file to be served.
     * @return an appropriate HTTP response.
     */
    @GET
    @Path("/demo/{file}")
    public Response respondWithDemoPage(@PathParam("file") String fileName) {
        File demoDir = ConfigurationListener.getDemoDirectory();
        return serveFileFromDirectory(demoDir, fileName);
    }

    
    /**
     * Returns the Event Manager's version, such as {@code
     * eventmanager-0.1}.
     *
     * @return the Event Manager's version, such as {@code
     *         eventmanager-0.1}.
     */
    @Override
    public String getVersionString() {
        return ConfigurationListener.getWebServiceVersion();
    }
    

    /**
     * Returns the API documentation for the Event Manager.
     *
     * @return the API documentation for the Event Manager.
     */
    @Override
    public File getApiDocument() {
        return ConfigurationListener.getApiDocument();
    }

    
    /**
     * Returns the tutorial document for the Event Manager.
     *
     * @return the tutorial document for the Event Manager.
     */
    @Override
    public File getTutorialDocument() {
        return ConfigurationListener.getTutorialDocument();
    }

    
    /**
     * Returns the welcome page for the Event Manager.
     *
     * @return the welcome page for the Event Manager.
     */
    @Override
    public File getWelcomePage() {
        return ConfigurationListener.getWelcomePage();
    }

    
    /**
     * Constructs an Event Manager resource with the provided entity
     * manager factory.
     *
     * @param emf the entity manager factory used by this resource.
     */
    public EventManagerResource() {
    }


    /**
     * Boolean to determine whether the user contained in the AuthToken
     * is authorized to execute the specified service method.
     * 
     * @param serviceMethodName     the name of the service method
     * @param authToken             the AuthToken containing the user name
     * @return  true if authorized to run the service method, else false
     */
    protected boolean isServiceMethodAuthorized(String serviceMethodName, 
                                              Rule.Permission permission, 
                                              AuthToken authToken) {
      boolean isAuthorized = false;
      NodeList nodeList = null;    
      String serviceDocumentStr = ConfigurationListener.getServiceDocument();
      Document document = XmlUtility.xmlStringToDoc(serviceDocumentStr);
      
      try {
        if (document != null) {
          NodeList documentNodeList = document.getChildNodes();
          Node rootNode = documentNodeList.item(0);
          nodeList = rootNode.getChildNodes();
          
          if (nodeList != null) {
            int nodeListLength = nodeList.getLength();
            for (int i = 0; i < nodeListLength; i++) {
              Node childNode = nodeList.item(i);
              String nodeName = childNode.getNodeName();
              String nodeValue = childNode.getNodeValue();
              if (nodeName.contains("service-method")) {
                Element serviceElement = (Element) nodeList.item(i);
                NamedNodeMap serviceAttributesList = serviceElement.getAttributes();
                
                for (int j = 0; j < serviceAttributesList.getLength(); j++) {
                  Node attributeNode = serviceAttributesList.item(j);
                  nodeName = attributeNode.getNodeName();
                  nodeValue = attributeNode.getNodeValue();
                  if (nodeName.equals("name")) {
                    String name = nodeValue;
                    if (name.equals(serviceMethodName)) {
                      NodeList accessNodeList = serviceElement
                              .getElementsByTagName("access");
                      Node accessNode = accessNodeList.item(0);
                      String accessXML = XmlUtility.nodeToXmlString(accessNode);
                      AccessMatrix accessMatrix = new AccessMatrix(accessXML);
                      String principalOwner = "pasta";
                      isAuthorized = accessMatrix.isAuthorized(authToken,
                        principalOwner, permission);
                    }
                  }
                }
              }
            }
          }
        }
        else {
          String message = "No service methods were found in the service.xml file";
          throw new IllegalStateException(message);
        }
      }
      catch (InvalidPermissionException e) {
        throw new IllegalStateException(e);
      }

      return isAuthorized;
    }


    protected Integer parseSubscriptionId(String s) {
      try {
          return new Integer(s);
      } 
      catch (NumberFormatException e) {
          String err = String.format("The provided subscription ID '%s' cannot be parsed as an integer.", s);
          throw new IllegalArgumentException(err);
      }
  }

}
