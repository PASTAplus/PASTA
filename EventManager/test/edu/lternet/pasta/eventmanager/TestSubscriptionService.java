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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.ResourceDeletedException;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.BasicAuthToken;
import edu.lternet.pasta.eventmanager.ConfigurationListener;
import edu.lternet.pasta.eventmanager.EmlSubscription;
import edu.lternet.pasta.eventmanager.SubscribedUrl;
import edu.lternet.pasta.eventmanager.SubscriptionService;
import edu.lternet.pasta.eventmanager.EmlSubscription.SubscriptionBuilder;

public class TestSubscriptionService {

    private EntityManagerFactory emf;
    private EntityManager em;
    private SubscriptionService service;
    private EmlSubscription subscription;
    private AuthToken token;
    
    private void clearDatabase() {

        Query query = em.createQuery("DELETE FROM EmlSubscription x");
        
        em.getTransaction().begin();
        query.executeUpdate();
        em.getTransaction().commit();
    }
    
    @Before
    public void init() {

        new ConfigurationListener().setContextSpecificProperties();
        String persistenceUnit = ConfigurationListener.getJUnitPersistenceUnit();
        emf = Persistence.createEntityManagerFactory(persistenceUnit);
        em = emf.createEntityManager();
        
        clearDatabase();
        
        service = new SubscriptionService(em);
        
        String s = BasicAuthToken.makeTokenString("anonymous", "password");
        token = new BasicAuthToken(s);

        subscription = new SubscriptionBuilder()
                             .setCreator(token.getUserId())
                             .setEmlPackageId(new EmlPackageId("blah", 1, 2))
                             .setUrl(new SubscribedUrl("http://test"))
                             .build();
        
    }
    
    @After
    public void cleanUp() {
        if (em.isOpen()) {
            em.close();
        }
        emf.close();
    }
    
    @Test
    public void testClose() {
        assertTrue(em.isOpen());
        service.close();
        assertFalse(em.isOpen());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConstructorWithNull() {
        new SubscriptionService(null);
    }
    
    @Test
    public void testCreate() {
        assertNull(subscription.getSubscriptionId());
        service.create(subscription, token);
        assertNotNull(subscription.getSubscriptionId());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateWithInactive() {
        subscription.inactivate();
        service.create(subscription, token);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateWithNonNullId() {
        service.create(subscription, token);
        service.create(subscription, token);
    }
    
    @Test(expected=ResourceExistsException.class)
    public void testCreateTwice() {
        service.create(subscription, token);
        service.create(subscription.toBuilder().build(), token);
    }
    
    @Test
    public void testExists() {
        service.create(subscription, token);
        Long id = subscription.getSubscriptionId();
        assertTrue(service.exists(id));
        assertFalse(service.exists(new Long(-1)));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testExistsWithNull() {
        service.exists(null);
    }
    
    private void assertEqualSubs(EmlSubscription s1, EmlSubscription s2) {
        assertEquals(s1.getCreator(),           s2.getCreator());
        assertEquals(s1.getScope(),             s2.getScope());
        assertEquals(s1.getIdentifier(),        s2.getIdentifier());
        assertEquals(s1.getRevision(),          s2.getRevision());
        assertEquals(s1.getUrl(),               s2.getUrl());
    }
    
    @Test
    public void testGetWithId() {
        service.create(subscription, token);
        Long id = subscription.getSubscriptionId();
        EmlSubscription s = service.get(id, token);
        assertNotNull(s);
        assertTrue(s.isActive());
        assertEqualSubs(subscription, s);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetWithIdWithNull() {
        service.get(null, token);
    }
    
    @Test(expected=ResourceNotFoundException.class)
    public void testGetWithIdWithNeverExisted() {
        service.get(new Long(-1), token);
    }

    @Test(expected=ResourceDeletedException.class)
    public void testGetWithIdWithDeleted() {
        
        service.create(subscription, token);
        Long id = subscription.getSubscriptionId();

        service.delete(id, token);
        service.get(id, token);
    }
    
    @Test
    public void testGetWithPackageIdNulls() {
        
        EmlSubscription s2 = subscription.toBuilder()
                       .setEmlPackageId(new EmlPackageId("blah", null, null))
                       .build();
        
        service.create(subscription, token);
        service.create(s2, token);
        
        List<EmlSubscription> matches = 
            service.getWithPackageIdNulls(s2.toBuilder());
        
        assertNotNull(matches);
        assertEquals(1, matches.size());
        assertEqualSubs(s2, matches.get(0));
    }
    
    @Test
    public void testGetWithoutPackageIdNulls() {
        
        EmlSubscription s2 = subscription.toBuilder()
                       .setEmlPackageId(new EmlPackageId("blah", null, null))
                       .build();
        
        service.create(subscription, token);
        service.create(s2, token);
        
        List<EmlSubscription> matches = 
            service.getWithoutPackageIdNulls(s2.toBuilder(), token);
        
        assertNotNull(matches);
        assertEquals(2, matches.size());
    }
    
    @Test
    public void testDelete() {
        
        service.create(subscription, token);
        Long id = subscription.getSubscriptionId();
        
        assertTrue(service.exists(id));
        assertTrue(subscription.isActive());
        
        service.delete(id, token);
        
        assertFalse(service.exists(id));
        assertFalse(subscription.isActive());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDeleteWithNullId() {
        service.delete(null, token);
    }

    @Test(expected=ResourceNotFoundException.class)
    public void testDeleteNeverExisted() {
        service.delete(new Long(-1), token);
    }

    @Test(expected=ResourceDeletedException.class)
    public void testDeleteTwice() {
        service.create(subscription, token);
        Long id = subscription.getSubscriptionId();
        service.delete(id, token);
        service.delete(id, token);
    }
}
