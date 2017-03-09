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

package edu.lternet.pasta.common.security.access.v1;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.validate.Validator.ValidatorResults;

public class TestAuthSystemValidator {

    @Test
    public void testCanonicalString() {
        assertEquals("ldap://ldap.lternet.edu", AuthSystemValidator.CANONICAL);
    }
    
    @Test
    public void testNonCanonicalStrings() {
        
        Set<String> set = AuthSystemValidator.NON_CANONICAL;
        
        assertEquals(12, set.size());
        assertTrue(set.contains("KNB"));
        assertTrue(set.contains("knb"));
        assertTrue(set.contains("PASTA"));
        assertTrue(set.contains("pasta"));
        assertTrue(set.contains("LTER"));
        assertTrue(set.contains("lter"));
        assertTrue(set.contains("ldap.lternet.edu"));
        assertTrue(set.contains("ldaps://ldap.lternet.edu"));
        assertTrue(set.contains("ldaps://ldap.lternet.edu:636"));
    }

    private AuthSystemValidator validator;
    
    @Before
    public void init() {
        validator = new AuthSystemValidator();
    }
    
    @Test
    public void testCanonicalizes() {
        assertTrue(validator.canonicalizes());
    }
    
    @Test
    public void testValidateWithCanonicalString() {
        
        String authSystem = "ldap://ldap.lternet.edu";
        ValidatorResults<String> results = validator.validate(authSystem);
        
        assertTrue(results.isValid());
        assertTrue(results.getSubResults().isEmpty());
        assertSame(authSystem, results.getEntity());
        assertEquals(authSystem, results.getCanonicalEntity());
        assertTrue(results.getComments().isEmpty());
    }
    
    @Test
    public void testValidateWithNonCanonicalStrings() {
        
        for (String authSystem : AuthSystemValidator.NON_CANONICAL) {
            
            ValidatorResults<String> results = validator.validate(authSystem);

            assertTrue(results.isValid());
            assertTrue(results.getSubResults().isEmpty());
            assertSame(authSystem, results.getEntity());
            assertEquals("ldap://ldap.lternet.edu", results.getCanonicalEntity());
            assertEquals(1, results.getComments().size());
        }
    }
    
    @Test
    public void testValidateWithInvalidString() {
        
        String authSystem = "blah blah blah";
        ValidatorResults<String> results = validator.validate(authSystem);
        
        assertFalse(results.isValid());
        assertTrue(results.getSubResults().isEmpty());
        assertSame(authSystem, results.getEntity());
        assertEquals("ldap://ldap.lternet.edu", results.getCanonicalEntity());
        assertEquals(1, results.getComments().size());
    }
}
