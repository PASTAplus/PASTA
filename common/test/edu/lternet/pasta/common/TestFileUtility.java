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

import org.junit.Test;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.ResourceNotFoundException;

public class TestFileUtility {

    public static final String HOME = "test/edu/lternet/pasta/common/META-INF";
    
    public static String makeFileName(String fileName) {
        return new File(HOME, fileName).getPath();
    }
    
    private String fileName = "test_file.txt";
    private String fullName = makeFileName(fileName);
    
    @Test
    public void testAssertExistsWithExistingFile() {
        File f1 = new File(fullName);
        File f2 = FileUtility.assertExists(f1);
        assertFalse(f1.isAbsolute());
        assertTrue(f2.isAbsolute());
        assertTrue(f2.getPath().endsWith(fileName));
        assertEquals(f1.getAbsoluteFile(), f2);
    }
    
    @Test
    public void testAssertExistsWithExistingFileName() {
        File f1 = FileUtility.assertExists(fullName);
        assertTrue(f1.isAbsolute());
        assertTrue(f1.getPath().endsWith(fileName));
    }
    
    @Test(expected=ResourceNotFoundException.class)
    public void testAssertExistWithNonexistentFileV1() {
        FileUtility.assertExists("non-existent_file.txt");
    }
    
    @Test(expected=ResourceNotFoundException.class)
    public void testAssertExistWithNonexistentFileV2() {
        FileUtility.assertExists(new File("non-existent_file.txt"));
    }
    
    @Test
    public void testAssertCanReadWithReadableFile() {
        File f1 = new File(fullName);
        File f2 = FileUtility.assertCanRead(f1);
        assertFalse(f1.isAbsolute());
        assertTrue(f2.isAbsolute());
        assertTrue(f2.getPath().endsWith(fileName));
        assertEquals(f1.getAbsoluteFile(), f2);
    }
    
    @Test
    public void testAssertCanReadWithReadableFileName() {
        File f1 = FileUtility.assertCanRead(fullName);
        assertTrue(f1.isAbsolute());
        assertTrue(f1.getPath().endsWith(fileName));
    }
    
    // Eclipse freaks out with non-readable files, but these worked
    
    /*  
    @Test(expected=UnauthorizedException.class)
    public void testAssertCanReadWithNonReadableFileV1() {
        FileUtility.assertCanRead(makeFileName("non-readable_file.txt"));
    }
    
    @Test(expected=UnauthorizedException.class)
    public void testAssertCanReadWithNonReadableFileV2() {
        FileUtility.assertCanRead(new File(makeFileName("non-readable_file.txt")));
    }
    */
    
    @Test
    public void testFileToString() {
        String content1 = FileUtility.fileToString(fullName);
        String content2 = FileUtility.fileToString(new File(fullName));
        assertEquals("test content", content1);
        assertEquals("test content", content2);
    }
}
