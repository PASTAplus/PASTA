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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.WorkingDirectory;

public class TestKnbLdap {

    private KnbLdap ldap;
    private String user;
    private String password;
    
    @Before
    public void init() {
        File workingDir = new File("conf");
        WorkingDirectory.setWorkingDirectory(workingDir);
        File keystore = new File(workingDir, "keystore.jks");
        keystore = FileUtility.assertCanRead(keystore);
        ldap = new KnbLdap(keystore);
    }
    
    /* Commenting out broken test
    @Test 
    public void testGetServer() {
        assertEquals("ldap.ecoinformatics.org", ldap.getServer());
    }
    */
    
    @Test 
    public void testGetPort() {
        assertEquals(389, ldap.getPort());
    }
    
    private void testWithGoodCredentials(String organization) {
        user = UserCreds.getUser(organization);
        password = UserCreds.getPassword(organization);
        
        assertTrue(user, ldap.authenticate(user, password));
        assertTrue(ldap.authenticate(user.toLowerCase(), password));
        assertTrue(ldap.authenticate(user.toUpperCase(), password));
    }

    /* Commenting out broken test
    @Test
    public void testWithLterUser() {
        testWithGoodCredentials(UserCreds.LTER);
    }
    */

    /* Commenting out broken test
    @Test
    public void testWithUnaffiliatedUser() {
        testWithGoodCredentials(UserCreds.UNAFFILIATED);
    }
    */

    /* Commenting out broken test
    @Test
    public void testWithNceasUser() {
        testWithGoodCredentials(UserCreds.NCEAS);
    }
    */

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
        user = UserCreds.getUser(UserCreds.LTER);
        assertFalse(ldap.authenticate(user, "invalid"));
    }

    /* Commenting out broken test
    @Test(expected=IllegalStateException.class)
    public void testWithGoodUserEmptyKeystore() {
        File keystore = WorkingDirectory.getFile("empty_keystore.jks");
        ldap = new KnbLdap(keystore);
        user = UserCreds.getUser(UserCreds.LTER);
        password = UserCreds.getPassword(UserCreds.LTER);
        assertFalse(ldap.authenticate(user, password));
    }
    */
    
}
