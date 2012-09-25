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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.NewCookie;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.UserCreds;

public class TestIdentityManager {
    
    private String user;
    private String password;
    private AuthSystemDef knb;
    
    @Before
    public void init() {
        user = UserCreds.getUser();
        password = UserCreds.getPassword();
        knb = AuthSystemDef.KNB;
    }
    
    @Test
    public void testGetRootUrl() {
        String url = IdentityManager.getRootUrl();
        assertNotNull(url);
        assertTrue(url.startsWith("http"));
    }
    
    @Test
    public void testGetCookie() {
        NewCookie cookie = IdentityManager.getCookie(user, password, knb);
        assertNotNull(cookie);
        assertEquals("auth-token", cookie.getName());
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testGetCookieWithBadAuthSystem() {
        NewCookie cookie = IdentityManager.getCookie(user, password, null);
        assertNotNull(cookie);
        assertEquals("auth-token", cookie.getName());
    }
    
}
