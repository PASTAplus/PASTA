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

package edu.lternet.pasta.datapackagemanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.AttrListAuthTokenV1;
import edu.lternet.pasta.common.security.token.AuthToken;

/**
 * Dummy HTTP headers except for Basic Access Authentication.
 */
public class DummyCookieHttpHeaders implements HttpHeaders {

    private final String user;
    List<MediaType> acceptHeaders = null;
    private final String COOKIE_NAME = "auth-token";
    private final long EXPIRATION = 2000000000;

    /**
     * Constructs an HTTP header object with the provided user and password. If
     * the provided user is {@code null}, the authorization credentials will be
     * non-existent.
     * 
     * @param user the user ID.
     */
    public DummyCookieHttpHeaders(String user) {
        this.user = user;
    }

    //@Override
    public Map<String, Cookie> getCookies() {
      Map<String, Cookie> m = new HashMap<String, Cookie>();
      Set<String> s = new HashSet<String>();
      s.add("vetted");
      s.add("authenticated");
      AuthToken attr =
        new AttrListAuthTokenV1(user, AuthSystemDef.KNB, EXPIRATION, s);
      Cookie c = new Cookie(COOKIE_NAME, attr.getTokenString());
      m.put(COOKIE_NAME, c);
      return m;
    }

    /**
     * Throws {@linkplain UnsupportedOperationException}.
     */
    //@Override
    public List<String> getRequestHeader(String header) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws {@linkplain UnsupportedOperationException}.
     */
    //@Override
    public List<Locale> getAcceptableLanguages() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return a list of acceptable media types as stored in the
     * acceptHeaders instance variable.
     */
    public List<MediaType> getAcceptableMediaTypes() {
        return acceptHeaders;
    }

    /**
     * Throws {@linkplain UnsupportedOperationException}.
     */
    //@Override
    public Locale getLanguage() {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws {@linkplain UnsupportedOperationException}.
     */
    //@Override
    public MediaType getMediaType() {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws {@linkplain UnsupportedOperationException}.
     */
    //@Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        return null;
    }
    
    
    /**
     * Sets the acceptHeaders value
     * 
     * @param  headers  a list of MediaType objects that specify
     *                  the MIME types that the client will accept
     *                  from the server
     */
    public void setAcceptHeaders(List<MediaType> headers) {
        this.acceptHeaders = headers;
    }

}
