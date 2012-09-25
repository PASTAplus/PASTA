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

import static org.junit.Assert.*;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.core.util.Base64;

import edu.lternet.pasta.common.security.token.BasicAuthToken;

public class TestBasicAuthToken {

    private String user;
    private String password;
    private String basic;
    private String pair;
    private String encoded;
    private String header;
    private BasicAuthToken token;
    
    @Before
    public void init() {
        user = "junit";
        password = "none";
        basic = "Basic       \t";
        pair = user + ":" + password;
        encoded = new String(Base64.encode(pair));
        header = "     \t" + basic + encoded + "  \t     ";
        token = new BasicAuthToken(header);
    }
    
    @Test
    public void testMakeTokenString() {
        String s = BasicAuthToken.makeTokenString(user, password);
        token = new BasicAuthToken(s);
        assertEquals(user, token.getUserId());
        assertEquals(password, token.getPassword());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMakeTokenWithNullUser() {
        BasicAuthToken.makeTokenString(null, password);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMakeTokenWithNullPassword() {
        BasicAuthToken.makeTokenString(user, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMakeTokenWithEmptyUser() {
        BasicAuthToken.makeTokenString("", password);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMakeTokenWithEmptyPassword() {
        BasicAuthToken.makeTokenString(user, "");
    }
    
    @Test
    public void testGetTokenString() {
        assertEquals(header, token.getTokenString());
    }
    
    @Test
    public void testGetUserId() {
        assertEquals(user, token.getUserId());
    }
    
    @Test
    public void testGetPassword() {
        assertEquals(password, token.getPassword());
    }
    
    @Test(expected=WebApplicationException.class)
    public void testConstructorWithoutBasic() {
        new BasicAuthToken(encoded);
    }
    
    @Test(expected=WebApplicationException.class)
    public void testConstructorWithoutPair() {
        new BasicAuthToken(basic);
    }
    
    @Test(expected=WebApplicationException.class)
    public void testConstructorWithNonBase64EncodedPair() {
        new BasicAuthToken(basic + pair);
    }
    
    @Test(expected=WebApplicationException.class)
    public void testConstructorWithTripeInsteadOfPair() {
        new BasicAuthToken(basic + new String(Base64.encode(pair + ":3rd")));
    }
    
}
