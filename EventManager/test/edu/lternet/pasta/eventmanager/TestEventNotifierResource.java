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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;

import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.eventmanager.ConfigurationListener;
import edu.lternet.pasta.eventmanager.EventNotifierResource;
import edu.lternet.pasta.eventmanager.DummyCookieHttpHeaders;

public class TestEventNotifierResource {

    private AsyncHttpClient client;
    private EventNotifierResource resource;
    private HttpHeaders headers;
    private HttpHeaders altheaders;
    private String scope;
    private String identifier;
    private String revision;
    private SubscriptionRegistry subscriptionRegistry;
    
    @Before
    public void init() throws Exception {
        client = new AsyncHttpClient();
        new ConfigurationListener().setContextSpecificProperties();
        resource = new EventNotifierResource(1000, 1000);
        scope = "test"; 
        identifier = "1";
        revision = "2";
        headers = new DummyCookieHttpHeaders("junit");
        altheaders = new DummyCookieHttpHeaders("junit-other");
        makeSubscriptions();
    }
    
    @Test
    public void testNotifyOfEvent() {
        Response r = 
            resource.notifyOfEvent(headers, scope, identifier, revision);
        assertNotNull(r);
        assertEquals(200, r.getStatus());
    }

    @Test
    public void testNotifyOfEventWithAlternateUser() {
        Response r = 
            resource.notifyOfEvent(altheaders, scope, identifier, revision);
        assertNotNull(r);
        assertEquals(200, r.getStatus());
    }

    private void makeSubscriptions()  throws Exception {
        
        // For counting POSTs
        String creator = "junit";
        String url = "http://localhost:8080/eventmanagertester/requests"; 
        
        try {
            subscriptionRegistry.addSubscription(creator, null, null, null, url);
        } 
        catch (ResourceExistsException e) {
            // ignore
        }

        try {
            subscriptionRegistry.addSubscription(creator, "junit", null, null, url);
        } 
        catch (ResourceExistsException e) {
            // ignore
        }

        try {
            subscriptionRegistry.addSubscription(creator, "junit", new Integer(1), null, url);
        } 
        catch (ResourceExistsException e) {
            // ignore
        }

        try {
            subscriptionRegistry.addSubscription(creator, "junit", new Integer(1), new Integer(1), url);
        } 
        catch (ResourceExistsException e) {
            // ignore
        }

        // Making a subscription for testing timeout
        url = "http://localhost:8080/eventmanagertester/request-timeout";
        try {
            subscriptionRegistry.addSubscription(creator, "junit-timeout", new Integer(1), new Integer(2), url);
        } 
        catch (ResourceExistsException e) {
            // ignore
        }
    }

    @Test
    public void testNotifyOfEvent10Times() 
           throws IOException, InterruptedException, ExecutionException, Exception {
        
        makeSubscriptions();
        String postUrl = "http://localhost:8080/eventmanagertester/requests/resetcount";
        String getUrl = "http://localhost:8080/eventmanagertester/requests/current";

        client.preparePost(postUrl).execute();
        
        for (int i = 0; i < 10; i ++) {
            resource.notifyOfEvent(headers, "junit", "1", "2");
        }
        
        
        int received = 0;
        
        // Waiting 1800 seconds maximum for the Event Manager to make its POSTs
        for (int i = 0; i < 18000; i ++) {
            
            com.ning.http.client.Response r = 
                client.prepareGet(getUrl).execute().get();
        
            String msg = "HTTP response: " + r.getResponseBody();
            assertEquals(msg, 200, r.getStatusCode());
            
            received = Integer.parseInt(r.getResponseBody());
        
            if (received == 40) {
                break;
            }

            Thread.sleep(100);
        }

        assertEquals(40, received);
    }

    @Test
    public void testNotifyOfEventWithRequestTimeout() {
        
        long time = System.currentTimeMillis();
        
        resource.notifyOfEvent(headers, "junit-timeout", identifier, revision);
        
        // Only true if POSTing is asynchronous
        assertTrue(System.currentTimeMillis() - time < 25000);
    }
    
    @Test
    public void testNotifyOfEventWithBadScope() {
        Response r = 
            resource.notifyOfEvent(headers, "@#$%^&", identifier, revision);
        assertNotNull(r);
        assertEquals(400, r.getStatus());
        assertNotNull(r.getEntity());
        assertTrue(r.getEntity() instanceof String);
    }
    
    @Test
    public void testNotifyOfEventWithBadIdentifier() {
        Response r = 
            resource.notifyOfEvent(headers, scope, "-1", revision);
        assertNotNull(r);
        assertEquals(400, r.getStatus());
        assertNotNull(r.getEntity());
        assertTrue(r.getEntity() instanceof String);
    }
    
    @Test
    public void testNotifyOfEventWithBadRevision() {
        Response r = 
            resource.notifyOfEvent(headers, scope, identifier, "-1");
        assertNotNull(r);
        assertEquals(400, r.getStatus());
        assertNotNull(r.getEntity());
        assertTrue(r.getEntity() instanceof String);
    }
}
