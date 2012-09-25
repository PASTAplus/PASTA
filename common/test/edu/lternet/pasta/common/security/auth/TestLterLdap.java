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

package edu.lternet.pasta.common.security.auth;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestLterLdap {

    private LterLdap ldap;
    private String user;
    private String password;
    
    @Before
    public void init() {
        ldap = new LterLdap();
        user = UserCreds.getUser(UserCreds.LTER);
        password = UserCreds.getPassword(UserCreds.LTER);
    }
    
    @Test 
    public void testGetServer() {
        assertEquals("ldap.lternet.edu", ldap.getServer());
    }
    
    @Test 
    public void testGetPort() {
        assertEquals(636, ldap.getPort());
    }
    
    @Test
    public void testWithGoodCredentials() {
        assertTrue(ldap.authenticate(user, password));
        assertTrue(ldap.authenticate(user.toLowerCase(), password));
        assertTrue(ldap.authenticate(user.toUpperCase(), password));
    }

    @Test
    public void testWithBadDistinguishedName() {
        assertFalse(ldap.authenticate("invalid", "invalid"));
    }
    
    @Test
    public void testWithNonexistentUser() {
        user = "uid=null,o=LTER,dc=ecoinformatics,dc=org";
        assertFalse(ldap.authenticate(user, "invalid"));
    }

    @Test
    public void testWithGoodUserBadPassword() {
        assertFalse(ldap.authenticate(user, "invalid"));
    }
    
}
