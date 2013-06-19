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

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.PastaServiceUtility;
import edu.lternet.pasta.common.PastaWebService;

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
     * Returns a subscription service.
     * @return a subscription service.
     */
    protected SubscriptionService getSubscriptionService() {
        return new SubscriptionService();
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
