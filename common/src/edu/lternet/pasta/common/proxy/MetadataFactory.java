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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.PercentEncoder;
import edu.lternet.pasta.common.WebResponseFactory;
import edu.lternet.pasta.common.EmlPackageIdFormat.Delimiter;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;

/**
 * Used to interact with PASTA's Metadata Factory via web service calls.
 *
 */
public final class MetadataFactory {

    private MetadataFactory() {
        // preventing instantiation
    }

    /**
     * The key used in {@code PastaUrls.properties} to define the Metadata
     * Factory's root URL.
     */
    public static final String URL_KEY = "metadata.factory.url";

    private static final EmlPackageIdFormat epiFormat =
        new EmlPackageIdFormat(Delimiter.DOT);

    /**
     * Returns the root URL used to interact with PASTA's Metadata Factory, such
     * as {@code https://metadata.lternet.edu/metadatafactory/}.
     *
     * @return the root URL used to interact with PASTA's Metadata Factory.
     */
    public static String getRootUrl() {
        return PastaUrls.getUrl(URL_KEY);
    }

    /**
     * Returns a URL query string representation of the provided EML packageId
     * and entityNames. The string returned will contain pairs of the form
     * <em>&lt;packageId&gt;=&lt;entityName&gt;</em> for each of the provided
     * entity names, delimited by ampersands (&amp;), but it will <em>not</em>
     * begin with a question mark (?).
     *
     * @param epi
     *            the EML packageId.
     *
     * @param entityNames
     *            entityNames in the specified EML document to be included in
     *            provenance metadata.
     *
     * @return a URL query string representation of the provided EML packageId
     *         and entityNames.
     *
     * @throws IllegalArgumentException
     *             if the packageId is incomplete, or if any of the provided
     *             entity names cannot be URL encoded.
     */
    public static String toQueryString(EmlPackageId epi,
                                       List<String> entityNames) {

        if (!epi.allElementsHaveValues()) {
            String s = "incomplete packageId: " + epi;
            throw new IllegalArgumentException(s);
        }

        String packageId = epiFormat.format(epi);

        if (entityNames.isEmpty()) {
            return packageId;
        }

        StringBuilder sb = new StringBuilder();

        Iterator<String> i = entityNames.iterator();

        while (i.hasNext()) {

            sb.append(packageId);
            sb.append('=');
            sb.append(PercentEncoder.encode(i.next()));

            if (i.hasNext()) {
                sb.append('&');
            }
        }

        return sb.toString();
    }

    /**
     * Returns a URL query string representation of the provided EML packageIds
     * and their corresponding entityNames. The string returned will contain
     * pairs of the form <em>&lt;packageId&gt;=&lt;entityName&gt;</em> for each
     * of the provided packageIds and corresponding entity names, delimited by
     * ampersands (&amp;), but it will <em>not</em> begin with a question mark
     * (?).
     *
     * @param pairs
     *            key are the packageIds of the parent EML documents, values are
     *            the entityNames in those document to be included in provenance
     *            metadata.
     *
     * @return a URL query string representation of the provided EML packageIds
     *         and their corresponding entityNames.
     *
     * @throws IllegalArgumentException
     *             if any of the packageIds are incomplete, or if any of the
     *             provided entity names cannot be URL encoded.
     */
    public static String toQueryString(Map<EmlPackageId, List<String>> pairs) {

        StringBuilder sb = new StringBuilder();

        Iterator<Entry<EmlPackageId, List<String>>> i =
            pairs.entrySet().iterator();

        while (i.hasNext()) {

            Entry<EmlPackageId, List<String>> entry = i.next();
            EmlPackageId epi = entry.getKey();
            List<String> entityNames = entry.getValue();

            sb.append(toQueryString(epi, entityNames));

            if (i.hasNext()) {
                sb.append('&');
            }
        }

        return sb.toString();
    }

    /**
     * Returns a complete URL that can be used to append provenance to an EML
     * document using PASTA's Metadata Factory. The provided entity names will
     * be URL encoded and added to the query string with their corresponding
     * packageId.
     *
     * @param pairs
     *            keys are the packageIds of the parent EML documents, values
     *            are the entityNames in those documents to be included in
     *            provenance metadata.
     *
     * @return a complete URL that can be used to append provenance to an EML
     *         document using PASTA's Metadata Factory.
     */
    public static String makeUrl(Map<EmlPackageId, List<String>> pairs) {

        StringBuilder sb = new StringBuilder();

        sb.append(getRootUrl());
        sb.append("eml/?");
        sb.append(toQueryString(pairs));

        return sb.toString();
    }

    /**
     * Appends provenance metadata to the provided EML document. The provided
     * EML document must contain a single {@code dataset} node. The provided EML
     * packageIds specify the "parent" EML documents, and the provided entity
     * names will be included in provenance.
     *
     * @param eml
     *            the EML document to which provenance will be appended.
     * @param pairs
     *            keys are the packageIds of the parent EML documents, values
     *            are the entityNames in those documents to be included in
     *            provenance metadata.
     * @param token
     *            contains the credentials of the requesting user.
     *
     * @return the EML document returned from the Metadata Factory.
     *
     * @throws JerseyProxyException
     *             if the response from the Metadata Catalog is other than
     *             expected.
     */
    public static String appendProvenance(String eml,
                                          Map<EmlPackageId, List<String>> pairs,
                                          AuthToken token) {

        String url = makeUrl(pairs);

        Builder request = makeRequest(url, token);

        ClientResponse response = null;

        try {
            request.header(HttpHeaders.CONTENT_TYPE, "application/xml");
            response = request.put(ClientResponse.class, eml);
        }

        catch (Throwable t) {

            String s = "An exception was thrown while sending a request to " +
                       "PASTA's Metadata Factory to append provenance to " +
                       "an EML document. The stack trace is shown below.";

            Response effect = WebResponseFactory.makeExpectationFailed(s, t);

            throw new JerseyProxyException(t, effect);
        }

        // If the request succeeded
        ClientResponse.Status status = response.getClientResponseStatus();

        if (status == ClientResponse.Status.OK) {
            return response.getEntity(String.class);
        }

        // If the response is unexpected
        String s = "A request was made to PASTA's Metadata Factory to "
                + "append provenance to an EML document, but the request "
                + "failed. The response from the Metadata Factory is "
                + "shown below.";

        BufferedClientResponse<String> bufferedResponse =
            new BufferedClientResponse<String>(response, String.class);

        Response effect =
            WebResponseFactory.makeExpectationFailed(s, bufferedResponse);

        throw new JerseyProxyException(bufferedResponse, effect);
    }

    private static Builder makeRequest(String url, AuthToken token) {

        // Building the request
        //WebResource r = ApacheHttpClient.create().resource(url);
        WebResource r = Client.create().resource(url);

        Builder b = r.accept(MediaType.APPLICATION_XML_TYPE,
                             MediaType.TEXT_PLAIN_TYPE,
                             MediaType.TEXT_HTML_TYPE);

        return AuthTokenFactory.addToken(token, b);
    }

}
