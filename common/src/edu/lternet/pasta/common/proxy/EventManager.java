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

package edu.lternet.pasta.common.proxy;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.WebResponseFactory;
import edu.lternet.pasta.common.EmlPackageIdFormat.Delimiter;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;

/**
 * Used to interact with PASTA's Event Manager via web service calls.
 *
 */
public final class EventManager {

    private EventManager() {
        // preventing instantiation
    }

    /**
     * The key used in {@code PastaUrls.properties} to define the Event
     * Manager's root URL.
     */
    public static final String URL_KEY = "event.manager.url";

    /**
     * Returns the root URL used to interact with PASTA's Event Manager, such
     * as {@code https://event.lternet.edu/eventmanager/}.
     *
     * @return the root URL used to interact with PASTA's Event Manager.
     */
    public static String getRootUrl() {
        return PastaUrls.getUrl(URL_KEY);
    }

    private static String makeUrl(EmlPackageId packageId) {

        if (!packageId.allElementsHaveValues()) {
            String s = "incomplete packageId: " + packageId;
            throw new IllegalArgumentException(s);
        }

        EmlPackageIdFormat format =
            new EmlPackageIdFormat(Delimiter.FORWARD_SLASH);

        return getRootUrl() + "event/eml/" + format.format(packageId);
    }

    /**
     * Notifies PASTA's Event Manager that an EML document in PASTA's Metadata
     * Catalog has been modified.
     *
     * @param packageId
     *            the packageId of the modified EML document.
     *
     * @param token
     *            the authorization credentials used during notification.
     *
     * @throws IllegalArgumentException
     *             if the provided packageId is incomplete.
     */
    public static void emlModified(EmlPackageId packageId, AuthToken token) {

        String url = makeUrl(packageId);

        Builder request = makeRequest(url, token);

        ClientResponse response = null;

        EmlPackageIdFormat epiFormat = new EmlPackageIdFormat(Delimiter.DOT);

        try {
            response = request.post(ClientResponse.class);
        }

        catch (Throwable t) {

            String s = "An exception was thrown while notifying PASTA's " +
                       "Event Manager that the EML document with packageId '" +
                       epiFormat.format(packageId) +
                       "' was modified. The stack trace is shown below.";

            Response effect = WebResponseFactory.makeExpectationFailed(s, t);

            throw new JerseyProxyException(t, effect);
        }

        // If the request succeeded
        StatusType status = response.getClientResponseStatus();

        if (status == ClientResponse.Status.OK) {
            return;
        }

        // If the response is unexpected
        BufferedClientResponse<String> bufferedResponse =
            new BufferedClientResponse<String>(response, String.class);

        String s = "A request was sent to PASTA's Event Manager to notify " +
                   "it that the EML document with packageId '" +
                   epiFormat.format(packageId) +
                   "' was modified, but the request failed. The response " +
                   "from the Event Manager is shown below.";

        Response effect =
            WebResponseFactory.makeExpectationFailed(s, bufferedResponse);

        throw new JerseyProxyException(bufferedResponse, effect);
    }

    private static Builder makeRequest(String url, AuthToken token) {

        // Building the request
        //WebResource r = ApacheHttpClient.create().resource(url);
        WebResource r = Client.create().resource(url);

        Builder b = r.accept(MediaType.APPLICATION_XML_TYPE,
                             MediaType.TEXT_PLAIN_TYPE);

        return AuthTokenFactory.addToken(token, b);
    }

}
