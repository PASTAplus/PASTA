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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.BasicAuthToken;

public class TestAccessControllerV1 {

    private final static String PASTA = 
        "uid=pasta,o=lter,dc=ecoinformatics,dc=org";
    
    private final static AuthToken PASTA_TOKEN = makeToken(PASTA);
    
    private static String readFile(String fileNamePrefix) {

        String dir = "test/edu/lternet/pasta/common/security/access/v1"; 
        String fileName = fileNamePrefix + ".xml";
        File file = new File(dir, fileName);

        return FileUtility.fileToString(file);
    }
    
    private static AuthToken makeToken(String userId) {
        return new BasicAuthToken(userId, "password");
    }
    
    private final AccessControllerV1 controller = new AccessControllerV1();
    
    
    
    @Test
    public void testIfUserPastaHasAllPermissions() {
        
        String acr = readFile("zero-flaws");
        String submitter = "anyone, it doesn't matter";
        
        assertTrue(controller.canRead(PASTA_TOKEN, acr, submitter));
        assertTrue(controller.canWrite(PASTA_TOKEN, acr, submitter));
        assertTrue(controller.canChangePermission(PASTA_TOKEN, acr, submitter));
        assertTrue(controller.canAll(PASTA_TOKEN, acr, submitter));
    }
    
    @Test
    public void testIfSubmitterHasAllPermissions() {
        
        String acr = readFile("zero-flaws");
        String submitter = "anyone, it doesn't matter";
        AuthToken token = makeToken(submitter);
        
        assertTrue(controller.canRead(token, acr, submitter));
        assertTrue(controller.canWrite(token, acr, submitter));
        assertTrue(controller.canChangePermission(token, acr, submitter));
        assertTrue(controller.canAll(token, acr, submitter));
    }

    @Test
    public void testZeroFlawsAcr() {
        
        String acr = readFile("zero-flaws");
        String submitter = "anyone, it doesn't matter";
        AuthToken token = makeToken("public");
        
        assertTrue(controller.canRead(token, acr, submitter));
        assertFalse(controller.canWrite(token, acr, submitter));
        assertFalse(controller.canChangePermission(token, acr, submitter));
        assertFalse(controller.canAll(token, acr, submitter));
        
        token = makeToken("someone else");
        
        assertTrue(controller.canRead(token, acr, submitter));
        assertFalse(controller.canWrite(token, acr, submitter));
        assertFalse(controller.canChangePermission(token, acr, submitter));
        assertFalse(controller.canAll(token, acr, submitter));
        
        token = makeToken("uid=ucarroll,o=lter,dc=ecoinformatics,dc=org");
        
        assertTrue(controller.canRead(token, acr, submitter));
        assertTrue(controller.canWrite(token, acr, submitter));
        assertTrue(controller.canChangePermission(token, acr, submitter));
        assertTrue(controller.canAll(token, acr, submitter));
    }
}
