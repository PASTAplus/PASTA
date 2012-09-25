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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.lternet.pasta.common.security.access.v1.PermissionStringValidator;
import edu.lternet.pasta.common.validate.Validator.ValidatorResults;

@RunWith(value = Parameterized.class)
public class TestPermissionStringValidator {

    private static final String ALL = PermissionStringValidator.ALL;
    private static final String READ = PermissionStringValidator.READ;
    private static final String WRITE = PermissionStringValidator.WRITE;
    private static final String CHANGE_PERMISSION = 
        PermissionStringValidator.CHANGE_PERMISSION;
    
    
    @Parameters
    public static Collection<?> data() {

        // permission, valid, number of comments, canonical
        return Arrays.asList(new Object[][] {
                { READ,                 true,  0, READ },
                { WRITE,                true,  0, WRITE },
                { CHANGE_PERMISSION,    true,  0, CHANGE_PERMISSION },
                { ALL,                  true,  0, ALL },
                { "read",               true,  0, READ},
                { "write",              true,  0, WRITE },
                { "changePermission",   true,  0, CHANGE_PERMISSION },
                { "all",                true,  0, ALL },
                { " read ",             true,  2, READ },
                { " write ",            true,  2, WRITE },
                { " changePermission ", true,  2, CHANGE_PERMISSION },    
                { " all ",              true,  2, ALL },
                { "READ",               true,  2, READ },
                { "WRITE",              true,  2, WRITE },
                { "CHANGEPERMISSION",   true,  2, CHANGE_PERMISSION },
                { "ALL",                true,  2, ALL },
                { "blah",               false, 1, null }
        });

    }
    
    private final String permission;
    private final boolean valid;
    private final int comments;
    private final String canonical;
    
    public TestPermissionStringValidator(String permission, 
                                         boolean valid, 
                                         int comments,
                                         String canonical) {
        this.permission = permission;
        this.valid = valid;
        this.comments = comments;
        this.canonical = canonical;
    }
    
    @Test
    public void test() {

        PermissionStringValidator validator = new PermissionStringValidator();
        assertTrue(validator.canonicalizes());
        
        ValidatorResults<String> results = validator.validate(permission);

        assertEquals(valid, results.isValid());
        assertEquals(comments, results.getComments().size());
        
        if (canonical == null) {
            testWithoutCanonical(results);
        } else {
            assertTrue(results.hasCanonicalEntity());
            assertEquals(canonical, results.getCanonicalEntity());
            assertSame(canonical, results.getCanonicalEntity());
        }
    }
    
    private void testWithoutCanonical(ValidatorResults<String> results) {
        
        assertFalse(results.hasCanonicalEntity());
        try {
            results.getCanonicalEntity();
        } catch (IllegalStateException e) {
            return;
        }
        fail("IllegalStateException not thrown");
    }
    
}
