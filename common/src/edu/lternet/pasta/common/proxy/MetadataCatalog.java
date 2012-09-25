/*
 * $Date$
 * $Author$
 * $Revision$
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
 * Used to interact with PASTA's Metadata Catalog via web service calls.
 *
 */
public final class MetadataCatalog {

    private MetadataCatalog() {
        // preventing instantiation
    }

    /**
     * The key used in {@code PastaUrls.properties} to define the Metadata
     * Catalog's root URL.
     */
    public static final String URL_KEY = "metadata.catalog.url";

    /**
     * Returns the root URL used to interact with PASTA's Metadata Catalog, such
     * as {@code https://metadata.lternet.edu/metadata/}.
     */
    public static String getRootUrl() {
        return PastaUrls.getUrl(URL_KEY);
    }

    /**
     * Returns a URL for the EML document with the provided packageId in PASTA's
     * Metadata Catalog.
     *
     * @param packageId
     *            the packageId of the EML document.
     *
     * @return a URL for the EML document with the provided packageId in PASTA's
     *         Metadata Catalog.
     *
     * @throws IllegalArgumentException
     *             if the provided packageId contains a {@code null} value.
     */
    public static String getMetadataUrl(EmlPackageId packageId) {

        if (!packageId.allElementsHaveValues()) {
            String s = "incomplete packageId: " + packageId;
            throw new IllegalArgumentException(s);
        }

        return makeUrl(packageId);
    }

    private static String makeUrl(EmlPackageId packageId) {

        EmlPackageIdFormat format =
            new EmlPackageIdFormat(Delimiter.FORWARD_SLASH);

        return getRootUrl() + "eml/" + format.format(packageId);
    }

    /**
     * Returns the EML document with the provided packageId by requesting it
     * from PASTA's Metadata Catalog.
     *
     * @param packageId
     *            the packageId of the EML document to be returned.
     * @param token
     *            the authorization token used to make the request.
     *
     * @return the EML document with the provided packageId by requesting it
     *         from PASTA's Metadata Catalog.
     *
     * @throws IllegalArgumentException
     *             if the provided packageId contains a {@code null} value.
     *
     * @throws JerseyProxyException
     *             if the request to PASTA's Metadata Catalog fails.
     */
    public static String getMetadata(EmlPackageId packageId, AuthToken token) {

        String url = getMetadataUrl(packageId);

        Builder request = makeRequest(url, token);

        EmlPackageIdFormat epiFormat = new EmlPackageIdFormat(Delimiter.DOT);
        ClientResponse response = null;

        try {
            response = request.get(ClientResponse.class);
        }

        catch (Throwable t) {

            String s = "An exception was thrown while sending a request to " +
                       "PASTA's Metadata Catalog for the EML document " +
                       "with packageId '" + epiFormat.format(packageId) +
                       "'. The stack trace is shown below.";

            Response effect = WebResponseFactory.makeExpectationFailed(s, t);

            throw new JerseyProxyException(t, effect);
        }

        // If the request succeeded
        StatusType status = response.getClientResponseStatus();

        if (status == ClientResponse.Status.OK) {
            return response.getEntity(String.class);
        }

        // If the response is unexpected
        String s = "The EML document with packageId '" +
                   epiFormat.format(packageId) +
                   "' was requested from PASTA's Metadata Catalog, " +
                   "but the request failed. The response from the Metadata " +
                   "Catalog is shown below.";

        BufferedClientResponse<String> bufferedResponse =
            new BufferedClientResponse<String>(response, String.class);

        Response effect =
            WebResponseFactory.makeExpectationFailed(s, bufferedResponse);

        throw new JerseyProxyException(bufferedResponse, effect);
    }

    /**
     * Inserts an EML document in PASTA's Metadata Catalog.
     *
     * @param eml
     *            the EML document to be inserted.
     *
     * @param token
     *            the authorization token used to make the request.
     *
     * @throws JerseyProxyException
     *             if the request to PASTA's Metadata Catalog fails.
     */
    public static void insertMetadata(String eml, AuthToken token) {

        String url = getRootUrl() + "eml/";

        Builder request = makeRequest(url, token);

        request = request.type(MediaType.APPLICATION_XML);

        ClientResponse response = null;

        try {
            response = request.post(ClientResponse.class, eml);
        }

        catch (Throwable t) {

            String s = "An exception was thrown while sending a request to " +
                       "PASTA's Metadata Catalog to create an EML document. " +
                       "The stack trace is shown below.";

            Response effect = WebResponseFactory.makeExpectationFailed(s, t);

            throw new JerseyProxyException(t, effect);
        }

        // If the request succeeded
        StatusType status = response.getClientResponseStatus();

        if (status == ClientResponse.Status.CREATED) {
            return;
        }

        // If the response is unexpected
        String s = "A request was made to create the provided EML document " +
                   "in PASTA's Metadata Catalog, but the request failed. " +
        		           "The response from the Metadata Catalog is shown below.";

        BufferedClientResponse<String> bufferedResponse =
            new BufferedClientResponse<String>(response, String.class);

        Response effect =
            WebResponseFactory.makeExpectationFailed(s, bufferedResponse);

        throw new JerseyProxyException(bufferedResponse, effect);
    }

    /**
     * Indicates if an EML document with the provided packageId exists in
     * PASTA's Metadata Catalog.
     *
     * @param packageId
     *            the EML packageId.
     *
     * @param token
     *            the authorization token used to make the request.
     *
     * @return {@code true} or {@code false}, if the Metadata Catalog clearly
     *         indicates existence or non-existence, respectively, of the
     *         specified EML document.
     *
     * @throws JerseyProxyException
     *             if the response from the Metdata Catalog does not clearly
     *             indicate either existence or non-existence of the specified
     *             EML document.
     */
    public static boolean exists(EmlPackageId packageId, AuthToken token) {

        try {

            getMetadata(packageId, token);
            return true;

        } catch (JerseyProxyException e) {

            if (!e.serverResponded()) {
                throw e;
            }

            ClientResponse.Status status =
                e.getResponseFromServer().getClientResponseStatus();

            if (status.equals(ClientResponse.Status.NOT_FOUND)) {
                return false;
            }

            throw e;
        }

    }

    private static Builder makeRequest(String url, AuthToken token) {

        // Building the request
        //WebResource r = ApacheHttpClient.create().resource(url);
        WebResource r = Client.create().resource(url);

        WebResource.Builder b = r.accept(MediaType.APPLICATION_XML_TYPE,
                                         MediaType.TEXT_PLAIN_TYPE);

        return AuthTokenFactory.addToken(token, b);
    }


}
