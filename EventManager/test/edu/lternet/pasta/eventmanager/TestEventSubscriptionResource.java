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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.eventmanager.ConfigurationListener;
import edu.lternet.pasta.eventmanager.EventSubscriptionResource;

public class TestEventSubscriptionResource {

    private EntityManagerFactory emf;
    private HttpHeaders headers;
    private EventSubscriptionResource resource;
    
    @Before
    public void init() {
        new ConfigurationListener().setContextSpecificProperties();
        clearDatabase();
        headers = new DummyCookieHttpHeaders("anonymous");
        resource = new EventSubscriptionResource(emf);
    }

    private void clearDatabase() {

        String persistenceUnit =
                ConfigurationListener.getJUnitPersistenceUnit();
        emf = Persistence.createEntityManagerFactory(persistenceUnit);
        
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("DELETE FROM EmlSubscription x");
        
        em.getTransaction().begin();
        query.executeUpdate();
        em.getTransaction().commit();
        
        em.close();
    }

    @After
    public void cleanUp() {
        emf.close();
    }
    
    @Test
    public void testValidQueryKeys() {
        
        Set<String> s = new TreeSet<String>();
        s.add("creator");
        s.add("scope");
        s.add("identifier");
        s.add("revision");
        s.add("url");

        assertEquals(s, EventSubscriptionResource.VALID_QUERY_KEYS);
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
        return getSubscriptionId(resource.createSubscription(headers, s1));
    }
    
    private String make2ndSubscription() {
        String s1 = make2ndSubscriptionXml();
        return getSubscriptionId(resource.createSubscription(headers, s1));
    }
    
    private String getSubscriptionId(Response createResponse) {
        URI uri = (URI) createResponse.getMetadata().get("Location").get(0);
        String[] parts = uri.toString().split("/");
        return parts[parts.length - 1];
    }
    
    @Test
    public void testCreateSubscription() throws URISyntaxException {
        
        String s1 = makeSubscriptionXml();
        
        Response r = resource.createSubscription(headers, s1);
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
        
        Response r = resource.createSubscription(headers, s1);
        assertEquals(201, r.getStatus());
        
        r = resource.createSubscription(headers, s1);
        assertEquals(409, r.getStatus());
    }

    @Test
    public void testCreateSubscriptionWithBadXml() throws URISyntaxException {
        Response r = resource.createSubscription(headers, "blah");
        assertEquals(400, r.getStatus());
    }
 
    private void assertContainsSubscription(String xml, String id) {
        
        assertTrue(xml.contains("<subscription type=\"eml\">"));
        assertTrue(xml.contains("<id>" + id + "</id>"));
        assertTrue(xml.contains("<creator>anonymous</creator>"));
        assertTrue(xml.contains("<packageId>test.1.2</packageId>"));
        assertTrue(xml.contains("<url>http://test</url>"));
        
    }

    @Test
    public void testGetSubscriptionWithId() {
        
        String id = makeSubscription();
        
        Response r = resource.getSubscriptionWithId(headers, id);
        assertEquals(200, r.getStatus());
        assertTrue(r.getEntity() instanceof String);
        
        String xml = (String) r.getEntity();
        
        assertContainsSubscription(xml, id);
        assertFalse(xml.contains("<subscriptions>"));
    }

    @Test
    public void testGetSubscriptionWithBadId() {
        Response r = resource.getSubscriptionWithId(headers, "bad");
        assertEquals(400, r.getStatus());
    }
    
    @Test
    public void testGetSubscriptionNeverExisted() {
        Response r = resource.getSubscriptionWithId(headers, "-1");
        assertEquals(404, r.getStatus());
        assertNotNull(r.getEntity());
        assertTrue(r.getEntity() instanceof String);
    }
    
    @Test
    public void testGetSubscriptionDeleted() {
        
        String id = makeSubscription();
        
        Response r = resource.deleteSubscription(headers, id);
        assertEquals(200, r.getStatus());
        
        r = resource.getSubscriptionWithId(headers, id);
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
        Response r = resource.getMatchingSubscriptions(headers, uriInfo);

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
        Response r = resource.getMatchingSubscriptions(headers, uriInfo);
        
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
        Response r = resource.getMatchingSubscriptions(headers, uriInfo);
        
        String xml = (String) r.getEntity();
        
        assertTrue(xml.contains("<subscriptions/>"));
    }
    
    @Test
    public void testGetMatchingSubscriptionsWithBadKey() {
        
        Map<String, String> query = 
            Collections.singletonMap("bad", "anonymous");
        UriInfo uriInfo = new DummyUriInfo(query);

        Response r = resource.getMatchingSubscriptions(headers, uriInfo);
        assertEquals(400, r.getStatus());
    }
    
    @Test
    public void testGetMatchingSubscriptionsWithBadPackageId() {
        
        Map<String, String> query = 
            Collections.singletonMap("revision", "2");
        UriInfo uriInfo = new DummyUriInfo(query);

        Response r = resource.getMatchingSubscriptions(headers, uriInfo);
        assertEquals(400, r.getStatus());
    }
    
    @Test
    public void testGetMatchingSubscriptionsWithBadUrl() {
        
        Map<String, String> query = 
            Collections.singletonMap("url", "bad");
        UriInfo uriInfo = new DummyUriInfo(query);

        Response r = resource.getMatchingSubscriptions(headers, uriInfo);
        assertEquals(400, r.getStatus());
    }

    @Test
    public void testDeleteSubscription() {
        
        String id = makeSubscription();
        
        Response r = resource.deleteSubscription(headers, id);
        assertEquals(200, r.getStatus());
        
        r = resource.getSubscriptionWithId(headers, id);
        assertEquals(410, r.getStatus());
    }

    @Test
    public void testDeleteSubscriptionWithBadId() {
        Response r = resource.deleteSubscription(headers, "bad");
        assertEquals(400, r.getStatus());
    }
    
    @Test
    public void testDeleteSubscriptionNeverExisted() {
        Response r = resource.deleteSubscription(headers, "-1");
        assertEquals(404, r.getStatus());
        assertNotNull(r.getEntity());
        assertTrue(r.getEntity() instanceof String);
    }
    
    @Test
    public void testDeleteSubscriptionTwice() {
        
        String id = makeSubscription();
        
        Response r = resource.deleteSubscription(headers, id);
        assertEquals(200, r.getStatus());
        
        r = resource.deleteSubscription(headers, id);
        assertEquals(410, r.getStatus());
    }
    
    @Test
    public void testResonseWithSchema() {
        Response r = resource.respondWithSchema();
        assertEquals(200, r.getStatus());
        XmlUtility.xmlStringToDoc((String) r.getEntity());
    }
}
