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

package edu.lternet.pasta.common.security.token;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.common.security.token.BasicAuthToken;

import static org.junit.Assert.*;

public class TestAuthTokenFactory {

    private String user;
    private String password;
    private String tokenString;
    private HttpHeaders headers;
    
    @Before
    public void init() {
        user = "user";
        password = "password";
        headers = new DummyBasicHttpHeaders(user, password);
        tokenString = BasicAuthToken.makeTokenString(user, password);
    }
    
    @Test
    public void testMakeAuthTokenWithStringReturnType() {
        Object obj = AuthTokenFactory.makeAuthToken(tokenString);
        assertTrue(obj instanceof BasicAuthToken);
    }
    
    @Test
    public void testMakeAuthTokenWithHeadersReturnType() {
        Object obj = AuthTokenFactory.makeAuthToken(headers);
        assertTrue(obj instanceof BasicAuthToken);
    }
    
    @Test
    public void testMakeAuthTokenWithPasswordWithStringReturnType() {
        Object obj = AuthTokenFactory.makeAuthTokenWithPassword(tokenString);
        assertTrue(obj instanceof BasicAuthToken);
    }
    
    @Test
    public void testMakeAuthTokenWithPasswordWithHeadersReturnType() {
        Object obj = AuthTokenFactory.makeAuthTokenWithPassword(headers);
        assertTrue(obj instanceof BasicAuthToken);
    }
    
    @Test(expected=WebApplicationException.class)
    public void testMakeAuthTokenWithBadString() {
        AuthTokenFactory.makeAuthToken("bad token");
    }
    
    @Test(expected=WebApplicationException.class)
    public void testMakeAuthTokenWithPasswordWithBadString() {
        AuthTokenFactory.makeAuthTokenWithPassword("bad token");
    }
    
    @Test(expected=WebApplicationException.class)
    public void testMakeAuthTokenWithoutTokenHeader() {
        headers = new DummyBasicHttpHeaders(null, null);
        AuthTokenFactory.makeAuthToken(headers);
    }
    
    @Test(expected=WebApplicationException.class)
    public void testMakeAuthTokenWithPasswordWithoutTokenHeader() {
        headers = new DummyBasicHttpHeaders(null, null);
        AuthTokenFactory.makeAuthTokenWithPassword(headers);
    }
    
    // Not testing with headers that contain an invalid token
    
    @Test
    public void testGetTokenString() {
        String s = AuthTokenFactory.getTokenString(headers);
        assertEquals(tokenString, s);
    }
    
    @Test(expected=WebApplicationException.class)
    public void testGetTokenStringWithoutTokenHeader() {
        headers = new DummyBasicHttpHeaders(null, null);
        AuthTokenFactory.getTokenString(headers);
    }
    
    // Not testing addToken methods because WebResource.Builders don't have
    // accessor methods.
}
