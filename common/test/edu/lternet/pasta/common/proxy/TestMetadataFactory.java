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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.security.token.AuthTokenWithPassword;
import edu.lternet.pasta.common.security.token.UserCreds;

public class TestMetadataFactory {

    private EmlPackageId packageId;
    private List<String> entityNames;
    
    @Before
    public void init() {
        packageId = new EmlPackageId("junit", 1, 0);
        entityNames = new LinkedList<String>();
        entityNames.add("name1");
        entityNames.add("name 2");
    }
    
    @Test
    public void testGetRootUrl() {
        String url = MetadataFactory.getRootUrl();
        assertNotNull(url);
        assertTrue(url.startsWith("http"));
    }
    
    @Test
    public void testToQueryString() {
        String query = MetadataFactory.toQueryString(packageId, entityNames);
        assertEquals("junit.1.0=name1&junit.1.0=name%202", query);
    }
    
    @Test
    public void testToQueryStringWithoutEntityNames() {
        entityNames = Collections.emptyList();
        String query = MetadataFactory.toQueryString(packageId, entityNames);
        assertEquals("junit.1.0", query);
    }

    
    @Test
    public void testToQueryStringWithMap() {
        
        Map<EmlPackageId, List<String>> m = 
            new LinkedHashMap<EmlPackageId, List<String>>();
        m.put(packageId, entityNames);
        m.put(new EmlPackageId("a", 1, 0), Collections.singletonList("b"));
        
        String query = MetadataFactory.toQueryString(m);
        assertEquals("junit.1.0=name1&junit.1.0=name%202&a.1.0=b", query);
    }
    
    @Test
    public void testToQueryStringWithMapWithoutEntityNames() {
        
        entityNames = Collections.emptyList();
        
        Map<EmlPackageId, List<String>> m = 
            new LinkedHashMap<EmlPackageId, List<String>>();
        m.put(packageId, entityNames);
        m.put(new EmlPackageId("a", 1, 0), entityNames);
        
        String query = MetadataFactory.toQueryString(m);
        assertEquals("junit.1.0&a.1.0", query);
    }
    
    @Test
    public void testMakeUrl() {

        entityNames = Collections.emptyList();
        
        Map<EmlPackageId, List<String>> m = 
            new LinkedHashMap<EmlPackageId, List<String>>();
        m.put(packageId, entityNames);
        m.put(new EmlPackageId("a", 1, 0), entityNames);
        
        String[] parts = MetadataFactory.makeUrl(m).split("[?]");
        
        assertEquals(2, parts.length);
        assertEquals(MetadataFactory.getRootUrl() + "eml/", parts[0]);
        assertEquals("junit.1.0&a.1.0", parts[1]);
    }
    
    @Test
    public void testAppendProvenance() {

        AuthTokenWithPassword token = UserCreds.getAuthToken();

        if (!MetadataCatalog.exists(packageId, token)) {
            String s = packageId + " does not exist in the Metadata Catalog.";
            fail(s);
        }

        String eml = "<eml><dataset><methods/></dataset></eml>";
        entityNames = Collections.singletonList("name1");
        
        Map<EmlPackageId, List<String>> pairs = 
            new LinkedHashMap<EmlPackageId, List<String>>();
        pairs.put(packageId, entityNames);
        
        String s = MetadataFactory.appendProvenance(eml, pairs, token);

        assertTrue(s.contains("<methods>"));
        assertTrue(s.contains("name1"));
        assertTrue(s.contains("junit/1/0"));
    }
}
