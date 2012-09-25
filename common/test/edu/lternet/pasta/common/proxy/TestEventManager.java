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

package edu.lternet.pasta.common.proxy;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.UserCreds;

public class TestEventManager {

    private AuthToken token;
    
    @Before
    public void init() {
        token = UserCreds.getAuthToken();
    }
    
    @Test
    public void testUrlKey() {
        assertEquals("event.manager.url", EventManager.URL_KEY);
    }
    
    @Test
    public void testGetRootUrl() {
        String url = EventManager.getRootUrl();
        assertTrue(url.startsWith("http"));
        assertTrue(url.endsWith("/eventmanager/"));
    }
    
    @Test
    public void testEmlModified() {
        EventManager.emlModified(new EmlPackageId("test", 1, 0), token);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmlModifiedWithIncompletePackageId() {
        EventManager.emlModified(new EmlPackageId("test", 1, null), token);
    }
   
}
