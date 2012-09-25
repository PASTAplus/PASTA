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

import edu.lternet.pasta.common.ResourceNotFoundException;

public class TestKnbAuthSystem {

    private KnbAuthSystem knb;
    private String user;
    private String password;
   
    @Before
    public void init() {
        knb = new KnbAuthSystem(new File("conf/keystore.jks"));
        user = UserCreds.getUser(UserCreds.LTER);
        password = UserCreds.getPassword(UserCreds.LTER);
    }
    
    @Test(expected=NullPointerException.class)
    public void testConstructorWithNullFile() {
        new KnbAuthSystem(null);
    }
    
    @Test(expected=ResourceNotFoundException.class)
    public void testConstructorWithNonExistentFile() {
        new KnbAuthSystem(new File("non-existent"));
    }
    
    @Test
    public void testGetAuthSystemDef() {
        assertEquals(AuthSystemDef.KNB, knb.getAuthSystemDef());
    }

    private List<String> makeDnVariations(String user) {

        List<String> variations = new LinkedList<String>();
        variations.add(user);
        
        String variation = stripDnOfSpaces(user);
        variations.add(variation);
        variations.add(variation.toLowerCase());
        variations.add(variation.toUpperCase());
        
        variation = padDnWithSpaces(user);
        variations.add(variation);
        variations.add(variation.toLowerCase());
        variations.add(variation.toUpperCase());
        
        variation = replaceCommaWithSemicolon(user);
        variations.add(variation);
        variations.add(variation.toLowerCase());
        variations.add(variation.toUpperCase());

        variation = surroundValuesWithQuotes(user);
        variations.add(variation);
        variations.add(variation.toLowerCase());
        variations.add(variation.toUpperCase());
        
        return variations;
    }
    
    private String surroundValuesWithQuotes(String user) {
        user = stripDnOfSpaces(user);
        user = user.replace("=", "=\"");
        user = user.replace(",", "\",");
        return user + "\"";
    }
    
    private String replaceCommaWithSemicolon(String dn) {
        return dn.replace(",", ";");
    }
    
    private String stripDnOfSpaces(String dn) {
        return dn.replace(" ", "");
    }
    
    private String padDnWithSpaces(String dn) {
        dn = dn.replace("=", " = ");
        dn = dn.replace(",", " , ");
        return "  " + dn + "  ";
    }
    
    @Test
    public void testAuthenticate() {
        for (String variation : makeDnVariations(user)) {
            //System.out.println(variation);
            assertTrue(knb.authenticate(variation, password));
        }
    }
    
    @Test
    public void testGetGroups() {
        assertTrue(knb.getGroups(user).isEmpty());
    }
    
    @Test
    public void testSameUserWithIdenticalStrings() {
        assertTrue(knb.sameUser(user, user));
    }
    
    @Test
    public void testSamePrincipalWithIdenticalStrings() {
        assertTrue(knb.samePrincipal(user, user));
    }
    
    @Test
    public void testSameUserWithEquivalentStrings() {
        for (String variation : makeDnVariations(user)) {
            assertTrue(knb.sameUser(user, variation));
        }
    }
    
    @Test
    public void testSamePrincipalWithEquivalentStrings() {
        for (String variation : makeDnVariations(user)) {
            assertTrue(knb.samePrincipal(user, variation));
        }
    }
    
    @Test
    public void testSameUserWithDifferentUsers() {
        String diffUser = UserCreds.getUser(UserCreds.NCEAS);
        assertFalse(knb.sameUser(user, diffUser));
    }
    
    @Test
    public void testSamePrincipalWithDifferentUsers() {
        String diffUser = UserCreds.getUser(UserCreds.NCEAS);
        assertFalse(knb.samePrincipal(user, diffUser));
    }
    
    @Test
    public void testSamePrincipalsWithNonValidDNs() {
        assertTrue(knb.samePrincipal("same", "same"));
        assertFalse(knb.samePrincipal("same", "diff"));
        assertFalse(knb.samePrincipal("diff", "same"));
    }
}
