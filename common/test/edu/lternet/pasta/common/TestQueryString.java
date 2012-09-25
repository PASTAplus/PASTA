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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.QueryString;

public class TestQueryString {

    private QueryString query;
    private Map<String, List<String>> queryParams;
    
    @Before
    public void init() {
        queryParams = new LinkedHashMap<String, List<String>>();
        
        List<String> list1 = Collections.emptyList();
        List<String> list2 = new LinkedList<String>();
        list2.add("");
        list2.add(null);
        List<String> list3 = new LinkedList<String>();
        list3.add("value");
        List<String> list4 = new LinkedList<String>();
        list4.add("value1");
        list4.add("value2");
        
        queryParams.put("1", list1);
        queryParams.put("2", list1);
        queryParams.put("3", list3);
        queryParams.put("4", list4);
        
        query = new QueryString(queryParams);
    }
    
    public void checkBadRequest(WebApplicationException e) {
        assertEquals(400, e.getResponse().getStatus());
        assertNotNull(e.getResponse().getEntity());
    }

    @Test
    public void testConstructorWithNullKey() {
        List<String> value = Collections.singletonList("value");
        queryParams = Collections.singletonMap(null, value);
        try {
            new QueryString(queryParams);
        } catch (WebApplicationException e) {
            checkBadRequest(e);
            return;
        }
        throw new IllegalStateException();
    }

    @Test
    public void testConstructorWithEmptyKey() {
        List<String> value = Collections.singletonList("value");
        queryParams = Collections.singletonMap("", value);
        try {
            new QueryString(queryParams);
        } catch (WebApplicationException e) {
            checkBadRequest(e);
            return;
        }
        throw new IllegalStateException();
    }

    @Test
    public void testConstructorWithNullValueList() {
        queryParams = Collections.singletonMap("k", null);
        assertEquals(0, new QueryString(queryParams).getParams().get("k").size());
    }
    
    @Test
    public void testConstructorNullAndEmptyValueRemoval() {
        assertTrue(query.getParams().get("2").isEmpty());
    }
    
    @Test
    public void testGetParams() {
        Map<String, List<String>> p = query.getParams();
        assertEquals(queryParams.keySet(), p.keySet());
        assertEquals(queryParams.get("3"), p.get("3"));
        assertEquals(queryParams.get("4"), p.get("4"));
    }
    
    @Test
    public void testGetRequiredValue() {
        assertEquals("value", query.getRequiredValue("3"));
    }
    
    @Test
    public void testGetRequiredValueWithNonExistentKey() {
        try {
            query.getRequiredValue("non-existent key");
        } catch (WebApplicationException e) {
            checkBadRequest(e);
            return;
        }
        throw new IllegalStateException();
    }
    
    @Test
    public void testGetRequiredValueWithNonExistentValue() {
        try {
            query.getRequiredValue("1");
        } catch (WebApplicationException e) {
            checkBadRequest(e);
            return;
        }
        throw new IllegalStateException();
    }
    
    @Test
    public void testGetRequiredValueWithMultipleValues() {
        try {
            query.getRequiredValue("4");
        } catch (WebApplicationException e) {
            checkBadRequest(e);
            return;
        }
        throw new IllegalStateException();
    }
    
    @Test
    public void testGetRequiredValues() {
        assertEquals(Collections.singletonList("value"), 
                     query.getRequiredValues("3"));
        assertEquals(queryParams.get("4"), 
                     query.getRequiredValues("4"));
    }
    
    @Test
    public void testGetRequiredValuesWithNonExistentKey() {
        try {
            query.getRequiredValues("non-existent key");
        } catch (WebApplicationException e) {
            checkBadRequest(e);
            return;
        }
        throw new IllegalStateException();
    }
    
    @Test
    public void testGetRequiredValuesWithNonExistentValues() {
        try {
            query.getRequiredValues("1");
        } catch (WebApplicationException e) {
            checkBadRequest(e);
            return;
        }
        throw new IllegalStateException();
    }

    @Test
    public void testCheckForIllegalKeys() {
        query.checkForIllegalKeys(queryParams.keySet());
    }
    
    @Test
    public void testCheckForIllegalKeysForExeption() {
        Set<String> validKeys = new TreeSet<String>();
        validKeys.add("1");
        validKeys.add("2");
        try {
            query.checkForIllegalKeys(validKeys);
        } catch (WebApplicationException e) {
            checkBadRequest(e);
            return;
        }
        throw new IllegalStateException();
    }
    
    @Test
    public void testToString() {
        
        String s = query.toString();

        assertTrue(s.charAt(0) == '?');
        assertTrue(s.contains("1"));
        assertFalse(s.contains("1="));
        assertTrue(s.contains("2"));
        assertFalse(s.contains("2="));
        assertTrue(s.contains("3=value"));
        assertTrue(s.contains("4=value1"));
        assertTrue(s.contains("4=value2"));
        assertTrue(s.split("&").length == 5);
    }
}
