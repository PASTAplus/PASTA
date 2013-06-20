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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.eventmanager.EmlSubscription;
import edu.lternet.pasta.eventmanager.SubscribedUrl;
import edu.lternet.pasta.eventmanager.EmlSubscription.SubscriptionBuilder;

@RunWith(Enclosed.class)
public class TestEmlSubscription {

    public static class TestSimply {
        
        private EmlSubscription sb;
        private String creator = "test_creator";
        private String scope = "lno";
        private Integer id = 12;
        private Integer rev = 37;
        private EmlPackageId packageId = new EmlPackageId(scope, id, rev);
        private SubscribedUrl url = new SubscribedUrl("http://test");
        
        @Before
        public void init() {
            sb = new EmlSubscription();
            sb.setCreator(creator);
            sb.setPackageId(packageId);
            sb.setUrl(url.toString());
        }

        @Test
        public void testIsActive() {
            assertTrue(sb.isActive());
        }
        
        @Test
        public void testInactivate() {
            EmlSubscription s = new EmlSubscription();
            s.inactivate();
            assertFalse(s.isActive());
        }
        
        @Test
        public void testGetSubscriptionId() {
            assertNull(sb.getSubscriptionId());
        }
        
        // Not testing getSubscriptionId() after persisting to a database.
        // It's tested in TestSubscriptionService.
        
        @Test
        public void testSetCreator() {
            assertEquals(creator, sb.getCreator());
        }
        
        @Test
        public void testSetEmlPackageIdScope() {
            assertEquals(scope, sb.getPackageId().getScope());
            assertEquals(scope, sb.getScope());
        }
        
        @Test
        public void testSetEmlPakcageIdIdentifier() {
            assertEquals(id, sb.getPackageId().getIdentifier());
            assertEquals(id, sb.getIdentifier());
        }
        
        @Test
        public void testSetEmlPackageIdRevision() {
            assertEquals(rev, sb.getPackageId().getRevision());
            assertEquals(rev, sb.getRevision());
        }
        
        @Test
        public void testSetUri() {
            assertEquals(url, sb.getUrl());
            assertEquals(url.toString(), sb.getUrl());
        }
        
        @Test(expected = IllegalArgumentException.class)
        public void testSetCreatorWithNull() {
            sb.setCreator(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testSetCreatorWithEmptyString() {
            sb.setCreator("");
        }

        @Test(expected = IllegalArgumentException.class)
        public void testSetUriWithNull() {
            sb.setUrl(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testSetEmlPackgaeIdWithNull() {
            sb.setPackageId(null);
        }
                
    }
    
    @RunWith(value=Parameterized.class)
    public static class TestIncompleteBuildStates {

        // produces a non-exhaustive set of test cases
        @Parameters
        public static Collection<?> data() {

            Object[] completeState = { "test_creator", 
                                       new EmlPackageId(null, null, null), 
                                       new SubscribedUrl("http://test") };

            Collection<Object[]> data = new LinkedList<Object[]>();

            int size = completeState.length;
            
            for (int i = 0; i < size; i ++) {

                Object[] incompleteState = { null, null, null }; 
                
                for (int j = i; j < size; j ++) {
                    
                    incompleteState[j] = completeState[j];

                    if (!Arrays.deepEquals(incompleteState, completeState)) {
                        data.add(Arrays.copyOf(incompleteState, size));
                    }
                }
                
            }

            Object[] incompleteState = { null, null, null }; 
            data.add(incompleteState);

            return data;
        }

        
        private SubscriptionBuilder sb;
        
        public TestIncompleteBuildStates(String creator,
                                         EmlPackageId packageId,
                                         SubscribedUrl uri) {
            sb = new SubscriptionBuilder();
            if (creator != null) {
                sb.setCreator(creator);
            }
            if (packageId != null) {
                sb.setEmlPackageId(packageId);
            }
            if (uri != null) {
                sb.setUrl(uri);
            }
        }
        
        @Test(expected=IllegalStateException.class)
        public void testBuildWithIncompleteState() {
            sb.build();
        }
        
    }
}
