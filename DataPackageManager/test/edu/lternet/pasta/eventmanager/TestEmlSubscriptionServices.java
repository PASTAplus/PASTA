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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManagerResource;

public class TestEmlSubscriptionServices {

	/*
	 * Class variables
	 */
    
    public static final String META_INF_DIR = 
        "test/edu/lternet/pasta/eventmanager/META-INF";
    private static ConfigurationListener configurationListener = null;
    private static final String dirPath = "WebRoot/WEB-INF/conf";

    
    /*
     * Instance variables
     */
    
    private HttpHeaders headers;
    private DataPackageManagerResource dataPackageManagerResource;
    
    
    /*
     * Class methods
     */
  
    /**
     * Initialize objects before any tests are run.
     */
    @BeforeClass
    public static void setUpClass() {
      configurationListener = new ConfigurationListener();
      configurationListener.initialize(dirPath);
    }
    
    
    public static String makeFileName(String fileName) {
        return new File(META_INF_DIR, fileName).getAbsolutePath();
    }
    
    
    /*
     * Instance methods
     */
    
    @Before
    
    public void init() {
        clearDatabase();
        headers = new DummyCookieHttpHeaders("junit");
        dataPackageManagerResource = new DataPackageManagerResource();
    }


	private void clearDatabase() {
		try {
			SubscriptionRegistry subscriptionRegistry = new SubscriptionRegistry();
			subscriptionRegistry.deleteTestSubscriptions();
		}
		catch (Exception e) {
			System.err
					.println(String
							.format("Failed to clear the database of JUnit subscriptions prior to testing: %s",
									e.getMessage()));
		}
	}


