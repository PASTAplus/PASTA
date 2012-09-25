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

package edu.lternet.pasta.common.validate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class TestComments {

    private Comments comments;
    private String info;
    private String warn;
    private String fatal;
    
    @Before 
    public void init() {
        comments = new Comments();
        info = "  info  ";
        warn = "  warn  ";
        fatal = "  fatal  ";
    }
    
    @Test
    public void testEmptyComments() {
        assertTrue(comments.asList().isEmpty());
    }

    @Test
    public void testInfo() {
        comments.info(info);
        assertEquals("INFO: " + info, comments.asList().get(0));
    }
    
    @Test
    public void testWarn() {
        comments.warn(warn);
        assertEquals("WARN: " + warn, comments.asList().get(0));
    }
    
    @Test
    public void testFatal() {
        comments.fatal(fatal);
        assertEquals("FATAL: " + fatal, comments.asList().get(0));
    }
    
    @Test
    public void testOrder() {
        comments.info(info);
        comments.fatal(fatal);
        comments.info(info);
        comments.warn(warn);
        comments.fatal(fatal);
        
        assertEquals("INFO: " + info,   comments.asList().get(0));
        assertEquals("FATAL: " + fatal, comments.asList().get(1));
        assertEquals("INFO: " + info,   comments.asList().get(2));
        assertEquals("WARN: " + warn,   comments.asList().get(3));
        assertEquals("FATAL: " + fatal, comments.asList().get(4));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInfoWithNullComment() {
        comments.info(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testWarnWithNullComment() {
        comments.warn(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testFatalWithNullComment() {
        comments.fatal(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInfoWithEmptyComment() {
        comments.info("");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testWarnWithEmptyComment() {
        comments.warn("");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testFatalWithEmptyComment() {
        comments.fatal("");
    }
    
    @Test
    public void testIfListIsLiveOrCopy() {
        comments.asList().add("blah blah");
        assertTrue(comments.asList().isEmpty());
    }
}
