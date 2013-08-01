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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.eventmanager.EmlSubscription;
import edu.lternet.pasta.eventmanager.SubscribedUrl;

@RunWith(Enclosed.class)
public class TestEmlSubscription {

    public static class TestSimply {
        
        private EmlSubscription emlSubscription;
        private String creator = "test_creator";
        private String scope = "lno";
        private Integer id = 12;
        private Integer rev = 37;
        private EmlPackageId packageId = new EmlPackageId(scope, id, rev);
        private SubscribedUrl url = new SubscribedUrl("http://test");
        
        @Before
        public void init() {
            emlSubscription = new EmlSubscription();
            emlSubscription.setCreator(creator);
            emlSubscription.setPackageId(packageId);
            emlSubscription.setUrl(url.toString());
        }

        @Test
        public void testIsActive() {
            assertTrue(emlSubscription.isActive());
        }
        
        @Test
        public void testInactivate() {
            EmlSubscription s = new EmlSubscription();
            s.inactivate();
            assertFalse(s.isActive());
        }
        
        @Test
        public void testGetSubscriptionId() {
            assertNull(emlSubscription.getSubscriptionId());
        }
        
        // Not testing getSubscriptionId() after persisting to a database.
        // It's tested in TestSubscriptionService.
        
        @Test
        public void testSetCreator() {
            assertEquals(creator, emlSubscription.getCreator());
        }
        
        @Test
        public void testSetEmlPackageIdScope() {
            assertEquals(scope, emlSubscription.getPackageId().getScope());
            assertEquals(scope, emlSubscription.getScope());
        }
        
        @Test
        public void testSetEmlPakcageIdIdentifier() {
            assertEquals(id, emlSubscription.getPackageId().getIdentifier());
            assertEquals(id, emlSubscription.getIdentifier());
        }
        
        @Test
        public void testSetEmlPackageIdRevision() {
            assertEquals(rev, emlSubscription.getPackageId().getRevision());
            assertEquals(rev, emlSubscription.getRevision());
        }
        
        @Test
        public void testSetUri() {
            assertEquals(url, emlSubscription.getUrl());
            assertEquals(url.toString(), emlSubscription.getUrl().toString());
        }
        
    }
    
}
