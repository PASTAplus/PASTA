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

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import edu.lternet.pasta.common.WebResponseFactory;
import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.common.security.token.BasicAuthToken;

/**
 * Used to interact with PASTA's Identity Manager via web service calls.
 *
 */
public final class IdentityManager {

    private IdentityManager() {
        // preventing instantiation
    }

    /**
     * The key used in {@code PastaUrls.properties} to define the Identity
     * Manager's root URL.
     */
    public static final String URL_KEY = "identity.manager.url";

    /**
     * Returns the root URL used to interact with PASTA's Identity Manager, such
     * as {@code https://auth.lternet.edu/identitymanager/}.
     *
     * @return the root URL used to interact with PASTA's Identity Manager.
     */
    public static String getRootUrl() {
        return PastaUrls.getUrl(URL_KEY);
    }

    /**
     * Authenticates the provided user and returns a cookie.
     *
     * @param user
     *            the ID of the user to be authenticated.
     *
     * @param password
     *            the user's password.
     *
     * @param authSystem
     *            the authSystem with which to authenticate the user.
     *
     * @return the cookie returned from the Identity Manager.
     *
     * @throws UnsupportedOperationException
     *             if the provided authSystem is not currently supported.
     *
     * @throws JerseyProxyException
     *             if the response from the Identity Manager is other than
     *             expected.
     */
    public static NewCookie getCookie(String user,
                                      String password,
                                      AuthSystemDef authSystem) {

        if (authSystem != AuthSystemDef.KNB) {
            String s = "The authSystem '" + authSystem + "' is not supported.";
            throw new UnsupportedOperationException(s);
        }

        String url = getRootUrl() + "cookie";

        AuthToken token = new BasicAuthToken(user, password);

        Builder request = makeRequest(url, token);

        ClientResponse response = null;

        try {
            response = request.get(ClientResponse.class);
        }

        catch (Throwable t) {

            String s = "An exception was thrown while sending a request to " +
                       "PASTA's Identity Manager to authenticate the user '" +
                       user + "' with authSystem '" + authSystem.name() +
                       "'. The stack trace is shown below.";

            Response effect = WebResponseFactory.makeExpectationFailed(s, t);

            throw new JerseyProxyException(t, effect);
        }

        ClientResponse.Status status = response.getClientResponseStatus();

        List<NewCookie> cookies = response.getCookies();

        // If the request succeeded
        if (status == ClientResponse.Status.OK && cookies.size() == 1) {
            return cookies.get(0);
        }

        // If the response is unexpected
        String s = "A request was made to PASTA's Identity Manager to " +
                   "authenticate the user '" + user + "' with authSystem '" +
                   "', but the request failed. The response from the " +
                   "Identity Manager is shown below.";

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
                             MediaType.TEXT_XML_TYPE,
                             MediaType.TEXT_PLAIN_TYPE,
                             MediaType.TEXT_HTML_TYPE);

        return AuthTokenFactory.addToken(token, b);
    }

}