    @Test
    public void testValidQueryKeys() {      
        Set<String> s = new TreeSet<String>();
        s.add("creator");
        s.add("scope");
        s.add("identifier");
        s.add("revision");
        s.add("url");
        assertEquals(s, DataPackageManagerResource.VALID_EVENT_QUERY_KEYS);
    }
    
    
    private String makeSubscriptionXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<subscription type=\"eml\">");
        sb.append("<packageId>test.1.2</packageId>");
        sb.append("<url>http://test</url>");
        sb.append("</subscription>");   
        return sb.toString();
    }
    
    
    private String make2ndSubscriptionXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<subscription type=\"eml\">");
        sb.append("<packageId>test</packageId>");
        sb.append("<url>http://test</url>");
        sb.append("</subscription>");
        return sb.toString();
    }
    
    
    private String makeSubscription() {
        String s1 = makeSubscriptionXml();
        return getSubscriptionId(dataPackageManagerResource.createSubscription(headers, s1));
    }
    
    
    private String make2ndSubscription() {
        String s1 = make2ndSubscriptionXml();
        return getSubscriptionId(dataPackageManagerResource.createSubscription(headers, s1));
    }
    
    
    private String getSubscriptionId(Response createResponse) {
    	MultivaluedMap<String, Object> mvMap = createResponse.getMetadata();
    	List<Object> objectList = mvMap.get("Location");
    	Object firstObject = objectList.get(0);
        URI uri = (URI) firstObject;
        String[] parts = uri.toString().split("/");
        return parts[parts.length - 1];
    }
    
    
    @Test
    public void testCreateSubscription() throws URISyntaxException {
        String s1 = makeSubscriptionXml();       
        Response r = dataPackageManagerResource.createSubscription(headers, s1);
        assertEquals(201, r.getStatus());
        List<Object> location = r.getMetadata().get("Location");
        assertNotNull(location);
        assertEquals(1, location.size());
        assertTrue(location.get(0) instanceof URI);
        Integer.parseInt(location.get(0).toString());
    }

    
    @Test
    public void testCreateSubscriptionTwice() {
        String s1 = makeSubscriptionXml();      
        Response r = dataPackageManagerResource.createSubscription(headers, s1);
        assertEquals(201, r.getStatus());       
        r = dataPackageManagerResource.createSubscription(headers, s1);
        assertEquals(409, r.getStatus());
    }

    
    @Test
    public void testCreateSubscriptionWithBadXml() throws URISyntaxException {
        Response r = dataPackageManagerResource.createSubscription(headers, "blah");
        assertEquals(400, r.getStatus());
    }
 
    
    private void assertContainsSubscription(String xml, String id) {   
        assertTrue(xml.contains("<subscription type=\"eml\">"));
        assertTrue(xml.contains("<id>" + id + "</id>"));
        assertTrue(xml.contains("<creator>junit</creator>"));
        assertTrue(xml.contains("<packageId>test.1.2</packageId>"));
        assertTrue(xml.contains("<url>http://test</url>"));
        
    }

    
    @Test
    public void testGetSubscriptionWithId() {   
        String id = makeSubscription();
        Response r = dataPackageManagerResource.getSubscriptionWithId(headers, id);
        assertEquals(200, r.getStatus());
        assertTrue(r.getEntity() instanceof String);
     
        String xml = (String) r.getEntity();
        assertContainsSubscription(xml, id);
        assertTrue(xml.contains("<subscriptions>"));
    }

    
    @Test
    public void testGetSubscriptionWithBadId() {
        Response r = dataPackageManagerResource.getSubscriptionWithId(headers, "bad");
        assertEquals(400, r.getStatus());
    }
    
    
    @Test
    public void testGetSubscriptionNeverExisted() {
        Response r = dataPackageManagerResource.getSubscriptionWithId(headers, "-1");
        assertEquals(404, r.getStatus());
        assertNotNull(r.getEntity());
        assertTrue(r.getEntity() instanceof String);
    }
    
    
    @Test
    public void testGetSubscriptionDeleted() {     
        String id = makeSubscription();      
        Response r = dataPackageManagerResource.deleteSubscription(headers, id);
        assertEquals(200, r.getStatus());      
        r = dataPackageManagerResource.getSubscriptionWithId(headers, id);
        assertEquals(410, r.getStatus());
        assertNotNull(r.getEntity());
        assertTrue(r.getEntity() instanceof String);
    }
    
    
    @Test
    public void testGetMatchingSubscriptionsWith2Matches() {        
        String id1 = makeSubscription();
        String id2 = make2ndSubscription();       
        Map<String, String> query = Collections.singletonMap("scope", "test");
        UriInfo uriInfo = new DummyUriInfo(query);
        Response r = dataPackageManagerResource.getMatchingSubscriptions(headers, uriInfo);
        
        String xml = (String) r.getEntity();      
        assertTrue(xml.contains("<subscriptions>"));
        assertContainsSubscription(xml, id1);
        assertContainsSubscription(xml, id2);
    }
    
    
    @Test
    public void testGetMatchingSubscriptionsWith1Match() {   
        String id1 = makeSubscription();
        make2ndSubscription();
        
        Map<String, String> query = new TreeMap<String, String>();
        query.put("scope", "test");
        query.put("identifier", "1");
        
        UriInfo uriInfo = new DummyUriInfo(query);
        Response r = dataPackageManagerResource.getMatchingSubscriptions(headers, uriInfo);
        
        String xml = (String) r.getEntity();
        
        assertTrue(xml.contains("<subscriptions>"));
        assertContainsSubscription(xml, id1);
        assertFalse(xml.contains("<packageId>test</packageId>"));
    }
    
    
    @Test
    public void testGetMatchingSubscriptionsWithoutMatches() {   
        Map<String, String> query = 
            Collections.singletonMap("creator", "no matches");
        UriInfo uriInfo = new DummyUriInfo(query);
        Response r = dataPackageManagerResource.getMatchingSubscriptions(headers, uriInfo);
        
        String xml = (String) r.getEntity();
        
        boolean containsSubscriptions = xml.contains("</subscriptions>");
        assertTrue(containsSubscriptions);
    }
    
    
    @Test
    public void testGetMatchingSubscriptionsWithBadKey() {
        Map<String, String> query = 
            Collections.singletonMap("bad", "junit");
        UriInfo uriInfo = new DummyUriInfo(query);

        Response r = dataPackageManagerResource.getMatchingSubscriptions(headers, uriInfo);
        assertEquals(400, r.getStatus());
    }
    
    
    @Test
    public void testDeleteSubscription() {     
        String id = makeSubscription();
        
        Response r = dataPackageManagerResource.deleteSubscription(headers, id);
        assertEquals(200, r.getStatus());
        
        r = dataPackageManagerResource.getSubscriptionWithId(headers, id);
        assertEquals(410, r.getStatus());
    }

    
    @Test
    public void testDeleteSubscriptionWithBadId() {
        Response r = dataPackageManagerResource.deleteSubscription(headers, "bad");
        assertEquals(400, r.getStatus());
    }
    
    
    @Test
    public void testDeleteSubscriptionNeverExisted() {
        Response r = dataPackageManagerResource.deleteSubscription(headers, "-1");
        assertEquals(404, r.getStatus());
        assertNotNull(r.getEntity());
        assertTrue(r.getEntity() instanceof String);
    }
    
    
    @Test
    public void testDeleteSubscriptionTwice() {
        String id = makeSubscription();
        
        Response r = dataPackageManagerResource.deleteSubscription(headers, id);
        assertEquals(200, r.getStatus());
        
        r = dataPackageManagerResource.deleteSubscription(headers, id);
        assertEquals(410, r.getStatus());
    }
    
    
    @Test
    public void testResponseWithSchema() {
        Response r = dataPackageManagerResource.respondWithSchema();
        assertEquals(200, r.getStatus());
        XmlUtility.xmlStringToDoc((String) r.getEntity());
    }
    
}
