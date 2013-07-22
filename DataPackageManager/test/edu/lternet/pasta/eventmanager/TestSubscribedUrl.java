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

package edu.lternet.pasta.eventmanager;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.junit.Test;

import edu.lternet.pasta.eventmanager.SubscribedUrl;

public class TestSubscribedUrl {

    @Test
    public void testToString() {
        String test = "http:stuff";
        assertEquals(test, new SubscribedUrl(test).toString());
    }
    
    @Test
    public void testToStringWithUnicodeCharacter() 
                            throws UnsupportedEncodingException {
        String input = "http:\u00F6stuff";
        String output = new SubscribedUrl(input).toString();
        assertFalse(input.equals(output));
        assertEquals(URI.create(input).toASCIIString(), output);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullUrl() {
        new SubscribedUrl(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyUrl() {
        new SubscribedUrl("");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidSyntaxUrl1() {
        new SubscribedUrl("invalid syntax because of spaces");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidSyntaxUrl2() {
        new SubscribedUrl(":invalid_syntax_because_of_colon");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUrlWithoutScheme() {
        new SubscribedUrl("no-scheme");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUrlWithInvalidScheme() {
        new SubscribedUrl("invalid-scheme:stuff");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUrlWithoutSchemeSpecificPart() {
        new SubscribedUrl("http:");
    }
    
    @Test
    public void testEquals() {
        SubscribedUrl s1 = new SubscribedUrl("http://1");
        SubscribedUrl s2 = new SubscribedUrl("http://1");
        SubscribedUrl s3 = new SubscribedUrl("http://3");
        assertTrue(s1.equals(s1));
        assertTrue(s1.equals(s2));
        assertFalse(s1.equals(s3));
    }
}
