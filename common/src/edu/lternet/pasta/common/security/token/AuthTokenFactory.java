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

package edu.lternet.pasta.common.security.token;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

import com.sun.jersey.api.client.WebResource;

import edu.lternet.pasta.common.WebExceptionFactory;
import edu.lternet.pasta.common.security.auth.AuthSystemDef;

/**
 * Used as a factory for authorization token objects. A string
 * representation of a token is provided, it is parsed, and a corresponding
 * token object is returned. Token objects can also be added to the headers
 * of Jersey Client {@link WebResource} objects.
 */
public final class AuthTokenFactory {

    private AuthTokenFactory() {
        // preventing instantiation
    }

    private static final String TOKEN_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String AUTH_TOKEN = "auth-token";

    /**
     * Returns an authorization token that corresponds to the provided string.
     *
     * @return an authorization token that corresponds to the provided string.
     *
     * @throws WebApplicationException
     *             if the provided token cannot be parsed.
     */
    public static AuthToken makeAuthToken(String token) {
        return makeAuthTokenWithPassword(token);
    }

    /**
     * Returns an authorization token that corresponds to the provided string.
     *
     * @return an authorization token that corresponds to the provided string.
     *
     * @throws WebApplicationException
     *             if the provided token cannot be parsed.
     */
    public static AuthTokenWithPassword
                    makeAuthTokenWithPassword(String token) {

        if (token == null) {
            throw new NullPointerException("null token string.");
        }

        return new BasicAuthToken(token);
    }

    public static AttrListAuthTokenV1 makeCookieAuthToken(String token) {
        if (token == null) {
            throw new NullPointerException("null token string.");
        }

        return new AttrListAuthTokenV1(token);
    }

    public static AttrListAuthTokenV1 makeCookieAuthToken(String user,
                                                   AuthSystemDef authSystem,
                                                            long expirationDate,
                                                     Set<String> groups) {
        if (user == null) {
            throw new NullPointerException("null user string.");
        }

        if (authSystem == null) {
            throw new IllegalStateException("null AuthSystemDef.");
        }

        if (groups == null) {
            throw new IllegalStateException("null groups.");
        }

        return new AttrListAuthTokenV1(user, authSystem,
                                       expirationDate,
                                       groups);
    }
    /**
     * Returns the token string contained in the provided HTTP request headers.
     *
     * @param requestHeaders
     *            the HTTP request headers.
     * @return the token string contained in the provided HTTP request headers.
     *
     * @throws WebApplicationException
     *             if the provided headers do not contain a token.
     */
    public static String getTokenString(HttpHeaders requestHeaders) {

        List<String> token = requestHeaders.getRequestHeader(TOKEN_HEADER);

        if (token == null || token.isEmpty()) {
            String s = "The request does not contain an '" +
                       TOKEN_HEADER + "' header.";
            throw WebExceptionFactory.makeBadRequest(s);
        }

        StringBuilder tokenStringBuilder = new StringBuilder();

        Iterator<String> i = token.iterator();

        while(i.hasNext()) {
            tokenStringBuilder.append(i.next());
            if (i.hasNext()) {
                tokenStringBuilder.append(' ');
            }
        }

        return tokenStringBuilder.toString();
    }

    public static String getTokenString(Map<String, Cookie> cookieMap) {

        if (!cookieMap.containsKey(AUTH_TOKEN)) {
            String s = "The request does not contain an '" + AUTH_TOKEN +
                       "' in the Cookie header.";
            throw WebExceptionFactory.makeUnauthorized(s);
        }

        return cookieMap.get(AUTH_TOKEN).getValue();
    }

    /**
     * Returns an authorization token that corresponds to the token string
     * contained in the provided HTTP headers.
     *
     * @param requestHeaders
     *            the HTTP request headers.
     * @return an authorization token that corresponds to the token string
     *         contained in the provided HTTP headers.
     *
     * @throws WebApplicationException
     *             if the provided headers do not contain a token, or if the
     *             token cannot be parsed.
     */
    public static AuthToken makeAuthToken(HttpHeaders requestHeaders) {

        String token = getTokenString(requestHeaders);

        return makeAuthToken(token);
    }

    public static AuthToken makeAuthToken(Map<String, Cookie> cookieMap) {

        String token = getTokenString(cookieMap);

        return makeCookieAuthToken(token);
    }
    /**
     * Returns an authorization token that corresponds to the token string
     * contained in the provided HTTP headers.
     *
     * @param requestHeaders
     *            the HTTP request headers.
     * @return an authorization token that corresponds to the token string
     *         contained in the provided HTTP headers.
     *
     * @throws WebApplicationException
     *             if the provided headers do not contain a token, or if the
     *             token cannot be parsed.
     */
    public static AuthTokenWithPassword
                    makeAuthTokenWithPassword(HttpHeaders requestHeaders) {

        String token = getTokenString(requestHeaders);

        return makeAuthTokenWithPassword(token);
    }

    /**
     * Converts the provided token object to a string and adds it to the headers
     * of the provided web resource builder.
     *
     * @param token
     *            the token to be added to the web resource.
     * @param builder
     *            the web resource builder.
     * @return a web resource builder with the provided token in its headers.
     */
    public static WebResource.Builder addToken(AuthToken token,
                                               WebResource.Builder builder) {

        return addToken(token.getTokenString(), builder);
    }

    /**
     * Adds the provided token string to the headers of the provided web
     * resource builder.
     *
     * @param token
     *            the token string to be added to the web resource.
     * @param builder
     *            the web resource builder.
     * @return a web resource builder with the provided token in its headers.
     */
    public static WebResource.Builder addToken(String token,
                                               WebResource.Builder builder) {

        Cookie c = new Cookie(AUTH_TOKEN, token);
        return builder.cookie(c);
    }

}
