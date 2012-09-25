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

package edu.lternet.pasta.common;

import static org.junit.Assert.*;

import java.io.File;

import javax.ws.rs.core.Response;

import org.junit.Test;

public class TestPastaWebService {

    @Test
    public void testServeFileFromDirectory() {
        
        File dir = new File(".");
        String fileName = "build.xml";
        
        File file = new File(dir, fileName);
        FileUtility.assertCanRead(file);
        
        Response r = PastaWebService.serveFileFromDirectory(dir, fileName);
        
        String entity = (String) r.getEntity();
        
        assertEquals(200, r.getStatus());
        assertEquals(entity, FileUtility.fileToString(file));
    }

    @Test
    public void testServeFileFromDirectoryWithRelativePathUp() {
        
        File dir = new File("./src");
        String fileName = "../build.xml";
        
        File file = new File(dir, fileName);
        FileUtility.assertCanRead(file);
        
        Response r = PastaWebService.serveFileFromDirectory(dir, fileName);
        
        String entity = (String) r.getEntity();
        
        assertEquals(404, r.getStatus());
        assertFalse(FileUtility.fileToString(file).equals(entity));
    }
}
