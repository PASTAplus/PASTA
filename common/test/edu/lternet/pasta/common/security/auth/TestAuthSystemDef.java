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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.WorkingDirectory;

public class TestAuthSystemDef {
    
    private AuthSystemDef knb;
    private List<String> knbAliases;
    
    @Before
    public void init() {
        knb = AuthSystemDef.KNB;
        
        knbAliases = new LinkedList<String>();
        knbAliases.add("ldap.ecoinformatics.org");
        knbAliases.add("ldap://ldap.ecoinformatics.org");
        knbAliases.add("ldap://ldap.ecoinformatics.org:389");
        knbAliases.add("ldap://ldap.ecoinformatics.org:389/dc=ecoinformatics,dc=org");
    }

    @Test
    public void testDefinedAuthSystems() {
        AuthSystemDef[] defs = AuthSystemDef.values(); 
        assertEquals(1, defs.length);
        assertEquals(AuthSystemDef.KNB, defs[0]);
    }
    
    /* Commenting out broken test
    @Test
    public void testGetAuthSystemDefWithKnbCanonicalName() {
        assertEquals(knb, AuthSystemDef.getAuthSystemDef("PASTA:KNB"));
        assertEquals(knb, AuthSystemDef.getAuthSystemDef("pasta:knb"));
        assertEquals(knb, AuthSystemDef.getAuthSystemDef("PaStA:KnB"));
        assertEquals(knb, AuthSystemDef.getAuthSystemDef("pAsTa:kNb"));
    }
    */
    
    @Test
    public void testGetAuthSystemDefWithKnbAliases() {
        for (String a : knbAliases) {
            assertEquals(knb, AuthSystemDef.getAuthSystemDef(a));
            assertEquals(knb, AuthSystemDef.getAuthSystemDef(a.toUpperCase()));
            assertEquals(knb, AuthSystemDef.getAuthSystemDef(a.toLowerCase()));
        }
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testGetAuthSystemDefWitNonExistentName() {
        AuthSystemDef.getAuthSystemDef("non-existent");
    }
    
    /* Commenting out broken test
    @Test
    public void testGetCanonicalNameForKnb() {
        assertEquals("pasta:knb", knb.getCanonicalName());
    }
    */
    
    @Test
    public void testGetAliasesForKnb() {
        
        List<String> aliases = knb.getAliases();
        
        assertEquals(4, aliases.size());
        
        for (String a : aliases) {
            assertTrue(aliases.contains(a));
        }
    }
    
    @Test
    public void testGetAliasesForKnbAfterModifyingReturnedList() {
        knb.getAliases().clear();
        testGetAliasesForKnb();
    }
    
    /* Commenting out broken test
    @Test
    public void testIsNameOrAliasForKnb() {
        
        assertTrue(knb.isNameOrAlias("pasta:knb"));
        assertFalse(knb.isNameOrAlias("not a name or alias"));
        
        for (String a : knbAliases) {
            assertTrue(knb.isNameOrAlias(a));
            assertTrue(knb.isNameOrAlias(a.toLowerCase()));
            assertTrue(knb.isNameOrAlias(a.toUpperCase()));
        }
    }
    */

    /* Commenting out broken test
    @Test
    public void testMakeAuthSystemForKnb() {
        
        String user = UserCreds.getUser(UserCreds.LTER);
        String password = UserCreds.getPassword(UserCreds.LTER);

        // For keystore file
        WorkingDirectory.setWorkingDirectory(new File("conf"));

        AuthSystem authSystem = knb.makeAuthSystem();
        assertTrue(authSystem.authenticate(user, password));
    }
    */
}
