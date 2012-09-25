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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.EmlUtility;
import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.WorkingDirectory;
import edu.lternet.pasta.common.validate.Validator.ValidatorResults;
import eml.ecoinformatics_org.access_2_1.AccessRule;
import eml.ecoinformatics_org.access_2_1.AccessType;

public class TestAccessTypeValidator {

    private static AccessType readFile(String fileNamePrefix) {

        String dir = "test/edu/lternet/pasta/common/security/access/v1"; 
        String fileName = fileNamePrefix + ".xml";
        File file = new File(dir, fileName);
        String accessString = FileUtility.fileToString(file);
        
        return EmlUtility.getAccessType2_1_0(accessString);
    }
    
    private static String toString(AccessType accessType) {
        return EmlUtility.toString(accessType);
    }
    
    private static void printResults(ValidatorResults<AccessType> results) {
        System.out.println("valid: " + results.isValid());
        printComments(results, "");
        System.out.println("\n"+toString(results.getEntity()));
        System.out.println(toString(results.getCanonicalEntity()));
    }
    
    private static void printComments(ValidatorResults<?> results, 
                                      String prefix) {

        for (ValidatorResults<?> sub : results.getSubResults()) {
            printComments(sub, prefix + "  ");
        }
        for (String s : results.getComments()) {
            System.out.println(prefix + s);
        }
        
    }
    
    private AccessTypeValidator validator;
    private ValidatorResults<AccessType> results;
    
    @Before
    public void init() {
        validator = new AccessTypeValidator();
        WorkingDirectory.setWorkingDirectory(new File("conf"));
    }

    @Test
    public void testAccessElementWithZeroFlaws() {

        AccessType accessType = readFile("zero-flaws");
        
        results = validator.validate(accessType);
        
        //printResults(results);
        
        assertTrue(results.isValid());
        assertTrue(results.getComments().isEmpty());
        assertFalse(results.getSubResults().isEmpty());
        assertTrue(results.hasCanonicalEntity());
        assertSame(accessType, results.getEntity());
        assertSame(accessType, results.getCanonicalEntity());
        
        assertEquals("ldap://ldap.lternet.edu", accessType.getAuthSystem());
        assertEquals("allowFirst", accessType.getOrder());
        
        List<JAXBElement<AccessRule>> rules = accessType.getAllowOrDeny();
        
        assertEquals(2, rules.size());
        assertEquals("allow", rules.get(0).getName().getLocalPart());
        assertEquals("allow", rules.get(1).getName().getLocalPart());

        AccessRule rule0 = rules.get(0).getValue();
        
        assertEquals(1, rule0.getPrincipal().size());
        String p = "uid=ucarroll,o=lter,dc=ecoinformatics,dc=org";
        assertEquals(p, rule0.getPrincipal().get(0));
        
        assertEquals(3, rule0.getPermission().size());
        assertTrue(rule0.getPermission().contains("read"));
        assertTrue(rule0.getPermission().contains("write"));
        assertTrue(rule0.getPermission().contains("changePermission"));
        
        AccessRule rule1 = rules.get(1).getValue();
        
        assertEquals(1, rule1.getPrincipal().size());
        assertTrue(rule1.getPrincipal().contains("public"));
        
        assertEquals(1, rule1.getPermission().size());
        assertTrue(rule1.getPermission().contains("read"));
    }

}
